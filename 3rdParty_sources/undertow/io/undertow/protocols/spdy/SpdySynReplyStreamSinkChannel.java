/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.undertow.protocols.spdy;

import io.undertow.server.protocol.framed.SendFrameHeader;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.ImmediatePooled;
import org.xnio.ChannelListener;
import org.xnio.ChannelListeners;
import org.xnio.Pooled;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

/**
 * @author Stuart Douglas
 */
public class SpdySynReplyStreamSinkChannel extends SpdyStreamStreamSinkChannel {

    private final HeaderMap headers = new HeaderMap();

    private boolean first = true;
    private final Deflater deflater;
    private ChannelListener<SpdySynReplyStreamSinkChannel> completionListener;


    SpdySynReplyStreamSinkChannel(SpdyChannel channel, int streamId, Deflater deflater) {
        super(channel, streamId);
        this.deflater = deflater;
    }

    @Override
    protected SendFrameHeader createFrameHeaderImpl() {
        final int fcWindow = grabFlowControlBytes(getBuffer().remaining());
        if (fcWindow == 0 && getBuffer().hasRemaining()) {
            //flow control window is exhausted
            return new SendFrameHeader(getBuffer().remaining(), null);
        }
        final boolean finalFrame = isWritesShutdown() && fcWindow >= getBuffer().remaining();
        Pooled<ByteBuffer> firstHeaderBuffer = getChannel().getBufferPool().allocate();
        Pooled<ByteBuffer>[] allHeaderBuffers = null;
        ByteBuffer firstBuffer = firstHeaderBuffer.getResource();
        boolean firstFrame = false;
        if (first) {
            firstFrame = true;
            first = false;
            int firstInt = SpdyChannel.CONTROL_FRAME | (getChannel().getSpdyVersion() << 16) | 2;
            SpdyProtocolUtils.putInt(firstBuffer, firstInt);
            SpdyProtocolUtils.putInt(firstBuffer, 0); //we back fill the length
            HeaderMap headers = this.headers;

            SpdyProtocolUtils.putInt(firstBuffer, getStreamId());


            headers.remove(Headers.CONNECTION); //todo: should this be here?
            headers.remove(Headers.KEEP_ALIVE);
            headers.remove(Headers.TRANSFER_ENCODING);

            allHeaderBuffers = createHeaderBlock(firstHeaderBuffer, allHeaderBuffers, firstBuffer, headers);
        }

        Pooled<ByteBuffer> currentPooled = allHeaderBuffers == null ? firstHeaderBuffer : allHeaderBuffers[allHeaderBuffers.length - 1];
        ByteBuffer currentBuffer = currentPooled.getResource();
        int remainingInBuffer = 0;
        if (getBuffer().remaining() > 0) {
            if (fcWindow > 0) {
                //make sure we have room in the header buffer
                if(currentBuffer.remaining() < 8) {
                    allHeaderBuffers = allocateAll(allHeaderBuffers, currentPooled);
                    currentPooled = allHeaderBuffers == null ? firstHeaderBuffer : allHeaderBuffers[allHeaderBuffers.length - 1];
                    currentBuffer = currentPooled.getResource();
                }
                remainingInBuffer = getBuffer().remaining() - fcWindow;
                getBuffer().limit(getBuffer().position() + fcWindow);
                SpdyProtocolUtils.putInt(currentBuffer, getStreamId());
                SpdyProtocolUtils.putInt(currentBuffer, ((finalFrame ? SpdyChannel.FLAG_FIN : 0) << 24) + fcWindow);
            } else {
                remainingInBuffer = getBuffer().remaining();
            }
        } else if(finalFrame && !firstFrame) {
            SpdyProtocolUtils.putInt(currentBuffer, getStreamId());
            SpdyProtocolUtils.putInt(currentBuffer, SpdyChannel.FLAG_FIN  << 24);
        }
        if (allHeaderBuffers == null) {
            //only one buffer required
            currentBuffer.flip();
            return new SendFrameHeader(remainingInBuffer, currentPooled);
        } else {
            //headers were too big to fit in one buffer
            //for now we will just copy them into a big buffer
            int length = 0;
            for (int i = 0; i < allHeaderBuffers.length; ++i) {
                length += allHeaderBuffers[i].getResource().position();
                allHeaderBuffers[i].getResource().flip();
            }
            try {
                ByteBuffer newBuf = ByteBuffer.allocate(length);

                for (int i = 0; i < allHeaderBuffers.length; ++i) {
                    newBuf.put(allHeaderBuffers[i].getResource());
                }
                newBuf.flip();
                return new SendFrameHeader(remainingInBuffer, new ImmediatePooled<ByteBuffer>(newBuf));
            } finally {
                //the allocate can oome
                for (int i = 0; i < allHeaderBuffers.length; ++i) {
                    allHeaderBuffers[i].free();
                }
            }
        }
    }

    @Override
    protected Deflater getDeflater() {
        return deflater;
    }

    public HeaderMap getHeaders() {
        return headers;
    }

    @Override
    protected void handleFlushComplete(boolean finalFrame) {
        super.handleFlushComplete(finalFrame);
        if (finalFrame) {
            if (completionListener != null) {
                ChannelListeners.invokeChannelListener(this, completionListener);
            }
        }
    }

    public ChannelListener<SpdySynReplyStreamSinkChannel> getCompletionListener() {
        return completionListener;
    }

    public void setCompletionListener(ChannelListener<SpdySynReplyStreamSinkChannel> completionListener) {
        this.completionListener = completionListener;
    }
}

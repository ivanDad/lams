/*
 *Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 *
 *This program is free software; you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *USA
 *
 *http://www.gnu.org/licenses/gpl.txt
 */
package org.lamsfoundation.lams.tool.mc;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * @author Ozgur Demirtas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * Holds uploaded file information  
 */

public class McUploadedFile implements Serializable
{
	/** identifier field */
    private Long uid;
    
    /** persistent field */
    private String uuid;

    /** persistent field */
    private boolean fileOnline;
    
    /** persistent field */
    private String filename;
    
    
    private Long mcContentId;
    
    /** persistent field */
    private McContent mcContent;
    
    public McUploadedFile(){};

    /** full constructor */
    public McUploadedFile(Long uid,
    					String uuid, 
    					boolean fileOnline, 
    					String filename,
						McContent mcContent)  
    {
    	this.uid=uid;
        this.uuid = uuid;
        this.fileOnline = fileOnline;
        this.filename = filename;
        this.mcContent=mcContent;
    }

    public McUploadedFile(String uuid, 
    					boolean fileOnline, 
    					String filename,
						McContent mcContent)  
    {
        this.uuid = uuid;
        this.fileOnline = fileOnline;
        this.filename = filename;
        this.mcContent=mcContent;
    }
    
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("uuid: ", getUuid())
			.toString();
    }
  
	
	/**
	 * @return Returns the mcContent.
	 */
	public McContent getMcContent() {
		return mcContent;
	}
	/**
	 * @param mcContent The mcContent to set.
	 */
	public void setMcContent(McContent mcContent) {
		this.mcContent = mcContent;
	}
	/**
	 * @return Returns the mcContentId.
	 */
	public Long getMcContentId() {
		return mcContentId;
	}
	/**
	 * @param mcContentId The mcContentId to set.
	 */
	public void setMcContentId(Long mcContentId) {
		this.mcContentId = mcContentId;
	}
	/**
	 * @return Returns the uid.
	 */
	public Long getSubmissionId() {
		return uid;
	}
	/**
	 * @param uid The uid to set.
	 */
	public void setSubmissionId(Long uid) {
		this.uid = uid;
	}
	/**
	 * @return Returns the uuid.
	 */
	public String getUuid() {
		return uuid;
	}
	/**
	 * @param uuid The uuid to set.
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	/**
	 * @return Returns the fileOnline.
	 */
	public boolean isFileOnline() {
		return fileOnline;
	}
	/**
	 * @param fileOnline The fileOnline to set.
	 */
	public void setFileOnline(boolean fileOnline) {
		this.fileOnline = fileOnline;
	}
	/**
	 * @return Returns the uid.
	 */
	public Long getUid() {
		return uid;
	}
	/**
	 * @param uid The uid to set.
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}
	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
}

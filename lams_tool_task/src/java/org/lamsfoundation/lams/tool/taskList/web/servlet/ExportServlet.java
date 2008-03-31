/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ****************************************************************
 */

/* $$Id$$ */

package org.lamsfoundation.lams.tool.taskList.web.servlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.taskList.TaskListConstants;
import org.lamsfoundation.lams.tool.taskList.dto.TaskSummary;
import org.lamsfoundation.lams.tool.taskList.dto.TaskSummaryItem;
import org.lamsfoundation.lams.tool.taskList.model.TaskList;
import org.lamsfoundation.lams.tool.taskList.model.TaskListItemAttachment;
import org.lamsfoundation.lams.tool.taskList.model.TaskListSession;
import org.lamsfoundation.lams.tool.taskList.model.TaskListUser;
import org.lamsfoundation.lams.tool.taskList.service.ITaskListService;
import org.lamsfoundation.lams.tool.taskList.service.TaskListApplicationException;
import org.lamsfoundation.lams.tool.taskList.service.TaskListServiceProxy;
import org.lamsfoundation.lams.tool.taskList.util.TaskListToolContentHandler;
import org.lamsfoundation.lams.util.FileUtil;
import org.lamsfoundation.lams.web.servlet.AbstractExportPortfolioServlet;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Export portfolio servlet to export all shared taskList into offline HTML
 * package.
 * 
 * @author Steve.Ni
 * @author Andrey Balan
 * 
 * @version $Revision$
 */
public class ExportServlet extends AbstractExportPortfolioServlet {
	private static final long serialVersionUID = -4529093489007108143L;

	private static Logger logger = Logger.getLogger(ExportServlet.class);

	private final String FILENAME = "taskList_main.html";

	private TaskListToolContentHandler handler;

	/**
	 * {@inheritDoc}
	 */
	public String doExport(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies) {

		//initial sessionMap
		SessionMap sessionMap = new SessionMap();
		request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
		
		try {
			if (StringUtils.equals(mode, ToolAccessMode.LEARNER.toString())) {
				sessionMap.put(AttributeNames.ATTR_MODE,ToolAccessMode.LEARNER);
				learner(request, response, directoryName, cookies,sessionMap);
			} else if (StringUtils.equals(mode, ToolAccessMode.TEACHER.toString())) {
				sessionMap.put(AttributeNames.ATTR_MODE,ToolAccessMode.TEACHER);
				teacher(request, response, directoryName, cookies,sessionMap);
			}
		} catch (TaskListApplicationException e) {
			logger.error("Cannot perform export for taskList tool.");
		}

		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		writeResponseToFile(basePath + "/pages/export/exportportfolio.jsp?sessionMapID="+sessionMap.getSessionID()
				, directoryName, FILENAME, cookies);

		return FILENAME;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected String doOfflineExport(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies) {
        if (toolContentID == null && toolSessionID == null) {
            logger.error("Tool content Id or and session Id are null. Unable to activity title");
        } else {
        	ITaskListService service = TaskListServiceProxy.getTaskListService(getServletContext());
        	TaskList content = null;
            if ( toolContentID != null ) {
            	content = service.getTaskListByContentId(toolContentID);
            } else {
            	TaskListSession session=service.getTaskListSessionBySessionId(toolSessionID);
            	if ( session != null )
            		content = session.getTaskList();
            }
            if ( content != null ) {
            	activityTitle = content.getTitle();
            }
        }
        return super.doOfflineExport(request, response, directoryName, cookies);
	}

	/**
	 * Internal method to make an export for specified learner.
	 * 
	 * @param request
	 * @param response
	 * @param directoryName
	 * @param cookies
	 * @param sessionMap
	 * @throws TaskListApplicationException
	 */
	private void learner(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies, HashMap sessionMap)
			throws TaskListApplicationException {

		ITaskListService service = TaskListServiceProxy.getTaskListService(getServletContext());

		if (userID == null || toolSessionID == null) {
			String error = "Tool session Id or user Id is null. Unable to continue";
			logger.error(error);
			throw new TaskListApplicationException(error);
		}

		TaskListUser learner = service.getUserByIDAndSession(userID,toolSessionID);

		if (learner == null) {
			String error = "The user with user id " + userID + " does not exist.";
			logger.error(error);
			throw new TaskListApplicationException(error);
		}

		TaskList content = service.getTaskListBySessionId(toolSessionID);

		if (content == null) {
			String error = "The content for this activity has not been defined yet.";
			logger.error(error);
			throw new TaskListApplicationException(error);
		}
		
		List<TaskSummary> taskSummaries = service.exportForLearner(toolSessionID, learner);
		
		saveFileToLocal(taskSummaries, directoryName);
		
		sessionMap.put(TaskListConstants.ATTR_TITLE, content.getTitle());
		sessionMap.put(TaskListConstants.ATTR_TASK_SUMMARY_LIST, taskSummaries);
	}

	/**
	 * Internal method to make an export for the whole TaskList.
	 * 
	 * @param request
	 * @param response
	 * @param directoryName
	 * @param cookies
	 * @param sessionMap
	 * @throws TaskListApplicationException
	 */
	private void teacher(HttpServletRequest request, HttpServletResponse response, String directoryName, Cookie[] cookies, HashMap sessionMap)
			throws TaskListApplicationException {
		ITaskListService service = TaskListServiceProxy.getTaskListService(getServletContext());

		// check if toolContentId exists in db or not
		if (toolContentID == null) {
			String error = "Tool Content Id is missing. Unable to continue";
			logger.error(error);
			throw new TaskListApplicationException(error);
		}

		TaskList content = service.getTaskListByContentId(toolContentID);

		if (content == null) {
			String error = "Data is missing from the database. Unable to Continue";
			logger.error(error);
			throw new TaskListApplicationException(error);
		}
		
		List<TaskSummary> taskSummaries = service.exportForTeacher(toolContentID);
		
		saveFileToLocal(taskSummaries, directoryName);
		
		// put it into HTTPSession
		sessionMap.put(TaskListConstants.ATTR_TITLE, content.getTitle());
		sessionMap.put(TaskListConstants.ATTR_TASK_SUMMARY_LIST, taskSummaries);
	}

    /**
     * Create download links for every attachment. 
     * 
     * @param taskSummaries
     * @param directoryName
     */
    private void saveFileToLocal(List<TaskSummary> taskSummaries, String directoryName) {
    	handler = getToolContentHandler();
    	
    	//save all the attachments
		for (TaskSummary taskSummary : taskSummaries) {
			for (TaskSummaryItem taskSummaryItem : taskSummary.getTaskSummaryItems()) {
				for (TaskListItemAttachment attachment : taskSummaryItem.getAttachments()) {
					try{
						int idx= 1;
						String userName = attachment.getCreateBy().getLoginName();
						String localDir;
						while(true){
							localDir = FileUtil.getFullPath(directoryName,userName + "/" + idx);
							File local = new File(localDir);
							if(!local.exists()){
								local.mkdirs();
								break;
							}
							idx++;
						}
						attachment.setAttachmentLocalUrl(userName + "/" + idx + "/" + attachment.getFileUuid() + '.' + FileUtil.getFileExtension(attachment.getFileName()));
						handler.saveFile(attachment.getFileUuid(), FileUtil.getFullPath(directoryName, attachment.getAttachmentLocalUrl()));
					} catch (Exception e) {
						logger.error("Export forum topic attachment failed: " + e.toString());
					}
				}
			}
		}

//    	//save two pictures describing either the taskLisiItem is done or not
//		handler.saveFile(attachment.getFileUuid(), FileUtil.getFullPath(directoryName, "images/completeitem.gif"));
//		handler.saveFile(attachment.getFileUuid(), FileUtil.getFullPath(directoryName, "images/incompleteitem.gif"));
	}

	/**
	 * {@inheritDoc}
	 */
	private TaskListToolContentHandler getToolContentHandler() {
  	    if ( handler == null ) {
    	      WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
    	      handler = (TaskListToolContentHandler) wac.getBean(TaskListConstants.TOOL_CONTENT_HANDLER_NAME);
    	    }
    	    return handler;
	}
}

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

/* $Id$ */
package org.lamsfoundation.lams.tool.rsrc.web.action;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.tomcat.util.json.JSONArray;
import org.apache.tomcat.util.json.JSONException;
import org.apache.tomcat.util.json.JSONObject;
import org.lamsfoundation.lams.notebook.model.NotebookEntry;
import org.lamsfoundation.lams.notebook.service.CoreNotebookConstants;
import org.lamsfoundation.lams.tool.rsrc.ResourceConstants;
import org.lamsfoundation.lams.tool.rsrc.dto.GroupSummary;
import org.lamsfoundation.lams.tool.rsrc.dto.ReflectDTO;
import org.lamsfoundation.lams.tool.rsrc.dto.ItemSummary;
import org.lamsfoundation.lams.tool.rsrc.model.Resource;
import org.lamsfoundation.lams.tool.rsrc.model.ResourceSession;
import org.lamsfoundation.lams.tool.rsrc.model.ResourceUser;
import org.lamsfoundation.lams.tool.rsrc.service.IResourceService;
import org.lamsfoundation.lams.util.DateUtil;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class MonitoringAction extends Action {
    public static Logger log = Logger.getLogger(MonitoringAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws IOException, ServletException, JSONException {
	String param = mapping.getParameter();

	request.setAttribute("initialTabId", WebUtil.readLongParam(request, AttributeNames.PARAM_CURRENT_TAB, true));

	if (param.equals("summary")) {
	    return summary(mapping, form, request, response);
	}
	if (param.equals("listuser")) {
	    return listuser(mapping, form, request, response);
	}
	if (param.equals("changeItemVisibility")) {
	    return changeItemVisibility(mapping, form, request, response);
	}
	if (param.equals("getSubgridData")) {
	    return getSubgridData(mapping, form, request, response);
	}
	if (param.equals("viewReflection")) {
	    return viewReflection(mapping, form, request, response);
	}

	return mapping.findForward(ResourceConstants.ERROR);
    }

    private ActionForward summary(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	// initial Session Map
	SessionMap sessionMap = new SessionMap();
	request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	request.setAttribute(ResourceConstants.ATTR_SESSION_MAP_ID, sessionMap.getSessionID());
	// save contentFolderID into session
	sessionMap.put(AttributeNames.PARAM_CONTENT_FOLDER_ID, WebUtil.readStrParam(request,
		AttributeNames.PARAM_CONTENT_FOLDER_ID));

	Long contentId = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	IResourceService service = getResourceService();
	List<GroupSummary> groupList = service.getSummary(contentId);

	Resource resource = service.getResourceByContentId(contentId);
	
	// Create reflectList if reflection is enabled.
	if (resource.isReflectOnActivity()) {
	    List<ReflectDTO> relectList = service.getReflectList(contentId);
	    sessionMap.put(ResourceConstants.ATTR_REFLECT_LIST, relectList);
	}

	// cache into sessionMap
	sessionMap.put(ResourceConstants.ATTR_SUMMARY_LIST, groupList);
	sessionMap.put(ResourceConstants.PAGE_EDITABLE, resource.isContentInUse());
	sessionMap.put(ResourceConstants.ATTR_RESOURCE, resource);
	sessionMap.put(ResourceConstants.ATTR_TOOL_CONTENT_ID, contentId);
	sessionMap.put(ResourceConstants.ATTR_IS_GROUPED_ACTIVITY, service.isGroupedActivity(contentId));
	return mapping.findForward(ResourceConstants.SUCCESS);
    }

    private ActionForward listuser(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	Long sessionId = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID);
	Long itemUid = WebUtil.readLongParam(request, ResourceConstants.PARAM_RESOURCE_ITEM_UID);

	// get user list by given item uid
	IResourceService service = getResourceService();
	List list = service.getUserListBySessionItem(sessionId, itemUid);

	// set to request
	request.setAttribute(ResourceConstants.ATTR_USER_LIST, list);
	return mapping.findForward(ResourceConstants.SUCCESS);
    }
    
    private ActionForward getSubgridData(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws JSONException, IOException {

	Long itemUid = WebUtil.readLongParam(request, ResourceConstants.ATTR_RESOURCE_ITEM_UID);
	Long sessionId = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID);

	IResourceService service = getResourceService();
	List<ResourceUser> userList = service.getUserListBySessionItem(sessionId, itemUid);
	
	JSONArray rows = new JSONArray();
	for (ResourceUser user : userList) {
	    DateFormat timeTakenFormatter = new SimpleDateFormat("H:mm:ss");
	    DateFormat dateFormatter = new SimpleDateFormat("d-MMM-yyyy h:mm a");
	    
	    JSONArray userData = new JSONArray();
	    userData.put(user.getUserId());
	    userData.put(user.getFirstName() + " " + user.getLastName());
	    String accessDate = (user.getAccessDate() == null) ? "" : dateFormatter.format(DateUtil.convertToUTC(user
		    .getAccessDate()));
	    userData.put(accessDate);
	    String completeDate = (user.getCompleteDate() == null) ? "" : dateFormatter.format(DateUtil
		    .convertToUTC(user.getCompleteDate()));
	    userData.put(completeDate);
	    String timeTaken = (user.getTimeTaken() == null) ? "" : timeTakenFormatter.format(DateUtil
		    .convertToUTC(user.getTimeTaken()));
	    userData.put(timeTaken);
	    
	    JSONObject userRow = new JSONObject();
	    userRow.put("id", user.getUserId());
	    userRow.put("cell", userData);
	    
	    rows.put(userRow);
	}

	JSONObject responseJSON = new JSONObject();
	responseJSON.put("total", 1);
	responseJSON.put("page", 1);
	responseJSON.put("records", rows.length());
	responseJSON.put("rows", rows);
	    
	response.setContentType("application/json;charset=utf-8");
	response.getWriter().write(responseJSON.toString());
	return null;
    }

    private ActionForward changeItemVisibility(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	Long itemUid = WebUtil.readLongParam(request, ResourceConstants.PARAM_RESOURCE_ITEM_UID);
	boolean isHideItem = WebUtil.readBooleanParam(request, ResourceConstants.PARAM_IS_HIDE_ITEM);
	IResourceService service = getResourceService();
	service.setItemVisible(itemUid, !isHideItem);

	return null;
    }

    private ActionForward viewReflection(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {

	Long uid = WebUtil.readLongParam(request, ResourceConstants.ATTR_USER_UID);
	Long sessionID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_SESSION_ID);

	IResourceService service = getResourceService();
	ResourceUser user = service.getUser(uid);
	NotebookEntry notebookEntry = service.getEntry(sessionID, CoreNotebookConstants.NOTEBOOK_TOOL,
		ResourceConstants.TOOL_SIGNATURE, user.getUserId().intValue());

	ResourceSession session = service.getResourceSessionBySessionId(sessionID);

	ReflectDTO refDTO = new ReflectDTO(user);
	if (notebookEntry == null) {
	    refDTO.setFinishReflection(false);
	    refDTO.setReflect(null);
	} else {
	    refDTO.setFinishReflection(true);
	    refDTO.setReflect(notebookEntry.getEntry());
	}
	refDTO.setReflectInstrctions(session.getResource().getReflectInstructions());

	request.setAttribute("userDTO", refDTO);
	return mapping.findForward("success");
    }

    // *************************************************************************************
    // Private method
    // *************************************************************************************
    private IResourceService getResourceService() {
	WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet()
		.getServletContext());
	return (IResourceService) wac.getBean(ResourceConstants.RESOURCE_SERVICE);
    }
}

/****************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
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
package org.lamsfoundation.lams.tool.commonCartridge.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.lamsfoundation.lams.authoring.web.AuthoringConstants;
import org.lamsfoundation.lams.contentrepository.client.IToolContentHandler;
import org.lamsfoundation.lams.tool.ToolAccessMode;
import org.lamsfoundation.lams.tool.commonCartridge.CommonCartridgeConstants;
import org.lamsfoundation.lams.tool.commonCartridge.model.CommonCartridge;
import org.lamsfoundation.lams.tool.commonCartridge.model.CommonCartridgeAttachment;
import org.lamsfoundation.lams.tool.commonCartridge.model.CommonCartridgeItem;
import org.lamsfoundation.lams.tool.commonCartridge.model.CommonCartridgeUser;
import org.lamsfoundation.lams.tool.commonCartridge.service.CommonCartridgeApplicationException;
import org.lamsfoundation.lams.tool.commonCartridge.service.ICommonCartridgeService;
import org.lamsfoundation.lams.tool.commonCartridge.service.UploadCommonCartridgeFileException;
import org.lamsfoundation.lams.tool.commonCartridge.util.CommonCartridgeItemComparator;
import org.lamsfoundation.lams.tool.commonCartridge.web.form.CommonCartridgeForm;
import org.lamsfoundation.lams.tool.commonCartridge.web.form.CommonCartridgeItemForm;
import org.lamsfoundation.lams.tool.commonCartridge.web.form.CommonCartridgePedagogicalPlannerForm;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.util.FileValidatorUtil;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Andrey Balan
 */
public class AuthoringAction extends Action {
    private static final String ITEM_TYPE = "itemType";

    private static Logger log = Logger.getLogger(AuthoringAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	String param = mapping.getParameter();
	// -----------------------CommonCartridge Author function ---------------------------
	if (param.equals("start")) {
	    ToolAccessMode mode = getAccessMode(request);
	    // teacher mode "check for new" button enter.
	    if (mode != null) {
		request.setAttribute(AttributeNames.ATTR_MODE, mode.toString());
	    } else {
		request.setAttribute(AttributeNames.ATTR_MODE, ToolAccessMode.AUTHOR.toString());
	    }
	    return start(mapping, form, request, response);
	}
	if (param.equals("definelater")) {
	    // update define later flag to true
	    Long contentId = new Long(WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID));
	    ICommonCartridgeService service = getCommonCartridgeService();
	    CommonCartridge commonCartridge = service.getCommonCartridgeByContentId(contentId);

	    commonCartridge.setDefineLater(true);
	    service.saveOrUpdateCommonCartridge(commonCartridge);

	    request.setAttribute(AttributeNames.ATTR_MODE, ToolAccessMode.TEACHER.toString());
	    return start(mapping, form, request, response);
	}
	if (param.equals("initPage")) {
	    return initPage(mapping, form, request, response);
	}

	if (param.equals("updateContent")) {
	    return updateContent(mapping, form, request, response);
	}
	if (param.equals("uploadOnlineFile")) {
	    return uploadOnline(mapping, form, request, response);
	}
	if (param.equals("uploadOfflineFile")) {
	    return uploadOffline(mapping, form, request, response);
	}
	if (param.equals("deleteOnlineFile")) {
	    return deleteOnlineFile(mapping, form, request, response);
	}
	if (param.equals("deleteOfflineFile")) {
	    return deleteOfflineFile(mapping, form, request, response);
	}
	// ----------------------- Add commonCartridge item function ---------------------------
	if (param.equals("newItemInit")) {
	    return newItemlInit(mapping, form, request, response);
	}
	if (param.equals("editItemInit")) {
	    return editItemInit(mapping, form, request, response);
	}
	if (param.equals("saveOrUpdateItem")) {
	    return saveOrUpdateItem(mapping, form, request, response);
	}
	if (param.equals("selectResources")) {
	    return selectResources(mapping, form, request, response);
	}
	if (param.equals("removeItem")) {
	    return removeItem(mapping, form, request, response);
	}
	// -----------------------CommonCartridge Item Instruction function ---------------------------
	if (param.equals("initPedagogicalPlannerForm")) {
	    return initPedagogicalPlannerForm(mapping, form, request, response);
	}
	if (param.equals("createPedagogicalPlannerItem")) {
	    return createPedagogicalPlannerItem(mapping, form, request, response);
	}
	if (param.equals("saveOrUpdatePedagogicalPlannerForm")) {
	    return saveOrUpdatePedagogicalPlannerForm(mapping, form, request, response);
	}

	return mapping.findForward(CommonCartridgeConstants.ERROR);
    }

    /**
     * Remove commonCartridge item from HttpSession list and update page display. As authoring rule, all persist only
     * happen when user submit whole page. So this remove is just impact HttpSession values.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    private ActionForward removeItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {

	// get back sessionMAP
	String sessionMapID = WebUtil.readStrParam(request, CommonCartridgeConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);

	int itemIdx = NumberUtils.stringToInt(request.getParameter(CommonCartridgeConstants.PARAM_ITEM_INDEX), -1);
	if (itemIdx != -1) {
	    SortedSet<CommonCartridgeItem> commonCartridgeList = getCommonCartridgeItemList(sessionMap);
	    List<CommonCartridgeItem> rList = new ArrayList<CommonCartridgeItem>(commonCartridgeList);
	    CommonCartridgeItem item = rList.remove(itemIdx);
	    commonCartridgeList.clear();
	    commonCartridgeList.addAll(rList);
	    // add to delList
	    List delList = getDeletedCommonCartridgeItemList(sessionMap);
	    delList.add(item);
	}

	request.setAttribute(CommonCartridgeConstants.ATTR_SESSION_MAP_ID, sessionMapID);
	return mapping.findForward(CommonCartridgeConstants.SUCCESS);
    }

    /**
     * Display edit page for existed commonCartridge item.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    private ActionForward editItemInit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {

	// get back sessionMAP
	String sessionMapID = WebUtil.readStrParam(request, CommonCartridgeConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);

	int itemIdx = NumberUtils.stringToInt(request.getParameter(CommonCartridgeConstants.PARAM_ITEM_INDEX), -1);
	CommonCartridgeItem item = null;
	if (itemIdx != -1) {
	    SortedSet<CommonCartridgeItem> commonCartridgeList = getCommonCartridgeItemList(sessionMap);
	    List<CommonCartridgeItem> rList = new ArrayList<CommonCartridgeItem>(commonCartridgeList);
	    item = rList.get(itemIdx);
	    if (item != null) {
		populateItemToForm(itemIdx, item, (CommonCartridgeItemForm) form, request);
	    }
	}
	return findForward(item == null ? -1 : item.getType(), mapping);
    }

    /**
     * Display empty page for new commonCartridge item.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    private ActionForward newItemlInit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	String sessionMapID = WebUtil.readStrParam(request, CommonCartridgeConstants.ATTR_SESSION_MAP_ID);
	((CommonCartridgeItemForm) form).setSessionMapID(sessionMapID);

	short type = (short) NumberUtils.stringToInt(request.getParameter(AuthoringAction.ITEM_TYPE));
	return findForward(type, mapping);
    }

    /**
     * This method will get necessary information from commonCartridge item form and save or update into
     * <code>HttpSession</code> CommonCartridgeItemList. Notice, this save is not persist them into database, just save
     * <code>HttpSession</code> temporarily. Only they will be persist when the entire authoring page is being
     * persisted.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    private ActionForward saveOrUpdateItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	CommonCartridgeItemForm itemForm = (CommonCartridgeItemForm) form;
	ActionErrors errors = validateCommonCartridgeItem(itemForm);

	if (!errors.isEmpty()) {
	    this.addErrors(request, errors);
	    return findForward(itemForm.getItemType(), mapping);
	}

	short type = itemForm.getItemType();
	try {
	    if (type == CommonCartridgeConstants.RESOURCE_TYPE_COMMON_CARTRIDGE) {
		uploadCommonCartridge(request, itemForm);
	    } else {
		extractFormToCommonCartridgeItem(request, itemForm);
	    }
	} catch (Exception e) {
	    // any upload exception will display as normal error message rather then throw exception directly
	    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
		    CommonCartridgeConstants.ERROR_MSG_UPLOAD_FAILED, e.getMessage()));
	    if (!errors.isEmpty()) {
		this.addErrors(request, errors);
		return findForward(itemForm.getItemType(), mapping);
	    }
	}
	// set session map ID so that itemlist.jsp can get sessionMAP
	request.setAttribute(CommonCartridgeConstants.ATTR_SESSION_MAP_ID, itemForm.getSessionMapID());
	// return null to close this window
	
	if (type == CommonCartridgeConstants.RESOURCE_TYPE_COMMON_CARTRIDGE) {
	    return mapping.findForward("selectResources");
	} else {
	    return mapping.findForward(CommonCartridgeConstants.SUCCESS);
	}	
    }
    
    /**
     * 
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    private ActionForward selectResources(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	//count uploaded resources
	String sessionMapID = WebUtil.readStrParam(request, CommonCartridgeConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);
	List<CommonCartridgeItem> uploadedCartridgeResources = getUploadedCartridgeResources(sessionMap);
	int countUploadedResources = uploadedCartridgeResources.size();
	
	SortedSet<CommonCartridgeItem> items = getCommonCartridgeItemList(sessionMap);
	
	for (int i = 0; i < countUploadedResources; i++) {
	    String itemStr = request.getParameter(CommonCartridgeConstants.ATTR_ITEM + i);
	    if (StringUtils.isBlank(itemStr)) {
		continue;
	    }
	    
	    CommonCartridgeItem resource = uploadedCartridgeResources.get(i);
	    
	    String launchUrl = request.getParameter(CommonCartridgeConstants.ATTR_LAUNCH_URL + i);
	    resource.setLaunchUrl(launchUrl);
	    String secureLaunchUrl = request.getParameter(CommonCartridgeConstants.ATTR_SECURE_LAUNCH_URL + i);
	    resource.setSecureLaunchUrl(secureLaunchUrl);
	    String toolKey = request.getParameter(CommonCartridgeConstants.ATTR_REMOTE_TOOL_KEY + i);
	    resource.setKey(toolKey);
	    String toolSecret = request.getParameter(CommonCartridgeConstants.ATTR_REMOTE_TOOL_SECRET + i);
	    resource.setSecret(toolSecret);
	    String buttonText = request.getParameter(CommonCartridgeConstants.ATTR_BUTTON_TEXT + i);
	    resource.setButtonText(buttonText);
	    String isOpenUrlNewWindow = request.getParameter(CommonCartridgeConstants.ATTR_OPEN_URL_NEW_WINDOW + i);
	    resource.setOpenUrlNewWindow(isOpenUrlNewWindow != null);
	    int frameHeight = WebUtil.readIntParam(request, CommonCartridgeConstants.ATTR_FRAME_HEIGHT + i, true);
	    resource.setFrameHeight(frameHeight);
	    
	    //add selected resource to item list
	    items.add(resource);
	}
	
	
	// set session map ID so that itemlist.jsp can get sessionMAP
	request.setAttribute(CommonCartridgeConstants.ATTR_SESSION_MAP_ID, sessionMapID);
	// return null to close this window
	

	return mapping.findForward(CommonCartridgeConstants.SUCCESS);
	
    }

    /**
     * Read commonCartridge data from database and put them into HttpSession. It will redirect to init.do directly after
     * this method run successfully.
     * 
     * This method will avoid read database again and lost un-saved resouce item lost when user "refresh page",
     * 
     * @throws ServletException
     * 
     */
    private ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws ServletException {

	// save toolContentID into HTTPSession
	Long contentId = new Long(WebUtil.readLongParam(request, CommonCartridgeConstants.PARAM_TOOL_CONTENT_ID));

	// get back the commonCartridge and item list and display them on page
	ICommonCartridgeService service = getCommonCartridgeService();

	List<CommonCartridgeItem> items = null;
	CommonCartridge commonCartridge = null;
	CommonCartridgeForm commonCartridgeForm = (CommonCartridgeForm) form;

	// Get contentFolderID and save to form.
	String contentFolderID = WebUtil.readStrParam(request, AttributeNames.PARAM_CONTENT_FOLDER_ID);
	commonCartridgeForm.setContentFolderID(contentFolderID);

	// initial Session Map
	SessionMap sessionMap = new SessionMap();
	request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	commonCartridgeForm.setSessionMapID(sessionMap.getSessionID());

	try {
	    commonCartridge = service.getCommonCartridgeByContentId(contentId);
	    // if commonCartridge does not exist, try to use default content instead.
	    if (commonCartridge == null) {
		commonCartridge = service.getDefaultContent(contentId);
		if (commonCartridge.getCommonCartridgeItems() != null) {
		    items = new ArrayList<CommonCartridgeItem>(commonCartridge.getCommonCartridgeItems());
		} else {
		    items = null;
		}
	    } else {
		items = service.getAuthoredItems(commonCartridge.getUid());
	    }

	    commonCartridgeForm.setCommonCartridge(commonCartridge);

	    // initialize instruction attachment list
	    List attachmentList = getAttachmentList(sessionMap);
	    attachmentList.clear();
	    attachmentList.addAll(commonCartridge.getAttachments());
	} catch (Exception e) {
	    AuthoringAction.log.error(e);
	    throw new ServletException(e);
	}

	// init it to avoid null exception in following handling
	if (items == null) {
	    items = new ArrayList<CommonCartridgeItem>();
	} else {
	    CommonCartridgeUser commonCartridgeUser = null;
	    // handle system default question: createBy is null, now set it to current user
	    for (CommonCartridgeItem item : items) {
		if (item.getCreateBy() == null) {
		    if (commonCartridgeUser == null) {
			// get back login user DTO
			HttpSession ss = SessionManager.getSession();
			UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
			commonCartridgeUser = new CommonCartridgeUser(user, commonCartridge);
		    }
		    item.setCreateBy(commonCartridgeUser);
		}
	    }
	}
	// init commonCartridge item list
	SortedSet<CommonCartridgeItem> commonCartridgeItemList = getCommonCartridgeItemList(sessionMap);
	commonCartridgeItemList.clear();
	commonCartridgeItemList.addAll(items);

	sessionMap.put(CommonCartridgeConstants.ATTR_RESOURCE_FORM, commonCartridgeForm);
	request.getSession().setAttribute(AttributeNames.PARAM_NOTIFY_CLOSE_URL,
		request.getParameter(AttributeNames.PARAM_NOTIFY_CLOSE_URL));
	return mapping.findForward(CommonCartridgeConstants.SUCCESS);
    }

    /**
     * Display same entire authoring page content from HttpSession variable.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    private ActionForward initPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws ServletException {
	String sessionMapID = WebUtil.readStrParam(request, CommonCartridgeConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);
	CommonCartridgeForm existForm = (CommonCartridgeForm) sessionMap
		.get(CommonCartridgeConstants.ATTR_RESOURCE_FORM);

	CommonCartridgeForm commonCartridgeForm = (CommonCartridgeForm) form;
	try {
	    PropertyUtils.copyProperties(commonCartridgeForm, existForm);
	} catch (Exception e) {
	    throw new ServletException(e);
	}

	ToolAccessMode mode = getAccessMode(request);
	if (mode.isAuthor()) {
	    return mapping.findForward(CommonCartridgeConstants.SUCCESS);
	} else {
	    return mapping.findForward(CommonCartridgeConstants.DEFINE_LATER);
	}
    }

    /**
     * This method will persist all inforamtion in this authoring page, include all commonCartridge item, information
     * etc.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    private ActionForward updateContent(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	CommonCartridgeForm commonCartridgeForm = (CommonCartridgeForm) form;

	// get back sessionMAP
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(commonCartridgeForm.getSessionMapID());

	ToolAccessMode mode = getAccessMode(request);

	ActionMessages errors = validate(commonCartridgeForm, mapping, request);
	if (!errors.isEmpty()) {
	    saveErrors(request, errors);
	    if (mode.isAuthor()) {
		return mapping.findForward("author");
	    } else {
		return mapping.findForward("monitor");
	    }
	}

	CommonCartridge commonCartridge = commonCartridgeForm.getCommonCartridge();
	ICommonCartridgeService service = getCommonCartridgeService();

	// **********************************Get CommonCartridge PO*********************
	CommonCartridge commonCartridgePO = service.getCommonCartridgeByContentId(commonCartridgeForm
		.getCommonCartridge().getContentId());
	if (commonCartridgePO == null) {
	    // new CommonCartridge, create it.
	    commonCartridgePO = commonCartridge;
	    commonCartridgePO.setCreated(new Timestamp(new Date().getTime()));
	    commonCartridgePO.setUpdated(new Timestamp(new Date().getTime()));
	} else {
	    if (mode.isAuthor()) {
		Long uid = commonCartridgePO.getUid();
		PropertyUtils.copyProperties(commonCartridgePO, commonCartridge);
		// get back UID
		commonCartridgePO.setUid(uid);
	    } else { // if it is Teacher, then just update basic tab content (definelater)
		commonCartridgePO.setInstructions(commonCartridge.getInstructions());
		commonCartridgePO.setTitle(commonCartridge.getTitle());
		// change define later status
		commonCartridgePO.setDefineLater(false);
	    }
	    commonCartridgePO.setUpdated(new Timestamp(new Date().getTime()));
	}

	// *******************************Handle user*******************
	// try to get form system session
	HttpSession ss = SessionManager.getSession();
	// get back login user DTO
	UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
	CommonCartridgeUser commonCartridgeUser = service.getUserByIDAndContent(new Long(user.getUserID().intValue()),
		commonCartridgeForm.getCommonCartridge().getContentId());
	if (commonCartridgeUser == null) {
	    commonCartridgeUser = new CommonCartridgeUser(user, commonCartridgePO);
	}

	commonCartridgePO.setCreatedBy(commonCartridgeUser);

	// **********************************Handle Authoring Instruction Attachement *********************
	// merge attachment info
	// so far, attPOSet will be empty if content is existed. because PropertyUtils.copyProperties() is executed
	Set attPOSet = commonCartridgePO.getAttachments();
	if (attPOSet == null) {
	    attPOSet = new HashSet();
	}
	List attachmentList = getAttachmentList(sessionMap);
	List deleteAttachmentList = getDeletedAttachmentList(sessionMap);

	// current attachemnt in authoring instruction tab.
	Iterator iter = attachmentList.iterator();
	while (iter.hasNext()) {
	    CommonCartridgeAttachment newAtt = (CommonCartridgeAttachment) iter.next();
	    attPOSet.add(newAtt);
	}
	attachmentList.clear();

	// deleted attachment. 2 possible types: one is persist another is non-persist before.
	iter = deleteAttachmentList.iterator();
	while (iter.hasNext()) {
	    CommonCartridgeAttachment delAtt = (CommonCartridgeAttachment) iter.next();
	    iter.remove();
	    // it is an existed att, then delete it from current attachmentPO
	    if (delAtt.getUid() != null) {
		Iterator attIter = attPOSet.iterator();
		while (attIter.hasNext()) {
		    CommonCartridgeAttachment att = (CommonCartridgeAttachment) attIter.next();
		    if (delAtt.getUid().equals(att.getUid())) {
			attIter.remove();
			break;
		    }
		}
		service.deleteCommonCartridgeAttachment(delAtt.getUid());
	    }// end remove from persist value
	}

	// copy back
	commonCartridgePO.setAttachments(attPOSet);
	
	// ************************* Handle commonCartridge items *******************
	
	Set itemList = new LinkedHashSet();
	SortedSet topics = getCommonCartridgeItemList(sessionMap);
	iter = topics.iterator();
	while (iter.hasNext()) {
	    CommonCartridgeItem item = (CommonCartridgeItem) iter.next();
	    if (item != null) {
		// This flushs user UID info to message if this user is a new user.
		item.setCreateBy(commonCartridgeUser);
		itemList.add(item);
	    }
	}
	commonCartridgePO.setCommonCartridgeItems(itemList);
	// delete instructino file from database.
	List delCommonCartridgeItemList = getDeletedCommonCartridgeItemList(sessionMap);
	iter = delCommonCartridgeItemList.iterator();
	while (iter.hasNext()) {
	    CommonCartridgeItem item = (CommonCartridgeItem) iter.next();
	    iter.remove();
	    if (item.getUid() != null) {
		service.deleteCommonCartridgeItem(item.getUid());
	    }
	}
	// handle commonCartridge item attachment file:
	List delItemAttList = getDeletedItemAttachmentList(sessionMap);
	iter = delItemAttList.iterator();
	while (iter.hasNext()) {
	    CommonCartridgeItem delAtt = (CommonCartridgeItem) iter.next();
	    iter.remove();
	}

	// if miniview number is bigger than available items, then set it topics size
	if (commonCartridgePO.getMiniViewCommonCartridgeNumber() > topics.size()) {
	    commonCartridgePO.setMiniViewCommonCartridgeNumber(topics.size());
	}
	// **********************************************
	// finally persist commonCartridgePO again
	service.saveOrUpdateCommonCartridge(commonCartridgePO);

	// initialize attachmentList again
	attachmentList = getAttachmentList(sessionMap);
	attachmentList.addAll(commonCartridge.getAttachments());
	commonCartridgeForm.setCommonCartridge(commonCartridgePO);

	request.setAttribute(AuthoringConstants.LAMS_AUTHORING_SUCCESS_FLAG, Boolean.TRUE);
	if (mode.isAuthor()) {
	    return mapping.findForward("author");
	} else {
	    return mapping.findForward("monitor");
	}
    }

    /**
     * Handle upload online instruction files request.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws UploadCommonCartridgeFileException
     */
    public ActionForward uploadOnline(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws UploadCommonCartridgeFileException {
	return uploadFile(mapping, form, IToolContentHandler.TYPE_ONLINE, request);
    }

    /**
     * Handle upload offline instruction files request.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws UploadCommonCartridgeFileException
     */
    public ActionForward uploadOffline(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws UploadCommonCartridgeFileException {
	return uploadFile(mapping, form, IToolContentHandler.TYPE_OFFLINE, request);
    }

    /**
     * Common method to upload online or offline instruction files request.
     * 
     * @param mapping
     * @param form
     * @param type
     * @param request
     * @return
     * @throws UploadCommonCartridgeFileException
     */
    private ActionForward uploadFile(ActionMapping mapping, ActionForm form, String type, HttpServletRequest request)
	    throws UploadCommonCartridgeFileException {

	CommonCartridgeForm commonCartridgeForm = (CommonCartridgeForm) form;
	// get back sessionMAP
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(commonCartridgeForm.getSessionMapID());

	FormFile file;
	if (StringUtils.equals(IToolContentHandler.TYPE_OFFLINE, type)) {
	    file = commonCartridgeForm.getOfflineFile();
	} else {
	    file = commonCartridgeForm.getOnlineFile();
	}

	if (file == null || StringUtils.isBlank(file.getFileName())) {
	    return mapping.findForward(CommonCartridgeConstants.SUCCESS);
	}

	// validate file size
	ActionMessages errors = new ActionMessages();
	FileValidatorUtil.validateFileSize(file, true, errors);
	if (!errors.isEmpty()) {
	    this.saveErrors(request, errors);
	    return mapping.findForward(CommonCartridgeConstants.SUCCESS);
	}

	ICommonCartridgeService service = getCommonCartridgeService();
	// upload to repository
	CommonCartridgeAttachment att = service.uploadInstructionFile(file, type);
	// handle session value
	List attachmentList = getAttachmentList(sessionMap);
	List deleteAttachmentList = getDeletedAttachmentList(sessionMap);
	// first check exist attachment and delete old one (if exist) to deletedAttachmentList
	Iterator iter = attachmentList.iterator();
	CommonCartridgeAttachment existAtt;
	while (iter.hasNext()) {
	    existAtt = (CommonCartridgeAttachment) iter.next();
	    if (StringUtils.equals(existAtt.getFileName(), att.getFileName())
		    && StringUtils.equals(existAtt.getFileType(), att.getFileType())) {
		// if there is same name attachment, delete old one
		deleteAttachmentList.add(existAtt);
		iter.remove();
		break;
	    }
	}
	// add to attachmentList
	attachmentList.add(att);

	return mapping.findForward(CommonCartridgeConstants.SUCCESS);

    }

    /**
     * Delete offline instruction file from current CommonCartridge authoring page.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward deleteOfflineFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return deleteFile(mapping, request, response, form, IToolContentHandler.TYPE_OFFLINE);
    }

    /**
     * Delete online instruction file from current CommonCartridge authoring page.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward deleteOnlineFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	return deleteFile(mapping, request, response, form, IToolContentHandler.TYPE_ONLINE);
    }

    /**
     * General method to delete file (online or offline)
     * 
     * @param mapping
     * @param request
     * @param response
     * @param form
     * @param type
     * @return
     */
    private ActionForward deleteFile(ActionMapping mapping, HttpServletRequest request, HttpServletResponse response,
	    ActionForm form, String type) {
	Long versionID = new Long(WebUtil.readLongParam(request, CommonCartridgeConstants.PARAM_FILE_VERSION_ID));
	Long uuID = new Long(WebUtil.readLongParam(request, CommonCartridgeConstants.PARAM_FILE_UUID));

	// get back sessionMAP
	String sessionMapID = WebUtil.readStrParam(request, CommonCartridgeConstants.ATTR_SESSION_MAP_ID);
	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(sessionMapID);

	// handle session value
	List attachmentList = getAttachmentList(sessionMap);
	List deleteAttachmentList = getDeletedAttachmentList(sessionMap);
	// first check exist attachment and delete old one (if exist) to deletedAttachmentList
	Iterator iter = attachmentList.iterator();
	CommonCartridgeAttachment existAtt;
	while (iter.hasNext()) {
	    existAtt = (CommonCartridgeAttachment) iter.next();
	    if (existAtt.getFileUuid().equals(uuID) && existAtt.getFileVersionId().equals(versionID)) {
		// if there is same name attachment, delete old one
		deleteAttachmentList.add(existAtt);
		iter.remove();
	    }
	}

	request.setAttribute(CommonCartridgeConstants.ATTR_FILE_TYPE_FLAG, type);
	request.setAttribute(CommonCartridgeConstants.ATTR_SESSION_MAP_ID, sessionMapID);
	return mapping.findForward(CommonCartridgeConstants.SUCCESS);

    }

    // *************************************************************************************
    // Private method
    // *************************************************************************************
    /**
     * Return CommonCartridgeService bean.
     */
    private ICommonCartridgeService getCommonCartridgeService() {
	WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet()
		.getServletContext());
	return (ICommonCartridgeService) wac.getBean(CommonCartridgeConstants.RESOURCE_SERVICE);
    }

    /**
     * @param request
     * @return
     */
    private List getAttachmentList(SessionMap sessionMap) {
	return getListFromSession(sessionMap, CommonCartridgeConstants.ATT_ATTACHMENT_LIST);
    }

    /**
     * @param request
     * @return
     */
    private List getDeletedAttachmentList(SessionMap sessionMap) {
	return getListFromSession(sessionMap, CommonCartridgeConstants.ATTR_DELETED_ATTACHMENT_LIST);
    }

    /**
     * List items from fresh uploaded cartridge.
     * 
     * @param request
     * @return
     */
    private List<CommonCartridgeItem> getUploadedCartridgeResources(SessionMap sessionMap) {
	return getListFromSession(sessionMap, "uploadedCartridgeResources");
    }
    
    /**
     * List save current commonCartridge items.
     * 
     * @param request
     * @return
     */
    private SortedSet<CommonCartridgeItem> getCommonCartridgeItemList(SessionMap sessionMap) {
	SortedSet<CommonCartridgeItem> list = (SortedSet<CommonCartridgeItem>) sessionMap
		.get(CommonCartridgeConstants.ATTR_RESOURCE_ITEM_LIST);
	if (list == null) {
	    list = new TreeSet<CommonCartridgeItem>(new CommonCartridgeItemComparator());
	    sessionMap.put(CommonCartridgeConstants.ATTR_RESOURCE_ITEM_LIST, list);
	}
	return list;
    }

    /**
     * List save deleted commonCartridge items, which could be persisted or non-persisted items.
     * 
     * @param request
     * @return
     */
    private List getDeletedCommonCartridgeItemList(SessionMap sessionMap) {
	return getListFromSession(sessionMap, CommonCartridgeConstants.ATTR_DELETED_RESOURCE_ITEM_LIST);
    }

    /**
     * If a commonCartridge item has attahment file, and the user edit this item and change the attachment to new file,
     * then the old file need be deleted when submitting the whole authoring page. Save the file uuid and version id
     * into CommonCartridgeItem object for temporarily use.
     * 
     * @param request
     * @return
     */
    private List getDeletedItemAttachmentList(SessionMap sessionMap) {
	return getListFromSession(sessionMap, CommonCartridgeConstants.ATTR_DELETED_RESOURCE_ITEM_ATTACHMENT_LIST);
    }

    /**
     * Get <code>java.util.List</code> from HttpSession by given name.
     * 
     * @param request
     * @param name
     * @return
     */
    private List getListFromSession(SessionMap sessionMap, String name) {
	List list = (List) sessionMap.get(name);
	if (list == null) {
	    list = new ArrayList();
	    sessionMap.put(name, list);
	}
	return list;
    }

    /**
     * Get back relative <code>ActionForward</code> from request.
     * 
     * @param type
     * @param mapping
     * @return
     */
    private ActionForward findForward(short type, ActionMapping mapping) {
	ActionForward forward;
	switch (type) {
	case CommonCartridgeConstants.RESOURCE_TYPE_BASIC_LTI:
	    forward = mapping.findForward("basiclti");
	    break;
	case CommonCartridgeConstants.RESOURCE_TYPE_COMMON_CARTRIDGE:
	    forward = mapping.findForward("commoncartridge");
	    break;
	default:
	    forward = null;
	    break;
	}
	return forward;
    }

    /**
     * This method will populate commonCartridge item information to its form for edit use.
     * 
     * @param itemIdx
     * @param item
     * @param form
     * @param request
     */
    private void populateItemToForm(int itemIdx, CommonCartridgeItem item, CommonCartridgeItemForm form,
	    HttpServletRequest request) {
	form.setDescription(item.getDescription());
	form.setTitle(item.getTitle());
	if (itemIdx >= 0) {
	    form.setItemIndex(new Integer(itemIdx).toString());
	}
	
	if (StringUtils.isBlank(item.getLaunchUrl()) && StringUtils.isNotBlank(item.getSecureLaunchUrl())) {
	    form.setUrl(item.getSecureLaunchUrl());
	} else {
	    form.setUrl(item.getLaunchUrl());
	}
	form.setKey(item.getKey());
	form.setSecret(item.getSecret());
	form.setCustomStr(item.getCustomStr());
	form.setButtonText(item.getButtonText());
	form.setOpenUrlNewWindow(item.isOpenUrlNewWindow());
	form.setFrameHeight(item.getFrameHeight());

	// FOR requirment from LDEV-754
	// add extra blank line for instructions
	// for(int idx=0;idx<INIT_INSTRUCTION_COUNT;idx++){
	// instructions.add("");
	// }
	if (item.getFileUuid() != null) {
	    form.setFileUuid(item.getFileUuid());
	    form.setFileVersionId(item.getFileVersionId());
	    form.setFileName(item.getFileName());
	    form.setHasFile(true);
	} else {
	    form.setHasFile(false);
	}
    }

    /**
     * Uploads common cartridge and parses it to commonCartridge items.
     * 
     * @param request
     * @param itemForm
     * @throws UploadCommonCartridgeFileException 
     */
    private void uploadCommonCartridge(HttpServletRequest request, CommonCartridgeItemForm itemForm) throws UploadCommonCartridgeFileException {
		
	// if the item is edit (not new add) then the getFile may return null
	// it may throw exception, so put it as first, to avoid other invlidate update:
	List<CommonCartridgeItem> items = null;
	if (itemForm.getFile() != null) {
	    try {
		CommonCartridgeItem itemTemp = new CommonCartridgeItem();
		ICommonCartridgeService service = getCommonCartridgeService();
		items = service.uploadCommonCartridgeFile(itemTemp, itemForm.getFile());
	    } catch (UploadCommonCartridgeFileException e) {
		throw e;
	    }
	}
	
	for (CommonCartridgeItem item : items) {
	    item.setCreateDate(new Timestamp(new Date().getTime()));
	    item.setType(CommonCartridgeConstants.RESOURCE_TYPE_BASIC_LTI);
	    item.setCreateByAuthor(true);
	    item.setHide(false);
	    //item.setDescription(itemForm.getDescription());
	}

	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(itemForm.getSessionMapID());
	List<CommonCartridgeItem> uploadedCartridgeResources = getUploadedCartridgeResources(sessionMap);
	uploadedCartridgeResources.clear();
	uploadedCartridgeResources.addAll(items);
    }

    /**
     * Extract web from content to commonCartridge item.
     * 
     * @param request
     * @param itemForm
     * @throws CommonCartridgeApplicationException
     */
    private void extractFormToCommonCartridgeItem(HttpServletRequest request, CommonCartridgeItemForm itemForm)
	    throws Exception {
	/*
	 * BE CAREFUL: This method will copy nessary info from request form to a old or new CommonCartridgeItem
	 * instance. It gets all info EXCEPT CommonCartridgeItem.createDate and CommonCartridgeItem.createBy, which need
	 * be set when persisting this commonCartridge item.
	 */

	SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(itemForm.getSessionMapID());
	// check whether it is "edit(old item)" or "add(new item)"
	SortedSet<CommonCartridgeItem> commonCartridgeList = getCommonCartridgeItemList(sessionMap);
	int itemIdx = NumberUtils.stringToInt(itemForm.getItemIndex(), -1);
	CommonCartridgeItem item = null;

	if (itemIdx == -1) { // add
	    item = new CommonCartridgeItem();
	    item.setCreateDate(new Timestamp(new Date().getTime()));
	    commonCartridgeList.add(item);
	} else { // edit
	    List<CommonCartridgeItem> rList = new ArrayList<CommonCartridgeItem>(commonCartridgeList);
	    item = rList.get(itemIdx);
	}
	short type = itemForm.getItemType();
	item.setType(itemForm.getItemType());
	
	item.setTitle(itemForm.getTitle());
	item.setCreateByAuthor(true);
	item.setHide(false);

	if (type == CommonCartridgeConstants.RESOURCE_TYPE_BASIC_LTI) {
	    
	    if (StringUtils.isBlank(item.getLaunchUrl()) && StringUtils.isNotBlank(item.getSecureLaunchUrl())) {
		item.setSecureLaunchUrl(itemForm.getUrl());
	    } else {
		item.setLaunchUrl(itemForm.getUrl());
	    }
	    
	    item.setKey(itemForm.getKey());
	    item.setSecret(itemForm.getSecret());
	    item.setButtonText(itemForm.getButtonText());
	    item.setOpenUrlNewWindow(itemForm.isOpenUrlNewWindow());
	    item.setFrameHeight(itemForm.getFrameHeight());
	    item.setCustomStr(itemForm.getCustomStr());
	}
	// if(type == CommonCartridgeConstants.RESOURCE_TYPE_WEBSITE
	// ||itemForm.getItemType() == CommonCartridgeConstants.RESOURCE_TYPE_LEARNING_OBJECT){
	item.setDescription(itemForm.getDescription());
	// }

    }

    /**
     * Vaidate commonCartridge item regards to their type (url/file/learning object/website zip file)
     * 
     * @param itemForm
     * @return
     */
    private ActionErrors validateCommonCartridgeItem(CommonCartridgeItemForm itemForm) {
	ActionErrors errors = new ActionErrors();

	if (itemForm.getItemType() == CommonCartridgeConstants.RESOURCE_TYPE_BASIC_LTI) {
	    if (StringUtils.isBlank(itemForm.getTitle())) {
		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
			CommonCartridgeConstants.ERROR_MSG_TITLE_BLANK));
	    }	    
	    
	    if (StringUtils.isBlank(itemForm.getUrl())) {
		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
			CommonCartridgeConstants.ERROR_MSG_URL_BLANK));
		// URL validation: Commom URL validate(1.3.0) work not very well: it can not support http://
		// address:port format!!!
		// UrlValidator validator = new UrlValidator();
		// if(!validator.isValid(itemForm.getUrl()))
		// errors.add(ActionMessages.GLOBAL_MESSAGE,new
		// ActionMessage(CommonCartridgeConstants.ERROR_MSG_INVALID_URL));
	    }    
	}
	// if(itemForm.getItemType() == CommonCartridgeConstants.RESOURCE_TYPE_WEBSITE
	// ||itemForm.getItemType() == CommonCartridgeConstants.RESOURCE_TYPE_LEARNING_OBJECT){
	// if(StringUtils.isBlank(itemForm.getDescription()))
	// errors.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage(CommonCartridgeConstants.ERROR_MSG_DESC_BLANK));
	// }
	if (itemForm.getItemType() == CommonCartridgeConstants.RESOURCE_TYPE_COMMON_CARTRIDGE) {
	    // validate item size
	    FileValidatorUtil.validateFileSize(itemForm.getFile(), true, errors);
	    // for edit validate: file already exist
	    if (!itemForm.isHasFile()
		    && (itemForm.getFile() == null || StringUtils.isEmpty(itemForm.getFile().getFileName()))) {
		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
			CommonCartridgeConstants.ERROR_MSG_FILE_BLANK));
	    }
	}
	return errors;
    }

    /**
     * Get ToolAccessMode from HttpRequest parameters. Default value is AUTHOR mode.
     * 
     * @param request
     * @return
     */
    private ToolAccessMode getAccessMode(HttpServletRequest request) {
	ToolAccessMode mode;
	String modeStr = request.getParameter(AttributeNames.ATTR_MODE);
	if (StringUtils.equalsIgnoreCase(modeStr, ToolAccessMode.TEACHER.toString())) {
	    mode = ToolAccessMode.TEACHER;
	} else {
	    mode = ToolAccessMode.AUTHOR;
	}
	return mode;
    }

    private ActionMessages validate(CommonCartridgeForm commonCartridgeForm, ActionMapping mapping,
	    HttpServletRequest request) {
	ActionMessages errors = new ActionMessages();
	// if (StringUtils.isBlank(commonCartridgeForm.getCommonCartridge().getTitle())) {
	// ActionMessage error = new ActionMessage("error.resource.item.title.blank");
	// errors.add(ActionMessages.GLOBAL_MESSAGE, error);
	// }

	// define it later mode(TEACHER) skip below validation.
	String modeStr = request.getParameter(AttributeNames.ATTR_MODE);
	if (StringUtils.equals(modeStr, ToolAccessMode.TEACHER.toString())) {
	    return errors;
	}

	// Some other validation outside basic Tab.

	return errors;
    }

    public ActionForward initPedagogicalPlannerForm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) {
	CommonCartridgePedagogicalPlannerForm plannerForm = (CommonCartridgePedagogicalPlannerForm) form;
	Long toolContentID = WebUtil.readLongParam(request, AttributeNames.PARAM_TOOL_CONTENT_ID);
	CommonCartridge taskList = getCommonCartridgeService().getCommonCartridgeByContentId(toolContentID);
	String command = WebUtil.readStrParam(request, AttributeNames.PARAM_COMMAND, true);
	if (command == null) {
	    plannerForm.fillForm(taskList);
	    String contentFolderId = WebUtil.readStrParam(request, AttributeNames.PARAM_CONTENT_FOLDER_ID);
	    plannerForm.setContentFolderID(contentFolderId);
	    return mapping.findForward(CommonCartridgeConstants.SUCCESS);
	} else {
	    try {
		String onlineInstructions = taskList.getOnlineInstructions();
		response.setContentType("text/html;charset=utf-8");
		PrintWriter writer = response.getWriter();

		if (AttributeNames.COMMAND_CHECK_EDITING_ADVICE.equals(command)) {
		    Integer activityIndex = WebUtil.readIntParam(request, AttributeNames.PARAM_ACTIVITY_INDEX);
		    String responseText = (StringUtils.isEmpty(taskList.getOnlineInstructions()) ? "NO" : "OK") + '&'
			    + activityIndex;
		    writer.print(responseText);

		} else if (AttributeNames.COMMAND_GET_EDITING_ADVICE.equals(command)) {
		    writer.print(onlineInstructions);
		}
	    } catch (IOException e) {
		AuthoringAction.log.error(e);
	    }
	    return null;
	}

    }

    public ActionForward saveOrUpdatePedagogicalPlannerForm(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) throws IOException {
	CommonCartridgePedagogicalPlannerForm plannerForm = (CommonCartridgePedagogicalPlannerForm) form;
	ActionMessages errors = plannerForm.validate();
	if (errors.isEmpty()) {
	    CommonCartridge taskList = getCommonCartridgeService().getCommonCartridgeByContentId(
		    plannerForm.getToolContentID());
	    taskList.setInstructions(plannerForm.getInstructions());

	    int itemIndex = 0;
	    String title = null;
	    CommonCartridgeItem commonCartridgeItem = null;
	    List<CommonCartridgeItem> newItems = new LinkedList<CommonCartridgeItem>();
	    Set<CommonCartridgeItem> commonCartridgeItems = taskList.getCommonCartridgeItems();
	    Iterator<CommonCartridgeItem> taskListItemIterator = commonCartridgeItems.iterator();
	    // We need to reverse the order, since the items are delivered newest-first
	    LinkedList<CommonCartridgeItem> reversedCommonCartridgeItems = new LinkedList<CommonCartridgeItem>();
	    while (taskListItemIterator.hasNext()) {
		reversedCommonCartridgeItems.addFirst(taskListItemIterator.next());
	    }
	    taskListItemIterator = reversedCommonCartridgeItems.iterator();
	    do {
		title = plannerForm.getTitle(itemIndex);
		if (StringUtils.isEmpty(title)) {
		    plannerForm.removeItem(itemIndex);
		} else {
		    if (taskListItemIterator.hasNext()) {
			commonCartridgeItem = taskListItemIterator.next();
		    } else {
			commonCartridgeItem = new CommonCartridgeItem();
			commonCartridgeItem.setCreateByAuthor(true);
			Date currentDate = new Date();
			commonCartridgeItem.setCreateDate(currentDate);

			HttpSession session = SessionManager.getSession();
			UserDTO user = (UserDTO) session.getAttribute(AttributeNames.USER);
			CommonCartridgeUser taskListUser = getCommonCartridgeService().getUserByIDAndContent(
				new Long(user.getUserID().intValue()), plannerForm.getToolContentID());
			commonCartridgeItem.setCreateBy(taskListUser);

			newItems.add(commonCartridgeItem);
		    }
		    commonCartridgeItem.setTitle(title);
		    Short type = plannerForm.getType(itemIndex);
		    commonCartridgeItem.setType(type);
		    boolean hasFile = commonCartridgeItem.getFileUuid() != null;
		    if (type.equals(CommonCartridgeConstants.RESOURCE_TYPE_BASIC_LTI)) {
			commonCartridgeItem.setUrl(plannerForm.getUrl(itemIndex));
			if (hasFile) {
			    commonCartridgeItem.setFileName(null);
			    commonCartridgeItem.setFileUuid(null);
			    commonCartridgeItem.setFileVersionId(null);
			    commonCartridgeItem.setFileType(null);
			}
		    } else if (type.equals(CommonCartridgeConstants.RESOURCE_TYPE_COMMON_CARTRIDGE)) {
			FormFile file = plannerForm.getFile(itemIndex);
			commonCartridgeItem.setUrl(null);
			ICommonCartridgeService service = getCommonCartridgeService();
			if (file != null) {
			    try {
				if (hasFile) {
				    // delete the old file
				    service.deleteFromRepository(commonCartridgeItem.getFileUuid(),
					    commonCartridgeItem.getFileVersionId());
				}
				service.uploadCommonCartridgeFile(commonCartridgeItem, file);
			    } catch (Exception e) {
				AuthoringAction.log.error(e);
				ActionMessage error = new ActionMessage("error.msg.io.exception");
				errors.add(ActionMessages.GLOBAL_MESSAGE, error);
				saveErrors(request, errors);
				plannerForm.setValid(false);
				return mapping.findForward(CommonCartridgeConstants.SUCCESS);
			    }
			}
			plannerForm.setFileName(itemIndex, commonCartridgeItem.getFileName());
			plannerForm.setFileUuid(itemIndex, commonCartridgeItem.getFileUuid());
			plannerForm.setFileVersion(itemIndex, commonCartridgeItem.getFileVersionId());
			plannerForm.setFile(itemIndex, null);
		    }
		    itemIndex++;
		}

	    } while (title != null);
	    // we need to clear it now, otherwise we get Hibernate error (item re-saved by cascade)
	    taskList.getCommonCartridgeItems().clear();
	    while (taskListItemIterator.hasNext()) {
		commonCartridgeItem = taskListItemIterator.next();
		taskListItemIterator.remove();
		getCommonCartridgeService().deleteCommonCartridgeItem(commonCartridgeItem.getUid());
	    }
	    reversedCommonCartridgeItems.addAll(newItems);

	    taskList.getCommonCartridgeItems().addAll(reversedCommonCartridgeItems);
	    getCommonCartridgeService().saveOrUpdateCommonCartridge(taskList);
	} else {
	    saveErrors(request, errors);
	}
	return mapping.findForward(CommonCartridgeConstants.SUCCESS);
    }

    public ActionForward createPedagogicalPlannerItem(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	CommonCartridgePedagogicalPlannerForm plannerForm = (CommonCartridgePedagogicalPlannerForm) form;
	int insertIndex = plannerForm.getItemCount();
	plannerForm.setTitle(insertIndex, "");
	plannerForm.setType(insertIndex,
		new Short(request.getParameter(CommonCartridgeConstants.ATTR_ADD_RESOURCE_TYPE)));
	plannerForm.setUrl(insertIndex, null);
	plannerForm.setFileName(insertIndex, null);
	plannerForm.setFile(insertIndex, null);
	plannerForm.setFileUuid(insertIndex, null);
	plannerForm.setFileVersion(insertIndex, null);
	return mapping.findForward(CommonCartridgeConstants.SUCCESS);
    }
}

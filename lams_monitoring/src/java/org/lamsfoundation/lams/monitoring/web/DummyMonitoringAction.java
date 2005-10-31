/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ***********************************************************************/

package org.lamsfoundation.lams.monitoring.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.lamsfoundation.lams.lesson.Lesson;
import org.lamsfoundation.lams.monitoring.service.IMonitoringService;
import org.lamsfoundation.lams.monitoring.service.MonitoringServiceProxy;
import org.lamsfoundation.lams.tool.exception.LamsToolServiceException;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.WebUtil;
import org.lamsfoundation.lams.web.action.LamsDispatchAction;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * <p>An action servlet to support the dummy monitoring page dummy.jsp.</p>
 * 
 * ----------------XDoclet Tags--------------------
 * 
 * @struts:action path="/dummy" 
 * 				  name="DummyForm"
 *                parameter="method" 
 *                validate="false"
 * @struts.action-exception key="error.system.monitor" scope="request"
 *                          type="org.lamsfoundation.lams.monitoring.service.MonitoringServiceException"
 *                          path=".systemError"
 * 							handler="org.lamsfoundation.lams.web.util.CustomStrutsExceptionHandler"
 * @struts:action-forward name="dummy" path="/dummy.jsp"
 * 
 * ----------------XDoclet Tags--------------------
 */
public class DummyMonitoringAction extends LamsDispatchAction
{
    //---------------------------------------------------------------------
    // Instance variables
    //---------------------------------------------------------------------
	private static Logger log = Logger.getLogger(DummyMonitoringAction.class);
	
	private IMonitoringService monitoringService;
    private IUserManagementService usermanageService;

    //---------------------------------------------------------------------
    // Class level constants - session attributes
    //---------------------------------------------------------------------
	// input parameters
    // output parameters
    private static final String DUMMY_FORWARD = "dummy";
    private static final String LESSONS_PARAMETER = "lessons";
    
    
    private static final Integer ORGANIZATION_ID = new Integer(1);
    
    
    //---------------------------------------------------------------------
    // Struts Dispatch Method
    //---------------------------------------------------------------------
    /**
     * The Struts dispatch method that initialised and start a lesson.
     * It will start a lesson with the current user as the staff and learner.
     * 
     * @param mapping An ActionMapping class that will be used by the Action class to tell
     * the ActionServlet where to send the end-user.
     *
     * @param form The ActionForm class that will contain any data submitted
     * by the end-user via a form.
     * @param request A standard Servlet HttpServletRequest class.
     * @param response A standard Servlet HttpServletResponse class.
     * @return An ActionForward class that will be returned to the ActionServlet indicating where
     *         the user is to go next.
     * @throws IOException
     * @throws ServletException
     */
    
    public ActionForward startLesson(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	
    	setupServices();
    	
    	// set up all the data needed
    	DummyForm dummyForm = (DummyForm) form;
    	Long ldId = dummyForm.getLearningDesignId();
    	if ( ldId == null )
    		throw new IOException("Learning design id must be set");
    	
        String title = dummyForm.getTitle();
        if ( title == null ) title = "lesson";
        String desc = dummyForm.getDesc(); 
        if ( desc == null ) desc = "description";

        User user = getUser();
        Organisation organisation = usermanageService.getOrganisationById(ORGANIZATION_ID);

        // initialize the lesson
        Lesson testLesson = monitoringService.initializeLesson(title,desc,ldId.longValue(),user);

        // create the lesson class
        LinkedList learners = new LinkedList();
        learners.add(user);
        LinkedList staffs = new LinkedList();
        staffs.add(user);
        testLesson = monitoringService.createLessonClassForLesson(testLesson.getLessonId().longValue(),
        		organisation,
				learners,
                staffs);

        // start the lesson.
        this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
        monitoringService.startlesson(testLesson.getLessonId().longValue());
        
        // now got back to the dummy screen with an updated list of lessons to the user 
        return unspecified(mapping, form, request, response);

    }

    /** Default method for this action. Gets a list of all the current lessons for this user and forwards to dummy.jsp */
    public ActionForward unspecified(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)throws IOException{
    	setupServices();

    	User user = getUser();
    	List lessons = monitoringService.getAllLessons(user.getUserId());
	    request.getSession().setAttribute(LESSONS_PARAMETER,lessons);
    	return mapping.findForward(DUMMY_FORWARD);
    }

    private void setupServices() {
    	
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet().getServletContext());
    	this.usermanageService= (IUserManagementService) wac.getBean("userManagementService");

    }
    private User getUser() throws IOException {
    	HttpSession ss = SessionManager.getSession();
    	UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
    	if ( user != null ) {
    		return usermanageService.getUserById(user.getUserID());
    	}
    	throw new IOException("Unable to get user. User in session manager is "+user);
    }
    
    public ActionForward gotoLearnerActivityURL(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException,LamsToolServiceException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Integer userID = new Integer(WebUtil.readIntParam(request,AttributeNames.PARAM_USER_ID));
    	Long activityID = new Long(WebUtil.readLongParam(request,AttributeNames.PARAM_ACTIVITY_ID));
    	String wddxPacket = monitoringService.getLearnerActivityURL(activityID,userID);
    	String url = extractURL(wddxPacket);
    	response.sendRedirect(response.encodeRedirectURL(url));
    	return null;
    }

    public ActionForward gotoMonitoringActivityURL(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException,LamsToolServiceException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long activityID = new Long(WebUtil.readLongParam(request,AttributeNames.PARAM_ACTIVITY_ID));
    	String wddxPacket = monitoringService.getActivityMonitorURL(activityID);
    	String url = extractURL(wddxPacket);
    	response.sendRedirect(response.encodeRedirectURL(url));
    	return null;
    }
    public ActionForward gotoDefineLaterActivityURL(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException,LamsToolServiceException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long activityID = new Long(WebUtil.readLongParam(request,AttributeNames.PARAM_ACTIVITY_ID));
    	String wddxPacket = monitoringService.getActivityDefineLaterURL(activityID);
    	String url = extractURL(wddxPacket);
    	response.sendRedirect(response.encodeRedirectURL(url));
    	return null;
    }

    /**
	 * @param wddxPacket
	 * @return
	 */
	private String extractURL(String wddxPacket) {
		String url = null;
    	String previousString = "<var name='activityURL'><string>";
    	int index = wddxPacket.indexOf(previousString);
    	if ( index > -1 && index+previousString.length() < wddxPacket.length() ) {
    		url = wddxPacket.substring(index+previousString.length());
    		index = url.indexOf("</string>");
    		url = url.substring(0,index);
    	}
    	url = WebUtil.convertToFullURL(url);
		return url;
	}

 /*    public ActionForward getLessonDetails(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	String wddxPacket = monitoringService.getLessonDetails(lessonID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward getLessonLearners(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	String wddxPacket = monitoringService.getLessonLearners(lessonID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward getLearningDesignDetails(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	String wddxPacket = monitoringService.getLearningDesignDetails(lessonID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward getAllLearnersProgress(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	String wddxPacket = monitoringService.getAllLearnersProgress(lessonID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward getAllContributeActivities(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	String wddxPacket = monitoringService.getAllContributeActivities(lessonID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward getActivityContributionURL(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());    	
    	Long activityID = new Long(WebUtil.readLongParam(request,"activityID"));
    	String wddxPacket = monitoringService.getActivityContributionURL(activityID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward moveLesson(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	Integer userID = new Integer(WebUtil.readIntParam(request,"userID"));
    	Integer targetWorkspaceFolderID = new Integer(WebUtil.readIntParam(request,"folderID"));
    	String wddxPacket = monitoringService.moveLesson(lessonID,targetWorkspaceFolderID,userID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    public ActionForward renameLesson(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException{
    	this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
    	Long lessonID = new Long(WebUtil.readLongParam(request,"lessonID"));
    	Integer userID = new Integer(WebUtil.readIntParam(request,"userID"));
    	String name = WebUtil.readStrParam(request,"name"); 
    	String wddxPacket = monitoringService.renameLesson(lessonID,name,userID);
    	return outputPacket(mapping, request, response, wddxPacket, "details");
    }
    
    public ActionForward checkGateStatus(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
        Long activityID = new Long(WebUtil.readLongParam(request, "activityID"));
        Long lessonID = new Long(WebUtil.readLongParam(request, "lessonID"));
        String wddxPacket = monitoringService.checkGateStatus(activityID, lessonID);
       // request.setAttribute(USE_JSP_OUTPUT, "1");
        return outputPacket(mapping, request, response, wddxPacket, "details");
        
    }
    
    public ActionForward releaseGate(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        this.monitoringService = MonitoringServiceProxy.getMonitoringService(getServlet().getServletContext());
        Long activityID = new Long(WebUtil.readLongParam(request, "activityID"));
        String wddxPacket = monitoringService.releaseGate(activityID);
       // request.setAttribute(USE_JSP_OUTPUT, "1");
        return outputPacket(mapping, request, response, wddxPacket, "details");
    }
*/
}

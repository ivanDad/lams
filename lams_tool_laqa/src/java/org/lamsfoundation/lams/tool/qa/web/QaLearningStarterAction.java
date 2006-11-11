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
package org.lamsfoundation.lams.tool.qa.web;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.lamsfoundation.lams.notebook.model.NotebookEntry;
import org.lamsfoundation.lams.notebook.service.CoreNotebookConstants;
import org.lamsfoundation.lams.tool.qa.GeneralLearnerFlowDTO;
import org.lamsfoundation.lams.tool.qa.QaAppConstants;
import org.lamsfoundation.lams.tool.qa.QaApplicationException;
import org.lamsfoundation.lams.tool.qa.QaComparator;
import org.lamsfoundation.lams.tool.qa.QaContent;
import org.lamsfoundation.lams.tool.qa.QaQueContent;
import org.lamsfoundation.lams.tool.qa.QaQueUsr;
import org.lamsfoundation.lams.tool.qa.QaSession;
import org.lamsfoundation.lams.tool.qa.QaUtils;
import org.lamsfoundation.lams.tool.qa.service.IQaService;
import org.lamsfoundation.lams.tool.qa.service.QaServiceProxy;
import org.lamsfoundation.lams.usermanagement.dto.UserDTO;
import org.lamsfoundation.lams.web.session.SessionManager;
import org.lamsfoundation.lams.web.util.AttributeNames;
import org.lamsfoundation.lams.web.util.SessionMap;

/**
 * 
 * @author Ozgur Demirtas
 *
 * <lams base path>/<tool's learner url>&userId=<learners user id>&toolSessionId=123&mode=teacher
 * 
 * Since the toolSessionId is passed, we will derive toolContentId from the toolSessionId
 *
 * This class is used to load the default content and initialize the presentation Map for Learner mode 
 * 
 * createToolSession will not be called once the tool is deployed.
 * 
 * It is important that ALL the session attributes created in this action gets removed by: QaUtils.cleanupSession(request) 
 * 
 */

/**
 * Tool Session:
 *
 * A tool session is the concept by which which the tool and the LAMS core manage a set of learners interacting with the tool. 
 * The tool session id (toolSessionId) is generated by the LAMS core and given to the tool.
 * A tool session represents the use of a tool for a particulate activity for a group of learners. 
 * So if an activity is ungrouped, then one tool session exist for for a tool activity in a learning design.
 *
 * More details on the tool session id are covered under monitoring.
 * When thinking about the tool content id and the tool session id, it might be helpful to think about the tool content id 
 * relating to the definition of an activity, whereas the tool session id relates to the runtime participation in the activity.
 * 
 */

/**
 * 
 * Learner URL:
 * The learner url display the screen(s) that the learner uses to participate in the activity. 
 * When the learner accessed this user, it will have a tool access mode ToolAccessMode.LEARNER.
 *
 * It is the responsibility of the tool to record the progress of the user. 
 * If the tool is a multistage tool, for example asking a series of questions, the tool must keep track of what the learner has already done. 
 * If the user logs out and comes back to the tool later, then the tool should resume from where the learner stopped.
 * When the user is completed with tool, then the tool notifies the progress engine by calling 
 * org.lamsfoundation.lams.learning.service.completeToolSession(Long toolSessionId, User learner).
 *
 * If the tool's content DefineLater flag is set to true, then the learner should see a "Please wait for the teacher to define this part...." 
 * style message.
 * If the tool's content RunOffline flag is set to true, then the learner should see a "This activity is not being done on the computer. 
 * Please see your instructor for details."
 *
 * ?? Would it be better to define a run offline message in the tool? We have instructions for the teacher but not the learner. ??
 * If the tool has a LockOnFinish flag, then the tool should lock learner's entries once they have completed the activity. 
 * If they return to the activity (e.g. via the progress bar) then the entries should be read only.
 *
 */

/**
<!--Learning Starter  -->
<action 
		path="/learningStarter" 
		type="org.lamsfoundation.lams.tool.qa.web.QaLearningStarterAction" 
		name="QaLearningForm" 
		scope="request"
	    unknown="false"
 	validate="false"
		input="/learningIndex.jsp"> 
			
	  	<forward
		    name="loadLearner"
		    path="/learning/AnswersContent.jsp"
		    redirect="false"
		  />
	  	
	      <forward
	        name="learnerRep"
	        path="/monitoring/LearnerRep.jsp"
		    redirect="false"
	      />

	      <forward
	        name="individualLearnerRep"
	        path="/learning/LearnerRep.jsp"
		    redirect="false"
	      />

	  	<forward
			name="loadMonitoring"
			path="/monitoring/MonitoringMaincontent.jsp"
		    redirect="false"
	  	/>

	  	<forward
		    name="refreshMonitoring"
		    path="/monitoring/MonitoringMaincontent.jsp"
		    redirect="false"
	  	/>
	      
	      <forward
	        name="learningStarter"
	        path="/learningIndex.jsp"
		    redirect="false"
	      />
	      
	  	<forward
		    name="defineLater"
	        path="/learning/defineLater.jsp"
		    redirect="false"
	  	/>
	  	
	  	<forward
		    name="runOffline"
	        path="/learning/RunOffline.jsp"
		    redirect="false"
	  	/>
	  	

		<forward
		    name="notebook"
		    path="/learning/Notebook.jsp"
		    redirect="false"
	  	/>   
	  	   
	</action>  
 * */

public class QaLearningStarterAction extends Action implements QaAppConstants {
	static Logger logger = Logger.getLogger(QaLearningStarterAction.class.getName());

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
  								throws IOException, ServletException, QaApplicationException {
		
		QaUtils.cleanUpSessionAbsolute(request);

		IQaService qaService = QaServiceProxy.getQaService(getServlet().getServletContext());
	    logger.debug("retrieving qaService: " + qaService);

		/* holds the question contents for a given tool session and relevant content */
		Map mapQuestions= new TreeMap(new QaComparator());
		
		/*holds the answers */  
		Map mapAnswers= new TreeMap(new QaComparator());
		
		QaLearningForm qaLearningForm = (QaLearningForm) form;
		
		GeneralLearnerFlowDTO generalLearnerFlowDTO= new GeneralLearnerFlowDTO();
		generalLearnerFlowDTO.setCurrentQuestionIndex(new Integer(1));
		generalLearnerFlowDTO.setCurrentAnswer("");
		

		logger.debug("reading httpSessionID");
		
 	    String httpSessionID=qaLearningForm.getHttpSessionID();
 	    logger.debug("httpSessionID: " + httpSessionID);

	    SessionMap sessionMap = (SessionMap) request.getSession().getAttribute(httpSessionID);
	    logger.debug("sessionMap: " + sessionMap);

	    if (sessionMap == null)
	    {
	        sessionMap = new SessionMap();
	        Map mapSequentialAnswers= new HashMap();
		    sessionMap.put(MAP_SEQUENTIAL_ANSWERS_KEY, mapSequentialAnswers);
		    request.getSession().setAttribute(sessionMap.getSessionID(), sessionMap);
	    }
	    
	    qaLearningForm.setHttpSessionID(sessionMap.getSessionID());
		generalLearnerFlowDTO.setHttpSessionID(sessionMap.getSessionID());
		
	    /*validate learning mode parameters*/
	    boolean validateParameters=validateParameters(request, mapping, qaLearningForm);
	    logger.debug("validateParamaters: " + validateParameters);
	    if (!validateParameters)
	    {
	        logger.debug("error during validation");
	    }
	    
	    String userID=qaLearningForm.getUserID();
		logger.debug("userID: " + userID);
		
	    /*
	     * use the incoming tool session id and later derive toolContentId from it. 
	     */
		String toolSessionID=qaLearningForm.getToolSessionID();
		logger.debug("toolSessionID: " + toolSessionID);
		generalLearnerFlowDTO.setToolSessionID(toolSessionID);
		qaLearningForm.setToolSessionID(toolSessionID);
		
	    /*
	     * By now, the passed tool session id MUST exist in the db by calling:
	     * public void createToolSession(Long toolSessionId, Long toolContentId) by the core.
	     *  
	     * make sure this session exists in tool's session table by now.
	     */
		
	    if (!QaUtils.existsSession(new Long(toolSessionID).longValue(), qaService)) 
		{
	    		QaUtils.cleanUpSessionAbsolute(request);
		    	logger.debug("error: The tool expects mcSession.");
				return (mapping.findForward(ERROR_LIST_LEARNER));
		}
	    
		
		/*
		 * by now, we made sure that the passed tool session id exists in the db as a new record
		 * Make sure we can retrieve it and relavent content
		 */
		
	    
		QaSession qaSession=qaService.retrieveQaSessionOrNullById(new Long(toolSessionID).longValue());
	    logger.debug("retrieving qaSession: " + qaSession);
	    /*
	     * find out what content this tool session is referring to
	     * get the content for this tool session (many to one mapping)
	     */
	    
	    /*
	     * Each passed tool session id points to a particular content. Many to one mapping.
	     */
		QaContent qaContent=qaSession.getQaContent();
	    logger.debug("using qaContent: " + qaContent);
	    if (qaContent == null)
	    {
    		QaUtils.cleanUpSessionAbsolute(request);	    	
	    	logger.debug("error: The tool expects qaContent.");
	    	return (mapping.findForward(ERROR_LIST_LEARNER));
	    }
	    
	    generalLearnerFlowDTO.setActivityTitle(qaContent.getTitle());
		generalLearnerFlowDTO.setActivityInstructions(qaContent.getInstructions());
		
		logger.debug("is tool reflective: " + qaContent.isReflect());
		generalLearnerFlowDTO.setReflection(new Boolean(qaContent.isReflect()).toString());
		logger.debug("reflection subject: " + qaContent.getReflectionSubject());
		generalLearnerFlowDTO.setReflectionSubject(qaContent.getReflectionSubject());
		
		
		logger.debug("attempt getting notebookEntry: ");
		NotebookEntry notebookEntry = qaService.getEntry(new Long(toolSessionID),
				CoreNotebookConstants.NOTEBOOK_TOOL,
				MY_SIGNATURE, new Integer(userID));
		
        logger.debug("notebookEntry: " + notebookEntry);
		
		if (notebookEntry != null) {
		    String notebookEntryPresentable=QaUtils.replaceNewLines(notebookEntry.getEntry());
		    generalLearnerFlowDTO.setNotebookEntry(notebookEntryPresentable);
		}
		
	    

	    logger.debug("using TOOL_CONTENT_ID: " + qaContent.getQaContentId());
	    generalLearnerFlowDTO.setToolContentID(qaContent.getQaContentId().toString());
	    
	    	    
	    /*
	     * The content we retrieved above must have been created before in Authoring time. 
	     * And the passed tool session id refers to it.
	     */
	    
	    
		logger.debug("REPORT_TITLE_LEARNER: " + qaContent.getReportTitle());
		generalLearnerFlowDTO.setReportTitleLearner(qaContent.getReportTitle());
	    
	    /*
	     * Is the tool activity been checked as Run Offline in the property inspector?
	     */
	    logger.debug("IS_TOOL_ACTIVITY_OFFLINE: " + qaContent.isRunOffline());
	    generalLearnerFlowDTO.setActivityOffline(new Boolean(qaContent.isRunOffline()).toString());
	    
	    logger.debug("IS_USERNAME_VISIBLE: " + qaContent.isUsernameVisible());
	    generalLearnerFlowDTO.setUserNameVisible(new Boolean(qaContent.isUsernameVisible()).toString());
	    
	    
	    /*
	     * Is the tool activity been checked as Define Later in the property inspector?
	     */
	    logger.debug("IS_DEFINE_LATER: " + qaContent.isDefineLater());
	    if ( qaContent.isDefineLater() ) {
            QaUtils.cleanUpSessionAbsolute(request);
	    	return (mapping.findForward(DEFINE_LATER));
	    }
	    
	    /*
	     * Learning mode requires this setting for jsp to generate the user's report 
	     */
	    logger.debug("IS_QUESTIONS_SEQUENCED: " + qaContent.isQuestionsSequenced());
	    String feedBackType="";
    	if (qaContent.isQuestionsSequenced())
    	{
    		generalLearnerFlowDTO.setQuestionListingMode(QUESTION_LISTING_MODE_SEQUENTIAL);
    		feedBackType=FEEDBACK_TYPE_SEQUENTIAL;
    	}
	    else
	    {
	        generalLearnerFlowDTO.setQuestionListingMode(QUESTION_LISTING_MODE_COMBINED);
    		feedBackType=FEEDBACK_TYPE_COMBINED;
	    }
	    logger.debug("QUESTION_LISTING_MODE: " + generalLearnerFlowDTO.getQuestionListingMode());
	    
    	/*
    	 * fetch question content from content
    	 */
    	Iterator contentIterator=qaContent.getQaQueContents().iterator();
    	while (contentIterator.hasNext())
    	{
    		QaQueContent qaQueContent=(QaQueContent)contentIterator.next();
    		logger.debug("qaQueContent: " + qaQueContent);
    		logger.debug("question: " + qaQueContent.getQuestion());
    		if (qaQueContent != null)
    		{
    			int displayOrder=qaQueContent.getDisplayOrder();
    			logger.debug("displayOrder: " + displayOrder);
    			
        		if (displayOrder != 0)
        		{
        			/*
    	    		 *  add the question to the questions Map in the displayOrder
    	    		 */
            		mapQuestions.put(new Integer(displayOrder).toString(),qaQueContent.getQuestion());
        		}
    		}
    	}
    	logger.debug("mapQuestions: " + mapQuestions);
    	generalLearnerFlowDTO.setMapQuestions(mapQuestions);
    	generalLearnerFlowDTO.setMapQuestionContentLearner(mapQuestions);
		
    	Iterator itMapQuestions = mapQuestions.entrySet().iterator();
    	
    	while (itMapQuestions.hasNext()) 
    	{
        	Map.Entry pairs = (Map.Entry)itMapQuestions.next();
        	mapAnswers.put(pairs.getKey(), "");

    	}
    	logger.debug("mapAnswers : " + mapAnswers);
    	generalLearnerFlowDTO.setMapAnswers(mapAnswers);
    	logger.debug("mapQuestions: " + mapQuestions);

    	logger.debug("mapQuestions has : " + mapQuestions.size() + " entries.");
    	
    	generalLearnerFlowDTO.setTotalQuestionCount(new Integer(mapQuestions.size()));
    	qaLearningForm.setTotalQuestionCount(new Integer(mapQuestions.size()).toString());
    	
    	String userFeedback= feedBackType + generalLearnerFlowDTO.getTotalQuestionCount() + QUESTIONS;
    	
    	generalLearnerFlowDTO.setUserFeedback(userFeedback);
    	
    	logger.debug("remaining question count: " + generalLearnerFlowDTO.getTotalQuestionCount().toString());
    	generalLearnerFlowDTO.setRemainingQuestionCount(generalLearnerFlowDTO.getTotalQuestionCount().toString());
    	generalLearnerFlowDTO.setInitialScreen(new Boolean(true).toString());
    	
    	request.setAttribute(GENERAL_LEARNER_FLOW_DTO, generalLearnerFlowDTO);
    	/* Is the request for a preview by the author?
    	Preview The tool must be able to show the specified content as if it was running in a lesson. 
		It will be the learner url with tool access mode set to ToolAccessMode.AUTHOR 
		3 modes are:
			author
			teacher
			learner
		*/
	    /*handling PREVIEW mode*/
		String mode=qaLearningForm.getMode();
	    logger.debug("mode: " + mode);
    	if ((mode != null) && (mode.equals("author")))
    	{
    		logger.debug("Author requests for a preview of the content.");
    	}
    	
    	/* by now, we know that the mode is either teacher or learner
    	 * check if the mode is teacher and request is for Learner Progress
    	 */
		logger.debug("userID: " + userID);
		String learnerProgressUserId=request.getParameter(USER_ID);
		logger.debug("learnerProgressUserId: " + learnerProgressUserId);
		
		if ((learnerProgressUserId != null) && (mode.equals("teacher")))
		{
			logger.debug("start generating learner progress report for toolSessionID: " + toolSessionID);
	    	
	    	/* the report should have only this user's entries(with userId)*/
	    	QaMonitoringAction qaMonitoringAction= new QaMonitoringAction();
	    	logger.debug("using generalLearnerFlowDTO: " + generalLearnerFlowDTO);
	    	generalLearnerFlowDTO.setRequestLearningReport(new Boolean(true).toString());
	    	generalLearnerFlowDTO.setRequestLearningReportProgress(new Boolean(true).toString());
	    	generalLearnerFlowDTO.setTeacherViewOnly(new Boolean(true).toString());
	    	
	    	qaMonitoringAction.refreshSummaryData(request, qaContent, qaService, true, true, toolSessionID, learnerProgressUserId, 
	    	        generalLearnerFlowDTO, false, toolSessionID);
    		
	    	logger.debug("presenting teacher's report");
	    	logger.debug("fwd'ing to for learner progress" + INDIVIDUAL_LEARNER_REPORT);
    		
    		return (mapping.findForward(INDIVIDUAL_LEARNER_REPORT));		    		
   		}
    	
		/* by now, we know that the mode is learner*/
	    /* find out if the content is set to run offline or online. If it is set to run offline , the learners are informed about that. */
	    boolean isRunOffline=QaUtils.isRunOffline(qaContent);
	    logger.debug("isRunOffline: " + isRunOffline);
	    if (isRunOffline == true)
	    {
    		QaUtils.cleanUpSessionAbsolute(request);
	    	logger.debug("warning to learner: the activity is offline.");
	    	logger.debug("fwding to :" + RUN_OFFLINE);
			return (mapping.findForward(RUN_OFFLINE));
	    }

    	
    	
    	/*
	     * Verify that userId does not already exist in the db.
	     * If it does exist and the passed tool session id exists in the db, that means the user already responded to the content and 
	     * his answers must be displayed  read-only
	     * 
	     * if the user's tool session id AND user id exists in the tool tables go to learner's report.
	     */
	    /* if the 'All Responses' has been clicked no more user entry is accepted, and isResponseFinalized() returns true*/
	    logger.debug("userID:" + userID);
	    Long currentToolSessionID=new Long(qaLearningForm.getToolSessionID());
	    logger.debug("currentToolSessionID: " + currentToolSessionID);
	    logger.debug("current session uid: " + qaSession.getUid());
	    
	    boolean lockWhenFinished=qaContent.isLockWhenFinished(); 
	    logger.debug("lockWhenFinished: " + lockWhenFinished);
	    
	    String sessionStatus=qaSession.getSession_status(); 
	    logger.debug("sessionStatus: " + sessionStatus);
	    
	    
	    if (userID != null)
	    {
		    QaQueUsr qaQueUsr=qaService.getQaUserBySession(new Long(userID), qaSession.getUid());
		    logger.debug("QaQueUsr:" + qaQueUsr);
		    
		    if ((qaQueUsr != null) && (qaQueUsr.isResponseFinalized()))
		    {
		        	logger.debug("is current user's response finalised: " + qaQueUsr.isResponseFinalized());
		    		QaSession checkSession=qaQueUsr.getQaSession();
		    		logger.debug("checkSession:" + checkSession);
			    	
		    		if (checkSession != null)
		    		{
		    			Long checkQaSessionId=checkSession.getQaSessionId();
		    			logger.debug("checkQaSessionId:" + checkQaSessionId);
		    			
		    			if (checkQaSessionId.toString().equals(currentToolSessionID.toString()))
		    			{
		    			    
		    			    logger.debug("the learner is in the same session and has already responsed to this content");
		    			    logger.debug("lockWhenFinished: " + lockWhenFinished);

		    		    	boolean isLearnerFinished=qaQueUsr.isLearnerFinished();
		    	    	    logger.debug("isLearnerFinished: " + isLearnerFinished);

		    	    	    generalLearnerFlowDTO.setLockWhenFinished(new Boolean(lockWhenFinished).toString());

		    				logger.debug("isUserNamesVisible: " + qaContent.isUsernameVisible());
		    				Boolean isUserNamesVisibleBoolean=new Boolean(qaContent.isUsernameVisible());
		    		    	boolean isUserNamesVisible=isUserNamesVisibleBoolean.booleanValue();
		    		    	
		    		    	QaMonitoringAction qaMonitoringAction= new QaMonitoringAction();
		    		    	/*the report should have all the users' entries OR
		    		    	 * the report should have only the current session's entries*/
		    		    	
		    		    	generalLearnerFlowDTO.setRequestLearningReport(new Boolean(true).toString());

		    		    	logger.debug("using generalLearnerFlowDTO: " + generalLearnerFlowDTO);
		    		    	qaMonitoringAction.refreshSummaryData(request, qaContent, qaService, isUserNamesVisible, true, 
		    		    	        currentToolSessionID.toString(), null, generalLearnerFlowDTO, false, toolSessionID);
		    		    	logger.debug("final generalLearnerFlowDTO: " + generalLearnerFlowDTO);

		    			    logger.debug("current sessionMap: " + sessionMap);
		    			    
		    			    mapAnswers=(Map)sessionMap.get(MAP_ALL_RESULTS_KEY);
		    		    	logger.debug("mapAnswers: " + mapAnswers);
		    		    	
		    		    	
		    	    	    if (isLearnerFinished)
		    	    	    {
		    	    	        logger.debug("isLearnerFinished is true");
		    	    	        generalLearnerFlowDTO.setRequestLearningReportViewOnly(new Boolean(true).toString());
			    		        
			    		    	request.setAttribute(GENERAL_LEARNER_FLOW_DTO, generalLearnerFlowDTO);
			    	    		logger.debug("before fwd, GENERAL_LEARNER_FLOW_DTO: " +  request.getAttribute(GENERAL_LEARNER_FLOW_DTO));
			    	    		
			    	    		logger.debug("fwd'ing to." + REVISITED_LEARNER_REP);
			    	    		return (mapping.findForward(REVISITED_LEARNER_REP));
		    	    	    }
		    	    	    else
		    	    	    {
		    	    	        logger.debug("NOT true both isLearnerFinished and lockWhenFinished");
			    		    	generalLearnerFlowDTO.setRequestLearningReportViewOnly(new Boolean(false).toString());
			    		    	
			    		        request.setAttribute(GENERAL_LEARNER_FLOW_DTO, generalLearnerFlowDTO);
			    	    		logger.debug("before fwd, GENERAL_LEARNER_FLOW_DTO: " +  request.getAttribute(GENERAL_LEARNER_FLOW_DTO));
		    	    	    }

		    		    	
		    	    		logger.debug("fwd'ing to." + INDIVIDUAL_LEARNER_REPORT);
		    	    		return (mapping.findForward(INDIVIDUAL_LEARNER_REPORT));
		    			}
		    		}
		    }
	    }
	    else
	    {
	    	logger.debug("userId is null, it should not be, report this.");
	    }
	    
    	/*
    	 * present user with the questions.
    	 */
	    request.setAttribute(GENERAL_LEARNER_FLOW_DTO, generalLearnerFlowDTO);
		logger.debug("GENERAL_LEARNER_FLOW_DTO: " +  request.getAttribute(GENERAL_LEARNER_FLOW_DTO));
        logger.debug("forwarding to: " + LOAD_LEARNER);
		return (mapping.findForward(LOAD_LEARNER));	
	}
	

	/**
	 * validates the learning mode parameters
	 * @param request
	 * @param mapping
	 * @return ActionForward
	 */
	protected boolean validateParameters(HttpServletRequest request, ActionMapping mapping, QaLearningForm qaLearningForm)
	{
		/*
	     * obtain and setup the current user's data 
	     */
	    String userID = "";
	    HttpSession ss = SessionManager.getSession();
	    logger.debug("ss: " + ss);
	    
	    if (ss != null)
	    {
		    UserDTO user = (UserDTO) ss.getAttribute(AttributeNames.USER);
		    if ((user != null) && (user.getUserID() != null))
		    {
		    	userID = user.getUserID().toString();
			    logger.debug("retrieved userId: " + userID);
			    qaLearningForm.setUserID(userID);
		    }
	    }
		
	    
	    /*
	     * process incoming tool session id and later derive toolContentId from it. 
	     */
    	String strToolSessionId=request.getParameter(AttributeNames.PARAM_TOOL_SESSION_ID);
	    long toolSessionId=0;
	    if ((strToolSessionId == null) || (strToolSessionId.length() == 0)) 
	    {
	    	persistError(request, "error.toolSessionId.required");
			return false;
	    }
	    else
	    {
	    	try
			{
	    		toolSessionId=new Long(strToolSessionId).longValue();
		    	logger.debug("passed TOOL_SESSION_ID : " + new Long(toolSessionId));
		    	qaLearningForm.setToolSessionID(new Long(toolSessionId).toString());
			}
	    	catch(NumberFormatException e)
			{
	    		logger.debug("add error.sessionId.numberFormatException to ActionMessages.");
	    		return false;
			}
	    }
	    
	    /*mode can be learner, teacher or author */
	    String mode=request.getParameter(MODE);
	    logger.debug("mode: " + mode);
	    
	    if ((mode == null) || (mode.length() == 0)) 
	    {
    		return false;
	    }
	    
	    if ((!mode.equals("learner")) && (!mode.equals("teacher")) && (!mode.equals("author")))
	    {
	        return false;
	    }

	    logger.debug("session LEARNING_MODE set to:" + mode);
		qaLearningForm.setMode(mode);	    
	    return true;
	}
	
	
	boolean isSessionCompleted(String userSessionId, IQaService qaService)
	{
		logger.debug("userSessionId:" + userSessionId);
		QaSession qaSession=qaService.retrieveQaSessionOrNullById(new Long(userSessionId).longValue());
	    logger.debug("retrieving qaSession: " + qaSession);
	    logger.debug("voteSession status : " + qaSession.getSession_status());
	    if  ((qaSession.getSession_status() != null) &&  (qaSession.getSession_status().equals(COMPLETED)))
	    {
	        logger.debug("this session is COMPLETED voteSession status : " + userSessionId);
	        return true;
	    }
	    return false;
	}

	
	/**
     * persists error messages to request scope
     * @param request
     * @param message
     */
	public void persistError(HttpServletRequest request, String message)
	{
		ActionMessages errors= new ActionMessages();
		errors.add(Globals.ERROR_KEY, new ActionMessage(message));
		logger.debug("add " + message +"  to ActionMessages:");
		saveErrors(request,errors);	    	    
	}
}  

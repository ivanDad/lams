/*
 * Created on May 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.lamsfoundation.lams.tool.sbmt.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;
import org.lamsfoundation.lams.tool.sbmt.SubmitFilesContent;
import org.lamsfoundation.lams.tool.sbmt.SubmitFilesSession;
import org.lamsfoundation.lams.tool.sbmt.exception.SubmitFilesException;
import org.lamsfoundation.lams.tool.sbmt.service.ISubmitFilesService;
import org.lamsfoundation.lams.tool.sbmt.service.SubmitFilesServiceProxy;

/**
 * @author Manpreet Minhas
 * @author Steve.Ni
 * @struts.action 
 * 			path="/learner"
 * 			parameter="method"
 * 			name="SbmtLearnerForm"
 * 			input="/sbmtLearner.jsp"
 * 			scope="request"
 * 			validate="false"
 * 
 * @struts.action-forward name="upload" path="/sbmtLearner.jsp"
 */
public class LearnerAction extends DispatchAction {
	
	public ISubmitFilesService submitFilesService;
	public static Logger logger = Logger.getLogger(LearnerAction.class);
	
	public ActionForward listFiles(ActionMapping mapping,
									ActionForm form,
									HttpServletRequest request,
									HttpServletResponse response){
		
		DynaActionForm authForm= (DynaActionForm)form;
		
		Long sessionID =(Long) authForm.get("toolSessionID");
		Long userID = (Long)authForm.get("userID");
		//get submission information from content table.E.g., title, instruction
		submitFilesService = SubmitFilesServiceProxy.getSubmitFilesService(this.getServlet().getServletContext());
		SubmitFilesSession session = submitFilesService.getSessionById(sessionID);
		SubmitFilesContent content = session.getContent();
		
		HttpSession httpSession = request.getSession(true);
		httpSession.setAttribute("content",content);
		
		List filesUploaded = submitFilesService.getFilesUploadedByUser(userID,sessionID);
		authForm.set("filesUploaded",filesUploaded);
		
		//to avoid user without patience click "upload" button too fast 
		saveToken(request);
		return mapping.getInputForward();
		
	}
	
	public ActionForward uploadFile(ActionMapping mapping,
									ActionForm form,
									HttpServletRequest request,
									HttpServletResponse response){
		
		DynaActionForm authForm= (DynaActionForm)form;
		if(!isTokenValid(request,true)){
			Long sessionID =(Long) authForm.get("toolSessionID");
			Long userID = (Long)authForm.get("userID");
			submitFilesService = SubmitFilesServiceProxy.getSubmitFilesService(this.getServlet().getServletContext());				
			List filesUploaded = submitFilesService.getFilesUploadedByUser(userID,sessionID);
			authForm.set("filesUploaded",filesUploaded);
			return returnErrors(mapping,request,"submit.upload.twice","upload");
		}
		
		Long sessionID =(Long) authForm.get("toolSessionID");
		Long userID = (Long)authForm.get("userID");
		String filePath = (String) authForm.get("filePath");
		String fileDescription = (String) authForm.get("fileDescription");
		
		submitFilesService = SubmitFilesServiceProxy.getSubmitFilesService(this.getServlet().getServletContext());
		//to avoid user without patience click "upload" button too fast 
		saveToken(request);
		try{
			submitFilesService.uploadFile(sessionID,filePath,fileDescription,userID);
			List filesUploaded = submitFilesService.getFilesUploadedByUser(userID,sessionID);
			authForm.set("filesUploaded",filesUploaded);
			return mapping.getInputForward();			
		}catch(SubmitFilesException se){
			logger.error("uploadFile: Submit Files Exception has occured" + se.getMessage());
			return returnErrors(mapping,request,se.getMessage(),"upload");
		}
	}
	
	/**
	 * This is a utily function for forwarding the errors to
	 * the respective JSP page indicated by <code>forward</code>
	 * 
	 * @param mapping 
	 * @param request
	 * @param errorMessage The error message to be displayed
	 * @param forward The JSP page to which the errors would be forwarded
	 * @return ActionForward
	 */
	private ActionForward returnErrors(ActionMapping mapping, 
							  HttpServletRequest request, 
							  String errorMessage,String forward){
		ActionMessages messages = new ActionMessages();
		messages.add(ActionMessages.GLOBAL_MESSAGE,
					 new ActionMessage(errorMessage));
		saveErrors(request,messages);
		return mapping.findForward(forward);
	}

}

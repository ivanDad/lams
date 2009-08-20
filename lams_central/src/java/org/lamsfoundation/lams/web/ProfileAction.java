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
package org.lamsfoundation.lams.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.lamsfoundation.lams.index.IndexLessonBean;
import org.lamsfoundation.lams.index.IndexOrgBean;
import org.lamsfoundation.lams.learning.service.ICoreLearnerService;
import org.lamsfoundation.lams.lesson.dto.LessonDTO;
import org.lamsfoundation.lams.themes.Theme;
import org.lamsfoundation.lams.themes.service.IThemeService;
import org.lamsfoundation.lams.usermanagement.Organisation;
import org.lamsfoundation.lams.usermanagement.OrganisationType;
import org.lamsfoundation.lams.usermanagement.SupportedLocale;
import org.lamsfoundation.lams.usermanagement.User;
import org.lamsfoundation.lams.usermanagement.service.IUserManagementService;
import org.lamsfoundation.lams.util.Configuration;
import org.lamsfoundation.lams.util.ConfigurationKeys;
import org.lamsfoundation.lams.util.IndexUtils;
import org.lamsfoundation.lams.util.LanguageUtil;
import org.lamsfoundation.lams.web.action.LamsDispatchAction;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @version
 * 
 * <p>
 * <a href="ProfileAction.java.html"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:fyang@melcoe.mq.edu.au">Fei Yang</a>
 * 
 * Created at 14:21:53 on 28/06/2006
 */

/**
 * @struts:action path="/profile" name="UserForm" scope="request"
 *                parameter="method" validate="false"
 * 
 * @struts:action-forward name="view" path=".profile"
 * @struts:action-forward name="lessons" path=".lessons"
 * @struts:action-forward name="edit" path=".editprofile"
 */
public class ProfileAction extends LamsDispatchAction {

    private static Logger log = Logger.getLogger(ProfileAction.class);

    private static IUserManagementService service;

    private static List<SupportedLocale> locales;

    private static ICoreLearnerService learnerService;

    private static IThemeService themeService;

    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	User requestor = (User) getService().getUserByLogin(request.getRemoteUser());
	String fullName = (requestor.getTitle() != null ? requestor.getTitle() + " " : "") + requestor.getFirstName()
		+ " " + requestor.getLastName();
	String email = requestor.getEmail();

	request.setAttribute("fullName", fullName);
	request.setAttribute("email", (email != null ? email : ""));
	request.setAttribute("portraitUuid", (requestor.getPortraitUuid() == null ? 0 : requestor.getPortraitUuid()));
	request.setAttribute("tab", "profile");

	return mapping.findForward("view");
    }

    public ActionForward lessons(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	// list all active lessons for this learner (single sql query)
	User requestor = (User) getService().getUserByLogin(request.getRemoteUser());
	LessonDTO[] lessons = getLearnerService().getActiveLessonsFor(requestor.getUserId());

	// make org-sorted beans out of the lessons
	HashMap<Integer, IndexOrgBean> orgBeansMap = new HashMap<Integer, IndexOrgBean>();
	for (LessonDTO lesson : lessons) {
	    Integer orgId = lesson.getOrganisationID();
	    Organisation org = (Organisation) getService().findById(Organisation.class, orgId);
	    Integer orgTypeId = org.getOrganisationType().getOrganisationTypeId();
	    IndexLessonBean lessonBean = new IndexLessonBean(lesson.getLessonName(), "javascript:openLearner("
		    + lesson.getLessonID() + ")");
	    lessonBean.setId(lesson.getLessonID());
	    log.debug("Lesson: " + lesson.getLessonName());

	    // insert or update bean if it is a course
	    if (orgTypeId.equals(OrganisationType.COURSE_TYPE)) {
		IndexOrgBean orgBean = (!orgBeansMap.containsKey(orgId)) ? new IndexOrgBean(org.getOrganisationId(),
			org.getName(), orgTypeId) : orgBeansMap.get(orgId);
		orgBean.addLesson(lessonBean);
		orgBeansMap.put(orgId, orgBean);
	    } else if (orgTypeId.equals(OrganisationType.CLASS_TYPE)) {

		// if it is a class, find existing or create new parent bean
		Organisation parentOrg = org.getParentOrganisation();
		Integer parentOrgId = parentOrg.getOrganisationId();
		IndexOrgBean parentOrgBean = (!orgBeansMap.containsKey(parentOrgId)) ? new IndexOrgBean(parentOrg
			.getOrganisationId(), parentOrg.getName(), OrganisationType.COURSE_TYPE) : orgBeansMap
			.get(parentOrgId);
		// create new bean for class, or use existing bean
		IndexOrgBean orgBean = new IndexOrgBean(org.getOrganisationId(), org.getName(), orgTypeId);
		List<IndexOrgBean> childOrgBeans = parentOrgBean.getChildIndexOrgBeans();
		if (childOrgBeans.contains(orgBean)) {
		    // use existing org bean
		    orgBean = getOrgBean(org.getName(), childOrgBeans);
		    childOrgBeans.remove(orgBean);
		    orgBean.addLesson(lessonBean);
		    childOrgBeans.add(orgBean);
		    parentOrgBean.setChildIndexOrgBeans(childOrgBeans);
		} else {
		    // using new org bean
		    orgBean.addLesson(lessonBean);
		    parentOrgBean.addChildOrgBean(orgBean);
		}
		orgBeansMap.put(parentOrgId, parentOrgBean);
	    }
	}

	// sort group and subgroup names
	ArrayList<IndexOrgBean> beans = new ArrayList<IndexOrgBean>(orgBeansMap.values());
	Collections.sort(beans);
	for (IndexOrgBean b : beans) {
	    Collections.sort(b.getChildIndexOrgBeans());
	}

	// sort lessons inside each org bean
	for (Object o : beans) {
	    IndexOrgBean bean = (IndexOrgBean) o;
	    Organisation org = (Organisation) service.findById(Organisation.class, bean.getId());

	    // put lesson beans into id-indexed map
	    HashMap<Long, IndexLessonBean> map = new HashMap<Long, IndexLessonBean>();
	    for (IndexLessonBean lbean : bean.getLessons()) {
		map.put(lbean.getId(), lbean);
	    }

	    bean.setLessons(IndexUtils.sortLessonBeans(org.getOrderedLessonIds(), map));
	}

	request.setAttribute("beans", beans);
	request.setAttribute("tab", "profile");

	return mapping.findForward("lessons");
    }

    private IndexOrgBean getOrgBean(String name, List<IndexOrgBean> list) {
	for (IndexOrgBean bean : list) {
	    if (StringUtils.equals(name, bean.getName())) {
		return bean;
	    }
	}
	return null;
    }

    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	User requestor = (User) getService().getUserByLogin(request.getRemoteUser());
	DynaActionForm userForm = (DynaActionForm) form;
	BeanUtils.copyProperties(userForm, requestor);
	SupportedLocale locale = requestor.getLocale();
	if (locale == null) {
	    locale = LanguageUtil.getDefaultLocale();
	}
	userForm.set("localeId", locale.getLocaleId());
	request.setAttribute("locales", locales);
	request.setAttribute("tab", "profile");

	boolean hasLamsCommunityToken = requestor.getLamsCommunityToken() != null;
	request.setAttribute("hasLamsCommunityToken", hasLamsCommunityToken);

	themeService = getThemeService();

	// Get all the css themes
	List<Theme> cssThemes = themeService.getAllCSSThemes();
	request.setAttribute("cssThemes", cssThemes);

	// Check the user css theme is still installed
	Long userSelectedCSSTheme = null;
	if (requestor.getHtmlTheme() != null) {
	    for (Theme theme : cssThemes) {
		if (theme.getThemeId() == requestor.getHtmlTheme().getThemeId()) {
		    userSelectedCSSTheme = theme.getThemeId();
		    break;
		}
	    }
	}
	// if still null, use the default 
	if (userSelectedCSSTheme == null) {
	    userSelectedCSSTheme = themeService.getDefaultCSSTheme().getThemeId();
	}
	userForm.set("userCSSTheme", userSelectedCSSTheme);
	
	// Get all the flash themes
	List<Theme> flashThemes = themeService.getAllFlashThemes();
	request.setAttribute("flashThemes", flashThemes);

	// Check the user flash theme is still installed
	Long userSelectedFlashTheme = null;
	if (requestor.getHtmlTheme() != null) {
	    for (Theme theme : flashThemes) {
		if (theme.getThemeId() == requestor.getFlashTheme().getThemeId()) {
		    userSelectedFlashTheme = theme.getThemeId();
		    break;
		}
	    }
	}
	// if still null, use the default 
	if (userSelectedFlashTheme == null) {
	    userSelectedFlashTheme = themeService.getDefaultFlashTheme().getThemeId();
	}
	userForm.set("userFlashTheme", userSelectedFlashTheme);

	return mapping.findForward("edit");
    }

    private IUserManagementService getService() {
	if (service == null) {
	    WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet()
		    .getServletContext());
	    service = (IUserManagementService) ctx.getBean("userManagementService");
	    locales = getService().findAll(SupportedLocale.class);
	    Collections.sort(locales);
	}
	return service;
    }

    private ICoreLearnerService getLearnerService() {
	if (learnerService == null) {
	    WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet()
		    .getServletContext());
	    learnerService = (ICoreLearnerService) ctx.getBean("learnerService");
	}
	return learnerService;
    }

    private IThemeService getThemeService() {
	if (themeService == null) {
	    WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet()
		    .getServletContext());
	    themeService = (IThemeService) ctx.getBean("themeService");
	}
	return themeService;
    }
}

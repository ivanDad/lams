<%@ include file="/common/taglibs.jsp"%>

<c:set var="formBean" value="<%=request.getAttribute(org.apache.struts.taglib.html.Constants.BEAN_KEY)%>" />
<c:set var="sessionMap" value="${sessionScope[formBean.sessionMapID]}" />
<c:set var="sessionMapID" value="${formBean.sessionMapID}" />

<!-- ========== Basic Tab ========== -->
<div class="form-group">
    <label for="title">
    	<fmt:message key="label.authoring.basic.title"/>
    </label>
    <html:text property="title" styleClass="form-control"/>
</div>

<div class="form-group">
    <label for="instructions">
    	<fmt:message key="label.authoring.basic.instructions"/>
    </label>
	<lams:CKEditor id="instructions" value="${formBean.instructions}"
			contentFolderID="${sessionMap.contentFolderID}">
	</lams:CKEditor>
</div>

<center id="center12">
<div id="flashContent">
	<lams:Alert type="warn" close="false">
				<fmt:message>message.enableFlashAuthorMonitor</fmt:message>
	</lams:Alert>
</div>
</center>

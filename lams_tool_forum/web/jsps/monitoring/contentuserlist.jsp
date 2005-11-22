<%@ include file="/includes/taglibs.jsp" %>

<html:errors/>
	<div id="datatablecontainer">
	<c:forEach var="element" items="${sessionUserMap}">
		<c:set var="sessionID" value="${element.key}"/>
		<c:set var="userlist" value="${element.value}"/>
		
  		<table class="forms">
		    <tr>
		        <td style="border-bottom:1px #000 solid;" colspan="4"><b>SESSION ID: <c:out value="${sessionID}" /></td>
		    </tr>
			<c:forEach var="user" items="${userlist}">
				<tr>
				<html:form  action="/monitoring/viewUserMark">
					<html:hidden property="toolSessionID" value="${sessionID}"/>
					<html:hidden property="userID" value="${user.userId}"/>
					<td colspan="2"><b><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/></b></td>
					<td><c:out value="${user.report.mark}"/> </td>
					<td class="formcontrol"><b><html:submit property="Mark" value="Mark"/></b></td>
				</html:form>
				</tr>		
			</c:forEach>
	  		<c:if test="${empty userlist}">
	  			<tr><td colspan="3">NO USERS AVAILABLE</td></tr>
	  		</c:if>
		    
				<tr>
	  			<td class="formcontrol">
	  			<html:form  action="/monitoring/viewAllMarks">
					<html:hidden property="toolSessionID" value="${sessionID}"/>
		  			<html:submit property="viewAllMarks" value="View all marks"/>
  				</html:form>
	  			</td>
	  			<td class="formcontrol">
	  			<html:form  action="/monitoring/releaseMarks">
					<html:hidden property="toolSessionID" value="${sessionID}"/>
		  			<html:submit property="releaseMarks" value="Release marks"/>
  				</html:form>
	  			</td>
	  			<td class="formcontrol">
	  			<html:form  action="/monitoring/downloadMarks">
					<html:hidden property="toolSessionID" value="${sessionID}"/>
		  			<html:submit property="downloadMarks" value="Download marks"/>
  				</html:form>
	  			</td>
				</tr>
		</table>
		<br><br>
	</c:forEach>
</div>

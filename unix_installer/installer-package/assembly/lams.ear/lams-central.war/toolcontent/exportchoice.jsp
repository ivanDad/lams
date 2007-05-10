<%@ page language="java"  pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="tags-lams" prefix="lams"%>
<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-fmt" prefix="fmt"%>
<c:set var="lams">
	<lams:LAMSURL />
</c:set>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<lams:html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf8">
		<title><fmt:message key="title.export.choose.format" /></title>
		<!-- ********************  CSS ********************** -->
		<lams:css />
		<script type="text/javascript">
			// if the download starts and finishes too quickly, then the page doesn't get a chance to finish rendering. So slow it down a tad.
			function goDownload() {
				var format = 1;
				if(document.getElementById("ims").checked)
					format = 2;
				location.href="<c:url value='/authoring/exportToolContent.do?method=loading&learningDesignID=${learningDesignID}&format='/>" + format;
			}
			function closeWin(){
				window.close();
			}
		</script>
	</head>

	<body class="stripes">

		 <div id="content">
	  
		<h1>
			<fmt:message key="title.export.choose.format" />
		</h1>
		
				<H2><fmt:message key="msg.export.choose.format.instruction" /></H2>
				<table><tr><td>
				<input type="radio" name="format" id="lams" value="1" checked="checked" class="noBorder"><fmt:message key="msg.export.choose.format.lams" />
				<BR><BR>
				<input type="radio" name="format" id="ims" value="2" class="noBorder"><fmt:message key="msg.export.choose.format.ims" />
				<div class="right-buttons">
					<a href="#" onclick="goDownload();" class="button"><fmt:message key="button.export" /></a>
					<a href="javascript:;" onclick="closeWin();" class="button"><fmt:message key="button.close" /></a>
				</div>
				</td></tr></table>
		
		</div>  <!--closes content-->
			
		<div id="footer">
		</div><!--closes footer-->

	</body>
</lams:html>

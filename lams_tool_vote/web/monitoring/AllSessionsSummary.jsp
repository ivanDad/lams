<%-- 
Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
License Information: http://lamsfoundation.org/licensing/lams/2.0/

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 as 
  published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  USA

  http://www.gnu.org/licenses/gpl.txt
--%>


<%@ taglib uri="tags-bean" prefix="bean"%> 
<%@ taglib uri="tags-html" prefix="html"%>
<%@ taglib uri="tags-logic" prefix="logic" %>
<%@ taglib uri="tags-core" prefix="c"%>
<%@ taglib uri="tags-fmt" prefix="fmt" %>
<%@ taglib uri="fck-editor" prefix="FCK" %>
<%@ taglib uri="tags-lams" prefix="lams" %>

<c:set var="lams"><lams:LAMSURL/></c:set>
<c:set var="tool"><lams:WebAppURL/></c:set>



				<table align="left">
				
						<c:if test="${statsTabActive != 'true'}"> 							
							<tr> 
						 		<td NOWRAP> </td>
								<td NOWRAP align=right>
									<jsp:include page="/monitoring/PullDownMenu.jsp" />					
								</td> 
							</tr>
						</c:if> 
						<c:if test="${statsTabActive == 'true'}"> 							
							<tr> 
						 		<td NOWRAP> </td>
								<td NOWRAP align=right>
									<jsp:include page="/monitoring/PullDownMenuStats.jsp" />					
								</td> 
							</tr>
						</c:if> 

						<tr>
					 		<td NOWRAP colspan=2 > </td>
						</tr>
						
						<c:forEach var="currentDto" items="${sessionScope.listVoteAllSessionsDTO}">
						
								<tr>
							 		<td NOWRAP colspan=2 > <b> <font size=2> <bean:message key="label.groupName"/> </b>
							 		<c:out value="${currentDto.sessionName}"/>  </td>
								</tr>
						
								<tr> <td NOWRAP colspan=2> 									
						  	 		<c:set var="currentSessionId" scope="request" value="${currentDto.sessionId}"/>
						  	 		
 							 			<table align=left>
						  					<tr>
										 		<td NOWRAP> <b> <font size=2> <bean:message key="label.total.students"/> </b> </td>
										 		<td> <font size=2> <c:out value="${currentDto.sessionUserCount}"/> </font></td>
											</tr>
					
						  					<tr>
										 		<td NOWRAP> <b> <font size=2> <bean:message key="label.total.completed.students"/> </b> </td> 
										 		<td NOWRAP> <font size=2> <c:out value="${currentDto.completedSessionUserCount}"/> 
											 		&nbsp(<c:out value="${currentDto.completedSessionUserPercent}"/> <bean:message key="label.percent"/>)
										 		</font></td>
											</tr>
					
											<tr>
										 		<td NOWRAP colspan=2> </td>
											</tr>
						  				
											<tr>
										 		<td NOWRAP colspan=2> 
							                            <c:out value="${activityInstructions}" escapeXml="false"/> 
												</td>
											</tr>
											
					
											<tr>
										 		<td NOWRAP colspan=2 > </td>
											</tr>
											


											<tr>
												<td NOWRAP colspan=2 valign=top align=left>
												<table align=center>
													<c:if test="${statsTabActive != 'true'}"> 							
														<tr> 
															<td> </td>
															<td NOWRAP valign=top align=left >
																<c:set var="viewURL">
																	<html:rewrite page="/chartGenerator?type=pie&currentSessionId=${currentSessionId}"/>
																</c:set>
																<a href="javascript:launchInstructionsPopup('<c:out value='${viewURL}' escapeXml='false'/>')">
																	 <font size=2>	<bean:message key="label.view.piechart"/>  </font>
																</a>
															</td>
														</tr>
														<tr> 
															<td> </td>
															<td NOWRAP valign=top align=left >
																<c:set var="viewURL">
																	<html:rewrite page="/chartGenerator?type=bar&currentSessionId=${currentSessionId}"/>
																</c:set>
																<a href="javascript:launchInstructionsPopup('<c:out value='${viewURL}' escapeXml='false'/>')">
																	 <font size=2>	<bean:message key="label.view.barchart"/>  </font>
																</a>
															</td>
														</tr>
													</c:if> 

												<tr>
											 		<td NOWRAP> <b> <font size=2> <bean:message key="label.nomination"/> </b> </td>
													<td NOWRAP> <b> <font size=2> <bean:message key="label.total.votes"/> </b> </td>
												</tr>
												
												<c:forEach var="currentNomination" items="${currentDto.mapStandardNominationsHTMLedContent}">
										  	 		<c:set var="currentNominationKey" scope="request" value="${currentNomination.key}"/>
										  	 		 <tr>
							  	 						<td NOWRAP valign=top align=left>
															<c:out value="${currentNomination.value}" escapeXml="false"/>
														 </td>
						


														<td NOWRAP valign=top align=left>				  	 		
												  	 		<c:forEach var="currentUserCount" items="${currentDto.mapStandardUserCount}">
													  	 		<c:set var="currentUserKey" scope="request" value="${currentUserCount.key}"/>
												  				<c:if test="${currentNominationKey == currentUserKey}"> 				
												  				
												  					<c:if test="${currentUserCount.value != '0' }"> 	
															  	 		<c:forEach var="currentQuestionUid" items="${currentDto.mapStandardQuestionUid}">
																  	 		<c:set var="currentQuestionUidKey" scope="request" value="${currentQuestionUid.key}"/>
															  				<c:if test="${currentQuestionUidKey == currentUserKey}"> 				
			
			
																	  	 		<c:forEach var="currentSessionUid" items="${currentDto.mapStandardToolSessionUid}">
																		  	 		<c:set var="currentSessionUidKey" scope="request" value="${currentSessionUid.key}"/>
																	  				<c:if test="${currentSessionUidKey == currentQuestionUidKey}"> 				
					
																						<c:set var="viewURL">
																							<html:rewrite page="/voteNominationViewer?questionUid=${currentQuestionUid.value}&sessionUid=${currentSessionUid.value}"/>
																						</c:set>
																	  																					
																						<a href="javascript:launchInstructionsPopup('<c:out value='${viewURL}' escapeXml='false'/>')">
																							<font size=2> <c:out value="${currentUserCount.value}"/>  </font>
																						</a>
					
					
																					</c:if> 	    
																				</c:forEach>		  
			
			
																			</c:if> 	    
																		</c:forEach>		  
																	</c:if> 	    								
													  				<c:if test="${currentUserCount.value == 0 }"> 		  				
																			<font size=2> <c:out value="${currentUserCount.value}"/>  </font>
																	</c:if> 	
																	    																								
																</c:if> 	    
															</c:forEach>		  
						
												  	 		<c:forEach var="currentRate" items="${currentDto.mapStandardRatesContent}">
													  	 		<c:set var="currentRateKey" scope="request" value="${currentRate.key}"/>
												  				<c:if test="${currentNominationKey == currentRateKey}"> 				
																			<font size=2> &nbsp(<c:out value="${currentRate.value}"/> <bean:message key="label.percent"/>) </font>
																</c:if> 	    
															</c:forEach>		  
														</td>			
												</tr>	
												</c:forEach>	
										</table>
										</td>
									</tr>


							<tr>
						 		<td NOWRAP colspan=2 > </td>
							</tr>
						
	
							<c:if test="${currentDto.existsOpenVote == 'true' }"> 			
							
								<tr>
							 		<td NOWRAP colspan=2> <bean:message key="label.summary.sessionSeparator"/></td>
								</tr>
	
								<tr>
							 		<td NOWRAP colspan=2 > </td>
								</tr>
							
								<tr>
							 		<td NOWRAP colspan=2>
						 			<table align=left>
						 					<tr>
										 		<td NOWRAP align=center> 
													<b> <font size=2> <bean:message key="label.openVotes"/> </b>
												</td>
											</tr>
	
												<tr> 
													 <td NOWRAP valign=top align=left> <b> <font size=2>  <bean:message key="label.vote"/> </font> </b> </td>  														 
													 <td NOWRAP valign=top align=left> <b> <font size=2>  <bean:message key="label.user"/> </font> </b> </td>  
							  						 <td NOWRAP valign=top align=left> <b> <font size=2>  <bean:message key="label.attemptTime"/></font> </b></td>
							 						 <c:if test="${statsTabActive != 'true'}"> 															  						 
								  						 <td NOWRAP valign=top align=left> <b> <font size=2>  <bean:message key="label.visible"/> </font> </b></td>								  						 
													 </c:if> 																					  						 
									  			</tr>				 
											
						 			
											<c:forEach var="dtoEntry" items="${currentDto.listUserEntries}">
						  							<c:forEach var="questionAttemptData" items="${dtoEntry.questionAttempts}">
											  	 		<c:set var="userData" scope="request" value="${questionAttemptData.value}"/>
		  	 									  	 		<c:set var="currentUid" scope="request" value="${userData.uid}"/>
														<tr> 
																<td NOWRAP valign=top align=left> 
																	<c:out value="${dtoEntry.question}" escapeXml="false"/> 
																	<c:if test="${userData.visible != 'true' }"> 			
												                                <font size=2> <i><bean:message key="label.hidden"/> </i> </font>											                                
																	</c:if> 								
																</td>
																 
																 <td NOWRAP valign=top align=left>   <font size=2> <c:out value="${userData.userName}"/> </font>  </td>  
																 <td NOWRAP valign=top align=left>   <font size=2> <c:out value="${userData.attemptTime}"/> </font> </td>
																 
										 						<c:if test="${statsTabActive != 'true'}"> 							
																	 <td NOWRAP valign=top align=left>
												 						<c:if test="${userData.visible == 'true' }"> 			
													                                <html:submit property="hideOpenVote" 
													                                             styleClass="linkbutton" 
											                                                     onclick="submitOpenVote(${currentUid}, 'hideOpenVote');">						                                             
													                                    <bean:message key="label.hide"/>
													                                </html:submit>
																		</c:if> 													
							
																		<c:if test="${userData.visible != 'true' }"> 			
													                                <html:submit property="showOpenVote" 
													                                             styleClass="linkbutton" 
											                                                     onclick="submitOpenVote(${currentUid}, 'showOpenVote');">						                                             
													                                    <bean:message key="label.show"/>
													                                </html:submit>
																		</c:if> 						
																	</td>																			
															</c:if> 	    																	
																 
														</tr>		
													</c:forEach>		  	
											</c:forEach>		
									</table> 
									</td>
								</tr>
							 </c:if> 	
							 
		 					<tr> <td NOWRAP colspan=2>  </td>
							</tr>
		 					<tr> <td NOWRAP colspan=2> <HR> </td>
							</tr>
		 					<tr> <td NOWRAP colspan=2>  </td>
							</tr>
							 
						</table>
						</td> </tr>	

					</c:forEach>		
							
					<tr>
				 		<td NOWRAP colspan=2 >  </td>
					</tr>

					</table>					


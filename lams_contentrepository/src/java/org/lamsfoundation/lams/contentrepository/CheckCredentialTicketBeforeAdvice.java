/*
 * Created on Jan 21, 2005
 */
package org.lamsfoundation.lams.contentrepository;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.aop.MethodBeforeAdvice;

/** 
 * Ticket checking functionality for implementers of IRepositoryAdmin.
 *
 * All calls to IRepositoryAdmin must have either ICredential or ITicket as
 * their first parameter. In any case, the parameter must not be null.
 * 
 * If the first argument is missing then it throws an AccessDeniedException
 *
 * @author Fiona Malikoff 
 */
public class CheckCredentialTicketBeforeAdvice implements MethodBeforeAdvice {

	protected Logger log = Logger.getLogger(CheckCredentialTicketBeforeAdvice.class);

	/** @throws AccessDeniedException if the first parameter is null or it is an unrecognised ticket.
	 *  @throws RuntimeRepositoryException if the method has as its first parameter an object that is not
	 *    an ICredential or an ITicket
	 */
	public void before(Method m, Object[] args, Object target) 
			throws AccessDeniedException,	RepositoryRuntimeException {
		// assume that the first argument is the ticket or credential

		if ( log.isDebugEnabled() ) {
			log.debug("Method "+m.getName()+" Checking credential/ticket "+args[0]);
		}
		
		if (  args[0] == null ) { 

			throw new AccessDeniedException("No ticket/credential supplied. Access to repository denied.");

		} else {

			if ( ITicket.class.isInstance(args[0]) ) {

				IRepositoryAdmin repository = (IRepositoryAdmin) target;
				if ( ! repository.isTicketOkay((ITicket) args[0]) ) {
					log.error("Supplied ticket not recognised. It may have timed out. Please log in again.");
					throw new AccessDeniedException("Supplied ticket not recognised. It may have timed out. Please log in again.");
				}

			} else if ( ! ICredentials.class.isInstance(args[0]) ) {
				
				String error = "Method has wrong signature. Method "+m.getName()+" has CheckCredentialTicketBeforeAdvice applied to it, but the first argument is a "
					+args[0].getClass().getName()+". It must be either a ICredential or a ITicket";
				log.error(error);
				throw new RepositoryRuntimeException(error);
				
			}
		}
	}

}


/*
 * Created on Jan 4, 2005
 */
package org.lamsfoundation.lams.contentrepository.dao.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.log4j.Logger;
import org.lamsfoundation.lams.contentrepository.CrCredential;
import org.lamsfoundation.lams.contentrepository.ICredentials;
import org.lamsfoundation.lams.contentrepository.IWorkspace;
import org.lamsfoundation.lams.contentrepository.RepositoryRuntimeException;
import org.lamsfoundation.lams.contentrepository.dao.ICredentialDAO;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;


/**
 *  
 * Implements the credentials lookup using Hibernate.
 * 
 * @author Fiona Malikoff
 *
 */
public class CredentialDAO extends HibernateDaoSupport implements ICredentialDAO {
  
	protected Logger log = Logger.getLogger(CredentialDAO.class);	

	/** 
	 * Checks whether a user can login to this workspace. The 
	 * Credential must include the password.
	 */
	public boolean checkCredential(ICredentials credential, IWorkspace workspace) 
						throws RepositoryRuntimeException {
		if ( log.isDebugEnabled() )
			log.debug("Checking credential "+credential+" for workspace "+workspace);

		if ( credential == null || workspace == null || workspace.getWorkspaceId() == null )
			return false;
		
		return checkCredentialInternal(credential, workspace);
	}

	/** 
	 * Checks whether a user can login to the repository. The 
	 * Credential must include the password.
	 */
	public boolean checkCredential(ICredentials credential) 
						throws RepositoryRuntimeException {
		if ( log.isDebugEnabled() )
			log.debug("Checking credential "+credential);

		if ( credential == null  )
			return false;
		
		return checkCredentialInternal(credential, null);
	}
	
	/** 
	 * Checks whether a user can login to the repository. The 
	 * Credential must include the password.
	 * 
	 * If workspace defined then checks workspace, otherwise just checks password
	 */
	public boolean checkCredentialInternal(ICredentials credential, IWorkspace workspace) 
						throws RepositoryRuntimeException {

		// given the input credential, is there a credential matching
		// in the database? why go to so much trouble in this code? I'm trying
		// to avoid converting the char[] to a string.
		// There will be better ways to do this, but this will do for starters
		// until I get more familiar with Spring.
		
		boolean credentialMatched = false;
		
		Session hibernateSession = getSession();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = hibernateSession.connection();
			
			StringBuffer buf = new StringBuffer(200);
			buf.append("select count(*) num from lams_cr_credential c");
			if ( workspace != null ) {
				buf.append(", lams_cr_workspace_credential wc ");
			}
			buf.append(" where c.name = \"");
			buf.append(credential.getName());
			buf.append("\" and c.password = \""); 
			buf.append(credential.getPassword());
			buf.append("\"");
			if ( workspace != null ) {
					buf.append(" and wc.credential_id = c.credential_id ");
					buf.append(" and wc.workspace_id = ");
					buf.append(workspace.getWorkspaceId());
			}
	
			ps = conn.prepareStatement(buf.toString());
			ps.execute();
			ResultSet rs = ps.getResultSet();
			if ( rs.next() )  {
				int val = rs.getInt("num");
				if ( val > 0 ) {
					credentialMatched = true;
					if ( val > 1 ) {
						log.warn("More than one credential found for workspace "
									+workspace.getWorkspaceId()
									+" credential name "
									+credential.getName());
					}
				}
			}
			
		} catch (HibernateException e) {
			log.error("Hibernate exception occured during login. ",e);
			throw new RepositoryRuntimeException("Unable to login due to internal error.", e);
		} catch (SQLException se) {
			log.error("SQL exception occured during login. ",se);
			throw new RepositoryRuntimeException("Unable to login due to internal error.", se);
		} finally {
			if ( ps != null ) {
				try {
					ps.close();
				} catch (SQLException se2) {
					log.error("SQL exception occured during login, while closing statement. ",se2);
					throw new RepositoryRuntimeException("Unable to login due to internal error.", se2);
				}
			}
		}

		return credentialMatched;
	}

	public void insert(Object object) {
		this.getHibernateTemplate().save(object);		
	}

	public void update(Object object) {
		this.getHibernateTemplate().update(object);
	}

	public void delete(Object object) {
		this.getHibernateTemplate().delete(object);
	}

	public CrCredential findByName(String name)  {

		log.debug("Getting credential for name "+name);
		
		String queryString = "from CrCredential as c where c.name = ?";
		List credentials = getHibernateTemplate().find(queryString,name);
		
		if(credentials.size() == 0){
			log.debug("No credentials found");
			return null;
		}else{
			return (CrCredential)credentials.get(0);
		}
	}
	public List findAll(Class objClass) {
		String query="from obj in class " + objClass.getName(); 
		return this.getHibernateTemplate().find(query);
	}
	
}

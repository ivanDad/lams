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

package org.lamsfoundation.lams.tool.dokumaran.util;

import org.lamsfoundation.lams.contentrepository.client.ToolContentHandler;

/**
 * Simple client for accessing the content repository.
 *
 * @author Fiona Malikoff
 */
public class DokumaranToolContentHandler extends ToolContentHandler {

    private static String repositoryWorkspaceName = "dokumaranworkspace";
    private static String repositoryUser = "dokumaran";
    private static char[] repositoryId = { 'l', 'a', 'm', 's', '-', 'd', 'o', 'k', 'u', 'm', 'a', 'r', 'a', 'n' };

    /**
     *
     */
    public DokumaranToolContentHandler() {
	super();
    }

    @Override
    public String getRepositoryWorkspaceName() {
	return repositoryWorkspaceName;
    }

    @Override
    public String getRepositoryUser() {
	return repositoryUser;
    }

    @Override
    public char[] getRepositoryId() {
	return repositoryId;
    }

}

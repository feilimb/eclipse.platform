/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.core.history;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.provider.FileHistoryProvider;

/**
 * This is API to access individual file histories.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients can
 * instead subclass {@link FileHistoryProvider}.
 * @since 3.2
 *
 */
public interface IFileHistoryProvider {
	
	/**
	 * Constant flag used with
	 * {@link #getFileHistoryFor(IResource, int, IProgressMonitor)} to indicate
	 * no flags.
	 */
	public static final int NONE = 0;
	
	/**
	 * Constant flag used with {@link #getFileHistoryFor(IResource, int, IProgressMonitor)}
	 * to indicate that only a single revision is desired.
	 */
	public static final int SINGLE_REVISION = 1;
	
	/**
	 * Constant flag used with
	 * {@link #getFileHistoryFor(IResource, int, IProgressMonitor)} to indicate
	 * that the resulting history will be restricted to a single line-of-descent
	 * (e.g. a single branch). In this mode, the
	 * {@link IFileHistory#getContributors(IFileRevision)} and
	 * {@link IFileHistory#getTargets(IFileRevision)} should either return zero
	 * or one revision.
	 */
	public static final int SINGLE_LINE_OF_DESCENT = 2;

	/**
	 * Returns the file history for the given in resource. If the flags contains
	 * {@link #SINGLE_REVISION} then only the revision corresponding to the base
	 * corresponding to the local resource is fetched. If the flags contains
	 * {@link #SINGLE_LINE_OF_DESCENT} the resulting history will be restricted
	 * to a single line-of-descent (e.g. a single branch). In this mode, the
	 * {@link IFileHistory#getContributors(IFileRevision)} and
	 * {@link IFileHistory#getTargets(IFileRevision)} should either return zero
	 * or one revision. If both flags are present, {@link #SINGLE_REVISION}
	 * should take precedence.
	 * 
	 * @param resource
	 *            the resource
	 * @param flags
	 *            to indicate what revisions should be included in the history
	 * @param monitor
	 *            a progress monitor
	 * @return the history of the file
	 */
	public abstract IFileHistory getFileHistoryFor(IResource resource, int flags, IProgressMonitor monitor);
	

	/**
	 * Returns the file revision of the passed in resource or null if that file revision cannot be
	 * determined
	 * 
	 * @param resource the resource
	 * @return the file revision belonging to the passed in resource or null
	 */
	public abstract IFileRevision getWorkspaceFileRevision(IResource resource);
	
	/**
	 * Returns an {@link IFileHistory} for the specified {@link IFileStore}.
	 * @param store an IFileStore
	 * @param monitor  a progress monitor
	 * @return the history for the IFileStore
	 */
	public abstract IFileHistory getFileHistoryFor(IFileStore store, int flags, IProgressMonitor monitor);
	
}

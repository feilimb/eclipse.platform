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
package org.eclipse.team.internal.core.mapping;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.core.mapping.provider.SynchronizationScopeManager;

/**
 * Concrete implementation of the {@link ISynchronizationScope}
 * interface for use by clients.
 * <p>
 * This class is not intended to be subclasses by clients.
 * 
 * @see org.eclipse.core.resources.mapping.ResourceMapping
 * 
 * @since 3.2
 */
public class ResourceMappingScope extends AbstractResourceMappingScope {
	
	private ResourceMapping[] inputMappings;
	private final Map mappingsToTraversals = new HashMap();
	private boolean hasAdditionalMappings;
	private boolean hasAdditionalResources;
	private final CompoundResourceTraversal compoundTraversal = new CompoundResourceTraversal();
	private final SynchronizationScopeManager manager;
	
	public ResourceMappingScope(ResourceMapping[] selectedMappings, SynchronizationScopeManager manager) {
		inputMappings = selectedMappings;
		this.manager = manager;
	}
	
	/**
	 * Add the mapping and its traversals to the scope. This method should
	 * only be invoked during the scope building process.
	 * @param mapping the mapping being added to the scope
	 * @param traversals the traversals for that mapping
	 */
	public ResourceTraversal[] addMapping(ResourceMapping mapping, ResourceTraversal[] traversals) {
		ResourceTraversal[] newTraversals = compoundTraversal.getUncoveredTraversals(traversals);
		mappingsToTraversals.put(mapping, traversals);
		compoundTraversal.addTraversals(traversals);
		return newTraversals;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getInputMappings()
	 */
	public ResourceMapping[] getInputMappings() {
		return inputMappings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getMappings()
	 */
	public ResourceMapping[] getMappings() {
		if (mappingsToTraversals.isEmpty())
			return inputMappings;
		return (ResourceMapping[]) mappingsToTraversals.keySet().toArray(new ResourceMapping[mappingsToTraversals.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getTraversals()
	 */
	public ResourceTraversal[] getTraversals() {
		return compoundTraversal.asTraversals();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#getTraversals(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	public ResourceTraversal[] getTraversals(ResourceMapping mapping) {
		return (ResourceTraversal[])mappingsToTraversals.get(mapping);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.IResourceMappingOperationScope#hasAdditionalMappings()
	 */
	public boolean hasAdditionalMappings() {
		return hasAdditionalMappings;
	}
	
	/**
	 * Set whether the scope has additional mappings to the input mappings.
	 * This method should only be invoked during the scope building process.
	 * @param hasAdditionalMappings whether the scope has additional mappings
	 */
	public void setHasAdditionalMappings(boolean hasAdditionalMappings) {
		this.hasAdditionalMappings = hasAdditionalMappings;
	}

	/**
	 * Set whether this scope has additional resources.
	 * This method should only be invoked during the scope building process.
	 * @param hasAdditionalResources whether the scope has additional resources
	 */
	public void setHasAdditionalResources(boolean hasAdditionalResources) {
		this.hasAdditionalResources = hasAdditionalResources;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScope#hasAdditonalResources()
	 */
	public boolean hasAdditonalResources() {
		return hasAdditionalResources;
	}

	public CompoundResourceTraversal getCompoundTraversal() {
		return compoundTraversal;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceMappingScope#asInputScope()
	 */
	public ISynchronizationScope asInputScope() {
		return new ResourceMappingInputScope(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ISynchronizationScope#getProjects()
	 */
	public IProject[] getProjects() {
		ResourceMappingContext context = getContext();
		if (context instanceof RemoteResourceMappingContext) {
			RemoteResourceMappingContext rrmc = (RemoteResourceMappingContext) context;
			return rrmc.getProjects();
		}
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ISynchronizationScope#getContext()
	 */
	public ResourceMappingContext getContext() {
		return manager.getContext();
	}

	public void refresh(ResourceMapping[] mappings) {
		manager.refresh(mappings);
	}
}

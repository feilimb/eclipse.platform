/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.structuremergeviewer;

import java.util.Iterator;

import org.eclipse.compare.ITypedElement;

/**
 * Interface used to compare hierarchical structures.
 * It is used by the differencing engine.
 * <p>
 * Clients typically implement this interface in an adaptor class which 
 * wrappers the objects to be compared.
 *
 * @see org.eclipse.compare.ResourceNode
 * @see Differencer
 */
public interface IStructureComparator {

	/**
	 * Returns an iterator for all children of this object or <code>null</code>
	 * if there are no children.
	 *
	 * @return an array with all children of this object, or an empty array if there are no children
	 */
	Object[] getChildren();

	/**
	 * Returns whether some other object is "equal to" this one
	 * with respect to a structural comparison. For example, when comparing
	 * Java class methods, <code>equals</code> would return <code>true</code>
	 * if two methods have the same signature (the argument names and the 
	 * method body might differ).
	 *
	 * @param other the reference object with which to compare
	 * @return <code>true</code> if this object is the same as the other argument; <code>false</code> otherwise
	 * @see java.lang.Object#equals
	 */
	boolean equals(Object other);
}

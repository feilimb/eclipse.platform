/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.contentmergeviewer;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.IDocument;


/**
 * Defines a subrange in a document.
 * <p>
 * It is used by text viewers that can work on a subrange of a document. For example,
 * a text viewer for Java compilation units might use this to restrict the view
 * to a single method.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see TextMergeViewer
 * @see org.eclipse.compare.structuremergeviewer.DocumentRangeNode
 */
public interface IDocumentRange {
	
	/**
	 * Returns the underlying document.
	 * 
	 * @return the underlying document
	 */
	IDocument getDocument();
	
	/**
	 * Returns a position that specifies a subrange in the underlying document,
	 * or <code>null</code> if this document range spans the whole underlying document.
	 * 
	 * @return a position that specifies a subrange in the underlying document, or <code>null</code>
	 */
	Position getRange();
}

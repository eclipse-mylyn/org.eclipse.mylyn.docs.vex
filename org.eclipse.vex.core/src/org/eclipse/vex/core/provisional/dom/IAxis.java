/*******************************************************************************
 * Copyright (c) 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.provisional.dom;

import java.util.List;

import org.eclipse.vex.core.IFilter;

/**
 * An Axis represents an iterable sequence of nodes. It is the main concept to navigate in the DOM, inspired by the
 * XPath axes. Besides implementing the Iterable interface, Axis provides a fluent interface to limit the sequence by
 * several criteria which are evaluated in the following order:
 * <ol>
 * <li>only nodes within a range in the content</li>
 * <li>omit text nodes</li>
 * <li>only nodes which match a given chain of filters</li>
 * <li>only nodes within a range of indices</li>
 * </ol>
 * 
 * Example:
 * 
 * <pre>
 * Iterator&lt;Node&gt; elementsInRange = parent.children().in(new ContentRange(1, 24)).matching(new IFilter&lt;Node&gt;() {
 * 	public boolean matches(Node node) {
 * 		return node instanceof Element;
 * 	}
 * }).iterator();
 * </pre>
 * 
 * The Iterable interface makes it very convenient to use the axis in a foreach loop.
 * 
 * @author Florian Thienel
 * @see <a href="http://www.w3.org/TR/xpath/#axes">http://www.w3.org/TR/xpath/#axes</a>
 */
public interface IAxis<T extends INode> extends Iterable<T> {

	/**
	 * @return the source node of this axis
	 */
	INode getSourceNode();

	/**
	 * Limit the nodes of this axis to the given content range. Cannot be combined with 'before' or 'after'. Can be
	 * applied only once.
	 * 
	 * @param range
	 *            the content range
	 * @return a reference to this axis
	 * @see IAxis#before
	 * @see IAxis#after
	 */
	IAxis<? extends T> in(ContentRange range);

	/**
	 * Limit the nodes of this axis to nodes before the given offset. Cannot be combined with 'in' or 'after'. Can be
	 * applied only once.
	 * 
	 * @param beforeOffset
	 *            the offset
	 * @return a reference to this axis
	 * @see IAxis#in
	 * @see IAxis#after
	 */
	IAxis<? extends T> before(int beforeOffset);

	/**
	 * Limit the nodes of this axis to nodes after the given offset. Cannot be combined with 'in' or 'before'. Can be
	 * applied only once.
	 * 
	 * @param afterOffset
	 *            the offset
	 * @return a reference to this axis
	 * @see IAxis#in
	 * @see IAxis#before
	 */
	IAxis<? extends T> after(int afterOffset);

	/**
	 * Do not include Text nodes in this axis.
	 * 
	 * @return a reference to this axis
	 */
	IAxis<? extends T> withoutText();

	/**
	 * Limit the nodes of this axis to nodes matching the given filter. Can be applied multiple times to chain multiple
	 * filters.
	 * 
	 * @param filter
	 *            the filter
	 * @return a reference to this axis
	 * @see Filters
	 */
	IAxis<? extends T> matching(IFilter<INode> filter);

	/**
	 * Start the sequence of this axis at the given index. Can be applied only once.
	 * 
	 * @param startIndex
	 *            the start index
	 * @return a reference to this axis
	 * @see IAxis#to
	 */
	IAxis<? extends T> from(int startIndex);

	/**
	 * End the sequence of this axis at the given index. Can be applied only once.
	 * 
	 * @param endIndex
	 *            the end index
	 * @return a reference to this axis
	 * @see IAxis#from
	 */
	IAxis<? extends T> to(int endIndex);

	/**
	 * @return true if this sequence of this axis with all given criteria applied is empty
	 */
	boolean isEmpty();

	/**
	 * Create a list with all nodes of this axis that satisfy all given criteria. Be aware that this method goes through
	 * the whole sequence to collect all matching nodes.
	 * 
	 * @return all nodes to which the given criteria apply as list
	 */
	List<T> asList();

	/**
	 * @return the first node of this axis that satisfies all given critera
	 */
	T first();

	/**
	 * Find the last node of this axis which satisfies all given criteria. Be aware that this method goes through the
	 * whole sequence to find the last matching node.
	 * 
	 * @return the last node of this axis that satisfies all given criteria
	 */
	T last();

	/**
	 * @param index
	 *            the index
	 * @return the node with the given index in the resulting sequence of nodes
	 */
	T get(int index);

	/**
	 * Visit all nodes of this axis that satisfy all given criteria.
	 * 
	 * @param visitor
	 *            the visitor
	 */
	void accept(INodeVisitor visitor);

	/**
	 * Count all nodes of this axis that satisfy all given criteria. Be aware that this method goes through the whole
	 * sequence to count all matching nodes.
	 * 
	 * @return the number of nodes of this axis that satisfy all given criteria
	 */
	int count();

}
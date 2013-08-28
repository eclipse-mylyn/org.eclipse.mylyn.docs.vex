/*******************************************************************************
 * Copyright (c) 2013 Carsten Hiesserich and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Carsten Hiesserich - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import org.eclipse.vex.core.IValidationResult;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;

/**
 * A representation of an XML processing insctruction in the DOM. PI's have textual content, a start and an end tag.
 * 
 * @author Carsten Hiesserich
 */
public class ProcessingInstruction extends Node implements IProcessingInstruction {

	private String target = null;

	/**
	 * Create a new processing instruction.
	 * 
	 * @param target
	 *            The target for this processing instruction.
	 * @throws DocumentValidationException
	 *             If the given String is no valid target.
	 */
	public ProcessingInstruction(final String target) throws DocumentValidationException {
		setTarget(target);
	}

	@Override
	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isKindOf(final INode node) {
		if (!(node instanceof IProcessingInstruction)) {
			return false;
		}
		return true;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public void setTarget(final String target) throws DocumentValidationException {
		final IValidationResult resultTarget = XML.validateProcessingInstructionTarget(target);
		if (!resultTarget.isOK()) {
			throw new DocumentValidationException(resultTarget.getMessage());
		}

		this.target = target;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();

		sb.append("ProcessingInstruction (");
		if (target != null) {
			sb.append(target);
			sb.append(") (");
		}
		if (isAssociated()) {
			sb.append(getStartOffset());
			sb.append(",");
			sb.append(getEndOffset());
		} else {
			sb.append("n/a");
		}
		sb.append(")");

		return sb.toString();
	}

}

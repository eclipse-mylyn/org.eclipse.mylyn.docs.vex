/*******************************************************************************
 * Copyright (c) 2004, 2010 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Dave Holroyd - Proper specificity for wildcard selector
 *     John Austin - Implement sibling selectors
 *     Florian Thienel - bug 306639 - remove serializability from StyleSheet
 *                       and dependend classes
 *******************************************************************************/
package org.eclipse.vex.core.internal.css;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.vex.core.internal.dom.CommentElement;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.RootElement;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SiblingSelector;

/**
 * Represents a pairing of a selector with a list of styles. This does not exactly correspond to a rule in a style
 * sheet; there is only one selector associated with an instance of this class, whereas multiple selectors may be
 * associated with a style sheet rule.
 * 
 * Note: <code>Rule</code> implements the <code>Comparable</code> interface in order to be sorted by "specificity" as
 * defined by the CSS spec. However, this ordering is <em>not</em> consistent with <code>equals</code> (rules with the
 * same specificity may not be equal). Therefore, <code>Rule</code> objects should not be used with sorted collections
 * or maps in the <code>java.util</code> package, unless a suitable <code>Comparator</code> is also used.
 */
public class Rule {

	private final Selector selector;
	private final List<PropertyDecl> propertyDecls = new ArrayList<PropertyDecl>();

	/**
	 * Class constructor.
	 * 
	 * @param selector
	 *            Selector for the rule.
	 */
	public Rule(final Selector selector) {
		this.selector = selector;
	}

	/**
	 * Adds a property declaration to the rule.
	 * 
	 * @param decl
	 *            new property declaration to add
	 */
	public void add(final PropertyDecl decl) {
		propertyDecls.add(decl);
	}

	/**
	 * Returns the selector for the rule.
	 */
	public Selector getSelector() {
		return selector;
	}

	/**
	 * Returns an array of the property declarations in this rule.
	 */
	public PropertyDecl[] getPropertyDecls() {
		return propertyDecls.toArray(new PropertyDecl[propertyDecls.size()]);
	}

	/**
	 * Calculates the specificity for the selector associated with this rule. The specificity is represented as an
	 * integer whose base-10 representation, xxxyyyzzz, can be decomposed into the number of "id" selectors (xxx),
	 * "class" selectors (yyy), and "element" selectors (zzz). Composite selectors result in a recursive call.
	 */
	public int getSpecificity() {
		return specificity(getSelector());
	}

	/**
	 * Returns true if the given element matches this rule's selector.
	 * 
	 * @param element
	 *            Element to check.
	 */
	public boolean matches(final Element element) {
		return matches(selector, element);
	}

	// ==================================================== PRIVATE

	/**
	 * Returns true if the given element matches the given selector.
	 */
	private static boolean matches(final Selector selector, final Element element) {

		if (element == null) {
			// This can happen when, e.g., with the rule "foo > *".
			// Since the root element matches the "*", we check if
			// its parent matches "foo", but of course its parent
			// is null
			return false;
		}

		final int selectorType = selector.getSelectorType();

		switch (selectorType) {
		case Selector.SAC_CONDITIONAL_SELECTOR:
			// This little wart is the product of a mismatch btn the CSS
			// spec an the Flute parser. CSS treats pseudo-elements as elements
			// attached to their parents, while Flute treats them like
			// attributes
			final ConditionalSelector cs = (ConditionalSelector) selector;
			if (cs.getCondition().getConditionType() == Condition.SAC_PSEUDO_CLASS_CONDITION) {
				if (element instanceof PseudoElement) {
					final AttributeCondition ac = (AttributeCondition) cs.getCondition();
					return ac.getValue().equals(element.getLocalName()) && matches(cs.getSimpleSelector(), element.getParentElement());
				} else if (element instanceof CommentElement) {
					final AttributeCondition ac = (AttributeCondition) cs.getCondition();
					return CommentElement.CSS_RULE_NAME.equals(ac.getValue()) && matches(cs.getSimpleSelector(), element.getParentElement());
				} else {
					return false;
				}
			} else {
				return matches(cs.getSimpleSelector(), element) && matchesCondition(cs.getCondition(), element);
			}

		case Selector.SAC_ANY_NODE_SELECTOR:
			// You'd think we land here if we have a * rule, but instead
			// it appears we get a SAC_ELEMENT_NODE_SELECTOR with
			// localName==null
			return true;

		case Selector.SAC_ROOT_NODE_SELECTOR:
			return element instanceof RootElement;

		case Selector.SAC_NEGATIVE_SELECTOR:
			break; // not yet supported

		case Selector.SAC_ELEMENT_NODE_SELECTOR:
			final String elementName = element.getLocalName();
			final String selectorName = ((ElementSelector) selector).getLocalName();
			if (selectorName == null) {
				// We land here if we have a wildcard selector (*) or
				// a pseudocondition w/o an element name (:before)
				// Probably other situations too (conditional w/o element
				// name? e.g. [attr=value])
				return true;
			}
			if (selectorName.equals(elementName)) {
				return true;
			}
			return false;

		case Selector.SAC_TEXT_NODE_SELECTOR:
			return false; // not yet supported

		case Selector.SAC_CDATA_SECTION_NODE_SELECTOR:
			return false; // not yet supported

		case Selector.SAC_PROCESSING_INSTRUCTION_NODE_SELECTOR:
			return false; // not yet supported

		case Selector.SAC_COMMENT_NODE_SELECTOR:
			return false; // not yet supported 

		case Selector.SAC_PSEUDO_ELEMENT_SELECTOR:
			final ElementSelector elementSelector = (ElementSelector) selector;
			return elementSelector.getLocalName().equals(element.getLocalName());

		case Selector.SAC_DESCENDANT_SELECTOR:
			final DescendantSelector ds = (DescendantSelector) selector;
			return matches(ds.getSimpleSelector(), element) && matchesAncestor(ds.getAncestorSelector(), element.getParentElement());

		case Selector.SAC_CHILD_SELECTOR:
			final DescendantSelector ds2 = (DescendantSelector) selector;
			final Element parent = element.getParentElement();
			return matches(ds2.getSimpleSelector(), element) && matches(ds2.getAncestorSelector(), parent);

		case Selector.SAC_DIRECT_ADJACENT_SELECTOR:

			final SiblingSelector ss = (SiblingSelector) selector;

			if (element != null && element.getParent() != null && matches(ss.getSiblingSelector(), element)) {

				// find next sibling
				final Iterator<Element> i = element.getParentElement().getChildElements().iterator();
				Element e = null;
				Element f = null;
				while (i.hasNext() && e != element) {
					f = e;
					e = i.next();
				}

				if (e == element) {
					return matches(ss.getSelector(), f);
				}
			}
			return false;

		default:
			// System.out.println("DEBUG: selector type not supported");
			// TODO: warning: selector not supported
		}
		return false;
	}

	/**
	 * Returns true if some ancestor of the given element matches the given selector.
	 */
	private static boolean matchesAncestor(final Selector selector, final Element element) {
		Element e = element;
		while (e != null) {
			if (matches(selector, e)) {
				return true;
			}
			e = e.getParentElement();
		}
		return false;
	}

	private static boolean matchesCondition(final Condition condition, final Element element) {

		AttributeCondition attributeCondition;
		String attributeName;
		String value;

		switch (condition.getConditionType()) {
		case Condition.SAC_PSEUDO_CLASS_CONDITION:
			return false;

		case Condition.SAC_ATTRIBUTE_CONDITION:
			attributeCondition = (AttributeCondition) condition;
			value = element.getAttributeValue(attributeCondition.getLocalName());
			if (attributeCondition.getValue() != null) {
				return attributeCondition.getValue().equals(value);
			} else {
				return value != null;
			}

		case Condition.SAC_ONE_OF_ATTRIBUTE_CONDITION:
		case Condition.SAC_CLASS_CONDITION:

			attributeCondition = (AttributeCondition) condition;

			if (condition.getConditionType() == Condition.SAC_CLASS_CONDITION) {
				attributeName = "class";
			} else {
				attributeName = attributeCondition.getLocalName();
			}

			value = element.getAttributeValue(attributeName);
			if (value == null) {
				return false;
			}
			final StringTokenizer st = new StringTokenizer(value);
			while (st.hasMoreTokens()) {
				if (st.nextToken().equals(attributeCondition.getValue())) {
					return true;
				}
			}
			return false;

		case Condition.SAC_AND_CONDITION:
			final CombinatorCondition ccon = (CombinatorCondition) condition;
			return matchesCondition(ccon.getFirstCondition(), element) && matchesCondition(ccon.getSecondCondition(), element);

		default:
			// TODO: warning: condition not supported
			System.out.println("Unsupported condition type: " + condition.getConditionType());
		}
		return false;
	}

	/**
	 * Calculates the specificity for a selector.
	 */
	private static int specificity(final Selector sel) {
		if (sel instanceof ElementSelector) {
			if (((ElementSelector) sel).getLocalName() == null) {
				// actually wildcard selector -- see comment in matches()
				return 0;
			} else {
				return 1;
			}
		} else if (sel instanceof DescendantSelector) {
			final DescendantSelector ds = (DescendantSelector) sel;
			return specificity(ds.getAncestorSelector()) + specificity(ds.getSimpleSelector());
		} else if (sel instanceof SiblingSelector) {
			final SiblingSelector ss = (SiblingSelector) sel;
			return specificity(ss.getSelector()) + specificity(ss.getSiblingSelector());
		} else if (sel instanceof NegativeSelector) {
			final NegativeSelector ns = (NegativeSelector) sel;
			return specificity(ns.getSimpleSelector());
		} else if (sel instanceof ConditionalSelector) {
			final ConditionalSelector cs = (ConditionalSelector) sel;
			return specificity(cs.getCondition()) + specificity(cs.getSimpleSelector());
		} else {
			return 0;
		}
	}

	/**
	 * Calculates the specificity for a condition.
	 */
	private static int specificity(final Condition cond) {
		if (cond instanceof CombinatorCondition) {
			final CombinatorCondition cc = (CombinatorCondition) cond;
			return specificity(cc.getFirstCondition()) + specificity(cc.getSecondCondition());
		} else if (cond instanceof AttributeCondition) {
			if (cond.getConditionType() == Condition.SAC_ID_CONDITION) {
				return 1000000;
			} else {
				return 1000;
			}
		} else {
			return 0;
		}
	}
}

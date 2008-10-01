/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xml.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.wst.xml.vex.core.internal.core.Insets;
import org.eclipse.wst.xml.vex.core.internal.css.CSS;
import org.eclipse.wst.xml.vex.core.internal.css.Styles;
import org.eclipse.wst.xml.vex.core.internal.dom.Element;


/**
 * An anonymous box that contains the table row groups for a table. This box
 * is generated by a TableBox and assumes the margins and borders of the 
 * table element.
 */
public class TableBodyBox extends AbstractBlockBox {

    public TableBodyBox(LayoutContext context, TableBox parent, int startOffset, int endOffset) {
        super(context, parent, startOffset, endOffset);
    }

    protected List createChildren(final LayoutContext context) {
        // TODO Auto-generated method stub
        
        // Walk children:
        //     each table-*-group gets a non-anonymous TableRowGroupBox
        //     runs of others get anonymous TableRowGroupBox
        
        final List children = new ArrayList();
        
        this.iterateChildrenByDisplayStyle(context.getStyleSheet(), childDisplayStyles, new ElementOrRangeCallback() {
            public void onElement(Element child, String displayStyle) {
                children.add(new TableRowGroupBox(context, TableBodyBox.this, child));
            }
            public void onRange(Element parent, int startOffset, int endOffset) {
                children.add(new TableRowGroupBox(context, TableBodyBox.this, startOffset, endOffset));
            }
        });

        return children;
    }

    /**
     * Return the insets of the parent box.
     */
    public Insets getInsets(LayoutContext context, int containerWidth) {
        if (this.getParent().getElement() != null) {
            Styles styles = context.getStyleSheet().getStyles(this.getParent().getElement());
            return AbstractBox.getInsets(styles, containerWidth);
        } else {
            return Insets.ZERO_INSETS;
        }
    }

    public void paint(LayoutContext context, int x, int y) {
        this.drawBox(context, this.getParent().getElement(), x, y, this.getParent().getWidth(), true);
        this.paintChildren(context, x, y);
    }
    
    
    //======================================================== PRIVATE

    private static Set childDisplayStyles = new HashSet();
    
    static {
        childDisplayStyles.add(CSS.TABLE_ROW_GROUP);
        childDisplayStyles.add(CSS.TABLE_HEADER_GROUP);
        childDisplayStyles.add(CSS.TABLE_FOOTER_GROUP);
    }

}

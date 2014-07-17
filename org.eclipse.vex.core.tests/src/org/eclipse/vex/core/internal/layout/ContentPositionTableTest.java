package org.eclipse.vex.core.internal.layout;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ContentPositionTableTest {

	FakeGraphics g;
	LayoutContext context;

	@Before
	public void setUp() throws Exception {

		// set CSS
		final String css = "root   {display:block}" + "inline {display:inline}" + "table  {display:table}" + "tcap   {display:table-caption}" + "td     {display:table-cell}"
				+ "tc     {display:table-column}" + "tcg    {display:table-column-group}" + "tfg    {display:table-footer-group}" + "thg    {display:table-header-group}"
				+ "tr     {display:table-row}" + "trg    {display:table-row-group}";
		final StyleSheet styleSheet = new StyleSheetReader().read(css);

		// FakeGraphics uses a fixed char width of 6
		g = new FakeGraphics();

		context = new LayoutContext();
		context.setBoxFactory(new CssBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(styleSheet);
		context.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));
	}

	@Test
	public void testViewToModel() throws Exception {
		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement table = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "table"));
		final IElement row1 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col11 = doc.insertElement(row1.getEndOffset(), new QualifiedName(null, "td"));
		final IElement row2 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col21 = doc.insertElement(row2.getEndOffset(), new QualifiedName(null, "td"));
		doc.insertText(col11.getEndOffset(), "line1 line2 line3");
		doc.insertText(col21.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		assertEquals(col11.getStartPosition().moveBy(1), rootBox.viewToModel(context, 0, 0));
		assertEquals(row1.getEndPosition(), rootBox.viewToModel(context, 100, 0));
		assertEquals(col21.getStartPosition().moveBy(1), rootBox.viewToModel(context, 0, 45));
		assertEquals(row2.getEndPosition(), rootBox.viewToModel(context, 100, 45));
	}

	@Test
	public void testGetNextLinePositionXLeft() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement table = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "table"));
		final IElement row1 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col11 = doc.insertElement(row1.getEndOffset(), new QualifiedName(null, "td"));
		final IElement row2 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col21 = doc.insertElement(row2.getEndOffset(), new QualifiedName(null, "td"));
		doc.insertText(col11.getEndOffset(), "line1 line2 line3");
		doc.insertText(col21.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		final ContentPosition linePosition = col11.getStartPosition().moveBy(1);
		ContentPosition nextLinePos = rootBox.getNextLinePosition(context, linePosition, 0);
		assertEquals(col11.getStartOffset() + 7, nextLinePos.getOffset()); // line2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 0);
		assertEquals(col11.getStartOffset() + 13, nextLinePos.getOffset()); // line3
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 0);
		assertEquals(row1.getEndOffset() + 1, nextLinePos.getOffset()); // Between row1 and row2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 0);
		assertEquals(col21.getStartOffset() + 1, nextLinePos.getOffset()); // row2 - line1
	}

	@Test
	@Ignore
	// CHI: currently ignored (see bug 421401)
	public void testGetNextLinePositionXRight() throws Exception {
		// This is the same test as before, but with a x position at the rigth of the table cells

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement table = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "table"));
		final IElement row1 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col11 = doc.insertElement(row1.getEndOffset(), new QualifiedName(null, "td"));
		final IElement row2 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col21 = doc.insertElement(row2.getEndOffset(), new QualifiedName(null, "td"));
		doc.insertText(col11.getEndOffset(), "line1 line2 line3");
		doc.insertText(col21.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		final ContentPosition linePosition = col11.getStartPosition().moveBy(6); // end of line 1
		ContentPosition nextLinePos = rootBox.getNextLinePosition(context, linePosition, 35); // X=35 is inside the space
		assertEquals(col11.getStartOffset() + 13, nextLinePos.getOffset()); // end of line2 (after the space)
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 35);
		assertEquals(col11.getStartOffset() + 18, nextLinePos.getOffset()); // placeholder at end of line3
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 35);
		assertEquals(row1.getEndOffset() + 1, nextLinePos.getOffset()); // Between row1 and row2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 28); // This time before the space
		assertEquals(col21.getStartOffset() + 6, nextLinePos.getOffset()); // row2 - end of line1 (after the '1`', before the space)
	}

	@Test
	public void testGetPreviousLinePosition() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement table = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "table"));
		final IElement row1 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col11 = doc.insertElement(row1.getEndOffset(), new QualifiedName(null, "td"));
		final IElement row2 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col21 = doc.insertElement(row2.getEndOffset(), new QualifiedName(null, "td"));
		doc.insertText(col11.getEndOffset(), "line1 line2 line3");
		doc.insertText(col21.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		// We test here with a x position inside the table cell.
		final ContentPosition linePosition = col21.getEndPosition().moveBy(-1);
		ContentPosition prevLinePos = rootBox.getPreviousLinePosition(context, linePosition, 2);
		assertEquals(col21.getEndOffset() - 11, prevLinePos.getOffset()); // start of line2
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 2);
		assertEquals(col21.getStartOffset() + 1, prevLinePos.getOffset()); // start of line 1
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 2);
		assertEquals(row1.getEndOffset() + 1, prevLinePos.getOffset()); // Between row1 and row2
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 2);
		assertEquals(col11.getEndOffset() - 5, prevLinePos.getOffset());
	}

	@Test
	@Ignore
	// CHI: currently ignored (see bug 421401)
	public void testGetPreviousLinePositionNoSpace() throws Exception {
		// Same test as before, but this time there is no space to split at

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement table = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "table"));
		final IElement row1 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col11 = doc.insertElement(row1.getEndOffset(), new QualifiedName(null, "td"));
		final IElement row2 = doc.insertElement(table.getEndOffset(), new QualifiedName(null, "tr"));
		final IElement col21 = doc.insertElement(row2.getEndOffset(), new QualifiedName(null, "td"));
		doc.insertText(col11.getEndOffset(), "line11line22line33");
		doc.insertText(col21.getEndOffset(), "line11line22line33");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		// We test here with a x position inside the table cell. A position more to the right would
		// move between the rows
		final ContentPosition linePosition = col21.getEndPosition().moveBy(-1);
		ContentPosition prevLinePos = rootBox.getPreviousLinePosition(context, linePosition, 35);
		assertEquals(col21.getEndOffset() - 6, prevLinePos.getOffset()); // end of line2
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 35);
		assertEquals(col21.getEndOffset() - 12, prevLinePos.getOffset()); // end of line 1
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 35);
		assertEquals(row1.getEndOffset() + 1, prevLinePos.getOffset()); // Between row1 and row2
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 35);
		assertEquals(col11.getEndOffset(), prevLinePos.getOffset()); // row1 - placeholder at end of line3
	}

}

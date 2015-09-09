package org.eclipse.vex.core.internal.layout;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.ContentPosition;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Before;
import org.junit.Test;

public class ContentPositionTest {

	FakeGraphics g;
	LayoutContext context;

	@Before
	public void setUp() throws Exception {
		final URL url = this.getClass().getResource("test.css");
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet styleSheet = reader.read(url);

		g = new FakeGraphics();

		context = new LayoutContext();
		context.setBoxFactory(new CssBoxFactory());
		context.setGraphics(g);
		context.setStyleSheet(styleSheet);
		context.setWhitespacePolicy(new CssWhitespacePolicy(styleSheet));
	}

	@Test
	public void testPositionAtLineStart() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement block1 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		final IElement block2 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		doc.insertText(block1.getEndOffset(), "line1 line2 line3");
		doc.insertText(block2.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		ContentPosition caretPosition = rootBox.viewToModel(context, 0, 6); // begin of line 1
		assertEquals(block1.getStartPosition().moveBy(1), caretPosition);

		caretPosition = rootBox.viewToModel(context, 0, 18); // begin of line 2
		assertEquals(block1.getStartPosition().moveBy(7), caretPosition);
	}

	@Test
	public void testPositionAtLineEnd() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement block1 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		final IElement block2 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		doc.insertText(block1.getEndOffset(), "line1 line2 line3");
		doc.insertText(block2.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		ContentPosition caretPosition = rootBox.viewToModel(context, 100, 6); // end of line 1

		assertEquals(block1.getStartPosition().moveBy(6), caretPosition);
		Caret caret = rootBox.getCaret(context, caretPosition);
		assertEquals(0, caret.getY());

		caretPosition = rootBox.viewToModel(context, 100, 18); // end of line 2
		assertEquals(block1.getStartPosition().moveBy(12), caretPosition);
		caret = rootBox.getCaret(context, caretPosition);
		assertEquals(12, caret.getY());

		caretPosition = rootBox.viewToModel(context, 100, 30); // end of line 3
		assertEquals(block1.getEndPosition(), caretPosition);
		caret = rootBox.getCaret(context, caretPosition);
		assertEquals(24, caret.getY());
	}

	@Test
	public void testMoveForward() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement block1 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		doc.insertText(block1.getEndOffset(), "line1 line22 line3");

		context.setDocument(doc);
		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		ContentPosition caretPosition = block1.getStartPosition().moveBy(6); // At the end of line1 before the space
		Caret caret = rootBox.getCaret(context, caretPosition);
		assertEquals("Expceting caret in line 1", 0, caret.getY());

		caretPosition = caretPosition.moveBy(1); // Moving by 1 should place the caret at the start of the next line
		caret = rootBox.getCaret(context, caretPosition);
		assertEquals("Expceting caret in line 2", 12, caret.getY());
	}

	@Test
	public void testGetNextLinePosition() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement block1 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		final IElement block2 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		doc.insertText(block1.getEndOffset(), "line1 line22 line3");
		doc.insertText(block2.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);
		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		final ContentPosition linePosition = block1.getStartPosition().moveBy(1);
		ContentPosition nextLinePos = rootBox.getNextLinePosition(context, linePosition, 0);
		assertEquals(block1.getStartPosition().moveBy(7), nextLinePos); // line2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 0);
		assertEquals(block1.getStartPosition().moveBy(14), nextLinePos); // line3
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 0);
		assertEquals(block2.getStartPosition(), nextLinePos); // Between block1 and block2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 0);
		assertEquals(block2.getStartPosition().moveBy(1), nextLinePos); // block2 - line1
	}

	@Test
	public void testGetNextLinePosition_CaretAtLineEnd() throws Exception {
		/*
		 * The same test as before, but this time there is no space character to split the first block a second time. In
		 * this case, the last caret position in the second line will be before the last character of the second line.
		 * The position after that character is not reachable on the second line. It is actually at the beginning of the
		 * third line.
		 *
		 * In fact there is no common behaviour among other editors (like Word, Open Office or Notepad++), so we stick
		 * with this solution as it works fine for the moment and the case (no spaces in one line of text) is really
		 * rare.
		 */

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement block1 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		final IElement block2 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		doc.insertText(block1.getEndOffset(), "line1 line22line3");
		doc.insertText(block2.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		final ContentPosition linePosition = rootBox.viewToModel(context, 100, 6); // x=100 -> after end of line 1
		assertEquals(block1.getStartOffset() + 6, linePosition.getOffset()); // line2
		ContentPosition nextLinePos = rootBox.getNextLinePosition(context, linePosition, 100);
		assertEquals(block1.getStartOffset() + 12, nextLinePos.getOffset()); // actually before the last character of the second line
		final Caret caret = rootBox.getCaret(context, nextLinePos);
		assertEquals(12, caret.getY()); // line height is 12, so this is line 2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 100);
		assertEquals(block1.getEndPosition(), nextLinePos); // line3
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 100);
		assertEquals(block2.getStartPosition(), nextLinePos); // Between block1 and block2
		nextLinePos = rootBox.getNextLinePosition(context, nextLinePos, 100);
		assertEquals(block2.getStartOffset() + 6, nextLinePos.getOffset()); // block2 - end of line1
	}

	@Test
	public void testGetPreviousLinePosition() throws Exception {

		final Document doc = new Document(new QualifiedName(null, "root"));
		final IElement root = doc.getRootElement();

		final IElement block1 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		final IElement block2 = doc.insertElement(root.getEndOffset(), new QualifiedName(null, "block"));
		doc.insertText(block1.getEndOffset(), "line1 line2 line3");
		doc.insertText(block2.getEndOffset(), "line1 line2 line3");

		context.setDocument(doc);

		final RootBox rootBox = new RootBox(context, doc, 36);
		rootBox.layout(context, 0, Integer.MAX_VALUE);

		final ContentPosition linePosition = block2.getEndPosition().moveBy(-1);
		ContentPosition prevLinePos = rootBox.getPreviousLinePosition(context, linePosition, 0);
		assertEquals(block2.getEndPosition().moveBy(-11), prevLinePos); // start of line2
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 0);
		assertEquals(block2.getStartPosition().moveBy(1), prevLinePos); // start of line 1
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 0);
		assertEquals(block2.getStartPosition(), prevLinePos); // Between block1 and block2
		prevLinePos = rootBox.getPreviousLinePosition(context, prevLinePos, 0);
		assertEquals(block1.getEndPosition().moveBy(-5), prevLinePos); // block1 - start of line3
	}

}

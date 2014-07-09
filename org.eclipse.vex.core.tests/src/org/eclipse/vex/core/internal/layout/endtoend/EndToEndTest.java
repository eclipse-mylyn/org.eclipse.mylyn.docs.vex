/*******************************************************************************
 * Copyright (c) 2014 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout.endtoend;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.IStyleSheetProvider;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.io.DocumentReader;
import org.eclipse.vex.core.internal.widget.BaseVexWidget;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.xml.sax.SAXException;

/**
 * @author Florian Thienel
 */
public class EndToEndTest {

	private static final String UNIX_SEPARATOR = "\n";
	private static final String LINE_SEPARATOR = "line.separator";
	private static final DisplayDevice DD_72DPI = new DisplayDevice() {
		@Override
		public int getHorizontalPPI() {
			return 72;
		}

		@Override
		public int getVerticalPPI() {
			return 72;
		}
	};

	@Rule
	public TestName name = new TestName();

	@Test
	public void simpleParagraph() throws Exception {
		assertRenderingWorks();
	}

	private void assertRenderingWorks() throws IOException, ParserConfigurationException, SAXException {
		final StyleSheet styleSheet = readStyleSheet();
		final IDocument document = readInputDocument(styleSheet);

		final String expectedTrace = readExpectedTrace();
		final String actualTrace = traceRendering(document, styleSheet);

		assertEquals(expectedTrace, actualTrace);
	}

	private static String traceRendering(final IDocument document, final StyleSheet styleSheet) throws IOException, ParserConfigurationException, SAXException {
		final ByteArrayOutputStream traceBuffer = new ByteArrayOutputStream();
		final PrintStream printStream = createUnixPrintStream(traceBuffer);

		DisplayDevice.setCurrent(DD_72DPI);
		final TracingHostComponent hostComponent = new TracingHostComponent(printStream);
		final BaseVexWidget widget = new BaseVexWidget(hostComponent);
		widget.setDocument(document, styleSheet);
		widget.paint(hostComponent.createDefaultGraphics(), 0, 0);
		return new String(traceBuffer.toByteArray());
	}

	private static PrintStream createUnixPrintStream(final OutputStream target) {
		final String originalSeparator = System.getProperty(LINE_SEPARATOR);
		System.setProperty(LINE_SEPARATOR, UNIX_SEPARATOR);
		final PrintStream printStream = new PrintStream(target, true);
		System.setProperty(LINE_SEPARATOR, originalSeparator);
		return printStream;
	}

	private StyleSheet readStyleSheet() throws IOException {
		return new StyleSheetReader().read(getClass().getResource(styleSheetName()));
	}

	private IDocument readInputDocument(final StyleSheet styleSheet) throws IOException, ParserConfigurationException, SAXException {
		final DocumentReader reader = new DocumentReader();
		reader.setValidator(IValidator.NULL);
		reader.setStyleSheetProvider(new IStyleSheetProvider() {
			@Override
			public StyleSheet getStyleSheet(final DocumentContentModel documentContentModel) {
				return styleSheet;
			}
		});
		reader.setWhitespacePolicyFactory(CssWhitespacePolicy.FACTORY);
		return reader.read(getClass().getResource(inputName()));
	}

	private String readExpectedTrace() throws IOException {
		return readAndCloseStream(getClass().getResourceAsStream(outputName()));
	}

	private static String readAndCloseStream(final InputStream in) throws IOException {
		try {
			final ByteArrayOutputStream content = new ByteArrayOutputStream();
			final byte[] readBuffer = new byte[1024];
			int readCount;
			while ((readCount = in.read(readBuffer)) > 0) {
				content.write(readBuffer, 0, readCount);
			}
			return new String(content.toByteArray());
		} finally {
			in.close();
		}
	}

	private String inputName() {
		return name.getMethodName() + "-input.xml";
	}

	private String outputName() {
		return name.getMethodName() + "-output.txt";
	}

	private String styleSheetName() {
		return name.getMethodName() + ".css";
	}
}

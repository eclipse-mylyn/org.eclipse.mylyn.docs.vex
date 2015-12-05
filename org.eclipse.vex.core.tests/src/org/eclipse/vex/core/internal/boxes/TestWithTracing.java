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
package org.eclipse.vex.core.internal.boxes;

import static org.eclipse.vex.core.internal.boxes.BoxFactory.frame;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.horizontalBar;
import static org.eclipse.vex.core.internal.boxes.BoxFactory.verticalBlock;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.layout.endtoend.TracingHostComponent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author Florian Thienel
 */
public class TestWithTracing {

	private static final String UNIX_SEPARATOR = "\n";
	private static final String LINE_SEPARATOR = "line.separator";

	private RootBox rootBox;

	@Rule
	public TestName name = new TestName();

	@Before
	public void setUp() throws Exception {
		rootBox = new RootBox();
	}

	@Test
	public void verticalBlockHorizontalBar() throws Exception {
		final StructuralFrame frame = frame(verticalBlock(horizontalBar(10, Color.BLACK)), new Margin(10, 20, 30, 40), new Border(10), new Padding(15, 25, 35, 45));

		rootBox.appendChild(frame);
		rootBox.setWidth(300);

		final String expected = readExpectedTrace();
		final String actual = traceRendering(rootBox);

		assertEquals(expected, actual);
	}

	private static String traceRendering(final RootBox rootBox) {
		final ByteArrayOutputStream traceBuffer = new ByteArrayOutputStream();
		final PrintStream printStream = createUnixPrintStream(traceBuffer);

		DisplayDevice.setCurrent(DisplayDevice._72DPI);
		final TracingHostComponent hostComponent = new TracingHostComponent(printStream);
		final Graphics graphics = hostComponent.createDefaultGraphics();
		rootBox.layout(graphics);
		rootBox.paint(graphics);
		return new String(traceBuffer.toByteArray());
	}

	private static PrintStream createUnixPrintStream(final OutputStream target) {
		final String originalSeparator = System.getProperty(LINE_SEPARATOR);
		System.setProperty(LINE_SEPARATOR, UNIX_SEPARATOR);
		final PrintStream printStream = new PrintStream(target, true);
		System.setProperty(LINE_SEPARATOR, originalSeparator);
		return printStream;
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

	private String readExpectedTrace() throws IOException {
		return readAndCloseStream(getClass().getResourceAsStream(outputName()));
	}

	private String outputName() {
		return name.getMethodName() + "-output.txt";
	}

}

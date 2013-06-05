/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.vex.core.internal.core.DisplayDevice;
import org.eclipse.vex.core.internal.css.CssWhitespacePolicy;
import org.eclipse.vex.core.internal.css.IStyleSheetProvider;
import org.eclipse.vex.core.internal.css.MockDisplayDevice;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.StyleSheetReader;
import org.eclipse.vex.core.internal.dom.DummyValidator;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.core.tests.VEXCoreTestPlugin;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SpaceNormalizerTest {

	protected static IProject fTestProject;
	private static boolean fTestProjectInitialized;
	private static final String TEST_PROJECT_NAME = "testproject";
	private static final String PROJECT_FILES_FOLDER_NAME = "testResources";

	@Before
	public void setUp() throws Exception {
		DisplayDevice.setCurrent(new MockDisplayDevice(90, 90));
		if (fTestProjectInitialized) {
			return;
		}

		getAndCreateProject();
		final Bundle coreTestBundle = Platform.getBundle(VEXCoreTestPlugin.PLUGIN_ID);
		final Enumeration<String> projectFilePaths = coreTestBundle.getEntryPaths("/" + PROJECT_FILES_FOLDER_NAME);
		while (projectFilePaths.hasMoreElements()) {
			final String absolutePath = projectFilePaths.nextElement();
			final URL url = coreTestBundle.getEntry(absolutePath);

			if (isFileUrl(url)) {
				final URL resolvedUrl = FileLocator.resolve(url);
				final String relativePath = absolutePath.substring(PROJECT_FILES_FOLDER_NAME.length());
				final IFile destFile = fTestProject.getFile(relativePath);
				System.out.println(destFile.getLocation() + " --> " + resolvedUrl.toExternalForm());
				if (isFromJarFile(resolvedUrl)) {
					copyTestFileToProject(coreTestBundle, absolutePath, destFile);
				} else {
					//if resource is not compressed, link
					destFile.createLink(resolvedUrl.toURI(), IResource.REPLACE, new NullProgressMonitor());
				}
			}
		}
		fTestProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		fTestProjectInitialized = true;
	}

	private static boolean isFileUrl(final URL url) {
		return !url.getFile().endsWith("/");
	}

	private boolean isFromJarFile(final URL resolvedUrl) {
		return resolvedUrl.toExternalForm().startsWith("jar:file");
	}

	private void copyTestFileToProject(final Bundle coreTestBundle, final String sourcePath, final IFile destinationFile) throws IOException, CoreException {
		final InputStream source = FileLocator.openStream(coreTestBundle, new Path(sourcePath), false);
		if (destinationFile.exists()) {
			destinationFile.delete(true, new NullProgressMonitor());
		}
		destinationFile.create(source, true, new NullProgressMonitor());
	}

	private IFile getFileInProject(final String path) {
		return fTestProject.getFile(new Path(path));
	}

	private static void getAndCreateProject() throws CoreException {
		final IWorkspace workspace = getWorkspace();
		final IWorkspaceRoot root = workspace.getRoot();
		fTestProject = root.getProject(TEST_PROJECT_NAME);

		createProject(fTestProject, null, null);
		fTestProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue(fTestProject.exists());
	}

	private static void createProject(final IProject project, IPath locationPath, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("creating test project", 10);
		// create the project
		try {
			if (!project.exists()) {
				final IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
				if (Platform.getLocation().equals(locationPath)) {
					locationPath = null;
				}
				desc.setLocation(locationPath);
				project.create(desc, monitor);
				monitor = null;
			}
			if (!project.isOpen()) {
				project.open(monitor);
				monitor = null;
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Returns the workspace instance.
	 */
	private static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Test the normalize method. Test cases are as follows.
	 * 
	 * <ul>
	 * <li>leading w/s trimmed</li>
	 * <li>trailing w/s trimmed</li>
	 * <li>internal w/s collapsed to a single space</li>
	 * <li>internal w/s before and after an inline child element collapsed to a single space.</li>
	 * <li>internal w/s before and after a block child element removed.</li>
	 * <li>spaces between blocks eliminated.</li>
	 * <li>no extraneous spaces before or after elements added</li>
	 * </ul>
	 */
	@Test
	public void testNormalize() throws Exception {

		final String input = "<doc>\n\t  " + "<block>\n\t foo\n\t <inline>foo\n\t bar</inline>\n\t baz\n\t </block>\n\t " + "<block>\n\t foo\n\t <block>bar</block>\n\t baz</block>"
				+ "<block>\n\t foo<inline> foo bar </inline>baz \n\t </block>" + "<block>\n\t foo<block>bar</block>baz \n\t</block>" + "\n\t </doc>";

		final StyleSheet ss = getStyleSheet();

		final IDocument doc = createDocument(input, ss);
		IElement element;

		element = doc.getRootElement();
		assertContent(element, "<block>", "<block>", "<block>", "<block>");

		final List<? extends IElement> children = element.childElements().asList();

		// --- Block 0 ---

		assertContent(children.get(0), "foo ", "<inline>", " baz");
		List<? extends IElement> c2 = children.get(0).childElements().asList();
		assertContent(c2.get(0), "foo bar");

		// --- Block 1 ---

		assertContent(children.get(1), "foo", "<block>", "baz");
		c2 = children.get(1).childElements().asList();
		assertContent(c2.get(0), "bar");

		// --- Block 2 ---

		assertContent(children.get(2), "foo", "<inline>", "baz");
		c2 = children.get(2).childElements().asList();
		assertContent(c2.get(0), "foo bar");

		// --- Block 3 ---

		assertContent(children.get(3), "foo", "<block>", "baz");
		c2 = children.get(3).childElements().asList();
		assertContent(c2.get(0), "bar");

	}

	@Test
	public void testPreNormalize1() throws ParserConfigurationException, SAXException, IOException {
		// ========= Now test with a PRE element =========

		final String input = "<doc>\n " + "<pre>\n foo\n</pre>\n " + "\n </doc>";

		final IDocument doc = createDocument(input, getStyleSheet());

		final IElement element = doc.getRootElement();
		assertContent(element, "<pre>");

		final IElement pre = element.childElements().first();
		assertContent(pre, "\n foo\n");
	}

	@Test
	public void testPreNormalize2() throws Exception {
		// ========= Now test with a PRE element =========

		final String input = "<doc>\n " + "<pre>\n foo\n <inline>\n foo\n bar\n </inline></pre>\n " + "\n </doc>";

		final IDocument doc = createDocument(input, getStyleSheet());

		final IElement element = doc.getRootElement();
		final IElement pre = element.childElements().first();
		final IElement inline = pre.childElements().first();
		assertContent(inline, "\n foo\n bar\n ");
	}

	@Test
	public void testPreElementNormalize() throws ParserConfigurationException, SAXException, IOException {
		// ========= Now test with a PRE element =========

		final String input = "<doc>\n  " + "<pre>\n\t foo\n\t <inline>\n\t foo\n\t bar\n\t </inline>\n\t baz\n\t </pre>\n " + "\n </doc>";

		final IDocument doc = createDocument(input, getStyleSheet());

		final IElement element = doc.getRootElement();
		assertContent(element, "<pre>");

		final IElement pre = element.childElements().first();
		assertContent(pre, "\n\t foo\n\t ", "<inline>", "\n\t baz\n\t ");

		final IElement inline = pre.childElements().first();
		assertContent(inline, "\n\t foo\n\t bar\n\t ");
	}

	private StyleSheet getStyleSheet() throws IOException {
		final StyleSheetReader reader = new StyleSheetReader();
		final URL url = getFileInProject("test.css").getLocationURI().toURL();
		final StyleSheet ss = reader.read(url);
		return ss;
	}

	// ========================================================= PRIVATE

	// private static final String DTD = "<!ELEMENT doc ANY>";

	/**
	 * Asserts the content of the given element matches the given list. If a string in content is enclosed in angle
	 * brackets, it's assume to refer to the name of an element; otherwise, it represents text content.
	 */
	private void assertContent(final IElement element, final String... strings) {
		final Iterator<INode> children = element.children().iterator();
		for (final String string : strings) {
			final INode node = children.next();
			if (string.startsWith("<")) {
				final String name = string.substring(1, string.length() - 1);
				assertTrue(node instanceof IElement);
				assertEquals(name, ((IElement) node).getPrefixedName());
			} else {
				assertTrue(node instanceof IText);
				final String contentText = node.getText();
				assertEquals(string, contentText);
			}
		}
		assertFalse("more strings expected", children.hasNext());
	}

	private IDocument createDocument(final String documentContent, final StyleSheet styleSheet) throws ParserConfigurationException, SAXException, IOException {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final XMLReader xmlReader = factory.newSAXParser().getXMLReader();
		final DocumentBuilder builder = new DocumentBuilder(null, new DummyValidator(), new IStyleSheetProvider() {
			public StyleSheet getStyleSheet(final DocumentContentModel documentContentModel) {
				return styleSheet;
			}
		}, CssWhitespacePolicy.FACTORY);

		final InputSource is = new InputSource(new ByteArrayInputStream(documentContent.getBytes()));
		xmlReader.setContentHandler(builder);
		xmlReader.parse(is);
		return builder.getDocument();
	}

}

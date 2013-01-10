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
package org.eclipse.vex.ui.internal.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.vex.core.internal.io.DocumentWriter;
import org.eclipse.vex.ui.internal.VexPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Represents a Vex plugin project in the workspace.
 */
public class PluginProject extends ConfigSource {

	public static final String PLUGIN_XML = "vex-plugin.xml"; //$NON-NLS-1$

	private final IProject project;

	public PluginProject(final IProject project) {
		super(project.getName());
		this.project = project;
	}

	@Override
	public URL getBaseUrl() {
		try {
			return getProject().getLocation().toFile().toURL();
		} catch (final MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * @return the IProject associated with this plugin project.
	 */
	public IProject getProject() {
		return project;
	}

	public void build() {
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (final CoreException e) {
			final String message = MessageFormat.format(Messages.getString("PluginProject.buildError"), //$NON-NLS-1$
					new Object[] { project.getName() });
			VexPlugin.getDefault().log(IStatus.ERROR, message, e);
		}
	}

	public void load() throws CoreException {
		checkProject();
		parseConfigXml(null);
		parseResources(null);
	}

	private void checkProject() {
		if (!isOpenPluginProject(project)) {
			throw new IllegalStateException(MessageFormat.format(Messages.getString("PluginProject.notPluginProject"), //$NON-NLS-1$
					project.getName()));
		}
	}

	public static boolean isOpenPluginProject(final IProject project) {
		return project.isOpen() && hasPluginProjectNature(project);
	}

	public static boolean hasPluginProjectNature(final IProject project) {
		try {
			return project.isOpen() && project.hasNature(PluginProjectNature.ID);
		} catch (final CoreException e) {
			VexPlugin.getDefault().getLog().log(e.getStatus());
			return false;
		}
	}

	/**
	 * Re-parses the vex-plugin.xml file.
	 */
	public void parseConfigXml(final IBuildProblemHandler problemHandler) throws CoreException {
		removeAllItems();

		final Document document = parseConfigXmlDom(problemHandler);
		if (document == null) {
			return;
		}

		final NodeList extensions = document.getElementsByTagName("extension"); //$NON-NLS-1$
		for (int i = 0; i < extensions.getLength(); i++) {
			final Element extensionElement = (Element) extensions.item(i);
			final String extensionPointId = extensionElement.getAttribute("point"); //$NON-NLS-1$
			final String id = extensionElement.getAttribute("id"); //$NON-NLS-1$
			final String name = extensionElement.getAttribute("name"); //$NON-NLS-1$
			final IConfigElement[] configElements = parseConfigElements(extensionElement);

			try {
				this.addItem(extensionPointId, id, name, configElements);
			} catch (final IOException e) {
				VexPlugin.getDefault().log(IStatus.ERROR, e.getMessage(), e);
			}
		}
	}

	private Document parseConfigXmlDom(final IBuildProblemHandler problemHandler) throws CoreException {
		final IFile configXml = project.getFile(PLUGIN_XML);
		final DocumentBuilder builder = createDocumentBuilder();
		try {
			final URL url = configXml.getLocation().toFile().toURL();
			return builder.parse(url.toString());
		} catch (final SAXParseException e) {
			if (problemHandler != null) {
				final BuildProblem problem = new BuildProblem(BuildProblem.SEVERITY_ERROR, configXml.getName(), e.getMessage(), e.getLineNumber());
				problemHandler.foundProblem(problem);
				return null;
			} else {
				VexPlugin.getDefault().log(IStatus.ERROR, MessageFormat.format("Cannot load {0}.", configXml.getFullPath()), e);
				return null;
			}
		} catch (final SAXException e) {
			VexPlugin.getDefault().log(IStatus.ERROR, MessageFormat.format("Cannot load {0}.", configXml.getFullPath()), e);
			return null;
		} catch (final IOException e) {
			VexPlugin.getDefault().log(IStatus.ERROR, MessageFormat.format("Cannot load {0}.", configXml.getFullPath()), e);
			return null;
		}
	}

	private IConfigElement[] parseConfigElements(final Element extensionElement) {
		final List<IConfigElement> result = new ArrayList<IConfigElement>();
		final NodeList childList = extensionElement.getChildNodes();
		for (int j = 0; j < childList.getLength(); j++) {
			final Node child = childList.item(j);
			if (child instanceof Element) {
				result.add(new DomConfigurationElement((Element) child));
			}
		}
		return result.toArray(new IConfigElement[result.size()]);
	}

	private static DocumentBuilder createDocumentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new AssertionError(e);
		} catch (final FactoryConfigurationError e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Writes this configuraton to the file vex-config.xml in the project.
	 */
	public void writeConfigXml() throws CoreException, IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(baos, "utf-8")); //$NON-NLS-1$

		final ConfigurationElement root = new ConfigurationElement("plugin"); //$NON-NLS-1$
		for (final ConfigItem item : getAllItems()) {
			final ConfigurationElement extElement = new ConfigurationElement("extension"); //$NON-NLS-1$
			extElement.setAttribute("id", item.getSimpleId()); //$NON-NLS-1$
			extElement.setAttribute("name", item.getName()); //$NON-NLS-1$
			extElement.setAttribute("point", item.getExtensionPointId()); //$NON-NLS-1$
			final IConfigItemFactory factory = getConfigItemFactory(item.getExtensionPointId());
			if (factory != null) {
				extElement.setChildren(factory.createConfigurationElements(item));
				root.addChild(extElement);
			}
		}
		writeElement(root, out, 0);

		out.close();

		final InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

		final IFile file = getProject().getFile(PLUGIN_XML);
		if (file.exists()) {
			file.setContents(inputStream, true, false, null);
		} else {
			file.create(inputStream, true, null);
		}
	}

	private static void writeElement(final IConfigElement element, final PrintWriter out, final int level) {
		final StringBuffer elementIndent = new StringBuffer();
		for (int i = 0; i < level; i++) {
			elementIndent.append("  "); //$NON-NLS-1$
		}
		final StringBuffer elementPrefix = new StringBuffer();
		elementPrefix.append("<"); //$NON-NLS-1$
		elementPrefix.append(element.getName());

		final StringBuffer attributeIndent = new StringBuffer(elementIndent.toString());
		for (int i = 0; i < elementPrefix.length() + 1; i++) {
			attributeIndent.append(" "); //$NON-NLS-1$
		}

		out.print(elementIndent.toString());
		out.print(elementPrefix.toString());
		final String[] attributeNames = element.getAttributeNames();
		for (int i = 0; i < attributeNames.length; i++) {
			final String attributeName = attributeNames[i];
			if (i > 0) {
				out.println();
				out.print(attributeIndent);
			} else {
				out.print(" "); //$NON-NLS-1$
			}

			out.print(attributeName);
			out.print("=\""); //$NON-NLS-1$
			out.print(DocumentWriter.escape(element.getAttribute(attributeName)));
			out.print("\""); //$NON-NLS-1$
		}
		out.println(">"); //$NON-NLS-1$

		final IConfigElement[] children = element.getChildren();
		for (final IConfigElement element2 : children) {
			writeElement(element2, out, level + 1);
		}

		out.print(elementIndent);
		out.print("</"); //$NON-NLS-1$
		out.print(element.getName());
		out.println(">"); //$NON-NLS-1$
	}

}

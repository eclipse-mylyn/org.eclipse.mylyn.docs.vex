package org.eclipse.jst.jsf.facelet.core.internal.registry.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.ComponentTagDefn;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.ConverterTagDefn;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletLibraryClassTagLib;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglibDefn;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglibFactory;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletXMLDefnTaglib;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.HandlerTagDefn;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.TagDefn;
import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.ValidatorTagDefn;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Facelet tag library document parser (dtd 1.0).
 * 
 * @author cbateman
 * 
 */
public class TagModelParser
{
    private static final String ELEMENT_NAME_VALIDATOR_ID         = "validator-id";                                         //$NON-NLS-1$
    private static final String ELEMENT_NAME_CONVERTER_ID         = "converter-id";                                         //$NON-NLS-1$
    private static final String ELEMENT_NAME_RENDERER_TYPE        = "renderer-type";                                        //$NON-NLS-1$
    private static final String ELEMENT_NAME_COMPONENT_TYPE       = "component-type";                                       //$NON-NLS-1$
    private static final String ELEMENT_NAME_VALIDATOR            = "validator";                                            //$NON-NLS-1$
    private static final String ELEMENT_NAME_CONVERTER            = "converter";                                            //$NON-NLS-1$
    private static final String ELEMENT_NAME_COMPONENT            = "component";                                            //$NON-NLS-1$
    private static final String ELEMENT_NAME_HANDLER_CLASS        = "handler-class";                                        //$NON-NLS-1$
    private static final String ELEMENT_NAME_TAG_NAME             = "tag-name";                                             //$NON-NLS-1$
    private static final String ELEMENT_NAME_TAG                  = "tag";                                                  //$NON-NLS-1$
    private static final String ELEMENT_NAME_NAMESPACE            = "namespace";                                            //$NON-NLS-1$
    private static final String ELEMENT_NAME_LIBRARY_CLASS        = "library-class";                                        //$NON-NLS-1$
    private static final String ELEMENT_NAME_FACELET_TAGLIB       = "facelet-taglib";                                       //$NON-NLS-1$
    private static final String URI_FACELET_TAGLIB_1_0_DTD        = "facelet-taglib_1_0.dtd";       //$NON-NLS-1$
    private static final String PUBLIC_DTD_FACELET_TAGLIB_1_0_DTD = "-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN"; //$NON-NLS-1$

    /**
     * @param taglibFile
     * @param defaultDTDSource
     * @return the default taglib dom Document
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document getDefaultTaglibDocument(
            final InputSource taglibFile, final InputSource defaultDTDSource)
            throws IOException, ParserConfigurationException, SAXException
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);

        final DocumentBuilder builder = factory.newDocumentBuilder();

        final DefaultHandler handler = new DefaultHandler()
        {
            @Override
            public InputSource resolveEntity(final String publicId,
                    final String systemId) throws IOException, SAXException
            {
                if (PUBLIC_DTD_FACELET_TAGLIB_1_0_DTD.equals(publicId)
                        || (systemId != null
                            && systemId.endsWith(URI_FACELET_TAGLIB_1_0_DTD)))

                {
                    return defaultDTDSource;
                }
                return super.resolveEntity(publicId, systemId);
            }
        };

        builder.setEntityResolver(handler);
        return builder.parse(taglibFile);
    }

    /**
     * @param doc
     * @return the facelet tag library or null
     */
    public static FaceletTaglibDefn processDocument(final Document doc)
    {
        Node curNode = null;

        for (int i = 0; i < doc.getChildNodes().getLength(); i++)
        {
            curNode = doc.getChildNodes().item(i);

            if (curNode.getNodeType() == Node.ELEMENT_NODE
                    && ELEMENT_NAME_FACELET_TAGLIB
                            .equals(curNode.getNodeName()))
            {
                break;
            }
        }

        if (curNode == null)
        {
            throw new IllegalArgumentException(
                    "Couldn't find facelet-taglib node"); //$NON-NLS-1$
        }

        for (int i = 0; i < curNode.getChildNodes().getLength(); i++)
        {
            final Node node = curNode.getChildNodes().item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (ELEMENT_NAME_LIBRARY_CLASS.equals(node.getNodeName()))
                {
                    final FaceletLibraryClassTagLib faceletLibraryClassTagLib = FaceletTaglibFactory.eINSTANCE
                            .createFaceletLibraryClassTagLib();
                    faceletLibraryClassTagLib.setLibraryClass(node
                            .getTextContent().trim());
                    return faceletLibraryClassTagLib;
                }
                return processFaceletTaglibWithTags(curNode);
            }
        }

        return null;
    }

    private static FaceletXMLDefnTaglib processFaceletTaglibWithTags(
            final Node node)
    {
        String namespace = null;
        final List<Node> tagNodes = new ArrayList<Node>();

        for (int i = 0; i < node.getChildNodes().getLength(); i++)
        {
            final Node childNode = node.getChildNodes().item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                if (ELEMENT_NAME_NAMESPACE.equals(childNode.getNodeName()))
                {
                    namespace = childNode.getTextContent();
                }
                else if (ELEMENT_NAME_TAG.equals(childNode.getNodeName()))
                {
                    tagNodes.add(childNode);
                }
            }
        }

        if (namespace != null)
        {
            final FaceletXMLDefnTaglib xmlDefnTaglib = FaceletTaglibFactory.eINSTANCE
                    .createFaceletXMLDefnTaglib();
            xmlDefnTaglib.setNamespace(namespace);

            for (final Node tagNode : tagNodes)
            {
                final TagDefn tag = createTagFromNode(namespace, tagNode);

                if (tag != null)
                {
                    xmlDefnTaglib.getTags().add(tag);
                }
            }

            return xmlDefnTaglib;
        }
        return null;
    }

    private static TagDefn createTagFromNode(final String namespace,
            final Node tagNode)
    {
        final Map<String, Node> children = indexChildren(tagNode);

        Node node = children.get(ELEMENT_NAME_TAG_NAME);

        if (node != null)
        {
            final String name = safeGetTextContext(node);

            if (name == null)
            {
                return null;
            }

            node = children.get(ELEMENT_NAME_HANDLER_CLASS);

            if (node != null)
            {
                final HandlerTagDefn handlerTag = FaceletTaglibFactory.eINSTANCE
                        .createHandlerTagDefn();
                handlerTag.setHandlerClass(safeGetTextContext(node));
                handlerTag.setName(name);
                return handlerTag;
            }

            node = children.get(ELEMENT_NAME_COMPONENT);

            if (node != null)
            {
                return createTagWithComponentType(namespace, name, node);
            }

            node = children.get(ELEMENT_NAME_CONVERTER);

            if (node != null)
            {
                return createTagWithConverter(namespace, name, node);
            }

            node = children.get(ELEMENT_NAME_VALIDATOR);

            if (node != null)
            {
                return createValidatorTag(namespace, name, node);
            }
        }

        return null;
    }

    private static ComponentTagDefn createTagWithComponentType(
            final String uri, final String tagName, final Node paramNode)
    {
        final Map<String, Node> componentChildren = indexChildren(paramNode);
        Node node = componentChildren.get(ELEMENT_NAME_COMPONENT_TYPE);

        if (node != null)
        {
            final String componentType = safeGetTextContext(node);
            String rendererType = null;
            String handlerClass = null;

            node = componentChildren.get(ELEMENT_NAME_RENDERER_TYPE);
            if (node != null)
            {
                rendererType = safeGetTextContext(node);
            }

            node = componentChildren.get(ELEMENT_NAME_HANDLER_CLASS);
            if (node != null)
            {
                handlerClass = safeGetTextContext(node);
            }

            final ComponentTagDefn componentTag = FaceletTaglibFactory.eINSTANCE
                    .createComponentTagDefn();
            componentTag.setName(tagName);
            componentTag.setComponentType(componentType);
            componentTag.setHandlerClass(handlerClass);
            componentTag.setRendererType(rendererType);
            return componentTag;
        }
        return null;
    }

    private static ConverterTagDefn createTagWithConverter(final String uri,
            final String tagName, final Node paramNode)
    {
        final Map<String, Node> converterChildren = indexChildren(paramNode);
        Node node = converterChildren.get(ELEMENT_NAME_CONVERTER_ID);

        if (node != null)
        {
            final String converterId = safeGetTextContext(node);
            String handlerClass = null;

            node = converterChildren.get(ELEMENT_NAME_HANDLER_CLASS);
            if (node != null)
            {
                handlerClass = safeGetTextContext(node);
            }

            final ConverterTagDefn converterTag = FaceletTaglibFactory.eINSTANCE
                    .createConverterTagDefn();
            converterTag.setName(tagName);
            converterTag.setConverterId(converterId);
            converterTag.setHandlerClass(handlerClass);
            return converterTag;
        }
        return null;
    }

    private static ValidatorTagDefn createValidatorTag(final String uri,
            final String tagName, final Node paramNode)
    {
        final Map<String, Node> converterChildren = indexChildren(paramNode);
        Node node = converterChildren.get(ELEMENT_NAME_VALIDATOR_ID);

        if (node != null)
        {
            final String validatorId = safeGetTextContext(node);
            String handlerClass = null;

            node = converterChildren.get(ELEMENT_NAME_HANDLER_CLASS);
            if (node != null)
            {
                handlerClass = safeGetTextContext(node);
            }

            final ValidatorTagDefn validatorTag = FaceletTaglibFactory.eINSTANCE
                    .createValidatorTagDefn();
            validatorTag.setName(tagName);
            validatorTag.setHandlerClass(handlerClass);
            validatorTag.setValidatorId(validatorId);
            return validatorTag;
        }
        return null;
    }

    private static Map<String, Node> indexChildren(final Node node)
    {
        final Map<String, Node> children = new HashMap<String, Node>();
        final NodeList nodeList = node.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++)
        {
            final Node childNode = nodeList.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                children.put(childNode.getNodeName(), childNode);
            }
        }

        return children;
    }

    private static String safeGetTextContext(final Node node)
    {
        String textContent = node.getTextContent();
        if (textContent == null)
        {
            return null;
        }

        textContent = textContent.trim();

        if ("".equals(textContent)) //$NON-NLS-1$
        {
            return null;
        }

        return textContent;
    }
}

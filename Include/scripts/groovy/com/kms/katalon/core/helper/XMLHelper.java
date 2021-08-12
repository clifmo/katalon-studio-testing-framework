package com.kms.katalon.core.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.kms.katalon.core.util.ArrayUtil;

public class XMLHelper {
    public interface NodeFinder {
        public boolean test(Node node);
    }

    public static Document readXML(String rawInput) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            return null;
        }

        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(rawInput);
        ByteArrayInputStream input;
        try {
            input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            return null;
        }

        Document doc;
        try {
            doc = builder.parse(input);
        } catch (SAXException | IOException e) {
            return null;
        }

        return doc;
    }

    public static String docToString(Document doc) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e1) {
            return null;
        }

        StringWriter writer = new StringWriter();
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            return null;
        }

        String xmlString = writer.getBuffer().toString();
        return xmlString;
    }

    public static Node findNodeByAttributes(Document doc, String... attributes) {
        Map<String, String> attrs = ArrayUtil.toMap(attributes);
        return findNodeByAttributes(getNodeList(doc), attrs);
    }

    public static Node findNodeByAttributes(Node root, String... attributes) {
        Map<String, String> attrs = ArrayUtil.toMap(attributes);
        return findNodeByAttributes(root, attrs);
    }

    public static Node findNodeByAttributes(Node root, Map<String, String> attributes) {
        return findNodeByAttributes(getNodeList(root), attributes);
    }

    public static Node findNodeByAttributes(NodeList nodes, Map<String, String> attributes) {
        List<Node> foundNodes = findNodesByAttributes(nodes, attributes);
        return ArrayUtil.getFirst(foundNodes);
    }

    public static List<Node> findNodesByAttributes(NodeList nodes, String... attributes) {
        Map<String, String> attrs = ArrayUtil.toMap(attributes);
        return findNodesByAttributes(nodes, attrs);
    }

    public static List<Node> findNodesByAttributes(NodeList nodes, Map<String, String> attributes) {
        return findNodes(nodes, (node) -> {
            return attributes.entrySet().stream().allMatch(entry -> {
                Node attrNode = node.getAttributes().getNamedItem(entry.getKey());
                if (attrNode == null) {
                    return false;
                }
                return StringUtils.equals(attrNode.getNodeValue(), entry.getValue());
            });
        });
    }

    public static Node findNodeByTagName(Document doc, String tagName) {
        return findNodeByTagName(getNodeList(doc), tagName);
    }

    public static Node findNodeByTagName(Node root, String tagName) {
        return findNodeByTagName(getNodeList(root), tagName);
    }

    public static Node findNodeByTagName(NodeList nodes, String tagName) {
        return findNode(nodes, (node) -> {
            return StringUtils.equals(node.getNodeName(), tagName);
        });
    }

    public static List<Node> findNodesByTagName(Node root, String tagName) {
        return findNodesByTagName(getNodeList(root), tagName);
    }

    public static List<Node> findNodesByTagName(NodeList nodes, String tagName) {
        return findNodes(nodes, (node) -> {
            return StringUtils.equals(node.getNodeName(), tagName);
        });
    }

    public static Node findNode(Document doc, NodeFinder filter) {
        return findNode(getNodeList(doc), filter);
    }

    public static Node findNode(Node root, NodeFinder filter) {
        return findNode(getNodeList(root), filter);
    }

    public static Node findNode(NodeList nodes, NodeFinder filter) {
        List<Node> foundNodes = findNodes(nodes, filter, true);
        return ArrayUtil.getFirst(foundNodes);
    }

    public static List<Node> findNodes(Document doc, NodeFinder filter) {
        return findNodes(getNodeList(doc), filter);
    }

    public static List<Node> findNodes(Node root, NodeFinder filter) {
        return findNodes(getNodeList(root), filter);
    }

    public static List<Node> findNodes(NodeList nodes, NodeFinder filter) {
        return findNodes(nodes, filter, false);
    }

    public static List<Node> findNodes(NodeList nodes, NodeFinder filter, boolean returnFirstMatch) {
        if (nodes == null) {
            return null;
        }

        List<Node> foundNodes = new ArrayList<Node>();
        int numChildren = nodes.getLength();
        for (int i = 0; i < numChildren; i++) {
            Node nodeI = nodes.item(i);
            if (nodeI.getAttributes() == null) { // Skip Text node
                continue;
            }
            boolean isMatched = filter == null || filter.test(nodeI);
            if (isMatched) {
                foundNodes.add(nodeI);
                if (returnFirstMatch) {
                    break;
                }
            }
        }
        return foundNodes;
    }

    public static Element createElement(Document doc, String tagName, String... attributes) {
        Map<String, String> attrs = ArrayUtil.toMap(attributes);
        return createElement(doc, tagName, attrs);
    }

    public static Element createElement(Document doc, String tagName, Map<String, String> attributes) {
        Element element = doc.createElement(tagName);
        attributes.entrySet().stream().forEachOrdered(attribute -> {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        });
        return element;
    }

    public static NodeList getNodeList(Document doc) {
        Node root = doc != null ? doc.getDocumentElement() : null;
        return getNodeList(root);
    }

    public static NodeList getNodeList(Node root) {
        return root != null ? root.getChildNodes() : null;
    }

    public static Map<String, String> getNodeAttributes(Node node) {
        Map<String, String> attrs = new HashMap<String, String>();
        NamedNodeMap nodeAttrs = node.getAttributes();
        if (nodeAttrs != null && nodeAttrs.getLength() > 0) {
            for (int i = 0; i < nodeAttrs.getLength(); i++) {
                Node attrI = nodeAttrs.item(i);
                attrs.put(attrI.getNodeName(), attrI.getNodeValue());
            }
        }
        return attrs;
    }

    public static String getNodeAttribute(Node node, String attributeName) {
        if (node == null || StringUtils.isBlank(attributeName)) {
            return null;
        }

        NamedNodeMap nodeAttrs = node.getAttributes();
        if (nodeAttrs != null && nodeAttrs.getLength() > 0) {
            for (int i = 0; i < nodeAttrs.getLength(); i++) {
                Node attrI = nodeAttrs.item(i);
                if (StringUtils.equals(attrI.getNodeName(), attributeName)) {
                    return attrI.getNodeValue();
                }
            }
        }
        return null;
    }
}

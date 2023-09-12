package com.svgtopng;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalTest {
    @Test
    public void test() throws Exception {
        String svgFile = "D:/device.svg";

        // 创建SVG DOM解析器工厂
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

        // 解析SVG文件
        Document document = factory.createDocument(new File(svgFile).toURI().toString());

        // 获取所有矩形元素
//        NodeList rectElements = document.getElementsByTagName("rect");
//        // 遍历矩形元素并获取属性
//        for (int i = 0; i < rectElements.getLength(); i++) {
//            Element rect = (Element) rectElements.item(i);
//            String x = rect.getAttribute("x");
//            String y = rect.getAttribute("y");
//            String width = rect.getAttribute("width");
//            String height = rect.getAttribute("height");
//            String fill = rect.getAttribute("fill");
//            System.out.println("矩形属性： x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", fill=" + fill);
//        }
        Map<String, List<String>> resultMap = new HashMap<>();
        resultMap.put("dp_tag_label", new ArrayList<>());
        resultMap.put("index_tag_label", new ArrayList<>());
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        traversalNodes(nodeList, resultMap);
        List<String> first = resultMap.get("dp_tag_label");
        List<String> second = resultMap.get("index_tag_label");
        System.out.println("first = " + first);
        System.out.println("second = " + second);
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            Node node = nodeList.item(i);
//            if (node.getNodeType() == Node.ELEMENT_NODE) {
//                Element element = (Element) node;
//                System.out.println("Element: " + element.getNodeName());
//                System.out.println("Attributes:");
//                NamedNodeMap attributeList = element.getAttributes();
//                for (int j = 0; j < attributeList.getLength(); j++) {
//                    System.out.println("\t" + attributeList.item(j).getNodeName() + ": " + attributeList.item(j).getNodeValue());
//                }
//                System.out.println();
//            }
//        }
    }
    public void traversalNodes(NodeList nodeList, Map<String, List<String>> map) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NamedNodeMap attributeList = element.getAttributes();
                for (int j = 0; j < attributeList.getLength(); j++) {
                    if (attributeList.item(j).getNodeName().equals("dp_tag_label")) {
                        List<String> list1 = map.get("dp_tag_label");
                        list1.add(attributeList.item(j).getNodeValue());
                    }
                    if (attributeList.item(j).getNodeName().equals("index_tag_label")) {
                        List<String> list2 = map.get("index_tag_label");
                        list2.add(attributeList.item(j).getNodeValue());
                    }
                }
                NodeList childrenList = node.getChildNodes();
                if (childrenList.getLength() > 0) {
                    traversalNodes(childrenList, map);
                }
            }
        }
    }
}

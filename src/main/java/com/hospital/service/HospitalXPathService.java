package com.hospital.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates XPath query execution on hospital-config.xml.
 */
public class HospitalXPathService {

    private Document getDocument() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("hospital-config.xml");
        if (is == null) {
            throw new IllegalStateException("hospital-config.xml not found");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    /**
     * Executes arbitrary XPath expression returning list of map items.
     */
    public List<Map<String, String>> queryDepartments(String expression) {
        List<Map<String, String>> results = new ArrayList<>();
        try {
            Document doc = getDocument();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i) instanceof Element) {
                    Element elem = (Element) nodeList.item(i);
                    Map<String, String> map = new HashMap<>();
                    map.put("id", elem.getAttribute("id"));

                    NodeList names = elem.getElementsByTagName("name");
                    if (names.getLength() > 0) map.put("name", names.item(0).getTextContent().trim());

                    NodeList rooms = elem.getElementsByTagName("room");
                    if (rooms.getLength() > 0) map.put("room", rooms.item(0).getTextContent().trim());

                    NodeList times = elem.getElementsByTagName("averageTime");
                    if (times.getLength() > 0) map.put("averageTime", times.item(0).getTextContent().trim());

                    results.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Map<String, String>> findDepartmentByName(String deptName) {
        return queryDepartments("//department[name='" + deptName + "']");
    }

    public List<Map<String, String>> findDepartmentById(String id) {
        return queryDepartments("//department[@id='" + id + "']");
    }

    public List<Map<String, String>> findDepartmentsWithAverageTimeGreaterThan(int minutes) {
        return queryDepartments("//department[averageTime > " + minutes + "]");
    }

    public String evaluateSingleString(String expression) {
        try {
            Document doc = getDocument();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            return (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

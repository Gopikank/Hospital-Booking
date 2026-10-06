package com.hospital.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates XML DOM Parser using DocumentBuilderFactory, DocumentBuilder, Document.
 * Reads config/hospital-config.xml and extracts hospital configuration.
 */
public class HospitalConfigParser {

    private String hospitalName;
    private int defaultAverageConsultationTime;
    private int maximumOnlineBookings;
    private boolean checkInRequired;
    private List<Map<String, String>> departments = new ArrayList<>();

    public HospitalConfigParser() {
        parseConfig();
    }

    public void parseConfig() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("hospital-config.xml");
            if (is == null) {
                System.err.println("hospital-config.xml not found in classpath.");
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disabling validation here so parse can run standalone without network entity lookup
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            // 1. Hospital name
            NodeList nameNodes = doc.getElementsByTagName("name");
            if (nameNodes.getLength() > 0) {
                this.hospitalName = nameNodes.item(0).getTextContent().trim();
            }

            // 2. Queue Settings
            NodeList avgNodes = doc.getElementsByTagName("defaultAverageConsultationTime");
            if (avgNodes.getLength() > 0) {
                this.defaultAverageConsultationTime = Integer.parseInt(avgNodes.item(0).getTextContent().trim());
            }

            NodeList maxBookings = doc.getElementsByTagName("maximumOnlineBookings");
            if (maxBookings.getLength() > 0) {
                this.maximumOnlineBookings = Integer.parseInt(maxBookings.item(0).getTextContent().trim());
            }

            NodeList checkInNodes = doc.getElementsByTagName("checkInRequired");
            if (checkInNodes.getLength() > 0) {
                this.checkInRequired = Boolean.parseBoolean(checkInNodes.item(0).getTextContent().trim());
            }

            // 3. Departments
            NodeList deptNodes = doc.getElementsByTagName("department");
            departments.clear();
            for (int i = 0; i < deptNodes.getLength(); i++) {
                Element deptElem = (Element) deptNodes.item(i);
                Map<String, String> dMap = new HashMap<>();
                dMap.put("id", deptElem.getAttribute("id"));

                NodeList dNames = deptElem.getElementsByTagName("name");
                if (dNames.getLength() > 0) dMap.put("name", dNames.item(0).getTextContent().trim());

                NodeList dRooms = deptElem.getElementsByTagName("room");
                if (dRooms.getLength() > 0) dMap.put("room", dRooms.item(0).getTextContent().trim());

                NodeList dTimes = deptElem.getElementsByTagName("averageTime");
                if (dTimes.getLength() > 0) dMap.put("averageTime", dTimes.item(0).getTextContent().trim());

                departments.add(dMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getHospitalName() { return hospitalName; }
    public int getDefaultAverageConsultationTime() { return defaultAverageConsultationTime; }
    public int getMaximumOnlineBookings() { return maximumOnlineBookings; }
    public boolean isCheckInRequired() { return checkInRequired; }
    public List<Map<String, String>> getDepartments() { return departments; }
}

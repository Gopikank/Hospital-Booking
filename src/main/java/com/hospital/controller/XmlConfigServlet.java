package com.hospital.controller;

import com.hospital.service.HospitalConfigParser;
import com.hospital.service.HospitalXPathService;
import com.hospital.service.XMLValidator;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/xml-config-action")
public class XmlConfigServlet extends HttpServlet {

    private final HospitalConfigParser parser = new HospitalConfigParser();
    private final HospitalXPathService xpathService = new HospitalXPathService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "info";

        switch (action.toLowerCase()) {
            case "validate-xsd" -> {
                Map<String, Object> xsdResult = XMLValidator.validateAgainstXSD();
                JsonUtil.sendSuccess(resp, "XSD validation executed.", xsdResult);
            }
            case "validate-dtd" -> {
                Map<String, Object> dtdResult = XMLValidator.validateAgainstDTD();
                JsonUtil.sendSuccess(resp, "DTD validation executed.", dtdResult);
            }
            case "xpath" -> {
                String query = req.getParameter("query");
                if (query == null || query.trim().isEmpty()) {
                    query = "//department[averageTime > 5]";
                }
                List<Map<String, String>> nodes = xpathService.queryDepartments(query);
                Map<String, Object> xData = new HashMap<>();
                xData.put("query", query);
                xData.put("results", nodes);
                xData.put("count", nodes.size());
                JsonUtil.sendSuccess(resp, "XPath query executed successfully.", xData);
            }
            default -> {
                // Return parsed hospital configuration via XML DOM Parser
                parser.parseConfig();
                Map<String, Object> parsedInfo = new HashMap<>();
                parsedInfo.put("hospitalName", parser.getHospitalName());
                parsedInfo.put("defaultAverageConsultationTime", parser.getDefaultAverageConsultationTime());
                parsedInfo.put("maximumOnlineBookings", parser.getMaximumOnlineBookings());
                parsedInfo.put("checkInRequired", parser.isCheckInRequired());
                parsedInfo.put("departments", parser.getDepartments());

                JsonUtil.sendSuccess(resp, "Hospital XML configuration parsed via DOM Parser.", parsedInfo);
            }
        }
    }
}

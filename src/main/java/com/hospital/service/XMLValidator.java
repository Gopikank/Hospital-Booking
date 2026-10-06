package com.hospital.service;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates hospital-config.xml against both DTD and XSD schemas.
 */
public class XMLValidator {

    public static Map<String, Object> validateAgainstXSD() {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            InputStream xsdStream = XMLValidator.class.getClassLoader().getResourceAsStream("hospital-config.xsd");
            InputStream xmlStream = XMLValidator.class.getClassLoader().getResourceAsStream("hospital-config.xml");

            if (xsdStream == null || xmlStream == null) {
                result.put("valid", false);
                errors.add("XSD schema or XML configuration file missing from classpath.");
                result.put("errors", errors);
                return result;
            }

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdStream));
            Validator validator = schema.newValidator();

            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    errors.add("Warning [Line " + e.getLineNumber() + "]: " + e.getMessage());
                }

                @Override
                public void error(SAXParseException e) {
                    errors.add("Error [Line " + e.getLineNumber() + "]: " + e.getMessage());
                }

                @Override
                public void fatalError(SAXParseException e) {
                    errors.add("Fatal Error [Line " + e.getLineNumber() + "]: " + e.getMessage());
                }
            });

            String xmlContent = new String(xmlStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            // Remove DOCTYPE when validating against XSD so parser does not seek external DTD in filesystem
            String xmlWithoutDoctype = xmlContent.replaceAll("<!DOCTYPE[^>]*>", "");

            validator.validate(new StreamSource(new java.io.StringReader(xmlWithoutDoctype)));
            result.put("valid", errors.isEmpty());
            result.put("message", errors.isEmpty() ? "XML configuration is strictly valid against hospital-config.xsd." : "XSD validation detected discrepancies.");
            result.put("errors", errors);

        } catch (Exception e) {
            result.put("valid", false);
            errors.add("Validation Exception: " + e.getMessage());
            result.put("errors", errors);
            result.put("message", "Validation failed: " + e.getMessage());
        }

        return result;
    }

    public static Map<String, Object> validateAgainstDTD() {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            InputStream xmlStream = XMLValidator.class.getClassLoader().getResourceAsStream("hospital-config.xml");
            if (xmlStream == null) {
                result.put("valid", false);
                errors.add("hospital-config.xml missing from classpath.");
                result.put("errors", errors);
                return result;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                if (systemId != null && systemId.endsWith("hospital-config.dtd")) {
                    InputStream dtdStream = XMLValidator.class.getClassLoader().getResourceAsStream("hospital-config.dtd");
                    return new InputSource(dtdStream);
                }
                return null;
            });

            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    errors.add("Warning [Line " + e.getLineNumber() + "]: " + e.getMessage());
                }

                @Override
                public void error(SAXParseException e) {
                    errors.add("Error [Line " + e.getLineNumber() + "]: " + e.getMessage());
                }

                @Override
                public void fatalError(SAXParseException e) {
                    errors.add("Fatal Error [Line " + e.getLineNumber() + "]: " + e.getMessage());
                }
            });

            builder.parse(xmlStream);
            result.put("valid", errors.isEmpty());
            result.put("message", errors.isEmpty() ? "XML configuration is strictly valid against hospital-config.dtd." : "DTD validation detected discrepancies.");
            result.put("errors", errors);

        } catch (Exception e) {
            result.put("valid", false);
            errors.add("DTD Validation Exception: " + e.getMessage());
            result.put("errors", errors);
            result.put("message", "DTD Validation failed: " + e.getMessage());
        }

        return result;
    }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Converts an MS-Test (.trx) file to a set of XML files in Junit format.
 */
public class MSTestToJUnit extends AbstractJUnitResults {

  private XMLStreamReader createXMLReader() throws XMLStreamException {
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    try {
      final Reader reader = new FileReader(getInputFile());
      // Read past the funny character prefix and <?...> bit which upset the parser
      while (reader.read() != '>') { // CSIGNORE
      }
      return factory.createXMLStreamReader(reader);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't open file", e);
    }
  }

  private static double parseTime(final String time) {
    if (time == null) {
      return 0;
    } else {
      final String[] times = time.split(":");
      return (double) (((Integer.parseInt(times[0]) * 60) + Integer.parseInt(times[1])) * 60)
          + Double.parseDouble(times[2]);
    }
  }

  @Override
  protected void readTests() throws IOException {
    try {
      final XMLStreamReader reader = createXMLReader();
      try {
        final Map<String, UnitTest> unitTestsById = new HashMap<>();
        UnitTest currentUnitTest = null;
        while (reader.hasNext()) {
          switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT: {
              final QName element = reader.getName();
              if (currentUnitTest == null) {
                if ("UnitTest".equals(element.getLocalPart())) {
                  currentUnitTest = new UnitTest(reader.getAttributeValue(null, "name"));
                  unitTestsById.put(reader.getAttributeValue(null, "id"), currentUnitTest);
                } else if ("UnitTestResult".equals(element.getLocalPart())) {
                  currentUnitTest = unitTestsById.get(reader.getAttributeValue(null, "testId"));
                  if (currentUnitTest != null) {
                    if ("Passed".equals(reader.getAttributeValue(null, "outcome"))) {
                      currentUnitTest.setPassed();
                    }
                    currentUnitTest.setComputerName(reader.getAttributeValue(null, "computerName"));
                    currentUnitTest.setStartTime(reader.getAttributeValue(null, "startTime"));
                    currentUnitTest.setDuration(parseTime(reader.getAttributeValue(null, "duration")));
                  }
                }
              } else {
                if ("TestMethod".equals(element.getLocalPart())) {
                  final String[] className = reader.getAttributeValue(null, "className").split(", ");
                  storeTest(className[0], currentUnitTest);
                }
              }
              break;
            }
            case XMLStreamConstants.CHARACTERS: {
              if (currentUnitTest != null) {
                final String text = reader.getText();
                currentUnitTest.appendText(text);
              }
              break;
            }
            case XMLStreamConstants.END_ELEMENT: {
              final QName element = reader.getName();
              if (currentUnitTest != null) {
                if ("UnitTest".equals(element.getLocalPart()) || "UnitTestResult".equals(element.getLocalPart())) {
                  currentUnitTest = null;
                }
              }
              break;
            }
          }
        }
      } finally {
        reader.close();
      }
    } catch (XMLStreamException e) {
      throw new IOException("XMLStreamException", e);
    }
  }

  public static void processOutput(final String inputFile, final String outputDir) {
    final MSTestToJUnit exec = new MSTestToJUnit();
    exec.setInputFile(inputFile);
    exec.setOutputDir(outputDir);
    exec.run();
  }

  /**
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    processOutput(args[0], args[1]);
  }

}

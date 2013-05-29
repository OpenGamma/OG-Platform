/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tools.ant.Task;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Converts an MS-Test (.trx) file to a set of XML files in Junit format.
 */
public class MSTestToJUnit implements Runnable {

  private String _inputFile;
  private String _outputDir;

  public void setInputFile(final String inputFile) {
    _inputFile = inputFile;
  }

  public String getInputFile() {
    return _inputFile;
  }

  public void setOutputDir(final String outputDir) {
    _outputDir = outputDir;
  }

  public String getOutputDir() {
    return _outputDir;
  }

  private XMLStreamReader createXMLReader() throws XMLStreamException {
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    try {
      final Reader reader = new FileReader(getInputFile());
      // Read past the funny character prefix and <?...> bit which upset the parser
      while (reader.read() != '>');  // CSIGNORE: deliberate short loop
      return factory.createXMLStreamReader(reader);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't open file", e);
    }
  }

  private static class UnitTest {

    private final String _name;
    private boolean _passed;
    private List<String> _text;
    private String _computerName;
    private String _startTime;
    private double _duration;

    public UnitTest(final String name) {
      _name = name;
    }

    public String getName() {
      return _name;
    }

    public void setPassed() {
      _passed = true;
    }

    public boolean isPassed() {
      return _passed;
    }

    public void appendText(String text) {
      text = text.trim();
      if (text.length() > 0) {
        if (_text == null) {
          _text = new LinkedList<String>();
        }
        _text.add(text);
      }
    }

    public String getMessage() {
      return (_text != null) ? _text.get(0) : "";
    }

    public List<String> getText() {
      return (_text != null) ? _text : Collections.<String>emptyList();
    }

    public void setComputerName(final String computerName) {
      _computerName = computerName;
    }

    public String getComputerName() {
      return _computerName;
    }

    public void setStartTime(final String startTime) {
      _startTime = startTime;
    }

    public String getStartTime() {
      return _startTime;
    }

    public void setDuration(final double duration) {
      _duration = duration;
    }

    public double getDuration() {
      return _duration;
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

  private Map<String, Collection<UnitTest>> readTests() {
    try {
      final XMLStreamReader reader = createXMLReader();
      final Map<String, UnitTest> unitTestsById = new HashMap<>();
      final Map<String, Collection<UnitTest>> unitTestsBySuite = new HashMap<>();
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
                final String[] codeBase = reader.getAttributeValue(null, "codeBase").split("/");
                final String[] className = reader.getAttributeValue(null, "className").split(", ");
                final String suite = codeBase[codeBase.length - 2] + "." + codeBase[codeBase.length - 1] + "."
                    + className[0];
                Collection<UnitTest> tests = unitTestsBySuite.get(suite);
                if (tests == null) {
                  tests = new LinkedList<UnitTest>();
                  unitTestsBySuite.put(suite, tests);
                }
                tests.add(currentUnitTest);
                currentUnitTest = null;
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
              if ("UnitTestResult".equals(element.getLocalPart())) {
                currentUnitTest = null;
              }
            }
            break;
          }
        }
      }
      reader.close();
      return unitTestsBySuite;
    } catch (XMLStreamException e) {
      throw new OpenGammaRuntimeException("Error reading XML", e);
    }
  }

  private static String getHostName(final Collection<UnitTest> tests) {
    for (UnitTest test : tests) {
      return test.getComputerName();
    }
    return null;
  }

  private static String getTimestamp(final Collection<UnitTest> tests) {
    final Iterator<UnitTest> test = tests.iterator();
    String earliest;
    do {
      earliest = test.next().getStartTime();
    } while ((earliest == null) && test.hasNext());
    if (earliest == null) {
      return "";
    }
    while (test.hasNext()) {
      final UnitTest nextTest = test.next();
      if (nextTest.getStartTime() != null) {
        if (earliest.compareTo(nextTest.getStartTime()) > 0) {
          earliest = nextTest.getStartTime();
        }
      }
    }
    return earliest;
  }

  private static double getTime(final Collection<UnitTest> tests) {
    double time = 0;
    for (UnitTest test : tests) {
      time += test.getDuration();
    }
    return time;
  }

  private static int getErrors(final Collection<UnitTest> tests) {
    int failures = 0;
    for (UnitTest test : tests) {
      if (!test.isPassed()) {
        failures++;
      }
    }
    return failures;
  }

  @Override
  public void run() {
    try {
      final XMLOutputFactory writerFactory = XMLOutputFactory.newInstance();
      for (Map.Entry<String, Collection<UnitTest>> testSuite : readTests().entrySet()) {
        final String path = getOutputDir() + File.separatorChar + "TEST-" + testSuite.getKey() + ".xml";
        final XMLStreamWriter out = writerFactory.createXMLStreamWriter(new FileOutputStream(path));
        out.writeStartDocument();
        out.writeComment("Generated by " + getClass().getName());
        out.writeStartElement("testsuite");
        out.writeAttribute("hostname", getHostName(testSuite.getValue()));
        out.writeAttribute("name", testSuite.getKey());
        out.writeAttribute("tests", Integer.toString(testSuite.getValue().size()));
        out.writeAttribute("errors", Integer.toString(getErrors(testSuite.getValue())));
        out.writeAttribute("timestamp", getTimestamp(testSuite.getValue()));
        out.writeAttribute("time", Double.toString(getTime(testSuite.getValue())));
        out.writeAttribute("failures", "0");
        for (UnitTest test : testSuite.getValue()) {
          out.writeStartElement("testcase");
          out.writeAttribute("name", test.getName());
          out.writeAttribute("time", Double.toString(test.getDuration()));
          out.writeAttribute("classname", testSuite.getKey());
          if (!test.isPassed()) {
            out.writeStartElement("error");
            out.writeAttribute("message", test.getMessage());
            for (String text : test.getText()) {
              out.writeCharacters(text);
            }
            out.writeEndElement();
          }
          out.writeEndElement();
        }
        out.writeEndElement();
        out.writeEndDocument();
        out.close();
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error writing to file", e);
    } catch (XMLStreamException e) {
      throw new OpenGammaRuntimeException("Error writing XML", e);
    }
  }

  public static void processOutput(final String inputFile, final String outputDir) {
    final MSTestToJUnit exec = new MSTestToJUnit();
    exec.setInputFile(inputFile);
    exec.setOutputDir(outputDir);
    exec.run();
  }

  /**
   * 
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    processOutput(args[0], args[1]);
  }

  /**
   * 
   */
  public static class AntTask extends Task {

    private String _inputFile;
    private String _outputDir;

    public void setInputFile(final String inputFile) {
      _inputFile = inputFile;
    }

    public String getInputFile() {
      return _inputFile;
    }

    public void setOutputDir(final String outputDir) {
      _outputDir = outputDir;
    }

    public String getOutputDir() {
      return _outputDir;
    }

    @Override
    public void execute() {
      processOutput(getInputFile(), getOutputDir());
    }

  }

}

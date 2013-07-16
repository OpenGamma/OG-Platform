/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.threeten.bp.LocalDateTime;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Converts test output to a set of XML files in JUnit format.
 */
public abstract class AbstractJUnitResults implements Runnable {

  private final Map<String, Collection<UnitTest>> _testsByClass = new HashMap<String, Collection<UnitTest>>();
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

  /**
   * Status information from an individual unit test.
   */
  public static class UnitTest {

    private final String _name;
    private boolean _passed;
    private StringBuilder _text;
    private String _computerName = "localhost";
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
      if (text.length() == 0) {
        return;
      }
      if (_text == null) {
        _text = new StringBuilder();
      } else {
        _text.append('\n');
      }
      _text.append(text);
    }

    public String getText() {
      if (_text == null) {
        return "";
      } else {
        return _text.toString();
      }
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

  protected String createBaseSuiteName() {
    String name = getInputFile();
    name = name.substring(name.lastIndexOf(File.separator) + 1);
    name = name.substring(0, name.lastIndexOf('-'));
    return name.replace('-', '.');
  }

  protected void storeTest(final String className, final UnitTest test) {
    Collection<UnitTest> tests = _testsByClass.get(className);
    if (tests == null) {
      tests = new LinkedList<UnitTest>();
      _testsByClass.put(className, tests);
    }
    tests.add(test);
  }

  protected abstract void readTests() throws IOException;

  protected int getErrors(final Collection<UnitTest> tests) {
    int count = 0;
    for (UnitTest test : tests) {
      if (!test.isPassed()) {
        count++;
      }
    }
    return count;
  }

  protected double getDuration(final Collection<UnitTest> tests) {
    double time = 0;
    for (UnitTest test : tests) {
      time += (double) test.getDuration();
    }
    return time / 1000;
  }

  private static String getHostName(final Collection<UnitTest> tests) {
    for (UnitTest test : tests) {
      if (test.getComputerName() != null) {
        return test.getComputerName();
      }
    }
    return "localhost";
  }

  protected static String getTimestamp(final Collection<UnitTest> tests) {
    final Iterator<UnitTest> test = tests.iterator();
    String earliest;
    do {
      earliest = test.next().getStartTime();
    } while ((earliest == null) && test.hasNext());
    if (earliest == null) {
      return LocalDateTime.now().toString();
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

  @Override
  public void run() {
    try {
      readTests();
      final String baseSuite = createBaseSuiteName();
      final XMLOutputFactory writerFactory = XMLOutputFactory.newInstance();
      for (Map.Entry<String, Collection<UnitTest>> testSuite : _testsByClass.entrySet()) {
        final String suite = baseSuite + '.' + testSuite.getKey();
        final String path = getOutputDir() + File.separatorChar + "TEST-" + suite + ".xml";
        final XMLStreamWriter out = writerFactory.createXMLStreamWriter(new FileOutputStream(path));
        out.writeStartDocument();
        out.writeComment("Generated by " + getClass().getName());
        out.writeStartElement("testsuite");
        out.writeAttribute("hostname", getHostName(testSuite.getValue()));
        out.writeAttribute("name", suite);
        out.writeAttribute("tests", Integer.toString(testSuite.getValue().size()));
        out.writeAttribute("errors", Integer.toString(getErrors(testSuite.getValue())));
        out.writeAttribute("timestamp", getTimestamp(testSuite.getValue()));
        out.writeAttribute("time", Double.toString(getDuration(testSuite.getValue())));
        out.writeAttribute("failures", "0");
        for (UnitTest test : testSuite.getValue()) {
          out.writeStartElement("testcase");
          out.writeAttribute("name", test.getName());
          out.writeAttribute("time", Double.toString((double) test.getDuration() / 1000));
          out.writeAttribute("classname", testSuite.getKey());
          if (!test.isPassed()) {
            out.writeStartElement("error");
            out.writeAttribute("message", test.getText());
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
      throw new OpenGammaRuntimeException("XML error", e);
    }
  }

}


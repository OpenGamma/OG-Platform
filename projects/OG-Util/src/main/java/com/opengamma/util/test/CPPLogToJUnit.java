/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tools.ant.Task;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Converts C++ log output to a set of XML files in JUnit format. Requires the
 * test output from OG-Language/Util/AbstractTest in at least INFO verbosity.
 */
public class CPPLogToJUnit implements Runnable {

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

  private static class UnitTest {

    private final String _name;
    private final String _className;
    private int _time;
    private String _error;

    public UnitTest(final String name, final String className) {
      _name = name;
      _className = className;
    }

    public String getName() {
      return _name;
    }

    public String getClassName() {
      return _className;
    }

    public int getTime() {
      return _time;
    }

    public void setTime(final int time) {
      _time = time;
    }

    public String getError() {
      return _error;
    }

    public void setError(final String error) {
      _error = error;
    }

  }

  private Map<String, Collection<UnitTest>> readTests() {
    final Map<String, Collection<UnitTest>> testsBySuite = new HashMap<>();
    final String baseSuite = getInputFile().substring(getInputFile().lastIndexOf(File.separator) + 1).replace('-', '.');
    final Pattern testClassStart = Pattern.compile(
        "INFO com\\.opengamma\\.language\\.util\\.AbstractTest null - Running test \\d+ - ([^\\s]+)");
    final Pattern timestamp = Pattern.compile("^(\\d+)");
    try (BufferedReader reader = new BufferedReader(new FileReader(getInputFile()))) {
      String str = reader.readLine();
      String currentSuite = null;
      String currentClass = null;
      UnitTest currentTest = null;
      Pattern testStart = null;
      Pattern testEnd = null;
      Pattern testFail = null;
      while (str != null) {
        Matcher matcher = testClassStart.matcher(str);
        if (matcher.find()) {
          testStart = Pattern.compile("\\.([^\\s\\.]+) null - Beginning " + matcher.group(1));
          currentClass = null;
        } else if (testStart != null) {
          matcher = testStart.matcher(str);
          if (matcher.find()) {
            if (currentClass == null) {
              currentClass = matcher.group(1);
              currentSuite = baseSuite + '.' + currentClass;
              testStart = Pattern.compile("\\." + matcher.group(1) + " null - Running test ([^\\s]+)");
            } else {
              currentTest = new UnitTest(matcher.group(1), currentClass);
              Collection<UnitTest> suite = testsBySuite.get(currentSuite);
              if (suite == null) {
                suite = new LinkedList<UnitTest>();
                testsBySuite.put(currentSuite, suite);
              }
              suite.add(currentTest);
              testEnd = Pattern.compile("\\." + currentClass + " null - Test " + currentTest.getName() + " complete");
              testFail = Pattern.compile("\\." + currentClass + " null - Assertion (\\d+) failed");
              testStart = null;
              matcher = timestamp.matcher(str);
              if (matcher.find()) {
                currentTest.setTime(Integer.parseInt(matcher.group(1)));
              }
            }
          }
        } else if (testEnd != null) {
          matcher = testEnd.matcher(str);
          if (matcher.find()) {
            matcher = timestamp.matcher(str);
            if (matcher.find()) {
              currentTest.setTime(Integer.parseInt(matcher.group(1)) - currentTest.getTime());
            }
            currentTest = null;
            testStart = Pattern.compile("\\." + currentClass + " null - Running test ([^\\s]+)");
            testEnd = null;
            testFail = null;
          } else {
            matcher = testFail.matcher(str);
            if (matcher.find()) {
              currentTest.setError("Assertion failed at line " + matcher.group(1));
              matcher = timestamp.matcher(str);
              if (matcher.find()) {
                currentTest.setTime(Integer.parseInt(matcher.group(1)) - currentTest.getTime());
              }
              currentTest = null;
              testStart = Pattern.compile("\\." + currentClass + " null - Running test ([^\\s]+)");
              testEnd = null;
              testFail = null;
            }
          }
        }
        str = reader.readLine();
      }
      return testsBySuite;
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error reading from file", e);
    }
  }

  private static int getErrors(final Collection<UnitTest> tests) {
    int count = 0;
    for (UnitTest test : tests) {
      if (test.getError() != null) {
        count++;
      }
    }
    return count;
  }

  private static double getTime(final Collection<UnitTest> tests) {
    double time = 0;
    for (UnitTest test : tests) {
      time += (double) test.getTime();
    }
    return time / 1000;
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
        out.writeAttribute("hostname", "localhost");
        out.writeAttribute("name", testSuite.getKey());
        out.writeAttribute("tests", Integer.toString(testSuite.getValue().size()));
        out.writeAttribute("errors", Integer.toString(getErrors(testSuite.getValue())));
        out.writeAttribute("timestamp", "=now()");
        out.writeAttribute("time", Double.toString(getTime(testSuite.getValue())));
        out.writeAttribute("failures", "0");
        for (UnitTest test : testSuite.getValue()) {
          out.writeStartElement("testcase");
          out.writeAttribute("name", test.getName());
          out.writeAttribute("time", Double.toString((double) test.getTime() / 1000));
          out.writeAttribute("classname", test.getClassName());
          if (test.getError() != null) {
            out.writeStartElement("error");
            out.writeAttribute("message", test.getError());
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

  public static void processOutput(final String inputFile, final String outputDir) {
    final CPPLogToJUnit exec = new CPPLogToJUnit();
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

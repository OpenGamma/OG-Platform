/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts C++ log output to a set of XML files in JUnit format. Requires the test output from OG-Language/Util/AbstractTest in at least INFO verbosity.
 */
public class CPPLogToJUnit extends AbstractJUnitResults {

  @Override
  protected void readTests() throws IOException {
    final Pattern testClassStart = Pattern.compile(
        "INFO com\\.opengamma\\.language\\.util\\.AbstractTest null - Running test \\d+ - ([^\\s]+)");
    final Pattern timestamp = Pattern.compile("^(\\d+)");
    final BufferedReader reader = new BufferedReader(new FileReader(getInputFile()));
    try {
      String str = reader.readLine();
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
              testStart = Pattern.compile("\\." + matcher.group(1) + " null - Running test ([^\\s]+)");
            } else {
              currentTest = new UnitTest(matcher.group(1));
              storeTest(currentClass, currentTest);
              testEnd = Pattern.compile("\\." + currentClass + " null - Test " + currentTest.getName() + " complete");
              testFail = Pattern.compile("\\." + currentClass + " null - Assertion (\\d+) failed");
              testStart = null;
              matcher = timestamp.matcher(str);
              if (matcher.find()) {
                currentTest.setStartTime(matcher.group(1));
              }
            }
          }
        } else if (testEnd != null) {
          matcher = testEnd.matcher(str);
          if (matcher.find()) {
            matcher = timestamp.matcher(str);
            if (matcher.find()) {
              currentTest.setDuration(Integer.parseInt(matcher.group(1)) - Integer.parseInt(currentTest.getStartTime()));
            }
            currentTest.setPassed();
            currentTest = null;
            testStart = Pattern.compile("\\." + currentClass + " null - Running test ([^\\s]+)");
            testEnd = null;
            testFail = null;
          } else {
            matcher = testFail.matcher(str);
            if (matcher.find()) {
              currentTest.appendText("Assertion failed at line " + matcher.group(1));
              matcher = timestamp.matcher(str);
              if (matcher.find()) {
                currentTest.setDuration(Integer.parseInt(matcher.group(1)) - Integer.parseInt(currentTest.getStartTime()));
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
    } finally {
      reader.close();
    }
  }

  public static void processOutput(final String inputFile, final String outputDir) {
    final CPPLogToJUnit exec = new CPPLogToJUnit();
    exec.setInputFile(inputFile);
    exec.setOutputDir(outputDir);
    exec.run();
  }

  /**
   * @param args command line arguments
   */
  public static void main(final String[] args) { //CSIGNORE
    processOutput(args[0], args[1]);
  }

}


/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import java.util.EnumSet;
import java.util.TreeSet;

/**
 * A standard set of logging levels for use with {@link LogEvent}.
 */
public enum LogLevel {

  /**
   * Detailed debug information.
   */
  TRACE,
  /**
   * Debug information.
   */
  DEBUG,
  /**
   * Information.
   */
  INFO,
  /**
   * Warning.
   */
  WARN,
  /**
   * Error.
   */
  ERROR,
  /**
   * Fatal.
   */
  FATAL,
}

class Test {

  public static void main(String[] args) {
    EnumSet<LogLevel> levels = EnumSet.noneOf(LogLevel.class);
    System.out.println(new TreeSet<LogLevel>(levels).last());
  }
}

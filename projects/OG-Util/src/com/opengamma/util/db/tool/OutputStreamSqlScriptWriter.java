/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.script.DbScript;

/**
 * Writes SQL scripts directly to an output stream. The overall output is itself a valid SQL script.   
 */
public class OutputStreamSqlScriptWriter implements SqlScriptWriter {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String COMMENT_PREFIX = "--";
  
  private final PrintWriter _printWriter;
  
  /**
   * Constructs an instance.
   * 
   * @param out  the output stream, not null
   */
  public OutputStreamSqlScriptWriter(OutputStream out) {
    ArgumentChecker.notNull(out, "out");
    _printWriter = new PrintWriter(out);
  }

  /**
   * Gets the print writer.
   * 
   * @return the print writer, not null
   */
  public PrintWriter getPrintWriter() {
    return _printWriter;
  }

  //-------------------------------------------------------------------------
  @Override
  public void write(String title, DbScript script) throws IOException {
    ArgumentChecker.notNull(title, "title");
    ArgumentChecker.notNull(script, "script");
    _printWriter.write(COMMENT_PREFIX + LINE_SEPARATOR);
    _printWriter.write(COMMENT_PREFIX + " " + title + LINE_SEPARATOR);
    _printWriter.write(COMMENT_PREFIX + LINE_SEPARATOR);
    _printWriter.write(script.getScript());
    _printWriter.write(LINE_SEPARATOR);
  }
  
  @Override
  public void close() throws IOException {
    _printWriter.close();
  }
  
}

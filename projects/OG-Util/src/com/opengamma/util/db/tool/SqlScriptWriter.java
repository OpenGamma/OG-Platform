/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.Closeable;
import java.io.File;

/**
 * Writes SQL scripts.
 */
public interface SqlScriptWriter extends Closeable {
  
  /**
   * Writes a SQL script from a string.
   * 
   * @param title  the title of the script for display purposes, not null
   * @param sql  the script, not null
   */
  void write(String title, String sql);
  
  /**
   * Writes a SQL script from a file.
   * 
   * @param title  the title of the script for display purposes, not null
   * @param sqlScript  the script, not null
   */
  void write(String title, File sqlScript);

}

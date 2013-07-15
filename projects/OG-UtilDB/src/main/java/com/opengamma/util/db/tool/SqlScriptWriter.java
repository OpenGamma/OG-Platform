/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.Closeable;
import java.io.IOException;

import com.opengamma.util.db.script.DbScript;

/**
 * Writes database scripts.
 */
public interface SqlScriptWriter extends Closeable {

  /**
   * Writes a database script.
   * 
   * @param title  the title of the script for display purposes, not null
   * @param script  the script, not null
   * @throws IOException  if there is a problem reading or writing the script
   */
  void write(String title, DbScript script) throws IOException;

}

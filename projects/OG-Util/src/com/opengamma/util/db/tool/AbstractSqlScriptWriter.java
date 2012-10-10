/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Abstract base implementation of {@link SqlScriptWriter}. 
 */
public abstract class AbstractSqlScriptWriter implements SqlScriptWriter {

  @Override
  public void write(String title, File sqlScript) {
    String sql;
    try {
      sql = FileUtils.readFileToString(sqlScript);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error reading SQL script '" + title + "' from file " + sqlScript, e);
    }
    write(title, sql);
  }
  
}

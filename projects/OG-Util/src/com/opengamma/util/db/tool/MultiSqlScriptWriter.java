/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.IOException;
import java.util.Collection;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.script.DbScript;

/**
 * Implementation of {@link SqlScriptWriter} which distributes calls to multiple underlying writers.
 */
public class MultiSqlScriptWriter implements SqlScriptWriter {

  private final Collection<SqlScriptWriter> _writers;
  
  public MultiSqlScriptWriter(Collection<SqlScriptWriter> writers) {
    ArgumentChecker.notNull(writers, "writers");
    _writers = writers;
  }

  @Override
  public void write(String title, DbScript script) throws IOException {
    for (SqlScriptWriter writer : _writers) {
      writer.write(title, script);
    }
  }
  
  @Override
  public void close() throws IOException {
    for (SqlScriptWriter writer : _writers) {
      writer.close();
    }
  }

}

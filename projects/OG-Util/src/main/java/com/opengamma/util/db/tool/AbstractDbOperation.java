/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.joda.beans.Property;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract representation of a database operation.
 * 
 * @param <T>  the type of database tool context
 */
public abstract class AbstractDbOperation<T extends DbToolContext> {

  private final T _dbToolContext;
  private final boolean _write;
  private final File _outputFile;
  
  /**
   * Base constructor.
   * 
   * @param dbToolContext  the database tool context, not null
   * @param write  true to modify the database, false to output the SQL that would be run 
   * @param outputFile  the file to which the SQL should be written, null not to write to a file
   */
  protected AbstractDbOperation(T dbToolContext, boolean write, File outputFile) {
    ArgumentChecker.notNull(dbToolContext, "dbToolContext");
    _dbToolContext = dbToolContext;
    _write = write;
    _outputFile = outputFile;
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the operation.
   */
  public abstract void execute();

  //-------------------------------------------------------------------------
  public T getDbToolContext() {
    return _dbToolContext;
  }
  
  public boolean isWrite() {
    return _write;
  }
  
  public File getOutputFile() {
    return _outputFile;
  }
  
  //-------------------------------------------------------------------------
  protected SqlScriptWriter createSqlScriptWriter() {
    SqlScriptWriter writer = null;
    if (getOutputFile() != null) {
      try {
        OutputStream out = new FileOutputStream(getOutputFile());
        writer = new OutputStreamSqlScriptWriter(out);
      } catch (FileNotFoundException e) {
        throw new OpenGammaRuntimeException("Error opening output file for writing", e);
      }
    }
    if (isWrite()) {
      SqlScriptWriter dbWriter = new DbSqlScriptWriter(getDbToolContext().getDbManagement(), getDbToolContext().getCatalog());
      writer = writer == null ? dbWriter : new MultiSqlScriptWriter(ImmutableList.of(writer, dbWriter));
    }
    if (writer == null) {
      writer = new OutputStreamSqlScriptWriter(System.out);
    }
    return writer;
  }
  
  protected void contextNotNull(Property<?> property) {
    if (property.get() == null) {
      throw new IllegalArgumentException(getDbToolContext().getClass().getSimpleName() + " must have the '" + property.name() + "' field set");
    }
  }

}

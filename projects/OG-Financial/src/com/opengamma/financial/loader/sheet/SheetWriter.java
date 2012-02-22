/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.sheet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;

public abstract class SheetWriter {
  
  public abstract void writeNextRow(Map<String, String> row);

  private String[] _columns; // The column names and order

  protected String[] getColumns() {
    return _columns;
  }

  protected void setColumns(String[] columns) {
    _columns = columns;
  }
  
  protected OutputStream openFile(String filename) {
    // Open input file for writing
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(filename);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading, exiting immediately.");
    }

    return fileOutputStream;
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.sheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;

/**
 * This abstract class represents a sheet writer that, given a map from column names to data, writes out a row containing that
 * data under the matching columns.
 */
public abstract class SheetWriter {

  private String[] _columns; // The column names and order
  
  public static SheetWriter newSheetWriter(String filename, String[] columns) {
    String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
    if (extension.equals(".csv")) {
      return new CsvSheetWriter(filename, columns);
//    } else if (extension.equals(".xls")) {
//      return new SimpleXlsSheetWriter(filename, columns, 0);
    } else {
      throw new OpenGammaRuntimeException("Could not identify the input format from the file name extension");
    }
  }

  public abstract void writeNextRow(Map<String, String> row);

  public abstract void flush();
  
  public abstract void close();
  
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
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for writing.");
    }

    return fileOutputStream;
  }

}

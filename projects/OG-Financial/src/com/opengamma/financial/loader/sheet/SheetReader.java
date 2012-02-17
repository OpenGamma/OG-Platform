/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;

/**
 * An abstract table class for importing portfolio data from spreadsheets
 */
public abstract class SheetReader {
  
  private String[] _columns; // The column names and order
  
  public static SheetReader newSheetReader(String filename) {
    String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
    if (extension.equals(".csv")) {
      return new CsvSheetReader(filename);
    } else if (extension.equals(".xls")) {
      return new SimpleXlsSheetReader(filename, 0);
    } else {
      throw new OpenGammaRuntimeException("Could not identify the input format from the file name extension");
    }
  }
  
  public abstract Map<String, String> loadNextRow();

  public String[] getColumns() {
    return _columns;
  }

  public void setColumns(String[] columns) {
    _columns = columns;
  }
  
  protected InputStream openFile(String filename) {
    // Open input file for reading
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(filename);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading, exiting immediately.");
    }

    return fileInputStream;
  }
  
}

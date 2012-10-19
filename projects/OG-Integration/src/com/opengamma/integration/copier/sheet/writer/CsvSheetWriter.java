/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * This class implements a sheet writer that facilitates writing rows to a csv file. The columns and their order are specified
 * in the constructor. Subsequently, rows to be written can be supplied as a map from column headings to the actual data. 
 */
public class CsvSheetWriter extends SheetWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(CsvSheetWriter.class);

  private CSVWriter _csvWriter;
  
  public CsvSheetWriter(String filename, String[] columns) {

    ArgumentChecker.notEmpty(filename, "filename");
    ArgumentChecker.notNull(columns, "columns");

    // Open file
    OutputStream fileOutputStream = openFile(filename);

    // Set up CSV Writer
    _csvWriter = new CSVWriter(new OutputStreamWriter(fileOutputStream));
    
    // Set columns
    setColumns(columns);
    
    // Write the column row
    _csvWriter.writeNext(columns);
    flush();
  }
   
  public CsvSheetWriter(OutputStream outputStream, String[] columns) {
    
    ArgumentChecker.notNull(outputStream, "outputStream");
    ArgumentChecker.notNull(columns, "columns");

    // Set up CSV Writer
    _csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
    
    // Set columns
    setColumns(columns);
    
    // Write the column row
    _csvWriter.writeNext(columns);
  }
 
  @Override
  public void writeNextRow(Map<String, String> row) {
    
    ArgumentChecker.notNull(row, "row");
    
    String[] rawRow = new String[getColumns().length];
    
    for (int i = 0; i < getColumns().length; i++) {
      if ((rawRow[i] = row.get(getColumns()[i])) == null) { //CSIGNORE
        s_logger.info("Missing data for column '" + getColumns()[i] + "' when writing row to CSV file");
        rawRow[i] = "";
      }
    }
    
    _csvWriter.writeNext(rawRow);
  }
  
  @Override
  public void flush() {
    try {
      _csvWriter.flush();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not flush to CSV file");
    }
  }
  
  @Override
  public void close() {
    try {
      _csvWriter.close();
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not close CSV file");
    }
  }
  
}

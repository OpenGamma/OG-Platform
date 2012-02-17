package com.opengamma.financial.loader.sheet;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvSheetWriter extends SheetWriter {

  private CSVWriter _csvWriter;

  public CsvSheetWriter(String filename, String[] columns) {

    // Open file
    OutputStream fileOutputStream = openFile(filename);

    // Set up CSV Writer
    _csvWriter = new CSVWriter(new OutputStreamWriter(fileOutputStream));
    
    // Set columns
    setColumns(columns);
    
    // Write the column row
    _csvWriter.writeNext(columns);
  }
   
  public CsvSheetWriter(OutputStream outputStream, String[] columns) {
    
    // Set up CSV Writer
    _csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
    
    // Set columns
    setColumns(columns);
    
    // Write the column row
    _csvWriter.writeNext(columns);
  }
 
  public void writeNextRow(Map<String, String> row) {
    
    String[] rawRow = new String[getColumns().length];
    
    for (int i = 0; i < getColumns().length; i++) {
      if ((rawRow[i] = row.get(getColumns()[i])) == null) {
        throw new OpenGammaRuntimeException("Missing column " + getColumns()[i] + " when writing a row to CSV file");
      }
    }
    
    _csvWriter.writeNext(rawRow);
  }
}

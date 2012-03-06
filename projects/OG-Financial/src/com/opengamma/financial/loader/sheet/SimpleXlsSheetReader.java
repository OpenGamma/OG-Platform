/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.sheet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.opengamma.OpenGammaRuntimeException;

/**
 * A class for importing portfolio data from XLS (pre-Excel 2007) worksheets
 * TODO XLS reader is incomplete, and does not really work yet!!!
 */
public class SimpleXlsSheetReader extends SheetReader {

  private Sheet _sheet;
  private Workbook _workbook;
  private int _currentRowNumber;
//  private int _firstRow;
//  private int _firstColumn;

  
  public SimpleXlsSheetReader(String filename, int sheetIndex) {
    InputStream fileInputStream = openFile(filename);
    _workbook = getWorkbook(fileInputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowNumber = _sheet.getFirstRowNum();
    
    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowNumber++);
    
    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }
  
  public SimpleXlsSheetReader(String filename, String sheetName) {
    InputStream fileInputStream = openFile(filename);
    _workbook = getWorkbook(fileInputStream);
    _sheet = _workbook.getSheet(sheetName);
    _currentRowNumber = _sheet.getFirstRowNum();

    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowNumber++);

    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow)); 
  }

  
  
  public SimpleXlsSheetReader(String filename, int sheetIndex, String[] columns) {
    InputStream fileInputStream = openFile(filename);
    _workbook = getWorkbook(fileInputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowNumber = _sheet.getFirstRowNum();
    setColumns(columns);
  }
  
  public SimpleXlsSheetReader(String filename, String sheetName, String[] columns) {
    InputStream fileInputStream = openFile(filename);
    _workbook = getWorkbook(fileInputStream);
    _sheet = _workbook.getSheet(sheetName);
    _currentRowNumber = _sheet.getFirstRowNum();
    setColumns(columns);
  }


  public SimpleXlsSheetReader(InputStream inputStream, int sheetIndex) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowNumber = _sheet.getFirstRowNum();
    
    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowNumber++);
    
    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }
  
  public SimpleXlsSheetReader(InputStream inputStream, String sheetName) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheet(sheetName);
    _currentRowNumber = _sheet.getFirstRowNum();

    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowNumber++);

    String[] columns = getColumnNames(rawRow);
    setColumns(columns); 
  }
  
  public SimpleXlsSheetReader(InputStream inputStream, int sheetIndex, String[] columns) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowNumber = _sheet.getFirstRowNum();
    setColumns(columns);
  }
  
  public SimpleXlsSheetReader(InputStream inputStream, String sheetName, String[] columns) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheet(sheetName);
    _currentRowNumber = _sheet.getFirstRowNum();
    setColumns(columns);
  }

  
  private Workbook getWorkbook(InputStream inputStream) {
    try {
      return new HSSFWorkbook(inputStream);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error opening Excel workbook: " + ex.getMessage());
    }    
  }
  
  @Override
  public Map<String, String> loadNextRow() {
    
    // Get a reference to the next Excel row
    Row rawRow = _sheet.getRow(_currentRowNumber++);

    // If the row is empty return null (assume end of table)
    if (rawRow == null || rawRow.getFirstCellNum() == -1) {
      return null; // new HashMap<String, String>();
    } 
      
   
    // Map read-in row onto expected columns
    Map<String, String> result = new HashMap<String, String>();
    for (int i = 0; i < getColumns().length; i++) {
      result.put(getColumns()[i], getCell(rawRow, rawRow.getFirstCellNum() + i));
    }

    return result;
  }
  
  
  /**
   * @param rawRow
   * @return
   */
  private String[] getColumnNames(Row rawRow) {
    String[] columns = new String[rawRow.getPhysicalNumberOfCells()];
    for (int i = 0; i < rawRow.getPhysicalNumberOfCells(); i++) {
      columns[i] = getCell(rawRow, i).trim().toLowerCase();
    }
    return columns;
  }
  
  public static Cell getCellSafe(Row rawRow, int column) {
     return rawRow.getCell(column, Row.CREATE_NULL_AS_BLANK);
  }
  
  public static String getCell(Row rawRow, int column) {
      return getCellAsString(getCellSafe(rawRow, column));
  }
  
  public static String getCellAsString(Cell cell) {

    if (cell == null) {
      return "";
    }
    switch (cell.getCellType()) {
      case Cell.CELL_TYPE_NUMERIC:
        return Double.toString(cell.getNumericCellValue());
      case Cell.CELL_TYPE_STRING:
        return cell.getStringCellValue();
      case Cell.CELL_TYPE_BOOLEAN:
        return Boolean.toString(cell.getBooleanCellValue());
      case Cell.CELL_TYPE_BLANK:
        return "";
      default:
        return "null";
    }
  }
  
}

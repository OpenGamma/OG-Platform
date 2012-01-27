/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

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
public class XlsSheetReader extends SheetReader {

  private Sheet _sheet;
  private Workbook _workbook;
  private int _firstRow;
//  private int _firstColumn;
  private int _currentRowNumber;
  
  public XlsSheetReader(InputStream inputStream, String[] columns) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheetAt(0);
    _firstRow = _sheet.getFirstRowNum();
    _currentRowNumber = _firstRow;
//    _firstColumn = 0;
  }

  public XlsSheetReader(InputStream inputStream, String sheetName, String[] columns) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheet(sheetName);
    //_currentRowNumber = _firstRow = firstRow;
    //_firstColumn = firstColumn;
  }

  public XlsSheetReader(InputStream inputStream, int sheetIndex, String[] columns) {
    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    //_currentRowNumber = _firstRow = firstRow;
    //_firstColumn = firstColumn;
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
    Row row = _sheet.getRow(_currentRowNumber++);

    // If the row is empty return an empty array
    if (row.getFirstCellNum() == -1) {
      return new HashMap<String, String>();
    } 
      
    // Create result array large enough to contain all the cells
    Map<String, String> result = new HashMap<String, String>(); // - row.getFirstCellNum()];
    
    for (Cell cell : row) {
      switch (cell.getCellType()) {
        case Cell.CELL_TYPE_NUMERIC:
          //result[cell.getColumnIndex()] = cell.getNumericCellValue();
          break;
        case Cell.CELL_TYPE_STRING:
          //result[cell.getColumnIndex()] = cell.getStringCellValue();
          break;
        default:
          //result[cell.getColumnIndex()] = null;
          break;
      }
    }
    
    return result;
  }

}

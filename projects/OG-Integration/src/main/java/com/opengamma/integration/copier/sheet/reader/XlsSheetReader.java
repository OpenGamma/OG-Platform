/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.reader;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * A class for importing portfolio data from XLS worksheets
 */
public class XlsSheetReader extends SheetReader {

  private static final Logger s_logger = LoggerFactory.getLogger(XlsSheetReader.class);

  private Sheet _sheet;
  private Workbook _workbook;
  private int _currentRowIndex;
  private InputStream _inputStream;
  
  public XlsSheetReader(String filename, int sheetIndex) {
    
    ArgumentChecker.notEmpty(filename, "filename");

    _inputStream = openFile(filename);
    _workbook = getWorkbook(_inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowIndex = _sheet.getFirstRowNum();
    
    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowIndex++);
    
    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }
  
  public XlsSheetReader(String filename, String sheetName) {
    
    ArgumentChecker.notEmpty(filename, "filename");
    ArgumentChecker.notEmpty(sheetName, "sheetName");

    InputStream fileInputStream = openFile(filename);
    _workbook = getWorkbook(fileInputStream);
    _sheet = getSheetSafely(sheetName);
    _currentRowIndex = _sheet.getFirstRowNum();

    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowIndex++);

    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow)); 
  }

  public XlsSheetReader(InputStream inputStream, int sheetIndex) {
    
    ArgumentChecker.notNull(inputStream, "inputStream");

    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowIndex = _sheet.getFirstRowNum();
    
    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowIndex++);
    
    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }
  
  public XlsSheetReader(InputStream inputStream, String sheetName) {
    
    ArgumentChecker.notNull(inputStream, "inputStream");
    ArgumentChecker.notEmpty(sheetName, "sheetName");

    _workbook = getWorkbook(inputStream);
    _sheet = getSheetSafely(sheetName);
    _currentRowIndex = _sheet.getFirstRowNum();

    // Read in the header row
    Row rawRow = _sheet.getRow(_currentRowIndex++);

    String[] columns = getColumnNames(rawRow);
    setColumns(columns); 
  }

  public XlsSheetReader(Workbook workbook, String sheetName) {
    ArgumentChecker.notNull(workbook, "workbook");
    ArgumentChecker.notEmpty(sheetName, "sheetName");
    _workbook = workbook;
    _sheet = getSheetSafely(sheetName);
    if (_sheet == null) {
      _sheet = _workbook.createSheet(sheetName);
      s_logger.warn("Workbook does not contain a sheet for {}", sheetName);
    }
    _currentRowIndex = _sheet.getFirstRowNum();
  }

  private Sheet getSheetSafely(String sheetName) {
    Sheet sheet = _workbook.getSheet(sheetName);
    if (sheet == null) {
      sheet = _workbook.createSheet(sheetName);
      s_logger.warn("Workbook does not contain a sheet for {}, temporary sheet created", sheetName);
    }
    return sheet;
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
    Row rawRow = _sheet.getRow(_currentRowIndex++);

    // If the row is empty return null (assume end of table)
    if (rawRow == null || rawRow.getFirstCellNum() == -1) {
      return null; // new HashMap<String, String>();
    } 

    // Map read-in row onto expected columns
    Map<String, String> result = new HashMap<String, String>();
    for (int i = 0; i < getColumns().length; i++) {
      String cell = getCell(rawRow, rawRow.getFirstCellNum() + i).trim();
      if (cell != null && cell.length() > 0) {
        result.put(getColumns()[i], cell);
      }
    }

    return result;
  }
  
  private String[] getColumnNames(Row rawRow) {
    String[] columns = new String[rawRow.getPhysicalNumberOfCells()];
    for (int i = 0; i < rawRow.getPhysicalNumberOfCells(); i++) {
      columns[i] = getCell(rawRow, i).trim().toLowerCase();
    }
    return columns;
  }
  
  private static Cell getCellSafe(Row rawRow, int column) {
    return rawRow.getCell(column, Row.CREATE_NULL_AS_BLANK);
  }
  
  private static String getCell(Row rawRow, int column) {
    return getCellAsString(getCellSafe(rawRow, column));
  }
  
  private static String getCellAsString(Cell cell) {

    if (cell == null) {
      return "";
    }
    switch (cell.getCellType()) {
      case Cell.CELL_TYPE_NUMERIC:
        return Double.toString(cell.getNumericCellValue());
        //return (new DecimalFormat("#.##")).format(cell.getNumericCellValue());
      case Cell.CELL_TYPE_STRING:
        return cell.getStringCellValue();
      case Cell.CELL_TYPE_BOOLEAN:
        return Boolean.toString(cell.getBooleanCellValue());
      case Cell.CELL_TYPE_BLANK:
        return "";
      default:
        return null;
    }
  }

  @Override
  public void close() {
    try {
      if (_inputStream != null) { //if sheet is multi sheeted, the first call with close input stream
        _inputStream.close();
      }
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error closing Excel workbook: " + ex.getMessage());
    }
  }

  public int getCurrentRowIndex() {
    return _currentRowIndex++;
  }

  /**
   * @param startRow, int to specify starting point, _currentRowIndex is set to startRow
   * @param startCol, int to specify starting point
   * @return Map<String, String> of all key/values until and empty row is reached.
   */
  public Map<String, String> readKeyValueBlock(int startRow, int startCol) {
    Map<String, String> keyValueMap = new HashMap<>();
    _currentRowIndex = startRow;
    Row row = _sheet.getRow(_currentRowIndex);
    while (row != null) {
      Cell keyCell = row.getCell(startCol);
      Cell valueCell = row.getCell(startCol + 1);
      keyValueMap.put(getCellAsString(keyCell), getCellAsString(valueCell));
      _currentRowIndex++;
      row = _sheet.getRow(_currentRowIndex);
    }
    _currentRowIndex++; //increment to prepare for next read method
    return keyValueMap;
  }

  /**
   * @param startRow, int to specify starting point, _currentRowIndex is set to startRow
   * @param startCol, int to specify starting point
   * @return Map<String, ObjectsPair<String, String>> of all key/value-pair until and empty row is reached.
   */
  public Map<String, ObjectsPair<String, String>> readKeyPairBlock(int startRow, int startCol) {
    Map<String, ObjectsPair<String, String>> keyPairMap = new HashMap<>();
    _currentRowIndex = startRow;
    Row row = _sheet.getRow(_currentRowIndex);
    while (row != null) {
      Cell keyCell = row.getCell(startCol);
      Cell firstValueCell = row.getCell(startCol + 1);
      Cell secondValueCell = row.getCell(startCol + 2);
      try {
        String stringCellValue = getCellAsString(keyCell);
        String stringFirstCellValue = getCellAsString(firstValueCell);
        String stringSecondCellValue = getCellAsString(secondValueCell);
        keyPairMap.put(stringCellValue,
                       ObjectsPair.of(stringFirstCellValue, stringSecondCellValue));
      } catch (IllegalStateException ise) {
        s_logger.error("Could not extract String value from cell col={} row={} sheet={}", startCol, _currentRowIndex, _sheet.getSheetName(), ise);
      }
      _currentRowIndex++;
      row = _sheet.getRow(_currentRowIndex);
    }
    _currentRowIndex++; //increment to prepare for next read method
    return keyPairMap;
  }

  /**
   * @param startRow, int to specify starting point, _currentRowIndex is set to startRow
   * @param startCol, int to specify starting point
   * @return Map<Pair<String, String>, String> of all ordinal-pair/value until and empty row is reached.
   */
  public Map<Pair<String, String>, String> readMatrix(int startRow, int startCol) {
    Map<Pair<String, String>, String> valueMap = new HashMap<>();
    _currentRowIndex = startRow;
    int tempRowIndex = _currentRowIndex + 1; // Ignore top left cell
    //Maps used to store the index of each x and y axis
    Map<Integer, String> colIndexToXAxis = new HashMap<>();
    Map<Integer, String> rowIndexToYAxis = new HashMap<>();

    Row xAxisRow = _sheet.getRow(_currentRowIndex);
    for (Cell cell : xAxisRow) {
      int columnIndex = cell.getColumnIndex();
      if (columnIndex != startCol) { // Ignore top left cell
        colIndexToXAxis.put(columnIndex, getCellAsString(cell));
      }
    }

    while (true) {
      Row yAxisRow = _sheet.getRow(tempRowIndex);
      if (yAxisRow == null) {
        break;
      }
      Cell yAxisCell = yAxisRow.getCell(startCol);
      rowIndexToYAxis.put(yAxisCell.getRowIndex(), getCellAsString(yAxisCell));
      tempRowIndex++;
    }

    _currentRowIndex++; //move to first row after x-axis

    while (true) {
      Row valueRow = _sheet.getRow(_currentRowIndex);
      if (valueRow == null) {
        break;
      }
      for (Cell valueCell : valueRow) {
        int columnIndex = valueCell.getColumnIndex();
        if (columnIndex != startCol) { // Ignore left y axis cells
          String xAxis = colIndexToXAxis.get(columnIndex);
          String yAxis = rowIndexToYAxis.get(_currentRowIndex);
          valueMap.put(ObjectsPair.of(xAxis, yAxis), valueCell.getStringCellValue());
        }
      }
      _currentRowIndex++;
    }
    _currentRowIndex++; //increment to prepare for next read method

    return valueMap;
  }
}

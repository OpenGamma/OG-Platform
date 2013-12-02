/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.writer;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class XlsSheetWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(XlsSheetWriter.class);

  private HSSFSheet _sheet;
  private final String[] _columns;
  private HSSFWorkbook _workbook;
  //private HashMap<Integer, String> _colIndexMap;
  private Integer _currentRow = 0;

  public XlsSheetWriter(HSSFWorkbook workbook, String sheetname, String[] columns) {

    ArgumentChecker.notEmpty(sheetname, "sheetname");
    ArgumentChecker.notNull(columns, "columns");
    ArgumentChecker.notNull(workbook, "workbook");

    _workbook = workbook;
    _sheet = _workbook.createSheet(sheetname);
    _columns = columns;
    //_colIndexMap = new HashMap<>();

    // Write the column row
    writeColumns();
  }

  private void writeColumns() {
    Row row = _sheet.getRow(_currentRow);
    if (row == null) {
      row = _sheet.createRow(_currentRow);
    }

    for (int i = 0; i < _columns.length; i++) {
      String value =  _columns[i];
      Cell cell = row.getCell(i);
      if (cell == null) {
        cell = row.createCell(i, Cell.CELL_TYPE_NUMERIC);
      }
      cell.setCellValue(value);
    }
    _currentRow++;
  }

  public void writeNextRow(Map<String, String> entries) {
    
    ArgumentChecker.notNull(entries, "entries");
    Row row = _sheet.getRow(_currentRow);
    if (row == null) {
      row = _sheet.createRow(_currentRow);
    }

    for (int i = 0; i < _columns.length; i++) {
      String colName = _columns[i];
      String value = entries.get(colName);
      Cell cell = row.getCell(i);
      if (cell == null) {
        cell = row.createCell(i, Cell.CELL_TYPE_NUMERIC);
      }
      cell.setCellValue(value);
    }
    _currentRow++;
  }
  
}

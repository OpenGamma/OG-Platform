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
import com.opengamma.util.tuple.ObjectsPair;

/**
 *
 */
public class XlsSheetWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(XlsSheetWriter.class);

  private HSSFSheet _sheet;
  //private final String[] _columns;
  private HSSFWorkbook _workbook;
  private Integer _currentRow = 0;

  //public XlsSheetWriter(HSSFWorkbook workbook, String sheetname, String[] columns) {
  //
  //  ArgumentChecker.notEmpty(sheetname, "sheetname");
  //  ArgumentChecker.notNull(columns, "columns");
  //  ArgumentChecker.notNull(workbook, "workbook");
  //
  //  _workbook = workbook;
  //  _sheet = _workbook.createSheet(sheetname);
  //  _columns = columns;
  //
  //}

  public XlsSheetWriter(HSSFWorkbook workbook, String sheetname) {

    ArgumentChecker.notEmpty(sheetname, "sheetname");
    ArgumentChecker.notNull(workbook, "workbook");

    _workbook = workbook;
    _sheet = _workbook.createSheet(sheetname);

  }

  private Row getCurrentRow() {
    Row row = _sheet.getRow(_currentRow);
    if (row == null) {
      row = _sheet.createRow(_currentRow);
    }
    return row;
  }

  private Cell getCell(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      cell = row.createCell(index, Cell.CELL_TYPE_STRING);
    }
    return cell;
  }

  //public void writeColumns() {
  //  Row row = getCurrentRow();
  //
  //  for (int i = 0; i < _columns.length; i++) {
  //    String value =  _columns[i];
  //    Cell cell = getCell(row, i);
  //    cell.setCellValue(value);
  //  }
  //  _currentRow++;
  //}
  //
  //public void writeNextRow(Map<String, String> entries) {
  //
  //  ArgumentChecker.notNull(entries, "entries");
  //  Row row = getCurrentRow();
  //
  //  for (int i = 0; i < _columns.length; i++) {
  //    String value = entries.get(_columns[i]);
  //    Cell cell = getCell(row, i);
  //    cell.setCellValue(value);
  //  }
  //  _currentRow++;
  //}

  public void writeBlock(Map<String, String> details) {
    ArgumentChecker.notNull(details, "details");

    for (Map.Entry<String, String> entry : details.entrySet()) {
      Row row = getCurrentRow();
      Cell keyCell = getCell(row, 0);
      Cell valueCell = getCell(row, 1);
      keyCell.setCellValue(entry.getKey());
      valueCell.setCellValue(entry.getValue());
      _currentRow++;
    }
    _currentRow++;

  }

  public void writePairBlock(Map<String, ObjectsPair<String, String>> details) {
    ArgumentChecker.notNull(details, "details");

    for (Map.Entry<String, ObjectsPair<String, String>> entry : details.entrySet()) {
      Row row = getCurrentRow();
      Cell keyCell = getCell(row, 1);
      keyCell.setCellValue(entry.getKey());
      if (entry.getValue().getFirst() != null) {
        Cell firstValueCell = getCell(row, 2);
        firstValueCell.setCellValue(entry.getValue().getFirst());
      }
      if (entry.getValue().getSecond() != null) {
        Cell secondValueCell = getCell(row, 3);
        secondValueCell.setCellValue(entry.getValue().getSecond());
      }
      _currentRow++;
    }
    _currentRow++;

  }
}

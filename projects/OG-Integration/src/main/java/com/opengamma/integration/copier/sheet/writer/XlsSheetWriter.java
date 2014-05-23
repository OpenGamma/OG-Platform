/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.writer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Provides tha ability to create and write to sheets in a given workbook
 */
public class XlsSheetWriter {

  private HSSFSheet _sheet;
  private HSSFWorkbook _workbook;
  private Integer _currentRowIndex = 0;
  private CellStyle _keyBlockStyle;
  private CellStyle _valueBlockStyle;
  private CellStyle _axisStyle;
  private HashSet<Integer> _columnIndices;

  public XlsSheetWriter(HSSFWorkbook workbook, String name) {

    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(workbook, "workbook");

    _workbook = workbook;
    _sheet = _workbook.createSheet(name);
    _columnIndices = new HashSet<>();
    _keyBlockStyle = getKeyBlockStyle();
    _valueBlockStyle = getValueBlockStyle();
    _axisStyle = getAxisStyle();
  }

  /**
   * Auto size all accessed columns, note this should only be called just before the workbook is closed.
   */
  public void autoSizeAllColumns() {
    for (int index : _columnIndices) {
      _sheet.autoSizeColumn(index);
    }
  }

  private Row getCurrentRow() {
    Row row = _sheet.getRow(_currentRowIndex);
    if (row == null) {
      row = _sheet.createRow(_currentRowIndex);
    }
    return row;
  }

  private Row getRow(int rowIndex) {
    Row row = _sheet.getRow(rowIndex);
    if (row == null) {
      row = _sheet.createRow(rowIndex);
    }
    return row;
  }

  public void decrementCurrentRowIndex() {
    _currentRowIndex--;
  }

  /**
   * @param row the current row
   * @param index the column index
   * @return Cell that matches the row/column co-ordinates
   * _columnIndices stores the unique column indices, needed for auto resize of columns
   */
  private Cell getCell(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      cell = row.createCell(index);
    }
    _columnIndices.add(index); //Store indices of columns
    return cell;
  }

  /**
   * @param row the current row
   * @param index the column index
   * @param cellType int that represents the type of cell
   * @return Cell that matches the row/column co-ordinates
   * _columnIndices stores the unique column indices, needed for auto resize of columns
   */
  private Cell getCell(Row row, int index, int cellType) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      cell = row.createCell(index, cellType);
    }
    _columnIndices.add(index); //Store indices of columns
    return cell;
  }

  private CellStyle getKeyBlockStyle() {
    CellStyle style = _workbook.createCellStyle();
    Font font = _workbook.createFont();
    font.setColor(HSSFColor.WHITE.index);
    HSSFPalette palette = _workbook.getCustomPalette();
    palette.setColorAtIndex(HSSFColor.BLUE.index, (byte) 3, (byte) 60, (byte) 90);
    style.setFillForegroundColor(HSSFColor.BLUE.index);
    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
    style.setFont(font);
    return style;
  }

  private CellStyle getValueBlockStyle() {
    CellStyle style = _workbook.createCellStyle();
    Font font = _workbook.createFont();
    HSSFPalette palette = _workbook.getCustomPalette();
    palette.setColorAtIndex(HSSFColor.BLUE_GREY.index, (byte) 238, (byte) 238, (byte) 238);
    style.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
    style.setFont(font);
    return style;
  }

  private CellStyle getAxisStyle() {
    CellStyle style = _workbook.createCellStyle();
    Font font = _workbook.createFont();
    font.setColor(HSSFColor.WHITE.index);
    HSSFPalette palette = _workbook.getCustomPalette();
    palette.setColorAtIndex(HSSFColor.GREY_50_PERCENT.index, (byte) 68, (byte) 68, (byte) 68);
    style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
    style.setFont(font);
    return style;
  }

  public void writeKeyValueBlock(Map<String, String> details) {
    ArgumentChecker.notNull(details, "details");

    for (Map.Entry<String, String> entry : details.entrySet()) {
      Row row = getCurrentRow();
      Cell keyCell = getCell(row, 0);
      Cell valueCell = getCell(row, 1);
      keyCell.setCellStyle(_keyBlockStyle);
      valueCell.setCellStyle(_valueBlockStyle);
      keyCell.setCellValue(entry.getKey());
      valueCell.setCellValue(entry.getValue());
      _currentRowIndex++;
    }
    _currentRowIndex++;
  }

  public void writeKeyPairBlock(Map<String, ObjectsPair<String, String>> details) {
    ArgumentChecker.notNull(details, "details");
    CellStyle currentStyle = _keyBlockStyle;
    for (Map.Entry<String, ObjectsPair<String, String>> entry : details.entrySet()) {
      Row row = getCurrentRow();
      Cell keyCell = getCell(row, 0);
      keyCell.setCellValue(entry.getKey());
      keyCell.setCellStyle(_keyBlockStyle);
      if (entry.getValue().getFirst() != null) {
        Cell firstValueCell = getCell(row, 1);
        firstValueCell.setCellValue(entry.getValue().getFirst());
        firstValueCell.setCellStyle(currentStyle);
      }
      if (entry.getValue().getSecond() != null) {
        Cell secondValueCell = getCell(row, 2);
        secondValueCell.setCellValue(entry.getValue().getSecond());
        secondValueCell.setCellStyle(currentStyle);
      }
      _currentRowIndex++;
      currentStyle = _valueBlockStyle;
    }
    _currentRowIndex++;
  }

  /**
   * @param xMap Set of ordered labels for the x axis
   * @param yMap  Set of ordered labels for the y axis
   * @param label String label for cell 0/0
   * @param valueMap Map containing a Pair of x and y co-ordinates to value
   * @param cellValueType int that represents the type of cell
   */
  public void writeMatrix(Set<String> xMap,
                          Set<String> yMap,
                          String label,
                          Map<Pair<String, String>, String> valueMap,
                          int cellValueType) {

    ArgumentChecker.notNull(xMap, "xMap");
    ArgumentChecker.notNull(yMap, "yMap");
    ArgumentChecker.notNull(valueMap, "valueMap");

    //Maps used to store the index of each x and y axis
    Map<String, Integer> xCol = new HashMap<>();
    Map<String, Integer> yRow = new HashMap<>();

    /* Print out the label */
    Row labelRow = getCurrentRow();
    Cell labelCell = getCell(labelRow, 0);
    labelCell.setCellValue(label);
    labelCell.setCellStyle(_axisStyle);

    //Print out the x axis
    int colIndex = 1;
    for (String entry : xMap) {
      Row row = getCurrentRow();
      Cell cell = getCell(row, colIndex);
      cell.setCellValue(entry);
      cell.setCellStyle(_axisStyle);
      xCol.put(entry, colIndex);
      colIndex++;
    }

    _currentRowIndex++;
    //Print out the y axis
    for (String entry : yMap) {
      Row row = getCurrentRow();
      Cell cell = getCell(row, 0, cellValueType);
      cell.setCellValue(entry);
      cell.setCellStyle(_axisStyle);
      yRow.put(entry, _currentRowIndex);
      _currentRowIndex++;
    }
    _currentRowIndex++;

    //Print out the values of the matrix, locate co-ordinates based on  key of valueMap and the xCol/yRow maps
    for (Map.Entry<Pair<String, String>, String> entry : valueMap.entrySet()) {
      Cell valueCell = getCell(getRow(yRow.get(entry.getKey().getSecond())), xCol.get(entry.getKey().getFirst()));
      valueCell.setCellValue(entry.getValue());
      valueCell.setCellStyle(_valueBlockStyle);
    }
  }

}

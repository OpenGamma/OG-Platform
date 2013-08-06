/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * The description of a GICS code.
 * <p>
 * This provides a description for {@link GICSCode}.
 * <p>
 * This is an effective singleton.
 * 
 *  S&P provides an Excel file of GICS code mappings.  To load support for these mappings, the file provided by S&P must be renamed 'gics_map.xls'
 *  and be located in the classpath. If the file does not have this name or the Classloader cannot find it, an error will be logged
 *  and GICS code mapping will not be available.
 *  
 *  @see <a href="http://www.standardandpoors.com/indices/gics/en/us">Standard and Poors</a>
 */
final class GICSCodeDescription {

  /** Logger. */
  public static final GICSCodeDescription INSTANCE = new GICSCodeDescription();
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(GICSCodeDescription.class);

  private static final String GICS_FILE_NAME = "gics_map.xls";

  /**
   * The descriptions.
   */
  private static final Map<String, String> DESCRIPTIONS = new HashMap<String, String>();
  static {
    InputStream xlsStream = GICSCodeDescription.class.getClassLoader().getResourceAsStream(GICS_FILE_NAME);
    processGICSExcelWorkbook(xlsStream, DESCRIPTIONS);
  }

  /**
   *  Load S&P GICS code mappings from an Excel file stream
   * @param inputStream opened stream based on Excel file
   * @param gicsMap Map to add mappings to
   */
  static void processGICSExcelWorkbook(InputStream inputStream, Map<String, String> gicsMap) {
    Workbook workbook;
    try {
      workbook = new HSSFWorkbook(new BufferedInputStream(inputStream));
    } catch (IOException e) {
      s_logger.warn("Unable to find S&P GICS Code Mapping file '" + GICS_FILE_NAME +
                    "' in classpath; unable to use GICS Codes: " + e);
      return;
    }
    processGICSExcelWorkbook(workbook, gicsMap);
  }

  /**
   *  Load S&P GICS code mappings from an Apace POI HSSFWorkbook 
   * @param workbook HSSFWorkbook to parse S&P GCIS Excel
   * @param gicsMap Map to add mappings to
   */
  static void processGICSExcelWorkbook(Workbook workbook, Map<String, String> gicsMap) {

    //Assume 1 sheet
    Sheet sheet = workbook.getSheetAt(0);
    if (sheet == null) {
      return;
    }
    for (int rowNum = sheet.getFirstRowNum(); rowNum <= sheet.getLastRowNum(); rowNum++) {
      Row row = sheet.getRow(rowNum);
      if (row == null) {
        continue;
      }
      for (int cellNum = 0; cellNum < row.getPhysicalNumberOfCells(); cellNum++) {
        Cell cell = row.getCell(cellNum, Row.CREATE_NULL_AS_BLANK);
        if (isNumeric(cell)) {
          //worst case if the Excel file is in an  incorrect (or updated) format
          // is that number -> random or empty string mappings will be created
          gicsMap.put(getGICSCellValue(cell), getGICSCellValue(row, cellNum + 1));
        } 
      }
    }
  }


  /**
   * Get the value of the Apache POI Cell as a String.  If the Cell type is numeric (always a double with POI),
   * the value is converted to an integer.  The GCIS file does not contain any floating point values so (at this time)
   * this is a valid operation
   * 
   * @param cell Apache POI Cell
   * @return String value
   */
  static String getGICSCellValue(Cell cell) {
    if (cell == null) {
      return "";
    } 
    switch (cell.getCellType()) {
      case Cell.CELL_TYPE_NUMERIC:
        return Integer.valueOf((int) cell.getNumericCellValue()).toString();
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


  /**
   * Get the value of the Apache POI Cell specified by the row and cell num (column) as a String. 
   * If row,cellNum defines a null or blank cell, an empty String is returned
   * @param row Apace POI Row
   * @param cellNum cell number in Row
   * @return String value of specified cell, or empty String if invalid cell
   */
  static String getGICSCellValue(Row row, int cellNum) {
    return getGICSCellValue(row.getCell(cellNum, Row.CREATE_NULL_AS_BLANK));

  }

  /**
   * Determine if specfied Cell contains a number or something else based on the cell 
   * type defined in the source Excel file.
   * @param cell Apace POI Cell
   * @return true if numeric, false if any other type
   */
  static boolean isNumeric(Cell cell) {
    return cell.getCellType() == Cell.CELL_TYPE_NUMERIC;
  }

  /**
   * Creates an instance.
   */
  private GICSCodeDescription() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the description for the code.
   * 
   * @param code  the code to lookup, not null
   * @return the description, "Unknown" if not found
   */
  String getDescription(String code) {
    String desc = DESCRIPTIONS.get(code);
    return Objects.firstNonNull(desc, "Unknown");
  }

  /**
   * Gets all the sector descriptions 
   * @return a collection of all the sector description strings
   */
  Collection<String> getAllSectorDescriptions() {
    return getAllDescriptions(2);
  }

  /**
   * Gets all the industry group descriptions 
   * @return a collection of all the industry group description strings
   */
  Collection<String> getAllIndustryGroupDescriptions() {
    return getAllDescriptions(4);
  }

  /**
   * Gets all the industry descriptions 
   * @return a collection of all the industry description strings
   */
  Collection<String> getAllIndustryDescriptions() {
    return getAllDescriptions(6);
  }

  /**
   * Gets all the sub-industry descriptions 
   * @return a collection of all the sub-industry description strings
   */
  Collection<String> getAllSubIndustryDescriptions() {
    return getAllDescriptions(8);
  }

  /**
   * Get all descriptions with a particular code length
   * @param codeLength the number of digits in the code
   * @return a collection of all the description strings
   */
  private Collection<String> getAllDescriptions(int codeLength) {
    Collection<String> results = new ArrayList<String>();
    for (Map.Entry<String, String> entry : DESCRIPTIONS.entrySet()) {
      if (entry.getKey().length() == codeLength) {
        results.add(entry.getValue());
      }
    }
    return results;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a simple string description for the class.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /**
   * Output the current contents of the GICS map to the log
   * @param gicsMap Map of GICS code -> description
   */
  static void dumpGICSMap(Map<String, String> gicsMap) {
    for (Map.Entry<String, String> entry : gicsMap.entrySet()) {
      s_logger.info(" {}  -> {} ", entry.getKey(), entry.getValue());
    }
  }


  /**
   * For testing.  Logs the contents of the GICS code->description map that is loaded
   * statically
   * @param args
   * @return
   */
  private boolean run(String[] args) {
    s_logger.info(this.toString() + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    dumpGICSMap(DESCRIPTIONS);
    s_logger.info(this.toString() + " is finished.");
    return true;
  }

  /**
   * For standalone testing 
   * @param args command line arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    boolean success = new GICSCodeDescription().run(args);
    System.exit(success ? 0 : 1);
  }

}

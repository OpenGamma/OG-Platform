/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Manages the ISDA test grids
 *
 * Martin Traverse, Niels Stchedroff (Riskcare)
 */
public class ISDATestGridManager {

  private static final String RESOURCE_DIR = "resources";
  private static final String TEST_GRID_DIR = "isda_test_grids";
  private static final String BENCHMARK_DIR = "benchmark";
  private static final String CORPORATE_DIR = "corporate";
  private static final String HOLIDAY_DIR = "holiday";

  public Map<String,String[]> findAllTestGrids() throws Exception {

    final Map<String,String[]> testGrids = new HashMap<>();
    testGrids.put(BENCHMARK_DIR, findAllTestGridsForCategory(BENCHMARK_DIR));
    testGrids.put(CORPORATE_DIR, findAllTestGridsForCategory(CORPORATE_DIR));
    testGrids.put(HOLIDAY_DIR, findAllTestGridsForCategory(HOLIDAY_DIR));
    return testGrids;
  }

  public String[] findAllTestGridsForCategory(final String category) throws Exception {

    final String path = RESOURCE_DIR + File.separator + TEST_GRID_DIR + File.separator + category;
    final URL directory = getClass().getClassLoader().getResource(path);

    if (directory == null) {
      throw new RuntimeException("ISDA test grid directory not found: " + path);
    }

    if (directory.getProtocol().equals("file")) {
      return new File(directory.toURI()).list();
    }

    throw new RuntimeException("Unknown file access protocol for ISDA test grids");
  }

  public ISDATestGrid loadTestGrid(final String category, final String fileName) throws Exception {

    InputStream is = null;

    try {
      final String path = RESOURCE_DIR + File.separator + TEST_GRID_DIR + File.separator + category + File.separator + fileName;
      is = getClass().getClassLoader().getResourceAsStream(path);
      final Workbook wb = new HSSFWorkbook(is);
      final Sheet sheet = wb.getSheetAt(0);

      final ISDATestGrid testGrid = new ISDATestGrid();
      testGrid.process(sheet);
      return testGrid;
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }

}

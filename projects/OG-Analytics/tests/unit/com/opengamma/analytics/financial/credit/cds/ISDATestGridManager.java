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
 */
public class ISDATestGridManager {
  
  public static final String s_resourcesDir = "resources";
  public static final String s_testGridDir = "isda_test_grids";
  public static final String s_benchmarkDir = "benchmark";
  public static final String s_corporateDir = "corporate";
  
  public Map<String,String[]> findAllTestGrids() throws Exception {
    
    Map<String,String[]> testGrids = new HashMap<String,String[]>();
    testGrids.put(s_benchmarkDir, findAllTestGridsForCategory(s_benchmarkDir));
    testGrids.put(s_corporateDir, findAllTestGridsForCategory(s_corporateDir));
    return testGrids;
  }

  public String[] findAllTestGridsForCategory(final String category) throws Exception {

    final String path = s_resourcesDir + File.separator + s_testGridDir + File.separator + category;
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
      String path = s_resourcesDir + File.separator + s_testGridDir + File.separator + category + File.separator + fileName;
      is = getClass().getClassLoader().getResourceAsStream(path);
      Workbook wb = new HSSFWorkbook(is);
      Sheet sheet = wb.getSheetAt(0);

      ISDATestGrid testGrid = new ISDATestGrid();
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

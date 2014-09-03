/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for the different Gamma analysis tutorials.
 */
public class GammaAnalysisUtils {

  /**
   * Parse a csv file to extract the curves shifts.
   * @param filename The name of the file (and its path).
   * @return The shifts as a double[][] : shift[i][j] is the i-th scenario and j-th node.
   * @throws IOException
   */
  public static double[][] parseShifts(String filename, double scaling) throws IOException {
    ArgumentChecker.notNull(filename, "fine name");
    ArrayList<double[]> list = new ArrayList<>();
    double[][] shift;
    try (CSVReader reader = getCSVReader(filename)) {
      String[] line;
      while ((line = reader.readNext()) != null) {
        double[] lineDouble = new double[line.length];
        for (int loopc = 0; loopc < line.length; loopc++) {
          lineDouble[loopc] = Double.parseDouble(line[loopc]) * scaling;
        }
        list.add(lineDouble);
      }
      shift = list.toArray(new double[0][0]);
    }
    return shift;
  }

  private static CSVReader getCSVReader(String filename) throws IOException {
    if (filename.endsWith(".zip")) {
      ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(new File(filename))));
      zipInputStream.getNextEntry();
      return new CSVReader(new InputStreamReader(zipInputStream));
    } else if (filename.endsWith(".csv") || filename.endsWith("*.tsv")) {
      return new CSVReader(new BufferedReader(new FileReader(new File(filename))));
    } else {
      throw new IOException("Unsupported file type " + filename);
    }
  }

}

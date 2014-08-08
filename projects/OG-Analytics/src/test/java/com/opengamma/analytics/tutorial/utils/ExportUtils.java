/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Utilities to export objects (typically in csv files) used in the tutorials.
 */
public class ExportUtils {

  /**
   * Export a multi-curve parameter sensitivity into a csv file.
   * @param sensitivity The sensitivity.
   * @param fileName The file name.
   */
  public static void exportMultipleCurrencyParameterSensitivity(MultipleCurrencyParameterSensitivity sensitivity, String fileName) {
    Map<Pair<String, Currency>, DoubleMatrix1D> map = sensitivity.getSensitivities();
    try {
      final FileWriter writer = new FileWriter(fileName);
      for (Pair<String, Currency> pair : map.keySet()) {
        writer.append(pair.getFirst().toString() + ", " + pair.getSecond().toString() + "\n");
        double[] matrix = map.get(pair).getData();
        String row = "";
        for (int i = 0; i < matrix.length; i++) {
          row = row + matrix[i] + ", ";
        }
        row = row + "\n";
        writer.append(row);
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

}

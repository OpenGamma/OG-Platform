/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.DoublesCurve;
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
  
  static double getRate(double value, double time, boolean transform) {
    if (transform) {
      return -Math.log(value) / time;
    } else {
      return value;
    }
  }

  public static void consolePrint(MultipleCurrencyParameterSensitivity sensitivities, MulticurveProviderDiscount curves) {
    System.out.println("--- Sensitivities ---");
    System.out.println("Curve name,Currency,Date fraction,Zero rate,PV01");
    for (String yieldCurveName : curves.getAllCurveNames()) {
      boolean transformDfToZeroRates = false;
      YieldAndDiscountCurve yieldAndDiscountCurve = curves.getCurve(yieldCurveName);
      Curve<Double, Double> yieldCurveValues = null;

      if (yieldAndDiscountCurve instanceof YieldCurve) {
        yieldCurveValues = ((YieldCurve)yieldAndDiscountCurve).getCurve();
      } else if (yieldAndDiscountCurve instanceof DiscountCurve) {
        yieldCurveValues = ((DiscountCurve)yieldAndDiscountCurve).getCurve();
        transformDfToZeroRates = true;
      }
      Double[] dateFractions = yieldCurveValues.getXData();
      Double[] zeroRates = yieldCurveValues.getYData();
      Map<Currency, DoubleMatrix1D> sensitivitiesPerCcy = sensitivities.getSensitivityByName(yieldCurveName);
      for (Currency ccy : sensitivitiesPerCcy.keySet()) {
        double[] sensitivitiesValues = sensitivitiesPerCcy.get(ccy).getData();
        for(int i = 0; i < sensitivitiesValues.length; ++i) {
          System.out.println(
              yieldCurveName + "," +
              ccy + "," +
              String.valueOf(dateFractions[i]) + "," +
              String.valueOf(getRate(zeroRates[i], dateFractions[i], transformDfToZeroRates)) + "," +
              String.valueOf(sensitivitiesValues[i]));
        }
      }
    }
  }

}

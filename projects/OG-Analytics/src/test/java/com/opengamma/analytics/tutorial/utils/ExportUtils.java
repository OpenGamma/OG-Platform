/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Utilities to export objects (typically in csv files or in the console) used in the tutorials.
 */
public class ExportUtils {

  /**
   * Export a multi-curve parameter sensitivity into a csv file.
   * @param sensitivity The sensitivity.
   * @param fileName The name of the file in which the curve description is exported.
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
  
  /**
   * Export a MulticurveProviderDiscounting into a csv file. 
   * Each curve should be of the type YieldCurve or DiscountCurve.
   * The underlying of each curve should be an InterpolatedDoublesCurve.
   * A file is created locally. Each curve is represented by its name, each currency for which it is a discounting curve and
   * each index (Ibor or Overnight) for which it is a forward curve.
   * The YieldCurve are described by the nodes times and rates (zero-coupon continously compounded). 
   * The DiscountCurve are described by the nodes times and discount factors. 
   * @param multicurve The multicurve provider.
   * @param fileName The name of the file in which the curve description is exported.
   */
  public static void exportMulticurveProviderDiscount(MulticurveProviderDiscount multicurve, String fileName) {
    Set<String> curveNamesSet = multicurve.getAllCurveNames();
    try {
      final FileWriter writer = new FileWriter(fileName);
      for(String name: curveNamesSet) {
        writer.append("Curve name: " + name + "\n");
        YieldAndDiscountCurve curve = multicurve.getCurve(name);
        Currency ccy = multicurve.getCurrencyForName(name);
        if(ccy != null) {
          writer.append("Currency: " + ccy.toString() + "\n");
        }
        IborIndex indexIbor = multicurve.getIborIndexForName(name);
        if(indexIbor != null) {
          writer.append("Ibor Index: " + indexIbor.toString() + "\n");
        }
        IndexON indexOn = multicurve.getOvernightIndexForName(name);
        if(indexOn != null) {
          writer.append("Overnight Index: " + indexOn.toString() + "\n");
        }
        ArgumentChecker.isTrue((curve instanceof YieldCurve) || (curve instanceof DiscountCurve) , 
            "curve should be YieldCurve or DiscountCurve");
        InterpolatedDoublesCurve interpolatedCurve;
        if(curve instanceof YieldCurve) { // YieldCurve
          writer.append("Time, Rate \n");
          YieldCurve yieldCurve = (YieldCurve) curve;
          ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve, 
              "curve underlying should be of the type interpolatedDoublesCurve");
          interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
        } else { // DiscountCurve
          writer.append("Time, Discount Factor \n");
          DiscountCurve discountCurve = (DiscountCurve) curve;
          ArgumentChecker.isTrue(discountCurve.getCurve() instanceof InterpolatedDoublesCurve, 
              "curve underlying should be of the type interpolatedDoublesCurve");
          interpolatedCurve = (InterpolatedDoublesCurve) discountCurve.getCurve();
        }
        double[] x = interpolatedCurve.getXDataAsPrimitive();
        double[] y = interpolatedCurve.getYDataAsPrimitive();
        int nbNode = x.length;
        for(int loopnode=0; loopnode<nbNode; loopnode++) {
          writer.append(x[loopnode] + ", " + y[loopnode] + "\n");
        }
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

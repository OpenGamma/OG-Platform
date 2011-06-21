/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PDEUtilityTools {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle> GRID_INTERPOLATOR2D = new GridInterpolator2D<Interpolator1DDoubleQuadraticDataBundle, Interpolator1DDoubleQuadraticDataBundle>(
      INTERPOLATOR_1D,
      INTERPOLATOR_1D);

  public static Map<Double, Interpolator1DDoubleQuadraticDataBundle> getInterpolatorDataBundle(PDEFullResults1D res) {
    int tNodes = res.getNumberTimeNodes();
    int xNodes = res.getNumberSpaceNodes();

    int n = xNodes * tNodes;
    Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      double t = res.getTimeValue(i);

      for (int j = 0; j < xNodes; j++) {
        double k = res.getSpaceValue(j);
        DoublesPair tk = new DoublesPair(t, k);
        out.put(tk, res.getFunctionValue(j, i));
      }
    }

    Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(out);
    return dataBundle;
  }

  /**
   * Takes the results from a forward PDE solve - grid of option prices by maturity and strike and returns a map between a DoublesPair (i.e. maturity and strike) and
   * the Black implied volatility 
   * @param forward
   * @param prices
   * @param minT Data before this time is ignored (not included in map)
   * @param maxT Data after this time is ignored (not included in map)
   * @param minK Strikes less than this are ignored (not included in map)
   * @param maxK Strikes greater than this are ignored (not included in map)
   * @return
   */
  public static Map<DoublesPair, Double> priceToImpliedVol(final ForwardCurve forward, final PDEFullResults1D prices,
      final double minT, final double maxT, final double minK, final double maxK) {
    int xNodes = prices.getNumberSpaceNodes();
    int tNodes = prices.getNumberTimeNodes();
    int n = xNodes * tNodes;
    Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);
    int count = tNodes * xNodes;

    for (int i = 0; i < tNodes; i++) {
      double t = prices.getTimeValue(i);
      if (t >= minT && t <= maxT) {
        BlackFunctionData data = new BlackFunctionData(forward.getForward(t), forward.getSpot() / forward.getForward(t), 0);
        for (int j = 0; j < xNodes; j++) {
          double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            double price = prices.getFunctionValue(j, i);
            EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
            try {
              double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, price);
              if (Math.abs(impVol) > 1e-15) {
                DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
                count--;
              }
            } catch (Exception e) {
              // System.out.println("can't find vol for strike: " + prices.getSpaceValue(j) + " and expiry " + prices.getTimeValue(i) + " . Not added to data set");
            }
          }
        }
      }
    }
    //    if (count > 0) {
    //      System.err.println(count + " out of " + xNodes * tNodes + " data points removed");
    //    }
    return out;
  }

  public static void printSurface(String name, PDEFullResults1D res) {
    int tNodes = res.getNumberTimeNodes();
    int xNodes = res.getNumberSpaceNodes();

    System.out.println(name);
    for (int i = 0; i < xNodes; i++) {
      double k = res.getSpaceValue(i);
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      double t = res.getTimeValue(j);
      System.out.print(t);
      for (int i = 0; i < xNodes; i++) {
        System.out.print("\t" + res.getFunctionValue(i, j));
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

  public static void printSurface(String name, Surface<Double, Double, Double> surface, double xMin, double xMax, double yMin, double yMax) {
    printSurface(name, surface, xMin, xMax, yMin, yMax, 100, 100);
  }

  public static void printSurface(String name, Surface<Double, Double, Double> surface, double xMin, double xMax, double yMin, double yMax, int xSteps, int ySteps) {

    Validate.isTrue(xMax > xMin, "need xMax > xMin");
    Validate.isTrue(yMax > yMin, "need yMax > yMin");
    Validate.isTrue(xSteps > 0, "need xSteps > 0");
    Validate.isTrue(ySteps > 0, "need ySteps > 0");

    System.out.println(name);
    for (int i = 0; i <= ySteps; i++) {
      double y = yMin + ((yMax - yMin) * i) / ySteps;
      System.out.print("\t" + y);
    }
    System.out.print("\n");

    for (int j = 0; j <= xSteps; j++) {
      double t = xMin + ((xMax - xMin) * j) / xSteps;
      System.out.print(t);
      for (int i = 0; i <= ySteps; i++) {
        double k = yMin + ((yMax - yMin) * i) / ySteps;
        System.out.print("\t" + surface.getZValue(t, k));
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

  public static void printSurfaceInterpolate(String name, PDEFullResults1D res) {

    Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle = getInterpolatorDataBundle(res);
    double tMin = res.getTimeValue(0);
    double tMax = res.getTimeValue(res.getNumberTimeNodes() - 1);
    double kMin = res.getSpaceValue(0);
    double kMax = res.getSpaceValue(res.getNumberSpaceNodes() - 1);
    printSurface(name, dataBundle, tMin, tMax, kMin, kMax, 100, 100);
  }

  public static void printSurface(String name, Map<Double, Interpolator1DDoubleQuadraticDataBundle> dataBundle, double tMin, double tMax, double kMin, double kMax,
      int xSteps, int ySteps) {

    System.out.println(name);
    for (int i = 0; i <= ySteps; i++) {
      double k = kMin + ((kMax - kMin) * i) / ySteps;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j <= xSteps; j++) {
      double t = tMin + ((tMax - tMin) * j) / xSteps;
      System.out.print(t);
      for (int i = 0; i <= ySteps; i++) {
        double k = kMin + ((kMax - kMin) * i) / ySteps;
        DoublesPair tk = new DoublesPair(t, k);

        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, tk));
      }
      System.out.print("\n");
    }
  }

}

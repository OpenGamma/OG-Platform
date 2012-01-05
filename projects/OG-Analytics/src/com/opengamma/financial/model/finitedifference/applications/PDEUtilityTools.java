/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PDEUtilityTools {

  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  public static Map<Double, Interpolator1DDataBundle> getInterpolatorDataBundle(final PDEFullResults1D res) {
    final int tNodes = res.getNumberTimeNodes();
    final int xNodes = res.getNumberSpaceNodes();

    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = res.getTimeValue(i);

      for (int j = 0; j < xNodes; j++) {
        final double k = res.getSpaceValue(j);
        final DoublesPair tk = new DoublesPair(t, k);
        out.put(tk, res.getFunctionValue(j, i));
      }
    }

    final Map<Double, Interpolator1DDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(out);
    return dataBundle;
  }

  /**
   * Takes the results from a forward PDE solve - grid of option prices by maturity and strike and returns a map between a DoublesPair (i.e. maturity and strike) and
   * the Black implied volatility
   * @param forwardCurve The forward
   * @param prices The forward (i.e. not discounted) option prices
   * @param minT Data before this time is ignored (not included in map)
   * @param maxT Data after this time is ignored (not included in map)
   * @param minK Strikes less than this are ignored (not included in map)
   * @param maxK Strikes greater than this are ignored (not included in map)
   * @param isCall true if call
   * @return The price to implied volatility map
   */
  public static Map<DoublesPair, Double> priceToImpliedVol(final ForwardCurve forwardCurve, final PDEFullResults1D prices, final double minT, final double maxT, final double minK, final double maxK,
      boolean isCall) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);
    int count = tNodes * xNodes;

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      final double forward = forwardCurve.getForward(t);
      if (t >= minT && t <= maxT) {
        //  final BlackFunctionData data = new BlackFunctionData(forward.getForward(t), forward.getSpot() / forward.getForward(t), 0);
        for (int j = 0; j < xNodes; j++) {
          final double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            final double price = prices.getFunctionValue(j, i);

            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(price, forward, k, t, isCall);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
                count--;
              }
            } catch (final Exception e) {
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

  /**
   * Takes the results from a forward PDE solve - grid of option prices by maturity and strike and returns a map between a DoublesPair (i.e. maturity and strike) and
   * the Black implied volatility
   * @param forwardCurve The forward
   * @param yieldCurve The discount curve
   * @param prices The option prices
   * @param minT Data before this time is ignored (not included in map)
   * @param maxT Data after this time is ignored (not included in map)
   * @param minK Strikes less than this are ignored (not included in map)
   * @param maxK Strikes greater than this are ignored (not included in map)
   * @return The price to implied volatility map
   */
  public static Map<DoublesPair, Double> priceToImpliedVol(final ForwardCurve forwardCurve,
      final YieldAndDiscountCurve discountCurve, final PDEFullResults1D prices, final double minT, final double maxT, final double minK, final double maxK) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);
    int count = tNodes * xNodes;

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      final double forward = forwardCurve.getForward(t);
      final double df = discountCurve.getDiscountFactor(t);
      if (t >= minT && t <= maxT) {
        for (int j = 0; j < xNodes; j++) {
          final double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            final double forwardPrice = prices.getFunctionValue(j, i) / df;
            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(forwardPrice, forward, k, t, true);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
                count--;
              }
            } catch (final Exception e) {
            }
          }
        }
      }
    }

    return out;
  }

  public static void printSurface(final String name, final PDEFullResults1D res) {
    PrintStream out = System.out;
    printSurface(name, res, out);
  }

  public static void printSurface(final String name, final PDEFullResults1D res, final PrintStream out) {
    final int tNodes = res.getNumberTimeNodes();
    final int xNodes = res.getNumberSpaceNodes();

    out.println(name);
    for (int i = 0; i < xNodes; i++) {
      final double k = res.getSpaceValue(i);
      out.print("\t" + k);
    }
    out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      final double t = res.getTimeValue(j);
      out.print(t);
      for (int i = 0; i < xNodes; i++) {
        out.print("\t" + res.getFunctionValue(i, j));
      }
      out.print("\n");
    }
    out.print("\n");
  }

  public static void printSurface(final String name, final Surface<Double, Double, Double> surface, final double xMin, final double xMax, final double yMin, final double yMax) {
    printSurface(name, surface, xMin, xMax, yMin, yMax, 100, 100);
  }

  public static void printSurface(final String name, final Surface<Double, Double, Double> surface, final double xMin, final double xMax, final double yMin, final double yMax, final int xSteps,
      final int ySteps) {

    Validate.isTrue(xMax > xMin, "need xMax > xMin");
    Validate.isTrue(yMax > yMin, "need yMax > yMin");
    Validate.isTrue(xSteps > 0, "need xSteps > 0");
    Validate.isTrue(ySteps > 0, "need ySteps > 0");

    String result = "";
    result += name;
    result += "\n";
    //System.out.println(name);
    for (int i = 0; i <= ySteps; i++) {
      final double y = yMin + ((yMax - yMin) * i) / ySteps;
      result += ("\t" + y);
    }
    result += "\n";

    for (int j = 0; j <= xSteps; j++) {
      final double t = xMin + ((xMax - xMin) * j) / xSteps;
      result += t;
      for (int i = 0; i <= ySteps; i++) {
        final double k = yMin + ((yMax - yMin) * i) / ySteps;
        result += "\t" + surface.getZValue(t, k);
      }
      result += "\n";
    }
    result += "\n";
    System.out.println(result);
  }

  public static void printSurfaceInterpolate(final String name, final PDEFullResults1D res) {

    final Map<Double, Interpolator1DDataBundle> dataBundle = getInterpolatorDataBundle(res);
    final double tMin = res.getTimeValue(0);
    final double tMax = res.getTimeValue(res.getNumberTimeNodes() - 1);
    final double kMin = res.getSpaceValue(0);
    final double kMax = res.getSpaceValue(res.getNumberSpaceNodes() - 1);
    printSurface(name, dataBundle, tMin, tMax, kMin, kMax, 100, 100);
  }

  public static void printSurface(final String name, final Map<Double, Interpolator1DDataBundle> dataBundle, final double tMin, final double tMax, final double kMin, final double kMax,
      final int xSteps, final int ySteps) {

    System.out.println(name);
    for (int i = 0; i <= ySteps; i++) {
      final double k = kMin + ((kMax - kMin) * i) / ySteps;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j <= xSteps; j++) {
      final double t = tMin + ((tMax - tMin) * j) / xSteps;
      System.out.print(t);
      for (int i = 0; i <= ySteps; i++) {
        final double k = kMin + ((kMax - kMin) * i) / ySteps;
        final DoublesPair tk = new DoublesPair(t, k);

        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, tk));
      }
      System.out.print("\n");
    }
  }

}

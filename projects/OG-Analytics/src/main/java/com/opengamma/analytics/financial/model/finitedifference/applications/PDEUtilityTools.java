/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PDEUtilityTools {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  public static Map<Double, Interpolator1DDataBundle> getInterpolatorDataBundle(final PDEFullResults1D res) {
    final int tNodes = res.getNumberTimeNodes();
    final int xNodes = res.getNumberSpaceNodes();

    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = res.getTimeValue(i);

      for (int j = 0; j < xNodes; j++) {
        final double k = res.getSpaceValue(j);
        final DoublesPair tk = DoublesPair.of(t, k);
        out.put(tk, res.getFunctionValue(j, i));
      }
    }

    final Map<Double, Interpolator1DDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(out);
    return dataBundle;
  }

  /**
   * Take the terminal result for a forward PDE (i.e. forward option prices) and returns a map between strikes and implied volatilities
   * @param forwardCurve The forward curve
   * @param expiry The expiry of this option strip
   * @param prices The results from the PDE solver
   * @param minK The minimum strike to return
   * @param maxK The maximum strike to return
   * @param isCall true for call
   * @return A map between strikes and implied volatilities
   */
  public static Map<Double, Double> priceToImpliedVol(final ForwardCurve forwardCurve, final double expiry, final PDETerminalResults1D prices, final double minK, final double maxK,
      final boolean isCall) {
    final int n = prices.getNumberSpaceNodes();
    final Map<Double, Double> out = new HashMap<>(n);
    for (int j = 0; j < n; j++) {
      final double k = prices.getSpaceValue(j);
      if (k >= minK && k <= maxK) {
        final double price = prices.getFunctionValue(j);
        try {
          final double impVol = BlackFormulaRepository.impliedVolatility(price, 1.0, k, expiry, isCall);
          if (Math.abs(impVol) > 1e-15) {
            out.put(k, impVol);
          }
        } catch (final Exception e) {
        }
      }
    }
    return out;
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
      final boolean isCall) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      final double forward = forwardCurve.getForward(t);
      if (t >= minT && t <= maxT) {
        for (int j = 0; j < xNodes; j++) {
          final double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            final double price = prices.getFunctionValue(j, i);

            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(price, forward, k, t, isCall);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = DoublesPair.of(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
              }
            } catch (final Exception e) {
            }
          }
        }
      }
    }
    return out;
  }

  public static Map<DoublesPair, Double> modifiedPriceToImpliedVol(final PDEFullResults1D prices, final double minT, final double maxT,
      final double minM, final double maxM, final boolean isCall) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      if (t >= minT && t <= maxT) {
        for (int j = 0; j < xNodes; j++) {
          final double m = prices.getSpaceValue(j);
          if (m >= minM && m <= maxM) {
            final double price = prices.getFunctionValue(j, i);

            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(price, 1.0, m, t, isCall);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = DoublesPair.of(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
              }
            } catch (final Exception e) {
            }
          }
        }
      }
    }

    return out;
  }

  /**
   * Takes the results from a forward PDE solve - grid of option prices by maturity and strike and returns a map between a DoublesPair (i.e. maturity and strike) and
   * the Black implied volatility
   * @param forwardCurve The forward
   * @param discountCurve The discount curve
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
    final Map<DoublesPair, Double> out = new HashMap<>(n);

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
                final DoublesPair pair = DoublesPair.of(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
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
    final PrintStream out = System.out;
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

    final StringBuffer result = new StringBuffer(name);
    result.append("\n");
    for (int i = 0; i <= ySteps; i++) {
      final double y = yMin + ((yMax - yMin) * i) / ySteps;
      result.append("\t");
      result.append(y);
    }
    result.append("\n");

    for (int j = 0; j <= xSteps; j++) {
      final double t = xMin + ((xMax - xMin) * j) / xSteps;
      result.append(t);
      for (int i = 0; i <= ySteps; i++) {
        final double k = yMin + ((yMax - yMin) * i) / ySteps;
        result.append("\t");
        result.append(surface.getZValue(t, k));
      }
      result.append("\n");
    }
    result.append("\n");
    System.out.println(result);
  }

  /**
   * This form takes vectors of x (typically expiry) and y (typically strike)
   *
   * @param name The name
   * @param surface The surface
   * @param x The x values
   * @param y The y values
   */

  /**
   * Prints out the values of the function f(x,y) where x takes the values x_1 to x_N and y takes the values y_1 to y_M, along with the x and y values, with x as the top row and
   * y as the left column. This format can be used by the Excel 3D surface plotter.
   * @param name Optional name for the output
   * @param data The data
   * @param x x-values
   * @param y y-values
   * @param out output
   */
  public static void printSurface(final String name, final double[][] data, final double[] x, final double[] y, final PrintStream out) {
    ArgumentChecker.notNull(data, "null data");
    ArgumentChecker.notNull(x, "null x");
    ArgumentChecker.notNull(y, "null y");
    ArgumentChecker.notNull(out, "null printStream");
    final int n = data.length;
    final int m = data[0].length;
    ArgumentChecker.isTrue(n == y.length, "Size of data is {} {}, but length of y is {}", n, m, y.length);
    ArgumentChecker.isTrue(m == x.length, "Size of data is {} {}, but length of x is {}", n, m, x.length);

    out.println(name);
    for (int j = 0; j < m; j++) {
      out.print("\t" + x[j]);
    }
    out.print("\n");
    for (int i = 0; i < n; i++) {
      out.print(y[i]);
      for (int j = 0; j < m; j++) {
        out.print("\t" + data[i][j]);
      }
      out.print("\n");
    }
    out.print("\n");
  }

  /**
   * This form takes vectors of x (typically expiry) and y (typically strike)
   * @param name The name of the surface
   * @param surface The surface
   * @param x Sample x values
   * @param y Sample y values
   */
  public static void printSurface(final String name, final Surface<Double, Double, Double> surface, final double[] x, final double[] y) {
    Validate.isTrue(x.length > 0, "The x-array was empty");
    Validate.isTrue(y.length > 0, "The y-array was empty");

    final StringBuffer result = new StringBuffer(name);
    result.append("\n");
    for (final double element : y) {
      result.append("\t");
      result.append(element);
    }
    result.append("\n");
    for (final double t : x) {
      result.append(t);
      for (final double k : y) {
        result.append("\t");
        result.append(surface.getZValue(t, k));
      }
      result.append("\n");
    }
    result.append("\n");
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
        final DoublesPair tk = DoublesPair.of(t, k);

        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, tk));
      }
      System.out.print("\n");
    }
  }

}

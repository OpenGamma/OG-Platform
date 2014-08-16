/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Combine the results of different strippings. This is normally used to combine multiple single strike results.  
 */
public class CombinedCapletStrippingResults {

  private final CapletStrippingResult[] _results;

  /**
   * combine the results 
   * @param results indiviual results 
   */
  public CombinedCapletStrippingResults(final CapletStrippingResult[] results) {
    ArgumentChecker.noNulls(results, "results");
    _results = results;
  }

  protected double[] getCapletExpiries() {
    final int nRes = _results.length;
    final double[][] t = new double[nRes][];
    for (int i = 0; i < nRes; i++) {
      t[i] = _results[i].getPricer().getCapletExpiries();
    }
    return toUnique(t);
  }

  protected double[] getStrikes() {
    final int nRes = _results.length;
    final double[][] k = new double[nRes][];
    for (int i = 0; i < nRes; i++) {
      k[i] = _results[i].getPricer().getStrikes();
    }
    return toUnique(k);
  }

  private double[] toUnique(final double[][] from) {
    final int n = from.length;
    final double[] base = from[0];
    List<Double> list = Arrays.asList(ArrayUtils.toObject(base));
    final Set<Double> set = new TreeSet<>(list);

    for (int i = 1; i < n; i++) {
      final double[] temp = from[i];
      if (!Arrays.equals(base, temp)) {
        final int m = temp.length;
        for (int j = 0; j < m; j++) {
          set.add(temp[j]);
        }
      }
    }
    list = new ArrayList<>(set);
    final double[] res = ArrayUtils.toPrimitive(list.toArray(new Double[0]));
    Arrays.sort(res);
    return res;

  }

  /**
   * Dump out the caplet volatility surface n a tab separated format (this allows easy pasting
   * into Excel). We store the (calibrated) caplet volatilities, rather than a continuous surface, so we create 
   * a surface using a 2D linear grid interpolator ({@link GridInterpolator2D}. 
   * @param out an output stream
   * @param nExpPoints number of sample points in the expiry direction 
   * @param nStrikePoints number of sample points in the strike direction 
   */
  public void printSurface(final PrintStream out, final int nExpPoints, final int nStrikePoints) {
    final double[] t = getCapletExpiries();
    final double[] k = getStrikes();
    final double timeRange = t[t.length - 1] - t[0];
    final double strikeRange = k[k.length - 1] - k[0];

    final int nRes = _results.length;
    final Map<DoublesPair, Double> map = new HashMap<>();
    for (int i = 0; i < nRes; i++) {
      final CapletStrippingResult result = _results[i];
      final DoublesPair[] expStrikes = result.getPricer().getExpiryStrikeArray();
      final DoubleMatrix1D vols = result.getCapletVols();
      final int n = expStrikes.length;
      for (int j = 0; j < n; j++) {
        map.put(expStrikes[j], vols.getEntry(j));
      }
    }

    final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final GridInterpolator2D interpolator2D = new GridInterpolator2D(interpolator, interpolator);
    final Map<Double, Interpolator1DDataBundle> db = interpolator2D.getDataBundle(map);

    final double[] times = new double[nExpPoints];
    final double[] strikes = new double[nStrikePoints];
    for (int i = 0; i < nStrikePoints; i++) {
      strikes[i] = k[0] + strikeRange * i / (nStrikePoints - 1.0);
    }
    out.println();
    for (int j = 0; j < nExpPoints; j++) {
      times[j] = t[0] + timeRange * j / (nExpPoints - 1.0);
      out.print("\t" + times[j]);
    }

    for (int i = 0; i < nStrikePoints; i++) {
      out.print("\n" + strikes[i]);
      for (int j = 0; j < nExpPoints; j++) {
        final Double vol = interpolator2D.interpolate(db, DoublesPair.of(times[j], strikes[i]));
        out.print("\t" + vol);
      }
    }
    out.println();
  }

}

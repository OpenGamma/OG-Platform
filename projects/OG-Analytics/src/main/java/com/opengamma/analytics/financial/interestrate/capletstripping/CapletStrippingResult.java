/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Holds the results of performing caplet stripping on a set of caps (on the same Ibor index)
 */
public abstract class CapletStrippingResult {

  private final DoubleMatrix1D _fitParms;
  private final DiscreteVolatilityFunction _func;
  private final MultiCapFloorPricer _pricer;

  /**
   * set up the results 
   * @param fitParms The calibrated model parameters 
   * @param func the function that maps model parameters into caplet volatilities 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors) used in the calibrate
   */
  public CapletStrippingResult(DoubleMatrix1D fitParms, DiscreteVolatilityFunction func, MultiCapFloorPricer pricer) {
    ArgumentChecker.notNull(fitParms, "fitParms");
    ArgumentChecker.notNull(func, "func");
    ArgumentChecker.notNull(pricer, "pricer");

    _fitParms = fitParms;
    _func = func;
    _pricer = pricer;
  }

  /**
   * This will be zero for root-finding methods. For least-squares methods it is a (weighted) sum of squares between
   * the market and (calibrated) model values  
   * @return The chi-squared 
   */
  public abstract double getChiSq();

  /**
   * The calibrated model parameters 
   * @return the fit parameters 
   */
  public DoubleMatrix1D getFitParameters() {
    return _fitParms;
  }

  /**
   * The calibrated caplet volatilities 
   * @return the caplet volatilities 
   */
  public DoubleMatrix1D getCapletVols() {
    return _func.evaluate(_fitParms);
  }

  /**
   * The calibrated cap prices 
   * @return cap prices 
   */
  public double[] getModelCapPrices() {
    return _pricer.priceFromCapletVols(getCapletVols().getData());
  }

  /**
   * The calibrated cap volatilities 
   * @return cap vols 
   */
  public double[] getModelCapVols() {
    return _pricer.impliedVols(getModelCapPrices());
  }

  /**
   * get the pricer used in the calibration 
   * @return the pricer 
   */
  MultiCapFloorPricer getPricer() {
    return _pricer;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Caplet Stripping Results\nchi2:\t");
    builder.append(getChiSq());
    builder.append("\nFit Parameters:");
    toTabSeparated(builder, getFitParameters());
    builder.append("\nCap Volatilities:");
    toTabSeparated(builder, getModelCapVols());
    builder.append("\nCaplet Volatilities:");
    toTabSeparated(builder, getCapletVols());
    builder.append("\n\n");
    return builder.toString();

  }

  /**
   * Dump out the caplet volatilities (order by strike then expiry) in a tab separated format (this allows easy pasting
   * into Excel) 
   * @param out an output stream 
   */
  public void printCapletVols(PrintStream out) {
    ArgumentChecker.notNull(out, "out");
    DoublesPair[] expStrikes = _pricer.getExpiryStrikeArray();
    DoubleMatrix1D vols = getCapletVols();
    int n = expStrikes.length;
    out.println("List of calibrated caplet volatilities");
    out.println("Expiry\tStrike\tVolatility");
    for (int i = 0; i < n; i++) {
      out.println(expStrikes[i].first + "\t" + expStrikes[i].second + "\t" + vols.getEntry(i));
    }
    out.println();
  }

  /**
   * Dump out the caplet volatility surface n a tab separated format (this allows easy pasting
   * into Excel). We store the (calibrated) caplet volatilities, rather than a continuous surface, so we create 
   * a surface using a 2D linear grid interpolator ({@link GridInterpolator2D}. 
   * @param out an output stream
   * @param nExpPoints number of sample points in the expiry direction 
   * @param nStrikePoints number of sample points in the strike direction 
   */
  public void printSurface(PrintStream out, int nExpPoints, int nStrikePoints) {
    ArgumentChecker.notNull(out, "out");
    ArgumentChecker.isTrue(nExpPoints > 1, "need at least 2 expiry points");
    ArgumentChecker.isTrue(nExpPoints > 2, "need at least 2 strike points");
    double[] t = _pricer.getCapletExpiries();
    double[] k = _pricer.getStrikes();
    double timeRange = t[t.length - 1] - t[0];
    double strikeRange = k[k.length - 1] - k[0];

    DoublesPair[] expStrikes = _pricer.getExpiryStrikeArray();
    DoubleMatrix1D vols = getCapletVols();
    int n = expStrikes.length;
    Map<DoublesPair, Double> map = new HashMap<>(n);

    for (int i = 0; i < n; i++) {
      map.put(expStrikes[i], vols.getEntry(i));
    }
    CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(interpolator, interpolator);
    Map<Double, Interpolator1DDataBundle> db = interpolator2D.getDataBundle(map);

    double[] times = new double[nExpPoints];
    double[] strikes = new double[nStrikePoints];
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
        Double vol = interpolator2D.interpolate(db, DoublesPair.of(times[j], strikes[i]));
        out.print("\t" + vol);
      }
    }
    out.println();
  }

  private void toTabSeparated(StringBuilder builder, DoubleMatrix1D data) {
    toTabSeparated(builder, data.getData());
  }

  private void toTabSeparated(StringBuilder builder, double[] data) {
    int n = data.length;
    for (int i = 0; i < n; i++) {
      builder.append("\t");
      builder.append(data[i]);
    }
  }

}

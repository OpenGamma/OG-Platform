/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStrippingResultLeastSquare implements CapletStrippingResult {

  private final LeastSquareResults _results;
  private final DiscreteVolatilityFunction _func;
  private final MultiCapFloorPricer _pricer;

  public CapletStrippingResultLeastSquare(final LeastSquareResults results, final DiscreteVolatilityFunction func, final MultiCapFloorPricer pricer) {
    ArgumentChecker.notNull(results, "results");
    ArgumentChecker.notNull(func, "func");
    ArgumentChecker.notNull(pricer, "pricer");

    _results = results;
    _func = func;
    _pricer = pricer;
  }

  @Override
  public double getChiSq() {
    return _results.getChiSq();
  }

  @Override
  public DoubleMatrix1D getFitParameters() {
    return _results.getFitParameters();
  }

  @Override
  public double[] getModelCapPrices() {
    return _pricer.priceFromCapletVols(getCapletVols().getData());
  }

  @Override
  public double[] getModelCapVols() {
    return _pricer.impliedVols(getModelCapPrices());
  }

  @Override
  public DoubleMatrix1D getCapletVols() {
    return _func.evaluate(getFitParameters());
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
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

  //Separated 
  private void toTabSeparated(final StringBuilder builder, final DoubleMatrix1D data) {
    toTabSeparated(builder, data.getData());
  }

  private void toTabSeparated(final StringBuilder builder, final double[] data) {
    final int n = data.length;
    for (int i = 0; i < n; i++) {
      builder.append("\t");
      builder.append(data[i]);
    }
  }

}

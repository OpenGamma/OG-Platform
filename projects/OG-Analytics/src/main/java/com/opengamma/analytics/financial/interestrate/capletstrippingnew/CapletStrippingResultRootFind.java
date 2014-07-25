/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStrippingResultRootFind implements CapletStrippingResult {

  private final DoubleMatrix1D _fitParms;
  private final DiscreteVolatilityFunction _func;
  private final MultiCapFloorPricer _pricer;

  public CapletStrippingResultRootFind(final DoubleMatrix1D fitParms, final DiscreteVolatilityFunction func, final MultiCapFloorPricer pricer) {
    ArgumentChecker.notNull(fitParms, "fitParms");
    ArgumentChecker.notNull(func, "func");
    ArgumentChecker.notNull(pricer, "pricer");

    _fitParms = fitParms;
    _func = func;
    _pricer = pricer;
  }

  @Override
  public double getChiSq() {
    return 0;
  }

  @Override
  public DoubleMatrix1D getFitParameters() {
    return _fitParms;
  }

  @Override
  public DoubleMatrix1D getCapletVols() {
    return _func.evaluate(_fitParms);
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

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for Hull-White parameters provider for one currency.
 */
public class MulticurveForwardPointsProvider implements MulticurveForwardPointsProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The forward points curve.
   */
  private final DoublesCurve _forwardPoints;
  /**
   * The currency pair for which the points are valid.
   */
  private final Pair<Currency, Currency> _ccyPair;

  /**
   * Constructor.
   * @param multicurves The multi-curve provider.
   * @param forwardPoints The forward points curve.
   * @param ccyPair The currency pair for which the points are valid.
   */
  public MulticurveForwardPointsProvider(final MulticurveProviderInterface multicurves, final DoublesCurve forwardPoints, final Pair<Currency, Currency> ccyPair) {
    _multicurveProvider = multicurves;
    _forwardPoints = forwardPoints;
    _ccyPair = ccyPair;
  }

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  public MulticurveForwardPointsProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new MulticurveForwardPointsProvider(multicurveProvider, _forwardPoints, _ccyPair);
  }

  /**
   * Returns the forward points curve.
   * @return The curve.
   */
  @Override
  public DoublesCurve getForwardPointsCurve() {
    return _forwardPoints;
  }

  /**
   * Returns the currency pair for which the points are valid.
   * @return the ccyPair
   */
  @Override
  public Pair<Currency, Currency> getCurrencyPair() {
    return _ccyPair;
  }

  /**
   * Returns the MulticurveProvider from which the HullWhiteOneFactorProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

}

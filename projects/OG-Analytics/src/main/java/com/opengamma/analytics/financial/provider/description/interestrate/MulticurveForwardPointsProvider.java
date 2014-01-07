/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for a curve provider for FX instruments where the market data is quoted
 * as forward points.
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
   * @param multicurves The multi-curve provider, not null
   * @param forwardPoints The forward points curve, not null
   * @param ccyPair The currency pair for which the points are valid, not null
   */
  public MulticurveForwardPointsProvider(final MulticurveProviderInterface multicurves, final DoublesCurve forwardPoints, final Pair<Currency, Currency> ccyPair) {
    ArgumentChecker.notNull(multicurves, "multicurves");
    ArgumentChecker.notNull(forwardPoints, "forwardPoints");
    ArgumentChecker.notNull(ccyPair, "ccyPair");
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
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
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

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurveProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ccyPair.hashCode();
    result = prime * result + _forwardPoints.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MulticurveForwardPointsProvider)) {
      return false;
    }
    final MulticurveForwardPointsProvider other = (MulticurveForwardPointsProvider) obj;
    if (!ObjectUtils.equals(_ccyPair, other._ccyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardPoints, other._forwardPoints)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    return true;
  }

}

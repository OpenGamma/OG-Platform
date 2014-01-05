/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a provider with discounting, forward and G2++ parameters.
 */
public class G2ppProvider implements G2ppProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The G2++ one factor model parameters.
   */
  private final G2ppPiecewiseConstantParameters _parameters;
  /**
   * The currency for which the G2++ parameters are valid (G2++ on the discounting curve).
   */
  private final Currency _ccyG2pp;

  /**
   * Constructor from exiting multicurveProvider and G2++ parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param multicurves The multi-curves provider, not null
   * @param parameters The G2++ parameters, not null
   * @param ccyG2pp The currency for which the G2++ arameters are valid (G2++ on the discounting curve), not null
   */
  public G2ppProvider(final MulticurveProviderInterface multicurves, final G2ppPiecewiseConstantParameters parameters, final Currency ccyG2pp) {
    ArgumentChecker.notNull(multicurves, "multicurves");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(ccyG2pp, "ccyG2pp");
    _multicurveProvider = multicurves;
    _parameters = parameters;
    _ccyG2pp = ccyG2pp;
  }

  @Override
  public G2ppProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new G2ppProvider(multicurveProvider, _parameters, _ccyG2pp);
  }

  @Override
  public G2ppPiecewiseConstantParameters getG2ppParameters() {
    return _parameters;
  }

  @Override
  public Currency getG2ppCurrency() {
    return _ccyG2pp;
  }

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
    result = prime * result + _ccyG2pp.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof G2ppProvider)) {
      return false;
    }
    final G2ppProvider other = (G2ppProvider) obj;
    if (!ObjectUtils.equals(_ccyG2pp, other._ccyG2pp)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}

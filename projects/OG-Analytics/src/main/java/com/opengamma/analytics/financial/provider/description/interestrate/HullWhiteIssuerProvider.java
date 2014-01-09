/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a provider with discounting, forward, credit curves and Hull-White parameters on one issuer curve.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class HullWhiteIssuerProvider implements HullWhiteIssuerProviderInterface {

  /**
   * The issuer provider.
   */
  private final IssuerProviderInterface _issuerProvider;
  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;

  /**
   * Constructor from exiting multicurveProvider and Hull-White parameters. The given provider and parameters are used for the new provider (the same maps are used, not copied).
   * @param issuer The issuer provider, not null
   * @param parameters The Hull-White one factor parameters, not null
   */
  public HullWhiteIssuerProvider(final IssuerProviderInterface issuer, final HullWhiteOneFactorPiecewiseConstantParameters parameters) {
    ArgumentChecker.notNull(issuer, "issuer");
    ArgumentChecker.notNull(parameters, "parameters");
    _issuerProvider = issuer;
    _parameters = parameters;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _issuerProvider;
  }

  @Override
  public HullWhiteIssuerProviderInterface copy() {
    final IssuerProviderInterface issuer = _issuerProvider.copy();
    return new HullWhiteIssuerProvider(issuer, getHullWhiteParameters());
  }

  @Override
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameters() {
    return _parameters;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _issuerProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _issuerProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _issuerProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _issuerProvider.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HullWhiteIssuerProvider)) {
      return false;
    }
    final HullWhiteIssuerProvider other = (HullWhiteIssuerProvider) obj;
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}

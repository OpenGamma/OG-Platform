/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a issuer provider created from a issuer provider where the discounting curve for one issuer is
 * shifted (decorated) by a a parallel spread (in the zero-coupon continuously compounded rate).
 */
public class IssuerProviderIssuerDecoratedSpread implements IssuerProviderInterface {

  /**
   * The underlying Issuer provider on which the multi-curves provider is based.
   */
  private final IssuerProviderInterface _issuerProvider;
  /**
   * The issuer/currency pair to be shifted.
   */
  private final LegalEntity _issuer;
  /**
   * The spread (shift).
   */
  private final double _spread;

  /**
   * Constructor.
   * @param issuerProvider The underlying Issuer provider on which the multi-curves provider is based.
   * @param issuerCcy The issuer/provider pair.
   * @param spread The spread.
   */
  public IssuerProviderIssuerDecoratedSpread(final IssuerProviderInterface issuerProvider, final LegalEntity issuer, final double spread) {
    _issuerProvider = issuerProvider;
    _issuer = issuer;
    _spread = spread;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderIssuerDecoratedSpread getIssuerProvider() {
    return this;
  }

  @Override
  public IssuerProviderInterface copy() {
    throw new UnsupportedOperationException("Copy not supported for decorated providers");
  }

  @Override
  public double getDiscountFactor(final LegalEntity issuer, final Double time) {
    final double df = _issuerProvider.getDiscountFactor(issuer, time);
    if (issuer.equals(_issuer)) {
      return df * Math.exp(-time * _spread);
    }
    return df;
  }

  @Override
  public String getName(final Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy) {
    return _issuerProvider.getName(issuerCcy);
  }

  @Override
  public String getName(final LegalEntity issuerCcy) {
    return _issuerProvider.getName(issuerCcy);
  }

  //  @Override
//  public String getName(final Pair<String, Currency> issuerCcy) {
//    return _issuerProvider.getName(issuerCcy);
//  }

  @Override
  public Set<String> getAllNames() {
    return _issuerProvider.getAllNames();
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    throw new UnsupportedOperationException("parameterSensitivity not supported for decorated providers");
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _issuerProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    return _issuerProvider.getNumberOfParameters(name);
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _issuerProvider.getUnderlyingCurvesNames(name);
  }

  @Override
  public Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuerProvider.getIssuers();
  }

//  @Override
//  public Set<Pair<String, Currency>> getIssuersCurrencies() {
//    return _issuerProvider.getIssuersCurrencies();
//  }

}

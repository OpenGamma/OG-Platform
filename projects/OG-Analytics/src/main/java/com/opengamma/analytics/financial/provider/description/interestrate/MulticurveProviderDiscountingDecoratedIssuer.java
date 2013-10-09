/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a multi-curves provider created from a issuer provider where the discounting curve
 * for one issuer replace (decorate) the discounting curve for one currency.
 */
public class MulticurveProviderDiscountingDecoratedIssuer implements MulticurveProviderInterface {

  /**
   * The underlying Issuer provider on which the multi-curves provider is based.
   */
  private final IssuerProviderInterface _issuerProvider;
  /**
   * The currency for which the discounting curve will be replaced (decorated).
   */
  private final Currency _decoratedCurrency;
  /**
   * The issuer for which the associated discounting curve will replace the currency discounting curve.
   */
  private final String _decoratingIssuer;
  /**
   * The issuer/currency pair.
   */
  private final Pair<String, Currency> _decoratingIssuerCcy;

  /**
   * Constructor.
   * @param issuerProvider The underlying Issuer provider on which the multi-curves provider is based.
   * @param decoratedCurrency The currency for which the discounting curve will be replaced (decorated).
   * @param decoratingIssuer The issuer for which the associated discounting curve will replace the currency discounting curve.
   */
  public MulticurveProviderDiscountingDecoratedIssuer(final IssuerProviderInterface issuerProvider, final Currency decoratedCurrency, final String decoratingIssuer) {
    _issuerProvider = issuerProvider;
    _decoratedCurrency = decoratedCurrency;
    _decoratingIssuer = decoratingIssuer;
    _decoratingIssuerCcy = new ObjectsPair<>(_decoratingIssuer, _decoratedCurrency);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return this;
  }

  @Override
  public MulticurveProviderInterface copy() {
    return new MulticurveProviderDiscountingDecoratedIssuer(_issuerProvider.copy(), _decoratedCurrency, _decoratingIssuer);
  }

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    if (ccy.equals(_decoratedCurrency)) {
      return _issuerProvider.getDiscountFactor(_decoratingIssuerCcy, time);
    }
    return _issuerProvider.getMulticurveProvider().getDiscountFactor(ccy, time);
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _issuerProvider.getMulticurveProvider().getFxRate(ccy1, ccy2);
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
  public Integer getNumberOfParameters(final String name) {
    return _issuerProvider.getNumberOfParameters(name);
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _issuerProvider.getUnderlyingCurvesNames(name);
  }

  @Override
  public String getName(final Currency ccy) {
    if (ccy.equals(_decoratedCurrency)) {
      return _issuerProvider.getName(_decoratingIssuerCcy);
    }
    return _issuerProvider.getMulticurveProvider().getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _issuerProvider.getMulticurveProvider().getCurrencies();
  }

  @Override
  public String getName(final IborIndex index) {
    return _issuerProvider.getMulticurveProvider().getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _issuerProvider.getMulticurveProvider().getIndexesIbor();
  }

  @Override
  public String getName(final IndexON index) {
    return _issuerProvider.getMulticurveProvider().getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _issuerProvider.getMulticurveProvider().getIndexesON();
  }

  @Override
  public FXMatrix getFxRates() {
    return _issuerProvider.getMulticurveProvider().getFxRates();
  }

  @Override
  public Set<String> getAllNames() {
    return _issuerProvider.getAllNames();
  }

}

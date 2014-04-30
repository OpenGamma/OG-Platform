/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InflationProviderDecoratedIssuer implements InflationProviderInterface {

  /**
   * The underlying Issuer provider on which the multi-curves provider is based.
   */
  private final InflationIssuerProviderInterface _inflationIssuerProvider;
  /**
   * The currency for which the discounting curve will be replaced (decorated).
   */
  private final Currency _decoratedCurrency;
  /**
   * The issuer for which the associated discounting curve will replace the currency discounting curve.
   */
  private final LegalEntity _decoratingIssuer;

  /**
   * Constructor.
   * @param inflationIssuerProvider The underlying inflation issuer provider on which the multi-curves provider is based, not null
   * @param decoratedCurrency The currency for which the discounting curve will be replaced (decorated), not null
   * @param decoratingIssuer The issuer for which the associated discounting curve will replace the currency discounting curve, not null
    */
  public InflationProviderDecoratedIssuer(final InflationIssuerProviderInterface inflationIssuerProvider, final Currency decoratedCurrency, final LegalEntity decoratingIssuer) {
    ArgumentChecker.notNull(inflationIssuerProvider, "inflationIssuerProvider");
    ArgumentChecker.notNull(decoratedCurrency, "decoratedCurrency");
    ArgumentChecker.notNull(decoratingIssuer, "decoratingIssuer");
    _inflationIssuerProvider = inflationIssuerProvider;
    _decoratedCurrency = decoratedCurrency;
    _decoratingIssuer = decoratingIssuer;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _inflationIssuerProvider.getMulticurveProvider();
  }

  @Override
  public InflationProviderInterface copy() {
    throw new UnsupportedOperationException("Copy not supported for decorated providers");
  }

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    if (ccy.equals(_decoratedCurrency)) {
      return _inflationIssuerProvider.getDiscountFactor(_decoratingIssuer, time);
    }
    return _inflationIssuerProvider.getMulticurveProvider().getDiscountFactor(ccy, time);
  }

  @Override
  public Set<String> getAllNames() {
    return _inflationIssuerProvider.getAllNames();
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    throw new UnsupportedOperationException("parameterSensitivity not supported for decorated providers");
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _inflationIssuerProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    return _inflationIssuerProvider.getNumberOfParameters(name);
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _inflationIssuerProvider.getUnderlyingCurvesNames(name);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _inflationIssuerProvider.getAllCurveNames();
  }

  @Override
  public InflationProviderInterface getInflationProvider() {
    return _inflationIssuerProvider.getInflationProvider();
  }

  @Override
  public double[] parameterInflationSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _inflationIssuerProvider.parameterInflationSensitivity(name, pointSensitivity);
  }

  @Override
  public double getPriceIndex(final IndexPrice index, final Double time) {
    return _inflationIssuerProvider.getPriceIndex(index, time);
  }

  @Override
  public String getName(final IndexPrice index) {
    return _inflationIssuerProvider.getName(index);
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _inflationIssuerProvider.getPriceIndexes();
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _inflationIssuerProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _inflationIssuerProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _inflationIssuerProvider.getFxRate(ccy1, ccy2);
  }

  @Override
  public String getName(final Currency ccy) {
    return _inflationIssuerProvider.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _inflationIssuerProvider.getCurrencies();
  }

  @Override
  public String getName(final IborIndex index) {
    return _inflationIssuerProvider.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _inflationIssuerProvider.getIndexesIbor();
  }

  @Override
  public String getName(final IndexON index) {
    return _inflationIssuerProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _inflationIssuerProvider.getIndexesON();
  }

  @Override
  public FXMatrix getFxRates() {
    return _inflationIssuerProvider.getFxRates();
  }

  @Override
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    return _inflationIssuerProvider.getInflationProvider().withDiscountFactor(ccy, replacement);
  }

  @Override
  public InflationProviderInterface withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    return _inflationIssuerProvider.getInflationProvider().withForward(index, replacement);
  }

  @Override
  public InflationProviderInterface withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    return _inflationIssuerProvider.getInflationProvider().withForward(index, replacement);
  }

}

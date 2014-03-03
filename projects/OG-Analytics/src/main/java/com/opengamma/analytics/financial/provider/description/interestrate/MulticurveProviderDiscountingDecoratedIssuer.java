/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

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
  private final LegalEntity _decoratingIssuer;

  /**
   * Constructor.
   * @param issuerProvider The underlying issuer provider on which the multi-curves provider is based, not null
   * @param decoratedCurrency The currency for which the discounting curve will be replaced (decorated), not null
   * @param decoratingIssuer The issuer for which the associated discounting curve will replace the currency discounting curve, not null
   */
  public MulticurveProviderDiscountingDecoratedIssuer(final IssuerProviderInterface issuerProvider, final Currency decoratedCurrency, final LegalEntity decoratingIssuer) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    ArgumentChecker.notNull(decoratedCurrency, "decoratedCurrency");
    ArgumentChecker.notNull(decoratingIssuer, "decoratingIssuer");
    _issuerProvider = issuerProvider;
    _decoratedCurrency = decoratedCurrency;
    _decoratingIssuer = decoratingIssuer;
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
      return _issuerProvider.getDiscountFactor(_decoratingIssuer, time);
    }
    return _issuerProvider.getMulticurveProvider().getDiscountFactor(ccy, time);
  }

  @Override
  public double getInvestmentFactor(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getInvestmentFactor(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getSimplyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getSimplyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime) {
    return _issuerProvider.getMulticurveProvider().getSimplyCompoundForwardRate(index, startTime, endTime);
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getAnnuallyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime) {
    return _issuerProvider.getMulticurveProvider().getAnnuallyCompoundForwardRate(index, startTime, endTime);
  }

  @Override
  public double getInvestmentFactor(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getInvestmentFactor(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getSimplyCompoundForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getSimplyCompoundForwardRate(final IndexON index, final double startTime, final double endTime) {
    return _issuerProvider.getMulticurveProvider().getSimplyCompoundForwardRate(index, startTime, endTime);
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _issuerProvider.getMulticurveProvider().getAnnuallyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IndexON index, final double startTime, final double endTime) {
    return _issuerProvider.getMulticurveProvider().getAnnuallyCompoundForwardRate(index, startTime, endTime);
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
      return _issuerProvider.getName(_decoratingIssuer);
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
    return _issuerProvider.getAllCurveNames();
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _issuerProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _decoratedCurrency.hashCode();
    result = prime * result + _decoratingIssuer.hashCode();
    result = prime * result + _issuerProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MulticurveProviderDiscountingDecoratedIssuer)) {
      return false;
    }
    final MulticurveProviderDiscountingDecoratedIssuer other = (MulticurveProviderDiscountingDecoratedIssuer) obj;
    if (!ObjectUtils.equals(_decoratedCurrency, other._decoratedCurrency)) {
      return false;
    }
    if (!ObjectUtils.equals(_decoratingIssuer, other._decoratingIssuer)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    return true;
  }

}

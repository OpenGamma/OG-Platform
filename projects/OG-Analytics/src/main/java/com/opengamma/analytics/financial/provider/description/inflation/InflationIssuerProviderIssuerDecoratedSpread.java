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
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a issuer provider created from a issuer provider where the discounting curve for one issuer is
 * shifted (decorated) by a a parallel spread (in the zero-coupon continuously compounded rate).
 */
public class InflationIssuerProviderIssuerDecoratedSpread implements InflationIssuerProviderInterface {

  /**
   * The underlying issuer provider on which the multi-curves provider is based.
   */
  private final InflationIssuerProviderInterface _inflationIssuerProvider;
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
   * @param inflationIssuerProvider The underlying inflation issuer provider on which the multi-curves provider is based, not null
   * @param issuer The issuer, not null
   * @param spread The spread
   */
  public InflationIssuerProviderIssuerDecoratedSpread(final InflationIssuerProviderInterface inflationIssuerProvider, final LegalEntity issuer, final double spread) {
    ArgumentChecker.notNull(inflationIssuerProvider, "inflationIssuerProvider");
    ArgumentChecker.notNull(issuer, "issuer");
    _inflationIssuerProvider = inflationIssuerProvider;
    _issuer = issuer;
    _spread = spread;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _inflationIssuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return this.getIssuerProvider();
  }

  @Override
  public InflationIssuerProviderInterface copy() {
    throw new UnsupportedOperationException("Copy not supported for decorated providers");
  }

  @Override
  public double getDiscountFactor(final LegalEntity issuer, final Double time) {
    final double df = _inflationIssuerProvider.getDiscountFactor(issuer, time);
    if (issuer.equals(_issuer)) {
      return df * Math.exp(-time * _spread);
    }
    return df;
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
  public Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _inflationIssuerProvider.getIssuers();
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _inflationIssuerProvider.getAllCurveNames();
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
  public double getDiscountFactor(final Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy, final Double time) {
    return _inflationIssuerProvider.getDiscountFactor(issuerCcy, time);
  }

  @Override
  public InflationProviderInterface getInflationProvider() {
    return _inflationIssuerProvider.getInflationProvider();
  }

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    return _inflationIssuerProvider.getDiscountFactor(ccy, time);
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
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final Pair<Object, LegalEntityFilter<LegalEntity>> replacement) {
    return _inflationIssuerProvider.withDiscountFactor(ccy, replacement);
  }

  @Override
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final LegalEntity replacement) {
    return _inflationIssuerProvider.withDiscountFactor(ccy, replacement);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_inflationIssuerProvider == null) ? 0 : _inflationIssuerProvider.hashCode());
    result = prime * result + ((_issuer == null) ? 0 : _issuer.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InflationIssuerProviderIssuerDecoratedSpread other = (InflationIssuerProviderIssuerDecoratedSpread) obj;
    if (_inflationIssuerProvider == null) {
      if (other._inflationIssuerProvider != null) {
        return false;
      }
    } else if (!_inflationIssuerProvider.equals(other._inflationIssuerProvider)) {
      return false;
    }
    if (_issuer == null) {
      if (other._issuer != null) {
        return false;
      }
    } else if (!_issuer.equals(other._issuer)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}

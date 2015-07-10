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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * An inflation provider modified by a spread. The discount factors for all currencies are multiplied by exp(-s*t)
 * with s the spread and t the time.
 */
public class InflationProviderDecorated implements InflationProviderInterface {

  /**
   * The underlying issuer provider on which the multi-curves provider is based.
   */
  private final InflationProviderInterface _inflationProvider;

  /**
   * The spread (shift).
   */
  private final double _spread;

  /**
   * Constructor.
   * @param inflationProvider The underlying issuer provider on which the multi-curves provider is based, not null
   * @param spread The spread
   */
  public InflationProviderDecorated(final InflationProviderInterface inflationProvider, final double spread) {
    ArgumentChecker.notNull(inflationProvider, "inflationProvider");
    _inflationProvider = inflationProvider;
    _spread = spread;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _inflationProvider.getMulticurveProvider();
  }

  @Override
  public InflationProviderInterface copy() {
    throw new UnsupportedOperationException("Copy not supported for decorated providers");
  }

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    final double df = _inflationProvider.getDiscountFactor(ccy, time);
    return df * Math.exp(-time * _spread);
  }

  @Override
  public Set<String> getAllNames() {
    return _inflationProvider.getAllNames();
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    throw new UnsupportedOperationException("parameterSensitivity not supported for decorated providers");
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _inflationProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    return _inflationProvider.getNumberOfParameters(name);
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return _inflationProvider.getUnderlyingCurvesNames(name);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _inflationProvider.getAllCurveNames();
  }

  @Override
  public InflationProviderInterface getInflationProvider() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double[] parameterInflationSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getPriceIndex(final IndexPrice index, final Double time) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getName(final IndexPrice index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getName(final Currency ccy) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Currency> getCurrencies() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName(final IborIndex index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName(final IndexON index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<IndexON> getIndexesON() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FXMatrix getFxRates() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InflationProviderInterface withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InflationProviderInterface withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_inflationProvider == null) ? 0 : _inflationProvider.hashCode());
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
    final InflationProviderDecorated other = (InflationProviderDecorated) obj;
    if (_inflationProvider == null) {
      if (other._inflationProvider != null) {
        return false;
      }
    } else if (!_inflationProvider.equals(other._inflationProvider)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}

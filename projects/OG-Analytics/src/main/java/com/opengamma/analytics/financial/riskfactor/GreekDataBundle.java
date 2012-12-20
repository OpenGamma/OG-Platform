/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.trade.OptionTradeData;

/**
 * 
 */
public class GreekDataBundle {
  private final GreekResultCollection _greekValues;
  private final Map<UnderlyingType, Double> _underlyingData;
  private final OptionTradeData _tradeData;

  public GreekDataBundle(final GreekResultCollection greekValues, final Map<UnderlyingType, Double> underlyingData, final OptionTradeData tradeData) {
    Validate.notNull(greekValues, "greek result collection");
    if (greekValues.isEmpty()) {
      throw new IllegalArgumentException("Greek result collection was empty");
    }
    Validate.notNull(underlyingData, "underlying data");
    Validate.notEmpty(underlyingData, "underlying data");
    Validate.notNull(tradeData, "trade data");
    _greekValues = greekValues;
    _underlyingData = underlyingData;
    _tradeData = tradeData;
  }

  public OptionTradeData getOptionTradeData() {
    return _tradeData;
  }

  public GreekResultCollection getGreekResults() {
    return _greekValues;
  }

  public Map<UnderlyingType, Double> getUnderlyingData() {
    return _underlyingData;
  }

  public double getUnderlyingDataForType(final UnderlyingType type) {
    if (_underlyingData.containsKey(type)) {
      return _underlyingData.get(type);
    }
    throw new IllegalArgumentException("Underlying data map did not contain a value for " + type);
  }

  public Double getGreekResultForGreek(final Greek greek) {
    if (_greekValues.contains(greek)) {
      return _greekValues.get(greek);
    }
    throw new IllegalArgumentException("Greek result collection did not contain a value for " + greek);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_greekValues == null) ? 0 : _greekValues.hashCode());
    result = prime * result + ((_underlyingData == null) ? 0 : _underlyingData.hashCode());
    result = prime * result + ((_tradeData == null) ? 0 : _tradeData.hashCode());
    return result;
  }

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
    final GreekDataBundle other = (GreekDataBundle) obj;
    return ObjectUtils.equals(_greekValues, other._greekValues) && ObjectUtils.equals(_underlyingData, other._underlyingData) && ObjectUtils.equals(_tradeData, other._tradeData);
  }
}

/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.securities.keys.SecurityKey;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * 
 * @author emcleod
 */
public class StandardVaRWithPriceDataBundle extends StandardVaRDataBundle {
  private static final Logger s_Log = LoggerFactory.getLogger(StandardVaRWithPriceDataBundle.class);
  private final Map<SecurityKey, Double> _riskFactorPrices;

  public StandardVaRWithPriceDataBundle(final Map<SecurityKey, DoubleTimeSeries> riskFactorReturns, final Map<SecurityKey, Double> riskFactorPrices) {
    super(riskFactorReturns);
    if (riskFactorPrices == null)
      throw new IllegalArgumentException("Map of prices was null");
    if (riskFactorPrices.isEmpty())
      throw new IllegalArgumentException("Map of prices was empty");
    for (final SecurityKey key : riskFactorReturns.keySet()) {
      if (!riskFactorPrices.containsKey(key)) {
        s_Log.info("Have a SecurityKey in the return data map (" + key + ") that is not in the price data map");
      }
    }
    for (final SecurityKey key : riskFactorPrices.keySet()) {
      if (!riskFactorReturns.containsKey(key)) {
        s_Log.info("Have a SecurityKey in the price data map (" + key + ") that is not in the return data map");
      }
    }
    _riskFactorPrices = riskFactorPrices;
  }

  public Map<SecurityKey, Double> getAllPriceData() {
    return _riskFactorPrices;
  }

  public Double getPrice(final SecurityKey key) {
    final Map<SecurityKey, Double> map = getAllPriceData();
    if (!map.containsKey(key))
      throw new IllegalArgumentException("Could not find price for " + key + " in data map");
    return map.get(key);
  }

  @Override
  public <T extends StandardVaRDataBundle> StandardVaRWithPriceDataBundle getSubSetBundle(final Set<SecurityKey> keys) {
    final StandardVaRDataBundle returnBundle = super.getSubSetBundle(keys);
    final Map<SecurityKey, Double> map = getAllPriceData();
    final Map<SecurityKey, Double> subSetData = new HashMap<SecurityKey, Double>();
    for (final SecurityKey key : keys) {
      if (!map.containsKey(key)) {
        s_Log.warn("Super set does not contain key " + key);
      } else {
        subSetData.put(key, map.get(key));
      }
    }
    return new StandardVaRWithPriceDataBundle(returnBundle.getAllReturnData(), subSetData);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (getAllPriceData() == null ? 0 : getAllPriceData().hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final StandardVaRWithPriceDataBundle other = (StandardVaRWithPriceDataBundle) obj;
    if (getAllPriceData() == null) {
      if (other.getAllPriceData() != null)
        return false;
    } else if (!getAllPriceData().equals(other.getAllPriceData()))
      return false;
    return true;
  }
}

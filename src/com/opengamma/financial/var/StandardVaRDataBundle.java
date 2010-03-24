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

import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class StandardVaRDataBundle {
  private static final Logger s_Log = LoggerFactory.getLogger(StandardVaRDataBundle.class);
  private final Map<DomainSpecificIdentifiers, DoubleTimeSeries> _riskFactorReturns;

  public StandardVaRDataBundle(final Map<DomainSpecificIdentifiers, DoubleTimeSeries> riskFactorReturns) {
    if (riskFactorReturns == null)
      throw new IllegalArgumentException("Data map was null");
    if (riskFactorReturns.isEmpty())
      throw new IllegalArgumentException("Data map was empty");
    _riskFactorReturns = riskFactorReturns;
  }

  public Map<DomainSpecificIdentifiers, DoubleTimeSeries> getAllReturnData() {
    return _riskFactorReturns;
  }

  public DoubleTimeSeries getReturnSeries(final DomainSpecificIdentifiers key) {
    if (key == null)
      throw new IllegalArgumentException("Key was null");
    final Map<DomainSpecificIdentifiers, DoubleTimeSeries> map = getAllReturnData();
    if (!map.containsKey(key))
      throw new IllegalArgumentException("Could not find return series for " + key + " in data map");
    return map.get(key);
  }

  public <T extends StandardVaRDataBundle> StandardVaRDataBundle getSubSetBundle(final Set<DomainSpecificIdentifiers> keys) {
    if (keys == null)
      throw new IllegalArgumentException("Set of keys was null");
    if (keys.isEmpty())
      throw new IllegalArgumentException("Set of keys was empty");
    final Map<DomainSpecificIdentifiers, DoubleTimeSeries> map = getAllReturnData();
    final Map<DomainSpecificIdentifiers, DoubleTimeSeries> subSetData = new HashMap<DomainSpecificIdentifiers, DoubleTimeSeries>();
    for (final DomainSpecificIdentifiers key : keys) {
      if (!map.containsKey(key)) {
        s_Log.warn("Super set does not contain key " + key);
      } else {
        subSetData.put(key, map.get(key));
      }
    }
    return new StandardVaRDataBundle(subSetData);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (getAllReturnData() == null ? 0 : getAllReturnData().hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final StandardVaRDataBundle other = (StandardVaRDataBundle) obj;
    if (getAllReturnData() == null) {
      if (other.getAllReturnData() != null)
        return false;
    } else if (!getAllReturnData().equals(other.getAllReturnData()))
      return false;
    return true;
  }
}

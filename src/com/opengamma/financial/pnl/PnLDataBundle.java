/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 *
 */
public class PnLDataBundle {
  private final Map<Sensitivity<?>, Map<Object, double[]>> _returns;
  private final Map<Sensitivity<?>, RiskFactorResult> _sensitivities;
  private long[] _times;
  private DateTimeNumericEncoding _encoding;

  public PnLDataBundle(final Map<Sensitivity<?>, RiskFactorResult> sensitivities, final Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> returns) {
    Validate.notNull(sensitivities, "sensitivities");
    ArgumentChecker.notEmpty(sensitivities, "sensitivities");
    Validate.notNull(returns, "returns");
    ArgumentChecker.notEmpty(returns, "returns");
    if (!sensitivities.keySet().equals(returns.keySet())) {
      throw new IllegalArgumentException("Had different set of sensitivities for sensititivity map and returns map");
    }
    _returns = new HashMap<Sensitivity<?>, Map<Object, double[]>>();
    _times = null;
    for (final Entry<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> entry1 : returns.entrySet()) {
      final Map<Object, double[]> m = new HashMap<Object, double[]>();
      final Sensitivity<?> s = entry1.getKey();
      final Map<Object, DoubleTimeSeries<?>> data = entry1.getValue();
      for (final Entry<Object, DoubleTimeSeries<?>> entry2 : data.entrySet()) {
        final Object o = entry2.getKey();
        final DoubleTimeSeries<?> ts = entry2.getValue();
        if (ts == null) {
          throw new IllegalArgumentException("Had a null time series for sensitivity " + s + ", underlying " + o);
        }
        if (ts.isEmpty()) {
          throw new IllegalArgumentException("Had an empty time series for sensitivity " + s + ", underlying " + o);
        }
        if (_times == null) {
          final FastLongDoubleTimeSeries fastTS = ts.toFastLongDoubleTimeSeries();
          _times = fastTS.timesArrayFast();
          _encoding = fastTS.getEncoding();
        }
        m.put(o, ts.toFastLongDoubleTimeSeries().valuesArrayFast());
      }
      _returns.put(s, m);
    }
    _sensitivities = sensitivities;
  }

  public Map<Sensitivity<?>, Map<Object, double[]>> getTimeSeriesReturns() {
    return _returns;
  }

  public Map<Sensitivity<?>, RiskFactorResult> getSensitivities() {
    return _sensitivities;
  }

  public long[] getTimes() {
    return _times;
  }

  public DateTimeNumericEncoding getEncoding() {
    return _encoding;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_encoding == null) ? 0 : _encoding.hashCode());
    if (_returns == null) {
      result = prime * result;
    } else {
      result = prime * result + _returns.keySet().hashCode();
      for (final Map<Object, double[]> data : _returns.values()) {
        result = prime * result + data.keySet().hashCode();
      }
    }
    result = prime * result + ((_sensitivities == null) ? 0 : _sensitivities.hashCode());
    result = prime * result + Arrays.hashCode(_times);
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
    final PnLDataBundle other = (PnLDataBundle) obj;
    if (!ObjectUtils.equals(_encoding, other._encoding)) {
      return false;
    }
    if (_returns == null) {
      if (other._returns != null) {
        return false;
      }
    } else {
      if (!_returns.keySet().equals(other._returns.keySet())) {
        return false;
      }
      for (final Map.Entry<Sensitivity<?>, Map<Object, double[]>> entry : _returns.entrySet()) {
        if (!entry.getValue().keySet().equals(other._returns.get(entry.getKey()).keySet())) {
          return false;
        }
        for (final Object o : entry.getValue().keySet()) {
          if (!Arrays.equals(entry.getValue().get(o), other._returns.get(entry.getKey()).get(o))) {
            return false;
          }
        }
      }
    }
    if (!ObjectUtils.equals(_sensitivities, other._sensitivities)) {
      return false;
    }
    if (!Arrays.equals(_times, other._times)) {
      return false;
    }
    return true;
  }
}

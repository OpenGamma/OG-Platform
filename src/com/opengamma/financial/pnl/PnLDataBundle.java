/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.sensitivity.Sensitivity;
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
    if (sensitivities == null)
      throw new IllegalArgumentException("Sensitivities map was null");
    if (sensitivities.isEmpty())
      throw new IllegalArgumentException("Sensitivities map was empty");
    if (returns == null)
      throw new IllegalArgumentException("Returns map was null");
    if (returns.isEmpty())
      throw new IllegalArgumentException("Returns map was empty");
    if (!sensitivities.keySet().equals(returns.keySet()))
      throw new IllegalArgumentException("Had different set of sensitivities for sensititivity map and returns map");
    _returns = new HashMap<Sensitivity<?>, Map<Object, double[]>>();
    _times = null;
    DoubleTimeSeries<?> ts;
    for (final Sensitivity<?> s : returns.keySet()) {
      final Map<Object, double[]> m = new HashMap<Object, double[]>();
      final Map<Object, DoubleTimeSeries<?>> data = returns.get(s);
      for (final Object o : data.keySet()) {
        ts = data.get(o);
        if (ts == null)
          throw new IllegalArgumentException("Had a null time series for sensitivity " + s + ", underlying " + o);
        if (ts.isEmpty())
          throw new IllegalArgumentException("Had an empty time series for sensitivity " + s + ", underlying " + o);
        if (_times == null) {
          final FastLongDoubleTimeSeries fastTS = data.get(o).toFastLongDoubleTimeSeries();
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

}

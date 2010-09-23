/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.riskfactor.TaylorExpansionMultiplierCalculator;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 *
 */
public class SensitivityPnLCalculator extends Function1D<PnLDataBundle, DoubleTimeSeries<?>> {

  @Override
  public DoubleTimeSeries<?> evaluate(final PnLDataBundle data) {
    Validate.notNull(data, "data");
    final Map<Sensitivity<?>, Map<Object, double[]>> returns = data.getTimeSeriesReturns();
    final Map<Sensitivity<?>, RiskFactorResult> sensitivities = data.getSensitivities();
    final long[] times = data.getTimes();
    final int n = times.length;
    final double[] pnl = new double[n];
    for (final Entry<Sensitivity<?>, RiskFactorResult> entry1 : sensitivities.entrySet()) {
      final Sensitivity<?> s = entry1.getKey();
      final Map<Object, double[]> tsData = returns.get(s);
      final Map<Object, Double> dataForDate = new HashMap<Object, Double>();
      for (int i = 0; i < n; i++) {
        for (final Entry<Object, double[]> entry2 : tsData.entrySet()) {
          dataForDate.put(entry2.getKey(), entry2.getValue()[i]);
        }
        pnl[i] += entry1.getValue().getResult() * TaylorExpansionMultiplierCalculator.getMultiplier(dataForDate, s.getUnderlying());
      }
    }
    return new FastArrayLongDoubleTimeSeries(data.getEncoding(), times, pnl);
  }
}

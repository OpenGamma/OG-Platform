/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.riskfactor.TaylorExpansionMultiplierCalculator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 *
 */
public class SensitivityPnLCalculator implements Function<SensitivityAndReturnDataBundle, DoubleTimeSeries<?>> {

  @Override
  public DoubleTimeSeries<?> evaluate(final SensitivityAndReturnDataBundle... data) {
    Validate.notNull(data, "data");
    DoubleTimeSeries<?> result = null;
    DoubleTimeSeries<?> pnl = null;
    for (final SensitivityAndReturnDataBundle bundle : data) {
      final Underlying underlying = bundle.getUnderlying();
      final Map<UnderlyingType, DoubleTimeSeries<?>> underlyingData = bundle.getUnderlyingReturnTS();
      if (result == null) {
        result = TaylorExpansionMultiplierCalculator.getTimeSeries(underlyingData, underlying);
        result = result.multiply(bundle.getValue());
      } else {
        pnl = TaylorExpansionMultiplierCalculator.getTimeSeries(underlyingData, underlying);
        result = result.add(pnl.multiply(bundle.getValue()));
      }
    }
    return result;
  }
}

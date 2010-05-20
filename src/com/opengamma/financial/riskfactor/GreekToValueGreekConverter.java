/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.MultipleGreekResult;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.sensitivity.MultipleValueGreekResult;
import com.opengamma.financial.sensitivity.SingleValueGreekResult;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekResult;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

public class GreekToValueGreekConverter extends Function1D<GreekDataBundle, Map<ValueGreek, ValueGreekResult<?>>> {

  @Override
  public Map<ValueGreek, ValueGreekResult<?>> evaluate(final GreekDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Greek data bundle was null");
    final GreekResultCollection greeks = data.getGreekResults();
    final Map<ValueGreek, ValueGreekResult<?>> riskFactors = new HashMap<ValueGreek, ValueGreekResult<?>>();
    Map<String, Double> multipleGreekResult;
    Map<String, Double> multipleValueGreekResult;
    Greek key;
    GreekResult<?> value;
    final Map<Object, Double> underlyingData = data.getUnderlyingData();
    for (final Pair<Greek, GreekResult<?>> entry : greeks) {
      key = entry.getKey();
      value = entry.getValue();
      if (entry.getValue() instanceof SingleGreekResult) {
        riskFactors.put(new ValueGreek(key), new SingleValueGreekResult(getValueGreek(key, underlyingData,
            (Double) value.getResult())));
      } else if (entry.getValue() instanceof MultipleGreekResult) {
        multipleGreekResult = ((MultipleGreekResult) value).getResult();
        multipleValueGreekResult = new HashMap<String, Double>();
        for (final Map.Entry<String, Double> e : multipleGreekResult.entrySet()) {
          multipleValueGreekResult.put(e.getKey(), getValueGreek(key, underlyingData, e.getValue()));
        }
        riskFactors.put(new ValueGreek(key), new MultipleValueGreekResult(multipleValueGreekResult));
      } else {
        throw new IllegalArgumentException("Can only handle SingleRiskFactorResult and MultipleRiskFactorResult");
      }
    }
    return riskFactors;
  }

  // TODO handle theta separately?
  Double getValueGreek(final Greek greek, final Map<Object, Double> underlyings, final Double greekValue) {
    final Underlying order = greek.getUnderlying();
    return TaylorExpansionMultiplierCalculator.getMultiplier(underlyings, order) * greekValue
        * underlyings.get(TradeData.NUMBER_OF_CONTRACTS) * underlyings.get(TradeData.POINT_VALUE)
        / TaylorExpansionMultiplierCalculator.getMultiplier(order);
  }
}

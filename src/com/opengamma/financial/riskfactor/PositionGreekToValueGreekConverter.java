/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.sensitivity.MultiplePositionGreekResult;
import com.opengamma.financial.sensitivity.MultipleValueGreekResult;
import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.financial.sensitivity.PositionGreekResult;
import com.opengamma.financial.sensitivity.SinglePositionGreekResult;
import com.opengamma.financial.sensitivity.SingleValueGreekResult;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekResult;
import com.opengamma.math.function.Function1D;

/**
 *
 */
public class PositionGreekToValueGreekConverter extends Function1D<PositionGreekDataBundle, Map<ValueGreek, ValueGreekResult<?>>> {

  @Override
  public Map<ValueGreek, ValueGreekResult<?>> evaluate(final PositionGreekDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Position greek data bundle was null");
    final Map<PositionGreek, PositionGreekResult<?>> riskFactors = data.getRiskFactorResults();
    final Map<ValueGreek, ValueGreekResult<?>> result = new HashMap<ValueGreek, ValueGreekResult<?>>();
    Map<String, Double> multipleRiskFactorResult;
    Map<String, Double> riskFactorResultMap;
    PositionGreek key;
    PositionGreekResult<?> value;
    final Map<Object, Double> underlyingData = data.getUnderlyingData();
    for (final Map.Entry<PositionGreek, PositionGreekResult<?>> entry : riskFactors.entrySet()) {
      key = entry.getKey();
      value = entry.getValue();
      if (entry.getValue() instanceof SinglePositionGreekResult) {
        result.put(new ValueGreek(key.getUnderlyingGreek()), new SingleValueGreekResult(getValueGreek(key, underlyingData, (Double) value.getResult())));
      } else if (entry.getValue() instanceof MultiplePositionGreekResult) {
        multipleRiskFactorResult = ((MultiplePositionGreekResult) value).getResult();
        riskFactorResultMap = new HashMap<String, Double>();
        for (final Map.Entry<String, Double> e : multipleRiskFactorResult.entrySet()) {
          riskFactorResultMap.put(e.getKey(), getValueGreek(key, underlyingData, e.getValue()));
        }
        result.put(new ValueGreek(key.getUnderlyingGreek()), new MultipleValueGreekResult(riskFactorResultMap));
      } else {
        throw new IllegalArgumentException("Can only handle SingleRiskFactorResult and MultipleRiskFactorResult");
      }
    }
    return result;
  }

  //TODO theta?
  Double getValueGreek(final PositionGreek positionGreek, final Map<Object, Double> underlyings, final Double greekValue) {
    final Underlying order = positionGreek.getUnderlyingGreek().getUnderlying();
    return TaylorExpansionMultiplierCalculator.getMultiplier(underlyings, order) * greekValue * underlyings.get(TradeData.POINT_VALUE)
        / TaylorExpansionMultiplierCalculator.getMultiplier(order);
  }

}

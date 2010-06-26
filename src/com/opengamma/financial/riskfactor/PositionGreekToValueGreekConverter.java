/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.function.Function1D;

/**
 *
 */
public class PositionGreekToValueGreekConverter extends Function1D<PositionGreekDataBundle, Map<ValueGreek, Double>> {

  @Override
  public Map<ValueGreek, Double> evaluate(final PositionGreekDataBundle data) {
    Validate.notNull(data, "data");
    final Map<PositionGreek, Double> riskFactors = data.getRiskFactorResults();
    final Map<ValueGreek, Double> result = new HashMap<ValueGreek, Double>();
    final Map<Object, Double> underlyingData = data.getUnderlyingData();
    for (final Map.Entry<PositionGreek, Double> entry : riskFactors.entrySet()) {
      PositionGreek key = entry.getKey();
      Double value = entry.getValue();
      result.put(new ValueGreek(key.getUnderlyingGreek()), getValueGreek(key, underlyingData, value));
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

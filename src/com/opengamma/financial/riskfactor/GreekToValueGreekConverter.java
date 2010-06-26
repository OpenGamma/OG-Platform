/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class GreekToValueGreekConverter extends Function1D<GreekDataBundle, Map<ValueGreek, Double>> {

  @Override
  public Map<ValueGreek, Double> evaluate(final GreekDataBundle data) {
    Validate.notNull(data, "data");
    final GreekResultCollection greeks = data.getGreekResults();
    final Map<ValueGreek, Double> riskFactors = new HashMap<ValueGreek, Double>();
    final Map<Object, Double> underlyingData = data.getUnderlyingData();
    for (final Pair<Greek, Double> entry : greeks) {
      Greek key = entry.getKey();
      Double value = entry.getValue();
      riskFactors.put(new ValueGreek(key), getValueGreek(key, underlyingData, value));
    }
    return riskFactors;
  }

  // TODO handle theta separately?
  Double getValueGreek(final Greek greek, final Map<Object, Double> underlyings, final Double greekValue) {
    final Underlying order = greek.getUnderlying();
    return TaylorExpansionMultiplierCalculator.getMultiplier(underlyings, order) * greekValue * underlyings.get(TradeData.NUMBER_OF_CONTRACTS)
        * underlyings.get(TradeData.POINT_VALUE) / TaylorExpansionMultiplierCalculator.getMultiplier(order);
  }
}

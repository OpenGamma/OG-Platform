/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.trade.OptionTradeData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class GreekToValueGreekConverter extends Function1D<GreekDataBundle, Map<ValueGreek, Double>> {

  @Override
  public Map<ValueGreek, Double> evaluate(final GreekDataBundle data) {
    ArgumentChecker.notNull(data, "data");
    final GreekResultCollection greeks = data.getGreekResults();
    final Map<ValueGreek, Double> riskFactors = new HashMap<>();
    final Map<UnderlyingType, Double> underlyingData = data.getUnderlyingData();
    final OptionTradeData tradeData = data.getOptionTradeData();
    for (final Pair<Greek, Double> entry : greeks) {
      final Greek key = entry.getFirst();
      final Double value = entry.getSecond();
      riskFactors.put(new ValueGreek(key), getValueGreek(key, value, underlyingData, tradeData));
    }
    return riskFactors;
  }

  // TODO handle theta separately?
  private Double getValueGreek(final Greek greek, final double greekValue, final Map<UnderlyingType, Double> underlyings, final OptionTradeData tradeData) {
    final Underlying order = greek.getUnderlying();
    return TaylorExpansionMultiplierCalculator.getValue(underlyings, order) * greekValue * tradeData.getNumberOfContracts()
        * tradeData.getPointValue() / TaylorExpansionMultiplierCalculator.getMultiplier(order);
  }
}

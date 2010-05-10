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
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.math.function.Function1D;

/**
 *
 */
public class GreekToPositionGreekConverter extends Function1D<GreekDataBundle, Map<PositionGreek, RiskFactorResult<?>>> {

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Map<PositionGreek, RiskFactorResult<?>> evaluate(final GreekDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Risk factor data bundle was null");
    final GreekResultCollection greeks = data.getAllGreekValues();
    final Map<PositionGreek, RiskFactorResult<?>> riskFactors = new HashMap<PositionGreek, RiskFactorResult<?>>();
    Map<String, Double> multipleGreekResult;
    Map<Object, Double> riskFactorResultMap;
    PositionGreek positionGreek;
    for (final Map.Entry<Greek, GreekResult<?>> entry : greeks.entrySet()) {
      positionGreek = new PositionGreek(entry.getKey());
      if (entry.getValue() instanceof SingleGreekResult) {
        riskFactors.put(positionGreek, new SingleRiskFactorResult((Double) entry.getValue().getResult()
            * data.getUnderlyingDataForGreek(entry.getKey(), TradeData.NUMBER_OF_CONTRACTS)));
      } else if (entry.getValue() instanceof MultipleGreekResult) {
        multipleGreekResult = ((MultipleGreekResult) entry.getValue()).getResult();
        riskFactorResultMap = new HashMap<Object, Double>();
        for (final Map.Entry<String, Double> e : multipleGreekResult.entrySet()) {
          riskFactorResultMap.put(e.getKey(), e.getValue()
              * data.getUnderlyingDataForGreek(entry.getKey(), TradeData.NUMBER_OF_CONTRACTS));
        }
        riskFactors.put(positionGreek, new MultipleRiskFactorResult(riskFactorResultMap));
      } else {
        throw new IllegalArgumentException("Can only handle SingleGreekResult and MultipleGreekResult");
      }
    }
    return riskFactors;
  }
}

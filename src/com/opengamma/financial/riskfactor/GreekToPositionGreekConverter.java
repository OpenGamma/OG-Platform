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
import com.opengamma.financial.sensitivity.MultiplePositionGreekResult;
import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.financial.sensitivity.PositionGreekResult;
import com.opengamma.financial.sensitivity.SinglePositionGreekResult;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class GreekToPositionGreekConverter extends
    Function1D<GreekDataBundle, Map<PositionGreek, PositionGreekResult<?>>> {

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Map<PositionGreek, PositionGreekResult<?>> evaluate(final GreekDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Risk factor data bundle was null");
    final GreekResultCollection greeks = data.getGreekResults();
    final Map<PositionGreek, PositionGreekResult<?>> riskFactors = new HashMap<PositionGreek, PositionGreekResult<?>>();
    Map<String, Double> multipleGreekResult;
    Map<String, Double> multiplePositionGreekResult;
    PositionGreek positionGreek;
    for (final Pair<Greek, GreekResult<?>> entry : greeks) {
      positionGreek = new PositionGreek(entry.getKey());
      if (entry.getValue() instanceof SingleGreekResult) {
        riskFactors.put(positionGreek, new SinglePositionGreekResult((Double) entry.getValue().getResult()
            * data.getUnderlyingDataForObject(TradeData.NUMBER_OF_CONTRACTS)));
      } else if (entry.getValue() instanceof MultipleGreekResult) {
        multipleGreekResult = ((MultipleGreekResult) entry.getValue()).getResult();
        multiplePositionGreekResult = new HashMap<String, Double>();
        for (final Map.Entry<String, Double> e : multipleGreekResult.entrySet()) {
          multiplePositionGreekResult.put(e.getKey(), e.getValue()
              * data.getUnderlyingDataForObject(TradeData.NUMBER_OF_CONTRACTS));
        }
        riskFactors.put(positionGreek, new MultiplePositionGreekResult(multiplePositionGreekResult));
      } else {
        throw new IllegalArgumentException("Can only handle SingleGreekResult and MultipleGreekResult");
      }
    }
    return riskFactors;
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class GreekToPositionGreekConverter extends
    Function1D<GreekDataBundle, Map<PositionGreek, Double>> {

  @Override
  public Map<PositionGreek, Double> evaluate(final GreekDataBundle data) {
    ArgumentChecker.notNull(data, "Risk factor data bundle");
    final GreekResultCollection greeks = data.getGreekResults();
    final Map<PositionGreek, Double> riskFactors = new HashMap<PositionGreek, Double>();
    PositionGreek positionGreek;
    for (final Pair<Greek, Double> entry : greeks) {
      positionGreek = new PositionGreek(entry.getKey());
      riskFactors.put(positionGreek, entry.getValue() * data.getUnderlyingDataForObject(TradeData.NUMBER_OF_CONTRACTS));
    }
    return riskFactors;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.sensitivity.PositionGreek;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class GreekToPositionGreekConverter extends Function1D<GreekDataBundle, Map<PositionGreek, Double>> {

  @Override
  public Map<PositionGreek, Double> evaluate(final GreekDataBundle data) {
    Validate.notNull(data, "Risk factor data bundle");
    final GreekResultCollection greeks = data.getGreekResults();
    final Map<PositionGreek, Double> riskFactors = new HashMap<PositionGreek, Double>();
    PositionGreek positionGreek;
    for (final Pair<Greek, Double> entry : greeks) {
      positionGreek = new PositionGreek(entry.getKey());
      riskFactors.put(positionGreek, entry.getValue() * data.getOptionTradeData().getNumberOfContracts());
    }
    return riskFactors;
  }

}

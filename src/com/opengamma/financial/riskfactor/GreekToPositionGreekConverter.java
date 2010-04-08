/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.AbstractGreekVisitor;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.MultipleGreekResult;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 *
 */
public class GreekToPositionGreekConverter extends Function1D<GreekDataBundle, Map<PositionGreek, RiskFactorResult<?>>> {
  private final GreekVisitor<PositionGreek> _visitor = new GreekToPositionGreekVisitor();

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
    for (final Map.Entry<Greek, GreekResult<?>> entry : greeks.entrySet()) {
      if (entry.getValue() instanceof SingleGreekResult) {
        riskFactors.put(entry.getKey().accept(_visitor), new SingleRiskFactorResult((Double) entry.getValue().getResult()
            * data.getUnderlyingDataForGreek(entry.getKey(), TradeData.NUMBER_OF_CONTRACTS)));
      } else if (entry.getValue() instanceof MultipleGreekResult) {
        multipleGreekResult = ((MultipleGreekResult) entry.getValue()).getResult();
        riskFactorResultMap = new HashMap<Object, Double>();
        for (final Map.Entry<String, Double> e : multipleGreekResult.entrySet()) {
          riskFactorResultMap.put(e.getKey(), e.getValue() * data.getUnderlyingDataForGreek(entry.getKey(), TradeData.NUMBER_OF_CONTRACTS));
        }
        riskFactors.put(entry.getKey().accept(_visitor), new MultipleRiskFactorResult(riskFactorResultMap));
      } else {
        throw new IllegalArgumentException("Can only handle SingleGreekResult and MultipleGreekResult");
      }
    }
    return riskFactors;
  }

  // TODO this will probably be better extracted out
  class GreekToPositionGreekVisitor extends AbstractGreekVisitor<PositionGreek> {

    /*
     * (non-Javadoc)
     * 
     * @see com.opengamma.financial.greeks.GreekVisitor#visitDelta()
     */
    @Override
    public PositionGreek visitDelta() {
      return PositionGreek.POSITION_DELTA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.opengamma.financial.greeks.GreekVisitor#visitGamma()
     */
    @Override
    public PositionGreek visitGamma() {
      return PositionGreek.POSITION_GAMMA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.opengamma.financial.greeks.GreekVisitor#visitRho()
     */
    @Override
    public PositionGreek visitRho() {
      return PositionGreek.POSITION_RHO;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.opengamma.financial.greeks.GreekVisitor#visitTheta()
     */
    @Override
    public PositionGreek visitTheta() {
      return PositionGreek.POSITION_THETA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.opengamma.financial.greeks.GreekVisitor#visitVega()
     */
    @Override
    public PositionGreek visitVega() {
      return PositionGreek.POSITION_VEGA;
    }

    @Override
    public PositionGreek visitVanna() {
      return PositionGreek.POSITION_VANNA;
    }
  }
}

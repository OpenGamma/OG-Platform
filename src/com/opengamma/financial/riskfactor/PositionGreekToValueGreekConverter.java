/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.greeks.FirstOrder;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.MixedSecondOrder;
import com.opengamma.financial.greeks.Order;
import com.opengamma.financial.greeks.SecondOrder;
import com.opengamma.financial.pnl.OptionTradeData;
import com.opengamma.financial.pnl.Underlying;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 *
 */
public class PositionGreekToValueGreekConverter extends Function1D<PositionGreekDataBundle, Map<Sensitivity, RiskFactorResult<?>>> {
  private final GreekVisitor<Sensitivity> _visitor = new GreekToValueGreekVisitor();

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Map<Sensitivity, RiskFactorResult<?>> evaluate(final PositionGreekDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Risk factor data bundle was null");
    final Map<PositionGreek, RiskFactorResult<?>> riskFactors = data.getAllRiskFactorValues();
    final Map<Sensitivity, RiskFactorResult<?>> result = new HashMap<Sensitivity, RiskFactorResult<?>>();
    Map<Object, Double> multipleRiskFactorResult;
    Map<Object, Double> riskFactorResultMap;
    PositionGreek key;
    RiskFactorResult<?> value;
    for (final Map.Entry<PositionGreek, RiskFactorResult<?>> entry : riskFactors.entrySet()) {
      key = entry.getKey();
      value = entry.getValue();
      if (entry.getValue() instanceof SingleRiskFactorResult) {
        result.put(key.getUnderlyingGreek().accept(_visitor), new SingleRiskFactorResult(getValueGreek(key, data, (Double) value.getResult())));
      } else if (entry.getValue() instanceof MultipleRiskFactorResult) {
        multipleRiskFactorResult = ((MultipleRiskFactorResult) value).getResult();
        riskFactorResultMap = new HashMap<Object, Double>();
        for (final Map.Entry<Object, Double> e : multipleRiskFactorResult.entrySet()) {
          riskFactorResultMap.put(e.getKey(), getValueGreek(key, data, e.getValue()));
        }
        result.put(key.getUnderlyingGreek().accept(_visitor), new MultipleRiskFactorResult(riskFactorResultMap));
      } else {
        throw new IllegalArgumentException("Can only handle SingleRiskFactorResult and MultipleRiskFactorResult");
      }
    }
    return result;
  }

  Double getValueGreek(final PositionGreek positionGreek, final PositionGreekDataBundle data, final Double positionGreekValue) {
    final Order order = positionGreek.getUnderlyingGreek().getOrder();
    final Map<Object, Double> underlyings = data.getAllUnderlyingDataForRiskFactor(positionGreek);
    if (order instanceof FirstOrder) {
      final Underlying underlying = ((FirstOrder) order).getVariable();
      return positionGreekValue * underlyings.get(underlying) * underlyings.get(OptionTradeData.POINT_VALUE);
    }
    if (order instanceof SecondOrder) {
      final Underlying underlying = ((SecondOrder) order).getVariable();
      final double x = underlyings.get(underlying);
      return positionGreekValue * x * x * underlyings.get(OptionTradeData.POINT_VALUE);
    }
    if (order instanceof MixedSecondOrder) {
      final MixedSecondOrder mixedFirst = ((MixedSecondOrder) order);
      return positionGreekValue * underlyings.get(mixedFirst.getFirstVariable().getVariable()) * underlyings.get(mixedFirst.getSecondVariable().getVariable())
          * underlyings.get(OptionTradeData.POINT_VALUE);
    }
    throw new IllegalArgumentException("Can currently only handle first-, mixed-second- and second-order greeks");// TODO
  }

}

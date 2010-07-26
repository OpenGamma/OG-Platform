/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.List;
import java.util.Map;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class PresentValueSensitivityCalculator implements InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> {

  @Override
  public Map<String, List<Pair<Double, Double>>> visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitCash(Cash cash, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitLibor(Libor libor, YieldCurveBundle curves) {
    return null;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    return null;
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;

/**
 * 
 */
public class InterestRateDerivativeVisitorTest {
  private static final String CURVE_NAME = "Test";

  private static final InterestRateDerivativeVisitor<Class<?>> VISITOR = new InterestRateDerivativeVisitor<Class<?>>() {

    private Class<?> visit(final InterestRateDerivative derivative, YieldCurveBundle curves) {
      return derivative.getClass();
    }

    @Override
    public Class<?> visitCash(Cash cash, YieldCurveBundle curves) {
      return visit(cash, curves);
    }

    @Override
    public Class<?> visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
      return visit(fra, curves);
    }

    @Override
    public Class<?> visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
      return visit(future, curves);
    }

    @Override
    public Class<?> visitLibor(Libor libor, YieldCurveBundle curves) {
      return visit(libor, curves);
    }

    @Override
    public Class<?> visitSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
      return visit(swap, curves);
    }
  };

  @Test
  public void test() {
    YieldCurveBundle curves = new YieldCurveBundle();
    final Cash cash = new Cash(1, CURVE_NAME);
    final ForwardRateAgreement fra = new ForwardRateAgreement(0, 1, CURVE_NAME);
    final InterestRateFuture future = new InterestRateFuture(0, 1, CURVE_NAME);
    final Libor libor = new Libor(1, CURVE_NAME);
    final FixedFloatSwap swap = new FixedFloatSwap(new double[] {1}, new double[] {1}, new double[] {0}, new double[] {0}, CURVE_NAME, CURVE_NAME);
    VariableAnnuity payLeg = new VariableAnnuity(new double[] {1}, CURVE_NAME, CURVE_NAME);
    final BasisSwap bSwap = new BasisSwap(payLeg, payLeg);
    assertEquals(cash.accept(VISITOR, curves), Cash.class);
    assertEquals(fra.accept(VISITOR, curves), ForwardRateAgreement.class);
    assertEquals(future.accept(VISITOR, curves), InterestRateFuture.class);
    assertEquals(libor.accept(VISITOR, curves), Libor.class);
    assertEquals(swap.accept(VISITOR, curves), FixedFloatSwap.class);
    assertEquals(bSwap.accept(VISITOR, curves), BasisSwap.class);
  }
}

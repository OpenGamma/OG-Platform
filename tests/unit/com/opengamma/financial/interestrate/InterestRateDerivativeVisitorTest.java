/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;

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
    public Class<?> visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitBond(Bond bond, YieldCurveBundle curves) {
      return visit(bond, curves);
    }

    @Override
    public Class<?> visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
      return visit(annuity, curves);
    }

    @Override
    public Class<?> visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitSwap(Swap swap, YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
      return visit(annuity, curves);
    }
  };

  @Test
  public void test() {
    YieldCurveBundle curves = new YieldCurveBundle();
    final Cash cash = new Cash(1, 0, CURVE_NAME);
    final ForwardRateAgreement fra = new ForwardRateAgreement(0, 1, 0, CURVE_NAME, CURVE_NAME);
    final InterestRateFuture future = new InterestRateFuture(0, 1, 0, CURVE_NAME);
    final Libor libor = new Libor(1, 0, CURVE_NAME);
    final Bond bond = new Bond(new double[] {1}, 0, CURVE_NAME);
    final VariableAnnuity floatLeg = new VariableAnnuity(new double[] {1}, CURVE_NAME, CURVE_NAME);
    final FixedAnnuity fixedLeg = new FixedAnnuity(new double[] {1}, CURVE_NAME);
    final FixedFloatSwap swap = new FixedFloatSwap(fixedLeg, floatLeg);
    final BasisSwap bSwap = new BasisSwap(floatLeg, floatLeg);
    assertEquals(cash.accept(VISITOR, curves), Cash.class);
    assertEquals(fra.accept(VISITOR, curves), ForwardRateAgreement.class);
    assertEquals(future.accept(VISITOR, curves), InterestRateFuture.class);
    assertEquals(libor.accept(VISITOR, curves), Libor.class);
    assertEquals(bond.accept(VISITOR, curves), Bond.class);
    assertEquals(fixedLeg.accept(VISITOR, curves), FixedAnnuity.class);
    assertEquals(floatLeg.accept(VISITOR, curves), VariableAnnuity.class);
    assertEquals(swap.accept(VISITOR, curves), FixedFloatSwap.class);
    assertEquals(bSwap.accept(VISITOR, curves), BasisSwap.class);
  }
}

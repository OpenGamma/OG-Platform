/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.definition.Libor;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * 
 */
public class InterestRateDerivativeVisitorTest {
  private static final String CURVE_NAME = "Test";

  private static final InterestRateDerivativeVisitor<Class<?>> VISITOR = new InterestRateDerivativeVisitor<Class<?>>() {

    private Class<?> visit(final InterestRateDerivative derivative, @SuppressWarnings("unused") final YieldCurveBundle curves) {
      return derivative.getClass();
    }

    @Override
    public Class<?> getValue(InterestRateDerivative ird, YieldCurveBundle curves) {
      return ird.accept(this, curves);
    }

    @Override
    public Class<?> visitCash(final Cash cash, final YieldCurveBundle curves) {
      return visit(cash, curves);
    }

    @Override
    public Class<?> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
      return visit(fra, curves);
    }

    @Override
    public Class<?> visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
      return visit(future, curves);
    }

    @Override
    public Class<?> visitBasisSwap(final BasisSwap swap, final YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitBond(final Bond bond, final YieldCurveBundle curves) {
      return visit(bond, curves);
    }

    @Override
    public Class<?> visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
      return visit(annuity, curves);
    }

    @Override
    public Class<?> visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitSwap(final Swap swap, final YieldCurveBundle curves) {
      return visit(swap, curves);
    }

    @Override
    public Class<?> visitVariableAnnuity(final VariableAnnuity annuity, final YieldCurveBundle curves) {
      return visit(annuity, curves);
    }

    @Override
    public Class<?> visitConstantCouponAnnuity(ConstantCouponAnnuity annuity, YieldCurveBundle curves) {
      return visit(annuity, curves);
    }

    @Override
    public Class<?> visitFloatingRateNote(FloatingRateNote frn, YieldCurveBundle curves) {
      return visit(frn, curves);
    }
  };

  @Test
  public void test() {

    final YieldCurveBundle curves = new YieldCurveBundle();
    final Cash cash = new Cash(1, 0, CURVE_NAME);
    final ForwardRateAgreement fra = new ForwardRateAgreement(0, 1, 0, CURVE_NAME, CURVE_NAME);
    final InterestRateFuture future = new InterestRateFuture(0, 1, 1, 0, CURVE_NAME);
    final Libor libor = new Libor(1, 0, CURVE_NAME);
    final Bond bond = new Bond(new double[] {1}, 0, CURVE_NAME);
    final VariableAnnuity floatLeg = new VariableAnnuity(new double[] {1}, CURVE_NAME, CURVE_NAME);
    final FixedAnnuity fixedLeg = new FixedAnnuity(new double[] {1}, 1.0, new double[] {0}, CURVE_NAME);
    final ConstantCouponAnnuity fixedLeg2 = new ConstantCouponAnnuity(new double[] {1}, 0.0, CURVE_NAME);
    final FixedFloatSwap swap = new FixedFloatSwap(fixedLeg2, floatLeg);
    final BasisSwap bSwap = new BasisSwap(floatLeg, floatLeg);
    final FloatingRateNote frn = new FloatingRateNote(floatLeg);
    assertEquals(VISITOR.getValue(cash, curves), Cash.class);
    assertEquals(fra.accept(VISITOR, curves), ForwardRateAgreement.class);
    assertEquals(future.accept(VISITOR, curves), InterestRateFuture.class);
    assertEquals(libor.accept(VISITOR, curves), Libor.class);
    assertEquals(bond.accept(VISITOR, curves), Bond.class);
    assertEquals(fixedLeg.accept(VISITOR, curves), FixedAnnuity.class);
    assertEquals(fixedLeg2.accept(VISITOR, curves), ConstantCouponAnnuity.class);
    assertEquals(floatLeg.accept(VISITOR, curves), VariableAnnuity.class);
    assertEquals(swap.accept(VISITOR, curves), FixedFloatSwap.class);
    assertEquals(bSwap.accept(VISITOR, curves), BasisSwap.class);
    assertEquals(frn.accept(VISITOR, curves), FloatingRateNote.class);
  }

}

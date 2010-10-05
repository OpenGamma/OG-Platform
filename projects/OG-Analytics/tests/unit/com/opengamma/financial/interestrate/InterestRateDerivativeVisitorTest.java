/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.definition.Libor;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;

/**
 * 
 */
public class InterestRateDerivativeVisitorTest {
  private static final String CURVE_NAME = "Test";

  private static final InterestRateDerivativeVisitor<Object, Class<?>> VISITOR = new InterestRateDerivativeVisitor<Object, Class<?>>() {

    private Class<?> visit(final InterestRateDerivative derivative, @SuppressWarnings("unused") final Object curves) {
      return derivative.getClass();
    }

    @Override
    public Class<?> getValue(InterestRateDerivative ird, Object anything) {
      return ird.accept(this, anything);
    }

    @Override
    public Class<?> visitCash(final Cash cash, final Object anything) {
      return visit(cash, anything);
    }

    @Override
    public Class<?> visitForwardRateAgreement(final ForwardRateAgreement fra, final Object anything) {
      return visit(fra, anything);
    }

    @Override
    public Class<?> visitInterestRateFuture(final InterestRateFuture future, final Object anything) {
      return visit(future, anything);
    }

    @Override
    public Class<?> visitTenorSwap(final TenorSwap swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitBond(final Bond bond, final Object anything) {
      return visit(bond, anything);
    }

    @Override
    public Class<?> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitFloatingRateNote(FloatingRateNote frn, Object anything) {
      return visit(frn, anything);
    }

    @Override
    public Class<?> visitFixedPayment(FixedPayment payment, Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitForwardLiborPayment(ForwardLiborPayment payment, Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitGenericAnnuity(GenericAnnuity<? extends Payment> annuity, Object anything) {
      return visit(annuity, anything);
    }

    @Override
    public Class<?> visitSwap(Swap<?, ?> swap, Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitContinuouslyMonitoredAverageRatePayment(ContinuouslyMonitoredAverageRatePayment payment, Object anything) {
      return visit(payment, anything);
    }
  };

  @Test
  public void test() {

    final Object curves = null;
    final Cash cash = new Cash(1, 0, CURVE_NAME);
    final ForwardRateAgreement fra = new ForwardRateAgreement(0, 1, 0, CURVE_NAME, CURVE_NAME);
    final InterestRateFuture future = new InterestRateFuture(0, 1, 1, 0, CURVE_NAME);
    final Libor libor = new Libor(1, 0, CURVE_NAME);
    final Bond bond = new Bond(new double[] {1}, 0, CURVE_NAME);
    final ForwardLiborAnnuity floatLeg = new ForwardLiborAnnuity(new double[] {1}, CURVE_NAME, CURVE_NAME);
    // final FixedAnnuity fixedLeg = new FixedAnnuity(new double[] {1}, 1.0, new double[] {0}, CURVE_NAME);
    final FixedCouponAnnuity fixedLeg2 = new FixedCouponAnnuity(new double[] {1}, 0.0, CURVE_NAME);
    final FixedFloatSwap swap = new FixedFloatSwap(fixedLeg2, floatLeg);
    final TenorSwap bSwap = new TenorSwap(floatLeg, floatLeg);
    final FloatingRateNote frn = new FloatingRateNote(floatLeg);
    final FixedPayment fixedPayment = new FixedPayment(1, 1, CURVE_NAME);
    final ForwardLiborPayment liborPayment = new ForwardLiborPayment(1.0, 0, 1, 1, 1, CURVE_NAME, CURVE_NAME);
    final GenericAnnuity<Payment> ga = new GenericAnnuity<Payment>(new Payment[] {fixedPayment, liborPayment});
    assertEquals(VISITOR.getValue(cash, curves), Cash.class);
    assertEquals(fra.accept(VISITOR, curves), ForwardRateAgreement.class);
    assertEquals(future.accept(VISITOR, curves), InterestRateFuture.class);
    assertEquals(libor.accept(VISITOR, curves), Libor.class);
    assertEquals(bond.accept(VISITOR, curves), Bond.class);
    assertEquals(fixedLeg2.accept(VISITOR, curves), FixedCouponAnnuity.class);
    assertEquals(floatLeg.accept(VISITOR, curves), ForwardLiborAnnuity.class);
    assertEquals(swap.accept(VISITOR, curves), FixedFloatSwap.class);
    assertEquals(bSwap.accept(VISITOR, curves), TenorSwap.class);
    assertEquals(frn.accept(VISITOR, curves), FloatingRateNote.class);
    assertEquals(fixedPayment.accept(VISITOR, curves), FixedPayment.class);
    assertEquals(liborPayment.accept(VISITOR, curves), ForwardLiborPayment.class);
    assertEquals(ga.accept(VISITOR, curves), GenericAnnuity.class);
  }

}

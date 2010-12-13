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

    @Override
    public Class<?> visit(final InterestRateDerivative derivative, final Object curves) {
      return derivative.getClass();
    }

    @Override
    public Class<?> visit(final InterestRateDerivative ird) {
      return ird.accept(this, null);
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
    public Class<?> visitFloatingRateNote(final FloatingRateNote frn, final Object anything) {
      return visit(frn, anything);
    }

    @Override
    public Class<?> visitFixedPayment(final FixedPayment payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitForwardLiborPayment(final ForwardLiborPayment payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final Object anything) {
      return visit(annuity, anything);
    }

    @Override
    public Class<?> visitSwap(final Swap<?, ?> swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitCash(final Cash cash) {
      return visit(cash);
    }

    @Override
    public Class<?> visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return visit(fra);
    }

    @Override
    public Class<?> visitInterestRateFuture(final InterestRateFuture future) {
      return visit(future);
    }

    @Override
    public Class<?> visitSwap(final Swap<?, ?> swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitTenorSwap(final TenorSwap swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitFloatingRateNote(final FloatingRateNote frn) {
      return visit(frn);
    }

    @Override
    public Class<?> visitBond(final Bond bond) {
      return visit(bond);
    }

    @Override
    public Class<?> visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity) {
      return visit(annuity);
    }

    @Override
    public Class<?> visitFixedPayment(final FixedPayment payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitForwardLiborPayment(final ForwardLiborPayment payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment) {
      return visit(payment);
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
    final FixedCouponAnnuity fixedLeg2 = new FixedCouponAnnuity(new double[] {1}, 0.0, CURVE_NAME);
    final FixedFloatSwap swap = new FixedFloatSwap(fixedLeg2, floatLeg);
    final TenorSwap bSwap = new TenorSwap(floatLeg, floatLeg);
    final FloatingRateNote frn = new FloatingRateNote(floatLeg);
    final FixedPayment fixedPayment = new FixedPayment(1, 1, CURVE_NAME);
    final ForwardLiborPayment liborPayment = new ForwardLiborPayment(1.0, 0, 1, 1, 1, CURVE_NAME, CURVE_NAME);
    final GenericAnnuity<Payment> ga = new GenericAnnuity<Payment>(new Payment[] {fixedPayment, liborPayment});
    assertEquals(VISITOR.visit(cash, curves), Cash.class);
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
    assertEquals(ga.accept(VISITOR), GenericAnnuity.class);
    assertEquals(VISITOR.visit(cash), Cash.class);
    assertEquals(fra.accept(VISITOR), ForwardRateAgreement.class);
    assertEquals(future.accept(VISITOR), InterestRateFuture.class);
    assertEquals(libor.accept(VISITOR), Libor.class);
    assertEquals(bond.accept(VISITOR), Bond.class);
    assertEquals(fixedLeg2.accept(VISITOR), FixedCouponAnnuity.class);
    assertEquals(floatLeg.accept(VISITOR), ForwardLiborAnnuity.class);
    assertEquals(swap.accept(VISITOR), FixedFloatSwap.class);
    assertEquals(bSwap.accept(VISITOR), TenorSwap.class);
    assertEquals(frn.accept(VISITOR), FloatingRateNote.class);
    assertEquals(fixedPayment.accept(VISITOR), FixedPayment.class);
    assertEquals(liborPayment.accept(VISITOR), ForwardLiborPayment.class);
    assertEquals(ga.accept(VISITOR), GenericAnnuity.class);
  }

}

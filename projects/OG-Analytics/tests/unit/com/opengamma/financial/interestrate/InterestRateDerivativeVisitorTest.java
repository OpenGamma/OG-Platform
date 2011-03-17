/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
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
  private static final AbstractInterestRateDerivativeVisitor<Object, Object> ABSTRACT_VISITOR = new AbstractInterestRateDerivativeVisitor<Object, Object>() {
  };
  private static final Cash CASH = new Cash(1, 0, CURVE_NAME);
  private static final ForwardRateAgreement FRA = new ForwardRateAgreement(0, 1, 0, CURVE_NAME, CURVE_NAME);
  private static final InterestRateFuture IR_FUTURE = new InterestRateFuture(0, 1, 1, 0, CURVE_NAME);
  private static final Bond BOND = new Bond(new double[] {1}, 0, CURVE_NAME);
  private static final BondForward BOND_FORWARD = new BondForward(BOND, .5, 0, 0);
  private static final BondFuture BOND_FUTURE = new BondFuture(new BondForward[] {BOND_FORWARD}, new double[] {1}, 131);
  private static final AnnuityCouponIbor FLOAT_LEG = new AnnuityCouponIbor(new double[] {1}, CURVE_NAME, CURVE_NAME);
  private static final AnnuityCouponFixed FIXED_LEG = new AnnuityCouponFixed(new double[] {1}, 0.0, CURVE_NAME);
  private static final FixedFloatSwap SWAP = new FixedFloatSwap(FIXED_LEG, FLOAT_LEG);
  private static final TenorSwap<CouponIbor> TENOR_SWAP = new TenorSwap<CouponIbor>(FLOAT_LEG, FLOAT_LEG);
  private static final FloatingRateNote FRN = new FloatingRateNote(FLOAT_LEG);
  private static final PaymentFixed FIXED_PAYMENT = new PaymentFixed(1, 1, CURVE_NAME);
  private static final CouponIbor LIBOR_PAYMENT = new CouponIbor(1.0, CURVE_NAME, 0, 1, 1, 1, 1, 0, CURVE_NAME);
  //  (1.0, 0, 1, 1, 1, CURVE_NAME, CURVE_NAME);
  private static final GenericAnnuity<Payment> GA = new GenericAnnuity<Payment>(new Payment[] {FIXED_PAYMENT, LIBOR_PAYMENT});
  private static final FixedCouponSwap<CouponIbor> FCS = new FixedCouponSwap<CouponIbor>(FIXED_LEG, FLOAT_LEG);
  private static final AnnuityCouponFixed FCA = new AnnuityCouponFixed(new double[] {1}, 0.05, CURVE_NAME);
  private static final AnnuityCouponIbor FLA = new AnnuityCouponIbor(new double[] {1}, 0.05, CURVE_NAME, CURVE_NAME);
  private static final CouponFixed FCP = new CouponFixed(1, CURVE_NAME, 1, 0.04);
  private static final ContinuouslyMonitoredAverageRatePayment CM = new ContinuouslyMonitoredAverageRatePayment(3, CURVE_NAME, 1, 100, 1, 1, 2, 0, CURVE_NAME);
  //(3, CURVE_NAME, 1, 2, 1, 1, 0, 1, CURVE_NAME);
  private static final Swap<Payment, Payment> FIXED_FIXED = new Swap<Payment, Payment>(GA, GA);
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
    public Class<?> visitTenorSwap(final TenorSwap<? extends Payment> swap, final Object anything) {
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
    public Class<?> visitFixedPayment(final PaymentFixed payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitCouponIbor(final CouponIbor payment, final Object anything) {
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
    public Class<?> visitTenorSwap(final TenorSwap<? extends Payment> swap) {
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
    public Class<?> visitFixedPayment(final PaymentFixed payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitCouponIbor(final CouponIbor payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitBondForward(final BondForward bondForward, final Object anything) {
      return visit(bondForward, anything);
    }

    @Override
    public Class<?> visitBondFuture(final BondFuture bondFuture, final Object anything) {
      return visit(bondFuture, anything);
    }

    @Override
    public Class<?> visitBondForward(final BondForward bondForward) {
      return visit(bondForward);
    }

    @Override
    public Class<?> visitBondFuture(final BondFuture bondFuture) {
      return visit(bondFuture);
    }

    @Override
    public Class<?> visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final Object anything) {
      return visit(fixedCouponAnnuity, anything);
    }

    @Override
    public Class<?> visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final Object anything) {
      return visit(forwardLiborAnnuity, anything);
    }

    @Override
    public Class<?> visitFixedFloatSwap(final FixedFloatSwap swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitFixedCouponPayment(final CouponFixed payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
      return visit(fixedCouponAnnuity);
    }

    @Override
    public Class<?> visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
      return visit(forwardLiborAnnuity);
    }

    @Override
    public Class<?> visitFixedFloatSwap(final FixedFloatSwap swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitFixedCouponPayment(final CouponFixed payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity) {
      return visit(genericAnnuity);
    }

    @Override
    public Class<?> visitCouponCMS(CouponCMS payment, Object data) {
      return visit(payment, data);
    }

    @Override
    public Class<?> visitCouponCMS(CouponCMS payment) {
      return visit(payment);
    }
  };

  @Test
  public void test() {
    final Object curves = null;
    assertEquals(VISITOR.visit(CASH, curves), Cash.class);
    assertEquals(FRA.accept(VISITOR, curves), ForwardRateAgreement.class);
    assertEquals(IR_FUTURE.accept(VISITOR, curves), InterestRateFuture.class);
    assertEquals(BOND.accept(VISITOR, curves), Bond.class);
    assertEquals(BOND_FORWARD.accept(VISITOR, curves), BondForward.class);
    assertEquals(BOND_FUTURE.accept(VISITOR, curves), BondFuture.class);
    assertEquals(FIXED_LEG.accept(VISITOR, curves), AnnuityCouponFixed.class);
    assertEquals(FLOAT_LEG.accept(VISITOR, curves), AnnuityCouponIbor.class);
    assertEquals(SWAP.accept(VISITOR, curves), FixedFloatSwap.class);
    assertEquals(TENOR_SWAP.accept(VISITOR, curves), TenorSwap.class);
    assertEquals(FRN.accept(VISITOR, curves), FloatingRateNote.class);
    assertEquals(FIXED_PAYMENT.accept(VISITOR, curves), PaymentFixed.class);
    assertEquals(LIBOR_PAYMENT.accept(VISITOR, curves), CouponIbor.class);
    assertEquals(FCA.accept(VISITOR, curves), AnnuityCouponFixed.class);
    assertEquals(FLA.accept(VISITOR, curves), AnnuityCouponIbor.class);
    assertEquals(FCS.accept(VISITOR, curves), FixedCouponSwap.class);
    assertEquals(FCP.accept(VISITOR, curves), CouponFixed.class);
    assertEquals(CM.accept(VISITOR, curves), ContinuouslyMonitoredAverageRatePayment.class);
    assertEquals(GA.accept(VISITOR, curves), GenericAnnuity.class);
    assertEquals(FIXED_FIXED.accept(VISITOR, curves), Swap.class);
    assertEquals(VISITOR.visit(CASH), Cash.class);
    assertEquals(FRA.accept(VISITOR), ForwardRateAgreement.class);
    assertEquals(IR_FUTURE.accept(VISITOR), InterestRateFuture.class);
    assertEquals(BOND.accept(VISITOR), Bond.class);
    assertEquals(BOND_FORWARD.accept(VISITOR), BondForward.class);
    assertEquals(BOND_FUTURE.accept(VISITOR), BondFuture.class);
    assertEquals(FIXED_LEG.accept(VISITOR), AnnuityCouponFixed.class);
    assertEquals(FLOAT_LEG.accept(VISITOR), AnnuityCouponIbor.class);
    assertEquals(SWAP.accept(VISITOR), FixedFloatSwap.class);
    assertEquals(TENOR_SWAP.accept(VISITOR), TenorSwap.class);
    assertEquals(FRN.accept(VISITOR), FloatingRateNote.class);
    assertEquals(FIXED_PAYMENT.accept(VISITOR), PaymentFixed.class);
    assertEquals(LIBOR_PAYMENT.accept(VISITOR), CouponIbor.class);
    assertEquals(GA.accept(VISITOR), GenericAnnuity.class);
    assertEquals(FCA.accept(VISITOR), AnnuityCouponFixed.class);
    assertEquals(FLA.accept(VISITOR), AnnuityCouponIbor.class);
    assertEquals(FCS.accept(VISITOR), FixedCouponSwap.class);
    assertEquals(FCP.accept(VISITOR), CouponFixed.class);
    assertEquals(CM.accept(VISITOR), ContinuouslyMonitoredAverageRatePayment.class);
    assertEquals(FIXED_FIXED.accept(VISITOR), Swap.class);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCash1() {
    ABSTRACT_VISITOR.visit(CASH, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCash2() {
    ABSTRACT_VISITOR.visit(CASH);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFRA1() {
    ABSTRACT_VISITOR.visit(FRA, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFRA2() {
    ABSTRACT_VISITOR.visit(FRA);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIRFuture1() {
    ABSTRACT_VISITOR.visit(IR_FUTURE, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIRFuture2() {
    ABSTRACT_VISITOR.visit(IR_FUTURE);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBond1() {
    ABSTRACT_VISITOR.visit(BOND, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBond2() {
    ABSTRACT_VISITOR.visit(BOND);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBondForward1() {
    ABSTRACT_VISITOR.visit(BOND_FORWARD, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBondForward2() {
    ABSTRACT_VISITOR.visit(BOND_FORWARD);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBondFuture1() {
    ABSTRACT_VISITOR.visit(BOND_FUTURE, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testBondFuture2() {
    ABSTRACT_VISITOR.visit(BOND_FUTURE);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFloatLeg1() {
    ABSTRACT_VISITOR.visit(FLOAT_LEG, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFloatLeg2() {
    ABSTRACT_VISITOR.visit(FLOAT_LEG);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFixedLeg1() {
    ABSTRACT_VISITOR.visit(FIXED_LEG, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFixedLeg2() {
    ABSTRACT_VISITOR.visit(FIXED_LEG);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSwap1() {
    ABSTRACT_VISITOR.visit(SWAP, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSwap2() {
    ABSTRACT_VISITOR.visit(SWAP);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTenorSwap1() {
    ABSTRACT_VISITOR.visit(TENOR_SWAP, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTenorSwap2() {
    ABSTRACT_VISITOR.visit(TENOR_SWAP);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFRN1() {
    ABSTRACT_VISITOR.visit(FRN, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFRN2() {
    ABSTRACT_VISITOR.visit(FRN);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFixedPayment1() {
    ABSTRACT_VISITOR.visit(FIXED_PAYMENT, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFixedPayment2() {
    ABSTRACT_VISITOR.visit(FIXED_PAYMENT);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testLiborPayment1() {
    ABSTRACT_VISITOR.visit(LIBOR_PAYMENT, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testLiborPayment2() {
    ABSTRACT_VISITOR.visit(LIBOR_PAYMENT);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGA1() {
    ABSTRACT_VISITOR.visit(GA, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGA2() {
    ABSTRACT_VISITOR.visit(GA);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFCS1() {
    ABSTRACT_VISITOR.visit(FCS, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFCS2() {
    ABSTRACT_VISITOR.visit(FCS);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFCA1() {
    ABSTRACT_VISITOR.visit(FCA, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFCA2() {
    ABSTRACT_VISITOR.visit(FCA);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFLA1() {
    ABSTRACT_VISITOR.visit(FLA, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFLA2() {
    ABSTRACT_VISITOR.visit(FLA);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFCP1() {
    ABSTRACT_VISITOR.visit(FCP, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFCP2() {
    ABSTRACT_VISITOR.visit(FCP);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGeneralSwap1() {
    ABSTRACT_VISITOR.visit(FIXED_FIXED, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGeneralSwap2() {
    ABSTRACT_VISITOR.visit(FIXED_FIXED);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCM1() {
    ABSTRACT_VISITOR.visit(CM, CURVE_NAME);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCM2() {
    ABSTRACT_VISITOR.visit(CM);
  }
}

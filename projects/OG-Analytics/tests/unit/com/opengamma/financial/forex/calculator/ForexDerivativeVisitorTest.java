/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthly;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * Tests the visitor of Forex derivatives.
 */
public class ForexDerivativeVisitorTest {

  private static final Forex FX = ForexInstrumentsDescriptionDataSet.createForex();
  private static final ForexSwap FX_SWAP = ForexInstrumentsDescriptionDataSet.createForexSwap();
  private static final ForexOptionVanilla FX_OPTION = ForexInstrumentsDescriptionDataSet.createForexOptionVanilla();
  private static final ForexOptionSingleBarrier FX_OPTION_SINGLE_BARRIER = ForexInstrumentsDescriptionDataSet.createForexOptionSingleBarrier();
  private static final ForexNonDeliverableForward NDF = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableForward();
  private static final ForexNonDeliverableOption NDO = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableOption();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @SuppressWarnings("synthetic-access")
  private static final MyAbstractVisitor<Object, String> VISITOR_ABSTRACT = new MyAbstractVisitor<Object, String>();

  @Test
  public void testVisitor() {
    final Object o = "G";
    assertEquals(FX.accept(VISITOR), "Forex1");
    assertEquals(FX.accept(VISITOR, o), "Forex2");
    assertEquals(FX_SWAP.accept(VISITOR), "ForexSwap1");
    assertEquals(FX_SWAP.accept(VISITOR, o), "ForexSwap2");
    assertEquals(FX_OPTION.accept(VISITOR), "ForexOptionVanilla1");
    assertEquals(FX_OPTION.accept(VISITOR, o), "ForexOptionVanilla2");
    assertEquals(FX_OPTION_SINGLE_BARRIER.accept(VISITOR), "ForexOptionSingleBarrier1");
    assertEquals(FX_OPTION_SINGLE_BARRIER.accept(VISITOR, o), "ForexOptionSingleBarrier2");
    assertEquals(NDF.accept(VISITOR), "ForexNonDeliverableForward1");
    assertEquals(NDF.accept(VISITOR, o), "ForexNonDeliverableForward2");
    assertEquals(NDO.accept(VISITOR), "ForexNonDeliverableOption1");
    assertEquals(NDO.accept(VISITOR, o), "ForexNonDeliverableOption2");
  }

  @Test
  public void testAbstractVisitorException() {
    final Object o = "G";
    testException(FX);
    testException(FX, o);
    testException(FX_SWAP);
    testException(FX_SWAP, o);
    testException(FX_OPTION);
    testException(FX_OPTION, o);
    testException(FX_OPTION_SINGLE_BARRIER);
    testException(FX_OPTION_SINGLE_BARRIER, o);
    testException(NDF);
    testException(NDF, o);
    testException(NDO);
    testException(NDO, o);
    final InstrumentDerivative[] forexArray = new InstrumentDerivative[] {FX, FX_SWAP};
    try {
      VISITOR_ABSTRACT.visit(forexArray[0]);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
    try {
      VISITOR_ABSTRACT.visit(forexArray);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
    try {
      VISITOR_ABSTRACT.visit(forexArray, o);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
  }

  private void testException(final InstrumentDerivative fx) {
    try {
      fx.accept(VISITOR_ABSTRACT);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
  }

  private void testException(final InstrumentDerivative fx, final Object o) {
    try {
      fx.accept(VISITOR_ABSTRACT, o);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
  }

  private static class MyVisitor<T, U> implements InstrumentDerivativeVisitor<T, String> {

    @Override
    public String visit(final InstrumentDerivative derivative, final T data) {
      return null;
    }

    @Override
    public String visit(final InstrumentDerivative derivative) {
      return null;
    }

    @Override
    public String[] visit(final InstrumentDerivative[] derivative, final T data) {
      return null;
    }

    @Override
    public String[] visit(final InstrumentDerivative[] derivative) {
      return null;
    }

    @Override
    public String visitForex(final Forex derivative, final T data) {
      return "Forex2";
    }

    @Override
    public String visitForex(final Forex derivative) {
      return "Forex1";
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative, final T data) {
      return "ForexSwap2";
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative) {
      return "ForexSwap1";
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative, final T data) {
      return "ForexOptionVanilla2";
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative) {
      return "ForexOptionVanilla1";
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final T data) {
      return "ForexOptionSingleBarrier2";
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
      return "ForexOptionSingleBarrier1";
    }

    @Override
    public String visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, T data) {
      return "ForexNonDeliverableForward2";
    }

    @Override
    public String visitForexNonDeliverableForward(ForexNonDeliverableForward derivative) {
      return "ForexNonDeliverableForward1";
    }

    @Override
    public String visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, T data) {
      return "ForexNonDeliverableOption2";
    }

    @Override
    public String visitForexNonDeliverableOption(ForexNonDeliverableOption derivative) {
      return "ForexNonDeliverableOption1";
    }

    @Override
    public String visitBondFixedSecurity(BondFixedSecurity bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(BondFixedTransaction bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(BondIborSecurity bond, T data) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(BondIborTransaction bond, T data) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity, T data) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, T data) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity, T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity, T data) {
      return null;
    }

    @Override
    public String visitSwap(Swap<?, ?> swap, T data) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(FixedCouponSwap<?> swap, T data) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(FixedFloatSwap swap, T data) {
      return null;
    }

    @Override
    public String visitOISSwap(OISSwap swap, T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, T data) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption, T data) {
      return null;
    }

    @Override
    public String visitTenorSwap(TenorSwap<? extends Payment> tenorSwap, T data) {
      return null;
    }

    @Override
    public String visitFloatingRateNote(FloatingRateNote frn, T data) {
      return null;
    }

    @Override
    public String visitCrossCurrencySwap(CrossCurrencySwap ccs, T data) {
      return null;
    }

    @Override
    public String visitForexForward(ForexForward fx, T data) {
      return null;
    }

    @Override
    public String visitCash(Cash cash, T data) {
      return null;
    }

    @Override
    public String visitBondFuture(BondFuture bondFuture, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFuture(InterestRateFuture future, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, T data) {
      return null;
    }

    @Override
    public String visitFixedPayment(PaymentFixed payment, T data) {
      return null;
    }

    @Override
    public String visitFixedCouponPayment(CouponFixed payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIbor payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborFixed(CouponIborFixed payment, T data) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(CouponIborGearing payment, T data) {
      return null;
    }

    @Override
    public String visitCouponOIS(CouponOIS payment, T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMS payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(CapFloorIbor payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMS payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMSSpread(CapFloorCMSSpread payment, T data) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(ForwardRateAgreement fra, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondFixedSecurity(BondFixedSecurity bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(BondFixedTransaction bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(BondIborSecurity bond) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(BondIborTransaction bond) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(GenericAnnuity<? extends Payment> genericAnnuity) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(AnnuityCouponIbor forwardLiborAnnuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity) {
      return null;
    }

    @Override
    public String visitSwap(Swap<?, ?> swap) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(FixedCouponSwap<?> swap) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(FixedFloatSwap swap) {
      return null;
    }

    @Override
    public String visitOISSwap(OISSwap swap) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitFloatingRateNote(FloatingRateNote frn) {
      return null;
    }

    @Override
    public String visitCrossCurrencySwap(CrossCurrencySwap ccs) {
      return null;
    }

    @Override
    public String visitForexForward(ForexForward fx) {
      return null;
    }

    @Override
    public String visitTenorSwap(TenorSwap<? extends Payment> tenorSwap) {
      return null;
    }

    @Override
    public String visitCash(Cash cash) {
      return null;
    }

    @Override
    public String visitBondFuture(BondFuture future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFuture future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option) {
      return null;
    }

    @Override
    public String visitFixedPayment(PaymentFixed payment) {
      return null;
    }

    @Override
    public String visitFixedCouponPayment(CouponFixed payment) {
      return null;
    }

    @Override
    public String visitCouponIborFixed(CouponIborFixed payment) {
      return null;
    }

    @Override
    public String visitCouponIbor(CouponIbor payment) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(CouponIborGearing payment) {
      return null;
    }

    @Override
    public String visitCouponOIS(CouponOIS payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(CouponCMS payment) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(CapFloorIbor payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMS payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMSSpread(CapFloorCMSSpread payment) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(ForwardRateAgreement fra) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond) {
      return null;
    }

  }

  private static class MyAbstractVisitor<T, U> extends AbstractInstrumentDerivativeVisitor<T, String> {

  }

}

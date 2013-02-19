/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborSpread;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TenorSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;

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
  private static final ForexOptionDigital FX_OPTION_DIGITAL = ForexInstrumentsDescriptionDataSet.createForexOptionDigital();

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
    assertEquals(FX_OPTION_DIGITAL.accept(VISITOR), "ForexOptionDigital1");
    assertEquals(FX_OPTION_DIGITAL.accept(VISITOR, o), "ForexOptionDigital2");
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
    testException(FX_OPTION_DIGITAL);
    testException(FX_OPTION_DIGITAL, o);
    final InstrumentDerivative[] forexArray = new InstrumentDerivative[] {FX, FX_SWAP};
    try {
      forexArray[0].accept(VISITOR_ABSTRACT);
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
    public String visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final T data) {
      return "ForexNonDeliverableForward2";
    }

    @Override
    public String visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
      return "ForexNonDeliverableForward1";
    }

    @Override
    public String visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final T data) {
      return "ForexNonDeliverableOption2";
    }

    @Override
    public String visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
      return "ForexNonDeliverableOption1";
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond, final T data) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond, final T data) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond, final T data) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond, final T data) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final T data) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final T data) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final T data) {
      return null;
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap, final T data) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final T data) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(final FixedFloatSwap swap, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final T data) {
      return null;
    }

    @Override
    public String visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final T data) {
      return null;
    }

    @Override
    public String visitFloatingRateNote(final FloatingRateNote frn, final T data) {
      return null;
    }

    @Override
    public String visitCrossCurrencySwap(final CrossCurrencySwap ccs, final T data) {
      return null;
    }

    @Override
    public String visitForexForward(final ForexForward fx, final T data) {
      return null;
    }

    @Override
    public String visitCash(final Cash cash, final T data) {
      return null;
    }

    @Override
    public String visitBondFuture(final BondFuture bondFuture, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFuture(final InterestRateFuture future, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final T data) {
      return null;
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFixed(final CouponFixed payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(final CouponIborSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponOIS(final CouponOIS payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final T data) {
      return null;
    }

    @Override
    public String visitBondFixedSecurity(final BondFixedSecurity bond) {
      return null;
    }

    @Override
    public String visitBondFixedTransaction(final BondFixedTransaction bond) {
      return null;
    }

    @Override
    public String visitBondIborSecurity(final BondIborSecurity bond) {
      return null;
    }

    @Override
    public String visitBondIborTransaction(final BondIborTransaction bond) {
      return null;
    }

    @Override
    public String visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
      return null;
    }

    @Override
    public String visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
      return null;
    }

    @Override
    public String visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
      return null;
    }

    @Override
    public String visitSwap(final Swap<?, ?> swap) {
      return null;
    }

    @Override
    public String visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
      return null;
    }

    @Override
    public String visitFixedFloatSwap(final FixedFloatSwap swap) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
      return null;
    }

    @Override
    public String visitFloatingRateNote(final FloatingRateNote frn) {
      return null;
    }

    @Override
    public String visitCrossCurrencySwap(final CrossCurrencySwap ccs) {
      return null;
    }

    @Override
    public String visitForexForward(final ForexForward fx) {
      return null;
    }

    @Override
    public String visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
      return null;
    }

    @Override
    public String visitCash(final Cash cash) {
      return null;
    }

    @Override
    public String visitBondFuture(final BondFuture future) {
      return null;
    }

    @Override
    public String visitInterestRateFuture(final InterestRateFuture future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
      return null;
    }

    @Override
    public String visitFixedPayment(final PaymentFixed payment) {
      return null;
    }

    @Override
    public String visitCouponFixed(final CouponFixed payment) {
      return null;
    }

    @Override
    public String visitCouponIborSpread(final CouponIborSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborGearing(final CouponIborGearing payment) {
      return null;
    }

    @Override
    public String visitCouponOIS(final CouponOIS payment) {
      return null;
    }

    @Override
    public String visitCouponCMS(final CouponCMS payment) {
      return null;
    }

    @Override
    public String visitCapFloorIbor(final CapFloorIbor payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(final CapFloorCMS payment) {
      return null;
    }

    @Override
    public String visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
      return null;
    }

    @Override
    public String visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
      return null;
    }

    @Override
    public String visitDepositIbor(final DepositIbor deposit, final T data) {
      return null;
    }

    @Override
    public String visitDepositIbor(final DepositIbor deposit) {
      return null;
    }

    @Override
    public String visitDepositCounterpart(final DepositCounterpart deposit, final T data) {
      return null;
    }

    @Override
    public String visitDepositCounterpart(final DepositCounterpart deposit) {
      return null;
    }

    @Override
    public String visitForexOptionDigital(final ForexOptionDigital derivative, final T data) {
      return "ForexOptionDigital2";
    }

    @Override
    public String visitForexOptionDigital(final ForexOptionDigital derivative) {
      return "ForexOptionDigital1";
    }

    @Override
    public String visitBillSecurity(final BillSecurity bill, final T data) {
      return null;
    }

    @Override
    public String visitBillSecurity(final BillSecurity bill) {
      return null;
    }

    @Override
    public String visitBillTransaction(final BillTransaction bill, final T data) {
      return null;
    }

    @Override
    public String visitBillTransaction(final BillTransaction bill) {
      return null;
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final T data) {
      return null;
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
      return null;
    }

    @Override
    public String visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final T data) {
      return null;
    }

    @Override
    public String visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
      return null;
    }

    @Override
    public String visitDepositZero(final DepositZero deposit, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitDepositZero(final DepositZero deposit) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final T data) {
      return null;
    }

    @Override
    public String visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
      return null;
    }

    @Override
    public String visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final T data) {
      return null;
    }

    @Override
    public String visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
      return null;
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIbor(final CouponIbor payment) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity, final T data) {
      return null;
    }

    @Override
    public String visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity) {
      return null;
    }

    @Override
    public String visitCouponIborCompounded(final CouponIborCompounded payment) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponIborCompounded(final CouponIborCompounded payment, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCDSDerivative(final ISDACDSDerivative cds, final T data) {
      return null;
    }

    @Override
    public String visitCDSDerivative(final ISDACDSDerivative cds) {
      return null;
    }

    @Override
    public String visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final T data) {
      return null;
    }

    @Override
    public String visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures) {
      return null;
    }

    @Override
    public String visitMetalForward(final MetalForward future, final T data) {
      return null;
    }

    @Override
    public String visitMetalForward(final MetalForward future) {
      return null;
    }

    @Override
    public String visitMetalFuture(final MetalFuture future, final T data) {
      return null;
    }

    @Override
    public String visitMetalFuture(final MetalFuture future) {
      return null;
    }

    @Override
    public String visitMetalFutureOption(final MetalFutureOption future, final T data) {
      return null;
    }

    @Override
    public String visitMetalFutureOption(final MetalFutureOption future) {
      return null;
    }

    @Override
    public String visitAgricultureForward(final AgricultureForward future, final T data) {
      return null;
    }

    @Override
    public String visitAgricultureForward(final AgricultureForward future) {
      return null;
    }

    @Override
    public String visitAgricultureFuture(final AgricultureFuture future, final T data) {
      return null;
    }

    @Override
    public String visitAgricultureFuture(final AgricultureFuture future) {
      return null;
    }

    @Override
    public String visitAgricultureFutureOption(final AgricultureFutureOption future, final T data) {
      return null;
    }

    @Override
    public String visitAgricultureFutureOption(final AgricultureFutureOption future) {
      return null;
    }

    @Override
    public String visitEnergyForward(final EnergyForward future, final T data) {
      return null;
    }

    @Override
    public String visitEnergyForward(final EnergyForward future) {
      return null;
    }

    @Override
    public String visitEnergyFuture(final EnergyFuture future, final T data) {
      return null;
    }

    @Override
    public String visitEnergyFuture(final EnergyFuture future) {
      return null;
    }

    @Override
    public String visitEnergyFutureOption(final EnergyFutureOption future, final T data) {
      return null;
    }

    @Override
    public String visitEnergyFutureOption(final EnergyFutureOption future) {
      return null;
    }

    @Override
    public String visitEquityFuture(final EquityFuture future) {
      return null;
    }

    @Override
    public String visitEquityFuture(final EquityFuture future, final T data) {
      return null;
    }

    @Override
    public String visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
      return null;
    }

    @Override
    public String visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final T data) {
      return null;
    }

    @Override
    public String visitEquityIndexOption(final EquityIndexOption option, final T data) {
      return null;
    }

    @Override
    public String visitEquityIndexOption(final EquityIndexOption option) {
      return null;
    }

    @Override
    public String visitVarianceSwap(final VarianceSwap varianceSwap) {
      return null;
    }

    @Override
    public String visitVarianceSwap(final VarianceSwap varianceSwap, final T data) {
      return null;
    }

    @Override
    public String visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
      return null;
    }

    @Override
    public String visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final T data) {
      return null;
    }

    @Override
    public String visitEquityOption(final EquityOption option, final T data) {
      return null;
    }

    @Override
    public String visitEquityOption(final EquityOption option) {
      return null;
    }

    @Override
    public String visitEquityIndexFutureOption(final EquityIndexFutureOption option, final T data) {
      return null;
    }

    @Override
    public String visitEquityIndexFutureOption(final EquityIndexFutureOption option) {
      return null;
    }

  }

  private static class MyAbstractVisitor<T, U> extends InstrumentDerivativeVisitorAdapter<T, String> {

  }

}

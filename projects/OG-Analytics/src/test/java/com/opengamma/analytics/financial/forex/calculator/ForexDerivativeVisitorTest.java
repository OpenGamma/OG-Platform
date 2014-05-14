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
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureTransaction;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.equity.Equity;
import com.opengamma.analytics.financial.equity.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
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
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolationWithMargin;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearMonthlyWithMargin;
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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.analytics.financial.volatilityswap.FXVolatilitySwap;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwap;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the visitor of Forex derivatives.
 */
@SuppressWarnings("deprecation")
@Test(groups = TestGroup.UNIT)
public class ForexDerivativeVisitorTest {

  private static final Forex FX = ForexInstrumentsDescriptionDataSet.createForexDeprecated();
  private static final ForexSwap FX_SWAP = ForexInstrumentsDescriptionDataSet.createForexSwapDeprecated();
  private static final ForexOptionVanilla FX_OPTION = ForexInstrumentsDescriptionDataSet.createForexOptionVanillaDeprecated();
  private static final ForexOptionSingleBarrier FX_OPTION_SINGLE_BARRIER = ForexInstrumentsDescriptionDataSet.createForexOptionSingleBarrierDeprecated();
  private static final ForexNonDeliverableForward NDF = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableForwardDeprecated();
  private static final ForexNonDeliverableOption NDO = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableOptionDeprecated();
  private static final ForexOptionDigital FX_OPTION_DIGITAL = ForexInstrumentsDescriptionDataSet.createForexOptionDigitalDeprecated();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object> VISITOR = new MyVisitor<>();

  @SuppressWarnings("synthetic-access")
  private static final MyAbstractVisitor<Object> VISITOR_ABSTRACT = new MyAbstractVisitor<>();

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
    testException(FX_OPTION_DIGITAL);
    testException(FX_OPTION_DIGITAL, o);
    final InstrumentDerivative[] forexArray = new InstrumentDerivative[] {FX, FX_SWAP };
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

  private static class MyVisitor<T> implements InstrumentDerivativeVisitor<T, String> {

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
    public String visitCash(final Cash cash, final T data) {
      return null;
    }

    @Override
    public String visitBondFuture(final BondFuture bondFuture, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final T data) {
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
    public String visitCouponOIS(final CouponON payment, final T data) {
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
    public String visitCash(final Cash cash) {
      return null;
    }

    @Override
    public String visitBondFuture(final BondFuture future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
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
    public String visitCouponOIS(final CouponON payment) {
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
      return null;
    }

    @Override
    public String visitDepositZero(final DepositZero deposit) {
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
    public String visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment, final T data) {
      return null;
    }

    @Override
    public String visitInterpolatedStubCoupon(final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment) {
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
    public String visitCouponIborCompounding(final CouponIborCompounding payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompounding(final CouponIborCompounding payment, final T data) {
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
    public String visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final T data) {
      return null;
    }

    @Override
    public String visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures) {
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
    public String visitVolatilitySwap(final VolatilitySwap volatilitySwap) {
      return null;
    }

    @Override
    public String visitVolatilitySwap(final VolatilitySwap volatilitySwap, final T data) {
      return null;
    }

    @Override
    public String visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap) {
      return null;
    }

    @Override
    public String visitFXVolatilitySwap(final FXVolatilitySwap volatilitySwap, final T data) {
      return null;
    }

    @Override
    public String visitEquityOption(final EquityOption option, final T data) {
      return null;
    }

    @Override
    public String visitForexForward(final ForexForward fx) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverage(final CouponIborAverage payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponIborAverage(final CouponIborAverage payment) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthly(final CouponInflationYearOnYearMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolation(final CouponInflationYearOnYearInterpolation coupon) {
      return null;
    }

    @Override
    public String visitEquityOption(final EquityOption option) {
      return null;
    }

    @Override
    public String visitForexForward(final ForexForward fx, final T data) {
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

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
      return null;
    }

    @Override
    public String visitCouponFixedCompounding(final CouponFixedCompounding payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFixedCompounding(final CouponFixedCompounding payment) {
      return null;
    }

    @Override
    public String visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap, final T data) {
      return null;
    }

    @Override
    public String visitFixedCompoundingCouponSwap(final SwapFixedCompoundingCoupon<?> swap) {
      return null;
    }

    @Override
    public String visitCashSettledFuture(final CashSettledFuture future, final T data) {
      return null;
    }

    @Override
    public String visitCashSettledFuture(final CashSettledFuture future) {
      return null;
    }

    @Override
    public String visitIndexFuture(final IndexFuture future, final T data) {
      return null;
    }

    @Override
    public String visitIndexFuture(final IndexFuture future) {
      return null;
    }

    @Override
    public String visitEquityIndexFuture(final EquityIndexFuture future, final T data) {
      return null;
    }

    @Override
    public String visitEquityIndexFuture(final EquityIndexFuture future) {
      return null;
    }

    @Override
    public String visitVolatilityIndexFuture(final VolatilityIndexFuture future, final T data) {
      return null;
    }

    @Override
    public String visitVolatilityIndexFuture(final VolatilityIndexFuture future) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverage(final CouponONArithmeticAverage payment) {
      return null;
    }

    @Override
    public String visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final T data) {
      return null;
    }

    @Override
    public String visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponInterpolation(final CapFloorInflationZeroCouponInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationZeroCouponMonthly(final CapFloorInflationZeroCouponMonthly coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation coupon) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon, final T data) {
      return null;
    }

    @Override
    public String visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly coupon) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified payment) {
      return null;
    }

    @Override
    public String visitBondFuturesSecurity(final BondFuturesSecurity bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesSecurity(final BondFuturesSecurity bondFutures) {
      return null;
    }

    @Override
    public String visitBondFuturesTransaction(final BondFuturesTransaction bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesTransaction(final BondFuturesTransaction bondFutures) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMargin coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon, final T data) {
      return null;
    }

    @Override
    public String visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMargin coupon) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedSecurity(final BondInterestIndexedSecurity<?, ?> bond, final T data) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond, final T data) {
      return null;
    }

    @Override
    public String visitBondInterestIndexedTransaction(final BondInterestIndexedTransaction<?, ?> bond) {
      return null;
    }

    @Override
    public String visitCouponONSpread(final CouponONSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONSpread(final CouponONSpread payment) {
      return null;
    }

    @Override
    public String visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
      return null;
    }

    @Override
    public String visitCouponONCompounded(final CouponONCompounded payment, final T data) {
      return null;
    }

    @Override
    public String visitCouponONCompounded(final CouponONCompounded payment) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption, final T data) {
      return null;
    }

    @Override
    public String visitSwaptionCashFixedCompoundedONCompounded(final SwaptionCashFixedCompoundedONCompounded swaption) {
      return null;
    }

    @Override
    public String visitSwaptionPhysicalFixedCompoundedONCompounded(final SwaptionPhysicalFixedCompoundedONCompounded swaption) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment, final T data) {
      return null;
    }

    @Override
    public String visitSwapMultileg(final SwapMultileg swap, final T data) {
      return null;
    }

    @Override
    public String visitSwapMultileg(final SwapMultileg swap) {
      return null;
    }

    @Override
    public String visitMetalFutureSecurity(final MetalFutureSecurity future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitMetalFutureSecurity(final MetalFutureSecurity future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureSecurity(final EnergyFutureSecurity future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureSecurity(final EnergyFutureSecurity future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitMetalFutureTransaction(final MetalFutureTransaction future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitMetalFutureTransaction(final MetalFutureTransaction future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureTransaction(final EnergyFutureTransaction future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitEnergyFutureTransaction(final EnergyFutureTransaction future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureTransaction(final AgricultureFutureTransaction future, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitAgricultureFutureTransaction(final AgricultureFutureTransaction future) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityCashSettle(final CouponCommodityCashSettle coupon) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitCouponCommodityPhysicalSettle(final CouponCommodityPhysicalSettle coupon) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward, final T data) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesYieldAverageSecurity(final BondFuturesYieldAverageSecurity bondFutures) {
      return null;
    }

    @Override
    public String visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction bondFutures, final T data) {
      return null;
    }

    @Override
    public String visitYieldAverageBondFuturesTransaction(final BondFuturesYieldAverageTransaction bondFutures) {
      return null;
    }

    @Override
    public String visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap) {
      return null;
    }

    @Override
    public String visitTotalReturnSwap(final TotalReturnSwap totalReturnSwap, final T data) {
      return null;
    }

    @Override
    public String visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap) {
      return null;
    }

    @Override
    public String visitBondTotalReturnSwap(final BondTotalReturnSwap totalReturnSwap, final T data) {
      return null;
    }

    @Override
    public String visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap) {
      return null;
    }

    @Override
    public String visitEquityTotalReturnSwap(final EquityTotalReturnSwap totalReturnSwap, final T data) {
      return null;
    }

    @Override
    public String visitEquity(final Equity equity) {
      return null;
    }

    @Override
    public String visitEquity(final Equity equity, final T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, T data) {
      return null;
    }

    @Override
    public String visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSimpleSpread(CouponIborCompoundingSimpleSpread payment) {
      return null;
    }

    @Override
    public String visitCouponIborCompoundingSimpleSpread(CouponIborCompoundingSimpleSpread payment, T data) {
      return null;
    }

  }

  private static class MyAbstractVisitor<T> extends InstrumentDerivativeVisitorAdapter<T, String> {

  }

}

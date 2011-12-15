/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.financial.instrument.cash.DepositDefinition;
import com.opengamma.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class testing the Fixed income instrument definition visitor.
 */
public class InstrumentDefinitionVisitorTest {
  private static final Currency CUR = Currency.USD;
  private static final DayCount DC = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar C = new MondayToFridayCalendar("F");
  private static final Convention CONVENTION = new Convention(2, DC, BD, C, "A");
  private static final CashDefinition CASH = new CashDefinition(CUR, DateUtils.getUTCDate(2011, 1, 1), 1, 0.04, CONVENTION);
  private static final ZonedDateTime SETTLE_DATE = DateUtils.getUTCDate(2011, 1, 1);
  private static final Period TENOR = Period.ofYears(2);
  private static final Period FIXED_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final boolean IS_EOM = true;
  private static final double NOTIONAL = 100000000; //100m
  private static final double FIXED_RATE = 0.05;
  private static final boolean IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition ANNUITY_FIXED = AnnuityCouponFixedDefinition.from(CUR, SETTLE_DATE, TENOR, FIXED_PERIOD, C, FIXED_DAY_COUNT, BD, IS_EOM, NOTIONAL, FIXED_RATE,
      IS_PAYER);
  private static final Period IBOR_PERIOD_1 = Period.ofMonths(3);
  private static final int SPOT_LAG = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final IborIndex IBOR_INDEX_1 = new IborIndex(CUR, IBOR_PERIOD_1, SPOT_LAG, C, IBOR_DAY_COUNT, BD, IS_EOM);
  private static final IndexSwap CMS_INDEX = new IndexSwap(IBOR_PERIOD_1, IBOR_DAY_COUNT, IBOR_INDEX_1, IBOR_PERIOD_1);
  private static final AnnuityCouponIborDefinition ANNUITY_IBOR = AnnuityCouponIborDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, !IS_PAYER);
  private static final Period IBOR_PERIOD_2 = Period.ofMonths(6);
  private static final IborIndex IBOR_INDEX_2 = new IborIndex(CUR, IBOR_PERIOD_2, SPOT_LAG, C, IBOR_DAY_COUNT, BD, IS_EOM);
  private static final double SPREAD = 0.001;
  private static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_1 = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_2, SPREAD, !IS_PAYER);
  private static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_2 = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, 0.0, IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_FIXED_IBOR = new SwapFixedIborDefinition(ANNUITY_FIXED, ANNUITY_IBOR);
  private static final SwapFixedIborSpreadDefinition SWAP_FIXED_IBOR_SPREAD = new SwapFixedIborSpreadDefinition(ANNUITY_FIXED, ANNUITY_IBOR_SPREAD_1);
  private static final SwapIborIborDefinition SWAP_IBOR_IBOR = new SwapIborIborDefinition(ANNUITY_IBOR_SPREAD_2, ANNUITY_IBOR_SPREAD_1);
  private static final AnnuityDefinition<PaymentFixedDefinition> GENERAL_ANNUITY = new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[] {
      new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2011, 1, 1), 1000), new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2012, 1, 1), 1000)});
  private static final CouponFloatingDefinition COUPON_FLOATING = new CouponFloatingDefinition(CUR, SETTLE_DATE.plusMonths(3), SETTLE_DATE, SETTLE_DATE.plusMonths(3), 0.25, NOTIONAL, SETTLE_DATE) {

    @Override
    public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
      return null;
    }

    @Override
    public Payment toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
      return null;
    }

  };
  private static final CouponCMSDefinition COUPON_CMS = CouponCMSDefinition.from(CouponIborDefinition.from(1000, SETTLE_DATE, IBOR_INDEX_1), CMS_INDEX);
  private static final AnnuityCouponCMSDefinition ANNUITY_COUPON_CMS = new AnnuityCouponCMSDefinition(new CouponCMSDefinition[] {COUPON_CMS});

  private static final InterestRateFutureDefinition IR_FUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurityDefinition();
  private static final BondFutureDefinition BNDFUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  private static final SwaptionCashFixedIborDefinition SWAPTION_CASH = SwaptionInstrumentsDescriptionDataSet.createSwaptionCashFixedIborDefinition();
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @Test
  public void test() {
    final Object o = "G";
    assertEquals(CASH.accept(VISITOR), "Cash2");
    assertEquals(CASH.accept(VISITOR, o), "Cash1");
    assertEquals(ANNUITY_FIXED.accept(VISITOR), "Annuity2");
    assertEquals(ANNUITY_FIXED.accept(VISITOR, o), "Annuity1");
    assertEquals(ANNUITY_IBOR.accept(VISITOR), "Annuity2");
    assertEquals(ANNUITY_IBOR.accept(VISITOR, o), "Annuity1");
    assertEquals(ANNUITY_IBOR_SPREAD_1.accept(VISITOR), "Annuity2");
    assertEquals(ANNUITY_IBOR_SPREAD_1.accept(VISITOR, o), "Annuity1");
    assertEquals(SWAP_FIXED_IBOR.accept(VISITOR), "SwapFixedIbor2");
    assertEquals(SWAP_FIXED_IBOR.accept(VISITOR, o), "SwapFixedIbor1");
    assertEquals(SWAP_FIXED_IBOR_SPREAD.accept(VISITOR), "SwapFixedIborSpread2");
    assertEquals(SWAP_FIXED_IBOR_SPREAD.accept(VISITOR, o), "SwapFixedIborSpread1");
    assertEquals(SWAP_IBOR_IBOR.accept(VISITOR), "SwapIborIbor2");
    assertEquals(SWAP_IBOR_IBOR.accept(VISITOR, o), "SwapIborIbor1");
    assertEquals(GENERAL_ANNUITY.accept(VISITOR), "Annuity2");
    assertEquals(GENERAL_ANNUITY.accept(VISITOR, o), "Annuity1");
    assertEquals(COUPON_FLOATING.accept(VISITOR), "CouponFloating2");
    assertEquals(COUPON_FLOATING.accept(VISITOR, o), "CouponFloating1");
    assertEquals(COUPON_CMS.accept(VISITOR), "CouponCMS2");
    assertEquals(COUPON_CMS.accept(VISITOR, o), "CouponCMS1");
    assertEquals(ANNUITY_COUPON_CMS.accept(VISITOR), "Annuity2");
    assertEquals(ANNUITY_COUPON_CMS.accept(VISITOR, o), "Annuity1");
    assertEquals(IR_FUT_SECURITY_DEFINITION.accept(VISITOR), "InterestRateFutureSecurity1");
    assertEquals(IR_FUT_SECURITY_DEFINITION.accept(VISITOR, o), "InterestRateFutureSecurity2");
    assertEquals(BNDFUT_SECURITY_DEFINITION.accept(VISITOR), "BondFutureSecurity1");
    assertEquals(BNDFUT_SECURITY_DEFINITION.accept(VISITOR, o), "BondFutureSecurity2");
    assertEquals(SWAPTION_CASH.accept(VISITOR), "SwaptionCashFixedIbor1");
    assertEquals(SWAPTION_CASH.accept(VISITOR, o), "SwaptionCashFixedIbor2");
    assertEquals(SWAPTION_PHYS.accept(VISITOR), "SwaptionPhysicalFixedIbor1");
    assertEquals(SWAPTION_PHYS.accept(VISITOR, o), "SwaptionPhysicalFixedIbor2");
  }

  private static class MyVisitor<T, U> implements InstrumentDefinitionVisitor<T, String> {

    @Override
    public String visit(final InstrumentDefinition<?> definition, final T data) {
      return definition.accept(this, data);
    }

    @Override
    public String visit(final InstrumentDefinition<?> definition) {
      return definition.accept(this);
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash, final T data) {
      return "Cash1";
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash) {
      return "Cash2";
    }

    @Override
    public String visitPaymentFixed(final PaymentFixedDefinition payment, final T data) {
      return "PaymentFixed1";
    }

    @Override
    public String visitPaymentFixed(final PaymentFixedDefinition payment) {
      return "PaymentFixed2";
    }

    @Override
    public String visitCouponFixed(final CouponFixedDefinition payment, final T data) {
      return "CouponFixed1";
    }

    @Override
    public String visitCouponFixed(final CouponFixedDefinition payment) {
      return "CouponFixed2";
    }

    @Override
    public String visitCouponIbor(final CouponIborDefinition payment, final T data) {
      return "CouponIbor1";
    }

    @Override
    public String visitCouponIbor(final CouponIborDefinition payment) {
      return "CouponIbor2";
    }

    @Override
    public String visitCouponCMS(final CouponCMSDefinition payment, final T data) {
      return "CouponCMS1";
    }

    @Override
    public String visitCouponCMS(final CouponCMSDefinition payment) {
      return "CouponCMS2";
    }

    @Override
    public String visitCouponIborSpread(final CouponIborDefinition payment, final T data) {
      return "CouponIborSpread1";
    }

    @Override
    public String visitCouponIborSpread(final CouponIborDefinition payment) {
      return "CouponIborSpread2";
    }

    @Override
    public String visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final T data) {
      return "Annuity1";
    }

    @Override
    public String visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
      return "Annuity2";
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap, final T data) {
      return "Swap1";
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap) {
      return "Swap2";
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final T data) {
      return "SwapFixedIbor1";
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
      return "SwapFixedIbor2";
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final T data) {
      return "SwapFixedIborSpread1";
    }

    @Override
    public String visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
      return "SwapFixedIborSpread2";
    }

    @Override
    public String visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final T data) {
      return "SwapIborIbor1";
    }

    @Override
    public String visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
      return "SwapIborIbor2";
    }

    @Override
    public String visitCouponFloating(final CouponFloatingDefinition payment, final T data) {
      return "CouponFloating1";
    }

    @Override
    public String visitCouponFloating(final CouponFloatingDefinition payment) {
      return "CouponFloating2";
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final T data) {
      return "ForwardRateAgreement1";
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
      return "ForwardRateAgreement2";
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future, final T data) {
      return "InterestRateFutureSecurity2";
    }

    @Override
    public String visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future) {
      return "InterestRateFutureSecurity1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final T data) {
      return "InterestRateFutureOptionPremiumSecurity1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
      return "InterestRateFutureOptionPremiumSecurity2";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final T data) {
      return "InterestRateFutureOptionPremiumTransaction1";
    }

    @Override
    public String visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
      return "InterestRateFutureOptionPremiumTransaction2";
    }

    @Override
    public String visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final T data) {
      return "BondFixedTransaction1";
    }

    @Override
    public String visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
      return "BondFixedTransaction2";
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final T data) {
      return "BondFixedSecurity1";
    }

    @Override
    public String visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
      return "BondFixedSecurity2";
    }

    @Override
    public String visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final T data) {
      return "BondIborTransaction1";
    }

    @Override
    public String visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
      return "BondIborTransaction2";
    }

    @Override
    public String visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final T data) {
      return "BondIborSecurity1";
    }

    @Override
    public String visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
      return "BondIborSecurity2";
    }

    @Override
    public String visitBondFutureSecurityDefinition(final BondFutureDefinition bond, final T data) {
      return "BondFutureSecurity2";
    }

    @Override
    public String visitBondFutureSecurityDefinition(final BondFutureDefinition bond) {
      return "BondFutureSecurity1";
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final T data) {
      return "SwaptionCashFixedIbor2";
    }

    @Override
    public String visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
      return "SwaptionCashFixedIbor1";
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final T data) {
      return "SwaptionPhysicalFixedIbor2";
    }

    @Override
    public String visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
      return "SwaptionPhysicalFixedIbor1";
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final T data) {
      return null;
    }

    @Override
    public String visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
      return null;
    }

    @Override
    public String visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption, T data) {
      return "SwaptionBermudaFixedIbor2";
    }

    @Override
    public String visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption) {
      return "SwaptionBermudaFixedIbor1";
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMSDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCapFloorCMS(CapFloorCMSDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond, T data) {
      return null;
    }

    @Override
    public String visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon, T data) {
      return null;
    }

    @Override
    public String visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
      return null;
    }

    @Override
    public String visitCouponOISSimplified(CouponOISSimplifiedDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponOISSimplified(CouponOISSimplifiedDefinition payment) {
      return null;
    }

    @Override
    public String visitCouponOIS(CouponOISDefinition payment, T data) {
      return null;
    }

    @Override
    public String visitCouponOIS(CouponOISDefinition payment) {
      return null;
    }

    @Override
    public String visitForexDefinition(ForexDefinition fx, T data) {
      return null;
    }

    @Override
    public String visitForexDefinition(ForexDefinition fx) {
      return null;
    }

    @Override
    public String visitForexSwapDefinition(ForexSwapDefinition fx, T data) {
      return null;
    }

    @Override
    public String visitForexSwapDefinition(ForexSwapDefinition fx) {
      return null;
    }

    @Override
    public String visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx, T data) {
      return null;
    }

    @Override
    public String visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx) {
      return null;
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx, T data) {
      return null;
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx) {
      return null;
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf, T data) {
      return null;
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf) {
      return null;
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo, T data) {
      return null;
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo) {
      return null;
    }

    @Override
    public String visitDepositDefinition(DepositDefinition deposit, T data) {
      return null;
    }

    @Override
    public String visitDepositDefinition(DepositDefinition deposit) {
      return null;
    }

    @Override
    public String visitDepositIborDefinition(DepositIborDefinition deposit, T data) {
      return null;
    }

    @Override
    public String visitDepositIborDefinition(DepositIborDefinition deposit) {
      return null;
    }

    @Override
    public String visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit, T data) {
      return null;
    }

    @Override
    public String visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit) {
      return null;
    }
  }
}

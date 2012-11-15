/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Set;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.collections.Sets;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class TestInstrumentDefinitions {
  public static final Currency CUR = Currency.USD;
  public static final BusinessDayConvention BD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  public static final Calendar C = new MondayToFridayCalendar("F");
  public static final CashDefinition CASH = new CashDefinition(CUR, DateUtils.getUTCDate(2011, 1, 2), DateUtils.getUTCDate(2012, 1, 2), 1.0, 0.04, 1.0);
  public static final ZonedDateTime SETTLE_DATE = DateUtils.getUTCDate(2011, 1, 1);
  public static final Period TENOR = Period.ofYears(2);
  public static final Period FIXED_PERIOD = Period.ofMonths(6);
  public static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  public static final boolean IS_EOM = true;
  public static final double NOTIONAL = 100000000; //100m
  public static final double FIXED_RATE = 0.05;
  public static final boolean IS_PAYER = true;
  public static final AnnuityCouponFixedDefinition ANNUITY_FIXED = AnnuityCouponFixedDefinition.from(CUR, SETTLE_DATE, TENOR, FIXED_PERIOD, C, FIXED_DAY_COUNT, BD,
      IS_EOM, NOTIONAL, FIXED_RATE, IS_PAYER);
  public static final Period IBOR_PERIOD_1 = Period.ofMonths(3);
  public static final int SPOT_LAG = 2;
  public static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  public static final IborIndex IBOR_INDEX_1 = new IborIndex(CUR, IBOR_PERIOD_1, SPOT_LAG, C, IBOR_DAY_COUNT, BD, IS_EOM);
  public static final IndexSwap CMS_INDEX = new IndexSwap(IBOR_PERIOD_1, IBOR_DAY_COUNT, IBOR_INDEX_1, IBOR_PERIOD_1);
  public static final AnnuityCouponIborDefinition ANNUITY_IBOR = AnnuityCouponIborDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, !IS_PAYER);
  public static final Period IBOR_PERIOD_2 = Period.ofMonths(6);
  public static final IborIndex IBOR_INDEX_2 = new IborIndex(CUR, IBOR_PERIOD_2, SPOT_LAG, C, IBOR_DAY_COUNT, BD, IS_EOM);
  public static final double SPREAD = 0.001;
  public static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_1 = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_2,
      SPREAD, !IS_PAYER);
  public static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_2 = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, 0.0,
      IS_PAYER);
  public static final SwapFixedIborDefinition SWAP_FIXED_IBOR = new SwapFixedIborDefinition(ANNUITY_FIXED, ANNUITY_IBOR);
  public static final SwapFixedIborSpreadDefinition SWAP_FIXED_IBOR_SPREAD = new SwapFixedIborSpreadDefinition(ANNUITY_FIXED, ANNUITY_IBOR_SPREAD_1);
  public static final SwapIborIborDefinition SWAP_IBOR_IBOR = new SwapIborIborDefinition(ANNUITY_IBOR_SPREAD_2, ANNUITY_IBOR_SPREAD_1);
  public static final AnnuityDefinition<PaymentFixedDefinition> GENERAL_ANNUITY = new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[] {
      new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2011, 1, 1), 1000), new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2012, 1, 1), 1000)});
  public static final CouponCMSDefinition COUPON_CMS = CouponCMSDefinition.from(CouponIborDefinition.from(1000, SETTLE_DATE, IBOR_INDEX_1), CMS_INDEX);
  public static final AnnuityCouponCMSDefinition ANNUITY_COUPON_CMS = new AnnuityCouponCMSDefinition(new CouponCMSDefinition[] {COUPON_CMS});

  public static final InterestRateFutureDefinition IR_FUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurityDefinition();
  public static final InterestRateFutureOptionMarginSecurityDefinition IR_FUT_OPT_MARGIN_SEC_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionMarginSecurityDefinition();
  public static final InterestRateFutureOptionMarginTransactionDefinition IR_FUT_OPT_MARGIN_T_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionMarginTransactionDefinition();
  public static final InterestRateFutureOptionPremiumSecurityDefinition IR_FUT_OPT_PREMIUM_SEC_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionPremiumSecurityDefinition();
  public static final InterestRateFutureOptionPremiumTransactionDefinition IR_FUT_OPT_PREMIUM_T_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionPremiumTransactionDefinition();
  public static final BondFutureDefinition BNDFUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  public static final SwaptionCashFixedIborDefinition SWAPTION_CASH = SwaptionInstrumentsDescriptionDataSet.createSwaptionCashFixedIborDefinition();
  public static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition();
  public static final PaymentFixedDefinition PAYMENT_FIXED = new PaymentFixedDefinition(CUR, SETTLE_DATE, NOTIONAL);
  private static final Set<InstrumentDefinition<?>> ALL_INSTRUMENTS = Sets.newHashSet();

  static {
    ALL_INSTRUMENTS.add(ANNUITY_FIXED);
    ALL_INSTRUMENTS.add(ANNUITY_IBOR);
    ALL_INSTRUMENTS.add(ANNUITY_COUPON_CMS);
    ALL_INSTRUMENTS.add(ANNUITY_IBOR_SPREAD_1);
    ALL_INSTRUMENTS.add(ANNUITY_IBOR_SPREAD_2);
    ALL_INSTRUMENTS.add(BNDFUT_SECURITY_DEFINITION);
    ALL_INSTRUMENTS.add(CASH);
    ALL_INSTRUMENTS.add(COUPON_CMS);
    ALL_INSTRUMENTS.add(GENERAL_ANNUITY);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_MARGIN_SEC_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_MARGIN_T_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_PREMIUM_SEC_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_PREMIUM_T_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_SECURITY_DEFINITION);
    ALL_INSTRUMENTS.add(PAYMENT_FIXED);
    ALL_INSTRUMENTS.add(SWAP_IBOR_IBOR);
    ALL_INSTRUMENTS.add(SWAP_FIXED_IBOR);
    ALL_INSTRUMENTS.add(SWAP_FIXED_IBOR_SPREAD);
    ALL_INSTRUMENTS.add(SWAPTION_CASH);
    ALL_INSTRUMENTS.add(SWAPTION_PHYS);
  }

  public static Set<InstrumentDefinition<?>> getAllInstruments() {
    return ALL_INSTRUMENTS;
  }
}

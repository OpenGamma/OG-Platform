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

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.AgricultureForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureOptionDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.DeliverableSwapFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class TestInstrumentDefinitions {
  public static final Currency CUR = Currency.USD;
  public static final BusinessDayConvention BD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  public static final Calendar C = new MondayToFridayCalendar("F");
  public static final ZonedDateTime SETTLE_DATE = DateUtils.getUTCDate(2011, 1, 1);
  public static final Period TENOR = Period.ofYears(2);
  public static final Period FIXED_PERIOD = Period.ofMonths(6);
  public static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  public static final boolean IS_EOM = true;
  public static final double NOTIONAL = 100000000; //100m
  public static final double FIXED_RATE = 0.05;
  public static final boolean IS_PAYER = true;
  public static final Period IBOR_PERIOD_1 = Period.ofMonths(3);
  public static final int SPOT_LAG = 2;
  public static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  public static final IborIndex IBOR_INDEX_1 = new IborIndex(CUR, IBOR_PERIOD_1, SPOT_LAG, C, IBOR_DAY_COUNT, BD, IS_EOM);
  public static final IndexON INDEX_ON = new IndexON("A", CUR, FIXED_DAY_COUNT, 0, C);
  public static final IndexSwap CMS_INDEX = new IndexSwap(IBOR_PERIOD_1, IBOR_DAY_COUNT, IBOR_INDEX_1, IBOR_PERIOD_1);
  public static final Period IBOR_PERIOD_2 = Period.ofMonths(6);
  public static final IborIndex IBOR_INDEX_2 = new IborIndex(CUR, IBOR_PERIOD_2, SPOT_LAG, C, IBOR_DAY_COUNT, BD, IS_EOM);
  public static final double SPREAD = 0.001;

  public static final CouponCMSDefinition COUPON_CMS = CouponCMSDefinition.from(CouponIborDefinition.from(1000, SETTLE_DATE, IBOR_INDEX_1), CMS_INDEX);
  public static final PaymentFixedDefinition PAYMENT_FIXED = new PaymentFixedDefinition(CUR, SETTLE_DATE, NOTIONAL);

  public static final DepositCounterpartDefinition DEPOSIT_COUNTERPART = new DepositCounterpartDefinition(CUR, SETTLE_DATE, SETTLE_DATE.plusDays(3), NOTIONAL, FIXED_RATE, FIXED_RATE, "a");
  public static final DepositIborDefinition DEPOSIT_IBOR = DepositIborDefinition.fromStart(SETTLE_DATE, NOTIONAL, FIXED_RATE, IBOR_INDEX_1);
  public static final DepositZeroDefinition DEPOSIT_ZERO = DepositZeroDefinition.from(CUR, SETTLE_DATE, SETTLE_DATE.plusDays(3), FIXED_DAY_COUNT, new ContinuousInterestRate(0.03));

  public static final AnnuityCouponCMSDefinition ANNUITY_COUPON_CMS = new AnnuityCouponCMSDefinition(new CouponCMSDefinition[] {COUPON_CMS});
  public static final AnnuityCouponFixedDefinition ANNUITY_FIXED = AnnuityCouponFixedDefinition.from(CUR, SETTLE_DATE, TENOR, FIXED_PERIOD, C, FIXED_DAY_COUNT, BD, IS_EOM, NOTIONAL, FIXED_RATE, IS_PAYER);
  public static final AnnuityCouponFixedDefinition ANNUITY_FIXED_UNIT_NOTIONAL = AnnuityCouponFixedDefinition.from(CUR, SETTLE_DATE, TENOR, FIXED_PERIOD, C, FIXED_DAY_COUNT, BD, IS_EOM, 1, FIXED_RATE, !IS_PAYER);
  public static final AnnuityCouponIborDefinition ANNUITY_IBOR = AnnuityCouponIborDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, !IS_PAYER);
  public static final AnnuityCouponIborDefinition ANNUITY_IBOR_UNIT_NOTIONAL = AnnuityCouponIborDefinition.from(SETTLE_DATE, TENOR, 1, IBOR_INDEX_1, IS_PAYER);
  public static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_RECEIVE = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_2, SPREAD, !IS_PAYER);
  public static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_PAY = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, 0.0, IS_PAYER);
  public static final AnnuityDefinition<PaymentFixedDefinition> GENERAL_ANNUITY = new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2011, 1, 1), 1000), new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2012, 1, 1), 1000)});

  public static final BillSecurityDefinition BILL_SECURITY = new BillSecurityDefinition(CUR, SETTLE_DATE.plusYears(1), NOTIONAL, 0, C, SimpleYieldConvention.BANK_OF_CANADA, FIXED_DAY_COUNT, "");
  public static final BillTransactionDefinition BILL_TRANSACTION = new BillTransactionDefinition(BILL_SECURITY, 100, SETTLE_DATE, -100);

  public static final BondFixedSecurityDefinition BOND_FIXED_SECURITY = BondFixedSecurityDefinition.from(CUR, SETTLE_DATE.plusYears(2), SETTLE_DATE, FIXED_PERIOD, FIXED_RATE, SPOT_LAG, C, FIXED_DAY_COUNT, BD, SimpleYieldConvention.DISCOUNT, IS_EOM);
  public static final BondFixedTransactionDefinition BOND_FIXED_TRANSACTION = new BondFixedTransactionDefinition(BOND_FIXED_SECURITY, 100, SETTLE_DATE, -100);
  public static final BondFutureDefinition BNDFUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  public static final BondFutureOptionPremiumSecurityDefinition BFO_SECURITY = FutureInstrumentsDescriptionDataSet.createBondFutureOptionPremiumSecurityDefinition();
  public static final BondFutureOptionPremiumTransactionDefinition BFO_TRANSACTION = new BondFutureOptionPremiumTransactionDefinition(BFO_SECURITY, -100, BFO_SECURITY.getExpirationDate().minusMonths(3), 100);

  public static final CashDefinition CASH = new CashDefinition(CUR, DateUtils.getUTCDate(2011, 1, 2), DateUtils.getUTCDate(2012, 1, 2), 1.0, 0.04, 1.0);
  public static final ForwardRateAgreementDefinition FRA = ForwardRateAgreementDefinition.from(SETTLE_DATE, SETTLE_DATE.plusMonths(3), NOTIONAL, IBOR_INDEX_1, FIXED_RATE);
  public static final FederalFundsFutureSecurityDefinition FF_SECURITY = FederalFundsFutureSecurityDefinition.from(SETTLE_DATE, INDEX_ON, NOTIONAL, 0.25, "a");
  public static final FederalFundsFutureTransactionDefinition FF_TRANSACTION = new FederalFundsFutureTransactionDefinition(FF_SECURITY, 100, SETTLE_DATE, 0.97);

  public static final AgricultureForwardDefinition AG_FWD = AgricultureForwardDefinition.withCashSettlement(SETTLE_DATE.plusYears(1), ExternalId.of("a", "b"), 100, NOTIONAL, "tonnes", 76, CUR, SETTLE_DATE);
  public static final AgricultureFutureDefinition AG_FUTURE = AgricultureFutureDefinition.withPhysicalSettlement(SETTLE_DATE, ExternalId.of("a", "b"), 100, SETTLE_DATE, SETTLE_DATE, NOTIONAL, "tonnes", 100, CUR, SETTLE_DATE.minusYears(1));
  public static final AgricultureFutureOptionDefinition AG_FUTURE_OPTION = new AgricultureFutureOptionDefinition(SETTLE_DATE, AG_FUTURE, 100, ExerciseDecisionType.AMERICAN, true);
  public static final EnergyForwardDefinition ENERGY_FWD = EnergyForwardDefinition.withCashSettlement(SETTLE_DATE.plusYears(1), ExternalId.of("a", "b"), 100, NOTIONAL, "watts", 76, CUR, SETTLE_DATE);
  public static final EnergyFutureDefinition ENERGY_FUTURE = EnergyFutureDefinition.withPhysicalSettlement(SETTLE_DATE, ExternalId.of("a", "b"), 100, SETTLE_DATE, SETTLE_DATE, NOTIONAL, "tonnes", 100, CUR, SETTLE_DATE.minusYears(1));
  public static final EnergyFutureOptionDefinition ENERGY_FUTURE_OPTION = new EnergyFutureOptionDefinition(SETTLE_DATE, ENERGY_FUTURE, 100, ExerciseDecisionType.AMERICAN, true);
  public static final MetalForwardDefinition METAL_FWD = MetalForwardDefinition.withCashSettlement(SETTLE_DATE.plusYears(1), ExternalId.of("a", "b"), 100, NOTIONAL, "troy oz", 1776, CUR, SETTLE_DATE);
  public static final MetalFutureDefinition METAL_FUTURE = MetalFutureDefinition.withPhysicalSettlement(SETTLE_DATE, ExternalId.of("a", "b"), 100, SETTLE_DATE, SETTLE_DATE, NOTIONAL, "tonnes", 100, CUR, SETTLE_DATE.minusYears(1));
  public static final MetalFutureOptionDefinition METAL_FUTURE_OPTION = new MetalFutureOptionDefinition(SETTLE_DATE, METAL_FUTURE, 100, ExerciseDecisionType.AMERICAN, true);
  public static final InterestRateFutureDefinition IR_FUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurityDefinition();
  public static final InterestRateFutureOptionMarginSecurityDefinition IR_FUT_OPT_MARGIN_SEC_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionMarginSecurityDefinition();
  public static final InterestRateFutureOptionMarginTransactionDefinition IR_FUT_OPT_MARGIN_T_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionMarginTransactionDefinition();
  public static final InterestRateFutureOptionPremiumSecurityDefinition IR_FUT_OPT_PREMIUM_SEC_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionPremiumSecurityDefinition();
  public static final InterestRateFutureOptionPremiumTransactionDefinition IR_FUT_OPT_PREMIUM_T_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionPremiumTransactionDefinition();

  public static final SwapDefinition SWAP = new SwapDefinition(ANNUITY_FIXED, ANNUITY_COUPON_CMS);
  public static final SwapFixedIborSpreadDefinition SWAP_FIXED_IBOR_SPREAD = new SwapFixedIborSpreadDefinition(ANNUITY_FIXED, ANNUITY_IBOR_SPREAD_RECEIVE);
  public static final SwapFixedIborDefinition SWAP_FIXED_IBOR = new SwapFixedIborDefinition(ANNUITY_FIXED, ANNUITY_IBOR);
  public static final SwapIborIborDefinition SWAP_IBOR_IBOR = new SwapIborIborDefinition(ANNUITY_IBOR_SPREAD_PAY, ANNUITY_IBOR_SPREAD_RECEIVE);
  public static final DeliverableSwapFuturesSecurityDefinition DELIVERABLE_SWAP_FUTURE = new DeliverableSwapFuturesSecurityDefinition(SETTLE_DATE, new SwapFixedIborDefinition(ANNUITY_FIXED_UNIT_NOTIONAL, ANNUITY_IBOR_UNIT_NOTIONAL), NOTIONAL);
  public static final SwaptionCashFixedIborDefinition SWAPTION_CASH = SwaptionInstrumentsDescriptionDataSet.createSwaptionCashFixedIborDefinition();
  public static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition();
  private static final Set<InstrumentDefinition<?>> ALL_INSTRUMENTS = Sets.newHashSet();

  static {
    ALL_INSTRUMENTS.add(AG_FUTURE);
    ALL_INSTRUMENTS.add(AG_FUTURE_OPTION);
    ALL_INSTRUMENTS.add(AG_FWD);
    ALL_INSTRUMENTS.add(ANNUITY_FIXED);
    ALL_INSTRUMENTS.add(ANNUITY_IBOR);
    ALL_INSTRUMENTS.add(ANNUITY_COUPON_CMS);
    ALL_INSTRUMENTS.add(ANNUITY_IBOR_SPREAD_RECEIVE);
    ALL_INSTRUMENTS.add(ANNUITY_IBOR_SPREAD_PAY);
    ALL_INSTRUMENTS.add(BFO_SECURITY);
    ALL_INSTRUMENTS.add(BFO_TRANSACTION);
    ALL_INSTRUMENTS.add(BILL_SECURITY);
    ALL_INSTRUMENTS.add(BILL_TRANSACTION);
    ALL_INSTRUMENTS.add(BNDFUT_SECURITY_DEFINITION);
    ALL_INSTRUMENTS.add(BOND_FIXED_SECURITY);
    ALL_INSTRUMENTS.add(BOND_FIXED_TRANSACTION);
    ALL_INSTRUMENTS.add(CASH);
    ALL_INSTRUMENTS.add(COUPON_CMS);
    ALL_INSTRUMENTS.add(DELIVERABLE_SWAP_FUTURE);
    ALL_INSTRUMENTS.add(DEPOSIT_COUNTERPART);
    ALL_INSTRUMENTS.add(DEPOSIT_IBOR);
    ALL_INSTRUMENTS.add(DEPOSIT_ZERO);
    ALL_INSTRUMENTS.add(ENERGY_FUTURE);
    ALL_INSTRUMENTS.add(ENERGY_FUTURE_OPTION);
    ALL_INSTRUMENTS.add(ENERGY_FWD);
    ALL_INSTRUMENTS.add(FF_SECURITY);
    ALL_INSTRUMENTS.add(FF_TRANSACTION);
    ALL_INSTRUMENTS.add(FRA);
    ALL_INSTRUMENTS.add(GENERAL_ANNUITY);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_MARGIN_SEC_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_MARGIN_T_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_PREMIUM_SEC_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_PREMIUM_T_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_SECURITY_DEFINITION);
    ALL_INSTRUMENTS.add(METAL_FUTURE);
    ALL_INSTRUMENTS.add(METAL_FUTURE_OPTION);
    ALL_INSTRUMENTS.add(METAL_FWD);
    ALL_INSTRUMENTS.add(PAYMENT_FIXED);
    ALL_INSTRUMENTS.add(SWAP);
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

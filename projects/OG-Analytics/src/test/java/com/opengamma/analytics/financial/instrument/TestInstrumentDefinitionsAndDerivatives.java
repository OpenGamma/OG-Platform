/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.testng.collections.Sets;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexDividendFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityOptionDefinition;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSPremiumDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.varianceswap.VarianceSwapDefinition;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 *
 */
@SuppressWarnings("unchecked")
public class TestInstrumentDefinitionsAndDerivatives {
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
  public static final IborIndex IBOR_INDEX_1 = new IborIndex(CUR, IBOR_PERIOD_1, SPOT_LAG, IBOR_DAY_COUNT, BD, IS_EOM, "Ibor1");
  public static final IndexON INDEX_ON = new IndexON("A", CUR, FIXED_DAY_COUNT, 0);
  public static final IndexSwap CMS_INDEX = new IndexSwap(IBOR_PERIOD_1, IBOR_DAY_COUNT, IBOR_INDEX_1, IBOR_PERIOD_1, C);
  public static final Period IBOR_PERIOD_2 = Period.ofMonths(6);
  public static final IborIndex IBOR_INDEX_2 = new IborIndex(CUR, IBOR_PERIOD_2, SPOT_LAG, IBOR_DAY_COUNT, BD, IS_EOM, "Ibor2");
  public static final double SPREAD = 0.001;
  public static final GeneratorSwapXCcyIborIbor XCCY_GENERATOR = new GeneratorSwapXCcyIborIbor("XCCY", IBOR_INDEX_2, IBOR_INDEX_1, C, C);
  public static final IndexPrice INDEX_PRICE = new IndexPrice("CPI", CUR);
  public static final Convention CONVENTION = new Convention(2, FIXED_DAY_COUNT, BD, C, "");
  public static final ISDACDSPremiumDefinition ISDA_PREMIUM = ISDACDSPremiumDefinition.from(SETTLE_DATE, SETTLE_DATE.plusYears(5), PeriodFrequency.SEMI_ANNUAL, CONVENTION, StubType.LONG_END, false,
      NOTIONAL, SPREAD, CUR, C);

  public static final CouponFixedDefinition COUPON_FIXED = CouponFixedDefinition.from(CUR, SETTLE_DATE, SETTLE_DATE, SETTLE_DATE, SPOT_LAG, NOTIONAL, FIXED_RATE);
  public static final CouponIborDefinition COUPON_IBOR = CouponIborDefinition.from(NOTIONAL, SETTLE_DATE, IBOR_INDEX_1, C);
  public static final CouponIborGearingDefinition COUPON_IBOR_GEARING = CouponIborGearingDefinition.from(COUPON_IBOR, 0.3, 2);
  public static final CouponIborSpreadDefinition COUPON_IBOR_SPREAD = CouponIborSpreadDefinition.from(COUPON_IBOR, 0.3);
  public static final CouponIborCompoundingDefinition COUPON_IBOR_COMPOUNDED = CouponIborCompoundingDefinition.from(NOTIONAL, SETTLE_DATE, TENOR, IBOR_INDEX_1, C);
  public static final CouponIborRatchetDefinition COUPON_IBOR_RATCHET = new CouponIborRatchetDefinition(CUR, SETTLE_DATE.plusMonths(3), SETTLE_DATE, SETTLE_DATE.plusMonths(3), 0.01, NOTIONAL,
      SETTLE_DATE.plusMonths(1), IBOR_INDEX_1, new double[] {1, 2, 3 }, new double[] {3, 4, 5 }, new double[] {5, 6, 7 }, C);
  public static final CouponCMSDefinition COUPON_CMS = CouponCMSDefinition.from(CouponIborDefinition.from(1000, SETTLE_DATE, IBOR_INDEX_1, C), CMS_INDEX, C);
  public static final CouponONSimplifiedDefinition COUPON_OIS_SIMPLIFIED = CouponONSimplifiedDefinition.from(INDEX_ON, SETTLE_DATE, SETTLE_DATE.plusDays(28), NOTIONAL, 2, C);
  public static final CouponONDefinition COUPON_OIS = CouponONDefinition.from(INDEX_ON, SETTLE_DATE, SETTLE_DATE.plusYears(1), NOTIONAL, SPOT_LAG, C);

  public static final CouponInflationZeroCouponMonthlyDefinition INFLATION_ZERO_COUPON = CouponInflationZeroCouponMonthlyDefinition.from(SETTLE_DATE, SETTLE_DATE.plusMonths(3), NOTIONAL, INDEX_PRICE,
      1, 1, true);
  public static final CouponInflationZeroCouponInterpolationDefinition INFLATION_INTERPOLATED_COUPON = CouponInflationZeroCouponInterpolationDefinition.from(SETTLE_DATE, SETTLE_DATE.plusYears(1),
      NOTIONAL, INDEX_PRICE, 2, 2, false);
  public static final CouponInflationZeroCouponMonthlyGearingDefinition INFLATION_ZERO_GEARING_COUPON = CouponInflationZeroCouponMonthlyGearingDefinition.from(SETTLE_DATE, SETTLE_DATE.plusYears(1),
      NOTIONAL, INDEX_PRICE, 100.0, 2, 2, false, 0.4);
  public static final CouponInflationZeroCouponInterpolationGearingDefinition INFLATION_INTERPOLATED_GEARING_COUPON = CouponInflationZeroCouponInterpolationGearingDefinition.from(SETTLE_DATE,
      SETTLE_DATE.plusYears(1), NOTIONAL, INDEX_PRICE, 100.0, 2, 2, false, 1.4);

  public static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> CAPITAL_INDEXED_BOND_SECURITY = BondCapitalIndexedSecurityDefinition.fromMonthly(
      INDEX_PRICE, SPOT_LAG, SETTLE_DATE, 100, SETTLE_DATE.plusYears(5), FIXED_PERIOD, NOTIONAL, FIXED_RATE, BD, 2, C, FIXED_DAY_COUNT, SimpleYieldConvention.AUSTRIA_ISMA_METHOD, IS_EOM, "");
  public static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> CAPITAL_INDEXED_BOND_TRANSACTION = new BondCapitalIndexedTransactionDefinition<>(
      CAPITAL_INDEXED_BOND_SECURITY, 1, SETTLE_DATE, 100);
  public static final PaymentFixedDefinition PAYMENT_FIXED = new PaymentFixedDefinition(CUR, SETTLE_DATE, NOTIONAL);

  public static final DepositCounterpartDefinition DEPOSIT_COUNTERPART = new DepositCounterpartDefinition(CUR, SETTLE_DATE, SETTLE_DATE.plusDays(3), NOTIONAL, FIXED_RATE, FIXED_RATE, "a");
  public static final DepositIborDefinition DEPOSIT_IBOR = DepositIborDefinition.fromStart(SETTLE_DATE, NOTIONAL, FIXED_RATE, IBOR_INDEX_1, C);
  public static final DepositZeroDefinition DEPOSIT_ZERO = DepositZeroDefinition.from(CUR, SETTLE_DATE, SETTLE_DATE.plusDays(3), FIXED_DAY_COUNT, new ContinuousInterestRate(0.03), C, FIXED_DAY_COUNT);

  public static final AnnuityCouponCMSDefinition ANNUITY_COUPON_CMS = new AnnuityCouponCMSDefinition(new CouponCMSDefinition[] {COUPON_CMS }, C);
  public static final AnnuityCouponFixedDefinition ANNUITY_FIXED = AnnuityCouponFixedDefinition.from(CUR, SETTLE_DATE, TENOR, FIXED_PERIOD, C, FIXED_DAY_COUNT, BD, IS_EOM, NOTIONAL, FIXED_RATE,
      IS_PAYER);
  public static final AnnuityCouponFixedDefinition ANNUITY_FIXED_UNIT_NOTIONAL = AnnuityCouponFixedDefinition.from(CUR, SETTLE_DATE, TENOR, FIXED_PERIOD, C, FIXED_DAY_COUNT, BD, IS_EOM, 1,
      FIXED_RATE, !IS_PAYER);
  public static final AnnuityCouponIborDefinition ANNUITY_IBOR = AnnuityCouponIborDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, !IS_PAYER, C);
  public static final AnnuityCouponIborDefinition ANNUITY_IBOR_UNIT_NOTIONAL = AnnuityCouponIborDefinition.from(SETTLE_DATE, TENOR, 1, IBOR_INDEX_1, IS_PAYER, C);
  public static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_RECEIVE = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_2, SPREAD, !IS_PAYER, C);
  public static final AnnuityCouponIborSpreadDefinition ANNUITY_IBOR_SPREAD_PAY = AnnuityCouponIborSpreadDefinition.from(SETTLE_DATE, TENOR, NOTIONAL, IBOR_INDEX_1, 0.0, IS_PAYER, C);
  public static final AnnuityDefinition<PaymentFixedDefinition> GENERAL_ANNUITY = new AnnuityDefinition<>(new PaymentFixedDefinition[] {
    new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2011, 1, 1), 1000), new PaymentFixedDefinition(CUR, DateUtils.getUTCDate(2012, 1, 1), 1000) }, C);

  public static final BillSecurityDefinition BILL_SECURITY = new BillSecurityDefinition(CUR, SETTLE_DATE.plusYears(1), NOTIONAL, 0, C, SimpleYieldConvention.BANK_OF_CANADA, FIXED_DAY_COUNT, "");
  public static final BillTransactionDefinition BILL_TRANSACTION = new BillTransactionDefinition(BILL_SECURITY, 100, SETTLE_DATE, -100);

  public static final BondFixedSecurityDefinition BOND_FIXED_SECURITY = BondFixedSecurityDefinition.from(CUR, SETTLE_DATE.plusYears(2), SETTLE_DATE, FIXED_PERIOD, FIXED_RATE, SPOT_LAG, C,
      FIXED_DAY_COUNT, BD, SimpleYieldConvention.DISCOUNT, IS_EOM, "");
  public static final BondFixedTransactionDefinition BOND_FIXED_TRANSACTION = new BondFixedTransactionDefinition(BOND_FIXED_SECURITY, 100, SETTLE_DATE, -100);
  public static final BondIborSecurityDefinition BOND_IBOR_SECURITY = BondIborSecurityDefinition.from(SETTLE_DATE.plusYears(2), SETTLE_DATE, IBOR_INDEX_1, 2, FIXED_DAY_COUNT, BD, IS_EOM, "", C);
  public static final BondIborTransactionDefinition BOND_IBOR_TRANSACTION = new BondIborTransactionDefinition(BOND_IBOR_SECURITY, 100, SETTLE_DATE, -100);
  public static final BondFutureDefinition BNDFUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  public static final BondFutureOptionPremiumSecurityDefinition BFO_SECURITY = FutureInstrumentsDescriptionDataSet.createBondFutureOptionPremiumSecurityDefinition();
  public static final BondFutureOptionPremiumTransactionDefinition BFO_TRANSACTION = new BondFutureOptionPremiumTransactionDefinition(BFO_SECURITY, -100, BFO_SECURITY.getExpirationDate().minusMonths(
      3), 100);

  public static final CashDefinition CASH = new CashDefinition(CUR, DateUtils.getUTCDate(2011, 1, 2), DateUtils.getUTCDate(2012, 1, 2), 1.0, 0.04, 1.0);
  public static final ForwardRateAgreementDefinition FRA = ForwardRateAgreementDefinition.from(SETTLE_DATE, SETTLE_DATE.plusMonths(3), NOTIONAL, IBOR_INDEX_1, FIXED_RATE, C);
  public static final FederalFundsFutureSecurityDefinition FF_SECURITY = FederalFundsFutureSecurityDefinition.from(SETTLE_DATE, INDEX_ON, NOTIONAL, 0.25, "a", C);
  public static final FederalFundsFutureTransactionDefinition FF_TRANSACTION = new FederalFundsFutureTransactionDefinition(FF_SECURITY, 100, SETTLE_DATE, 0.97);

  public static final AgricultureForwardDefinition AG_FWD = AgricultureForwardDefinition.withCashSettlement(SETTLE_DATE.plusYears(1), ExternalId.of("a", "b"), 100, NOTIONAL, "tonnes", 76, CUR,
      SETTLE_DATE);
  public static final AgricultureFutureDefinition AG_FUTURE = AgricultureFutureDefinition.withPhysicalSettlement(SETTLE_DATE, ExternalId.of("a", "b"), 100, SETTLE_DATE, SETTLE_DATE, NOTIONAL,
      "tonnes", 100, CUR, SETTLE_DATE.minusYears(1));
  public static final AgricultureFutureOptionDefinition AG_FUTURE_OPTION = new AgricultureFutureOptionDefinition(SETTLE_DATE, AG_FUTURE, 100, ExerciseDecisionType.AMERICAN, true);
  public static final EnergyForwardDefinition ENERGY_FWD = EnergyForwardDefinition.withCashSettlement(SETTLE_DATE.plusYears(1), ExternalId.of("a", "b"), 100, NOTIONAL, "watts", 76, CUR, SETTLE_DATE);
  public static final EnergyFutureDefinition ENERGY_FUTURE = EnergyFutureDefinition.withPhysicalSettlement(SETTLE_DATE, ExternalId.of("a", "b"), 100, SETTLE_DATE, SETTLE_DATE, NOTIONAL, "tonnes",
      100, CUR, SETTLE_DATE.minusYears(1));
  public static final EnergyFutureOptionDefinition ENERGY_FUTURE_OPTION = new EnergyFutureOptionDefinition(SETTLE_DATE, ENERGY_FUTURE, 100, ExerciseDecisionType.AMERICAN, true);
  public static final MetalForwardDefinition METAL_FWD = MetalForwardDefinition.withCashSettlement(SETTLE_DATE.plusYears(1), ExternalId.of("a", "b"), 100, NOTIONAL, "troy oz", 1776, CUR, SETTLE_DATE);
  public static final MetalFutureDefinition METAL_FUTURE = MetalFutureDefinition.withPhysicalSettlement(SETTLE_DATE, ExternalId.of("a", "b"), 100, SETTLE_DATE, SETTLE_DATE, NOTIONAL, "tonnes", 100,
      CUR, SETTLE_DATE.minusYears(1));
  public static final MetalFutureOptionDefinition METAL_FUTURE_OPTION = new MetalFutureOptionDefinition(SETTLE_DATE, METAL_FUTURE, 100, ExerciseDecisionType.AMERICAN, true);

  public static final IndexFutureDefinition INDEX_FUTURE = new IndexFutureDefinition(SETTLE_DATE, SETTLE_DATE, 100, CUR, 100, ExternalId.of("a", "b"));
  public static final EquityIndexDividendFutureDefinition EQUITY_INDEX_DIVIDEND_FUTURE = new EquityIndexDividendFutureDefinition(SETTLE_DATE, SETTLE_DATE, 1200, CUR, 100);
  public static final EquityIndexOptionDefinition EQUITY_INDEX_OPTION = new EquityIndexOptionDefinition(true, 100, CUR, ExerciseDecisionType.AMERICAN, SETTLE_DATE, SETTLE_DATE.toLocalDate(), 25,
      SettlementType.CASH);
  public static final EquityOptionDefinition EQUITY_OPTION = new EquityOptionDefinition(false, 34, CUR, ExerciseDecisionType.EUROPEAN, SETTLE_DATE, SETTLE_DATE.toLocalDate(), 25,
      SettlementType.PHYSICAL);
  public static final EquityIndexFutureOptionDefinition EQUITY_INDEX_FUTURE_OPTION = new EquityIndexFutureOptionDefinition(SETTLE_DATE, INDEX_FUTURE, 100, ExerciseDecisionType.EUROPEAN, true, 100, 0);

  public static final InterestRateFutureSecurityDefinition IR_FUT_SECURITY_DEFINITION = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurityDefinition();
  public static final InterestRateFutureOptionMarginSecurityDefinition IR_FUT_OPT_MARGIN_SEC_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionMarginSecurityDefinition();
  public static final InterestRateFutureOptionMarginTransactionDefinition IR_FUT_OPT_MARGIN_T_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionMarginTransactionDefinition();
  public static final InterestRateFutureOptionPremiumSecurityDefinition IR_FUT_OPT_PREMIUM_SEC_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionPremiumSecurityDefinition();
  public static final InterestRateFutureOptionPremiumTransactionDefinition IR_FUT_OPT_PREMIUM_T_DEF = FutureInstrumentsDescriptionDataSet.createInterestRateFutureOptionPremiumTransactionDefinition();

  public static final SwapDefinition SWAP = new SwapDefinition(ANNUITY_FIXED, ANNUITY_COUPON_CMS);
  public static final SwapFixedIborSpreadDefinition SWAP_FIXED_IBOR_SPREAD = new SwapFixedIborSpreadDefinition(ANNUITY_FIXED, ANNUITY_IBOR_SPREAD_RECEIVE);
  public static final SwapFixedIborDefinition SWAP_FIXED_IBOR = new SwapFixedIborDefinition(ANNUITY_FIXED, ANNUITY_IBOR);
  public static final SwapIborIborDefinition SWAP_IBOR_IBOR = new SwapIborIborDefinition(ANNUITY_IBOR_SPREAD_PAY, ANNUITY_IBOR_SPREAD_RECEIVE);
  public static final SwapFuturesPriceDeliverableSecurityDefinition DELIVERABLE_SWAP_FUTURE = new SwapFuturesPriceDeliverableSecurityDefinition(SETTLE_DATE, new SwapFixedIborDefinition(
      ANNUITY_FIXED_UNIT_NOTIONAL, ANNUITY_IBOR_UNIT_NOTIONAL), NOTIONAL);

  public static final SwaptionCashFixedIborDefinition SWAPTION_CASH = SwaptionInstrumentsDescriptionDataSet.createSwaptionCashFixedIborDefinition();
  public static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition();
  public static final SwaptionPhysicalFixedIborSpreadDefinition SWAPTION_PHYS_SPREAD = SwaptionPhysicalFixedIborSpreadDefinition.from(SETTLE_DATE, SWAP_FIXED_IBOR_SPREAD, IS_EOM);
  public static final SwaptionBermudaFixedIborDefinition SWAPTION_BERMUDA = SwaptionBermudaFixedIborDefinition.from(SWAP_FIXED_IBOR, false, new ZonedDateTime[] {SETTLE_DATE.minusMonths(6),
    SETTLE_DATE.minusMonths(5), SETTLE_DATE.minusMonths(4), SETTLE_DATE.minusMonths(3) });

  public static final CapFloorIborDefinition CAP_FLOOR_IBOR = CapFloorIborDefinition.from(COUPON_IBOR, FIXED_RATE, true, C);
  public static final CapFloorCMSDefinition CAP_FLOOR_CMS = CapFloorCMSDefinition.from(COUPON_CMS, FIXED_RATE, true);
  public static final CapFloorCMSSpreadDefinition CAP_FLOOR_CMS_SPREAD = CapFloorCMSSpreadDefinition.from(SETTLE_DATE.plusMonths(3), SETTLE_DATE, SETTLE_DATE.plusMonths(3), 0.1, NOTIONAL, CMS_INDEX,
      CMS_INDEX, FIXED_RATE, false, C, C);

  public static final SwapXCcyIborIborDefinition XCCY_SWAP = SwapXCcyIborIborDefinition.from(SETTLE_DATE, TENOR, XCCY_GENERATOR, NOTIONAL, NOTIONAL, SPREAD, IS_PAYER, C, C);

  public static final ISDACDSDefinition ISDA_CDS = new ISDACDSDefinition(SETTLE_DATE, SETTLE_DATE.plusYears(2), ISDA_PREMIUM, NOTIONAL, SPREAD, FIXED_RATE, false, false, true,
      PeriodFrequency.SEMI_ANNUAL, CONVENTION, StubType.LONG_END);

  public static final ForexDefinition FX = ForexDefinition.fromAmounts(CUR, Currency.AUD, SETTLE_DATE, NOTIONAL, -NOTIONAL * 1.5);
  public static final ForexSwapDefinition FX_SWAP = new ForexSwapDefinition(FX, ForexDefinition.fromAmounts(CUR, Currency.AUD, SETTLE_DATE.plusMonths(3), -NOTIONAL, NOTIONAL * 1.5));
  public static final ForexOptionVanillaDefinition FX_VANILLA_OPTION = new ForexOptionVanillaDefinition(FX, SETTLE_DATE.minusMonths(6), false, false);
  public static final ForexOptionSingleBarrierDefinition FX_BARRIER_OPTION = new ForexOptionSingleBarrierDefinition(FX_VANILLA_OPTION, new Barrier(KnockType.IN, BarrierType.DOWN,
      ObservationType.CONTINUOUS, 1.5));
  public static final ForexNonDeliverableForwardDefinition FX_NDF = new ForexNonDeliverableForwardDefinition(CUR, Currency.AUD, NOTIONAL, -1.5 * NOTIONAL, SETTLE_DATE.minusMonths(2), SETTLE_DATE);
  public static final ForexNonDeliverableOptionDefinition FX_NDO = new ForexNonDeliverableOptionDefinition(FX_NDF, true, false);
  public static final ForexOptionDigitalDefinition FX_DIGITAL = new ForexOptionDigitalDefinition(FX, SETTLE_DATE, IS_PAYER, IS_EOM);

  public static final VarianceSwapDefinition VARIANCE_SWAP = VarianceSwapDefinition
      .fromVarianceParams(SETTLE_DATE, SETTLE_DATE.plusYears(1), SETTLE_DATE, PeriodFrequency.DAILY, CUR, C, 1, 0.03, 1000);
  public static final EquityVarianceSwapDefinition EQUITY_VARIANCE_SWAP = EquityVarianceSwapDefinition.fromVarianceParams(SETTLE_DATE, SETTLE_DATE.plusYears(1), SETTLE_DATE, PeriodFrequency.DAILY,
      CUR, C, 1, 0.03, 1000, true);
  private static final Set<InstrumentDefinition<?>> ALL_INSTRUMENTS = Sets.newHashSet();
  private static final Set<InstrumentDerivative> ALL_DERIVATIVES = Sets.newHashSet();

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
    ALL_INSTRUMENTS.add(BOND_IBOR_SECURITY);
    ALL_INSTRUMENTS.add(BOND_IBOR_TRANSACTION);
    ALL_INSTRUMENTS.add(CAP_FLOOR_CMS);
    ALL_INSTRUMENTS.add(CAP_FLOOR_CMS_SPREAD);
    ALL_INSTRUMENTS.add(CAP_FLOOR_IBOR);
    ALL_INSTRUMENTS.add(CAPITAL_INDEXED_BOND_SECURITY);
    ALL_INSTRUMENTS.add(CAPITAL_INDEXED_BOND_TRANSACTION);
    ALL_INSTRUMENTS.add(CASH);
    ALL_INSTRUMENTS.add(COUPON_CMS);
    ALL_INSTRUMENTS.add(COUPON_FIXED);
    ALL_INSTRUMENTS.add(COUPON_IBOR);
    ALL_INSTRUMENTS.add(COUPON_IBOR_COMPOUNDED);
    ALL_INSTRUMENTS.add(COUPON_IBOR_GEARING);
    ALL_INSTRUMENTS.add(COUPON_IBOR_RATCHET);
    ALL_INSTRUMENTS.add(COUPON_IBOR_SPREAD);
    ALL_INSTRUMENTS.add(COUPON_OIS);
    ALL_INSTRUMENTS.add(COUPON_OIS_SIMPLIFIED);
    ALL_INSTRUMENTS.add(DELIVERABLE_SWAP_FUTURE);
    ALL_INSTRUMENTS.add(DEPOSIT_COUNTERPART);
    ALL_INSTRUMENTS.add(DEPOSIT_IBOR);
    ALL_INSTRUMENTS.add(DEPOSIT_ZERO);
    ALL_INSTRUMENTS.add(ENERGY_FUTURE);
    ALL_INSTRUMENTS.add(ENERGY_FUTURE_OPTION);
    ALL_INSTRUMENTS.add(ENERGY_FWD);
    ALL_INSTRUMENTS.add(INDEX_FUTURE);
    ALL_INSTRUMENTS.add(EQUITY_INDEX_DIVIDEND_FUTURE);
    ALL_INSTRUMENTS.add(EQUITY_INDEX_FUTURE_OPTION);
    ALL_INSTRUMENTS.add(EQUITY_INDEX_OPTION);
    ALL_INSTRUMENTS.add(EQUITY_OPTION);
    ALL_INSTRUMENTS.add(EQUITY_VARIANCE_SWAP);
    ALL_INSTRUMENTS.add(FF_SECURITY);
    ALL_INSTRUMENTS.add(FF_TRANSACTION);
    ALL_INSTRUMENTS.add(FRA);
    ALL_INSTRUMENTS.add(FX);
    ALL_INSTRUMENTS.add(FX_BARRIER_OPTION);
    ALL_INSTRUMENTS.add(FX_DIGITAL);
    ALL_INSTRUMENTS.add(FX_NDF);
    ALL_INSTRUMENTS.add(FX_NDO);
    ALL_INSTRUMENTS.add(FX_SWAP);
    ALL_INSTRUMENTS.add(FX_VANILLA_OPTION);
    ALL_INSTRUMENTS.add(GENERAL_ANNUITY);
    ALL_INSTRUMENTS.add(INFLATION_INTERPOLATED_COUPON);
    ALL_INSTRUMENTS.add(INFLATION_INTERPOLATED_GEARING_COUPON);
    ALL_INSTRUMENTS.add(INFLATION_ZERO_COUPON);
    ALL_INSTRUMENTS.add(INFLATION_ZERO_GEARING_COUPON);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_MARGIN_SEC_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_MARGIN_T_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_PREMIUM_SEC_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_OPT_PREMIUM_T_DEF);
    ALL_INSTRUMENTS.add(IR_FUT_SECURITY_DEFINITION);
    ALL_INSTRUMENTS.add(ISDA_CDS);
    ALL_INSTRUMENTS.add(METAL_FUTURE);
    ALL_INSTRUMENTS.add(METAL_FUTURE_OPTION);
    ALL_INSTRUMENTS.add(METAL_FWD);
    ALL_INSTRUMENTS.add(PAYMENT_FIXED);
    ALL_INSTRUMENTS.add(SWAP);
    ALL_INSTRUMENTS.add(SWAP_IBOR_IBOR);
    ALL_INSTRUMENTS.add(SWAP_FIXED_IBOR);
    ALL_INSTRUMENTS.add(SWAP_FIXED_IBOR_SPREAD);
    ALL_INSTRUMENTS.add(SWAPTION_BERMUDA);
    ALL_INSTRUMENTS.add(SWAPTION_CASH);
    ALL_INSTRUMENTS.add(SWAPTION_PHYS);
    ALL_INSTRUMENTS.add(SWAPTION_PHYS_SPREAD);
    ALL_INSTRUMENTS.add(VARIANCE_SWAP);
    ALL_INSTRUMENTS.add(XCCY_SWAP);

    final ZonedDateTime endDate = DateUtils.getUTCDate(2013, 11, 1);
    ZonedDateTime date = DateUtils.getUTCDate(2000, 1, 1);
    final List<ZonedDateTime> dates = new ArrayList<>();
    final List<Double> data = new ArrayList<>();
    while (!date.isAfter(endDate)) {
      dates.add(date);
      data.add(0.01);
      date = date.plusDays(1);
    }
    final ZonedDateTimeDoubleTimeSeries ts = ImmutableZonedDateTimeDoubleTimeSeries.of(dates, data, ZoneOffset.UTC);
    ALL_DERIVATIVES.add(AG_FUTURE.toDerivative(AG_FUTURE.getSettlementDate()));
    ALL_DERIVATIVES.add(AG_FUTURE_OPTION.toDerivative(AG_FUTURE_OPTION.getExpiryDate().minusDays(1)));
    ALL_DERIVATIVES.add(AG_FWD.toDerivative(AG_FWD.getSettlementDate()));
    ALL_DERIVATIVES.add(ANNUITY_FIXED.toDerivative(ANNUITY_FIXED.getPayments()[0].getPaymentDate(), ts));
    ALL_DERIVATIVES.add(ANNUITY_IBOR.toDerivative(ANNUITY_IBOR.getPayments()[0].getFixingDate(), ts));
    ALL_DERIVATIVES.add(ANNUITY_COUPON_CMS.toDerivative(ANNUITY_COUPON_CMS.getPayments()[0].getFixingDate().minusDays(1), ts));
    ALL_DERIVATIVES.add(ANNUITY_IBOR_SPREAD_RECEIVE.toDerivative(ANNUITY_IBOR.getPayments()[0].getFixingDate().minusDays(1), ts));
    ALL_DERIVATIVES.add(ANNUITY_IBOR_SPREAD_PAY.toDerivative(ANNUITY_IBOR.getPayments()[0].getFixingDate().minusDays(1), ts));
    ALL_DERIVATIVES.add(BFO_SECURITY.toDerivative(BFO_SECURITY.getUnderlyingFuture().getDeliveryLastDate().minusDays(1)));
    ALL_DERIVATIVES.add(BFO_TRANSACTION.toDerivative(BFO_TRANSACTION.getUnderlyingOption().getUnderlyingFuture().getDeliveryLastDate().minusDays(1)));
    ALL_DERIVATIVES.add(BILL_SECURITY.toDerivative(BILL_SECURITY.getEndDate().minusDays(2)));
    ALL_DERIVATIVES.add(BILL_TRANSACTION.toDerivative(BILL_TRANSACTION.getSettlementDate()));
    ALL_DERIVATIVES.add(BNDFUT_SECURITY_DEFINITION.toDerivative(BNDFUT_SECURITY_DEFINITION.getTradingLastDate(), 2.));
    ALL_DERIVATIVES.add(BOND_FIXED_SECURITY.toDerivative(BOND_FIXED_SECURITY.getCoupons().getPayments()[0].getPaymentDate()));
    ALL_DERIVATIVES.add(BOND_FIXED_TRANSACTION.toDerivative(BOND_FIXED_TRANSACTION.getSettlementDate()));
    ALL_DERIVATIVES.add(BOND_IBOR_SECURITY.toDerivative(BOND_IBOR_SECURITY.getCoupons().getPayments()[0].getAccrualStartDate().minusDays(1), ts));
    ALL_DERIVATIVES.add(BOND_IBOR_TRANSACTION.toDerivative(BOND_IBOR_TRANSACTION.getSettlementDate(), ts));
    ALL_DERIVATIVES.add(CAP_FLOOR_CMS.toDerivative(CAP_FLOOR_CMS.getFixingDate(), ts));
    ALL_DERIVATIVES.add(CAP_FLOOR_CMS_SPREAD.toDerivative(CAP_FLOOR_CMS_SPREAD.getFixingDate(), ts));
    ALL_DERIVATIVES.add(CAP_FLOOR_IBOR.toDerivative(CAP_FLOOR_IBOR.getFixingDate(), ts));
    ALL_DERIVATIVES.add(CAPITAL_INDEXED_BOND_SECURITY.toDerivative(CAPITAL_INDEXED_BOND_SECURITY.getCoupons().getPayments()[0].getPaymentDate(), ts));
    ALL_DERIVATIVES.add(CAPITAL_INDEXED_BOND_TRANSACTION.toDerivative(CAPITAL_INDEXED_BOND_TRANSACTION.getSettlementDate(), ts));
    ALL_DERIVATIVES.add(CASH.toDerivative(CASH.getStartDate()));
    ALL_DERIVATIVES.add(COUPON_CMS.toDerivative(COUPON_CMS.getAccrualStartDate(), ts));
    ALL_DERIVATIVES.add(COUPON_FIXED.toDerivative(COUPON_FIXED.getPaymentDate()));
    ALL_DERIVATIVES.add(COUPON_IBOR.toDerivative(COUPON_IBOR.getPaymentDate(), ts));
    ALL_DERIVATIVES.add(COUPON_IBOR_COMPOUNDED.toDerivative(COUPON_IBOR_COMPOUNDED.getPaymentDate(), ts));
    ALL_DERIVATIVES.add(COUPON_IBOR_GEARING.toDerivative(COUPON_IBOR_GEARING.getFixingDate(), ts));
    ALL_DERIVATIVES.add(COUPON_IBOR_RATCHET.toDerivative(COUPON_IBOR_RATCHET.getFixingDate()));
    ALL_DERIVATIVES.add(COUPON_IBOR_SPREAD.toDerivative(COUPON_IBOR_SPREAD.getFixingDate(), ts));
    ALL_DERIVATIVES.add(COUPON_OIS.toDerivative(COUPON_OIS.getAccrualStartDate(), ts));
    ALL_DERIVATIVES.add(COUPON_OIS_SIMPLIFIED.toDerivative(COUPON_OIS_SIMPLIFIED.getAccrualStartDate()));
    ALL_DERIVATIVES.add(DELIVERABLE_SWAP_FUTURE.toDerivative(DELIVERABLE_SWAP_FUTURE.getLastTradingDate().minusDays(10)));
    ALL_DERIVATIVES.add(DEPOSIT_COUNTERPART.toDerivative(DEPOSIT_COUNTERPART.getStartDate()));
    ALL_DERIVATIVES.add(DEPOSIT_IBOR.toDerivative(DEPOSIT_IBOR.getStartDate()));
    ALL_DERIVATIVES.add(DEPOSIT_ZERO.toDerivative(DEPOSIT_ZERO.getStartDate()));
    ALL_DERIVATIVES.add(ENERGY_FUTURE.toDerivative(ENERGY_FUTURE.getSettlementDate()));
    ALL_DERIVATIVES.add(ENERGY_FUTURE_OPTION.toDerivative(ENERGY_FUTURE_OPTION.getExpiryDate().minusDays(1)));
    ALL_DERIVATIVES.add(ENERGY_FWD.toDerivative(ENERGY_FWD.getSettlementDate()));
    ALL_DERIVATIVES.add(INDEX_FUTURE.toDerivative(SETTLE_DATE.plusDays(1)));
    ALL_DERIVATIVES.add(EQUITY_INDEX_DIVIDEND_FUTURE.toDerivative(SETTLE_DATE.plusDays(1)));
    ALL_DERIVATIVES.add(EQUITY_INDEX_OPTION.toDerivative(SETTLE_DATE.minusDays(100)));
    ALL_DERIVATIVES.add(EQUITY_INDEX_FUTURE_OPTION.toDerivative(SETTLE_DATE.minusDays(100)));
    ALL_DERIVATIVES.add(EQUITY_OPTION.toDerivative(SETTLE_DATE.minusDays(100)));
    ALL_DERIVATIVES.add(EQUITY_VARIANCE_SWAP.toDerivative(SETTLE_DATE.minusDays(100), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES));
    ALL_DERIVATIVES.add(FF_SECURITY.toDerivative(FF_SECURITY.getFixingPeriodDate()[0]));
    ALL_DERIVATIVES.add(FF_TRANSACTION.toDerivative(FF_TRANSACTION.getTradeDate(), new DoubleTimeSeries[] {ts, ts }));
    ALL_DERIVATIVES.add(FRA.toDerivative(FRA.getAccrualStartDate(), ts));
    ALL_DERIVATIVES.add(FX.toDerivative(FX.getPaymentCurrency1().getPaymentDate()));
    ALL_DERIVATIVES.add(FX_BARRIER_OPTION.toDerivative(FX_BARRIER_OPTION.getUnderlyingOption().getExpirationDate().minusDays(1)));
    ALL_DERIVATIVES.add(FX_DIGITAL.toDerivative(FX_DIGITAL.getExpirationDate().minusDays(1)));
    ALL_DERIVATIVES.add(FX_NDF.toDerivative(FX_NDF.getFixingDate()));
    ALL_DERIVATIVES.add(FX_NDO.toDerivative(FX_NDO.getUnderlyingNDF().getFixingDate()));
    ALL_DERIVATIVES.add(FX_SWAP.toDerivative(FX_SWAP.getNearLeg().getExchangeDate().minusDays(1)));
    ALL_DERIVATIVES.add(FX_VANILLA_OPTION.toDerivative(FX_VANILLA_OPTION.getExpirationDate().minusDays(1)));
    ALL_DERIVATIVES.add(GENERAL_ANNUITY.toDerivative(GENERAL_ANNUITY.getPayments()[0].getPaymentDate(), ts));
    ALL_DERIVATIVES.add(INFLATION_INTERPOLATED_COUPON.toDerivative(INFLATION_INTERPOLATED_COUPON.getAccrualStartDate(), ts));
    ALL_DERIVATIVES.add(INFLATION_INTERPOLATED_GEARING_COUPON.toDerivative(INFLATION_INTERPOLATED_GEARING_COUPON.getAccrualStartDate(), ts));
    ALL_DERIVATIVES.add(INFLATION_ZERO_COUPON.toDerivative(INFLATION_ZERO_COUPON.getAccrualStartDate().minusDays(3), ts));
    ALL_DERIVATIVES.add(INFLATION_ZERO_GEARING_COUPON.toDerivative(INFLATION_ZERO_GEARING_COUPON.getAccrualStartDate().minusDays(3), ts));
    ALL_DERIVATIVES.add(IR_FUT_OPT_MARGIN_SEC_DEF.toDerivative(IR_FUT_OPT_MARGIN_SEC_DEF.getExpirationDate().minusDays(1)));
    ALL_DERIVATIVES.add(IR_FUT_OPT_MARGIN_T_DEF.toDerivative(IR_FUT_OPT_MARGIN_T_DEF.getTradeDate(), 0.99));
    ALL_DERIVATIVES.add(IR_FUT_OPT_PREMIUM_SEC_DEF.toDerivative(IR_FUT_OPT_PREMIUM_SEC_DEF.getExpirationDate().minusDays(1)));
    ALL_DERIVATIVES.add(IR_FUT_OPT_PREMIUM_T_DEF.toDerivative(IR_FUT_OPT_PREMIUM_T_DEF.getUnderlyingOption().getExpirationDate().minusDays(1)));
    ALL_DERIVATIVES.add(IR_FUT_SECURITY_DEFINITION.toDerivative(IR_FUT_SECURITY_DEFINITION.getLastTradingDate().minusDays(1)));
    ALL_DERIVATIVES.add(ISDA_CDS.toDerivative(ISDA_CDS.getMaturity().minusDays(10)));
    ALL_DERIVATIVES.add(METAL_FUTURE.toDerivative(METAL_FUTURE.getSettlementDate()));
    ALL_DERIVATIVES.add(METAL_FUTURE_OPTION.toDerivative(METAL_FUTURE_OPTION.getExpiryDate().minusDays(1)));
    ALL_DERIVATIVES.add(METAL_FWD.toDerivative(METAL_FWD.getSettlementDate()));
    ALL_DERIVATIVES.add(PAYMENT_FIXED.toDerivative(PAYMENT_FIXED.getPaymentDate().minusDays(1)));
    ALL_DERIVATIVES.add(SWAP.toDerivative(SWAP.getSecondLeg().getPayments()[0].getPaymentDate(), new ZonedDateTimeDoubleTimeSeries[] {ts, ts }));
    ALL_DERIVATIVES.add(SWAP_IBOR_IBOR.toDerivative(SWAP_IBOR_IBOR.getFirstLeg().getPayments()[0].getPaymentDate(), new ZonedDateTimeDoubleTimeSeries[] {ts, ts }));
    ALL_DERIVATIVES.add(SWAP_FIXED_IBOR.toDerivative(SWAP_FIXED_IBOR.getFirstLeg().getPayments()[0].getPaymentDate(), new ZonedDateTimeDoubleTimeSeries[] {ts, ts }));
    ALL_DERIVATIVES.add(SWAP_FIXED_IBOR_SPREAD.toDerivative(SWAP_FIXED_IBOR_SPREAD.getFirstLeg().getPayments()[0].getPaymentDate(), new ZonedDateTimeDoubleTimeSeries[] {ts, ts }));
    ALL_DERIVATIVES.add(SWAPTION_BERMUDA.toDerivative(SWAPTION_BERMUDA.getExpiryDate()[0].minusDays(1)));
    ALL_DERIVATIVES.add(SWAPTION_CASH.toDerivative(SWAPTION_CASH.getExpiry().getExpiry().minusDays(1)));
    ALL_DERIVATIVES.add(SWAPTION_PHYS.toDerivative(SWAPTION_PHYS.getExpiry().getExpiry().minusDays(1)));
    ALL_DERIVATIVES.add(SWAPTION_PHYS_SPREAD.toDerivative(SWAPTION_PHYS_SPREAD.getExpiry().getExpiry().minusDays(10)));
    ALL_DERIVATIVES.add(VARIANCE_SWAP.toDerivative(SETTLE_DATE.minusDays(100), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES));
    ALL_DERIVATIVES.add(XCCY_SWAP.toDerivative(XCCY_SWAP.getFirstLeg().getPayments()[0].getPaymentDate(), new ZonedDateTimeDoubleTimeSeries[] {ts, ts }));
  }

  public static Set<InstrumentDefinition<?>> getAllInstruments() {
    return ALL_INSTRUMENTS;
  }

  public static Set<InstrumentDerivative> getAllDerivatives() {
    return ALL_DERIVATIVES;
  }
}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.DateSet;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.CalendarECBSettlements;
import com.opengamma.financial.analytics.CalendarTarget;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarBusinessDateUtils;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Tests related to the conversion of nodes used in curve construction to OG-Analytics objects.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeToDefinitionConverterTest {

  private static final MondayToFridayCalendar CALENDAR = new MondayToFridayCalendar("Weekend");
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final String SCHEME = "Test";
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final DayCount THIRTY_360 = DayCounts.THIRTY_U_360;
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId GB = ExternalSchemes.financialRegionId("GB");
  private static final ExternalId EU = ExternalSchemes.financialRegionId("EU");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  private static final String BBG_TICKER = "BLOOMBERG_TICKER";
  private static final ExternalId FIXED_LEG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg");
  private static final ExternalId FIXED_LEG_PAY_LAG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg Pay Lag");
  private static final ExternalId DEPOSIT_1D_ID = ExternalId.of(SCHEME, "USD 1d Deposit");
  private static final ExternalId DEPOSIT_1M_ID = ExternalId.of(SCHEME, "USD 1m Deposit");
  private static final String USDLIBOR_ACT_360_CONVENTION_NAME = "USD Libor ACT/360";
  private static final ExternalId USDLIBOR_ACT_360_CONVENTION_ID = ExternalId.of(SCHEME, USDLIBOR_ACT_360_CONVENTION_NAME);
  private static final String USDLIBOR_30_360_CONVENTION_NAME = "USD Libor 30/360";
  private static final ExternalId USDLIBOR_30_360_ID = ExternalId.of(SCHEME, USDLIBOR_30_360_CONVENTION_NAME);
  private static final ExternalId RATE_FUTURE_Q3M_ID = ExternalId.of(SCHEME, "USD Q/3m Rate Future");
  private static final ExternalId RATE_FUTURE_M3M_ID = ExternalId.of(SCHEME, "USD M/3m Rate Future");
  private static final ExternalId RATE_FUTURE_Q1M_ID = ExternalId.of(SCHEME, "USD Q/1m Rate Future");
  private static final ExternalId RATE_FUTURE_M1M_ID = ExternalId.of(SCHEME, "USD M/1m Rate Future");
  private static final ExternalId FED_FUND_FUTURE_ID = ExternalId.of(SCHEME, "FF Future");
  private static final ExternalId DELIVERABLE_SWAP_FUTURE_ID = ExternalId.of(SCHEME, "DSF");
  private static final ExternalId LEG_USDLIBOR3M_ID = ExternalId.of(SCHEME, "USD 3m Floating Leg");
  private static final ExternalId LEG_USDLIBOR6M_ID = ExternalId.of(SCHEME, "USD 6m Floating Leg");
  private static final ExternalId USD_OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, "USD Overnight");
  private static final ExternalId LEG_ON_CMP_ID = ExternalId.of(SCHEME, "USD OIS Leg");
  private static final String ON_AA_NAME = "USD ON Arith. Average Leg";
  private static final ExternalId ON_AA_ID = ExternalId.of(SCHEME, ON_AA_NAME);
  private static final ExternalId FX_FORWARD_ID = ExternalId.of(SCHEME, "FX Forward");
  private static final ExternalId FX_SPOT_ID = ExternalId.of(SCHEME, "FX Spot");
  private static final ExternalId IMM_3M_FUTURE_EXPIRY_CONVENTION = ExternalId.of(SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME);
  private static final ExternalId IMM_1M_FUTURE_EXPIRY_CONVENTION = ExternalId.of(SCHEME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME);
  private static final ExternalId FIXED_IBOR_3M_SWAP_ID = ExternalId.of(SCHEME, "Swap");
  // LIBOR Index
  private static final String USDLIBOR1M_NAME = "USDLIBOR1M";
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR1M = 
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR1M_NAME, "ICE LIBOR 1M - USD", Tenor.ONE_MONTH, USDLIBOR_ACT_360_CONVENTION_ID);
  private static final ExternalId USDLIBOR1M_ID = ExternalId.of(BBG_TICKER, "US0001M Index");
  private static final String USDLIBOR3M_NAME = "USDLIBOR3M";
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR3M = 
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR3M_NAME, "ICE LIBOR 3M - USD", Tenor.THREE_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  private static final ExternalId USDLIBOR3M_ID = ExternalId.of(BBG_TICKER, "US0003M Index");
  private static final String USDLIBOR6M_NAME = "USDLIBOR6M";
  private static final com.opengamma.financial.security.index.IborIndex USDLIBOR6M = 
      new com.opengamma.financial.security.index.IborIndex(USDLIBOR6M_NAME, "ICE LIBOR 6M - USD", Tenor.SIX_MONTHS, USDLIBOR_ACT_360_CONVENTION_ID);
  private static final ExternalId USDLIBOR6M_ID = ExternalId.of(BBG_TICKER, "US0006M Index");
  private static final SwapFixedLegConvention FIXED_LEG = new SwapFixedLegConvention("USD Swap Fixed Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 0);
  private static final SwapFixedLegConvention FIXED_LEG_PAY_LAG = new SwapFixedLegConvention("USD Swap Fixed Leg Pay Lag", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention LEG_USDLIBOR3M = new VanillaIborLegConvention("USD 3m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 3m Floating Leg")),
      USDLIBOR3M_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention LEG_USDLIBOR6M = new VanillaIborLegConvention("USD 6m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 6m Floating Leg")),
      USDLIBOR6M_ID, false, SCHEME, Tenor.SIX_MONTHS, 2, false, StubType.LONG_END, false, 2);
  private static final String LIBOR_1M_CMP_3M_NAME = "USD 1M x 3M Ibor Cmp Leg";
  private static final ExternalId LIBOR_1M_CMP_3M_ID = ExternalId.of(SCHEME, LIBOR_1M_CMP_3M_NAME);
  private static final CompoundingIborLegConvention LIBOR_1M_CMP_3M = new CompoundingIborLegConvention(LIBOR_1M_CMP_3M_NAME,
      ExternalIdBundle.of(LIBOR_1M_CMP_3M_ID), USDLIBOR1M_ID, Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING,
      Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, false, 0);
  private static final String LIBOR_1M_CMP_FLAT_3M_NAME = "USD 1M x 3M Ibor Cmp Flat Leg";
  private static final ExternalId LIBOR_1M_CMP_FLAT_3M_ID = ExternalId.of(SCHEME, LIBOR_1M_CMP_FLAT_3M_NAME);
  private static final CompoundingIborLegConvention LIBOR_1M_CMP_FLAT_3M = new CompoundingIborLegConvention(LIBOR_1M_CMP_FLAT_3M_NAME,
      ExternalIdBundle.of(LIBOR_1M_CMP_FLAT_3M_ID), USDLIBOR1M_ID, Tenor.THREE_MONTHS, CompoundingType.FLAT_COMPOUNDING,
      Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, false, 0);
  private static final String USD_FEDFUND_INDEX_NAME = "Fed Funds Effective Rate";
  private static final OvernightIndex USD_FEDFUND_INDEX = new OvernightIndex(USD_FEDFUND_INDEX_NAME, USD_OVERNIGHT_CONVENTION_ID);
  private static final ExternalId USD_FEDFUND_INDEX_ID = ExternalId.of(BBG_TICKER, "FEDL1 Index");
  private static final OISLegConvention LEG_ON_CMP = new OISLegConvention("USD OIS Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD OIS Leg")), USD_FEDFUND_INDEX_ID,
      Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 2);
  private static final ONArithmeticAverageLegConvention ON_AA = new ONArithmeticAverageLegConvention(ON_AA_NAME, ExternalIdBundle.of(ExternalId.of(SCHEME, ON_AA_NAME)),
      USD_FEDFUND_INDEX_ID, Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 0);
  private static final DepositConvention DEPOSIT_1D = new DepositConvention("USD 1d Deposit", ExternalIdBundle.of(DEPOSIT_1D_ID),
      ACT_360, MODIFIED_FOLLOWING, 0, false, Currency.USD, US);
  private static final DepositConvention DEPOSIT_1M = new DepositConvention("USD 1m Deposit", ExternalIdBundle.of(DEPOSIT_1M_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
  private static final IborIndexConvention USDLIBOR_ACT_360 = new IborIndexConvention(USDLIBOR_ACT_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_ACT_360_CONVENTION_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  private static final IborIndexConvention LIBOR_30_360 = new IborIndexConvention(USDLIBOR_30_360_CONVENTION_NAME, ExternalIdBundle.of(USDLIBOR_30_360_ID),
      THIRTY_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  private static final InterestRateFutureConvention RATE_FUTURE_Q3M = new InterestRateFutureConvention("USD Q/3m Rate Future", ExternalIdBundle.of(RATE_FUTURE_Q3M_ID),
      IMM_3M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR3M_ID);
  private static final InterestRateFutureConvention RATE_FUTURE_M3M = new InterestRateFutureConvention("USD M/3m Rate Future", ExternalIdBundle.of(RATE_FUTURE_M3M_ID),
      IMM_1M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR3M_ID);
  private static final InterestRateFutureConvention RATE_FUTURE_Q1M = new InterestRateFutureConvention("USD Q/1m Rate Future", ExternalIdBundle.of(RATE_FUTURE_Q1M_ID),
      IMM_3M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR1M_ID);
  private static final InterestRateFutureConvention RATE_FUTURE_M1M = new InterestRateFutureConvention("USD M/1m Rate Future", ExternalIdBundle.of(RATE_FUTURE_M1M_ID),
      IMM_1M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR1M_ID);
  private static final OvernightIndexConvention USD_OVERNIGHT_CONVENTION = new OvernightIndexConvention("USD Overnight", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Overnight")),
      ACT_360, 1, Currency.USD, NYLON);
  
  private static final FXSpotConvention FX_SPOT = new FXSpotConvention("FX Spot", ExternalIdBundle.of(ExternalId.of(SCHEME, "FX Spot")), 1, US);
  private static final FXForwardAndSwapConvention FX_FORWARD = new FXForwardAndSwapConvention("FX Forward", ExternalIdBundle.of(ExternalId.of(SCHEME, "FX Forward")), FX_SPOT_ID, MODIFIED_FOLLOWING,
      false, US);
  private static final FederalFundsFutureConvention FED_FUND_FUTURES = new FederalFundsFutureConvention("FF Future", ExternalIdBundle.of(FED_FUND_FUTURE_ID), IMM_1M_FUTURE_EXPIRY_CONVENTION, US, USD_FEDFUND_INDEX_ID, 5000000);
  private static final SwapConvention SWAP = new SwapConvention("Swap", ExternalIdBundle.of(FIXED_IBOR_3M_SWAP_ID), FIXED_LEG_ID, LEG_USDLIBOR3M_ID);
  private static final DeliverablePriceQuotedSwapFutureConvention SWAP_FUTURE = new DeliverablePriceQuotedSwapFutureConvention("DSF", ExternalIdBundle.of(DELIVERABLE_SWAP_FUTURE_ID),
      IMM_3M_FUTURE_EXPIRY_CONVENTION, US, FIXED_IBOR_3M_SWAP_ID, 1);

  private static final String IMM_FRA_CONVENTION_NAME = "IMMFRA-Quarterly-3M";
  private static final ExternalId QUARTERLY_IMM_DATES = ExternalId.of(SCHEME_NAME, RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
  private static final ExternalId IMM_FRA_CONVENTION_ID = ExternalId.of(SCHEME, IMM_FRA_CONVENTION_NAME);
  private static final RollDateFRAConvention IMM_FRA_CONVENTION = new RollDateFRAConvention(IMM_FRA_CONVENTION_NAME, ExternalIdBundle.of(IMM_FRA_CONVENTION_ID), USDLIBOR3M_ID, QUARTERLY_IMM_DATES);
  private static final String FIXED_LEG_ROLL_NAME = "USD Fixed Leg 6MIMMQ";
  private static final ExternalId FIXED_LEG_ROLL_ID = ExternalId.of(SCHEME, FIXED_LEG_ROLL_NAME);
  private static final FixedLegRollDateConvention FIXED_LEG_ROLL = new FixedLegRollDateConvention(FIXED_LEG_ROLL_NAME, ExternalIdBundle.of(FIXED_LEG_ROLL_ID), Tenor.SIX_MONTHS,
      THIRTY_360, Currency.USD, NYLON, StubType.SHORT_START, false, 0);
  private static final String LIBOR_3M_LEG_ROLL_NAME = "USD Libor Leg 3MIMMQ";
  private static final ExternalId LIBOR_3M_LEG_ROLL_ID = ExternalId.of(SCHEME, LIBOR_3M_LEG_ROLL_NAME);
  private static final VanillaIborLegRollDateConvention LIBOR_3M_LEG_ROLL = new VanillaIborLegRollDateConvention(LIBOR_3M_LEG_ROLL_NAME, ExternalIdBundle.of(LIBOR_3M_LEG_ROLL_ID),
      USDLIBOR3M_ID, true, Tenor.THREE_MONTHS, StubType.SHORT_START, false, 0);
  private static final String LIBOR_6M_LEG_ROLL_NAME = "USD Libor Leg 6MIMMQ";
  private static final ExternalId LIBOR_6M_LEG_ROLL_ID = ExternalId.of(SCHEME, LIBOR_6M_LEG_ROLL_NAME);
  private static final VanillaIborLegRollDateConvention LIBOR_6M_LEG_ROLL = new VanillaIborLegRollDateConvention(LIBOR_6M_LEG_ROLL_NAME, ExternalIdBundle.of(LIBOR_6M_LEG_ROLL_ID),
      USDLIBOR6M_ID, true, Tenor.SIX_MONTHS, StubType.SHORT_START, false, 0);

  private static final String ON_3M_LEG_ROLL_NAME = "USD ON Leg 3MIMMQ";
  private static final ExternalId ON_3M_LEG_ROLL_ID = ExternalId.of(SCHEME, ON_3M_LEG_ROLL_NAME);
  private static final ONCompoundedLegRollDateConvention ON_3M_LEG_ROLL = new ONCompoundedLegRollDateConvention(ON_3M_LEG_ROLL_NAME, ExternalIdBundle.of(ON_3M_LEG_ROLL_ID),
      USD_FEDFUND_INDEX_ID, Tenor.THREE_MONTHS, StubType.SHORT_START, false, 0);


  private static final String SWAP_QIMM_6MLIBOR3M_CONVENTION_NAME = "USD Swap-QIMM-6M-LIBOR3M";
  private static final ExternalId SWAP_QIMM_6MLIBOR3M_CONVENTION_ID = ExternalId.of(SCHEME, SWAP_QIMM_6MLIBOR3M_CONVENTION_NAME);
  private static final RollDateSwapConvention SWAP_6MIMMLIBOR3MIMM_CONVENTION = new RollDateSwapConvention(SWAP_QIMM_6MLIBOR3M_CONVENTION_NAME, 
      ExternalIdBundle.of(SWAP_QIMM_6MLIBOR3M_CONVENTION_ID), FIXED_LEG_ROLL_ID, LIBOR_3M_LEG_ROLL_ID, QUARTERLY_IMM_DATES);

  private static final String SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_NAME = "USD Swap-QIMM-LIBOR6M-LIBOR3M";
  private static final ExternalId SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID = ExternalId.of(SCHEME, SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_NAME);
  private static final RollDateSwapConvention SWAP_LIBOR6MIMMLIBOR3MIMM_CONVENTION = new RollDateSwapConvention(SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_NAME,
      ExternalIdBundle.of(SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID), LIBOR_3M_LEG_ROLL_ID, LIBOR_6M_LEG_ROLL_ID, QUARTERLY_IMM_DATES);

  private static final String SWAP_QIMM_LIBOR3MFF3M_CONVENTION_NAME = "USD Swap-QIMM-LIBOR3M-FF3M";
  private static final ExternalId SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID = ExternalId.of(SCHEME, SWAP_QIMM_LIBOR3MFF3M_CONVENTION_NAME);
  private static final RollDateSwapConvention SWAP_QIMM_LIBOR3MFF3M_CONVENTION = new RollDateSwapConvention(SWAP_QIMM_LIBOR3MFF3M_CONVENTION_NAME,
      ExternalIdBundle.of(SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID), ON_3M_LEG_ROLL_ID, LIBOR_3M_LEG_ROLL_ID, QUARTERLY_IMM_DATES);

  // EUR conventions
  private static final String EURIBOR_CONVENTION_NAME = "EUR Euribor";
  private static final ExternalId EURIBOR_CONVENTION_ID = ExternalId.of(SCHEME, EURIBOR_CONVENTION_NAME);
  private static final IborIndexConvention EURIBOR_CONVENTION = new IborIndexConvention(EURIBOR_CONVENTION_NAME, ExternalIdBundle.of(EURIBOR_CONVENTION_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.EUR, LocalTime.of(11, 0), "EU", EU, EU, "Page");

  private static final String EURIBOR1M_NAME = "EURIBOR1M";
  private static final com.opengamma.financial.security.index.IborIndex EURIBOR1M = 
      new com.opengamma.financial.security.index.IborIndex(EURIBOR1M_NAME, "EURIBOR 1M ACT/360", Tenor.ONE_MONTH, EURIBOR_CONVENTION_ID);
  private static final ExternalId EURIBOR1M_ID = ExternalId.of(BBG_TICKER, "EUR001M Index");
  private static final String EURIBOR3M_NAME = "EURIBOR3M";
  private static final com.opengamma.financial.security.index.IborIndex EURIBOR3M = 
      new com.opengamma.financial.security.index.IborIndex(EURIBOR3M_NAME, "EURIBOR 3M ACT/360", Tenor.THREE_MONTHS, EURIBOR_CONVENTION_ID);
  private static final ExternalId EURIBOR3M_ID = ExternalId.of(BBG_TICKER, "EUR003M Index");
  private static final String EURIBOR6M_NAME = "EURIBOR6M";
  private static final com.opengamma.financial.security.index.IborIndex EURIBOR6M = 
      new com.opengamma.financial.security.index.IborIndex(EURIBOR6M_NAME, "EURIBOR 6M ACT/360", Tenor.SIX_MONTHS, EURIBOR_CONVENTION_ID);
  private static final ExternalId EURIBOR6M_ID = ExternalId.of(BBG_TICKER, "EUR006M Index");
  
  private static final String LEG_EURIBOR3M_NAME = "EUR Euribor 3M";
  private static final ExternalId LEG_EURIBOR3M_ID = ExternalId.of(SCHEME, LEG_EURIBOR3M_NAME);
  private static final VanillaIborLegConvention LEG_EURIBOR3M = new VanillaIborLegConvention(LEG_EURIBOR3M_NAME, ExternalIdBundle.of(LEG_EURIBOR3M_ID),
      EURIBOR3M_ID, true, SCHEME, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_START, false, 0);
  private static final String LEG_EURIBOR6M_NAME = "EUR Euribor 6M";
  private static final ExternalId LEG_EURIBOR6M_ID = ExternalId.of(SCHEME, LEG_EURIBOR6M_NAME);
  private static final VanillaIborLegConvention LEG_EURIBOR6M = new VanillaIborLegConvention(LEG_EURIBOR6M_NAME, ExternalIdBundle.of(LEG_EURIBOR6M_ID),
      EURIBOR6M_ID, true, SCHEME, Tenor.SIX_MONTHS, 2, true, StubType.SHORT_START, false, 0);

  private static final String EUR_OVERNIGHT_CONVENTION_NAME = "EUR Overnight";
  private static final ExternalId EUR_OVERNIGHT_CONVENTION_ID = ExternalId.of(SCHEME, EUR_OVERNIGHT_CONVENTION_NAME);
  private static final OvernightIndexConvention EUR_OVERNIGHT_CONVENTION = new OvernightIndexConvention(EUR_OVERNIGHT_CONVENTION_NAME, ExternalIdBundle.of(EUR_OVERNIGHT_CONVENTION_ID),
      ACT_360, 0, Currency.EUR, EU);
  private static final String EUR_EONIA_INDEX_NAME = "EUR EONIA";
  private static final ExternalId EUR_EONIA_INDEX_ID = ExternalId.of(BBG_TICKER, "EONIA Index");
  private static final OvernightIndex EUR_EONIA_INDEX = new OvernightIndex(EUR_EONIA_INDEX_NAME, EUR_OVERNIGHT_CONVENTION_ID);

  private static final String EUR_1Y_ON_CMP_NAME = "EUR 1Y ON Cmp";
  private static final ExternalId EUR_1Y_ON_CMP_ID = ExternalId.of(SCHEME, EUR_1Y_ON_CMP_NAME);
  private static final OISLegConvention EUR_1Y_ON_CMP = new OISLegConvention(EUR_1Y_ON_CMP_NAME, ExternalIdBundle.of(EUR_1Y_ON_CMP_ID), EUR_EONIA_INDEX_ID,
      Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, false, StubType.SHORT_START, false, 2);

  private static final String EUR1Y_FIXED_NAME = "EUR 1Y Fixed ";
  private static final ExternalId EUR1Y_FIXED_ID = ExternalId.of(SCHEME, EUR1Y_FIXED_NAME);
  private static final SwapFixedLegConvention EUR1Y_FIXED = new SwapFixedLegConvention(EUR1Y_FIXED_NAME, ExternalIdBundle.of(EUR1Y_FIXED_ID),
      Tenor.ONE_YEAR, THIRTY_360, MODIFIED_FOLLOWING, Currency.EUR, EU, 2, true, StubType.SHORT_START, false, 0);

  private static final String EUR_SWAP_1Y_ONCMP_NAME = "EUR 1Y Fixed 1Y ON Cmp";
  private static final ExternalId EUR_SWAP_1Y_ONCMP_ID = ExternalId.of(SCHEME, EUR_SWAP_1Y_ONCMP_NAME);
  private static final SwapConvention EUR_SWAP_1Y_ONCMP = new SwapConvention(EUR_SWAP_1Y_ONCMP_NAME, ExternalIdBundle.of(EUR_SWAP_1Y_ONCMP_ID), EUR1Y_FIXED_ID, EUR_1Y_ON_CMP_ID);

  private static final Map<ExternalId, Convention> CONVENTIONS = new HashMap<>();
  private static final Map<ExternalIdBundle, Security> SECURITY_MAP = new HashMap<>();
  private static final SecuritySource SECURITY_SOURCE;
  private static final ConfigSource CONFIG_SOURCE;
  private static final ConfigMaster CONFIG_MASTER;
  private static final ConventionSource CONVENTION_SOURCE;
  private static final HolidaySource HOLIDAY_SOURCE;
  private static final RegionSource REGION_SOURCE;
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 5, 1);
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR, Currency.USD, 1.30d);

  static {
    CONVENTIONS.put(DEPOSIT_1D_ID, DEPOSIT_1D);
    CONVENTIONS.put(DEPOSIT_1M_ID, DEPOSIT_1M);
    CONVENTIONS.put(FIXED_LEG_ID, FIXED_LEG);
    CONVENTIONS.put(FIXED_LEG_PAY_LAG_ID, FIXED_LEG_PAY_LAG);
    CONVENTIONS.put(FIXED_LEG_ROLL_ID, FIXED_LEG_ROLL);
    CONVENTIONS.put(USDLIBOR_ACT_360_CONVENTION_ID, USDLIBOR_ACT_360);
    CONVENTIONS.put(USDLIBOR_30_360_ID, LIBOR_30_360);
    CONVENTIONS.put(RATE_FUTURE_Q3M_ID, RATE_FUTURE_Q3M);
    CONVENTIONS.put(RATE_FUTURE_M3M_ID, RATE_FUTURE_M3M);
    CONVENTIONS.put(RATE_FUTURE_Q1M_ID, RATE_FUTURE_Q1M);
    CONVENTIONS.put(RATE_FUTURE_M1M_ID, RATE_FUTURE_M1M);
    CONVENTIONS.put(LEG_USDLIBOR3M_ID, LEG_USDLIBOR3M);
    CONVENTIONS.put(LEG_USDLIBOR6M_ID, LEG_USDLIBOR6M);
    CONVENTIONS.put(LIBOR_3M_LEG_ROLL_ID, LIBOR_3M_LEG_ROLL);
    CONVENTIONS.put(LIBOR_6M_LEG_ROLL_ID, LIBOR_6M_LEG_ROLL);
    CONVENTIONS.put(ON_3M_LEG_ROLL_ID, ON_3M_LEG_ROLL);
    CONVENTIONS.put(SWAP_QIMM_6MLIBOR3M_CONVENTION_ID, SWAP_6MIMMLIBOR3MIMM_CONVENTION);
    CONVENTIONS.put(SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID, SWAP_LIBOR6MIMMLIBOR3MIMM_CONVENTION);
    CONVENTIONS.put(SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID, SWAP_QIMM_LIBOR3MFF3M_CONVENTION);
    CONVENTIONS.put(LIBOR_1M_CMP_3M_ID, LIBOR_1M_CMP_3M);
    CONVENTIONS.put(LIBOR_1M_CMP_FLAT_3M_ID, LIBOR_1M_CMP_FLAT_3M);
    CONVENTIONS.put(USD_OVERNIGHT_CONVENTION_ID, USD_OVERNIGHT_CONVENTION);
    CONVENTIONS.put(LEG_ON_CMP_ID, LEG_ON_CMP);
    CONVENTIONS.put(ON_AA_ID, ON_AA);
    CONVENTIONS.put(FX_SPOT_ID, FX_SPOT);
    CONVENTIONS.put(FX_FORWARD_ID, FX_FORWARD);
    CONVENTIONS.put(FED_FUND_FUTURE_ID, FED_FUND_FUTURES);
    CONVENTIONS.put(FIXED_IBOR_3M_SWAP_ID, SWAP);
    CONVENTIONS.put(DELIVERABLE_SWAP_FUTURE_ID, SWAP_FUTURE);
    CONVENTIONS.put(IMM_FRA_CONVENTION_ID, IMM_FRA_CONVENTION);
    // EUR
    CONVENTIONS.put(EUR_OVERNIGHT_CONVENTION_ID, EUR_OVERNIGHT_CONVENTION);
    CONVENTIONS.put(EURIBOR_CONVENTION_ID, EURIBOR_CONVENTION);
    CONVENTIONS.put(EUR_1Y_ON_CMP_ID, EUR_1Y_ON_CMP);
    CONVENTIONS.put(LEG_EURIBOR3M_ID, LEG_EURIBOR3M);
    CONVENTIONS.put(LEG_EURIBOR6M_ID, LEG_EURIBOR6M);
    CONVENTIONS.put(EUR1Y_FIXED_ID, EUR1Y_FIXED);
    CONVENTIONS.put(EUR_SWAP_1Y_ONCMP_ID, EUR_SWAP_1Y_ONCMP);
    // Security map. Used for index.
    SECURITY_MAP.put(USD_FEDFUND_INDEX_ID.toBundle(), USD_FEDFUND_INDEX);
    SECURITY_MAP.put(USDLIBOR1M_ID.toBundle(), USDLIBOR1M);
    SECURITY_MAP.put(USDLIBOR3M_ID.toBundle(), USDLIBOR3M);
    SECURITY_MAP.put(USDLIBOR6M_ID.toBundle(), USDLIBOR6M);
    SECURITY_MAP.put(EUR_EONIA_INDEX_ID.toBundle(), EUR_EONIA_INDEX);
    SECURITY_MAP.put(EURIBOR1M_ID.toBundle(), EURIBOR1M);
    SECURITY_MAP.put(EURIBOR3M_ID.toBundle(), EURIBOR3M);
    SECURITY_MAP.put(EURIBOR6M_ID.toBundle(), EURIBOR6M);

    SECURITY_SOURCE = new MySecuritySource(SECURITY_MAP);
    CONVENTION_SOURCE = new TestConventionSource(CONVENTIONS);
    HOLIDAY_SOURCE = new MyHolidaySource(new ExternalId[] {US, EU, GB}, new Calendar[] {CALENDAR, CALENDAR, CALENDAR});
    REGION_SOURCE = new MyRegionSource(new ExternalId[] {US, EU, GB}, new String[] {"US", "EU", "GB"});
    CONFIG_MASTER = new InMemoryConfigMaster();
    CONFIG_SOURCE = new MasterConfigSource(CONFIG_MASTER);

    DateSet ecbCalendar = DateSet.of(Sets.newTreeSet(Lists.newArrayList(LocalDate.of(2013, 5, 8), LocalDate.of(2013, 10, 9), LocalDate.of(2013, 11, 13), LocalDate.of(2013, 12, 11),
                                                                        LocalDate.of(2014, 1, 15), LocalDate.of(2014, 2, 12), LocalDate.of(2014, 3, 12), LocalDate.of(2014, 4, 9),
                                                                        LocalDate.of(2014, 5, 14), LocalDate.of(2014, 6, 11), LocalDate.of(2014, 7, 9), LocalDate.of(2014, 8, 13),
                                                                        LocalDate.of(2014, 9, 10), LocalDate.of(2014, 10, 8), LocalDate.of(2014, 11, 12), LocalDate.of(2014, 12, 10),
                                                                        LocalDate.of(2015, 1, 8), LocalDate.of(2015, 2, 11))));
    CONFIG_MASTER.add(new ConfigDocument(ConfigItem.of(ecbCalendar, "ECB Settlement Calendar")));
  }

  @BeforeMethod
  public static void setUp() {
    VersionCorrectionProvider versionCorrectionProvider = new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    };
    ServiceContext serviceContext = ServiceContext.of(VersionCorrectionProvider.class, versionCorrectionProvider)
        .with(ConventionSource.class, CONVENTION_SOURCE)
        .with(SecuritySource.class, SECURITY_SOURCE);
    ThreadLocalServiceContext.init(serviceContext);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionForCash() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final CashNode cashNode = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    cashNode.accept(converter);
  }

  // TODO - ideally this would throw a different exception - see PLAT-6588
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionTypeForCash() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final CashNode cashNode = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    cashNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionForFRA() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongConventionForFRA() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    fraNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoFutureConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.98;
    marketValues.setDataPoint(marketDataId, rate);
    final RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, ExternalId.of(SCHEME, "Test"),
        "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    futureNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongFutureConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.98;
    marketValues.setDataPoint(marketDataId, rate);
    final RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS,
        Tenor.THREE_MONTHS, FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    futureNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapPayFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, ExternalId.of(SCHEME, "Test"), LEG_USDLIBOR3M_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testWrongSwapPayFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, FIXED_LEG_ID, USDLIBOR_ACT_360_CONVENTION_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapReceiveFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, LEG_USDLIBOR3M_ID, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testWrongSwapReceiveFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, USDLIBOR_ACT_360_CONVENTION_ID, FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapPayOISLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, ExternalId.of(SCHEME, "Test"), FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapReceiveOISLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, FIXED_LEG_ID, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSwapFloatLegIborConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final VanillaIborLegConvention iborConvention = new VanillaIborLegConvention("Test", ExternalIdBundle.of(ExternalId.of(SCHEME, "Test")),
        LEG_USDLIBOR6M_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 3);
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FIXED_LEG_ID, FIXED_LEG);
    conventions.put(LEG_USDLIBOR3M_ID, LEG_USDLIBOR3M);
    conventions.put(LEG_USDLIBOR6M_ID, LEG_USDLIBOR6M);
    conventions.put(ExternalId.of(SCHEME, "Test"), iborConvention);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, FIXED_LEG_ID, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, new TestConventionSource(conventions), HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW, FX_MATRIX);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFXForwardConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.USD, Currency.CAD, "Mapper");
    setupEmptyConventions();
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(
        HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  private void setupEmptyConventions() {
    setupConventions(ImmutableMap.<ExternalId, Convention>of());
  }

  private void setupConventions(Map<ExternalId, Convention> conventions) {
    ServiceContext serviceContext = ThreadLocalServiceContext.getInstance()
        .with(ConventionSource.class, new TestConventionSource(conventions));
    ThreadLocalServiceContext.init(serviceContext);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFXSpotConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.USD, Currency.CAD, "Mapper");
    final Map<ExternalId, Convention> conventions = ImmutableMap.<ExternalId, Convention>of(FX_FORWARD_ID, FX_FORWARD);
    setupConventions(conventions);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new FXForwardNodeConverter(HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongConventionForFXForward() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, LEG_USDLIBOR3M_ID, Currency.USD, Currency.CAD, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongUnderlyingConventionForFXForward() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardAndSwapConvention fxForward = new FXForwardAndSwapConvention(
        "FX Forward", ExternalIdBundle.of(ExternalId.of(SCHEME, "FX Forward")),
        USDLIBOR_ACT_360_CONVENTION_ID, MODIFIED_FOLLOWING, false, US);
    final FXForwardNode node = new FXForwardNode(
        Tenor.of(Period.ZERO), Tenor.ONE_YEAR, ExternalId.of(SCHEME, "FX Forward"),
        Currency.USD, Currency.CAD, "Mapper");
    setupConventions(ImmutableMap.<ExternalId, Convention>of(
        FX_FORWARD_ID, fxForward,
        USDLIBOR_ACT_360_CONVENTION_ID, USDLIBOR_ACT_360));
    final CurveNodeVisitor<InstrumentDefinition<?>> converter =
        new FXForwardNodeConverter(HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test
  public void testOneDayDeposit() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US1d");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    // P0D-P1D
    final CurveNode cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_DAY, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definition = cashNode.accept(converter);
    assertTrue("CashNode: converter with P0D-P1D", definition instanceof CashDefinition);
    final CashDefinition cash = (CashDefinition) definition;
    final CashDefinition expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P0D-P1D", expectedCash, cash);
    // P0D-ON
    final CurveNode cashNodeON = new CashNode(Tenor.of(Period.ZERO), Tenor.ON, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definitionON = cashNodeON.accept(converter);
    assertTrue("CashNode: converter with P0D-ON", definitionON instanceof CashDefinition);
    final CashDefinition cashON = (CashDefinition) definitionON;
    final CashDefinition expectedCashON = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P0D-ON", expectedCashON, cashON);
    // P1D-ON
    final CurveNode cashNode1DON = new CashNode(Tenor.ONE_DAY, Tenor.ON, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definition1DON = cashNode1DON.accept(converter);
    assertTrue("CashNode: converter with P1D-ON", definition1DON instanceof CashDefinition);
    final CashDefinition cash1DON = (CashDefinition) definition1DON;
    final CashDefinition expectedCash1DON = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 5, 3), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P1D-ON", expectedCash1DON, cash1DON);
    // ON-ON
    final CurveNode cashNodeONON = new CashNode(Tenor.ONE_DAY, Tenor.ON, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definitionONON = cashNodeONON.accept(converter);
    assertTrue("CashNode: converter with P1D-ON", definitionONON instanceof CashDefinition);
    final CashDefinition cashONON = (CashDefinition) definitionONON;
    final CashDefinition expectedCashONON = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 5, 3), 1, rate, 1. / 360);
    assertEquals("CashNode: converter with P1D-ON", expectedCashONON, cashONON);
    // POD-TN(WE)
    final ZonedDateTime now2 = DateUtils.getUTCDate(2013, 12, 20);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter2 = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now2);
    final CurveNode cashNode0DTN = new CashNode(Tenor.of(Period.ZERO), Tenor.TN, DEPOSIT_1D_ID, "Mapper");
    final InstrumentDefinition<?> definition0DTN = cashNode0DTN.accept(converter2);
    assertTrue("CashNode: converter with P0D-TN", definition0DTN instanceof CashDefinition);
    final CashDefinition cash0DTN = (CashDefinition) definition0DTN;
    final CashDefinition expectedCash0DTN = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 12, 20), DateUtils.getUTCDate(2013, 12, 24), 1, rate, 4. / 360);
    assertEquals("CashNode: converter with P0D-TN", expectedCash0DTN, cash0DTN);
  }

  @Test
  public void testOneMonthDeposit() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US1d");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    CurveNode cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, "Mapper");
    CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    CashDefinition cash = (CashDefinition) definition;
    CashDefinition expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 3, 6), 1, rate, 28. / 360);
    assertEquals(expectedCash, cash);
    now = DateUtils.getUTCDate(2013, 5, 2);
    converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, "Mapper");
    definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    cash = (CashDefinition) definition;
    expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 6), DateUtils.getUTCDate(2013, 6, 6), 1, rate, 31. / 360);
    assertEquals(expectedCash, cash);
    now = DateUtils.getUTCDate(2013, 5, 7);
    converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    cashNode = new CashNode(Tenor.ONE_MONTH, Tenor.THREE_MONTHS, DEPOSIT_1M_ID, "Mapper");
    definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    cash = (CashDefinition) definition;
    expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 6, 10), DateUtils.getUTCDate(2013, 9, 10), 1, rate, 92. / 360);
    assertEquals(expectedCash, cash);
  }

  @Test
  public void testLibor() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US3mLibor");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    // 3M node on 3M index
    CurveNode iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR3M_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    final IborIndex ibor3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    DepositIborDefinition ibor = (DepositIborDefinition) definition;
    DepositIborDefinition expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, rate, 89d / 360d, ibor3m);
    assertEquals("CurveNodeToDefinitionConverter: Libir fixing 3M", expectedLibor, ibor);
    // 6M Node on 6M index
    iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.SIX_MONTHS, USDLIBOR6M_ID, "Mapper");
    definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    ibor = (DepositIborDefinition) definition;
    final IborIndex ibor6m = ConverterUtils.indexIbor(USDLIBOR6M_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 8, 6), 1, rate, 181d / 360d, ibor6m);
    assertEquals(expectedLibor, ibor);
    // 3M node on 6M index
    iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR6M_ID, "Mapper");
    definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    ibor = (DepositIborDefinition) definition;
    expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, rate, 89d / 360d, ibor6m);
    assertEquals(expectedLibor, ibor);
  }

  @Test
  public void testFRA() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US3mLibor");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USDLIBOR3M_ID, "Mapper");
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final InstrumentDefinition<?> definition = fraNode.accept(converter);
    assertTrue(definition instanceof ForwardRateAgreementDefinition);
    final ForwardRateAgreementDefinition fra = (ForwardRateAgreementDefinition) definition;
    final ForwardRateAgreementDefinition expectedFRA = ForwardRateAgreementDefinition.from(DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 12, 5), 1, index, rate, CALENDAR);
    assertEquals(expectedFRA, fra);
  }

  /**
   * Test the roll date FRA converter. Creates FRA with increasing IMM dates numbers and test them against hard-coded dates.
   */
  @Test
  public void testRolDateFRA() {
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMMFRA1");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 9, 2);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateFRANodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final int[] startNumbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1};
    final int[] endNumbers = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13};
    final ZonedDateTime[] expectedStartDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15), DateUtils.getUTCDate(2013, 9, 18)};
    final ZonedDateTime[] expectedEndDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15), DateUtils.getUTCDate(2016, 9, 21), DateUtils.getUTCDate(2016, 9, 21)};
    final int nbTest = startNumbers.length;
    for(int loopt=0; loopt<nbTest; loopt++) {
      final RollDateFRANode immFraNode = new RollDateFRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, startNumbers[loopt], endNumbers[loopt], IMM_FRA_CONVENTION_ID, "Mapper", "IMM FRA 3M 1/2");
      final InstrumentDefinition<?> definition = immFraNode.accept(converter);
      assertTrue("IMMFRANodeConverter: testIMMFRA - FRA instanceof", definition instanceof ForwardRateAgreementDefinition);
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(expectedStartDates[loopt], -index.getSpotLag(), CALENDAR);
      final double acrualFactor = index.getDayCount().getDayCountFraction(expectedStartDates[loopt],  expectedEndDates[loopt]);
      final ForwardRateAgreementDefinition expectedFRA = new ForwardRateAgreementDefinition(index.getCurrency(), expectedStartDates[loopt], expectedStartDates[loopt],
          expectedEndDates[loopt], acrualFactor, 1, fixingDate, expectedStartDates[loopt], expectedEndDates[loopt], index, rate, CALENDAR);
      final ForwardRateAgreementDefinition fra = (ForwardRateAgreementDefinition) definition;
      assertEquals("IMMFRANodeConverter: testIMMFRA - FRA dates " + loopt, expectedFRA, fra);
    }
  }

  @Test
  public void test3M3MIRFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US3mLibor");
    final double rate = 0.98;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_Q3M_ID, "Mapper");
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = futureNode.accept(converter);
    InterestRateFutureTransactionDefinition future = (InterestRateFutureTransactionDefinition) definition;
    InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 6, 17), index, 1, 0.25, "", CALENDAR);
    InterestRateFutureTransactionDefinition expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_Q3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 9, 16), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(4, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_Q3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2014, 3, 17), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(5, Tenor.ONE_YEAR, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_Q3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2015, 6, 15), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
  }

  @Test
  public void testM3MIRFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "M3M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.98;
    marketValues.setDataPoint(marketDataId, rate);
    RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_M3M_ID, "Mapper");
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = futureNode.accept(converter);
    InterestRateFutureTransactionDefinition future = (InterestRateFutureTransactionDefinition) definition;
    InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 5, 13), index, 1, 0.25, "", CALENDAR);
    InterestRateFutureTransactionDefinition expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_M3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 7, 15), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(4, Tenor.of(Period.ZERO), Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_M3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 8, 19), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(5, Tenor.ONE_YEAR, Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_M3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2014, 9, 15), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
  }

  @Test
  public void testQ1MIRFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.98;
    marketValues.setDataPoint(marketDataId, rate);
    final double accrual = 1. / 12;
    RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_Q1M_ID, "Mapper");
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR1M_NAME, USDLIBOR_ACT_360, Tenor.ONE_MONTH);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = futureNode.accept(converter);
    InterestRateFutureTransactionDefinition future = (InterestRateFutureTransactionDefinition) definition;
    InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 6, 17), index, 1, accrual, "", CALENDAR);
    InterestRateFutureTransactionDefinition expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_Q1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 9, 16), index, 1, accrual, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(4, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_Q1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2014, 3, 17), index, 1, accrual, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(5, Tenor.ONE_YEAR, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_Q1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2015, 6, 15), index, 1, accrual, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, 1, now, rate).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
  }

  @Test
  public void testFixedIborRollDateSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMM Swap 0408");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.02;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final int startNumber = 4;
    final int endNumber = 8;
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_6MLIBOR3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0408");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("FixedIborIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.of(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final AnnuityDefinition<CouponFixedDefinition> fixedLeg = AnnuityDefinitionBuilder.couponFixedRollDate(Currency.USD, adjustedStartDate, startNumber, endNumber, adjuster,
        Period.ofMonths(6), 1, rate, true, THIRTY_360, CALENDAR, StubType.SHORT_START, FIXED_LEG_ROLL.getPaymentLag());
    assertEquals("FixedIborIMMSwap", swap.getFirstLeg(), fixedLeg);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_6MIMMLIBOR3MIMM_CONVENTION.getReceiveLegConvention())).getResetTenor());
    final AnnuityDefinition<CouponIborDefinition> iborLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index, 1, false, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("FixedIborIMMSwap", swap.getSecondLeg(), iborLeg);
  }

  @Test
  public void testIborIborRollDateSwap0408() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMM Basis Swap 0408");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.0015;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final int startNumber = 4;
    final int endNumber = 8;
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0408");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("IborIborIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.of(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final IborIndex index3M = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final AnnuityDefinition<CouponIborSpreadDefinition> ibor3MLeg = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, 
        adjuster, index3M, 1, spread, true, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getFirstLeg(), ibor3MLeg);
    final IborIndex index6M = ConverterUtils.indexIbor(USDLIBOR6M_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    final AnnuityDefinition<CouponIborDefinition> ibor6MLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, 
        adjuster, index6M, 1, false, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getSecondLeg(), ibor6MLeg);
  }

  /**
   * Test IMM basis swap 6M/3M+s with only one IMM quarterly period.
   */
  @Test
  public void testIborIborRollDateSwap0405() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMM Basis Swap 0405");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.0015;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final int startNumber = 4;
    final int endNumber = 5;
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0405");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("IborIborIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.of(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final IborIndex index3M = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_6MIMMLIBOR3MIMM_CONVENTION.getReceiveLegConvention())).getResetTenor());
    final AnnuityDefinition<CouponIborSpreadDefinition> ibor3MLeg = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, 
        adjuster, index3M, 1, spread, true, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getFirstLeg(), ibor3MLeg);
    final IborIndex index6M = ConverterUtils.indexIbor(USDLIBOR6M_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    final AnnuityDefinition<CouponIborDefinition> ibor6MLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, 
        index6M, 1, false, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getSecondLeg().getNumberOfPayments(), 1);
    assertEquals("IborIborIMMSwap", swap.getSecondLeg(), ibor6MLeg);
  }

  @Test
  public void testIborONRollDateSwap0408() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMM Basis Swap 0408");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.0015;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final int startNumber = 4;
    final int endNumber = 8;
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0408");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("IborONIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    final RollDateAdjuster adjuster = RollDateAdjusterFactory.of(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final IndexON indexON = ConverterUtils.indexON(USD_FEDFUND_INDEX.getName(), USD_OVERNIGHT_CONVENTION);
    final AnnuityDefinition<CouponONSpreadSimplifiedDefinition> onLeg = AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplifiedRollDate(adjustedStartDate, startNumber, endNumber,
        adjuster, ON_3M_LEG_ROLL.getPaymentTenor().getPeriod(), 1.0d, spread, indexON, true, CALENDAR, ON_3M_LEG_ROLL.getStubType(), ON_3M_LEG_ROLL.getPaymentLag());
    assertEquals("IborONIMMSwap", swap.getFirstLeg(), onLeg); // First leg is quote leg
    final IborIndex index3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final AnnuityDefinition<CouponIborDefinition> ibor3MLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index3m, 1,
        false, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborONIMMSwap", swap.getSecondLeg(), ibor3MLeg); // Second leg is non-quote leg.
  }

  @Test
  public void testFixedONCalendarSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "ECB swap 0204");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0045;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 12, 5);
    final int startNumber = 2;
    final int endNumber = 4;
    final Calendar ecb = new CalendarECBSettlements();
    final String ecbName = "ECB Settlement Calendar";
    //final ExternalId ecbId = ExternalId.of(SCHEME, ecbName);
    //final SimpleRegion region = new SimpleRegion();
    //region.addExternalId(ecbId);
    //region.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), ecbId.getValue()));
    //final Map<ExternalIdBundle, Region> regionMap = new HashMap<>();
    //regionMap.put(ecbId.toBundle(), region);
    //final Map<ExternalIdBundle, Calendar> calendarMap = new HashMap<>();
    //calendarMap.put(ecbId.toBundle(), ecb);
    final RegionSource regionSource = new MyRegionSource(new ExternalId[] {US, EU, GB}, new String[] {"US", "EU", "GB"}, new HashMap<ExternalIdBundle, Region>());
    final HolidaySource holidaySource = new MyHolidaySource(new ExternalId[] {US, EU, GB}, new Calendar[] {CALENDAR, TARGET, CALENDAR}, new HashMap<ExternalIdBundle, Calendar>());
    final ConfigSourceQuery<DateSet> calendarQuery = new ConfigSourceQuery<>(CONFIG_SOURCE, DateSet.class, VersionCorrection.LATEST);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CalendarSwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, holidaySource, regionSource, marketValues, marketDataId, now, calendarQuery);
    final Period startPeriod = Period.ofDays(1);
    final CalendarSwapNode swapNode = new CalendarSwapNode(ecbName, Tenor.of(startPeriod), startNumber, endNumber, EUR_SWAP_1Y_ONCMP_ID, false, SCHEME, "CalendarSwapNode0204");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("FixedONCalendarSwap: instance", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final LocalDate adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod)).toLocalDate();
    final LocalDate effectiveDate = CalendarBusinessDateUtils.nthNonGoodBusinessDate(adjustedStartDate, ecb, startNumber);
    final LocalDate maturityDate = CalendarBusinessDateUtils.nthNonGoodBusinessDate(adjustedStartDate, ecb, endNumber);
    final int nbCpnFixed = swap.getFirstLeg().getNumberOfPayments();
    assertTrue("FixedONCalendarSwap: nb Coupon", nbCpnFixed == 1);
    assertTrue("FixedONCalendarSwap: instance", swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedDefinition);
    final AnnuityCouponFixedDefinition fixedLeg = (AnnuityCouponFixedDefinition) swap.getFirstLeg();
    assertEquals("FixedONCalendarSwap: effective date", effectiveDate, fixedLeg.getNthPayment(0).getAccrualStartDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: maturity date", maturityDate, fixedLeg.getNthPayment(nbCpnFixed-1).getAccrualEndDate().toLocalDate());
    assertTrue("FixedONCalendarSwap: instance", swap.getSecondLeg().getNthPayment(0) instanceof CouponONSpreadSimplifiedDefinition);
    final AnnuityDefinition<?> onLeg = swap.getSecondLeg();
    final int nbCpnON = onLeg.getNumberOfPayments();
    assertTrue("FixedONCalendarSwap: nb Coupon", nbCpnON == 1);
    assertEquals("FixedONCalendarSwap: effective date", effectiveDate, ((CouponONSpreadSimplifiedDefinition)onLeg.getNthPayment(0)).getAccrualStartDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: effective date", effectiveDate, ((CouponONSpreadSimplifiedDefinition)onLeg.getNthPayment(0)).getFixingPeriodStartDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: effective date", maturityDate, ((CouponONSpreadSimplifiedDefinition)onLeg.getNthPayment(nbCpnON-1)).getAccrualEndDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: effective date", maturityDate, ((CouponONSpreadSimplifiedDefinition)onLeg.getNthPayment(nbCpnON-1)).getFixingPeriodEndDate().toLocalDate());
    final int startNumber2 = 1;
    final int endNumber2 = 14; // More than 1 Year: 2 cpn
    final Period startPeriod2 = Period.ofMonths(1);
    final CalendarSwapNode swapNode2 = new CalendarSwapNode(ecbName, Tenor.of(startPeriod2), startNumber2, endNumber2, EUR_SWAP_1Y_ONCMP_ID, false, SCHEME, "CalendarSwapNode0204");
    final InstrumentDefinition<?> definition2 = swapNode2.accept(converter);
    assertTrue("FixedONCalendarSwap: instance", definition2 instanceof SwapDefinition);
    final SwapDefinition swap2 = (SwapDefinition) definition2;
    final LocalDate adjustedStartDate2 = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod2)).toLocalDate();
    final LocalDate effectiveDate2 = CalendarBusinessDateUtils.nthNonGoodBusinessDate(adjustedStartDate2, ecb, startNumber2);
    final LocalDate maturityDate2 = CalendarBusinessDateUtils.nthNonGoodBusinessDate(adjustedStartDate2, ecb, endNumber2);
    final int nbCpnFixed2 = swap2.getFirstLeg().getNumberOfPayments();
    assertTrue("FixedONCalendarSwap: nb Coupon", nbCpnFixed2 == 2);
    assertTrue("FixedONCalendarSwap: instance", swap2.getFirstLeg().getNthPayment(0) instanceof CouponFixedDefinition);
    final AnnuityCouponFixedDefinition fixedLeg2 = (AnnuityCouponFixedDefinition) swap2.getFirstLeg();
    assertEquals("FixedONCalendarSwap: effective date", effectiveDate2, fixedLeg2.getNthPayment(0).getAccrualStartDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: maturity date", maturityDate2, fixedLeg2.getNthPayment(nbCpnFixed2-1).getAccrualEndDate().toLocalDate());
    assertTrue("FixedONCalendarSwap: instance", swap2.getSecondLeg().getNthPayment(0) instanceof CouponONSpreadSimplifiedDefinition);
    final AnnuityDefinition<?> onLeg2 = swap2.getSecondLeg();
    final int nbCpnON2 = onLeg2.getNumberOfPayments();
    assertTrue("FixedONCalendarSwap: nb Coupon", nbCpnON2 == 2);
    assertEquals("FixedONCalendarSwap: effective date", effectiveDate2, ((CouponONSpreadSimplifiedDefinition)onLeg2.getNthPayment(0)).getAccrualStartDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: effective date", effectiveDate2, ((CouponONSpreadSimplifiedDefinition)onLeg2.getNthPayment(0)).getFixingPeriodStartDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: effective date", maturityDate2, ((CouponONSpreadSimplifiedDefinition)onLeg2.getNthPayment(nbCpnON2-1)).getAccrualEndDate().toLocalDate());
    assertEquals("FixedONCalendarSwap: effective date", maturityDate2, ((CouponONSpreadSimplifiedDefinition)onLeg2.getNthPayment(nbCpnON2-1)).getFixingPeriodEndDate().toLocalDate());
  }

  @Test
  public void testIndexIbor() {
    final Period tenor = Period.ofMonths(3);
    final IborIndex index = NodeConverterUtils.indexIbor(USDLIBOR_ACT_360, tenor);
    final IborIndex indexExpected = new IborIndex(USDLIBOR_ACT_360.getCurrency(), tenor, USDLIBOR_ACT_360.getSettlementDays(),
       USDLIBOR_ACT_360.getDayCount(), USDLIBOR_ACT_360.getBusinessDayConvention(), USDLIBOR_ACT_360.isIsEOM(), USDLIBOR_ACT_360_CONVENTION_NAME);
    assertEquals("indexIbor", indexExpected, index);
  }

  @Test
  public void testFixedIborSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.02;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final Period swapTenor = Period.ofYears(2);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    final IborIndex index = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), FIXED_LEG_ID, LEG_USDLIBOR3M_ID, "Mapper");
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    ZonedDateTime maturityDate = settlementDate.plus(swapTenor);
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    AnnuityCouponFixedDefinition fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG.getPaymentTenor().getPeriod(),
        CALENDAR, FIXED_LEG.getDayCount(), FIXED_LEG.getBusinessDayConvention(), FIXED_LEG.isIsEOM(), 1.0d, rate, true, FIXED_LEG.getStubType(), FIXED_LEG.getPaymentLag());
    AnnuityCouponIborDefinition floatLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 1.0d, index, false,
        index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), LEG_USDLIBOR3M_ID, FIXED_LEG_ID, "Mapper");
    settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    maturityDate = settlementDate.plus(swapTenor);
    definition = swapNode.accept(converter);
    assertTrue("FixedIborSwap", definition instanceof SwapDefinition);
    fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG.getPaymentTenor().getPeriod(),
        CALENDAR, FIXED_LEG.getDayCount(), FIXED_LEG.getBusinessDayConvention(), FIXED_LEG.isIsEOM(), 1.0d, rate,false, FIXED_LEG.getStubType(), FIXED_LEG.getPaymentLag());
    floatLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 1.0d, index, true,
        index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    assertEquals("FixedIborSwap", new SwapDefinition(floatLeg, fixedLeg), definition);
    swapNode = new SwapNode(Tenor.FIVE_MONTHS, Tenor.of(swapTenor), FIXED_LEG_ID, LEG_USDLIBOR3M_ID, "Mapper");
    settlementDate = DateUtils.getUTCDate(2013, 8, 5);
    maturityDate = settlementDate.plus(swapTenor);
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG.getPaymentTenor().getPeriod(),
        CALENDAR, FIXED_LEG.getDayCount(), FIXED_LEG.getBusinessDayConvention(), FIXED_LEG.isIsEOM(), 1.0d, rate, true, FIXED_LEG.getStubType(), FIXED_LEG.getPaymentLag());
    floatLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 1.0d, index, false,
        index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    assertEquals("FixedIborSwap", new SwapDefinition(fixedLeg, floatLeg), definition);
  }

  @Test
  public void testIborIborSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3Mx6M basis spread");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.001;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final Period swapTenor = Period.ofYears(2);
    ZonedDateTime maturityDate = settlementDate.plus(swapTenor);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), LEG_USDLIBOR3M_ID, LEG_USDLIBOR6M_ID, "Mapper");
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final IborIndex index3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final IborIndex index6m = ConverterUtils.indexIbor(USDLIBOR6M_NAME, USDLIBOR_ACT_360, Tenor.SIX_MONTHS);
    AnnuityDefinition<CouponIborSpreadDefinition> payLeg = AnnuityDefinitionBuilder.couponIborSpread(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 
        1.0d, spread, index3m, true, index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), 
        LEG_USDLIBOR3M.getPaymentLag());
    final AnnuityCouponIborDefinition receiveLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR6M.getResetTenor().getPeriod(),
        1.0d, index6m, false, index6m.getDayCount(), index6m.getBusinessDayConvention(), index6m.isEndOfMonth(), CALENDAR, LEG_USDLIBOR6M.getStubType(), 
        LEG_USDLIBOR6M.getPaymentLag());
    assertEquals(new SwapDefinition(payLeg, receiveLeg), definition);
    settlementDate = DateUtils.getUTCDate(2014, 3, 5);
    maturityDate = settlementDate.plus(swapTenor);
    swapNode = new SwapNode(Tenor.ONE_YEAR, Tenor.of(swapTenor), LEG_USDLIBOR3M_ID, LEG_USDLIBOR6M_ID, "Mapper");
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    payLeg = AnnuityDefinitionBuilder.couponIborSpread(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 
        1.0d, spread, index3m, true, index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    final AnnuityCouponIborDefinition spreadLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR6M.getResetTenor().getPeriod(),
        1.0d, index6m, false, index6m.getDayCount(), index6m.getBusinessDayConvention(), index6m.isEndOfMonth(), CALENDAR, LEG_USDLIBOR6M.getStubType(), LEG_USDLIBOR6M.getPaymentLag());
    assertEquals(new SwapDefinition(payLeg, spreadLeg), definition);
  }

  @Test
  public void testXCcyIborIborSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "USD/EUR basis spread");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.001;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    // First test
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final Period swapTenor = Period.ofYears(1);
    ZonedDateTime maturityDate = settlementDate.plus(swapTenor);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), LEG_USDLIBOR3M_ID, LEG_EURIBOR3M_ID, "Mapper");
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final IborIndex usdLibor3M = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final IborIndex euribor3M = ConverterUtils.indexIbor(EURIBOR3M_NAME, EURIBOR_CONVENTION, Tenor.THREE_MONTHS);
    AnnuityDefinition<CouponDefinition> quoteLeg = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(settlementDate, maturityDate, 1.0, spread,
        usdLibor3M, true, CALENDAR, LEG_USDLIBOR3M.getStubType(), 2, true, true);
    final double notional = FX_MATRIX.getFxRate(Currency.USD, Currency.EUR);
    AnnuityDefinition<CouponDefinition> nonQuoteLeg = AnnuityDefinitionBuilder.couponIborWithNotional(settlementDate, maturityDate, notional,
        euribor3M, false, CALENDAR, LEG_EURIBOR3M.getStubType(), 0, true, true);
    assertEquals(new SwapDefinition(quoteLeg, nonQuoteLeg), definition);
    // Second test
    settlementDate = DateUtils.getUTCDate(2014, 3, 5);
    maturityDate = settlementDate.plus(swapTenor);
    swapNode = new SwapNode(Tenor.ONE_YEAR, Tenor.of(swapTenor), LEG_USDLIBOR3M_ID, LEG_EURIBOR3M_ID, "Mapper");
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    quoteLeg = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(settlementDate, maturityDate, 1.0, spread, 
        usdLibor3M, true, CALENDAR, LEG_USDLIBOR3M.getStubType(), 2, true, true);
    nonQuoteLeg = AnnuityDefinitionBuilder.couponIborWithNotional(settlementDate, maturityDate, notional, 
        euribor3M, false, CALENDAR, LEG_EURIBOR3M.getStubType(), 0, true, true);
    assertEquals(new SwapDefinition(quoteLeg, nonQuoteLeg), definition);
  }

  @Test
  public void testIborONAASwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "FF basis spread");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.001;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    final Tenor tenor = Tenor.FIVE_YEARS;
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), tenor,ON_AA_ID, LEG_USDLIBOR3M_ID, "Mapper");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime maturity = settlementDate.plus(tenor.getPeriod());
    final IndexON indexON = ConverterUtils.indexON(USD_FEDFUND_INDEX.getName(), USD_OVERNIGHT_CONVENTION);
    final AnnuityDefinition<CouponONArithmeticAverageSpreadSimplifiedDefinition> quoteLeg = AnnuityDefinitionBuilder.couponONArithmeticAverageSpreadSimplified(settlementDate, maturity,
        ON_AA.getPaymentTenor().getPeriod(), 1.0, spread, indexON, true, ON_AA.getBusinessDayConvention(), ON_AA.isIsEOM(), CALENDAR, ON_AA.getStubType());
    for(int loopcpn=0; loopcpn<quoteLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborONAASwap: second leg - cpn " + loopcpn, quoteLeg.getNthPayment(loopcpn), (swap.getFirstLeg().getNthPayment(loopcpn)));
    }
    final IborIndex index3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final AnnuityDefinition<CouponIborDefinition> nonQuoteLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturity, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 
        1.0d, index3m, false, ACT_360, USDLIBOR_ACT_360.getBusinessDayConvention(), USDLIBOR_ACT_360.isIsEOM(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    for(int loopcpn=0; loopcpn<nonQuoteLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborONAASwap: first leg - cpn " + loopcpn, nonQuoteLeg.getNthPayment(loopcpn), (swap.getSecondLeg().getNthPayment(loopcpn)));
    }
  }

  @Test
  public void testIborIborCompoundingSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3Mx6M basis spread");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.001;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final Tenor legTenor = Tenor.TWO_YEARS;
    final ZonedDateTime maturityDate = settlementDate.plus(legTenor.getPeriod());
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), legTenor, LIBOR_1M_CMP_3M_ID, LEG_USDLIBOR3M_ID, "Mapper");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final Period paymentPeriod = Period.ofMonths(3);
    final IborIndex index3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final IborIndex index1m = ConverterUtils.indexIbor(USDLIBOR1M_NAME, USDLIBOR_ACT_360, Tenor.ONE_MONTH);
    final AnnuityDefinition<CouponIborCompoundingSpreadDefinition> quoteLeg = AnnuityDefinitionBuilder.couponIborCompoundingSpread(settlementDate, settlementDate.plus(legTenor.getPeriod()), paymentPeriod, 1, spread,
        index1m, StubType.SHORT_START, true, MODIFIED_FOLLOWING, true, CALENDAR, StubType.SHORT_START);
    for(int loopcpn=0; loopcpn<quoteLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborIborCompoundingSwap: quote leg - cpn " + loopcpn, quoteLeg.getNthPayment(loopcpn), ((SwapDefinition)definition).getFirstLeg().getNthPayment(loopcpn));
    }
    assertEquals("IborIborCompoundingSwap: quote leg", quoteLeg, ((SwapDefinition)definition).getFirstLeg());
    final AnnuityCouponIborDefinition nonQuoteLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 1.0d, 
        index3m, false, index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    assertEquals("IborIborCompoundingSwap: non-quote leg", nonQuoteLeg, ((SwapDefinition)definition).getSecondLeg());
  }

  @Test
  public void testIborIborCompoundingFlatSwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3Mx6M basis spread");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.001;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final Tenor legTenor = Tenor.TWO_YEARS;
    final ZonedDateTime maturityDate = settlementDate.plus(legTenor.getPeriod());
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), legTenor, LIBOR_1M_CMP_FLAT_3M_ID, LEG_USDLIBOR3M_ID, "Mapper");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final Period paymentPeriod = Period.ofMonths(3);
    final IborIndex index3m = ConverterUtils.indexIbor(USDLIBOR3M_NAME, USDLIBOR_ACT_360, Tenor.THREE_MONTHS);
    final IborIndex index1m = ConverterUtils.indexIbor(USDLIBOR1M_NAME, USDLIBOR_ACT_360, Tenor.ONE_MONTH);
    final AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> quoteLeg = AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(settlementDate,
        settlementDate.plus(legTenor.getPeriod()), paymentPeriod, 1, spread, index1m, StubType.SHORT_START, true, MODIFIED_FOLLOWING, true, CALENDAR, StubType.SHORT_START);
    final AnnuityCouponIborDefinition nonQuoteLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_USDLIBOR3M.getResetTenor().getPeriod(), 1.0d, 
        index3m, false, index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_USDLIBOR3M.getStubType(), LEG_USDLIBOR3M.getPaymentLag());
    assertEquals("IborIborCompoundingSwap: first leg", nonQuoteLeg, ((SwapDefinition)definition).getSecondLeg());
    for(int loopcpn=0; loopcpn<quoteLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborIborCompoundingSwap: first leg - cpn " + loopcpn, quoteLeg.getNthPayment(loopcpn), ((SwapDefinition)definition).getFirstLeg().getNthPayment(loopcpn));
    }
    assertEquals("IborIborCompoundingSwap: first leg", quoteLeg, ((SwapDefinition)definition).getFirstLeg());
  }

  @Test
  public void testOIS() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.001;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final Period swapTenor = Period.ofYears(2);
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), FIXED_LEG_PAY_LAG_ID, LEG_ON_CMP_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now, FX_MATRIX);
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final IndexON index = ConverterUtils.indexON(USD_FEDFUND_INDEX_NAME, USD_OVERNIGHT_CONVENTION);
    ZonedDateTime maturityDate = settlementDate.plus(swapTenor);
    AnnuityCouponFixedDefinition fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG_PAY_LAG.getPaymentTenor().getPeriod(),
        CALENDAR, ACT_360, MODIFIED_FOLLOWING, false, 1, rate, true, StubType.SHORT_START, FIXED_LEG_PAY_LAG.getPaymentLag());
    AnnuityDefinition<CouponONSpreadSimplifiedDefinition> floatLeg = AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(settlementDate, maturityDate,
        LEG_ON_CMP.getPaymentTenor().getPeriod(), 1.0d, 0.0, index, false, LEG_ON_CMP.getBusinessDayConvention(), LEG_ON_CMP.isIsEOM(), CALENDAR, LEG_ON_CMP.getStubType(), LEG_ON_CMP.getPaymentLag());
    assertEquals(new SwapDefinition(fixedLeg, floatLeg), definition);
    settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), LEG_ON_CMP_ID, FIXED_LEG_PAY_LAG_ID, "Mapper");
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG_PAY_LAG.getPaymentTenor().getPeriod(), CALENDAR, ACT_360,
        MODIFIED_FOLLOWING, false, 1, rate, false, StubType.SHORT_START, FIXED_LEG_PAY_LAG.getPaymentLag());
    floatLeg = AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(settlementDate, maturityDate,
        LEG_ON_CMP.getPaymentTenor().getPeriod(), 1.0d, 0.0, index, true, LEG_ON_CMP.getBusinessDayConvention(), LEG_ON_CMP.isIsEOM(), CALENDAR, LEG_ON_CMP.getStubType(), LEG_ON_CMP.getPaymentLag());
    assertEquals(new SwapDefinition(floatLeg, fixedLeg), definition);
    settlementDate = DateUtils.getUTCDate(2013, 4, 5);
    swapNode = new SwapNode(Tenor.ONE_MONTH, Tenor.of(swapTenor), FIXED_LEG_PAY_LAG_ID, LEG_ON_CMP_ID, "Mapper");
    maturityDate = settlementDate.plus(swapTenor);
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG_PAY_LAG.getPaymentTenor().getPeriod(), CALENDAR, ACT_360,
        MODIFIED_FOLLOWING, false, 1, rate, true, StubType.SHORT_START, FIXED_LEG_PAY_LAG.getPaymentLag());
    floatLeg = AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplified(settlementDate, maturityDate,
        LEG_ON_CMP.getPaymentTenor().getPeriod(), 1.0d, 0.0, index, false, LEG_ON_CMP.getBusinessDayConvention(), LEG_ON_CMP.isIsEOM(), CALENDAR, LEG_ON_CMP.getStubType(), LEG_ON_CMP.getPaymentLag());
    assertEquals(new SwapDefinition(fixedLeg, floatLeg), definition);
  }

  @Test
  public void testThreeLegSwapNode() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "EUR Basis Three Leg 3Mx6M");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.0010;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final Tenor tenorStart = Tenor.ONE_YEAR;
    final Tenor tenorSwap = Tenor.ONE_YEAR;
    final ThreeLegBasisSwapNode node = new ThreeLegBasisSwapNode(tenorStart, tenorSwap, LEG_EURIBOR3M_ID, LEG_EURIBOR6M_ID, EUR1Y_FIXED_ID, false, "Mapper", "1Yx3Y baiss");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new ThreeLegBasisSwapNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final SwapMultilegDefinition swap = (SwapMultilegDefinition) definition;
    assertTrue("ThreeLegSwapNode: correct type", definition instanceof SwapMultilegDefinition);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(now, EUR1Y_FIXED.getSettlementDays(), CALENDAR);
    final ZonedDateTime effectiveDate = ScheduleCalculator.getAdjustedDate(spot, tenorStart.getPeriod(), EUR1Y_FIXED.getBusinessDayConvention(), CALENDAR, EUR1Y_FIXED.isIsEOM());
    final ZonedDateTime maturityDate = effectiveDate.plus(tenorSwap.getPeriod());
    final AnnuityCouponFixedDefinition spreadLeg = AnnuityDefinitionBuilder.couponFixed(EUR1Y_FIXED.getCurrency(), effectiveDate, maturityDate, EUR1Y_FIXED.getPaymentTenor().getPeriod(),
        CALENDAR, EUR1Y_FIXED.getDayCount(), EUR1Y_FIXED.getBusinessDayConvention(), EUR1Y_FIXED.isIsEOM(), 1.0d, spread, true, EUR1Y_FIXED.getStubType(), 0);
    assertEquals("ThreeLegSwapNode: spread leg", spreadLeg, swap.getLegs()[0]);
    final IborIndex euribor3M = ConverterUtils.indexIbor(EURIBOR3M_NAME, EURIBOR_CONVENTION, Tenor.THREE_MONTHS);
    final AnnuityDefinition<CouponIborDefinition>  associatedLeg = AnnuityDefinitionBuilder.couponIbor(effectiveDate, maturityDate, LEG_EURIBOR3M.getResetTenor().getPeriod(), 1.0d, euribor3M, true,
        euribor3M.getDayCount(), euribor3M.getBusinessDayConvention(), euribor3M.isEndOfMonth(), CALENDAR, LEG_EURIBOR3M.getStubType(), 0);
    assertEquals("ThreeLegSwapNode: associated leg", associatedLeg, swap.getLegs()[1]);
    final IborIndex euribor6M = ConverterUtils.indexIbor(EURIBOR6M_NAME, EURIBOR_CONVENTION, Tenor.SIX_MONTHS);
    final AnnuityDefinition<CouponIborDefinition>  otherLeg = AnnuityDefinitionBuilder.couponIbor(effectiveDate, maturityDate, LEG_EURIBOR6M.getResetTenor().getPeriod(), 1.0d, euribor6M, false,
        euribor6M.getDayCount(), euribor6M.getBusinessDayConvention(), euribor6M.isEndOfMonth(), CALENDAR, LEG_EURIBOR3M.getStubType(), 0);
    assertEquals("ThreeLegSwapNode: associated leg", otherLeg, swap.getLegs()[2]);
  }

  @Test
  public void testFXForward() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.4;
    marketValues.setDataPoint(marketDataId, forward);
    final Tenor tenorFx = Tenor.ONE_YEAR;
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), tenorFx, FX_FORWARD_ID, Currency.USD, Currency.CAD, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, NOW);
    final InstrumentDefinition<?> definition = node.accept(converter);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(NOW, FX_SPOT.getSettlementDays(), CALENDAR);
    final ZonedDateTime payDate = ScheduleCalculator.getAdjustedDate(spotDate, tenorFx.getPeriod(), FX_FORWARD.getBusinessDayConvention(), CALENDAR, FX_FORWARD.isIsEOM());
    final PaymentFixedDefinition payment1 = new PaymentFixedDefinition(Currency.USD, payDate, 1);
    final PaymentFixedDefinition payment2 = new PaymentFixedDefinition(Currency.CAD, payDate, -forward);
    assertEquals(new ForexDefinition(payment1, payment2), definition);
  }

  @Test
  public void testFedFundFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "FF Future");
    final double rate = 0.98;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.ONE_MONTH, Tenor.ONE_DAY, FED_FUND_FUTURE_ID, "Mapper");
    final IndexON index = ConverterUtils.indexON(USD_FEDFUND_INDEX_NAME, USD_OVERNIGHT_CONVENTION);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(SECURITY_SOURCE, CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final InstrumentDefinition<?> definition = futureNode.accept(converter);
    final FederalFundsFutureTransactionDefinition future = (FederalFundsFutureTransactionDefinition) definition;
    final FederalFundsFutureSecurityDefinition securityDefinition = FederalFundsFutureSecurityDefinition.from(DateUtils.getUTCDate(2013, 5, 1), index, 1, 1. / 12, "", CALENDAR);
    final FederalFundsFutureTransactionDefinition expectedFuture = new FederalFundsFutureTransactionDefinition(securityDefinition, 1, now, rate);
    assertEquals(expectedFuture, future);
  }

  /**
   * Test to be corrected when the node contains the swap rate.
   */
  @Test(enabled = false)
  public void testDeliverableSwapFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "DSF");
    final double rate = 0.02;
    final double price = 0.99;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final DeliverableSwapFutureNode futureNode = new DeliverableSwapFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.TEN_YEARS, DELIVERABLE_SWAP_FUTURE_ID, FIXED_IBOR_3M_SWAP_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new DeliverableSwapFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, NOW);
    final InstrumentDefinition<?> definition = futureNode.accept(converter);
    final Currency currency = Currency.USD;
    final DayCount dayCount = THIRTY_360;
    final BusinessDayConvention businessDayConvention = MODIFIED_FOLLOWING;
    final boolean eom = false;
    final Period indexTenor = Period.ofMonths(3);
    final int spotLagIndex = 2;
    final IborIndex iborIndex = new IborIndex(currency, indexTenor, spotLagIndex, dayCount, businessDayConvention, eom, "USD 3m Libor");
    final GeneratorSwapFixedIbor generator = new GeneratorSwapFixedIbor("", Period.ofMonths(6), ACT_360, iborIndex, CALENDAR);
    final SwapFixedIborDefinition underlying = SwapFixedIborDefinition.from(DateUtils.getUTCDate(2013, 6, 19), Period.ofYears(10), generator, 1, rate, false);
    final SwapFuturesPriceDeliverableSecurityDefinition securityDefinition = new SwapFuturesPriceDeliverableSecurityDefinition(DateUtils.getUTCDate(2013, 6, 17), underlying, 1);
    final SwapFuturesPriceDeliverableTransactionDefinition transaction = new SwapFuturesPriceDeliverableTransactionDefinition(securityDefinition, 1, NOW, price);
    assertEquals(transaction, definition);
  }
  
//  private IndexON index(final OvernightIndex index) {
//    final OvernightIndexConvention convention = (OvernightIndexConvention) CONVENTION_SOURCE.getSingle(index.getConventionId());
//    return new IndexON(index.getName(), convention.getCurrency(), convention.getDayCount(), convention.getPublicationLag());
//  }


  /**
   * A simplified local version of a HolidaySource for tests.
   */
  private static class MySecuritySource implements SecuritySource {
    
    /** Security source as a map for tests **/
    private final Map<ExternalIdBundle, Security> _map;
    
    /**
     * @param map The map of id/Security
     */
    public MySecuritySource(Map<ExternalIdBundle, Security> map) {
      super();
      _map = map;
    }

    @Override
    public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Collection<Security>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Collection<Security> get(ExternalIdBundle bundle) {
      return null;
    }

    @Override
    public Security getSingle(ExternalIdBundle bundle) {
      return _map.get(bundle);
    }

    @Override
    public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      return _map.containsKey(bundle) ? _map.get(bundle) : null;
    }

    @Override
    public Map<ExternalIdBundle, Security> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Security get(UniqueId uniqueId) {
      return null;
    }

    @Override
    public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<UniqueId, Security> get(Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, Security> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return null;
    }
    
  }


  /**
   * A simplified local version of a HolidaySource for tests.
   */
  private static class MyHolidaySource implements HolidaySource {
    private final Map<ExternalIdBundle, Calendar> _map;

    public MyHolidaySource(final ExternalId[] ids, final Calendar[] calendars) {
      _map = new HashMap<>();
      init(ids, calendars, _map);
    }

    public MyHolidaySource(final ExternalId[] ids, final Calendar[] calendars, final Map<ExternalIdBundle, Calendar> map) {
      _map = map;
      init(ids, calendars, map);
    }

    private static void init(final ExternalId[] ids, final Calendar[] calendars, final Map<ExternalIdBundle, Calendar> map) {
      final int nbRegion = calendars.length;
      for(int loopc=0; loopc<nbRegion; loopc++) {
        map.put(ExternalIdBundle.of(ids[loopc]), calendars[loopc]);
      }
    }

    @Override
    public Holiday get(final UniqueId uniqueId) {
      return null;
    }

    @Override
    public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Collection<Holiday> get(HolidayType holidayType,
                                   ExternalIdBundle regionOrExchangeIds) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Holiday> get(Currency currency) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
      return Collections.emptyMap();
    }

    @Override
    public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      return Collections.emptyMap();
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return false;
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
      return !_map.get(regionOrExchangeIds).isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
      return false;
    }

  }

  /**
   * A simplified local version of a RegionSource for tests.
   */
  private static class MyRegionSource implements RegionSource {
    private final Map<ExternalIdBundle, Region> _map;

    public MyRegionSource(final ExternalId[] ids, final String[] countryId) {
      _map = new HashMap<>();
      init(ids, countryId, _map);
    }

    public MyRegionSource(final ExternalId[] ids, final String[] countryId, final Map<ExternalIdBundle, Region> map) {
      _map = map;
      init(ids, countryId, map);
    }

    private static void init(final ExternalId[] ids, final String[] countryId, final Map<ExternalIdBundle, Region> map) {
      final int nbRegion = countryId.length;
      for(int loopr=0; loopr<nbRegion; loopr++) {
        final SimpleRegion region = new SimpleRegion();
        final ExternalId id = ExternalSchemes.financialRegionId(countryId[loopr]);
        region.addExternalId(id);
        region.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), id.getValue()));
        map.put(ExternalIdBundle.of(ids[loopr]), region);
      }
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      return Collections.singleton(_map.get(bundle));
    }

    @Override
    public Map<ExternalIdBundle, Collection<Region>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      final Map<ExternalIdBundle, Collection<Region>> result = new HashMap<>();
      for (final ExternalIdBundle bundle : bundles) {
        final Region region = _map.get(bundle);
        if (region != null) {
          result.put(bundle, Collections.singleton(region));
        }
      }
      return result;
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle) {
      final Region region = _map.get(bundle);
      if (region != null) {
        return Collections.singleton(region);
      }
      return Collections.emptySet();
    }

    @Override
    public Region getSingle(final ExternalIdBundle bundle) {
      return _map.get(bundle);
    }

    @Override
    public Region getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      return _map.get(bundle);
    }

    @Override
    public Map<ExternalIdBundle, Region> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      final Map<ExternalIdBundle, Region> result = new HashMap<>();
      for (final ExternalIdBundle bundle : bundles) {
        final Region region = _map.get(bundle);
        if (region != null) {
          result.put(bundle, region);
        }
      }
      return result;
    }

    @Override
    public Region get(final UniqueId uniqueId) {
      return null;
    }

    @Override
    public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public Map<UniqueId, Region> get(final Collection<UniqueId> uniqueIds) {
      return null;
    }

    @Override
    public Map<ObjectId, Region> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return DummyChangeManager.INSTANCE;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalId externalId) {
      return _map.get(ExternalIdBundle.of(externalId));
    }

    @Override
    public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
      return _map.get(bundle);
    }
  }

}

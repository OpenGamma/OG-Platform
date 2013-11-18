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

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
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
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.impl.SimpleHoliday;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
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
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
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
  private static final String SCHEME = "Test";
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount THIRTY_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId EU = ExternalSchemes.financialRegionId("EU");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  private static final ExternalId FIXED_LEG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg");
  private static final ExternalId FIXED_LEG_PAY_LAG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg Pay Lag");
  private static final ExternalId DEPOSIT_1D_ID = ExternalId.of(SCHEME, "USD 1d Deposit");
  private static final ExternalId DEPOSIT_1M_ID = ExternalId.of(SCHEME, "USD 1m Deposit");
  private static final String USDLIBOR_ACT_360_NAME = "USD Libor ACT/360";
  private static final ExternalId USDLIBOR_ACT_360_ID = ExternalId.of(SCHEME, USDLIBOR_ACT_360_NAME);
  private static final String USDLIBOR_30_360_NAME = "USD Libor 30/360";
  private static final ExternalId USDLIBOR_30_360_ID = ExternalId.of(SCHEME, USDLIBOR_30_360_NAME);
  private static final ExternalId RATE_FUTURE_3M3M_ID = ExternalId.of(SCHEME, "USD 3m/3m Rate Future");
  private static final ExternalId RATE_FUTURE_3M1M_ID = ExternalId.of(SCHEME, "USD 3m/1m Rate Future");
  private static final ExternalId RATE_FUTURE_1M1M_ID = ExternalId.of(SCHEME, "USD 1m/1m Rate Future");
  private static final ExternalId FED_FUND_FUTURE_ID = ExternalId.of(SCHEME, "FF Future");
  private static final ExternalId DELIVERABLE_SWAP_FUTURE_ID = ExternalId.of(SCHEME, "DSF");
  private static final ExternalId LEG_3M_IBOR_ID = ExternalId.of(SCHEME, "USD 3m Floating Leg");
  private static final ExternalId SWAP_6M_IBOR_ID = ExternalId.of(SCHEME, "USD 6m Floating Leg");
  private static final ExternalId OVERNIGHT_ID = ExternalId.of(SCHEME, "USD Overnight");
  private static final ExternalId LEG_ON_CMP_ID = ExternalId.of(SCHEME, "USD OIS Leg");
  private static final String ON_AA_NAME = "USD ON Arith. Average Leg";
  private static final ExternalId ON_AA_ID = ExternalId.of(SCHEME, ON_AA_NAME);
  private static final ExternalId FX_FORWARD_ID = ExternalId.of(SCHEME, "FX Forward");
  private static final ExternalId FX_SPOT_ID = ExternalId.of(SCHEME, "FX Spot");
  private static final ExternalId IMM_3M_FUTURE_EXPIRY_CONVENTION = ExternalId.of(SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME);
  private static final ExternalId IMM_1M_FUTURE_EXPIRY_CONVENTION = ExternalId.of(SCHEME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME);
  private static final ExternalId FIXED_IBOR_3M_SWAP_ID = ExternalId.of(SCHEME, "Swap");
  private static final SwapFixedLegConvention FIXED_LEG = new SwapFixedLegConvention("USD Swap Fixed Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 0);
  private static final SwapFixedLegConvention FIXED_LEG_PAY_LAG = new SwapFixedLegConvention("USD Swap Fixed Leg Pay Lag", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention LEG_3M_LIBOR = new VanillaIborLegConvention("USD 3m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 3m Floating Leg")),
      USDLIBOR_ACT_360_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention LEG_6M_LIBOR = new VanillaIborLegConvention("USD 6m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 6m Floating Leg")),
      USDLIBOR_30_360_ID, false, SCHEME, Tenor.SIX_MONTHS, 2, false, StubType.LONG_END, false, 2);
  private static final String LIBOR_1M_CMP_3M_NAME = "USD 1M x 3M Ibor Cmp Leg";
  private static final ExternalId LIBOR_1M_CMP_3M_ID = ExternalId.of(SCHEME, LIBOR_1M_CMP_3M_NAME);
  private static final CompoundingIborLegConvention LIBOR_1M_CMP_3M = new CompoundingIborLegConvention(LIBOR_1M_CMP_3M_NAME,
      ExternalIdBundle.of(LIBOR_1M_CMP_3M_ID), USDLIBOR_ACT_360_ID, Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING,
      Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, false, 0);
  private static final String LIBOR_1M_CMP_FLAT_3M_NAME = "USD 1M x 3M Ibor Cmp Flat Leg";
  private static final ExternalId LIBOR_1M_CMP_FLAT_3M_ID = ExternalId.of(SCHEME, LIBOR_1M_CMP_FLAT_3M_NAME);
  private static final CompoundingIborLegConvention LIBOR_1M_CMP_FLAT_3M = new CompoundingIborLegConvention(LIBOR_1M_CMP_FLAT_3M_NAME,
      ExternalIdBundle.of(LIBOR_1M_CMP_FLAT_3M_ID), USDLIBOR_ACT_360_ID, Tenor.THREE_MONTHS, CompoundingType.FLAT_COMPOUNDING,
      Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, false, 0);
  private static final OISLegConvention LEG_ON_CMP = new OISLegConvention("USD OIS Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD OIS Leg")), OVERNIGHT_ID,
      Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 2);
  private static final ONArithmeticAverageLegConvention ON_AA = new ONArithmeticAverageLegConvention(ON_AA_NAME, ExternalIdBundle.of(ExternalId.of(SCHEME, ON_AA_NAME)), 
      OVERNIGHT_ID, Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 0);
  private static final DepositConvention DEPOSIT_1D = new DepositConvention("USD 1d Deposit", ExternalIdBundle.of(DEPOSIT_1D_ID),
      ACT_360, MODIFIED_FOLLOWING, 0, false, Currency.USD, US);
  private static final DepositConvention DEPOSIT_1M = new DepositConvention("USD 1m Deposit", ExternalIdBundle.of(DEPOSIT_1M_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
  private static final IborIndexConvention LIBOR_ACT_360 = new IborIndexConvention(USDLIBOR_ACT_360_NAME, ExternalIdBundle.of(USDLIBOR_ACT_360_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  private static final IborIndexConvention LIBOR_30_360 = new IborIndexConvention(USDLIBOR_30_360_NAME, ExternalIdBundle.of(USDLIBOR_30_360_ID),
      THIRTY_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  private static final InterestRateFutureConvention RATE_FUTURE_3M3M = new InterestRateFutureConvention("USD 3m/3m Rate Future", ExternalIdBundle.of(RATE_FUTURE_3M3M_ID),
      IMM_3M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR_ACT_360_ID);
  private static final InterestRateFutureConvention RATE_FUTURE_3M1M = new InterestRateFutureConvention("USD 3m/1m Rate Future", ExternalIdBundle.of(RATE_FUTURE_3M1M_ID),
      IMM_3M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR_ACT_360_ID);
  private static final InterestRateFutureConvention RATE_FUTURE_1M1M = new InterestRateFutureConvention("USD 1m/1m Rate Future", ExternalIdBundle.of(RATE_FUTURE_1M1M_ID),
      IMM_1M_FUTURE_EXPIRY_CONVENTION, NYLON, USDLIBOR_ACT_360_ID);
  private static final OvernightIndexConvention OVERNIGHT = new OvernightIndexConvention("USD Overnight", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Overnight")),
      ACT_360, 1, Currency.USD, NYLON);
  private static final FXSpotConvention FX_SPOT = new FXSpotConvention("FX Spot", ExternalIdBundle.of(ExternalId.of(SCHEME, "FX Spot")), 1, US);
  private static final FXForwardAndSwapConvention FX_FORWARD = new FXForwardAndSwapConvention("FX Forward", ExternalIdBundle.of(ExternalId.of(SCHEME, "FX Forward")), FX_SPOT_ID, MODIFIED_FOLLOWING,
      false, US);
  private static final FederalFundsFutureConvention FED_FUND = new FederalFundsFutureConvention("FF Future", ExternalIdBundle.of(FED_FUND_FUTURE_ID), IMM_1M_FUTURE_EXPIRY_CONVENTION, US, OVERNIGHT_ID, 5000000);
  private static final SwapConvention SWAP = new SwapConvention("Swap", ExternalIdBundle.of(FIXED_IBOR_3M_SWAP_ID), FIXED_LEG_ID, LEG_3M_IBOR_ID);
  private static final DeliverablePriceQuotedSwapFutureConvention SWAP_FUTURE = new DeliverablePriceQuotedSwapFutureConvention("DSF", ExternalIdBundle.of(DELIVERABLE_SWAP_FUTURE_ID),
      IMM_3M_FUTURE_EXPIRY_CONVENTION, US, FIXED_IBOR_3M_SWAP_ID, 1);
  
  private static final String IMM_FRA_CONVENTION_NAME = "IMMFRA-Quarterly-3M";
  private static final ExternalId QUARTERLY_IMM_DATES = ExternalId.of(SCHEME_NAME, RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
  private static final ExternalId IMM_FRA_CONVENTION_ID = ExternalId.of(SCHEME, IMM_FRA_CONVENTION_NAME);
  private static final RollDateFRAConvention IMM_FRA_CONVENTION = new RollDateFRAConvention(IMM_FRA_CONVENTION_NAME, ExternalIdBundle.of(IMM_FRA_CONVENTION_ID), USDLIBOR_ACT_360_ID, QUARTERLY_IMM_DATES);
  private static final String FIXED_LEG_ROLL_NAME = "USD Fixed Leg 6MIMMQ";
  private static final ExternalId FIXED_LEG_ROLL_ID = ExternalId.of(SCHEME, FIXED_LEG_ROLL_NAME);
  private static final FixedLegRollDateConvention FIXED_LEG_ROLL = new FixedLegRollDateConvention(FIXED_LEG_ROLL_NAME, ExternalIdBundle.of(FIXED_LEG_ROLL_ID), Tenor.SIX_MONTHS, 
      THIRTY_360, Currency.USD, NYLON, StubType.SHORT_START, false, 0);
  private static final String LIBOR_3M_LEG_ROLL_NAME = "USD Libor Leg 3MIMMQ";
  private static final ExternalId LIBOR_3M_LEG_ROLL_ID = ExternalId.of(SCHEME, LIBOR_3M_LEG_ROLL_NAME);
  private static final VanillaIborLegRollDateConvention LIBOR_3M_LEG_ROLL = new VanillaIborLegRollDateConvention(LIBOR_3M_LEG_ROLL_NAME, ExternalIdBundle.of(LIBOR_3M_LEG_ROLL_ID), 
      USDLIBOR_ACT_360_ID, true, Tenor.THREE_MONTHS, StubType.SHORT_START, false, 0);
  private static final String LIBOR_6M_LEG_ROLL_NAME = "USD Libor Leg 6MIMMQ";
  private static final ExternalId LIBOR_6M_LEG_ROLL_ID = ExternalId.of(SCHEME, LIBOR_6M_LEG_ROLL_NAME);
  private static final VanillaIborLegRollDateConvention LIBOR_6M_LEG_ROLL = new VanillaIborLegRollDateConvention(LIBOR_6M_LEG_ROLL_NAME, ExternalIdBundle.of(LIBOR_6M_LEG_ROLL_ID), 
      USDLIBOR_ACT_360_ID, true, Tenor.SIX_MONTHS, StubType.SHORT_START, false, 0);

  private static final String ON_3M_LEG_ROLL_NAME = "USD ON Leg 3MIMMQ";
  private static final ExternalId ON_3M_LEG_ROLL_ID = ExternalId.of(SCHEME, ON_3M_LEG_ROLL_NAME);
  private static final ONCompoundedLegRollDateConvention ON_3M_LEG_ROLL = new ONCompoundedLegRollDateConvention(ON_3M_LEG_ROLL_NAME, ExternalIdBundle.of(ON_3M_LEG_ROLL_ID), 
      OVERNIGHT_ID, Tenor.THREE_MONTHS, StubType.SHORT_START, false, 0);
  
  
  private static final String SWAP_QIMM_6MLIBOR3M_CONVENTION_NAME = "USD Swap-QIMM-6M-LIBOR3M";
  private static final ExternalId SWAP_QIMM_6MLIBOR3M_CONVENTION_ID = ExternalId.of(SCHEME, SWAP_QIMM_6MLIBOR3M_CONVENTION_NAME);
  private static final RollDateSwapConvention SWAP_6MIMMLIBOR3MIMM_CONVENTION = new RollDateSwapConvention(SWAP_QIMM_6MLIBOR3M_CONVENTION_NAME, ExternalIdBundle.of(SWAP_QIMM_6MLIBOR3M_CONVENTION_ID), 
      FIXED_LEG_ROLL_ID, LIBOR_3M_LEG_ROLL_ID, QUARTERLY_IMM_DATES);
  
  private static final String SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_NAME = "USD Swap-QIMM-LIBOR6M-LIBOR3M";
  private static final ExternalId SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID = ExternalId.of(SCHEME, SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_NAME);
  private static final RollDateSwapConvention SWAP_LIBOR6MIMMLIBOR3MIMM_CONVENTION = new RollDateSwapConvention(SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_NAME, 
      ExternalIdBundle.of(SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID), LIBOR_6M_LEG_ROLL_ID, LIBOR_3M_LEG_ROLL_ID, QUARTERLY_IMM_DATES);
  
  private static final String SWAP_QIMM_LIBOR3MFF3M_CONVENTION_NAME = "USD Swap-QIMM-LIBOR3M-FF3M";
  private static final ExternalId SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID = ExternalId.of(SCHEME, SWAP_QIMM_LIBOR3MFF3M_CONVENTION_NAME);
  private static final RollDateSwapConvention SWAP_QIMM_LIBOR3MFF3M_CONVENTION = new RollDateSwapConvention(SWAP_QIMM_LIBOR3MFF3M_CONVENTION_NAME, 
      ExternalIdBundle.of(SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID), LIBOR_3M_LEG_ROLL_ID, ON_3M_LEG_ROLL_ID, QUARTERLY_IMM_DATES);
  
  // EUR conventions
  private static final String EURIBOR_NAME = "EUR Euribor";
  private static final ExternalId EURIBOR_ID = ExternalId.of(SCHEME, EURIBOR_NAME);
  private static final IborIndexConvention EURIBOR = new IborIndexConvention(EURIBOR_NAME, ExternalIdBundle.of(EURIBOR_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.EUR, LocalTime.of(11, 0), "EU", EU, EU, "Page");
  private static final String LEG_EURIBOR3M_NAME = "EUR Euribor 3M";
  private static final ExternalId LEG_EURIBOR3M_ID = ExternalId.of(SCHEME, LEG_EURIBOR3M_NAME);
  private static final VanillaIborLegConvention LEG_EURIBOR3M = new VanillaIborLegConvention(LEG_EURIBOR3M_NAME, ExternalIdBundle.of(LEG_EURIBOR3M_ID),
      EURIBOR_ID, true, SCHEME, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_START, false, 0);
  private static final String LEG_EURIBOR6M_NAME = "EUR Euribor 6M";
  private static final ExternalId LEG_EURIBOR6M_ID = ExternalId.of(SCHEME, LEG_EURIBOR6M_NAME);
  private static final VanillaIborLegConvention LEG_EURIBOR6M = new VanillaIborLegConvention(LEG_EURIBOR6M_NAME, ExternalIdBundle.of(LEG_EURIBOR6M_ID),
      EURIBOR_ID, true, SCHEME, Tenor.SIX_MONTHS, 2, true, StubType.SHORT_START, false, 0);

  private static final String EUR1Y_FIXED_NAME = "EUR 1Y Fixed ";
  private static final ExternalId EUR1Y_FIXED_ID = ExternalId.of(SCHEME, EUR1Y_FIXED_NAME);
  private static final SwapFixedLegConvention EUR1Y_FIXED = new SwapFixedLegConvention(EUR1Y_FIXED_NAME, ExternalIdBundle.of(EUR1Y_FIXED_ID),
      Tenor.ONE_YEAR, THIRTY_360, MODIFIED_FOLLOWING, Currency.EUR, EU, 2, true, StubType.SHORT_START, false, 0);
  
  private static final Map<ExternalId, Convention> CONVENTIONS = new HashMap<>();
  private static final ConventionSource CONVENTION_SOURCE;
  private static final HolidaySource HOLIDAY_SOURCE;
  private static final RegionSource REGION_SOURCE;
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 5, 1);

  static {
    CONVENTIONS.put(DEPOSIT_1D_ID, DEPOSIT_1D);
    CONVENTIONS.put(DEPOSIT_1M_ID, DEPOSIT_1M);
    CONVENTIONS.put(FIXED_LEG_ID, FIXED_LEG);
    CONVENTIONS.put(FIXED_LEG_PAY_LAG_ID, FIXED_LEG_PAY_LAG);
    CONVENTIONS.put(FIXED_LEG_ROLL_ID, FIXED_LEG_ROLL);
    CONVENTIONS.put(USDLIBOR_ACT_360_ID, LIBOR_ACT_360);
    CONVENTIONS.put(USDLIBOR_30_360_ID, LIBOR_30_360);
    CONVENTIONS.put(RATE_FUTURE_3M3M_ID, RATE_FUTURE_3M3M);
    CONVENTIONS.put(RATE_FUTURE_3M1M_ID, RATE_FUTURE_3M1M);
    CONVENTIONS.put(RATE_FUTURE_1M1M_ID, RATE_FUTURE_1M1M);
    CONVENTIONS.put(LEG_3M_IBOR_ID, LEG_3M_LIBOR);
    CONVENTIONS.put(SWAP_6M_IBOR_ID, LEG_6M_LIBOR);
    CONVENTIONS.put(LIBOR_3M_LEG_ROLL_ID, LIBOR_3M_LEG_ROLL);
    CONVENTIONS.put(LIBOR_6M_LEG_ROLL_ID, LIBOR_6M_LEG_ROLL);
    CONVENTIONS.put(ON_3M_LEG_ROLL_ID, ON_3M_LEG_ROLL);
    CONVENTIONS.put(SWAP_QIMM_6MLIBOR3M_CONVENTION_ID, SWAP_6MIMMLIBOR3MIMM_CONVENTION);
    CONVENTIONS.put(SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID, SWAP_LIBOR6MIMMLIBOR3MIMM_CONVENTION);
    CONVENTIONS.put(SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID, SWAP_QIMM_LIBOR3MFF3M_CONVENTION);
    CONVENTIONS.put(LIBOR_1M_CMP_3M_ID, LIBOR_1M_CMP_3M);
    CONVENTIONS.put(LIBOR_1M_CMP_FLAT_3M_ID, LIBOR_1M_CMP_FLAT_3M);
    CONVENTIONS.put(OVERNIGHT_ID, OVERNIGHT);
    CONVENTIONS.put(LEG_ON_CMP_ID, LEG_ON_CMP);
    CONVENTIONS.put(ON_AA_ID, ON_AA);
    CONVENTIONS.put(FX_SPOT_ID, FX_SPOT);
    CONVENTIONS.put(FX_FORWARD_ID, FX_FORWARD);
    CONVENTIONS.put(FED_FUND_FUTURE_ID, FED_FUND);
    CONVENTIONS.put(FIXED_IBOR_3M_SWAP_ID, SWAP);
    CONVENTIONS.put(DELIVERABLE_SWAP_FUTURE_ID, SWAP_FUTURE);
    CONVENTIONS.put(IMM_FRA_CONVENTION_ID, IMM_FRA_CONVENTION);
    // EUR
    CONVENTIONS.put(EURIBOR_ID, EURIBOR);
    CONVENTIONS.put(LEG_EURIBOR3M_ID, LEG_EURIBOR3M);
    CONVENTIONS.put(LEG_EURIBOR6M_ID, LEG_EURIBOR6M);
    CONVENTIONS.put(EUR1Y_FIXED_ID, EUR1Y_FIXED);
    
    CONVENTION_SOURCE = new TestConventionSource(CONVENTIONS);
    HOLIDAY_SOURCE = new MyHolidaySource(CALENDAR, "US");
    REGION_SOURCE = new MyRegionSource("US");
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoConventionForCash() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final CashNode cashNode = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    cashNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongConventionTypeForCash() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.0012345;
    marketValues.setDataPoint(marketDataId, rate);
    final CashNode cashNode = new CashNode(Tenor.ONE_DAY, Tenor.FIVE_MONTHS, FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    futureNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapPayFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, ExternalId.of(SCHEME, "Test"), LEG_3M_IBOR_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testWrongSwapPayFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, FIXED_LEG_ID, USDLIBOR_ACT_360_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapReceiveFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, LEG_3M_IBOR_ID, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testWrongSwapReceiveFixedLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, USDLIBOR_ACT_360_ID, FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapPayOISLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, ExternalId.of(SCHEME, "Test"), FIXED_LEG_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNoSwapReceiveOISLegConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, FIXED_LEG_ID, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongSwapFloatLegIborConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.01;
    marketValues.setDataPoint(marketDataId, rate);
    final VanillaIborLegConvention iborConvention = new VanillaIborLegConvention("Test", ExternalIdBundle.of(ExternalId.of(SCHEME, "Test")),
        SWAP_6M_IBOR_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 3);
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FIXED_LEG_ID, FIXED_LEG);
    conventions.put(LEG_3M_IBOR_ID, LEG_3M_LIBOR);
    conventions.put(SWAP_6M_IBOR_ID, LEG_6M_LIBOR);
    conventions.put(ExternalId.of(SCHEME, "Test"), iborConvention);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.TEN_YEARS, FIXED_LEG_ID, ExternalId.of(SCHEME, "Test"), "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(new TestConventionSource(conventions), HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    swapNode.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFXForwardConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.USD, Currency.CAD, "Mapper");
    final Map<ExternalId, Convention> conventions = Collections.<ExternalId, Convention>emptyMap();
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(new TestConventionSource(conventions), HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFXSpotConvention() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.USD, Currency.CAD, "Mapper");
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FX_FORWARD_ID, FX_FORWARD);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(new TestConventionSource(conventions), HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongConventionForFXForward() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, LEG_3M_IBOR_ID, Currency.USD, Currency.CAD, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingConventionForFXForward() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "Data");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double forward = 1.5;
    marketValues.setDataPoint(marketDataId, forward);
    final FXForwardAndSwapConvention fxForward = new FXForwardAndSwapConvention("FX Forward", ExternalIdBundle.of(ExternalId.of(SCHEME, "FX Forward")), USDLIBOR_ACT_360_ID, MODIFIED_FOLLOWING, false, US);
    final FXForwardNode node = new FXForwardNode(Tenor.of(Period.ZERO), Tenor.ONE_YEAR, ExternalId.of(SCHEME, "FX Forward"), Currency.USD, Currency.CAD, "Mapper");
    final Map<ExternalId, Convention> conventions = new HashMap<>();
    conventions.put(FX_FORWARD_ID, fxForward);
    conventions.put(USDLIBOR_ACT_360_ID, LIBOR_ACT_360);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FXForwardNodeConverter(new TestConventionSource(conventions), HOLIDAY_SOURCE, REGION_SOURCE,
        marketValues, marketDataId, NOW);
    node.accept(converter);
  }

  @Test
  public void testOneDayDeposit() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US1d");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNode cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_DAY, DEPOSIT_1D_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final InstrumentDefinition<?> definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    final CashDefinition cash = (CashDefinition) definition;
    final CashDefinition expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 5, 2), 1, rate, 1. / 360);
    assertEquals(expectedCash, cash);
  }

  @Test
  public void testOneMonthDeposit() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US1d");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    ZonedDateTime now = DateUtils.getUTCDate(2013, 2, 4);
    CurveNode cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, "Mapper");
    CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    CashDefinition cash = (CashDefinition) definition;
    CashDefinition expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 3, 6), 1, rate, 28. / 360);
    assertEquals(expectedCash, cash);
    now = DateUtils.getUTCDate(2013, 5, 2);
    converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    cashNode = new CashNode(Tenor.of(Period.ZERO), Tenor.ONE_MONTH, DEPOSIT_1M_ID, "Mapper");
    definition = cashNode.accept(converter);
    assertTrue(definition instanceof CashDefinition);
    cash = (CashDefinition) definition;
    expectedCash = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 5, 6), DateUtils.getUTCDate(2013, 6, 6), 1, rate, 31. / 360);
    assertEquals(expectedCash, cash);
    now = DateUtils.getUTCDate(2013, 5, 7);
    converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
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
    CurveNode iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, USDLIBOR_30_360_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new CashNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    DepositIborDefinition ibor = (DepositIborDefinition) definition;
    final IborIndex ibor3m = new IborIndex(Currency.USD, Tenor.THREE_MONTHS.getPeriod(), 2, THIRTY_360, MODIFIED_FOLLOWING, false, USDLIBOR_30_360_ID.getValue());
    DepositIborDefinition expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 5, 6), 1, rate, 90. / 360, ibor3m);
    assertEquals(expectedLibor, ibor);
    iborNode = new CashNode(Tenor.of(Period.ZERO), Tenor.SIX_MONTHS, USDLIBOR_ACT_360_ID, "Mapper");
    definition = iborNode.accept(converter);
    assertTrue(definition instanceof DepositIborDefinition);
    ibor = (DepositIborDefinition) definition;
    final IborIndex ibor6m = new IborIndex(Currency.USD, Tenor.SIX_MONTHS.getPeriod(), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    expectedLibor = new DepositIborDefinition(Currency.USD, DateUtils.getUTCDate(2013, 2, 6), DateUtils.getUTCDate(2013, 8, 6), 1, rate, 181. / 360, ibor6m);
    assertEquals(expectedLibor, ibor);
  }

  @Test
  public void testFRA() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US3mLibor");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final FRANode fraNode = new FRANode(Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, USDLIBOR_ACT_360_ID, "Mapper");
    final IborIndex index = new IborIndex(Currency.USD, Tenor.THREE_MONTHS.getPeriod(), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new FRANodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final InstrumentDefinition<?> definition = fraNode.accept(converter);
    assertTrue(definition instanceof ForwardRateAgreementDefinition);
    final ForwardRateAgreementDefinition fra = (ForwardRateAgreementDefinition) definition;
    final ForwardRateAgreementDefinition expectedFRA = ForwardRateAgreementDefinition.from(DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 12, 5), 1, index, rate, CALENDAR);
    assertEquals(expectedFRA, fra);
  }
  
  @Test
  /**
   * Test the IMM FRA converter roll dates. Creates FRA with increasing IMM dates numbers and test them against hard-coded dates.
   */
  public void testIMMFRA() {
    final IborIndex index = new IborIndex(Currency.USD, Tenor.THREE_MONTHS.getPeriod(), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMMFRA1");
    final double rate = 0.0012345;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 9, 2);    
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateFRANodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
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
    int nbTest = startNumbers.length;
    for(int loopt=0; loopt<nbTest; loopt++) {
      final RollDateFRANode immFraNode = new RollDateFRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, startNumbers[loopt], endNumbers[loopt], IMM_FRA_CONVENTION_ID, "Mapper", "IMM FRA 3M 1/2");
      final InstrumentDefinition<?> definition = immFraNode.accept(converter);
      assertTrue("IMMFRANodeConverter: testIMMFRA - FRA instanceof", definition instanceof ForwardRateAgreementDefinition);
      final ForwardRateAgreementDefinition fra = (ForwardRateAgreementDefinition) definition;
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(expectedStartDates[loopt], -index.getSpotLag(), CALENDAR);
      final double acrualFactor = index.getDayCount().getDayCountFraction(expectedStartDates[loopt],  expectedEndDates[loopt]);
      final ForwardRateAgreementDefinition expectedFRA = new ForwardRateAgreementDefinition(index.getCurrency(), expectedStartDates[loopt], expectedStartDates[loopt], 
          expectedEndDates[loopt], acrualFactor, 1, fixingDate, expectedStartDates[loopt], expectedEndDates[loopt], index, rate, CALENDAR);
      assertEquals("IMMFRANodeConverter: testIMMFRA - FRA dates " +loopt, expectedFRA, fra);
    }  
  }

  @Test
  public void test3M3MIRFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "US3mLibor");
    final double rate = 0.98;
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    marketValues.setDataPoint(marketDataId, rate);
    RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M3M_ID, "Mapper");
    final IborIndex index = new IborIndex(Currency.USD, Tenor.THREE_MONTHS.getPeriod(), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_NAME);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = futureNode.accept(converter);
    InterestRateFutureTransactionDefinition future = (InterestRateFutureTransactionDefinition) definition;
    InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 6, 17), index, 1, 0.25, "", CALENDAR);
    InterestRateFutureTransactionDefinition expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 9, 16), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(4, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2014, 3, 17), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(5, Tenor.ONE_YEAR, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M3M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2015, 6, 15), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
  }

  @Test
  public void test1M3MIRFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.98;
    marketValues.setDataPoint(marketDataId, rate);
    RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_1M1M_ID, "Mapper");
    final IborIndex index = new IborIndex(Currency.USD, Tenor.THREE_MONTHS.getPeriod(), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_NAME);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = futureNode.accept(converter);
    InterestRateFutureTransactionDefinition future = (InterestRateFutureTransactionDefinition) definition;
    InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 5, 13), index, 1, 0.25, "", CALENDAR);
    InterestRateFutureTransactionDefinition expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_1M1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 7, 15), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(4, Tenor.of(Period.ZERO), Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_1M1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 8, 19), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(5, Tenor.ONE_YEAR, Tenor.ONE_MONTH, Tenor.THREE_MONTHS, RATE_FUTURE_1M1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2014, 9, 15), index, 1, 0.25, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
  }

  @Test
  public void test3M1MIRFuture() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.98;
    marketValues.setDataPoint(marketDataId, rate);
    final double accrual = 1. / 12;
    RateFutureNode futureNode = new RateFutureNode(1, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_3M1M_ID, "Mapper");
    final IborIndex index = new IborIndex(Currency.USD, Tenor.ONE_MONTH.getPeriod(), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_NAME);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = futureNode.accept(converter);
    InterestRateFutureTransactionDefinition future = (InterestRateFutureTransactionDefinition) definition;
    InterestRateFutureSecurityDefinition securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 6, 17), index, 1, accrual, "", CALENDAR);
    InterestRateFutureTransactionDefinition expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_3M1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2013, 9, 16), index, 1, accrual, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(4, Tenor.of(Period.ZERO), Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_3M1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2014, 3, 17), index, 1, accrual, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
    assertEquals(expectedFuture, future);
    futureNode = new RateFutureNode(5, Tenor.ONE_YEAR, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, RATE_FUTURE_3M1M_ID, "Mapper");
    definition = futureNode.accept(converter);
    future = (InterestRateFutureTransactionDefinition) definition;
    securityDefinition = new InterestRateFutureSecurityDefinition(DateUtils.getUTCDate(2015, 6, 15), index, 1, accrual, "", CALENDAR);
    expectedFuture = new InterestRateFutureTransactionDefinition(securityDefinition, now, rate, 1).withNewNotionalAndTransactionPrice(1, rate);
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_6MLIBOR3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0408");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("FixedIborIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final AnnuityDefinition<CouponFixedDefinition> fixedLeg = AnnuityDefinitionBuilder.couponFixedRollDate(Currency.USD, adjustedStartDate, startNumber, endNumber, adjuster, 
        Period.ofMonths(6), 1, rate, true, THIRTY_360, CALENDAR, StubType.SHORT_START, FIXED_LEG_ROLL.getPaymentLag());
    assertEquals("FixedIborIMMSwap", swap.getFirstLeg(), fixedLeg);
    final IborIndex index = NodeConverterUtils.indexIbor(LIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_6MIMMLIBOR3MIMM_CONVENTION.getReceiveLegConvention())).getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition> iborLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index, 1, false, ACT_360, CALENDAR, StubType.SHORT_START);
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0408");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("IborIborIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final IborIndex index6M = NodeConverterUtils.indexIbor(LIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_LIBOR6MIMMLIBOR3MIMM_CONVENTION.getPayLegConvention())).getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition> ibor6MLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index6M, 1, 
        true, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getFirstLeg(), ibor6MLeg);
    final IborIndex index3M = NodeConverterUtils.indexIbor(LIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_6MIMMLIBOR3MIMM_CONVENTION.getReceiveLegConvention())).getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborSpreadDefinition> ibor3MLeg = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index3M, spread, 1, 
        false, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getSecondLeg(), ibor3MLeg);
  }

  @Test
  /**
   * Test IMM basis swap 6M/3M+s with only one IMM quarterly period. 
   */
  public void testIborIborRollDateSwap0405() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "IMM Basis Swap 0405");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.0015;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    final int startNumber = 4;
    final int endNumber = 5;
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_LIBOR6MLIBOR3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0405");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("IborIborIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final IborIndex index6M = NodeConverterUtils.indexIbor(LIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_LIBOR6MIMMLIBOR3MIMM_CONVENTION.getPayLegConvention())).getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition> ibor6MLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index6M, 1, 
        true, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getFirstLeg().getNumberOfPayments(), 1);
    assertEquals("IborIborIMMSwap", swap.getFirstLeg(), ibor6MLeg);
    final IborIndex index3M = NodeConverterUtils.indexIbor(LIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_6MIMMLIBOR3MIMM_CONVENTION.getReceiveLegConvention())).getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborSpreadDefinition> ibor3MLeg = AnnuityDefinitionBuilder.couponIborSpreadRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index3M, spread, 1, 
        false, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborIMMSwap", swap.getSecondLeg(), ibor3MLeg);
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RollDateSwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final Period startPeriod = Period.ofDays(1);
    final RollDateSwapNode swapNode = new RollDateSwapNode(Tenor.of(startPeriod), startNumber, endNumber, SWAP_QIMM_LIBOR3MFF3M_CONVENTION_ID, true, SCHEME, "SwapIMMQ0408");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue("IborONIMMSwap", definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime adjustedStartDate = FOLLOWING.adjustDate(CALENDAR, now.plus(startPeriod));
    RollDateAdjuster adjuster = RollDateAdjusterFactory.getAdjuster(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);
    final IborIndex index3M = NodeConverterUtils.indexIbor(LIBOR_ACT_360, ((VanillaIborLegRollDateConvention) CONVENTIONS.get(SWAP_QIMM_LIBOR3MFF3M_CONVENTION.getPayLegConvention())).getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition> ibor3MLeg = AnnuityDefinitionBuilder.couponIborRollDateIndexAdjusted(adjustedStartDate, startNumber, endNumber, adjuster, index3M, 1, 
        true, ACT_360, CALENDAR, StubType.SHORT_START);
    assertEquals("IborONIMMSwap", swap.getFirstLeg(), ibor3MLeg);
    final IndexON indexON = NodeConverterUtils.indexON(OVERNIGHT);
    AnnuityDefinition<CouponONSpreadSimplifiedDefinition> onLeg = AnnuityDefinitionBuilder.couponONSimpleCompoundedSpreadSimplifiedRollDate(adjustedStartDate, startNumber, endNumber, 
        adjuster, ON_3M_LEG_ROLL.getPaymentTenor().getPeriod(), 1.0d, spread, indexON, false, CALENDAR, ON_3M_LEG_ROLL.getStubType(), ON_3M_LEG_ROLL.getPaymentLag());
    assertEquals("IborONIMMSwap", swap.getSecondLeg(), onLeg);
  }

  @Test
  public void testIndexIbor() {
    final Period tenor = Period.ofMonths(3);
    final IborIndex index = NodeConverterUtils.indexIbor(LIBOR_ACT_360, tenor);
    final IborIndex indexExpected = new IborIndex(LIBOR_ACT_360.getCurrency(), tenor, LIBOR_ACT_360.getSettlementDays(), 
       LIBOR_ACT_360.getDayCount(), LIBOR_ACT_360.getBusinessDayConvention(), LIBOR_ACT_360.isIsEOM(), USDLIBOR_ACT_360_NAME);
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final IborIndex index = new IborIndex(Currency.USD, Period.ofMonths(3), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), FIXED_LEG_ID, LEG_3M_IBOR_ID, "Mapper");
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    ZonedDateTime maturityDate = settlementDate.plus(swapTenor);
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    AnnuityCouponFixedDefinition fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG.getPaymentTenor().getPeriod(), 
        CALENDAR, FIXED_LEG.getDayCount(), FIXED_LEG.getBusinessDayConvention(), FIXED_LEG.isIsEOM(), 1.0d, rate, true, FIXED_LEG.getStubType(), FIXED_LEG.getPaymentLag());
    AnnuityCouponIborDefinition floatLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index, false, 
        index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), LEG_3M_IBOR_ID, FIXED_LEG_ID, "Mapper");
    settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    maturityDate = settlementDate.plus(swapTenor);
    definition = swapNode.accept(converter);
    assertTrue("FixedIborSwap", definition instanceof SwapDefinition);
    fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG.getPaymentTenor().getPeriod(), 
        CALENDAR, FIXED_LEG.getDayCount(), FIXED_LEG.getBusinessDayConvention(), FIXED_LEG.isIsEOM(), 1.0d, rate,false, FIXED_LEG.getStubType(), FIXED_LEG.getPaymentLag());
    floatLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index, true, 
        index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    assertEquals("FixedIborSwap", new SwapDefinition(floatLeg, fixedLeg), definition);
    swapNode = new SwapNode(Tenor.FIVE_MONTHS, Tenor.of(swapTenor), FIXED_LEG_ID, LEG_3M_IBOR_ID, "Mapper");
    settlementDate = DateUtils.getUTCDate(2013, 8, 5);
    maturityDate = settlementDate.plus(swapTenor);
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    fixedLeg = AnnuityDefinitionBuilder.couponFixed(Currency.USD, settlementDate, maturityDate, FIXED_LEG.getPaymentTenor().getPeriod(), 
        CALENDAR, FIXED_LEG.getDayCount(), FIXED_LEG.getBusinessDayConvention(), FIXED_LEG.isIsEOM(), 1.0d, rate, true, FIXED_LEG.getStubType(), FIXED_LEG.getPaymentLag());
    floatLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index, false, 
        index.getDayCount(), index.getBusinessDayConvention(), index.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), LEG_3M_IBOR_ID, SWAP_6M_IBOR_ID, "Mapper");
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final IborIndex index3m = new IborIndex(Currency.USD, Period.ofMonths(3), 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    final IborIndex index6m = new IborIndex(Currency.USD, Period.ofMonths(6), 2, THIRTY_360, MODIFIED_FOLLOWING, false, USDLIBOR_30_360_ID.getValue());
    AnnuityCouponIborDefinition payLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index3m, true, 
        index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    final AnnuityDefinition<CouponIborSpreadDefinition> receiveLeg = AnnuityDefinitionBuilder.couponIborSpread(settlementDate, maturityDate, LEG_6M_LIBOR.getResetTenor().getPeriod(), 
        1.0d, spread, index6m, false, index6m.getDayCount(), index6m.getBusinessDayConvention(), index6m.isEndOfMonth(), CALENDAR, LEG_6M_LIBOR.getStubType(), LEG_6M_LIBOR.getPaymentLag());
    assertEquals(new SwapDefinition(payLeg, receiveLeg), definition);
    settlementDate = DateUtils.getUTCDate(2014, 3, 5);
    maturityDate = settlementDate.plus(swapTenor);
    swapNode = new SwapNode(Tenor.ONE_YEAR, Tenor.of(swapTenor), LEG_3M_IBOR_ID, SWAP_6M_IBOR_ID, "Mapper");
    definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    payLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index3m, true, 
        index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    final AnnuityDefinition<CouponIborSpreadDefinition> spreadLeg = AnnuityDefinitionBuilder.couponIborSpread(settlementDate, maturityDate, LEG_6M_LIBOR.getResetTenor().getPeriod(), 
        1.0d, spread, index6m, false, index6m.getDayCount(), index6m.getBusinessDayConvention(), index6m.isEndOfMonth(), CALENDAR, LEG_6M_LIBOR.getStubType(), LEG_6M_LIBOR.getPaymentLag());
    assertEquals(new SwapDefinition(payLeg, spreadLeg), definition);
  }

  @Test
  public void testIborONAASwap() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "FF basis spread");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double spread = 0.001;
    marketValues.setDataPoint(marketDataId, spread);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    Tenor tenor = Tenor.FIVE_YEARS;
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), tenor, LEG_3M_IBOR_ID, ON_AA_ID, "Mapper");
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final SwapDefinition swap = (SwapDefinition) definition;
    final ZonedDateTime maturity = settlementDate.plus(tenor.getPeriod());
    final IborIndex index3m = NodeConverterUtils.indexIbor(LIBOR_ACT_360, LEG_3M_LIBOR.getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition> payLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturity, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index3m, true, 
        ACT_360, LIBOR_ACT_360.getBusinessDayConvention(), LIBOR_ACT_360.isIsEOM(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    for(int loopcpn=0; loopcpn<payLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborONAASwap: first leg - cpn " + loopcpn, payLeg.getNthPayment(loopcpn), (swap.getFirstLeg().getNthPayment(loopcpn)));
    }
    final IndexON indexON = NodeConverterUtils.indexON(OVERNIGHT);
    AnnuityDefinition<CouponONArithmeticAverageSpreadSimplifiedDefinition> recLeg = AnnuityDefinitionBuilder.couponONArithmeticAverageSpreadSimplified(settlementDate, maturity, 
        ON_AA.getPaymentTenor().getPeriod(), 1.0, spread, indexON, false, ON_AA.getBusinessDayConvention(), ON_AA.isIsEOM(), CALENDAR, ON_AA.getStubType());
    for(int loopcpn=0; loopcpn<recLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborONAASwap: second leg - cpn " + loopcpn, recLeg.getNthPayment(loopcpn), (swap.getSecondLeg().getNthPayment(loopcpn)));
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), legTenor, LEG_3M_IBOR_ID, LIBOR_1M_CMP_3M_ID, "Mapper");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final Period paymentPeriod = Period.ofMonths(3);
    final Period compositionPeriod = Period.ofMonths(1);
    final IborIndex index3m = new IborIndex(Currency.USD, paymentPeriod, 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue()); // Not correct conventions. Only for testing.
    final IborIndex index1m = new IborIndex(Currency.USD, compositionPeriod, 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    final AnnuityCouponIborDefinition payLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index3m, true, 
        index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    final AnnuityDefinition<CouponIborCompoundingSpreadDefinition> receiveLeg = AnnuityDefinitionBuilder.couponIborCompoundingSpread(settlementDate, settlementDate.plus(legTenor.getPeriod()), paymentPeriod, 1, spread,
        index1m, StubType.SHORT_START, false, MODIFIED_FOLLOWING, true, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborCompoundingSwap: first leg", payLeg, ((SwapDefinition)definition).getFirstLeg());
    for(int loopcpn=0; loopcpn<receiveLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborIborCompoundingSwap: first leg - cpn " + loopcpn, receiveLeg.getNthPayment(loopcpn), ((SwapDefinition)definition).getSecondLeg().getNthPayment(loopcpn));
    }
    assertEquals("IborIborCompoundingSwap: first leg", receiveLeg, ((SwapDefinition)definition).getSecondLeg());
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), legTenor, LEG_3M_IBOR_ID, LIBOR_1M_CMP_FLAT_3M_ID, "Mapper");
    final InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final Period paymentPeriod = Period.ofMonths(3);
    final Period compositionPeriod = Period.ofMonths(1);
    final IborIndex index3m = new IborIndex(Currency.USD, paymentPeriod, 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    final IborIndex index1m = new IborIndex(Currency.USD, compositionPeriod, 2, ACT_360, MODIFIED_FOLLOWING, false, USDLIBOR_ACT_360_ID.getValue());
    final AnnuityCouponIborDefinition payLeg = AnnuityDefinitionBuilder.couponIbor(settlementDate, maturityDate, LEG_3M_LIBOR.getResetTenor().getPeriod(), 1.0d, index3m, true, 
        index3m.getDayCount(), index3m.getBusinessDayConvention(), index3m.isEndOfMonth(), CALENDAR, LEG_3M_LIBOR.getStubType(), LEG_3M_LIBOR.getPaymentLag());
    final AnnuityDefinition<CouponIborCompoundingFlatSpreadDefinition> receiveLeg = AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(settlementDate, 
        settlementDate.plus(legTenor.getPeriod()), paymentPeriod, 1, spread, index1m, StubType.SHORT_START, false, MODIFIED_FOLLOWING, true, CALENDAR, StubType.SHORT_START);
    assertEquals("IborIborCompoundingSwap: first leg", payLeg, ((SwapDefinition)definition).getFirstLeg());
    for(int loopcpn=0; loopcpn<receiveLeg.getNumberOfPayments(); loopcpn++) {
      assertEquals("IborIborCompoundingSwap: first leg - cpn " + loopcpn, receiveLeg.getNthPayment(loopcpn), ((SwapDefinition)definition).getSecondLeg().getNthPayment(loopcpn));
    }
    assertEquals("IborIborCompoundingSwap: first leg", receiveLeg, ((SwapDefinition)definition).getSecondLeg());
  }

  @Test
  public void testOIS() {
    final ExternalId marketDataId = ExternalId.of(SCHEME, "3M1M future");
    final SnapshotDataBundle marketValues = new SnapshotDataBundle();
    final double rate = 0.001;
    marketValues.setDataPoint(marketDataId, rate);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 3, 1);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2013, 3, 5);
    Period swapTenor = Period.ofYears(2);
    SwapNode swapNode = new SwapNode(Tenor.of(Period.ZERO), Tenor.of(swapTenor), FIXED_LEG_PAY_LAG_ID, LEG_ON_CMP_ID, "Mapper");
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new SwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = swapNode.accept(converter);
    assertTrue(definition instanceof SwapDefinition);
    final IndexON index = new IndexON(OVERNIGHT_ID.getValue(), Currency.USD, ACT_360, 1);
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
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new ThreeLegBasisSwapNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    InstrumentDefinition<?> definition = node.accept(converter);
    final SwapMultilegDefinition swap = (SwapMultilegDefinition) definition;
    assertTrue("ThreeLegSwapNode: correct type", definition instanceof SwapMultilegDefinition);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(now, EUR1Y_FIXED.getSettlementDays(), CALENDAR);
    final ZonedDateTime effectiveDate = ScheduleCalculator.getAdjustedDate(spot, tenorStart.getPeriod(), EUR1Y_FIXED.getBusinessDayConvention(), CALENDAR, EUR1Y_FIXED.isIsEOM());
    final ZonedDateTime maturityDate = effectiveDate.plus(tenorSwap.getPeriod());
    AnnuityCouponFixedDefinition spreadLeg = AnnuityDefinitionBuilder.couponFixed(EUR1Y_FIXED.getCurrency(), effectiveDate, maturityDate, EUR1Y_FIXED.getPaymentTenor().getPeriod(), 
        CALENDAR, EUR1Y_FIXED.getDayCount(), EUR1Y_FIXED.getBusinessDayConvention(), EUR1Y_FIXED.isIsEOM(), 1.0d, spread, true, EUR1Y_FIXED.getStubType());
    assertEquals("ThreeLegSwapNode: spread leg", spreadLeg, swap.getLegs()[0]);
    final IborIndex euribor3M = NodeConverterUtils.indexIbor(EURIBOR, LEG_EURIBOR3M.getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition>  associatedLeg = AnnuityDefinitionBuilder.couponIbor(effectiveDate, maturityDate, LEG_EURIBOR3M.getResetTenor().getPeriod(), 1.0d, euribor3M, true,
        euribor3M.getDayCount(), euribor3M.getBusinessDayConvention(), euribor3M.isEndOfMonth(), CALENDAR, LEG_EURIBOR3M.getStubType());
    assertEquals("ThreeLegSwapNode: associated leg", associatedLeg, swap.getLegs()[1]);
    final IborIndex euribor6M = NodeConverterUtils.indexIbor(EURIBOR, LEG_EURIBOR6M.getResetTenor().getPeriod());
    AnnuityDefinition<CouponIborDefinition>  otherLeg = AnnuityDefinitionBuilder.couponIbor(effectiveDate, maturityDate, LEG_EURIBOR6M.getResetTenor().getPeriod(), 1.0d, euribor6M, false,
        euribor6M.getDayCount(), euribor6M.getBusinessDayConvention(), euribor6M.isEndOfMonth(), CALENDAR, LEG_EURIBOR3M.getStubType());
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
    final IndexON index = new IndexON(OVERNIGHT_ID.getValue(), Currency.USD, ACT_360, 1);
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 5, 1);
    final CurveNodeVisitor<InstrumentDefinition<?>> converter = new RateFutureNodeConverter(CONVENTION_SOURCE, HOLIDAY_SOURCE, REGION_SOURCE, marketValues, marketDataId, now);
    final InstrumentDefinition<?> definition = futureNode.accept(converter);
    final FederalFundsFutureTransactionDefinition future = (FederalFundsFutureTransactionDefinition) definition;
    final FederalFundsFutureSecurityDefinition securityDefinition = FederalFundsFutureSecurityDefinition.from(DateUtils.getUTCDate(2013, 5, 1), index, 1, 1. / 12, "", CALENDAR);
    final FederalFundsFutureTransactionDefinition expectedFuture = new FederalFundsFutureTransactionDefinition(securityDefinition, 1, now, rate);
    assertEquals(expectedFuture, future);
  }

  @Test(enabled = false)
  /**
   * Test to be corrected when the node contains the swap rate.
   */
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
    final SwapFuturesPriceDeliverableTransactionDefinition transaction = new SwapFuturesPriceDeliverableTransactionDefinition(securityDefinition, NOW, price, 1);
    assertEquals(transaction, definition);
  }

  private static class MyHolidaySource implements HolidaySource {
    private final Calendar _calendar;
    private final ExternalId _regionId;
    private final UniqueId _uniqueId;
    private final SimpleHoliday _holiday;

    public MyHolidaySource(final Calendar calendar, final String country) {
      _calendar = calendar;
      _regionId = ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, country);
      _holiday = new SimpleHoliday();
      _uniqueId = UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), _regionId.getValue());
      _holiday.setUniqueId(_uniqueId);
    }

    @Override
    public Holiday get(final UniqueId uniqueId) {
      if (uniqueId.equals(_uniqueId)) {
        return _holiday;
      }
      return null;
    }

    @Override
    public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      if (_regionId.equals(objectId)) {
        return _holiday;
      }
      return null;
    }

    @Override
    public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
      for (final UniqueId uniqueId : uniqueIds) {
        if (_uniqueId.equals(uniqueId)) {
          return Collections.<UniqueId, Holiday>singletonMap(uniqueId, _holiday);
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      for (final ObjectId objectId : objectIds) {
        if (_regionId.equals(objectId)) {
          return Collections.<ObjectId, Holiday>singletonMap(objectId, _holiday);
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
      return !_calendar.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
      return !_calendar.isWorkingDay(dateToCheck);
    }

    @Override
    public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
      return !_calendar.isWorkingDay(dateToCheck);
    }

  }

  private static class MyRegionSource implements RegionSource {
    private final ExternalIdBundle _regionBundle;
    private final Region _region;

    public MyRegionSource(final String countryId) {
      final SimpleRegion region = new SimpleRegion();
      final ExternalId id = ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, countryId);
      region.addExternalId(id);
      region.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), id.getValue()));
      _regionBundle = ExternalIdBundle.of(id);
      _region = region;
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      return Collections.singleton(_region);
    }

    @Override
    public Map<ExternalIdBundle, Collection<Region>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      for (final ExternalIdBundle bundle : bundles) {
        if (_regionBundle.equals(bundle)) {
          return Collections.<ExternalIdBundle, Collection<Region>>singletonMap(_regionBundle, Collections.singleton(_region));
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public Collection<Region> get(final ExternalIdBundle bundle) {
      if (_regionBundle.equals(bundle)) {
        return Collections.singleton(_region);
      }
      return Collections.emptySet();
    }

    @Override
    public Region getSingle(final ExternalIdBundle bundle) {
      if (_regionBundle.equals(bundle)) {
        return _region;
      }
      return null;
    }

    @Override
    public Region getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
      if (_regionBundle.equals(bundle)) {
        return _region;
      }
      return null;
    }

    @Override
    public Map<ExternalIdBundle, Region> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
      for (final ExternalIdBundle bundle : bundles) {
        if (_regionBundle.equals(bundle)) {
          return Collections.<ExternalIdBundle, Region>singletonMap(_regionBundle, _region);
        }
      }
      return Collections.emptyMap();
    }

    @Override
    public Region get(final UniqueId uniqueId) {
      if (_region.getUniqueId().equals(uniqueId)) {
        return _region;
      }
      return null;
    }

    @Override
    public Region get(final ObjectId objectId, final VersionCorrection versionCorrection) {
      if (_region.getUniqueId().equals(objectId.atLatestVersion())) {
        return _region;
      }
      return null;
    }

    @Override
    public Map<UniqueId, Region> get(final Collection<UniqueId> uniqueIds) {
      for (final UniqueId uniqueId : uniqueIds) {
        if (_region.getUniqueId().equals(uniqueId)) {
          return Collections.singletonMap(uniqueId, _region);
        }
      }
      return null;
    }

    @Override
    public Map<ObjectId, Region> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
      for (final ObjectId objectId : objectIds) {
        if (_region.getUniqueId().equals(objectId.atLatestVersion())) {
          return Collections.singletonMap(objectId, _region);
        }
      }
      return null;
    }

    @Override
    public ChangeManager changeManager() {
      return DummyChangeManager.INSTANCE;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalId externalId) {
      return _region;
    }

    @Override
    public Region getHighestLevelRegion(final ExternalIdBundle bundle) {
      return _region;
    }

  }
}

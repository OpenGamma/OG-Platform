/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.CME_DELIVERABLE_SWAP_FUTURE;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FED_FUNDS_FUTURE;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FRA;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.GOVT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IBOR_CMP_FLAT_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IMM;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.INFLATION_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.MONTHLY;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.MONTHLY_IMM_DATES;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.ON_AA_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.ON_CMP_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.PRICE_INDEX;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.QUARTERLY;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.QUARTERLY_IMM_DATES;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SERIAL;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.STIR_FUTURES;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SWAP;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SWAP_INDEX;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_12M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_1M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_1Y;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_6M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_SHORT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.FedFundFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * The conventions for USA.
 */
public class USConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new USConventions();
  /** OIS X-Ccy USD/JPY ON leg convention string **/
  public static final String OIS_USD_JPY_ON_LEG = "USD Overnight USD/JPY XCcy Leg";

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final DayCount THIRTY_360 = DayCounts.THIRTY_U_360;
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_AFB;
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");

  /**
   * Restricted constructor.
   */
  protected USConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    final String depositConventionName = getConventionName(Currency.USD, DEPOSIT);
    final String depositONConventionName = getConventionName(Currency.USD, DEPOSIT_ON);
    final String overnightConventionName = getConventionName(Currency.USD, OVERNIGHT);
    final String liborConventionName = getConventionName(Currency.USD, LIBOR);
    final String fedFundFutureConventionName = FED_FUNDS_FUTURE;
    final String cmeDeliverableSwapFutureConventionName = CME_DELIVERABLE_SWAP_FUTURE;
    final String inflationConventionName = getConventionName(Currency.USD, INFLATION_LEG);
    final String priceIndexName = getConventionName(Currency.USD, PRICE_INDEX);
    final String swapIndexConventionName = getConventionName(Currency.USD, SWAP_INDEX);
    
    // Libor
    final ExternalId liborConventionId = ExternalId.of(SCHEME_NAME, liborConventionName);
    final IborIndexConvention liborConvention = new IborIndexConvention(
        liborConventionName, getIds(Currency.USD, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.USD,
        LocalTime.of(11, 00), "US", NYLON, US, "");
    
    // ON - Fed Funds
    final ExternalId overnightConventionId = ExternalId.of(SCHEME_NAME, overnightConventionName);
    final OvernightIndexConvention overnightConvention = new OvernightIndexConvention(
        overnightConventionName, getIds(Currency.USD, OVERNIGHT), ACT_360, 1, Currency.USD, US);
    
    // Price index - inflation
    final ExternalId priceIndexId = ExternalId.of(SCHEME_NAME, priceIndexName);
    
    // Deposit
    final DepositConvention depositConvention = new DepositConvention(
        depositConventionName, getIds(Currency.USD, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.USD, US);
    final DepositConvention depositONConvention = new DepositConvention(
        depositONConventionName, getIds(Currency.USD, DEPOSIT_ON), ACT_360, FOLLOWING, 0, false, Currency.USD, US);

    // IMM FRA
    final String fraIMMQuarterlyConventionName = getConventionName(Currency.USD, FRA + " " + IMM + " " + QUARTERLY);
    final RollDateFRAConvention immFRAQuarterlyConvention = new RollDateFRAConvention(
        fraIMMQuarterlyConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, fraIMMQuarterlyConventionName)), 
        liborConventionId, QUARTERLY_IMM_DATES);
    final String fraIMMMonthlyConventionName = getConventionName(Currency.USD, FRA + " " + IMM + " " + MONTHLY);
    final RollDateFRAConvention immFRAMonthlyConvention = new RollDateFRAConvention(
        fraIMMMonthlyConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, fraIMMMonthlyConventionName)), 
        liborConventionId, MONTHLY_IMM_DATES);
    
    // Fixed Leg
    final String fixedLeg1YPayLagConventionName = getConventionName(Currency.USD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention fixedLeg1YPayLagConvention = new SwapFixedLegConvention(
        fixedLeg1YPayLagConventionName, getIds(Currency.USD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, MODIFIED_FOLLOWING, Currency.USD, US, 2, true, StubType.SHORT_START, false, 2);
    final String fixedLegShortPayLagConventionName = getConventionName(Currency.USD, TENOR_STR_SHORT, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention fixedLegShortPayLagConvention = new SwapFixedLegConvention(
        fixedLegShortPayLagConventionName, getIds(Currency.USD, TENOR_STR_SHORT, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, FOLLOWING, Currency.USD, US, 2, false, StubType.SHORT_START, false, 2);
    final String legFixed6MIMMQConventionName = getConventionName(Currency.USD, TENOR_STR_6M, FIXED_LEG + " " + IMM + " " + QUARTERLY);
    final ExternalId legFixed6MIMMQConventionId = ExternalId.of(SCHEME_NAME, legFixed6MIMMQConventionName);
    final FixedLegRollDateConvention legFixed6MIMMQConvention = new FixedLegRollDateConvention(legFixed6MIMMQConventionName, 
        ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, legFixed6MIMMQConventionName)), Tenor.SIX_MONTHS, THIRTY_360, Currency.USD, NYLON, StubType.SHORT_START, false, 0);
    
    // Fixed 1Y- ON compounded 1Y
    final String oisFixedLegConventionName = getConventionName(Currency.USD, OIS_FIXED_LEG);
    final SwapFixedLegConvention oisFixedLegConvention = new SwapFixedLegConvention(
        oisFixedLegConventionName, getIds(Currency.USD, OIS_FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, MODIFIED_FOLLOWING, Currency.USD, US, 2, true, StubType.SHORT_START, false, 2);
    final String oisONLegConventionName = getConventionName(Currency.USD, OIS_ON_LEG);
    final OISLegConvention oisONLegConvention = new OISLegConvention(
        oisONLegConventionName, getIds(Currency.USD, OIS_ON_LEG), overnightConventionId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    
    // ON Simple Compounded 3M
    final String onCmp3MLegConventionName = getConventionName(Currency.USD, TENOR_STR_3M, ON_CMP_LEG);
    final OISLegConvention onCmp3MLegConvention = new OISLegConvention(
        onCmp3MLegConventionName, getIds(Currency.USD, TENOR_STR_3M, ON_CMP_LEG), overnightConventionId,
        Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 2);

    // ON Arithmetic Average 3M
    final String onAA3MLegConventionName = getConventionName(Currency.USD, TENOR_STR_3M, ON_AA_LEG);
    final ONArithmeticAverageLegConvention onAA3MLegConvention = new ONArithmeticAverageLegConvention(
        onAA3MLegConventionName, getIds(Currency.USD, TENOR_STR_3M, ON_AA_LEG), 
        overnightConventionId, Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 0);
    
    // Ibor legs - no payment delay
    final String irsFixedLegConventionName = getConventionName(Currency.USD, IRS_FIXED_LEG);
    final SwapFixedLegConvention irsFixedLegConvention = new SwapFixedLegConvention(
        irsFixedLegConventionName, getIds(Currency.USD, IRS_FIXED_LEG),
        Tenor.SIX_MONTHS, THIRTY_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, true, StubType.SHORT_START, false, 0);
    
    final String liborLeg1MConventionName = getConventionName(Currency.USD, TENOR_STR_1M, IRS_IBOR_LEG);
    final VanillaIborLegConvention liborLeg1MConvention = new VanillaIborLegConvention(
        liborLeg1MConventionName, getIds(Currency.USD, TENOR_STR_1M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.ONE_MONTH, 2, true, StubType.SHORT_START, false, 0);
    final String liborLeg3MConventionName = getConventionName(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG);
    final VanillaIborLegConvention liborLeg3MConvention = new VanillaIborLegConvention(
        liborLeg3MConventionName, getIds(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_START, false, 0);
    final String liborLeg6MConventionName = getConventionName(Currency.USD, TENOR_STR_6M, IRS_IBOR_LEG);
    final VanillaIborLegConvention liborLeg6MConvention = new VanillaIborLegConvention(
        liborLeg6MConventionName, getIds(Currency.USD, TENOR_STR_6M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 2, true, StubType.SHORT_START, false, 0);
    final String liborLeg12MConventionName = getConventionName(Currency.USD, TENOR_STR_12M, IRS_IBOR_LEG);
    final VanillaIborLegConvention liborLeg12MConvention = new VanillaIborLegConvention(
        liborLeg12MConventionName, getIds(Currency.USD, TENOR_STR_12M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.TWELVE_MONTHS, 2, true, StubType.SHORT_START, false, 0);

    // Ibor legs - with payment delay
    final String liborLeg3MPayLagConventionName = getConventionName(Currency.USD, TENOR_STR_3M, PAY_LAG + IBOR_LEG);
    final VanillaIborLegConvention liborLeg3MPayLagConvention = new VanillaIborLegConvention(
        liborLeg3MPayLagConventionName, getIds(Currency.USD, TENOR_STR_3M, PAY_LAG + IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.NONE, false, 2);

    // Ibor legs - compounded
    final String liborLeg1MComp3MConventionName = getConventionName(Currency.USD, TENOR_STR_1M + " x " + TENOR_STR_3M, IBOR_CMP_FLAT_LEG); // "USD 1M x 3M Comp Ibor Leg"  
    final CompoundingIborLegConvention liborLeg1MComp3MConvention = new CompoundingIborLegConvention(
        liborLeg1MComp3MConventionName, getIds(Currency.USD, TENOR_STR_1M + " x " + TENOR_STR_3M, IBOR_CMP_FLAT_LEG), 
        liborConventionId, Tenor.THREE_MONTHS, CompoundingType.FLAT_COMPOUNDING, Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, false, 0);
 
    // Ibor legs - IMM
    final String legIbor3MIMMQConventionName = getConventionName(Currency.USD, TENOR_STR_3M, IBOR_LEG + " " + IMM + " " + QUARTERLY);
    final ExternalId legIbor3MIMMQConventionId = ExternalId.of(SCHEME_NAME, legIbor3MIMMQConventionName);
    final VanillaIborLegRollDateConvention legIbor3MIMMQConvention = new VanillaIborLegRollDateConvention(legIbor3MIMMQConventionName, 
        ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, legIbor3MIMMQConventionName)), liborConventionId, true, Tenor.THREE_MONTHS, StubType.SHORT_START, false, 0);
    
    // Swaps
    final SwapConvention swapConvention = new SwapConvention(
        "USD Swap", ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, "USD Swap")),
        ExternalId.of(SCHEME_NAME, getConventionName(Currency.USD, IRS_FIXED_LEG)),
        ExternalId.of(SCHEME_NAME, getConventionName(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG)));
    final SwapIndexConvention swapIndexConvention = new SwapIndexConvention(
        swapIndexConventionName, getIds(Currency.USD, SWAP_INDEX), LocalTime.of(11, 0), ExternalId.of(SCHEME_NAME, "USD Swap"));
    
    final String swapIMMQConventionName = getConventionName(Currency.USD, SWAP + " " + TENOR_STR_6M + TENOR_STR_3M + " " + IMM + " " + QUARTERLY);
    final ExternalId swapIMMQConventionId = ExternalId.of(SCHEME_NAME, swapIMMQConventionName);
    final RollDateSwapConvention swapIMMQConvention = new RollDateSwapConvention(swapIMMQConventionName, ExternalIdBundle.of(swapIMMQConventionId), legFixed6MIMMQConventionId, 
        legIbor3MIMMQConventionId, QUARTERLY_IMM_DATES);    
    
    // Futures (for ED-LIBOR3M and EM-LIBOR1M)
    final String quartFutureConventionName = getConventionName(Currency.USD, STIR_FUTURES + QUARTERLY);
    final InterestRateFutureConvention quartSTIRFutureConvention = new InterestRateFutureConvention(
        quartFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, quartFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), US, liborConventionId);
    final String serialFutureConventionName = getConventionName(Currency.USD, STIR_FUTURES + SERIAL);
    final InterestRateFutureConvention serialSTIRFutureConvention = new InterestRateFutureConvention(
        serialFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, serialFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME), US, liborConventionId);
    
    final FederalFundsFutureConvention fedFundsConvention = new FederalFundsFutureConvention(
        fedFundFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, FedFundFutureAndFutureOptionMonthlyExpiryCalculator.NAME), US, overnightConventionId, 5000000);
    final DeliverablePriceQuotedSwapFutureConvention cmsDeliverableSwapFutureConvention = new DeliverablePriceQuotedSwapFutureConvention(
        cmeDeliverableSwapFutureConventionName,
        ExternalIdBundle.of(SCHEME_NAME, CME_DELIVERABLE_SWAP_FUTURE), ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME,
            IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), US, liborConventionId, 100000);
    
    // Inflation
    final PriceIndexConvention priceIndexConvention = new PriceIndexConvention(
        priceIndexName, getIds(Currency.USD, PRICE_INDEX), Currency.USD, US,
        ExternalSchemes.bloombergTickerSecurityId("CPURNSA Index"));
    final InflationLegConvention inflationConvention = new InflationLegConvention(
        inflationConventionName, getIds(Currency.USD, INFLATION_LEG), MODIFIED_FOLLOWING, ACT_360, false, 3, 2,
        priceIndexId);
    
    // US Treasury: Synthetic swaps to represent bonds, using yield
    final String fixedLegGovtConventionName = getConventionName(Currency.USD, TENOR_STR_6M, GOVT + FIXED_LEG);
    final SwapFixedLegConvention fixedLegGovtConvention = new SwapFixedLegConvention(
        fixedLegGovtConventionName, getIds(Currency.USD, TENOR_STR_6M, GOVT + FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_ACT, FOLLOWING, Currency.USD, US, 2, false, StubType.SHORT_START, false, 0);
    final String liborLegGovtConventionName = getConventionName(Currency.USD, TENOR_STR_3M, GOVT + IBOR_LEG);
    final VanillaIborLegConvention liborLegGovtConvention = new VanillaIborLegConvention(
        liborLegGovtConventionName, getIds(Currency.USD, TENOR_STR_3M, GOVT + IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 0);

    // X-Ccy OIS
    final OISLegConvention oisXCcyJPYLegConvention = new OISLegConvention(
        OIS_USD_JPY_ON_LEG, getIds(OIS_USD_JPY_ON_LEG), overnightConventionId,
        Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    
    // Convention add
    addConvention(master, liborConvention);
    addConvention(master, overnightConvention);
    addConvention(master, depositConvention);
    addConvention(master, depositONConvention);
    addConvention(master, immFRAQuarterlyConvention);
    addConvention(master, immFRAMonthlyConvention);
    addConvention(master, fixedLeg1YPayLagConvention);
    addConvention(master, fixedLegShortPayLagConvention);
    addConvention(master, liborLeg1MConvention);
    addConvention(master, liborLeg3MConvention);
    addConvention(master, liborLeg6MConvention);
    addConvention(master, liborLeg12MConvention);
    addConvention(master, liborLeg3MPayLagConvention);
    addConvention(master, liborLeg1MComp3MConvention);
    addConvention(master, oisONLegConvention);
    addConvention(master, onAA3MLegConvention);
    addConvention(master, irsFixedLegConvention);
    addConvention(master, oisFixedLegConvention);
    addConvention(master, onCmp3MLegConvention);
    addConvention(master, swapIndexConvention);
    addConvention(master, quartSTIRFutureConvention);
    addConvention(master, serialSTIRFutureConvention);
    addConvention(master, fedFundsConvention);
    addConvention(master, cmsDeliverableSwapFutureConvention);
    addConvention(master, legFixed6MIMMQConvention);
    addConvention(master, legIbor3MIMMQConvention);
    addConvention(master, swapIMMQConvention);
    addConvention(master, priceIndexConvention);
    addConvention(master, inflationConvention);
    addConvention(master, swapConvention);
    addConvention(master, oisXCcyJPYLegConvention);
    addConvention(master, fixedLegGovtConvention);
    addConvention(master, liborLegGovtConvention);
  }

}

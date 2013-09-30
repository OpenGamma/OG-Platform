/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.CME_DELIVERABLE_SWAP_FUTURE;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.FED_FUNDS_FUTURE;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.GOVT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IBOR_CMP_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.INFLATION_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.ON_CMP_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.PRICE_INDEX;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.QUARTERLY;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SERIAL;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.STIR_FUTURES;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SWAP_INDEX;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_12M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_1M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_1Y;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_6M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_SHORT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.FedFundFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IMMFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionMaster;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Conventions for USD nodes.
 */
public class USConventions {
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount THIRTY_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual AFB");
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  /** OIS X-Ccy USD/JPY ON leg convention string **/
  public static final String OIS_USD_JPY_ON_LEG = "USD Overnight USD/JPY XCcy Leg";

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String depositConventionName = getConventionName(Currency.USD, DEPOSIT);
    final String depositONConventionName = getConventionName(Currency.USD, DEPOSIT_ON);
    final String overnightConventionName = getConventionName(Currency.USD, OVERNIGHT);
    final String liborConventionName = getConventionName(Currency.USD, LIBOR);
    final String fedFundFutureConventionName = FED_FUNDS_FUTURE;
    final String cmeDeliverableSwapFutureConventionName = CME_DELIVERABLE_SWAP_FUTURE;
    final String inflationConventionName = getConventionName(Currency.USD, INFLATION_LEG);
    final String priceIndexName = getConventionName(Currency.USD, PRICE_INDEX);
    final String swapIndexConventionName = getConventionName(Currency.USD, SWAP_INDEX);
    final ExternalId liborConventionId = ExternalId.of(SCHEME_NAME, liborConventionName);
    final ExternalId overnightConventionId = ExternalId.of(SCHEME_NAME, overnightConventionName);
    final ExternalId priceIndexId = ExternalId.of(SCHEME_NAME, priceIndexName);
    final Convention liborConvention = new IborIndexConvention(liborConventionName, getIds(Currency.USD, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.USD,
        LocalTime.of(11, 00), "US", NYLON, US, "");
    final Convention overnightConvention = new OvernightIndexConvention(overnightConventionName, getIds(Currency.USD, OVERNIGHT), ACT_360, 1, Currency.USD, US);
    
    // Deposit
    final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(Currency.USD, DEPOSIT), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.USD, US);
    final DepositConvention depositONConvention = new DepositConvention(depositONConventionName, getIds(Currency.USD, DEPOSIT_ON), ACT_360, FOLLOWING, 0, false, Currency.USD, US);
    
    // Fixed Leg
    final String fixedLeg1YPayLagConventionName = getConventionName(Currency.USD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final Convention fixedLeg1YPayLagConvention = new SwapFixedLegConvention(fixedLeg1YPayLagConventionName, getIds(Currency.USD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, MODIFIED_FOLLOWING, Currency.USD, US, 2, true, StubType.SHORT_START, false, 2);
    final String fixedLegShortPayLagConventionName = getConventionName(Currency.USD, TENOR_STR_SHORT, PAY_LAG + FIXED_LEG);
    final Convention fixedLegShortPayLagConvention = new SwapFixedLegConvention(fixedLegShortPayLagConventionName, getIds(Currency.USD, TENOR_STR_SHORT, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, FOLLOWING, Currency.USD, US, 2, false, StubType.SHORT_START, false, 2);
    // Fixed 1Y- ON compounded 1Y
    final String oisFixedLegConventionName = getConventionName(Currency.USD, OIS_FIXED_LEG);
    final String oisONLegConventionName = getConventionName(Currency.USD, OIS_ON_LEG);
    final Convention oisFixedLegConvention = new SwapFixedLegConvention(oisFixedLegConventionName, getIds(Currency.USD, OIS_FIXED_LEG),
        Tenor.ONE_YEAR, ACT_360, MODIFIED_FOLLOWING, Currency.USD, US, 2, true, StubType.SHORT_START, false, 2);
    final Convention oisONLegConvention = new OISLegConvention(oisONLegConventionName, getIds(Currency.USD, OIS_ON_LEG), overnightConventionId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 2);
    
    // ON Compounded 3M
    final String onCmp3MLegConventionName = getConventionName(Currency.USD, TENOR_STR_3M, ON_CMP_LEG);
    final Convention onCmp3MLegConvention = new OISLegConvention(onCmp3MLegConventionName, getIds(Currency.USD, TENOR_STR_3M, ON_CMP_LEG), overnightConventionId,
        Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, false, StubType.SHORT_START, false, 2);
    
    // Ibor legs - no payment delay
    final String irsFixedLegConventionName = getConventionName(Currency.USD, IRS_FIXED_LEG);
    final Convention irsFixedLegConvention = new SwapFixedLegConvention(irsFixedLegConventionName, getIds(Currency.USD, IRS_FIXED_LEG),
        Tenor.SIX_MONTHS, THIRTY_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, true, StubType.SHORT_START, false, 0);
    
    final String liborLeg1MConventionName = getConventionName(Currency.USD, TENOR_STR_1M, IRS_IBOR_LEG);
    final Convention liborLeg1MConvention = new VanillaIborLegConvention(liborLeg1MConventionName, getIds(Currency.USD, TENOR_STR_1M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.ONE_MONTH, 2, true, StubType.SHORT_START, false, 0);
    final String liborLeg3MConventionName = getConventionName(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG);
    final Convention liborLeg3MConvention = new VanillaIborLegConvention(liborLeg3MConventionName, getIds(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_START, false, 0);
    final String liborLeg6MConventionName = getConventionName(Currency.USD, TENOR_STR_6M, IRS_IBOR_LEG);
    final Convention liborLeg6MConvention = new VanillaIborLegConvention(liborLeg6MConventionName, getIds(Currency.USD, TENOR_STR_6M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 2, true, StubType.SHORT_START, false, 0);
    final String liborLeg12MConventionName = getConventionName(Currency.USD, TENOR_STR_12M, IRS_IBOR_LEG);
    final Convention liborLeg12MConvention = new VanillaIborLegConvention(liborLeg12MConventionName, getIds(Currency.USD, TENOR_STR_12M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.TWELVE_MONTHS, 2, true, StubType.SHORT_START, false, 0);

    // Ibor legs - with payment delay
    final String liborLeg3MPayLagConventionName = getConventionName(Currency.USD, TENOR_STR_3M, PAY_LAG + IBOR_LEG);
    final Convention liborLeg3MPayLagConvention = new VanillaIborLegConvention(liborLeg3MPayLagConventionName, getIds(Currency.USD, TENOR_STR_3M, PAY_LAG + IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.NONE, false, 2);

    // Ibor legs - compounded
    final String liborLeg1MComp3MConventionName = getConventionName(Currency.USD, TENOR_STR_1M + " x " + TENOR_STR_3M, IBOR_CMP_LEG); // "USD 1M x 3M Comp Ibor Leg"  
    final Convention liborLeg1MComp3MConvention = new CompoundingIborLegConvention(liborLeg1MComp3MConventionName, getIds(Currency.USD, TENOR_STR_1M + " x " + TENOR_STR_3M, IBOR_CMP_LEG), 
        liborConventionId, Tenor.THREE_MONTHS, CompoundingType.FLAT_COMPOUNDING, Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, false, 0);
    
    // Swaps
    final Convention swapConvention = new SwapConvention("USD Swap", ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, "USD Swap")),
        ExternalId.of(SCHEME_NAME, getConventionName(Currency.USD, IRS_FIXED_LEG)),
        ExternalId.of(SCHEME_NAME, getConventionName(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG)));
    final Convention swapIndexConvention = new SwapIndexConvention(swapIndexConventionName, getIds(Currency.USD, SWAP_INDEX), LocalTime.of(11, 0), ExternalId.of(SCHEME_NAME, "USD Swap"));
    
    // Futures (for ED-LIBOR3M and EM-LIBOR1M)
    final String quartFutureConventionName = getConventionName(Currency.USD, STIR_FUTURES + QUARTERLY);
    final Convention quartSTIRFutureConvention = new InterestRateFutureConvention(quartFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, quartFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), US, liborConventionId);
    final String serialFutureConventionName = getConventionName(Currency.USD, STIR_FUTURES + SERIAL);
    final Convention serialSTIRFutureConvention = new InterestRateFutureConvention(serialFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, serialFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME), US, liborConventionId);
    
    final Convention fedFundsConvention = new FederalFundsFutureConvention(fedFundFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, FED_FUNDS_FUTURE)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, FedFundFutureAndFutureOptionMonthlyExpiryCalculator.NAME), US, overnightConventionId, 5000000);
    final Convention cmsDeliverableSwapFutureConvention = new DeliverablePriceQuotedSwapFutureConvention(cmeDeliverableSwapFutureConventionName,
        ExternalIdBundle.of(SCHEME_NAME, CME_DELIVERABLE_SWAP_FUTURE), ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME,
            IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), US, liborConventionId, 100000);
    // Inflation
    final PriceIndexConvention priceIndexConvention = new PriceIndexConvention(priceIndexName, getIds(Currency.USD, PRICE_INDEX), Currency.USD, US,
        ExternalSchemes.bloombergTickerSecurityId("CPURNSA Index"));
    final Convention inflationConvention = new InflationLegConvention(inflationConventionName, getIds(Currency.USD, INFLATION_LEG), MODIFIED_FOLLOWING, ACT_360, false, 3, 2,
        priceIndexId);
    // US Treasury: Synthetic swaps to represent bonds, using yield
    final String fixedLegGovtConventionName = getConventionName(Currency.USD, TENOR_STR_6M, GOVT + FIXED_LEG);
    final Convention fixedLegGovtConvention = new SwapFixedLegConvention(fixedLegGovtConventionName, getIds(Currency.USD, TENOR_STR_6M, GOVT + FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_ACT, FOLLOWING, Currency.USD, US, 2, false, StubType.SHORT_START, false, 0);
    final String liborLegGovtConventionName = getConventionName(Currency.USD, TENOR_STR_3M, GOVT + IBOR_LEG);
    final Convention liborLegGovtConvention = new VanillaIborLegConvention(liborLegGovtConventionName, getIds(Currency.USD, TENOR_STR_3M, GOVT + IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 0);

    // X-Ccy OIS
    final Convention oisXCcyJPYLegConvention = new OISLegConvention(OIS_USD_JPY_ON_LEG, getIds(OIS_USD_JPY_ON_LEG), overnightConventionId,
        Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    
    // Convention add
    conventionMaster.add(depositConvention);
    conventionMaster.add(depositONConvention);
    conventionMaster.add(liborConvention);
    conventionMaster.add(overnightConvention);
    conventionMaster.add(fixedLeg1YPayLagConvention);
    conventionMaster.add(fixedLegShortPayLagConvention);
    conventionMaster.add(liborLeg1MConvention);
    conventionMaster.add(liborLeg3MConvention);
    conventionMaster.add(liborLeg6MConvention);
    conventionMaster.add(liborLeg12MConvention);
    conventionMaster.add(liborLeg3MPayLagConvention);
    conventionMaster.add(liborLeg1MComp3MConvention);
    conventionMaster.add(oisONLegConvention);
    conventionMaster.add(irsFixedLegConvention);
    conventionMaster.add(oisFixedLegConvention);
    conventionMaster.add(onCmp3MLegConvention);
    conventionMaster.add(swapIndexConvention);
    conventionMaster.add(quartSTIRFutureConvention);
    conventionMaster.add(serialSTIRFutureConvention);
    conventionMaster.add(fedFundsConvention);
    conventionMaster.add(cmsDeliverableSwapFutureConvention);
    conventionMaster.add(priceIndexConvention);
    conventionMaster.add(inflationConvention);
    conventionMaster.add(swapConvention);
    conventionMaster.add(oisXCcyJPYLegConvention);
    conventionMaster.add(fixedLegGovtConvention);
    conventionMaster.add(liborLegGovtConvention);
  }

}

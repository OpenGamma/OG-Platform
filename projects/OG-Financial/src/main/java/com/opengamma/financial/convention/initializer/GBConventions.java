/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.ON_CMP_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.QUARTERLY;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SERIAL;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.STIR_FUTURES;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_1M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_1Y;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_6M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionMonthlyExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * The conventions for Great Britain.
 */
public class GBConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new GBConventions();

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final ExternalId GB = ExternalSchemes.financialRegionId("GB");

  /**
   * Restricted constructor.
   */
  protected GBConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    addConventions(master);
  }

  protected void addConventions(final ConventionMaster master) {

    // Index (Overnight and Ibor-like)
    final String onIndexName = getConventionName(Currency.GBP, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final OvernightIndexConvention onIndex = new OvernightIndexConvention(
        onIndexName, getIds(Currency.GBP, OVERNIGHT), ACT_365, 0, Currency.GBP, GB);

    final String liborConventionName = getConventionName(Currency.GBP, LIBOR);
    final IborIndexConvention liborIndex = new IborIndexConvention(
        liborConventionName, getIds(Currency.GBP, LIBOR), ACT_365, MODIFIED_FOLLOWING, 0, true, Currency.GBP,
        LocalTime.of(11, 00), "GB", GB, GB, "");
    final ExternalId liborConventionId = ExternalId.of(SCHEME_NAME, liborConventionName);
    
    // Deposit
    final String depositONConventionName = getConventionName(Currency.GBP, DEPOSIT_ON);
    final DepositConvention depositONConvention = new DepositConvention(
        depositONConventionName, getIds(Currency.GBP, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, Currency.GBP, GB);
    final String depositConventionName = getConventionName(Currency.GBP, DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(Currency.GBP, DEPOSIT), ACT_365, MODIFIED_FOLLOWING, 0, true, Currency.GBP, GB);
    
    // Fixed legs
    final String oisFixedLegConventionName = getConventionName(Currency.GBP, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention oisFixedLegConvention = new SwapFixedLegConvention(
        oisFixedLegConventionName, getIds(Currency.GBP, TENOR_STR_1Y, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.GBP, GB, 0, true, StubType.SHORT_START, false, 1);
    
    final String irsFixedLeg12MConventionName = getConventionName(Currency.GBP, TENOR_STR_1Y, FIXED_LEG);
    final SwapFixedLegConvention irsFixed12MLegConvention = new SwapFixedLegConvention(
        irsFixedLeg12MConventionName, getIds(Currency.GBP, TENOR_STR_1Y, FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.GBP, GB, 0, true, StubType.SHORT_START, false, 0);
    
    final String irsFixedLeg6MConventionName = getConventionName(Currency.GBP, TENOR_STR_6M, FIXED_LEG);
    final SwapFixedLegConvention irsFixed6MLegConvention = new SwapFixedLegConvention(
        irsFixedLeg6MConventionName, getIds(Currency.GBP, TENOR_STR_6M, FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.GBP, GB, 0, true, StubType.SHORT_START, false, 0);
    
    final String irsFixedLeg3MConventionName = getConventionName(Currency.GBP, TENOR_STR_3M, FIXED_LEG);
    final SwapFixedLegConvention irsFixed3MLegConvention = new SwapFixedLegConvention(
        irsFixedLeg3MConventionName, getIds(Currency.GBP, TENOR_STR_3M, FIXED_LEG),
        Tenor.THREE_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.GBP, GB, 0, true, StubType.SHORT_START, false, 0);
    
    // ON compounded legs
    final String oisFloatLegConventionName = getConventionName(Currency.GBP, ON_CMP_LEG);
    final OISLegConvention oisFloatLegConvention = new OISLegConvention(
        oisFloatLegConventionName, getIds(Currency.GBP, ON_CMP_LEG), onIndexId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 0, true, StubType.SHORT_START, false, 1);

    // Ibor legs
    final String irsLibor6MLegConventionName = getConventionName(Currency.GBP, TENOR_STR_6M, LIBOR_LEG);
    final VanillaIborLegConvention irsLibor6MLegConvention = new VanillaIborLegConvention(
        irsLibor6MLegConventionName, getIds(Currency.GBP, TENOR_STR_6M, LIBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 0, true, StubType.SHORT_START, false, 0);
    
    final String irsLibor3MLegConventionName = getConventionName(Currency.GBP, TENOR_STR_3M, LIBOR_LEG);
    final VanillaIborLegConvention irsLibor3MLegConvention = new VanillaIborLegConvention(
        irsLibor3MLegConventionName, getIds(Currency.GBP, TENOR_STR_3M, LIBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 0, true, StubType.SHORT_START, false, 0);
    
    final String irsLibor1MLegConventionName = getConventionName(Currency.GBP, TENOR_STR_1M, LIBOR_LEG);
    final VanillaIborLegConvention irsLibor1MLegConvention = new VanillaIborLegConvention(
        irsLibor1MLegConventionName, getIds(Currency.GBP, TENOR_STR_1M, LIBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.ONE_MONTH, 0, true, StubType.SHORT_START, false, 0);
    
    // TODO: Note: Temporally used to retrieve underlying index convention. - To be removed
    final String irsLibor6MLegConventionName2 = getConventionName(Currency.GBP, TENOR_STR_6M, IRS_IBOR_LEG);
    final VanillaIborLegConvention irsLibor6MLegConvention2 = new VanillaIborLegConvention(
        irsLibor6MLegConventionName2, getIds(Currency.GBP, TENOR_STR_6M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 0, true, StubType.SHORT_START, false, 0);
    
    final String irsLibor3MLegConventionName2 = getConventionName(Currency.GBP, TENOR_STR_3M, IRS_IBOR_LEG);
    final VanillaIborLegConvention irsLibor3MLegConvention2 = new VanillaIborLegConvention(
        irsLibor3MLegConventionName2, getIds(Currency.GBP, TENOR_STR_3M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 0, true, StubType.SHORT_START, false, 0);
    
    final String irsLibor1MLegConventionName2 = getConventionName(Currency.GBP, TENOR_STR_1M, IRS_IBOR_LEG);
    final VanillaIborLegConvention irsLibor1MLegConvention2 = new VanillaIborLegConvention(
        irsLibor1MLegConventionName2, getIds(Currency.GBP, TENOR_STR_1M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.ONE_MONTH, 0, true, StubType.SHORT_START, false, 0);
    
    // Futures
    final String quarterlySTIRFutureConventionName = getConventionName(Currency.GBP, STIR_FUTURES + QUARTERLY);    
    final InterestRateFutureConvention quarterlySTIRFutureConvention = new InterestRateFutureConvention(
        quarterlySTIRFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, quarterlySTIRFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), GB, liborConventionId);
    final String serialFutureConventionName = getConventionName(Currency.GBP, STIR_FUTURES + SERIAL);
    final InterestRateFutureConvention serialSTIRFutureConvention = new InterestRateFutureConvention(
        serialFutureConventionName, ExternalIdBundle.of(ExternalId.of(SCHEME_NAME, serialFutureConventionName)),
        ExternalId.of(ExchangeTradedInstrumentExpiryCalculator.SCHEME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME), GB, liborConventionId);

    addConvention(master, liborIndex);
    addConvention(master, onIndex);
    addConvention(master, depositONConvention);
    addConvention(master, depositConvention);
    addConvention(master, oisFixedLegConvention);
    addConvention(master, irsFixed12MLegConvention);
    addConvention(master, irsFixed6MLegConvention);
    addConvention(master, irsFixed3MLegConvention);
    addConvention(master, oisFloatLegConvention);
    addConvention(master, irsLibor6MLegConvention);
    addConvention(master, irsLibor3MLegConvention);
    addConvention(master, irsLibor1MLegConvention);
    addConvention(master, irsLibor6MLegConvention2);
    addConvention(master, irsLibor3MLegConvention2);
    addConvention(master, irsLibor1MLegConvention2);
    addConvention(master, quarterlySTIRFutureConvention);
    addConvention(master, serialSTIRFutureConvention);
  }

}

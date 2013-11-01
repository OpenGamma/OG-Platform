/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;


import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.ON_CMP_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
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
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * The conventions for Australia.
 */
public class AUConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new AUConventions();
  /** The BBSW string **/
  private static final String BBSW = "BBSW";
  /** The BBSW leg string **/
  private static final String BBSW_LEG = "BBSW Leg";

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("Actual/365"); 
  private static final ExternalId AU = ExternalSchemes.financialRegionId("AU");

  /**
   * Restricted constructor.
   */
  protected AUConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    // Index Overnight
    final String onIndexName = getConventionName(Currency.AUD, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final OvernightIndexConvention onIndex = createOvernightIndexConvention(onIndexName);
    // Index BBSW
    final String bbswConventionName = getConventionName(Currency.AUD, BBSW);
    final ExternalId bbswConventionId = ExternalId.of(SCHEME_NAME, bbswConventionName);
    final IborIndexConvention bbswIndex = createIborIndexConvention(bbswConventionName);
    // Deposit
    final String depositONConventionName = getConventionName(Currency.AUD, DEPOSIT_ON);
    final DepositConvention depositONConvention = createDepositConvention(depositONConventionName);
    // Fixed Legs
    final String fixedLeg3MConventionName = getConventionName(Currency.AUD, TENOR_STR_3M, FIXED_LEG);
    final SwapFixedLegConvention fixedLeg3MConvention = createSwapFixedLeg3MConvention(fixedLeg3MConventionName);
    final String fixedLeg6MConventionName = getConventionName(Currency.AUD, TENOR_STR_6M, FIXED_LEG);
    final SwapFixedLegConvention fixedLeg6MConvention = createSwapFixedLeg6MConvention(fixedLeg6MConventionName);
    final String fixedLeg1YPayLagConventionName = getConventionName(Currency.AUD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention fixedLeg1YPayLagConvention = createSwapFixedLeg1YPayLagConvention(fixedLeg1YPayLagConventionName);
    // BBSW Legs
    final String bbsw3MLegConventionName = getConventionName(Currency.AUD, TENOR_STR_3M, BBSW_LEG);
    final VanillaIborLegConvention bbsw3MLegConvention = createVanillaIborLeg3MConvention(bbsw3MLegConventionName, bbswConventionId);
    final String bbsw6MLegConventionName = getConventionName(Currency.AUD, TENOR_STR_6M, BBSW_LEG);
    final VanillaIborLegConvention bbsw6MLegConvention = createVanillaIborLeg6MConvention(bbsw6MLegConventionName, bbswConventionId);
    // Overnight Legs
    final String onLegConventionName = getConventionName(Currency.AUD, ON_CMP_LEG);
    final OISLegConvention onLegConvention = createOISLegConvention(onLegConventionName, onIndexId);
    
    // Convention add
    addConvention(master, onIndex);
    addConvention(master, bbswIndex);
    addConvention(master, depositONConvention);
    addConvention(master, fixedLeg3MConvention);
    addConvention(master, fixedLeg6MConvention);
    addConvention(master, fixedLeg1YPayLagConvention);
    addConvention(master, bbsw3MLegConvention);
    addConvention(master, bbsw6MLegConvention);
    addConvention(master, onLegConvention);
  }

  protected OvernightIndexConvention createOvernightIndexConvention(final String onIndexName) {
    return new OvernightIndexConvention(
        onIndexName, getIds(Currency.AUD, OVERNIGHT), ACT_365, 0, Currency.AUD, AU);
  }

  protected IborIndexConvention createIborIndexConvention(final String bbswConventionName) {
    return new IborIndexConvention(
        bbswConventionName, getIds(Currency.AUD, BBSW), ACT_365, MODIFIED_FOLLOWING, 2, true, Currency.AUD,
        LocalTime.of(11, 00), "AU", AU, AU, "");
  }

  protected DepositConvention createDepositConvention(final String depositONConventionName) {
    return new DepositConvention(
        depositONConventionName, getIds(Currency.AUD, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, Currency.AUD, AU);
  }

  protected SwapFixedLegConvention createSwapFixedLeg3MConvention(final String fixedLeg3MConventionName) {
    return new SwapFixedLegConvention(
        fixedLeg3MConventionName, getIds(Currency.AUD, TENOR_STR_3M, FIXED_LEG),
        Tenor.THREE_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 2, true, StubType.SHORT_START, false, 0);
  }

  protected SwapFixedLegConvention createSwapFixedLeg6MConvention(final String fixedLeg6MConventionName) {
    return new SwapFixedLegConvention(
        fixedLeg6MConventionName, getIds(Currency.AUD, TENOR_STR_6M, FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 2, true, StubType.SHORT_START, false, 0);
  }

  protected SwapFixedLegConvention createSwapFixedLeg1YPayLagConvention(final String fixedLeg1YPayLagConventionName) {
    return new SwapFixedLegConvention(
        fixedLeg1YPayLagConventionName, getIds(Currency.AUD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 2, true, StubType.SHORT_START, false, 2);
  }

  protected VanillaIborLegConvention createVanillaIborLeg3MConvention(final String bbsw3MLegConventionName, final ExternalId bbswConventionId) {
    return new VanillaIborLegConvention(
        bbsw3MLegConventionName, getIds(Currency.AUD, TENOR_STR_3M, BBSW_LEG),
        bbswConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_START, false, 0);
  }

  protected VanillaIborLegConvention createVanillaIborLeg6MConvention(final String bbsw6MLegConventionName, final ExternalId bbswConventionId) {
    return new VanillaIborLegConvention(
        bbsw6MLegConventionName, getIds(Currency.AUD, TENOR_STR_6M, BBSW_LEG),
        bbswConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 2, true, StubType.SHORT_START, false, 0);
  }

  protected OISLegConvention createOISLegConvention(final String onLegConventionName, final ExternalId onIndexId) {
    return new OISLegConvention(
        onLegConventionName, getIds(Currency.AUD, ON_CMP_LEG), onIndexId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 2);
  }

}

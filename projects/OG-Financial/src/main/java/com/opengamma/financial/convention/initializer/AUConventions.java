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
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
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
  public static final String BBSW = "BBSW";
  /** The BBSW leg string **/
  private static final String BBSW_LEG = "BBSW Leg";

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_365 = DayCounts.ACT_365; 
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
    final SwapFixedLegConvention fixedLeg3MConvention = createSwapFixedLegConvention(fixedLeg3MConventionName, TENOR_STR_3M, Tenor.THREE_MONTHS);
    final String fixedLeg6MConventionName = getConventionName(Currency.AUD, TENOR_STR_6M, FIXED_LEG);
    final SwapFixedLegConvention fixedLeg6MConvention = createSwapFixedLegConvention(fixedLeg6MConventionName, TENOR_STR_6M, Tenor.SIX_MONTHS);
    final String fixedLeg1YPayLagConventionName = getConventionName(Currency.AUD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention fixedLeg1YPayLagConvention = createSwapFixedLegPayLagConvention(fixedLeg1YPayLagConventionName, TENOR_STR_1Y, Tenor.ONE_YEAR);
    
    // BBSW Legs
    final String bbsw3MLegConventionName = getConventionName(Currency.AUD, TENOR_STR_3M, BBSW_LEG);
    final VanillaIborLegConvention bbsw3MLegConvention = createVanillaIborLegConvention(bbsw3MLegConventionName, bbswConventionId, TENOR_STR_3M, Tenor.THREE_MONTHS);
    final String bbsw6MLegConventionName = getConventionName(Currency.AUD, TENOR_STR_6M, BBSW_LEG);
    final VanillaIborLegConvention bbsw6MLegConvention = createVanillaIborLegConvention(bbsw6MLegConventionName, bbswConventionId, TENOR_STR_6M, Tenor.SIX_MONTHS);
    
    // Overnight Legs
    final String onLegConventionName = getConventionName(Currency.AUD, TENOR_STR_1Y, ON_CMP_LEG);
    final OISLegConvention onLegConvention = createOISLegConvention(onLegConventionName, onIndexId, TENOR_STR_1Y, Tenor.ONE_YEAR);
    
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
        bbswConventionName, getIds(Currency.AUD, BBSW), ACT_365, MODIFIED_FOLLOWING, 0, true, Currency.AUD,
        LocalTime.of(11, 00), "AU", AU, AU, "");
  }

  protected DepositConvention createDepositConvention(final String depositONConventionName) {
    return new DepositConvention(
        depositONConventionName, getIds(Currency.AUD, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, Currency.AUD, AU);
  }

  protected SwapFixedLegConvention createSwapFixedLegConvention(final String fixedLegConventionName, 
      final String tenorString, final Tenor resetTenor) {
    return new SwapFixedLegConvention(
        fixedLegConventionName, getIds(Currency.AUD, tenorString, FIXED_LEG),
        resetTenor, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 1, true, StubType.SHORT_START, false, 0);
  }

  protected SwapFixedLegConvention createSwapFixedLegPayLagConvention(final String fixedLeg1YPayLagConventionName, 
      final String tenorString, final Tenor resetTenor) {
    return new SwapFixedLegConvention(
        fixedLeg1YPayLagConventionName, getIds(Currency.AUD, tenorString, PAY_LAG + FIXED_LEG),
        resetTenor, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 1, true, StubType.SHORT_START, false, 1);
  }

  protected VanillaIborLegConvention createVanillaIborLegConvention(final String bbswLegConventionName, final ExternalId bbswConventionId, 
      final String tenorString, final Tenor resetTenor) {
    return new VanillaIborLegConvention(
        bbswLegConventionName, getIds(Currency.AUD, tenorString, BBSW_LEG),
        bbswConventionId, true, Interpolator1DFactory.LINEAR, resetTenor, 1, true, StubType.SHORT_START, false, 0);
  }

  protected OISLegConvention createOISLegConvention(final String onLegConventionName, final ExternalId onIndexId, 
      final String tenorString, final Tenor resetTenor) {
    return new OISLegConvention(
        onLegConventionName, getIds(Currency.AUD, tenorString, ON_CMP_LEG), onIndexId,
        resetTenor, MODIFIED_FOLLOWING, 1, true, StubType.SHORT_START, false, 1);
  }

}

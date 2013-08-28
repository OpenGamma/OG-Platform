/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;


import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.ON_CMP_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_1Y;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.TENOR_STR_6M;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionMaster;
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
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * The conventions for EUR.
 * FIXME: This is a temporary in-code convention master. This should be moved to database before going to production.
 */
public class AUConventions {
  

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("Actual/365"); 
  private static final ExternalId AU = ExternalSchemes.financialRegionId("AU");
  
  /** The BBSW string **/
  public static final String BBSW = "BBSW";
  /** The BBSW leg string **/
  public static final String BBSW_LEG = "BBSW Leg";

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    // Index Overnight
    final String onIndexName = getConventionName(Currency.AUD, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final Convention onIndex = new OvernightIndexConvention(onIndexName, getIds(Currency.AUD, OVERNIGHT), ACT_365, 0, Currency.AUD, AU);
    // Index BBSW
    final String bbswConventionName = getConventionName(Currency.AUD, BBSW);
    final Convention bbswIndex = new IborIndexConvention(bbswConventionName, getIds(Currency.AUD, BBSW), ACT_365, MODIFIED_FOLLOWING, 2, true, Currency.AUD,
        LocalTime.of(11, 00), "AU", AU, AU, "");
    final ExternalId bbswConventionId = ExternalId.of(SCHEME_NAME, bbswConventionName);
    // Deposit
    final String depositONConventionName = getConventionName(Currency.AUD, DEPOSIT_ON);
    final DepositConvention depositONConvention = new DepositConvention(depositONConventionName, getIds(Currency.AUD, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, Currency.AUD, AU);
    // Fixed Legs
    final String fixedLeg3MConventionName = getConventionName(Currency.AUD, TENOR_STR_3M, FIXED_LEG);
    final Convention fixedLeg3MConvention = new SwapFixedLegConvention(fixedLeg3MConventionName, getIds(Currency.AUD, TENOR_STR_3M, FIXED_LEG),
        Tenor.THREE_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 2, true, StubType.SHORT_START, false, 0);
    final String fixedLeg6MConventionName = getConventionName(Currency.AUD, TENOR_STR_6M, FIXED_LEG);
    final Convention fixedLeg6MConvention = new SwapFixedLegConvention(fixedLeg6MConventionName, getIds(Currency.AUD, TENOR_STR_6M, FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 2, true, StubType.SHORT_START, false, 0);
    final String fixedLeg1YPayLagConventionName = getConventionName(Currency.AUD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final Convention fixedLeg1YPayLagConvention = new SwapFixedLegConvention(fixedLeg1YPayLagConventionName, getIds(Currency.AUD, TENOR_STR_1Y, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.AUD, AU, 2, true, StubType.SHORT_START, false, 2);
    // BBSW Legs
    final String bbsw3MLegConventionName = getConventionName(Currency.AUD, TENOR_STR_3M, BBSW_LEG);
    final Convention bbsw3MLegConvention = new VanillaIborLegConvention(bbsw3MLegConventionName, getIds(Currency.AUD, TENOR_STR_3M, BBSW_LEG),
        bbswConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, true, StubType.SHORT_START, false, 0);
    final String bbsw6MLegConventionName = getConventionName(Currency.AUD, TENOR_STR_6M, BBSW_LEG);
    final Convention bbsw6MLegConvention = new VanillaIborLegConvention(bbsw6MLegConventionName, getIds(Currency.AUD, TENOR_STR_6M, BBSW_LEG),
        bbswConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 2, true, StubType.SHORT_START, false, 0);
    // Overnight Legs
    final String onLegConventionName = getConventionName(Currency.AUD, ON_CMP_LEG);
    final Convention onLegConvention = new OISLegConvention(onLegConventionName, getIds(Currency.AUD, ON_CMP_LEG), onIndexId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 2);
    
    // Convention add
    conventionMaster.add(onIndex);
    conventionMaster.add(bbswIndex);
    conventionMaster.add(depositONConvention);
    conventionMaster.add(fixedLeg3MConvention);
    conventionMaster.add(fixedLeg6MConvention);
    conventionMaster.add(fixedLeg1YPayLagConvention);
    conventionMaster.add(bbsw3MLegConvention);
    conventionMaster.add(bbsw6MLegConvention);
    conventionMaster.add(onLegConvention);
  }

}

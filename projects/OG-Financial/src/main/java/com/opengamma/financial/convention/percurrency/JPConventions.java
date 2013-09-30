/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
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
 * The conventions for JPY.
 */
// FIXME: This is a temporary in-code convention master. This should be moved to database before going to production.
public class JPConventions {
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ExternalId JP = ExternalSchemes.financialRegionId("JP");
  private static final ExternalId JPGB = ExternalSchemes.financialRegionId("JP+GB");
  /** OIS X-Ccy USD/JPY ON leg convention string **/
  public static final String OIS_USD_JPY_ON_LEG = "JPY Overnight USD/JPY XCcy Leg";

  /** The Tibor string **/
  public static final String TIBOR = "Tibor";
  /** The Tibor - Japanese Yen (domestic) string **/
  public static final String TIBOR_JAPANESE = TIBOR + " Japanese Yen";
  /** The Tibor - Euroyen string **/
  public static final String TIBOR_EUROYEN = TIBOR + " Euroyen";

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String tenorString = "6M";
    // Index (Overnight and Ibor-like)
    final String onIndexName = getConventionName(Currency.JPY, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final Convention onIndex = new OvernightIndexConvention(onIndexName, getIds(Currency.JPY, OVERNIGHT), ACT_365, 1, Currency.JPY, JP);
    final String iborConventionName = getConventionName(Currency.JPY, LIBOR);
    final Convention liborIndex = new IborIndexConvention(iborConventionName, getIds(Currency.JPY, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.JPY,
        LocalTime.of(11, 00), "JP", JPGB, JP, "");
    final ExternalId liborConventionId = ExternalId.of(SCHEME_NAME, iborConventionName);
    final Convention tiborJPIndex = new IborIndexConvention(getConventionName(Currency.JPY, TIBOR_JAPANESE), getIds(Currency.JPY, TIBOR_JAPANESE), ACT_365, MODIFIED_FOLLOWING, 2, true, Currency.JPY,
        LocalTime.of(11, 00), "JP", JP, JP, "");
    final Convention tiborEuIndex = new IborIndexConvention(getConventionName(Currency.JPY, TIBOR_EUROYEN), getIds(Currency.JPY, TIBOR_EUROYEN), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.JPY,
        LocalTime.of(11, 00), "JP", JP, JP, "");
    // Deposit
    final String depositONConventionName = getConventionName(Currency.JPY, DEPOSIT_ON);
    final DepositConvention depositONConvention = new DepositConvention(depositONConventionName, getIds(Currency.JPY, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, Currency.JPY, JP);
    final String depositConventionName = getConventionName(Currency.JPY, DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(Currency.JPY, DEPOSIT), ACT_365, FOLLOWING, 2, false, Currency.JPY, JP);
    // OIS legs
    final String oisFixedLegConventionName = getConventionName(Currency.JPY, OIS_FIXED_LEG);
    final String oisFloatLegConventionName = getConventionName(Currency.JPY, OIS_ON_LEG);
    final Convention oisFixedLegConvention = new SwapFixedLegConvention(oisFixedLegConventionName, getIds(Currency.JPY, OIS_FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.JPY, JP, 2, true, StubType.SHORT_START, false, 2);
    final Convention oisFloatLegConvention = new OISLegConvention(oisFloatLegConventionName, getIds(Currency.JPY, OIS_ON_LEG), onIndexId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    // Ibor swap legs
    final String irsFixedLegConventionName = getConventionName(Currency.JPY, IRS_FIXED_LEG);
    final String irsIborLegConventionName = getConventionName(Currency.JPY, tenorString, IRS_IBOR_LEG);
    final Convention irsFixedLegConvention = new SwapFixedLegConvention(irsFixedLegConventionName, getIds(Currency.JPY, IRS_FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.JPY, JP, 2, true, StubType.SHORT_START, false, 2);
    final Convention irsIborLegConvention = new VanillaIborLegConvention(irsIborLegConventionName, getIds(Currency.JPY, tenorString, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 2, true, StubType.NONE, false, 2);
    // X-Ccy OIS
    final Convention oisXCcyUSDLegConvention = new OISLegConvention(OIS_USD_JPY_ON_LEG, getIds(OIS_USD_JPY_ON_LEG), onIndexId,
        Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    conventionMaster.add(oisXCcyUSDLegConvention);
    // Convention add
    conventionMaster.add(onIndex);
    conventionMaster.add(liborIndex);
    conventionMaster.add(tiborJPIndex);
    conventionMaster.add(tiborEuIndex);
    conventionMaster.add(depositONConvention);
    conventionMaster.add(depositConvention);
    conventionMaster.add(oisFixedLegConvention);
    conventionMaster.add(oisFloatLegConvention);
    conventionMaster.add(irsFixedLegConvention);
    conventionMaster.add(irsIborLegConvention);
  }

}

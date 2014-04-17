/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
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
 * The conventions for Japan.
 */
public class JPConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new JPConventions();
  /** OIS X-Ccy USD/JPY ON leg convention string **/
  public static final String OIS_USD_JPY_ON_LEG = "JPY Overnight USD/JPY XCcy Leg";
  /** The Tibor string **/
  public static final String TIBOR = "Tibor";
  /** The Tibor - Japanese Yen (domestic) string **/
  public static final String TIBOR_JAPANESE = TIBOR + " Japanese Yen";
  /** The Tibor - Euroyen string **/
  public static final String TIBOR_EUROYEN = TIBOR + " Euroyen";

  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final ExternalId JP = ExternalSchemes.financialRegionId("JP");
  private static final ExternalId JPGB = ExternalSchemes.financialRegionId("JP+GB");

  /**
   * Restricted constructor.
   */
  protected JPConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    final String tenorString = "6M";
    // Index (Overnight and Ibor-like)
    final String onIndexName = getConventionName(Currency.JPY, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final OvernightIndexConvention onIndex = new OvernightIndexConvention(
        onIndexName, getIds(Currency.JPY, OVERNIGHT), ACT_365, 1, Currency.JPY, JP);
    final String iborConventionName = getConventionName(Currency.JPY, LIBOR);
    final IborIndexConvention liborIndex = new IborIndexConvention(
        iborConventionName, getIds(Currency.JPY, LIBOR), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.JPY,
        LocalTime.of(11, 00), "JP", JPGB, JP, "");
    final ExternalId liborConventionId = ExternalId.of(SCHEME_NAME, iborConventionName);
    final IborIndexConvention tiborJPIndex = new IborIndexConvention(
        getConventionName(Currency.JPY, TIBOR_JAPANESE), getIds(Currency.JPY, TIBOR_JAPANESE), ACT_365, MODIFIED_FOLLOWING, 2, true, Currency.JPY,
        LocalTime.of(11, 00), "JP", JP, JP, "");
    final IborIndexConvention tiborEuIndex = new IborIndexConvention(
        getConventionName(Currency.JPY, TIBOR_EUROYEN), getIds(Currency.JPY, TIBOR_EUROYEN), ACT_360, MODIFIED_FOLLOWING, 2, true, Currency.JPY,
        LocalTime.of(11, 00), "JP", JP, JP, "");
    // Deposit
    final String depositONConventionName = getConventionName(Currency.JPY, DEPOSIT_ON);
    final DepositConvention depositONConvention = new DepositConvention(
        depositONConventionName, getIds(Currency.JPY, DEPOSIT_ON), ACT_365, FOLLOWING, 0, false, Currency.JPY, JP);
    final String depositConventionName = getConventionName(Currency.JPY, DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(
        depositConventionName, getIds(Currency.JPY, DEPOSIT), ACT_365, FOLLOWING, 2, false, Currency.JPY, JP);
    // OIS legs
    final String oisFixedLegConventionName = getConventionName(Currency.JPY, OIS_FIXED_LEG);
    final String oisFloatLegConventionName = getConventionName(Currency.JPY, OIS_ON_LEG);
    final SwapFixedLegConvention oisFixedLegConvention = new SwapFixedLegConvention(
        oisFixedLegConventionName, getIds(Currency.JPY, OIS_FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.JPY, JP, 2, true, StubType.SHORT_START, false, 2);
    final OISLegConvention oisFloatLegConvention = new OISLegConvention(
        oisFloatLegConventionName, getIds(Currency.JPY, OIS_ON_LEG), onIndexId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    // Ibor swap legs
    final String irsFixedLegConventionName = getConventionName(Currency.JPY, IRS_FIXED_LEG);
    final String irsIborLegConventionName = getConventionName(Currency.JPY, tenorString, IRS_IBOR_LEG);
    final SwapFixedLegConvention irsFixedLegConvention = new SwapFixedLegConvention(
        irsFixedLegConventionName, getIds(Currency.JPY, IRS_FIXED_LEG),
        Tenor.SIX_MONTHS, ACT_365, MODIFIED_FOLLOWING, Currency.JPY, JP, 2, true, StubType.SHORT_START, false, 2);
    final VanillaIborLegConvention irsIborLegConvention = new VanillaIborLegConvention(
        irsIborLegConventionName, getIds(Currency.JPY, tenorString, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.SIX_MONTHS, 2, true, StubType.NONE, false, 2);
    // X-Ccy OIS
    final OISLegConvention oisXCcyUSDLegConvention = new OISLegConvention(
        OIS_USD_JPY_ON_LEG, getIds(OIS_USD_JPY_ON_LEG), onIndexId,
        Tenor.THREE_MONTHS, MODIFIED_FOLLOWING, 2, true, StubType.NONE, false, 2);
    
    // Convention add
    addConvention(master, onIndex);
    addConvention(master, liborIndex);
    addConvention(master, tiborJPIndex);
    addConvention(master, tiborEuIndex);
    addConvention(master, depositONConvention);
    addConvention(master, depositConvention);
    addConvention(master, oisFixedLegConvention);
    addConvention(master, oisFloatLegConvention);
    addConvention(master, irsFixedLegConvention);
    addConvention(master, irsIborLegConvention);
    addConvention(master, oisXCcyUSDLegConvention);
  }

}

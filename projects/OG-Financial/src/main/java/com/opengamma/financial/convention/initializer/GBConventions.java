/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.PAY_LAG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_1Y;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
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
    final String onIndexName = getConventionName(Currency.GBP, OVERNIGHT);
    final ExternalId onIndexId = ExternalId.of(SCHEME_NAME, onIndexName);
    final OvernightIndexConvention onIndex = new OvernightIndexConvention(
        onIndexName, getIds(Currency.GBP, OVERNIGHT), ACT_365, 0, Currency.GBP, GB);

    final String liborConventionName = getConventionName(Currency.GBP, LIBOR);
    final IborIndexConvention liborIndex = new IborIndexConvention(
        liborConventionName, getIds(Currency.GBP, LIBOR), ACT_365, MODIFIED_FOLLOWING, 0, true, Currency.GBP,
        LocalTime.of(11, 00), "GB", GB, GB, "");

    final String oisFixedLegConventionName = getConventionName(Currency.GBP, TENOR_STR_1Y, PAY_LAG + FIXED_LEG);
    final SwapFixedLegConvention oisFixedLegConvention = new SwapFixedLegConvention(
        oisFixedLegConventionName, getIds(Currency.GBP, TENOR_STR_1Y, PAY_LAG + FIXED_LEG),
        Tenor.ONE_YEAR, ACT_365, MODIFIED_FOLLOWING, Currency.GBP, GB, 2, true, StubType.SHORT_START, false, 2);
    final String oisFloatLegConventionName = getConventionName(Currency.GBP, OIS_ON_LEG);
    final OISLegConvention oisFloatLegConvention = new OISLegConvention(
        oisFloatLegConventionName, getIds(Currency.GBP, OIS_ON_LEG), onIndexId,
        Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, true, StubType.SHORT_START, false, 2);


    addConvention(master, liborIndex);
    addConvention(master, onIndex);
    addConvention(master, oisFixedLegConvention);
    addConvention(master, oisFloatLegConvention);
  }

}

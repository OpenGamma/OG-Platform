/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;

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
    addIborIndexConvention(master);
  }

  protected void addIborIndexConvention(final ConventionMaster master) {
    final String liborConventionName = getConventionName(Currency.GBP, LIBOR);
    final IborIndexConvention liborIndex = new IborIndexConvention(
        liborConventionName, getIds(Currency.GBP, LIBOR), ACT_365, MODIFIED_FOLLOWING, 0, true, Currency.GBP,
        LocalTime.of(11, 00), "GB", GB, GB, "");
    addConvention(master, liborIndex);
  }

}

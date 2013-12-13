/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.initializer;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.JIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
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
 * The conventions for Great Britain.
 */
public class ZAConventions extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ZAConventions();

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final ExternalId ZA = ExternalSchemes.financialRegionId("ZA");
  private static final Currency ZAR = Currency.of("ZAR");

  /**
   * Restricted constructor.
   */
  protected ZAConventions() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster master) {
    addSwapFixedLegConvention(master);
    addVanillaIborLegConvention(master);
    addDepositConventions(master);
    addLiborConventions(master);
  }

  protected void addSwapFixedLegConvention(final ConventionMaster master) {
    final String fixedSwapLegConventionName = getConventionName(ZAR, IRS_FIXED_LEG);
    final SwapFixedLegConvention fixedLegConvention = new SwapFixedLegConvention(fixedSwapLegConventionName, getIds(ZAR, IRS_FIXED_LEG),
        Tenor.THREE_MONTHS, ACT_365, FOLLOWING, ZAR, ZA, 2, false, StubType.NONE, false, 2);
    addConvention(master, fixedLegConvention);
  }

  protected void addVanillaIborLegConvention(final ConventionMaster master) {
    final String vanillaIborLegConventionName = getConventionName(ZAR, IRS_IBOR_LEG);
    final String tenorString = "3m";
    final String libor3mConventionName = getConventionName(ZAR, tenorString, JIBOR);
    final ExternalId libor3mConventionId = PerCurrencyConventionHelper.simpleNameId(libor3mConventionName);
    final VanillaIborLegConvention vanillaIborLegConvention = new VanillaIborLegConvention(
        vanillaIborLegConventionName, getIds(ZAR, tenorString, IRS_IBOR_LEG),
        libor3mConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
    addConvention(master, vanillaIborLegConvention);
  }

  protected void addDepositConventions(final ConventionMaster master) {
    final String depositConventionName = getConventionName(ZAR, DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(
        depositConventionName, getIds(ZAR, DEPOSIT), ACT_360, FOLLOWING, 0, false, ZAR, ZA);
    addConvention(master, depositConvention);
  }

  protected void addLiborConventions(final ConventionMaster master) {
    final String liborConventionName = getConventionName(ZAR, JIBOR);
    final IborIndexConvention liborConvention = new IborIndexConvention(
        liborConventionName, getIds(ZAR, JIBOR), ACT_365, FOLLOWING, 2, false, ZAR,
        LocalTime.of(11, 00), "ZA", ZA, ZA, "");
    addConvention(master, liborConvention);
  }

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.percurrency;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.JIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.InMemoryConventionMaster;
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
 *
 */
public class ZAConventions {
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ExternalId ZA = ExternalSchemes.financialRegionId("ZA");
  private static final Currency ZAR = Currency.of("ZAR");

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionMaster conventionMaster) {
    final String fixedSwapLegConventionName = getConventionName(ZAR, IRS_FIXED_LEG);
    final String vanillaIborLegConventionName = getConventionName(ZAR, IRS_IBOR_LEG);
    final String tenorString = "3m";
    final String libor3mConventionName = getConventionName(ZAR, tenorString, JIBOR);
    final ExternalId libor3mConventionId = InMemoryConventionBundleMaster.simpleNameSecurityId(libor3mConventionName);
    final Convention fixedLegConvention = new SwapFixedLegConvention(fixedSwapLegConventionName, getIds(ZAR, IRS_FIXED_LEG),
        Tenor.THREE_MONTHS, ACT_365, FOLLOWING, ZAR, ZA, 2, false, StubType.NONE, false, 2);
    final Convention vanillaIborLegConvention = new VanillaIborLegConvention(vanillaIborLegConventionName, getIds(ZAR, tenorString, IRS_IBOR_LEG),
        libor3mConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
    conventionMaster.add(fixedLegConvention);
    conventionMaster.add(vanillaIborLegConvention);
    addDepositConventions(conventionMaster);
    addLiborConventions(conventionMaster);
  }

  private static void addDepositConventions(final InMemoryConventionMaster conventionMaster) {
    final String depositConventionName = getConventionName(ZAR, DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(depositConventionName, getIds(ZAR, DEPOSIT), ACT_360, FOLLOWING, 0, false, ZAR, ZA);
    conventionMaster.add(depositConvention);
  }

  private static void addLiborConventions(final InMemoryConventionMaster conventionMaster) {
    final String liborConventionName = getConventionName(ZAR, JIBOR);
    final Convention liborConvention = new IborIndexConvention(liborConventionName, getIds(ZAR, JIBOR), ACT_365, FOLLOWING, 2, false, ZAR,
        LocalTime.of(11, 00), "ZA", ZA, ZA, "");
    conventionMaster.add(liborConvention);
  }
}

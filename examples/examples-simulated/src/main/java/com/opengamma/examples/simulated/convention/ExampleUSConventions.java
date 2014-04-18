/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.DEPOSIT_ON;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.TENOR_STR_3M;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getId;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getIds;

import org.threeten.bp.LocalTime;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * US conventions.
 */
public class ExampleUSConventions extends ConventionMasterInitializer {
  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new ExampleUSConventions();
  /** US holidays */
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  /** New York + London holidays */
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");

  /**
   * Restricted constructor.
   */
  protected ExampleUSConventions() {
  }

  @Override
  public void init(final ConventionMaster master) {
    // Deposit conventions
    final String depositConventionName = getConventionName(Currency.USD, DEPOSIT);
    final DepositConvention depositConvention = new DepositConvention(
        depositConventionName, getIds(Currency.USD, DEPOSIT), DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
    final String depositONConventionName = getConventionName(Currency.USD, DEPOSIT_ON);
    final DepositConvention depositONConvention = new DepositConvention(
        depositONConventionName, getIds(Currency.USD, DEPOSIT_ON), DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, 0, false, Currency.USD, US);

    // Ibor conventions
    final String iborTicker = "USDLIBORP3M";
    final ExternalId liborConventionId = ExternalSchemes.syntheticSecurityId(iborTicker);
    final IborIndexConvention liborIndexConvention = new IborIndexConvention(iborTicker, liborConventionId.toBundle(),
        DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 00), "US", NYLON, US, "");
    final String liborLeg3MConventionName = getConventionName(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG);
    final VanillaIborLegConvention liborLeg3MConvention = new VanillaIborLegConvention(
        liborLeg3MConventionName, getIds(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG),
        liborConventionId, true, Interpolator1DFactory.LINEAR, Tenor.THREE_MONTHS, 2, false, StubType.SHORT_START, false, 0);
    final String irsFixedLegConventionName = getConventionName(Currency.USD, IRS_FIXED_LEG);
    final SwapFixedLegConvention irsFixedLegConvention = new SwapFixedLegConvention(
        irsFixedLegConventionName, getIds(Currency.USD, IRS_FIXED_LEG),
        Tenor.SIX_MONTHS, DayCounts.THIRTY_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.SHORT_START, false, 0);
    final String iborTicker6m = "USDLIBORP6M";
    final ExternalId liborConventionId6m = ExternalSchemes.syntheticSecurityId(iborTicker6m);
    final IborIndexConvention liborIndexConvention6m = new IborIndexConvention(iborTicker6m, liborConventionId6m.toBundle(),
        DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, 2, true, Currency.USD, LocalTime.of(11, 00), "US", NYLON, US, "");

    // Overnight conventions
    final String overnightTicker = "USDFF";
    final ExternalId overnightConventionId = ExternalSchemes.syntheticSecurityId(overnightTicker);
    final OvernightIndexConvention overnightConvention = new OvernightIndexConvention(
        overnightTicker, overnightConventionId.toBundle(), DayCounts.ACT_360, 1, Currency.USD, US);
    final String oisFixedLegConventionName = getConventionName(Currency.USD, OIS_FIXED_LEG);
    final SwapFixedLegConvention oisFixedLegConvention = new SwapFixedLegConvention(
        oisFixedLegConventionName, getIds(Currency.USD, OIS_FIXED_LEG),
        Tenor.ONE_YEAR, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, Currency.USD, US, 2, false, StubType.SHORT_START, false, 2);
    final String oisONLegConventionName = getConventionName(Currency.USD, OIS_ON_LEG);
    final OISLegConvention oisONLegConvention = new OISLegConvention(
        oisONLegConventionName, getIds(Currency.USD, OIS_ON_LEG), overnightConventionId,
        Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 2);

    // Swap and swap index index conventions
    final String swapConventionName = "USD Vanilla Ibor Swap";
    final ExternalId swapConventionId = ExternalId.of(SCHEME_NAME, swapConventionName);
    final String swapIndexName = "USD ISDA Fixing";
    final SwapConvention swapConvention = new SwapConvention(swapConventionName, ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(swapConventionName)),
        getId(Currency.USD, TENOR_STR_3M, IRS_IBOR_LEG), liborConventionId);
    final SwapIndexConvention swapIndexConvention = new SwapIndexConvention(swapIndexName,
        ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(swapIndexName)), LocalTime.of(11, 0), swapConventionId);

    addConvention(master, depositConvention);
    addConvention(master, depositONConvention);
    addConvention(master, liborIndexConvention);
    addConvention(master, liborIndexConvention6m);
    addConvention(master, liborLeg3MConvention);
    addConvention(master, irsFixedLegConvention);
    addConvention(master, overnightConvention);
    addConvention(master, oisFixedLegConvention);
    addConvention(master, oisONLegConvention);
    addConvention(master, swapConvention);
    addConvention(master, swapIndexConvention);
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.core.id.ExternalSchemes.tullettPrebonSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of BRL instruments
 */
public class BRConventions {
  /** Month codes used by Bloomberg */
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};

  /**
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount bus252 = DayCounts.BUSINESS_252;
    final ExternalId br = ExternalSchemes.financialRegionId("BR");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    for (int i = 1; i < 3; i++) {
      final String dayDepositName = "BRL DEPOSIT " + i + "d";
      final ExternalId dayBbgDeposit = bloombergTickerSecurityId("BCDR" + i + "T Curncy");
      final ExternalId daySimpleDeposit = simpleNameSecurityId(dayDepositName);
      final String weekDepositName = "BRL DEPOSIT " + i + "w";
      final ExternalId weekBbgDeposit = bloombergTickerSecurityId("BCDR" + i + "Z Curncy");
      final ExternalId weekSimpleDeposit = simpleNameSecurityId(weekDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(dayBbgDeposit, daySimpleDeposit), dayDepositName, bus252, following, Period.ofDays(i), 2, false, br);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgDeposit, weekSimpleDeposit), weekDepositName, bus252, following, Period.ofDays(i * 7), 2, false, br);
    }

    for (int i = 1; i < 12; i++) {
      final String depositName = "BRL DEPOSIT " + i + "m";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("BCDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      final String impliedDepositName = "BRL IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullettImpliedDeposit = tullettPrebonSecurityId("LMIDPBRLSPT" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simpleImpliedDeposit = simpleNameSecurityId(impliedDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, bus252, following, Period.ofMonths(i), 2, false, br);
      utils.addConventionBundle(ExternalIdBundle.of(tullettImpliedDeposit, simpleImpliedDeposit), impliedDepositName, bus252, following, Period.ofMonths(i), 2, false, br);
    }

    for (int i = 1; i < 2; i++) {
      final String depositName = "BRL DEPOSIT " + i + "y";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("BCDR" + i + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, bus252, following, Period.ofYears(i), 2, false, br);
    }

    final DayCount swapFixedLegDayCount = DayCounts.BUSINESS_252;
    final BusinessDayConvention swapFixedLegBusinessDayConvention = BusinessDayConventions.MODIFIED_FOLLOWING;
    final Frequency swapFixedLegPaymentFrequency = PeriodFrequency.ANNUAL;
    final int swapFixedLegSettlementDays = 2;
    final ExternalId swapFixedLegRegion = br;
    final Frequency swapFixedLegCompoundingFrequency = PeriodFrequency.DAILY;
    final InterestRate.Type swapFixedLegCompoundingType = InterestRate.Type.CONTINUOUS;
    final DayCount swapFloatingLegDayCount = DayCounts.BUSINESS_252;
    final BusinessDayConvention swapFloatingLegBusinessDayConvention = BusinessDayConventions.MODIFIED_FOLLOWING;
    final Frequency swapFloatingLegPaymentFrequency = PeriodFrequency.ANNUAL;
    final int swapFloatingLegSettlementDays = 2;
    final ExternalId swapFloatingLegInitialRate = bloombergTickerSecurityId("BZDIOVRA Index");
    final ExternalId swapFloatingLegRegion = br;
    final Frequency swapFloatingLegCompoundingFrequency = PeriodFrequency.DAILY;
    final InterestRate.Type swapFloatingLegCompoundingType = InterestRate.Type.CONTINUOUS;
    final boolean isEOM = true;

    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("BZDIOVRA Index"), simpleNameSecurityId("Brazil Cetip Interbank Deposit Rate")),
        "Brazil Cetip Interbank Deposit Rate", bus252, following, Period.ofDays(1), 0, false, br, 0);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("BRL_DI_SWAP")), "BRL_DI_SWAP", swapFixedLegDayCount, swapFixedLegBusinessDayConvention, swapFixedLegPaymentFrequency,
        swapFixedLegSettlementDays, swapFixedLegRegion, swapFixedLegCompoundingFrequency, swapFixedLegCompoundingType, swapFloatingLegDayCount, swapFloatingLegBusinessDayConvention,
        swapFloatingLegPaymentFrequency, swapFloatingLegSettlementDays, swapFloatingLegCompoundingFrequency, swapFloatingLegCompoundingType, swapFloatingLegInitialRate, swapFloatingLegRegion, isEOM);
  }


  /**
   * Adds conventions for BRL inflation government bonds.
   * @param conventionMaster The convention master, not null
   */
  public static void addInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("BR_INFLATION_BOND_CONVENTION")), "BR_INFLATION_BOND_CONVENTION", false,
        true, 0, 1, true);
  }

}

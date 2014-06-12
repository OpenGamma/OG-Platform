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

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of GBP instruments.
 *
 */
public class GBConventions {
  /** Month codes used by Bloomberg */
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K' };
  /** Modified business day convention */
  private static final BusinessDayConvention MODIFIED = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** Following business day convention */
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  /** Act/365 */
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  /** Annual frequency */
  private static final Frequency ANNUAL = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
  /** Semi-annual frequency */
  private static final Frequency SEMI_ANNUAL = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
  /** Quarterly frequency */
  private static final Frequency QUARTERLY = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
  /** GB holidays */
  private static final ExternalId GB = ExternalSchemes.financialRegionId("GB");

  /**
   * Adds conventions for deposit, Libor fixings, swaps, FRAs and IR futures.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP00O/N Index"), simpleNameSecurityId("GBP LIBOR O/N"),
        tullettPrebonSecurityId("ASLIBGBPONL")), "GBP LIBOR O/N", ACT_365, FOLLOWING, Period.ofDays(1), 0, false, GB);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP00T/N Index"), simpleNameSecurityId("GBP LIBOR T/N")),
        "GBP LIBOR T/N", ACT_365, FOLLOWING, Period.ofDays(1), 1, false, GB);

    for (int i = 1; i < 4; i++) {
      final String dayDepositName = "GBP DEPOSIT " + i + "d";
      final ExternalId dayBbgDeposit = bloombergTickerSecurityId("BPDR" + i + "T Curncy");
      final ExternalId daySimpleDeposit = simpleNameSecurityId(dayDepositName);
      final String weekDepositName = "GBP DEPOSIT " + i + "w";
      final ExternalId weekBbgDeposit = bloombergTickerSecurityId("BPDR" + i + "Z Curncy");
      final ExternalId weekTullettDeposit = tullettPrebonSecurityId("MNDEPGBDTDY0" + i + "W");
      final ExternalId weekSimpleDeposit = simpleNameSecurityId(weekDepositName);
      final String weekLiborName = "GBP LIBOR " + i + "w";
      final ExternalId weekBbgLibor = bloombergTickerSecurityId("BP000" + i + "W Index");
      final ExternalId weekTullettLibor = tullettPrebonSecurityId("ASLIBGBP" + i + "WL");
      final ExternalId weekSimpleLibor = simpleNameSecurityId(weekDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(dayBbgDeposit, daySimpleDeposit), dayDepositName, ACT_365, FOLLOWING, Period.ofDays(i), 0, false, GB);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgDeposit, weekTullettDeposit, weekSimpleDeposit), weekDepositName, ACT_365, FOLLOWING, Period.ofDays(i * 7), 0, false, GB);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgLibor, weekTullettLibor, weekSimpleLibor), weekLiborName, ACT_365, FOLLOWING, Period.ofDays(i * 7), 0, false, GB);
    }

    for (int i = 1; i < 13; i++) {
      final String liborName = "GBP LIBOR " + i + "m";
      final ExternalId bbgLibor = bloombergTickerSecurityId("BP00" + (i < 10 ? "0" : "") + i + "M Index");
      final ExternalId tullettLibor = tullettPrebonSecurityId("ASLIBGBP" + (i < 10 ? "0" : "") + i + "L");
      final ExternalId simpleLibor = simpleNameSecurityId(liborName);
      final String depositName = "GBP DEPOSIT " + i + "m";
      ExternalId bbgDeposit;
      if (i == 12) {
        bbgDeposit = bloombergTickerSecurityId("BPDR1" + " Curncy");
      } else {
        bbgDeposit = bloombergTickerSecurityId("BPDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      }
      final ExternalId tullettDeposit = tullettPrebonSecurityId("MNDEPGBDTDY" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgLibor, tullettLibor, simpleLibor), liborName, ACT_365, MODIFIED, Period.ofMonths(i), 0, false, GB);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, tullettDeposit, simpleDeposit), depositName, ACT_365, MODIFIED, Period.ofMonths(i), 0, false, GB);
    }

    for (int i = 1; i < 6; i++) {
      final String depositName = "GBP DEPOSIT " + i + "y";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("BPDR" + i + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, ACT_365, MODIFIED, Period.ofYears(i), 0, false, GB);
    }

    final DayCount swapFixedDayCount = ACT_365;
    final BusinessDayConvention swapFixedBusinessDay = MODIFIED;
    final Frequency swapFixedPaymentFrequency = SEMI_ANNUAL;
    final Frequency swapFixedPaymentFrequency1Y = ANNUAL;
    final DayCount liborDayCount = ACT_365;
    // Overnight Index Swap Convention have additional flag, publicationLag
    final int publicationLagON = 0;

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_SWAP")), "GBP_SWAP", ACT_365, MODIFIED, SEMI_ANNUAL, 0, GB, ACT_365,
        MODIFIED, SEMI_ANNUAL, 0, simpleNameSecurityId("GBP LIBOR 6m"), GB, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_3M_SWAP")), "GBP_3M_SWAP", swapFixedDayCount, MODIFIED, ANNUAL, 0, GB, ACT_365,
        MODIFIED, QUARTERLY, 0, simpleNameSecurityId("GBP LIBOR 3m"), GB, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_6M_SWAP")), "GBP_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 0, GB,
        liborDayCount, MODIFIED, SEMI_ANNUAL, 0, simpleNameSecurityId("GBP LIBOR 6m"), GB, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_3M_FRA")), "GBP_3M_FRA", ACT_365, MODIFIED, ANNUAL, 0, GB, ACT_365,
        MODIFIED, QUARTERLY, 0, simpleNameSecurityId("GBP LIBOR 3m"), GB, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_6M_FRA")), "GBP_6M_FRA", ACT_365, MODIFIED, SEMI_ANNUAL, 0, GB,
        ACT_365, MODIFIED, SEMI_ANNUAL, 0, simpleNameSecurityId("GBP LIBOR 6m"), GB, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_P3M")), IndexType.Libor + "_P3M", ACT_365, MODIFIED,
        null, 0, false, GB, 0);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_P6M")), IndexType.Libor + "_P6M", ACT_365, MODIFIED,
        null, 0, false, GB, 0);
    // SONIA
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SONIO/N Index"), simpleNameSecurityId("GBP SONIO/N")), "GBP SONIO/N", ACT_365,
        FOLLOWING, Period.ofDays(1), 0, false, GB, publicationLagON);
    // OIS - SONIA
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_OIS_SWAP")), "GBP_OIS_SWAP", ACT_365, MODIFIED, ANNUAL, 2, GB,
        ACT_365, MODIFIED, ANNUAL, 2, simpleNameSecurityId("GBP SONIO/N"), GB, true, publicationLagON);

    //TODO sort out the swap names so that they are consistent
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_IBOR_INDEX")), "GBP_IBOR_INDEX", ACT_365, MODIFIED, 0, false);

    final int[] isdaFixTenor = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30 };
    // ISDA fixing Libor 11:00am London
    utils.addConventionBundle(
        ExternalIdBundle.of(simpleNameSecurityId("GBP_ISDAFIX_GBPLIBOR11_1Y"), ExternalSchemes.ricSecurityId("GBPSFIX1Y="),
            bloombergTickerSecurityId("BPISDB01 Index")), "GBP_ISDAFIX_GBPLIBOR11_1Y", swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency1Y, 0, GB, liborDayCount, MODIFIED,
        QUARTERLY, 2, simpleNameSecurityId("GBP LIBOR 3m"), GB, true, Period.ofYears(1));
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String tenorStringBbg = String.format("%02d", element);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_ISDAFIX_GBPLIBOR11_" + tenorString),
          ExternalSchemes.ricSecurityId("GBPSFIX" + tenorString + "="), bloombergTickerSecurityId("BPISDB" + tenorStringBbg + " Index")), "GBP_ISDAFIX_GBPLIBOR11_" + tenorString,
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 0, GB, liborDayCount, MODIFIED, SEMI_ANNUAL, 0, simpleNameSecurityId("GBP LIBOR 6m"),
          GB, true, Period.ofYears(element));
    }

  }

  /**
   * Adds conventions for GBP government bonds.
   * @param conventionMaster The convention master, not null
   */
  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GB_TREASURY_BOND_CONVENTION")), "GB_TREASURY_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  /**
   * Adds conventions for GBP government bonds.
   * @param conventionMaster The convention master, not null
   */
  public static void addInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GB_INFLATION_BOND_CONVENTION")), "GB_INFLATION_BOND_CONVENTION", false,
        true, 6, 2, true);
  }

  /**
   * Adds conventions for GBP corporate inflation bonds issued in NL.
   * @param conventionMaster The convention master, not null
   */
  public static void addNLInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("NL_INFLATION_BOND_CONVENTION")), "NL_INFLATION_BOND_CONVENTION", false,
        true, 6, 2, true);
  }

  /**
   * Adds conventions for GBP corporate inflation bonds issued in JE.
   * @param conventionMaster The convention master, not null
   */
  public static void addJEInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JE_INFLATION_BOND_CONVENTION")), "JE_INFLATION_BOND_CONVENTION", false,
        true, 6, 2, true);
  }

  /**
   * Adds conventions for GBP corporate inflation bonds issued in KY.
   * @param conventionMaster The convention master, not null
   */
  public static void addKYInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("KY_INFLATION_BOND_CONVENTION")), "KY_INFLATION_BOND_CONVENTION", false,
        true, 6, 2, true);
  }

  /**
   * Adds conventions for GBP corporate inflation bonds issued in US.
   * @param conventionMaster The convention master, not null
   */
  public static void addUSInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_INFLATION_BOND_CONVENTION")), "uS_INFLATION_BOND_CONVENTION", false,
        true, 6, 3, true);
  }

  /**
   * Adds conventions for GBP-denominated corporate bonds.
   * @param conventionMaster The convention master, not null
   */
  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GB_CORPORATE_BOND_CONVENTION")), "GB_CORPORATE_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  /**
   * Adds conventions for GBP bond futures.
   * @param conventionMaster The convention master, not null
   */
  public static void addBondFutureConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_BOND_FUTURE_DELIVERABLE_CONVENTION")),
        "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION", true, true, 7, 0, DayCounts.ACT_365, BusinessDayConventions.FOLLOWING,
        SimpleYieldConvention.MONEY_MARKET);
  }
}

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
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of DKK instruments.
 */
public class DKConventions {

  /**
   * Adds conventions for deposit, Libor and Cibor fixings, swaps and FRAs.
   * @param conventionMaster The convention master, not null
   */
  public static void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId dk = ExternalSchemes.financialRegionId("DK");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO01W Index"), simpleNameSecurityId("DKK CIBOR 1w"),
        tullettPrebonSecurityId("ASLIBDDK1WL")), "DKK CIBOR 1w", act360, following, Period.ofDays(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO02W Index"), simpleNameSecurityId("DKK CIBOR 2w"),
        tullettPrebonSecurityId("ASLIBDDK2WL")), "DKK CIBOR 2w", act360, following, Period.ofDays(14), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO01M Index"), simpleNameSecurityId("DKK CIBOR 1m"),
        tullettPrebonSecurityId("ASLIBDDK01L")), "DKK CIBOR 1m", act360, following, Period.ofMonths(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO02M Index"), simpleNameSecurityId("DKK CIBOR 2m"),
        tullettPrebonSecurityId("ASLIBDDK02L")), "DKK CIBOR 2m", act360, following, Period.ofMonths(2), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO03M Index"), simpleNameSecurityId("DKK CIBOR 3m"),
        tullettPrebonSecurityId("ASLIBDDK03L")), "DKK CIBOR 3m", act360, following, Period.ofMonths(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO04M Index"), simpleNameSecurityId("DKK CIBOR 4m"),
        tullettPrebonSecurityId("ASLIBDDK04L")), "DKK CIBOR 4m", act360, following, Period.ofMonths(4), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO05M Index"), simpleNameSecurityId("DKK CIBOR 5m"),
        tullettPrebonSecurityId("ASLIBDDK05L")), "DKK CIBOR 5m", act360, following, Period.ofMonths(5), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO06M Index"), simpleNameSecurityId("DKK CIBOR 6m"),
        tullettPrebonSecurityId("ASLIBDDK06L")), "DKK CIBOR 6m", act360, following, Period.ofMonths(6), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO07M Index"), simpleNameSecurityId("DKK CIBOR 7m"),
        tullettPrebonSecurityId("ASLIBDDK07L")), "DKK CIBOR 7m", act360, following, Period.ofMonths(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO08M Index"), simpleNameSecurityId("DKK CIBOR 8m"),
        tullettPrebonSecurityId("ASLIBDDK08L")), "DKK CIBOR 8m", act360, following, Period.ofMonths(8), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO09M Index"), simpleNameSecurityId("DKK CIBOR 9m"),
        tullettPrebonSecurityId("ASLIBDDK09L")), "DKK CIBOR 9m", act360, following, Period.ofMonths(9), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO09M Index"), simpleNameSecurityId("DKK CIBOR 10m"),
        tullettPrebonSecurityId("ASLIBDDK10L")), "DKK CIBOR 10m", act360, following, Period.ofMonths(10), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO09M Index"), simpleNameSecurityId("DKK CIBOR 11m"),
        tullettPrebonSecurityId("ASLIBDDK11L")), "DKK CIBOR 11m", act360, following, Period.ofMonths(11), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CIBO09M Index"), simpleNameSecurityId("DKK CIBOR 1y"),
        tullettPrebonSecurityId("ASLIBDDK12L")), "DKK CIBOR 1y", act360, following, Period.ofYears(1), 2, false, dk);

    for (int i = 1; i < 3; i++) {
      final ExternalId tullett = tullettPrebonSecurityId("ASLIBDKK" + i + "WL");
      final String name = "DKK LIBOR " + i + "w";
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, Period.ofDays(i * 7), 2, false, dk);
    }

    for (int i = 1; i < 12; i++) {
      final ExternalId tullett = tullettPrebonSecurityId("ASLIBDKK" + (i < 10 ? "0" : "") + i + "L");
      final String name = "DKK LIBOR " + i + "m";
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, Period.ofMonths(i), 2, false, dk);
    }

    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBDKK12L"), simpleNameSecurityId("DKK CIBOR 1y")),
        "DKK CIBOR 1y", act360, following, Period.ofYears(1), 2, false, dk);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR1T Curncy"), simpleNameSecurityId("DKK DEPOSIT 1d")), "DKK DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR2T Curncy"), simpleNameSecurityId("DKK DEPOSIT 2d")), "DKK DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR3T Curncy"), simpleNameSecurityId("DKK DEPOSIT 3d")), "DKK DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR1Z Curncy"), simpleNameSecurityId("DKK DEPOSIT 1w")), "DKK DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR2Z Curncy"), simpleNameSecurityId("DKK DEPOSIT 2w")), "DKK DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR3Z Curncy"), simpleNameSecurityId("DKK DEPOSIT 3w")), "DKK DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRA Curncy"), simpleNameSecurityId("DKK DEPOSIT 1m")), "DKK DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRB Curncy"), simpleNameSecurityId("DKK DEPOSIT 2m")), "DKK DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRC Curncy"), simpleNameSecurityId("DKK DEPOSIT 3m")), "DKK DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRD Curncy"), simpleNameSecurityId("DKK DEPOSIT 4m")), "DKK DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRE Curncy"), simpleNameSecurityId("DKK DEPOSIT 5m")), "DKK DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRF Curncy"), simpleNameSecurityId("DKK DEPOSIT 6m")), "DKK DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRG Curncy"), simpleNameSecurityId("DKK DEPOSIT 7m")), "DKK DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRH Curncy"), simpleNameSecurityId("DKK DEPOSIT 8m")), "DKK DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRI Curncy"), simpleNameSecurityId("DKK DEPOSIT 9m")), "DKK DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRJ Curncy"), simpleNameSecurityId("DKK DEPOSIT 10m")), "DKK DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDRK Curncy"), simpleNameSecurityId("DKK DEPOSIT 11m")), "DKK DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR1 Curncy"), simpleNameSecurityId("DKK DEPOSIT 1y")), "DKK DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR2 Curncy"), simpleNameSecurityId("DKK DEPOSIT 2y")), "DKK DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR3 Curncy"), simpleNameSecurityId("DKK DEPOSIT 3y")), "DKK DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR4 Curncy"), simpleNameSecurityId("DKK DEPOSIT 4y")), "DKK DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DKDR5 Curncy"), simpleNameSecurityId("DKK DEPOSIT 5y")), "DKK DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, dk);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_SWAP")), "DKK_SWAP", thirty360, modified, annual, 1, dk, act360,
        modified, semiAnnual, 1, simpleNameSecurityId("DKK CIBOR 6m"), dk, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_3M_SWAP")), "DKK_3M_SWAP", thirty360, modified, annual, 2, dk,
        act360, modified, quarterly, 2, simpleNameSecurityId("DKK CIBOR 3m"), dk, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_6M_SWAP")), "DKK_6M_SWAP", thirty360, modified, annual, 2, dk,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("DKK CIBOR 6m"), dk, true);

    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLagON = 0;
    // Overnight-like rate
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("DETNT/N Index"), simpleNameSecurityId("DKK T/N")),
        "DKK T/N", act360, following, Period.ofDays(1), 1, false, dk, publicationLagON);
    // OIS-like swap
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_OIS_SWAP")), "DKK_OIS_SWAP", act360, modified, annual, 1, dk,
        act360, modified, annual, 1, simpleNameSecurityId("DKK T/N"), dk, true, publicationLagON);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_IBOR_INDEX")), "DKK_IBOR_INDEX", act360, following, 1, false);

    // FRA conventions stored as IRS
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_3M_FRA")), "DKK_3M_FRA", thirty360, modified, annual, 2, dk, act360,
        modified, quarterly, 2, simpleNameSecurityId("DKK CIBOR 3m"), dk, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DKK_6M_FRA")), "DKK_6M_FRA", thirty360, modified, annual, 2, dk, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("DKK CIBOR 6m"), dk, true);
  }

  //TODO all of the conventions named treasury need to be changed
  //TODO the ex-dividend days is wrong
  public static void addDKTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DK_TREASURY_BOND_CONVENTION")), "DK_TREASURY_BOND_CONVENTION", true,
        true, 30, 3, true);
  }

  //TODO all of the conventions named treasury need to be changed
  //TODO the ex-dividend days is wrong
  public static void addDKCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("DK_CORPORATE_BOND_CONVENTION")), "DK_CORPORATE_BOND_CONVENTION", true,
        true, 30, 3, true);
  }

}

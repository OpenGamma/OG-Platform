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
import com.opengamma.util.time.Tenor;

/**
 * Contains information used to construct standard versions of CAD instruments.
 */
public class CAConventions {

  /**
   * Adds conventions for deposit, Libor, BA fixings, swaps, FRAs and BA futures.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId ca = ExternalSchemes.financialRegionId("CA");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD00O/N Index"), simpleNameSecurityId("CAD LIBOR O/N"),
        tullettPrebonSecurityId("ASLIBCADONL")),
        "CAD LIBOR O/N", act360, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD00S/N Index"), simpleNameSecurityId("CAD LIBOR S/N")),
        "CAD LIBOR S/N", act360, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD00T/N Index"), simpleNameSecurityId("CAD LIBOR T/N")),
        "CAD LIBOR T/N", act360, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0001W Index"), simpleNameSecurityId("CAD LIBOR 1w"),
        tullettPrebonSecurityId("ASLIBCAD1WL")),
        "CAD LIBOR 1w", act360, following, Period.ofDays(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0002W Index"), simpleNameSecurityId("CAD LIBOR 2w"),
        tullettPrebonSecurityId("ASLIBCAD2WL")),
        "CAD LIBOR 2w", act360, following, Period.ofDays(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0001M Index"), simpleNameSecurityId("CAD LIBOR 1m"),
        tullettPrebonSecurityId("ASLIBCAD01L")),
        "CAD LIBOR 1m", act360, following, Period.ofMonths(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0002M Index"), simpleNameSecurityId("CAD LIBOR 2m"),
        tullettPrebonSecurityId("ASLIBCAD02L")),
        "CAD LIBOR 2m", act360, following, Period.ofMonths(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0003M Index"), simpleNameSecurityId("CAD LIBOR 3m"),
        tullettPrebonSecurityId("ASLIBCAD03L")),
        "CAD LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0004M Index"), simpleNameSecurityId("CAD LIBOR 4m"),
        tullettPrebonSecurityId("ASLIBCAD04L")),
        "CAD LIBOR 4m", act360, following, Period.ofMonths(4), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0005M Index"), simpleNameSecurityId("CAD LIBOR 5m"),
        tullettPrebonSecurityId("ASLIBCAD05L")),
        "CAD LIBOR 5m", act360, following, Period.ofMonths(5), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0006M Index"), simpleNameSecurityId("CAD LIBOR 6m"),
        tullettPrebonSecurityId("ASLIBCAD06L")),
        "CAD LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0007M Index"), simpleNameSecurityId("CAD LIBOR 7m"),
        tullettPrebonSecurityId("ASLIBCAD07L")),
        "CAD LIBOR 7m", act360, following, Period.ofMonths(7), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0008M Index"), simpleNameSecurityId("CAD LIBOR 8m"),
        tullettPrebonSecurityId("ASLIBCAD08L")),
        "CAD LIBOR 8m", act360, following, Period.ofMonths(8), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0009M Index"), simpleNameSecurityId("CAD LIBOR 9m"),
        tullettPrebonSecurityId("ASLIBCAD09L")),
        "CAD LIBOR 9m", act360, following, Period.ofMonths(9), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0010M Index"), simpleNameSecurityId("CAD LIBOR 10m"),
        tullettPrebonSecurityId("ASLIBCAD10L")),
        "CAD LIBOR 10m", act360, following, Period.ofMonths(10), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0011M Index"), simpleNameSecurityId("CAD LIBOR 11m"),
        tullettPrebonSecurityId("ASLIBCAD11L")),
        "CAD LIBOR 11m", act360, following, Period.ofMonths(11), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0012M Index"), simpleNameSecurityId("CAD LIBOR 12m"),
        tullettPrebonSecurityId("ASLIBCAD12L")),
        "CAD LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, ca);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR1T Curncy"), simpleNameSecurityId("CAD DEPOSIT 1d"),
        tullettPrebonSecurityId("MNDEPCADTDYTOM")),
        "CAD DEPOSIT 1d", act365, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR2T Curncy"), simpleNameSecurityId("CAD DEPOSIT 2d")),
        "CAD DEPOSIT 2d", act365, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR3T Curncy"), simpleNameSecurityId("CAD DEPOSIT 3d")),
        "CAD DEPOSIT 3d", act365, following, Period.ofDays(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR1Z Curncy"), simpleNameSecurityId("CAD DEPOSIT 1w"),
        tullettPrebonSecurityId("MNDEPCADSPT01W")),
        "CAD DEPOSIT 1w", act365, following, Period.ofDays(7), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR2Z Curncy"), simpleNameSecurityId("CAD DEPOSIT 2w")),
        "CAD DEPOSIT 2w", act365, following, Period.ofDays(14), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR3Z Curncy"), simpleNameSecurityId("CAD DEPOSIT 3w")),
        "CAD DEPOSIT 3w", act365, following, Period.ofDays(21), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRA Curncy"), simpleNameSecurityId("CAD DEPOSIT 1m"),
        tullettPrebonSecurityId("MNDEPCADSPT01M")),
        "CAD DEPOSIT 1m", act365, following, Period.ofMonths(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRB Curncy"), simpleNameSecurityId("CAD DEPOSIT 2m"),
        tullettPrebonSecurityId("MNDEPCADSPT02M")),
        "CAD DEPOSIT 2m", act365, following, Period.ofMonths(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRC Curncy"), simpleNameSecurityId("CAD DEPOSIT 3m"),
        tullettPrebonSecurityId("MNDEPCADSPT03M")),
        "CAD DEPOSIT 3m", act365, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRD Curncy"), simpleNameSecurityId("CAD DEPOSIT 4m")),
        "CAD DEPOSIT 4m", act365, following, Period.ofMonths(4), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRE Curncy"), simpleNameSecurityId("CAD DEPOSIT 5m")),
        "CAD DEPOSIT 5m", act365, following, Period.ofMonths(5), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRF Curncy"), simpleNameSecurityId("CAD DEPOSIT 6m"),
        tullettPrebonSecurityId("MNDEPCADSPT06M")),
        "CAD DEPOSIT 6m", act365, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRG Curncy"), simpleNameSecurityId("CAD DEPOSIT 7m")),
        "CAD DEPOSIT 7m", act365, following, Period.ofMonths(7), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRH Curncy"), simpleNameSecurityId("CAD DEPOSIT 8m")),
        "CAD DEPOSIT 8m", act365, following, Period.ofMonths(8), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRI Curncy"), simpleNameSecurityId("CAD DEPOSIT 9m"),
        tullettPrebonSecurityId("MNDEPCADSPT09M")),
        "CAD DEPOSIT 9m", act365, following, Period.ofMonths(9), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRJ Curncy"), simpleNameSecurityId("CAD DEPOSIT 10m")),
        "CAD DEPOSIT 10m", act365, following, Period.ofMonths(10), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRK Curncy"), simpleNameSecurityId("CAD DEPOSIT 11m")),
        "CAD DEPOSIT 11m", act365, following, Period.ofMonths(11), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR1 Curncy"), simpleNameSecurityId("CAD DEPOSIT 1y"),
        tullettPrebonSecurityId("MNDEPCADSPT12M")),
        "CAD DEPOSIT 1y", act365, following, Period.ofYears(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR2 Curncy"), simpleNameSecurityId("CAD DEPOSIT 2y")),
        "CAD DEPOSIT 2y", act365, following, Period.ofYears(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR3 Curncy"), simpleNameSecurityId("CAD DEPOSIT 3y")),
        "CAD DEPOSIT 3y", act365, following, Period.ofYears(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR4 Curncy"), simpleNameSecurityId("CAD DEPOSIT 4y")),
        "CAD DEPOSIT 4y", act365, following, Period.ofYears(4), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR5 Curncy"), simpleNameSecurityId("CAD DEPOSIT 5y")),
        "CAD DEPOSIT 5y", act365, following, Period.ofYears(5), 2, false, ca);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR01 Index"), bloombergTickerSecurityId("CDOR01 RBC Index"),
            simpleNameSecurityId("CDOR 1m"), tullettPrebonSecurityId("ASLIBCDF01L")),
            "CDOR 1m", act365, following, Period.ofMonths(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR02 Index"), bloombergTickerSecurityId("CDOR02 RBC Index"),
            simpleNameSecurityId("CDOR 2m"), tullettPrebonSecurityId("ASLIBCDF02L")),
            "CDOR 2m", act365, following, Period.ofMonths(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR03 Index"), bloombergTickerSecurityId("CDOR03 RBC Index"),
            simpleNameSecurityId("CDOR 3m"), tullettPrebonSecurityId("ASLIBCDF03L")),
            "CDOR 3m", act365, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR06 Index"), bloombergTickerSecurityId("CDOR06 RBC Index"),
            simpleNameSecurityId("CDOR 6m"), tullettPrebonSecurityId("ASLIBCDF06L")),
            "CDOR 6m", act365, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR12 Index"), bloombergTickerSecurityId("CDOR12 RBC Index"),
            simpleNameSecurityId("CDOR 12m"), tullettPrebonSecurityId("ASLIBCDF12L")),
            "CDOR 12m", act365, following, Period.ofMonths(12), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CAONREPO Index"), simpleNameSecurityId("RBC OVERNIGHT REPO")),
        "RBC OVERNIGHT REPO", act365, following, Period.ofDays(1), 0, false, ca, 0);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_SWAP")), "CAD_SWAP", act365, modified, semiAnnual, 0, ca, act365, modified,
        quarterly, 0, simpleNameSecurityId("CDOR 3m"), ca, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_1Y_SWAP")), "CAD_1Y_SWAP", act365, modified, annual, 0, ca, act365, modified,
        quarterly, 0, simpleNameSecurityId("CDOR 3m"), ca, true);

    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLag = 1;
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_OIS_SWAP")), "CAD_OIS_SWAP", act365, modified, annual, 0, ca, act365, modified,
        annual, 0, simpleNameSecurityId("RBC OVERNIGHT REPO"), ca, true, publicationLag);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_3M_FRA")), "CAD_3M_FRA", act365, following, quarterly, 2, ca, act365,
        following, quarterly, 2, simpleNameSecurityId("CDOR 3m"), ca, false);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_6M_FRA")), "USD_6M_FRA", act365, following, semiAnnual, 2, ca, act365,
        following, semiAnnual, 2, simpleNameSecurityId("CDOR 6m"), ca, false);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_FRA")), "CAD_FRA", act365, following, quarterly, 2, ca, act365, following,
        quarterly, 2, simpleNameSecurityId("CDOR 3m"), ca, false);

    //TODO
    //"Floating leg compounded quarterly at CDOR Flat paid semi-annually or annually for 1y"
    //Don't know how we're going to put that in
  }

  /**
   * Adds conventions for CAD government bonds
   * @param conventionMaster The convention master, not null
   */
  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CA_TREASURY_BOND_CONVENTION")), "CA_TREASURY_BOND_CONVENTION", true, true, 0, 2, 3,
        true, Tenor.TWO_YEARS);
  }


  /**
   * Adds conventions for CAD-denominated corporate bonds
   * @param conventionMaster The convention master, not null
   */
  //TODO need to get the correct convention
  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CA_CORPORATE_BOND_CONVENTION")), "CA_CORPORATE_BOND_CONVENTION", true, true, 0, 3,
        true);
  }
  
}

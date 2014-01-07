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
 * Contains information used to construct standard versions of JPY instruments.
 */
public class JPConventions {

  /**
   * Adds conventions for deposit, Libor fixings, swaps, FRAs and IR futures.
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
    final ExternalId jp = ExternalSchemes.financialRegionId("JP");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY00O/N Index"), simpleNameSecurityId("JPY LIBOR O/N")),
        "JPY LIBOR O/N", act360, following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY00S/N Index"), simpleNameSecurityId("JPY LIBOR S/N"),
        tullettPrebonSecurityId("ASLIBJPYSNL")), "JPY LIBOR S/N", act360, following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY00T/N Index"), simpleNameSecurityId("JPY LIBOR T/N")),
        "JPY LIBOR T/N", act360, following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0001W Index"), simpleNameSecurityId("JPY LIBOR 1w"),
        tullettPrebonSecurityId("ASLIBJPY1WL")), "JPY LIBOR 1w", act360, following, Period.ofDays(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0002W Index"), simpleNameSecurityId("JPY LIBOR 2w"),
        tullettPrebonSecurityId("ASLIBJPY2WL")), "JPY LIBOR 2w", act360, following, Period.ofDays(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0001M Index"), simpleNameSecurityId("JPY LIBOR 1m"),
        tullettPrebonSecurityId("ASLIBJPY01L")), "JPY LIBOR 1m", act360, following, Period.ofMonths(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0002M Index"), simpleNameSecurityId("JPY LIBOR 2m"),
        tullettPrebonSecurityId("ASLIBJPY02L")), "JPY LIBOR 2m", act360, following, Period.ofMonths(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0003M Index"), simpleNameSecurityId("JPY LIBOR 3m"),
        tullettPrebonSecurityId("ASLIBJPY03L")), "JPY LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0004M Index"), simpleNameSecurityId("JPY LIBOR 4m"),
        tullettPrebonSecurityId("ASLIBJPY04L")), "JPY LIBOR 4m", act360, following, Period.ofMonths(4), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0005M Index"), simpleNameSecurityId("JPY LIBOR 5m"),
        tullettPrebonSecurityId("ASLIBJPY05L")), "JPY LIBOR 5m", act360, following, Period.ofMonths(5), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0006M Index"), simpleNameSecurityId("JPY LIBOR 6m"),
        tullettPrebonSecurityId("ASLIBJPY06L")), "JPY LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0007M Index"), simpleNameSecurityId("JPY LIBOR 7m"),
        tullettPrebonSecurityId("ASLIBJPY07L")), "JPY LIBOR 7m", act360, following, Period.ofMonths(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0008M Index"), simpleNameSecurityId("JPY LIBOR 8m"),
        tullettPrebonSecurityId("ASLIBJPY08L")), "JPY LIBOR 8m", act360, following, Period.ofMonths(8), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0009M Index"), simpleNameSecurityId("JPY LIBOR 9m"),
        tullettPrebonSecurityId("ASLIBJPY09L")), "JPY LIBOR 9m", act360, following, Period.ofMonths(9), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0010M Index"), simpleNameSecurityId("JPY LIBOR 10m"),
        tullettPrebonSecurityId("ASLIBJPY10L")), "JPY LIBOR 10m", act360, following, Period.ofMonths(10), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0011M Index"), simpleNameSecurityId("JPY LIBOR 11m"),
        tullettPrebonSecurityId("ASLIBJPY11L")), "JPY LIBOR 11m", act360, following, Period.ofMonths(11), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0012M Index"), simpleNameSecurityId("JPY LIBOR 12m"),
        tullettPrebonSecurityId("ASLIBJPY12L")), "JPY LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, jp);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR1T Curncy"), simpleNameSecurityId("JPY DEPOSIT 1d")), "JPY DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR2T Curncy"), simpleNameSecurityId("JPY DEPOSIT 2d")), "JPY DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR3T Curncy"), simpleNameSecurityId("JPY DEPOSIT 3d")), "JPY DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR1Z Curncy"), simpleNameSecurityId("JPY DEPOSIT 1w"),
        tullettPrebonSecurityId("MNDEPJPYSPT01W")), "JPY DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR2Z Curncy"), simpleNameSecurityId("JPY DEPOSIT 2w"),
        tullettPrebonSecurityId("MNDEPJPYSPT02W")), "JPY DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR3Z Curncy"), simpleNameSecurityId("JPY DEPOSIT 3w"),
        tullettPrebonSecurityId("MNDEPJPYSPT03W")), "JPY DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRA Curncy"), simpleNameSecurityId("JPY DEPOSIT 1m"),
        tullettPrebonSecurityId("MNDEPJPYSPT01M")), "JPY DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRB Curncy"), simpleNameSecurityId("JPY DEPOSIT 2m"),
        tullettPrebonSecurityId("MNDEPJPYSPT02M")), "JPY DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRC Curncy"), simpleNameSecurityId("JPY DEPOSIT 3m"),
        tullettPrebonSecurityId("MNDEPJPYSPT03M")), "JPY DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRD Curncy"), simpleNameSecurityId("JPY DEPOSIT 4m"),
        tullettPrebonSecurityId("MNDEPJPYSPT04M")), "JPY DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRE Curncy"), simpleNameSecurityId("JPY DEPOSIT 5m"),
        tullettPrebonSecurityId("MNDEPJPYSPT05M")), "JPY DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRF Curncy"), simpleNameSecurityId("JPY DEPOSIT 6m"),
        tullettPrebonSecurityId("MNDEPJPYSPT06M")), "JPY DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRG Curncy"), simpleNameSecurityId("JPY DEPOSIT 7m"),
        tullettPrebonSecurityId("MNDEPJPYSPT07M")), "JPY DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRH Curncy"), simpleNameSecurityId("JPY DEPOSIT 8m"),
        tullettPrebonSecurityId("MNDEPJPYSPT08M")), "JPY DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRI Curncy"), simpleNameSecurityId("JPY DEPOSIT 9m"),
        tullettPrebonSecurityId("MNDEPJPYSPT09M")), "JPY DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRJ Curncy"), simpleNameSecurityId("JPY DEPOSIT 10m"),
        tullettPrebonSecurityId("MNDEPJPYSPT10M")), "JPY DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRK Curncy"), simpleNameSecurityId("JPY DEPOSIT 11m"),
        tullettPrebonSecurityId("MNDEPJPYSPT11M")), "JPY DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR1 Curncy"), simpleNameSecurityId("JPY DEPOSIT 1y"),
        tullettPrebonSecurityId("MNDEPJPYSPT12M")), "JPY DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR2 Curncy"), simpleNameSecurityId("JPY DEPOSIT 2y")), "JPY DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR3 Curncy"), simpleNameSecurityId("JPY DEPOSIT 3y")), "JPY DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR4 Curncy"), simpleNameSecurityId("JPY DEPOSIT 4y")), "JPY DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR5 Curncy"), simpleNameSecurityId("JPY DEPOSIT 5y")), "JPY DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, jp);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_SWAP")), "JPY_SWAP", act365, modified, semiAnnual, 2, jp, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("JPY LIBOR 6m"), jp, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_3M_SWAP")), "JPY_3M_SWAP", act365, modified, semiAnnual, 2, jp,
        act360, modified, quarterly, 2, simpleNameSecurityId("JPY LIBOR 3m"), jp, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_6M_SWAP")), "JPY_6M_SWAP", act365, modified, semiAnnual, 2, jp,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("JPY LIBOR 6m"), jp, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_3M_FRA")), "JPY_3M_FRA", act365, modified, semiAnnual, 2, jp,
        act360, modified, quarterly, 2, simpleNameSecurityId("JPY LIBOR 3m"), jp, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_6M_FRA")), "JPY_6M_FRA", act365, modified, semiAnnual, 2, jp,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("JPY LIBOR 6m"), jp, true);

    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLag = 0;
    // TONAR
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MUTSCALM Index"), simpleNameSecurityId("JPY TONAR")),
        "JPY TONAR", act365, following, Period.ofDays(1), 2, false, jp, publicationLag);
    // OIS - TONAR
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_OIS_SWAP")), "JPY_OIS_SWAP", act365, modified, annual, 2, jp,
        act365, modified, annual, 2, simpleNameSecurityId("JPY TONAR"), jp, true, publicationLag);

    utils
    .addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_OIS_CASH")), "JPY_OIS_CASH", act365, following, null, 2, false, null);

    //TODO check this
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_IBOR_INDEX")), "JPY_IBOR_INDEX", act360, following, 2, false);
  }

  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JP_TREASURY_BOND_CONVENTION")), "JP_TREASURY_BOND_CONVENTION", true,
        true, 0, 3, true);

  }
}

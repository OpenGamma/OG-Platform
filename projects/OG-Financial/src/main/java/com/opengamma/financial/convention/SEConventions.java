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
 * Contains information used to construct standard versions of SEK instruments.
 */
public class SEConventions {

  /**
   * Adds conventions for deposit, Libor and Stibor fixings.
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

    final ExternalId se = ExternalSchemes.financialRegionId("SE");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR1T Curncy"), simpleNameSecurityId("SEK DEPOSIT 1d")), "SEK DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR2T Curncy"), simpleNameSecurityId("SEK DEPOSIT 2d")), "SEK DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR3T Curncy"), simpleNameSecurityId("SEK DEPOSIT 3d")), "SEK DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR1Z Curncy"), simpleNameSecurityId("SEK DEPOSIT 1w")), "SEK DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR2Z Curncy"), simpleNameSecurityId("SEK DEPOSIT 2w")), "SEK DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR3Z Curncy"), simpleNameSecurityId("SEK DEPOSIT 3w")), "SEK DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRA Curncy"), simpleNameSecurityId("SEK DEPOSIT 1m")), "SEK DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRB Curncy"), simpleNameSecurityId("SEK DEPOSIT 2m")), "SEK DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRC Curncy"), simpleNameSecurityId("SEK DEPOSIT 3m")), "SEK DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRD Curncy"), simpleNameSecurityId("SEK DEPOSIT 4m")), "SEK DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRE Curncy"), simpleNameSecurityId("SEK DEPOSIT 5m")), "SEK DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRF Curncy"), simpleNameSecurityId("SEK DEPOSIT 6m")), "SEK DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRG Curncy"), simpleNameSecurityId("SEK DEPOSIT 7m")), "SEK DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRH Curncy"), simpleNameSecurityId("SEK DEPOSIT 8m")), "SEK DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRI Curncy"), simpleNameSecurityId("SEK DEPOSIT 9m")), "SEK DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRJ Curncy"), simpleNameSecurityId("SEK DEPOSIT 10m")), "SEK DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDRK Curncy"), simpleNameSecurityId("SEK DEPOSIT 11m")), "SEK DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR1 Curncy"), simpleNameSecurityId("SEK DEPOSIT 1y")), "SEK DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR2 Curncy"), simpleNameSecurityId("SEK DEPOSIT 2y")), "SEK DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR3 Curncy"), simpleNameSecurityId("SEK DEPOSIT 3y")), "SEK DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR4 Curncy"), simpleNameSecurityId("SEK DEPOSIT 4y")), "SEK DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SKDR5 Curncy"), simpleNameSecurityId("SEK DEPOSIT 5y")), "SEK DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, se);

    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK1WL"), simpleNameSecurityId("SEK STIBOR 1w")), "SEK STIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK01L"), simpleNameSecurityId("SEK STIBOR 1m")), "SEK STIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK02L"), simpleNameSecurityId("SEK STIBOR 2m")), "SEK STIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK03L"), simpleNameSecurityId("SEK STIBOR 3m")), "SEK STIBOR 3m", act360,
        following, Period.ofMonths(3), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK06L"), simpleNameSecurityId("SEK STIBOR 6m")), "SEK STIBOR 6m", act360,
        following, Period.ofMonths(6), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK09L"), simpleNameSecurityId("SEK STIBOR 9m")), "SEK STIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSDK12L"), simpleNameSecurityId("SEK STIBOR 1y")), "SEK STIBOR 1y", act360,
        following, Period.ofMonths(12), 2, false, se);

    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK1WL"), simpleNameSecurityId("SEK LIBOR 1w")), "SEK LIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK2WL"), simpleNameSecurityId("SEK LIBOR 2w")), "SEK LIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK01L"), simpleNameSecurityId("SEK LIBOR 1m")), "SEK LIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK02L"), simpleNameSecurityId("SEK LIBOR 2m")), "SEK LIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK03L"), simpleNameSecurityId("SEK LIBOR 3m")), "SEK LIBOR 3m", act360,
        following, Period.ofMonths(3), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK04L"), simpleNameSecurityId("SEK LIBOR 4m")), "SEK LIBOR 4m", act360,
        following, Period.ofMonths(4), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK05L"), simpleNameSecurityId("SEK LIBOR 5m")), "SEK LIBOR 5m", act360,
        following, Period.ofMonths(5), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK06L"), simpleNameSecurityId("SEK LIBOR 6m")), "SEK LIBOR 6m", act360,
        following, Period.ofMonths(6), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK07L"), simpleNameSecurityId("SEK LIBOR 7m")), "SEK LIBOR 7m", act360,
        following, Period.ofMonths(7), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK08L"), simpleNameSecurityId("SEK LIBOR 8m")), "SEK LIBOR 8m", act360,
        following, Period.ofMonths(8), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK09L"), simpleNameSecurityId("SEK LIBOR 9m")), "SEK LIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK10L"), simpleNameSecurityId("SEK LIBOR 10m")), "SEK LIBOR 10m", act360,
        following, Period.ofMonths(10), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK11L"), simpleNameSecurityId("SEK LIBOR 11m")), "SEK LIBOR 11m", act360,
        following, Period.ofMonths(11), 2, false, se);
    utils.addConventionBundle(ExternalIdBundle.of(tullettPrebonSecurityId("ASLIBSEK12L"), simpleNameSecurityId("SEK LIBOR 1y")), "SEK LIBOR 1y", act360,
        following, Period.ofMonths(12), 2, false, se);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("SEK_SWAP")), "SEK_SWAP", thirty360, modified, annual, 1, se, act360,
        modified, quarterly, 1, simpleNameSecurityId("SEK DEPOSIT 3m"), se, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("SEK_3M_SWAP")), "SEK_3M_SWAP", thirty360, modified, annual, 2, se,
        act360, modified, quarterly, 2, simpleNameSecurityId("SEK DEPOSIT 3m"), se, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("SEK_6M_SWAP")), "SEK_6M_SWAP", thirty360, modified, annual, 2, se,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("SEK DEPOSIT 6m"), se, true);

  }
}

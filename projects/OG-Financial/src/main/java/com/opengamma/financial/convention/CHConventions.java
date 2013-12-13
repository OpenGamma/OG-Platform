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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard version of CHF instruments
 */
public class CHConventions {

  /**
   * Adds conventions for deposit, Libor, swaps and FRAs
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final ExternalId ch = ExternalSchemes.financialRegionId("CH");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    //TODO check that it's actually libor that we need
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF00O/N Index"), simpleNameSecurityId("CHF LIBOR O/N")), "CHF LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF00S/N Index"), simpleNameSecurityId("CHF LIBOR S/N"),
        tullettPrebonSecurityId("ASLIBCHFSNL")), "CHF LIBOR S/N", act360,
        following, Period.ofDays(1), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF00T/N Index"), simpleNameSecurityId("CHF LIBOR T/N")), "CHF LIBOR T/N", act360,
        following, Period.ofDays(1), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0001W Index"), simpleNameSecurityId("CHF LIBOR 1w"),
        tullettPrebonSecurityId("ASLIBCHF1WL")), "CHF LIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0002W Index"), simpleNameSecurityId("CHF LIBOR 2w"),
        tullettPrebonSecurityId("ASLIBCHF2WL")), "CHF LIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0001M Index"), simpleNameSecurityId("CHF LIBOR 1m"),
        tullettPrebonSecurityId("ASLIBCHF01L")), "CHF LIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0002M Index"), simpleNameSecurityId("CHF LIBOR 2m"),
        tullettPrebonSecurityId("ASLIBCHF02L")), "CHF LIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0003M Index"), simpleNameSecurityId("CHF LIBOR 3m"),
        tullettPrebonSecurityId("ASLIBCHF03L")), "CHF LIBOR 3m", act360,
        following, Period.ofMonths(3), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0004M Index"), simpleNameSecurityId("CHF LIBOR 4m"),
        tullettPrebonSecurityId("ASLIBCHF04L")), "CHF LIBOR 4m", act360,
        following, Period.ofMonths(4), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0005M Index"), simpleNameSecurityId("CHF LIBOR 5m"),
        tullettPrebonSecurityId("ASLIBCHF05L")), "CHF LIBOR 5m", act360,
        following, Period.ofMonths(5), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0006M Index"), simpleNameSecurityId("CHF LIBOR 6m"),
        tullettPrebonSecurityId("ASLIBCHF06L")), "CHF LIBOR 6m", act360,
        following, Period.ofMonths(6), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0007M Index"), simpleNameSecurityId("CHF LIBOR 7m"),
        tullettPrebonSecurityId("ASLIBCHF07L")), "CHF LIBOR 7m", act360,
        following, Period.ofMonths(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0008M Index"), simpleNameSecurityId("CHF LIBOR 8m"),
        tullettPrebonSecurityId("ASLIBCHF08L")), "CHF LIBOR 8m", act360,
        following, Period.ofMonths(8), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0009M Index"), simpleNameSecurityId("CHF LIBOR 9m"),
        tullettPrebonSecurityId("ASLIBCHF09L")), "CHF LIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0010M Index"), simpleNameSecurityId("CHF LIBOR 10m"),
        tullettPrebonSecurityId("ASLIBCHF10L")), "CHF LIBOR 10m", act360,
        following, Period.ofMonths(10), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0011M Index"), simpleNameSecurityId("CHF LIBOR 11m"),
        tullettPrebonSecurityId("ASLIBCHF11L")), "CHF LIBOR 11m", act360,
        following, Period.ofMonths(11), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SF0012M Index"), simpleNameSecurityId("CHF LIBOR 12m"),
        tullettPrebonSecurityId("ASLIBCHF12L")), "CHF LIBOR 12m", act360,
        following, Period.ofMonths(12), 2, false, ch);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR1T Curncy"), simpleNameSecurityId("CHF DEPOSIT 1d")), "CHF DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR2T Curncy"), simpleNameSecurityId("CHF DEPOSIT 2d")), "CHF DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR3T Curncy"), simpleNameSecurityId("CHF DEPOSIT 3d")), "CHF DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR1Z Curncy"), simpleNameSecurityId("CHF DEPOSIT 1w"),
        tullettPrebonSecurityId("MNDEPCHFSPT01W")), "CHF DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR2Z Curncy"), simpleNameSecurityId("CHF DEPOSIT 2w"),
        tullettPrebonSecurityId("MNDEPCHFSPT02W")), "CHF DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR3Z Curncy"), simpleNameSecurityId("CHF DEPOSIT 3w"),
        tullettPrebonSecurityId("MNDEPCHFSPT03W")), "CHF DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRA Curncy"), simpleNameSecurityId("CHF DEPOSIT 1m"),
        tullettPrebonSecurityId("MNDEPCHFSPT01M")), "CHF DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRB Curncy"), simpleNameSecurityId("CHF DEPOSIT 2m"),
        tullettPrebonSecurityId("MNDEPCHFSPT02M")), "CHF DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRC Curncy"), simpleNameSecurityId("CHF DEPOSIT 3m"),
        tullettPrebonSecurityId("MNDEPCHFSPT03M")), "CHF DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRD Curncy"), simpleNameSecurityId("CHF DEPOSIT 4m"),
        tullettPrebonSecurityId("MNDEPCHFSPT04M")), "CHF DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRE Curncy"), simpleNameSecurityId("CHF DEPOSIT 5m"),
        tullettPrebonSecurityId("MNDEPCHFSPT05M")), "CHF DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRF Curncy"), simpleNameSecurityId("CHF DEPOSIT 6m"),
        tullettPrebonSecurityId("MNDEPCHFSPT06M")), "CHF DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRG Curncy"), simpleNameSecurityId("CHF DEPOSIT 7m"),
        tullettPrebonSecurityId("MNDEPCHFSPT07M")), "CHF DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRH Curncy"), simpleNameSecurityId("CHF DEPOSIT 8m"),
        tullettPrebonSecurityId("MNDEPCHFSPT08M")), "CHF DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRI Curncy"), simpleNameSecurityId("CHF DEPOSIT 9m"),
        tullettPrebonSecurityId("MNDEPCHFSPT09M")), "CHF DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRJ Curncy"), simpleNameSecurityId("CHF DEPOSIT 10m"),
        tullettPrebonSecurityId("MNDEPCHFSPT10M")), "CHF DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDRK Curncy"), simpleNameSecurityId("CHF DEPOSIT 11m"),
        tullettPrebonSecurityId("MNDEPCHFSPT11M")), "CHF DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR1 Curncy"), simpleNameSecurityId("CHF DEPOSIT 1y"),
        tullettPrebonSecurityId("MNDEPCHFSPT12M")), "CHF DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR2 Curncy"), simpleNameSecurityId("CHF DEPOSIT 2y")), "CHF DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR3 Curncy"), simpleNameSecurityId("CHF DEPOSIT 3y")), "CHF DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR4 Curncy"), simpleNameSecurityId("CHF DEPOSIT 4y")), "CHF DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SFDR5 Curncy"), simpleNameSecurityId("CHF DEPOSIT 5y")), "CHF DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, ch);

    //TODO check reference rate
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_SWAP")), "CHF_SWAP", thirty360, modified, annual, 2, ch, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_3M_SWAP")), "CHF_3M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, quarterly, 2, simpleNameSecurityId("CHF LIBOR 3m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_6M_SWAP")), "CHF_6M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_3M_FRA")), "CHF_3M_FRA", thirty360, modified, annual, 2, ch, act360,
        modified, quarterly, 2, simpleNameSecurityId("CHF LIBOR 3m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_6M_FRA")), "CHF_6M_FRA", thirty360, modified, annual, 2, ch, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_CHF_P3M")), IndexType.Libor + "_CHF_P3M", thirty360, modified,
        null, 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_CHF_P6M")), IndexType.Libor + "_CHF_P6M", thirty360, modified,
        null, 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Euribor + "_CHF_P3M")), IndexType.Euribor + "_CHF_P3M", thirty360,
        modified,
        null, 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Euribor + "_CHF_P6M")), IndexType.Euribor + "_CHF_P6M", thirty360,
        modified,
        null, 2, false, ch);

    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLagON = 0; // TODO CASE PublicationLag CHF - Confirm 0
    // CHF Overnight Index
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TOISTOIS Index"), simpleNameSecurityId("CHF TOISTOIS")), "CHF TOISTOIS", act360,
        following, Period.ofDays(1), 2, false, ch, publicationLagON);
    // OIS
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_OIS_SWAP")), "CHF_OIS_SWAP", act360, modified, annual, 2, ch,
        act360, modified, annual, 2, simpleNameSecurityId("CHF TOISTOIS"), ch, true, publicationLagON);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_IBOR_INDEX")), "CHF_IBOR_INDEX", act360, following, 2, false);
  }

  /**
   * Adds conventions for CHF government bonds
   * @param conventionMaster The convention master, not null
   */
  //TODO all of the conventions named treasury need to be changed
  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CH_TREASURY_BOND_CONVENTION")), "CH_TREASURY_BOND_CONVENTION", true,
        true, 0, 3, true);
  }

  /**
   * Adds conventions for CHF-denominated corporate bonds
   * @param conventionMaster The convention master, not null
   */
  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CH_CORPORATE_BOND_CONVENTION")), "CH_CORPORATE_BOND_CONVENTION", true,
        true, 0, 3, true);
  }

}

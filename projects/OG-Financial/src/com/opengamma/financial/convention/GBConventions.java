/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Instrument 
 * 
 */
public class GBConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId gb = RegionUtils.financialRegionId("GB");

    //TODO looked at BSYM and the codes seem right but need to check
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR O/N")), "GBP LIBOR O/N", act365,
        following, Period.ofDays(1), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR T/N")), "GBP LIBOR T/N", act365,
        following, Period.ofDays(1), 1, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 1w")), "GBP LIBOR 1w", act365,
        following, Period.ofDays(7), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 2w")), "GBP LIBOR 2w", act365,
        following, Period.ofDays(14), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 1m")), "GBP LIBOR 1m", act365,
        modified, Period.ofMonths(1), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 2m")), "GBP LIBOR 2m", act365,
        modified, Period.ofMonths(2), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 3m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPLIBORP3M")), "GBP LIBOR 3m", act365, modified, Period.ofMonths(3), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 4m")), "GBP LIBOR 4m", act365,
        modified, Period.ofMonths(4), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 5m")), "GBP LIBOR 5m", act365,
        modified, Period.ofMonths(5), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPLIBORP6M")), "GBP LIBOR 6m", act365, modified, Period.ofMonths(6), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 7m")), "GBP LIBOR 7m", act365,
        modified, Period.ofMonths(7), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 8m")), "GBP LIBOR 8m", act365,
        modified, Period.ofMonths(8), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 9m")), "GBP LIBOR 9m", act365,
        modified, Period.ofMonths(9), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 10m")), "GBP LIBOR 10m", act365,
        modified, Period.ofMonths(10), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 11m")), "GBP LIBOR 11m", act365,
        modified, Period.ofMonths(11), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BP0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 12m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPLIBORP12M")), "GBP LIBOR 12m", act365, modified, Period.ofMonths(12), 0, false, gb);

    //TODO need to check that these are right for deposit rates
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 1d")), "GBP DEPOSIT 1d", act365,
        following, Period.ofDays(1), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 2d")), "GBP DEPOSIT 2d", act365,
        following, Period.ofDays(1), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 3d")), "GBP DEPOSIT 3d", act365,
        following, Period.ofDays(1), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 1w")), "GBP DEPOSIT 1w", act365,
        following, Period.ofDays(7), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 2w")), "GBP DEPOSIT 2w", act365,
        following, Period.ofDays(14), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 3w")), "GBP DEPOSIT 3w", act365,
        following, Period.ofDays(21), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 1m")), "GBP DEPOSIT 1m", act365,
        following, Period.ofMonths(1), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 2m")), "GBP DEPOSIT 2m", act365,
        following, Period.ofMonths(2), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 3m")), "GBP DEPOSIT 3m", act365,
        following, Period.ofMonths(3), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 4m")), "GBP DEPOSIT 4m", act365,
        following, Period.ofMonths(4), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 5m")), "GBP DEPOSIT 5m", act365,
        following, Period.ofMonths(5), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 6m")), "GBP DEPOSIT 6m", act365,
        following, Period.ofMonths(6), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 7m")), "GBP DEPOSIT 7m", act365,
        following, Period.ofMonths(7), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 8m")), "GBP DEPOSIT 8m", act365,
        following, Period.ofMonths(8), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 9m")), "GBP DEPOSIT 9m", act365,
        following, Period.ofMonths(9), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 10m")), "GBP DEPOSIT 10m", act365,
        following, Period.ofMonths(10), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 11m")), "GBP DEPOSIT 11m", act365,
        following, Period.ofMonths(11), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 1y")), "GBP DEPOSIT 1y", act365,
        following, Period.ofYears(1), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 2y")), "GBP DEPOSIT 2y", act365,
        following, Period.ofYears(2), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 3y")), "GBP DEPOSIT 3y", act365,
        following, Period.ofYears(3), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 4y")), "GBP DEPOSIT 4y", act365,
        following, Period.ofYears(4), 2, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("BPDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP DEPOSIT 5y")), "GBP DEPOSIT 5y", act365,
        following, Period.ofYears(5), 2, false, gb);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_SWAP")), "GBP_SWAP", act365, modified, semiAnnual, 0, gb, act365,
        modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 6m"), gb, true);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_3M_SWAP")), "GBP_3M_SWAP", act365, modified, annual, 0, gb, act365,
        modified, quarterly, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 3m"), gb, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_6M_SWAP")), "GBP_6M_SWAP", act365, modified, semiAnnual, 0, gb,
        act365, modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 6m"), gb, true);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_3M_FRA")), "GBP_3M_FRA", act365, modified, annual, 0, gb, act365,
        modified, quarterly, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 3m"), gb, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_6M_FRA")), "GBP_6M_FRA", act365, modified, semiAnnual, 0, gb,
        act365, modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 6m"), gb, true);

    //    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_1Y_SWAP")), "GBP_1Y_SWAP", act365, modified, quarterly, 0, gb,
    //        act365, modified, quarterly, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP LIBOR 3m"), gb, true);

    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLagON = 0;
    // SONIA
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SONIO/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP SONIO/N")), "GBP SONIO/N", act365,
        following, Period.ofDays(1), 0, false, gb, publicationLagON);
    // OIS - SONIA
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_OIS_SWAP")), "GBP_OIS_SWAP", act365, modified, annual, 2, gb,
        act365, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP SONIO/N"), gb, true, publicationLagON);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_OIS_CASH")), "GBP_OIS_CASH", act365, following, null, 0, false, gb,
        publicationLagON);

    //TODO sort out the swap names so that they are consistent
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_IBOR_INDEX")), "GBP_IBOR_INDEX", act365, modified, 0, false);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP1D"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP1D")),
        "GBPCASHP1D", act365, following, Period.ofDays(1), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP1M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP1M")),
        "GBPCASHP1M", act365, modified, Period.ofMonths(1), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP2M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP2M")),
        "GBPCASHP2M", act365, modified, Period.ofMonths(2), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP3M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP3M")),
        "GBPCASHP3M", act365, modified, Period.ofMonths(3), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP4M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP4M")),
        "GBPCASHP4M", act365, modified, Period.ofMonths(4), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP5M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP5M")),
        "GBPCASHP5M", act365, modified, Period.ofMonths(5), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP6M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP6M")),
        "GBPCASHP6M", act365, modified, Period.ofMonths(6), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP7M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP7M")),
        "GBPCASHP7M", act365, modified, Period.ofMonths(7), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP8M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP8M")),
        "GBPCASHP8M", act365, modified, Period.ofMonths(8), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP9M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP9M")),
        "GBPCASHP9M", act365, modified, Period.ofMonths(9), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP10M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP10M")),
        "GBPCASHP10M", act365, modified, Period.ofMonths(10), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP11M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP11M")),
        "GBPCASHP11M", act365, modified, Period.ofMonths(11), 0, false, gb);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBPCASHP12M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "GBPCASHP12M")),
        "GBPCASHP12M", act365, modified, Period.ofMonths(12), 0, false, gb);
  }

  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GB_TREASURY_BOND_CONVENTION")), "GB_TREASURY_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GB_CORPORATE_BOND_CONVENTION")), "GB_CORPORATE_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  public static void addBondFutureConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION")),
        "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION", true, true, 7, 0, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        SimpleYieldConvention.MONEY_MARKET);
  }
}

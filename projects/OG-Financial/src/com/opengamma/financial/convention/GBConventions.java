/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.IndexType;
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

    final ExternalId gb = ExternalSchemes.financialRegionId("GB");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    //TODO looked at BSYM and the codes seem right but need to check
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP00O/N Index"), simpleNameSecurityId("GBP LIBOR O/N")), 
        "GBP LIBOR O/N", act365, following, Period.ofDays(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP00T/N Index"), simpleNameSecurityId("GBP LIBOR T/N")), 
        "GBP LIBOR T/N", act365, following, Period.ofDays(1), 1, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0001W Index"), simpleNameSecurityId("GBP LIBOR 1w")), 
        "GBP LIBOR 1w", act365, following, Period.ofDays(7), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0002W Index"), simpleNameSecurityId("GBP LIBOR 2w")), 
        "GBP LIBOR 2w", act365, following, Period.ofDays(14), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0001M Index"), simpleNameSecurityId("GBP LIBOR 1m")), 
        "GBP LIBOR 1m", act365, modified, Period.ofMonths(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0002M Index"), simpleNameSecurityId("GBP LIBOR 2m")), 
        "GBP LIBOR 2m", act365, modified, Period.ofMonths(2), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0003M Index"), simpleNameSecurityId("GBP LIBOR 3m")), 
        "GBP LIBOR 3m", act365, modified, Period.ofMonths(3), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0004M Index"), simpleNameSecurityId("GBP LIBOR 4m")), 
        "GBP LIBOR 4m", act365, modified, Period.ofMonths(4), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0005M Index"), simpleNameSecurityId("GBP LIBOR 5m")), 
        "GBP LIBOR 5m", act365, modified, Period.ofMonths(5), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0006M Index"), simpleNameSecurityId("GBP LIBOR 6m")), 
        "GBP LIBOR 6m", act365, modified, Period.ofMonths(6), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0007M Index"), simpleNameSecurityId("GBP LIBOR 7m")), 
        "GBP LIBOR 7m", act365, modified, Period.ofMonths(7), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0008M Index"), simpleNameSecurityId("GBP LIBOR 8m")), 
        "GBP LIBOR 8m", act365, modified, Period.ofMonths(8), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0009M Index"), simpleNameSecurityId("GBP LIBOR 9m")), 
        "GBP LIBOR 9m", act365, modified, Period.ofMonths(9), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0010M Index"), simpleNameSecurityId("GBP LIBOR 10m")), 
        "GBP LIBOR 10m", act365, modified, Period.ofMonths(10), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0011M Index"), simpleNameSecurityId("GBP LIBOR 11m")), 
        "GBP LIBOR 11m", act365, modified, Period.ofMonths(11), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BP0012M Index"), simpleNameSecurityId("GBP LIBOR 12m")), 
        "GBP LIBOR 12m", act365, modified, Period.ofMonths(12), 0, false, gb);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR1T Curncy"), simpleNameSecurityId("GBP DEPOSIT 1d")), "GBP DEPOSIT 1d", act365,
        following, Period.ofDays(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR2T Curncy"), simpleNameSecurityId("GBP DEPOSIT 2d")), "GBP DEPOSIT 2d", act365,
        following, Period.ofDays(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR3T Curncy"), simpleNameSecurityId("GBP DEPOSIT 3d")), "GBP DEPOSIT 3d", act365,
        following, Period.ofDays(1), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR1Z Curncy"), simpleNameSecurityId("GBP DEPOSIT 1w")), "GBP DEPOSIT 1w", act365,
        following, Period.ofDays(7), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR2Z Curncy"), simpleNameSecurityId("GBP DEPOSIT 2w")), "GBP DEPOSIT 2w", act365,
        following, Period.ofDays(14), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR3Z Curncy"), simpleNameSecurityId("GBP DEPOSIT 3w")), "GBP DEPOSIT 3w", act365,
        following, Period.ofDays(21), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRA Curncy"), simpleNameSecurityId("GBP DEPOSIT 1m")), "GBP DEPOSIT 1m", act365,
        following, Period.ofMonths(1), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRB Curncy"), simpleNameSecurityId("GBP DEPOSIT 2m")), "GBP DEPOSIT 2m", act365,
        following, Period.ofMonths(2), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRC Curncy"), simpleNameSecurityId("GBP DEPOSIT 3m")), "GBP DEPOSIT 3m", act365,
        following, Period.ofMonths(3), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRD Curncy"), simpleNameSecurityId("GBP DEPOSIT 4m")), "GBP DEPOSIT 4m", act365,
        following, Period.ofMonths(4), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRE Curncy"), simpleNameSecurityId("GBP DEPOSIT 5m")), "GBP DEPOSIT 5m", act365,
        following, Period.ofMonths(5), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRF Curncy"), simpleNameSecurityId("GBP DEPOSIT 6m")), "GBP DEPOSIT 6m", act365,
        following, Period.ofMonths(6), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRG Curncy"), simpleNameSecurityId("GBP DEPOSIT 7m")), "GBP DEPOSIT 7m", act365,
        following, Period.ofMonths(7), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRH Curncy"), simpleNameSecurityId("GBP DEPOSIT 8m")), "GBP DEPOSIT 8m", act365,
        following, Period.ofMonths(8), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRI Curncy"), simpleNameSecurityId("GBP DEPOSIT 9m")), "GBP DEPOSIT 9m", act365,
        following, Period.ofMonths(9), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRJ Curncy"), simpleNameSecurityId("GBP DEPOSIT 10m")), "GBP DEPOSIT 10m", act365,
        following, Period.ofMonths(10), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDRK Curncy"), simpleNameSecurityId("GBP DEPOSIT 11m")), "GBP DEPOSIT 11m", act365,
        following, Period.ofMonths(11), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR1 Curncy"), simpleNameSecurityId("GBP DEPOSIT 1y")), "GBP DEPOSIT 1y", act365,
        following, Period.ofYears(1), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR2 Curncy"), simpleNameSecurityId("GBP DEPOSIT 2y")), "GBP DEPOSIT 2y", act365,
        following, Period.ofYears(2), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR3 Curncy"), simpleNameSecurityId("GBP DEPOSIT 3y")), "GBP DEPOSIT 3y", act365,
        following, Period.ofYears(3), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR4 Curncy"), simpleNameSecurityId("GBP DEPOSIT 4y")), "GBP DEPOSIT 4y", act365,
        following, Period.ofYears(4), 2, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BPDR5 Curncy"), simpleNameSecurityId("GBP DEPOSIT 5y")), "GBP DEPOSIT 5y", act365,
        following, Period.ofYears(5), 2, false, gb);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_SWAP")), "GBP_SWAP", act365, modified, semiAnnual, 0, gb, act365,
        modified, semiAnnual, 0, simpleNameSecurityId("GBP LIBOR 6m"), gb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_3M_SWAP")), "GBP_3M_SWAP", act365, modified, annual, 0, gb, act365,
        modified, quarterly, 0, simpleNameSecurityId("GBP LIBOR 3m"), gb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_6M_SWAP")), "GBP_6M_SWAP", act365, modified, semiAnnual, 0, gb,
        act365, modified, semiAnnual, 0, simpleNameSecurityId("GBP LIBOR 6m"), gb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_3M_FRA")), "GBP_3M_FRA", act365, modified, annual, 0, gb, act365,
        modified, quarterly, 0, simpleNameSecurityId("GBP LIBOR 3m"), gb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_6M_FRA")), "GBP_6M_FRA", act365, modified, semiAnnual, 0, gb,
        act365, modified, semiAnnual, 0, simpleNameSecurityId("GBP LIBOR 6m"), gb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_P3M")), IndexType.Libor + "_P3M", act365, modified,
        null, 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_P6M")), IndexType.Libor + "_P6M", act365, modified,
        null, 0, false, gb);

    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLagON = 0;
    // SONIA
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SONIO/N Index"), simpleNameSecurityId("GBP SONIO/N")), "GBP SONIO/N", act365,
        following, Period.ofDays(1), 0, false, gb, publicationLagON);
    // OIS - SONIA
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_OIS_SWAP")), "GBP_OIS_SWAP", act365, modified, annual, 2, gb,
        act365, modified, annual, 2, simpleNameSecurityId("GBP SONIO/N"), gb, true, publicationLagON);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_OIS_CASH")), "GBP_OIS_CASH", act365, following, null, 0, false, gb,
        publicationLagON);

    //TODO sort out the swap names so that they are consistent
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_IBOR_INDEX")), "GBP_IBOR_INDEX", act365, modified, 0, false);
  }

  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GB_TREASURY_BOND_CONVENTION")), "GB_TREASURY_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GB_CORPORATE_BOND_CONVENTION")), "GB_CORPORATE_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  public static void addBondFutureConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_BOND_FUTURE_DELIVERABLE_CONVENTION")),
        "GBP_BOND_FUTURE_DELIVERABLE_CONVENTION", true, true, 7, 0, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        SimpleYieldConvention.MONEY_MARKET);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

/**
 * 
 */
public class CAConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId ca = ExternalSchemes.financialRegionId("CA");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    //TODO looked at BSYM and the codes seem right but need to check
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD00O/N Index"), simpleNameSecurityId("CAD LIBOR O/N")),
        "CAD LIBOR O/N", act360, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD00S/N Index"), simpleNameSecurityId("CAD LIBOR S/N")),
        "CAD LIBOR S/N", act360, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD00T/N Index"), simpleNameSecurityId("CAD LIBOR T/N")),
        "CAD LIBOR T/N", act360, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0001W Index"), simpleNameSecurityId("CAD LIBOR 1w")),
        "CAD LIBOR 1w", act360, following, Period.ofDays(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0002W Index"), simpleNameSecurityId("CAD LIBOR 2w")),
        "CAD LIBOR 2w", act360, following, Period.ofDays(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0001M Index"), simpleNameSecurityId("CAD LIBOR 1m")),
        "CAD LIBOR 1m", act360, following, Period.ofMonths(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0002M Index"), simpleNameSecurityId("CAD LIBOR 2m")),
        "CAD LIBOR 2m", act360, following, Period.ofMonths(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0003M Index"), simpleNameSecurityId("CAD LIBOR 3m")), 
        "CAD LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0004M Index"), simpleNameSecurityId("CAD LIBOR 4m")),
        "CAD LIBOR 4m", act360, following, Period.ofMonths(4), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0005M Index"), simpleNameSecurityId("CAD LIBOR 5m")),
        "CAD LIBOR 5m", act360, following, Period.ofMonths(5), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0006M Index"), simpleNameSecurityId("CAD LIBOR 6m")), 
        "CAD LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0007M Index"), simpleNameSecurityId("CAD LIBOR 7m")),
        "CAD LIBOR 7m", act360, following, Period.ofMonths(7), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0008M Index"), simpleNameSecurityId("CAD LIBOR 8m")),
        "CAD LIBOR 8m", act360, following, Period.ofMonths(8), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0009M Index"), simpleNameSecurityId("CAD LIBOR 9m")),
        "CAD LIBOR 9m", act360, following, Period.ofMonths(9), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0010M Index"), simpleNameSecurityId("CAD LIBOR 10m")),
        "CAD LIBOR 10m", act360, following, Period.ofMonths(10), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0011M Index"), simpleNameSecurityId("CAD LIBOR 11m")),
        "CAD LIBOR 11m", act360, following, Period.ofMonths(11), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CD0012M Index"), simpleNameSecurityId("CAD LIBOR 12m")), 
        "CAD LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, ca);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR1T Curncy"), simpleNameSecurityId("CAD DEPOSIT 1d")),
        "CAD DEPOSIT 1d", act365, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR2T Curncy"), simpleNameSecurityId("CAD DEPOSIT 2d")),
        "CAD DEPOSIT 2d", act365, following, Period.ofDays(1), 0, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR3T Curncy"), simpleNameSecurityId("CAD DEPOSIT 3d")),
        "CAD DEPOSIT 3d", act365, following, Period.ofDays(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR1Z Curncy"), simpleNameSecurityId("CAD DEPOSIT 1w")),
        "CAD DEPOSIT 1w", act365, following, Period.ofDays(7), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR2Z Curncy"), simpleNameSecurityId("CAD DEPOSIT 2w")),
        "CAD DEPOSIT 2w", act365, following, Period.ofDays(14), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR3Z Curncy"), simpleNameSecurityId("CAD DEPOSIT 3w")),
        "CAD DEPOSIT 3w", act365, following, Period.ofDays(21), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRA Curncy"), simpleNameSecurityId("CAD DEPOSIT 1m")),
        "CAD DEPOSIT 1m", act365, following, Period.ofMonths(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRB Curncy"), simpleNameSecurityId("CAD DEPOSIT 2m")),
        "CAD DEPOSIT 2m", act365, following, Period.ofMonths(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRC Curncy"), simpleNameSecurityId("CAD DEPOSIT 3m")),
        "CAD DEPOSIT 3m", act365, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRD Curncy"), simpleNameSecurityId("CAD DEPOSIT 4m")),
        "CAD DEPOSIT 4m", act365, following, Period.ofMonths(4), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRE Curncy"), simpleNameSecurityId("CAD DEPOSIT 5m")),
        "CAD DEPOSIT 5m", act365, following, Period.ofMonths(5), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRF Curncy"), simpleNameSecurityId("CAD DEPOSIT 6m")),
        "CAD DEPOSIT 6m", act365, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRG Curncy"), simpleNameSecurityId("CAD DEPOSIT 7m")),
        "CAD DEPOSIT 7m", act365, following, Period.ofMonths(7), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRH Curncy"), simpleNameSecurityId("CAD DEPOSIT 8m")),
        "CAD DEPOSIT 8m", act365, following, Period.ofMonths(8), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRI Curncy"), simpleNameSecurityId("CAD DEPOSIT 9m")),
        "CAD DEPOSIT 9m", act365, following, Period.ofMonths(9), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRJ Curncy"), simpleNameSecurityId("CAD DEPOSIT 10m")),
        "CAD DEPOSIT 10m", act365, following, Period.ofMonths(10), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDRK Curncy"), simpleNameSecurityId("CAD DEPOSIT 11m")),
        "CAD DEPOSIT 11m", act365, following, Period.ofMonths(11), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR1 Curncy"), simpleNameSecurityId("CAD DEPOSIT 1y")),
        "CAD DEPOSIT 1y", act365, following, Period.ofYears(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR2 Curncy"), simpleNameSecurityId("CAD DEPOSIT 2y")),
        "CAD DEPOSIT 2y", act365, following, Period.ofYears(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR3 Curncy"), simpleNameSecurityId("CAD DEPOSIT 3y")),
        "CAD DEPOSIT 3y", act365, following, Period.ofYears(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR4 Curncy"), simpleNameSecurityId("CAD DEPOSIT 4y")),
        "CAD DEPOSIT 4y", act365, following, Period.ofYears(4), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDDR5 Curncy"), simpleNameSecurityId("CAD DEPOSIT 5y")),
        "CAD DEPOSIT 5y", act365, following, Period.ofYears(5), 2, false, ca);
    //TODO check daycount
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR01 Index"), bloombergTickerSecurityId("CDOR01 RBC Index"),
            simpleNameSecurityId("CDOR 1m")), "CDOR 1m", act365, following, Period.ofMonths(1), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR02 Index"), bloombergTickerSecurityId("CDOR02 RBC Index"),
            simpleNameSecurityId("CDOR 2m")), "CDOR 2m", act365, following, Period.ofMonths(2), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR03 Index"), bloombergTickerSecurityId("CDOR03 RBC Index"),
            simpleNameSecurityId("CDOR 3m")), "CDOR 3m", act365, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR06 Index"), bloombergTickerSecurityId("CDOR06 RBC Index"),
            simpleNameSecurityId("CDOR 6m")), "CDOR 6m", act365, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CDOR12 Index"), bloombergTickerSecurityId("CDOR12 RBC Index"),
            simpleNameSecurityId("CDOR 12m")), "CDOR 12m", act365, following, Period.ofMonths(12), 2, false, ca);
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

    // TODO check the following details - copied from old CAD_FRA
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_3M_FRA")), "CAD_3M_FRA", act365, following, quarterly, 2, ca, act365,
        following, quarterly, 2, simpleNameSecurityId("CDOR 3m"), ca, false);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_6M_FRA")), "USD_6M_FRA", act365, following, semiAnnual, 2, ca, act365,
        following, semiAnnual, 2, simpleNameSecurityId("CDOR 6m"), ca, false);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_FRA")), "CAD_FRA", act365, following, quarterly, 2, ca, act365, following,
        quarterly, 2, simpleNameSecurityId("CDOR 3m"), ca, false);

    //TODO according to my information:
    //"Floating leg compounded quarterly at CDOR Flat paid semi-annually or annually for 1y"
    //Don't know how we're going to put that in
  }

  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CA_TREASURY_BOND_CONVENTION")), "CA_TREASURY_BOND_CONVENTION", true, true, 0, 2, 3,
        true, Tenor.TWO_YEARS);
  }
}

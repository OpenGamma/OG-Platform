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
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class JPConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId jp = ExternalSchemes.financialRegionId("JP");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    
    //TODO looked at BSYM and the codes seem right but need to check
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY00O/N Index"), simpleNameSecurityId("JPY LIBOR O/N")), "JPY LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY00S/N Index"), simpleNameSecurityId("JPY LIBOR S/N")), "JPY LIBOR S/N", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY00T/N Index"), simpleNameSecurityId("JPY LIBOR T/N")), "JPY LIBOR T/N", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0001W Index"), simpleNameSecurityId("JPY LIBOR 1w")), "JPY LIBOR 1w", act360,
        following, Period.ofDays(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0002W Index"), simpleNameSecurityId("JPY LIBOR 2w")), "JPY LIBOR 2w", act360,
        following, Period.ofDays(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0001M Index"), simpleNameSecurityId("JPY LIBOR 1m")), "JPY LIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0002M Index"), simpleNameSecurityId("JPY LIBOR 2m")), "JPY LIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0003M Index"), simpleNameSecurityId("JPY LIBOR 3m")), "JPY LIBOR 3m", act360, 
        following, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0004M Index"), simpleNameSecurityId("JPY LIBOR 4m")), "JPY LIBOR 4m", act360,
        following, Period.ofMonths(4), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0005M Index"), simpleNameSecurityId("JPY LIBOR 5m")), "JPY LIBOR 5m", act360,
        following, Period.ofMonths(5), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0006M Index"), simpleNameSecurityId("JPY LIBOR 6m")), "JPY LIBOR 6m", act360, 
        following, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0007M Index"), simpleNameSecurityId("JPY LIBOR 7m")), "JPY LIBOR 7m", act360,
        following, Period.ofMonths(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0008M Index"), simpleNameSecurityId("JPY LIBOR 8m")), "JPY LIBOR 8m", act360,
        following, Period.ofMonths(8), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0009M Index"), simpleNameSecurityId("JPY LIBOR 9m")), "JPY LIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0010M Index"), simpleNameSecurityId("JPY LIBOR 10m")), "JPY LIBOR 10m", act360,
        following, Period.ofMonths(10), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0011M Index"), simpleNameSecurityId("JPY LIBOR 11m")), "JPY LIBOR 11m", act360,
        following, Period.ofMonths(11), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JY0012M Index"), simpleNameSecurityId("JPY LIBOR 12m")), "JPY LIBOR 12m", act360, 
        following, Period.ofMonths(12), 2, false, jp);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR1T Curncy"), simpleNameSecurityId("JPY DEPOSIT 1d")), "JPY DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR2T Curncy"), simpleNameSecurityId("JPY DEPOSIT 2d")), "JPY DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR3T Curncy"), simpleNameSecurityId("JPY DEPOSIT 3d")), "JPY DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR1Z Curncy"), simpleNameSecurityId("JPY DEPOSIT 1w")), "JPY DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR2Z Curncy"), simpleNameSecurityId("JPY DEPOSIT 2w")), "JPY DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR3Z Curncy"), simpleNameSecurityId("JPY DEPOSIT 3w")), "JPY DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRA Curncy"), simpleNameSecurityId("JPY DEPOSIT 1m")), "JPY DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRB Curncy"), simpleNameSecurityId("JPY DEPOSIT 2m")), "JPY DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRC Curncy"), simpleNameSecurityId("JPY DEPOSIT 3m")), "JPY DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRD Curncy"), simpleNameSecurityId("JPY DEPOSIT 4m")), "JPY DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRE Curncy"), simpleNameSecurityId("JPY DEPOSIT 5m")), "JPY DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRF Curncy"), simpleNameSecurityId("JPY DEPOSIT 6m")), "JPY DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRG Curncy"), simpleNameSecurityId("JPY DEPOSIT 7m")), "JPY DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRH Curncy"), simpleNameSecurityId("JPY DEPOSIT 8m")), "JPY DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRI Curncy"), simpleNameSecurityId("JPY DEPOSIT 9m")), "JPY DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRJ Curncy"), simpleNameSecurityId("JPY DEPOSIT 10m")), "JPY DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDRK Curncy"), simpleNameSecurityId("JPY DEPOSIT 11m")), "JPY DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("JYDR1 Curncy"), simpleNameSecurityId("JPY DEPOSIT 1y")), "JPY DEPOSIT 1y", act360,
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
}

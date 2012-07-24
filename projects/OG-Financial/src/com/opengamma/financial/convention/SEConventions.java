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
public class SEConventions {

  public static void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
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

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("SEK_SWAP")), "SEK_SWAP", thirty360, modified, annual, 1, se, act360,
        modified, quarterly, 1, simpleNameSecurityId("SEK DEPOSIT 3m"), se, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("SEK_3M_SWAP")), "SEK_3M_SWAP", thirty360, modified, annual, 2, se,
        act360, modified, quarterly, 2, simpleNameSecurityId("SEK DEPOSIT 3m"), se, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("SEK_6M_SWAP")), "SEK_6M_SWAP", thirty360, modified, annual, 2, se,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("SEK DEPOSIT 6m"), se, true);

  }
}

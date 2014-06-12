/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.apache.commons.lang.Validate;
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

/**
 * 
 */
public class NOConventions {

  public static void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId dk = ExternalSchemes.financialRegionId("NO");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR1T Curncy"), simpleNameSecurityId("NOK DEPOSIT 1d")), "NOK DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR2T Curncy"), simpleNameSecurityId("NOK DEPOSIT 2d")), "NOK DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR3T Curncy"), simpleNameSecurityId("NOK DEPOSIT 3d")), "NOK DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR1Z Curncy"), simpleNameSecurityId("NOK DEPOSIT 1w")), "NOK DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR2Z Curncy"), simpleNameSecurityId("NOK DEPOSIT 2w")), "NOK DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR3Z Curncy"), simpleNameSecurityId("NOK DEPOSIT 3w")), "NOK DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRA Curncy"), simpleNameSecurityId("NOK DEPOSIT 1m")), "NOK DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRB Curncy"), simpleNameSecurityId("NOK DEPOSIT 2m")), "NOK DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRC Curncy"), simpleNameSecurityId("NOK DEPOSIT 3m")), "NOK DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRD Curncy"), simpleNameSecurityId("NOK DEPOSIT 4m")), "NOK DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRE Curncy"), simpleNameSecurityId("NOK DEPOSIT 5m")), "NOK DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRF Curncy"), simpleNameSecurityId("NOK DEPOSIT 6m")), "NOK DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRG Curncy"), simpleNameSecurityId("NOK DEPOSIT 7m")), "NOK DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRH Curncy"), simpleNameSecurityId("NOK DEPOSIT 8m")), "NOK DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRI Curncy"), simpleNameSecurityId("NOK DEPOSIT 9m")), "NOK DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRJ Curncy"), simpleNameSecurityId("NOK DEPOSIT 10m")), "NOK DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDRK Curncy"), simpleNameSecurityId("NOK DEPOSIT 11m")), "NOK DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR1 Curncy"), simpleNameSecurityId("NOK DEPOSIT 1y")), "NOK DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR2 Curncy"), simpleNameSecurityId("NOK DEPOSIT 2y")), "NOK DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR3 Curncy"), simpleNameSecurityId("NOK DEPOSIT 3y")), "NOK DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR4 Curncy"), simpleNameSecurityId("NOK DEPOSIT 4y")), "NOK DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("NKDR5 Curncy"), simpleNameSecurityId("NOK DEPOSIT 5y")), "NOK DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, dk);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("NOK_SWAP")), "NOK_SWAP", thirty360, modified, annual, 1, dk, act360,
        modified, semiAnnual, 1, simpleNameSecurityId("NOK DEPOSIT 6m"), dk, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("NOK_3M_SWAP")), "NOK_3M_SWAP", thirty360, modified, annual, 2, dk,
        act360, modified, quarterly, 2, simpleNameSecurityId("NOK DEPOSIT 3m"), dk, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("NOK_6M_SWAP")), "NOK_6M_SWAP", thirty360, modified, annual, 2, dk,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("NOK DEPOSIT 6m"), dk, true);

  }
}

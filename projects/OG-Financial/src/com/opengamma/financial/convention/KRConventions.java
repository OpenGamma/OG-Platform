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
public class KRConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId kr = ExternalSchemes.financialRegionId("KR");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR1T Curncy"), simpleNameSecurityId("KRW DEPOSIT 1d")), "KRW DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR2T Curncy"), simpleNameSecurityId("KRW DEPOSIT 2d")), "KRW DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR3T Curncy"), simpleNameSecurityId("KRW DEPOSIT 3d")), "KRW DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR1Z Curncy"), simpleNameSecurityId("KRW DEPOSIT 1w")), "KRW DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR2Z Curncy"), simpleNameSecurityId("KRW DEPOSIT 2w")), "KRW DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR3Z Curncy"), simpleNameSecurityId("KRW DEPOSIT 3w")), "KRW DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRA Curncy"), simpleNameSecurityId("KRW DEPOSIT 1m")), "KRW DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRB Curncy"), simpleNameSecurityId("KRW DEPOSIT 2m")), "KRW DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRC Curncy"), simpleNameSecurityId("KRW DEPOSIT 3m")), "KRW DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRD Curncy"), simpleNameSecurityId("KRW DEPOSIT 4m")), "KRW DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRE Curncy"), simpleNameSecurityId("KRW DEPOSIT 5m")), "KRW DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRF Curncy"), simpleNameSecurityId("KRW DEPOSIT 6m")), "KRW DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRG Curncy"), simpleNameSecurityId("KRW DEPOSIT 7m")), "KRW DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRH Curncy"), simpleNameSecurityId("KRW DEPOSIT 8m")), "KRW DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRI Curncy"), simpleNameSecurityId("KRW DEPOSIT 9m")), "KRW DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRJ Curncy"), simpleNameSecurityId("KRW DEPOSIT 10m")), "KRW DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDRK Curncy"), simpleNameSecurityId("KRW DEPOSIT 11m")), "KRW DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR1 Curncy"), simpleNameSecurityId("KRW DEPOSIT 1y")), "KRW DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR2 Curncy"), simpleNameSecurityId("KRW DEPOSIT 2y")), "KRW DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR3 Curncy"), simpleNameSecurityId("KRW DEPOSIT 3y")), "KRW DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR4 Curncy"), simpleNameSecurityId("KRW DEPOSIT 4y")), "KRW DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWDR5 Curncy"), simpleNameSecurityId("KRW DEPOSIT 5y")), "KRW DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, kr);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWCDC Curncy"), bloombergTickerSecurityId("KWCDC Index"), simpleNameSecurityId("KRW SWAP FIXING 3m")),
        "KRW SWAP FIXING 3m", act365, modified, Period.ofMonths(3), 0, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("KRW_SWAP")), "KRW_SWAP", act365, modified,
        quarterly, 2, kr, act365, modified, quarterly, 2, simpleNameSecurityId("KRW SWAP FIXING 3m"), kr, true);
  }
}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class TRConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final ExternalId sg = ExternalSchemes.financialRegionId("TR");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR1T Curncy"), simpleNameSecurityId("TRY DEPOSIT 1d")), "TRY DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR2T Curncy"), simpleNameSecurityId("TRY DEPOSIT 2d")), "TRY DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR3T Curncy"), simpleNameSecurityId("TRY DEPOSIT 3d")), "TRY DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR1Z Curncy"), simpleNameSecurityId("TRY DEPOSIT 1w")), "TRY DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR2Z Curncy"), simpleNameSecurityId("TRY DEPOSIT 2w")), "TRY DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR3Z Curncy"), simpleNameSecurityId("TRY DEPOSIT 3w")), "TRY DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRA Curncy"), simpleNameSecurityId("TRY DEPOSIT 1m")), "TRY DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRB Curncy"), simpleNameSecurityId("TRY DEPOSIT 2m")), "TRY DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRC Curncy"), simpleNameSecurityId("TRY DEPOSIT 3m")), "TRY DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRD Curncy"), simpleNameSecurityId("TRY DEPOSIT 4m")), "TRY DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRE Curncy"), simpleNameSecurityId("TRY DEPOSIT 5m")), "TRY DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRF Curncy"), simpleNameSecurityId("TRY DEPOSIT 6m")), "TRY DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRG Curncy"), simpleNameSecurityId("TRY DEPOSIT 7m")), "TRY DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRH Curncy"), simpleNameSecurityId("TRY DEPOSIT 8m")), "TRY DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRI Curncy"), simpleNameSecurityId("TRY DEPOSIT 9m")), "TRY DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRJ Curncy"), simpleNameSecurityId("TRY DEPOSIT 10m")), "TRY DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDRK Curncy"), simpleNameSecurityId("TRY DEPOSIT 11m")), "TRY DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR1 Curncy"), simpleNameSecurityId("TRY DEPOSIT 1y")), "TRY DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR2 Curncy"), simpleNameSecurityId("TRY DEPOSIT 2y")), "TRY DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR3 Curncy"), simpleNameSecurityId("TRY DEPOSIT 3y")), "TRY DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR4 Curncy"), simpleNameSecurityId("TRY DEPOSIT 4y")), "TRY DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("TYDR5 Curncy"), simpleNameSecurityId("TRY DEPOSIT 5y")), "TRY DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, sg);
  }
}

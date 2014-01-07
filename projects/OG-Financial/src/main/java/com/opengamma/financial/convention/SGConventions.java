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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class SGConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final ExternalId sg = ExternalSchemes.financialRegionId("SG");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR1T Curncy"), simpleNameSecurityId("SGD DEPOSIT 1d")), "SGD DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR2T Curncy"), simpleNameSecurityId("SGD DEPOSIT 2d")), "SGD DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR3T Curncy"), simpleNameSecurityId("SGD DEPOSIT 3d")), "SGD DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR1Z Curncy"), simpleNameSecurityId("SGD DEPOSIT 1w")), "SGD DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR2Z Curncy"), simpleNameSecurityId("SGD DEPOSIT 2w")), "SGD DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR3Z Curncy"), simpleNameSecurityId("SGD DEPOSIT 3w")), "SGD DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRA Curncy"), simpleNameSecurityId("SGD DEPOSIT 1m")), "SGD DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRB Curncy"), simpleNameSecurityId("SGD DEPOSIT 2m")), "SGD DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRC Curncy"), simpleNameSecurityId("SGD DEPOSIT 3m")), "SGD DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRD Curncy"), simpleNameSecurityId("SGD DEPOSIT 4m")), "SGD DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRE Curncy"), simpleNameSecurityId("SGD DEPOSIT 5m")), "SGD DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRF Curncy"), simpleNameSecurityId("SGD DEPOSIT 6m")), "SGD DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRG Curncy"), simpleNameSecurityId("SGD DEPOSIT 7m")), "SGD DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRH Curncy"), simpleNameSecurityId("SGD DEPOSIT 8m")), "SGD DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRI Curncy"), simpleNameSecurityId("SGD DEPOSIT 9m")), "SGD DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRJ Curncy"), simpleNameSecurityId("SGD DEPOSIT 10m")), "SGD DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRK Curncy"), simpleNameSecurityId("SGD DEPOSIT 11m")), "SGD DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR1 Curncy"), simpleNameSecurityId("SGD DEPOSIT 1y")), "SGD DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR2 Curncy"), simpleNameSecurityId("SGD DEPOSIT 2y")), "SGD DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR3 Curncy"), simpleNameSecurityId("SGD DEPOSIT 3y")), "SGD DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR4 Curncy"), simpleNameSecurityId("SGD DEPOSIT 4y")), "SGD DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR5 Curncy"), simpleNameSecurityId("SGD DEPOSIT 5y")), "SGD DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, sg);
  }
}

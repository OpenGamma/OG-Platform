/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class SGConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final ExternalId sg = ExternalSchemes.financialRegionId("SG");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR1T Curncy"), simpleNameSecurityId("SGD DEPOSIT 1d")), "SGD DEPOSIT 1d", act360,
        following, DateUtils.periodOfDays(1), 0, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR2T Curncy"), simpleNameSecurityId("SGD DEPOSIT 2d")), "SGD DEPOSIT 2d", act360,
        following, DateUtils.periodOfDays(1), 0, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR3T Curncy"), simpleNameSecurityId("SGD DEPOSIT 3d")), "SGD DEPOSIT 3d", act360,
        following, DateUtils.periodOfDays(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR1Z Curncy"), simpleNameSecurityId("SGD DEPOSIT 1w")), "SGD DEPOSIT 1w", act360,
        following, DateUtils.periodOfDays(7), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR2Z Curncy"), simpleNameSecurityId("SGD DEPOSIT 2w")), "SGD DEPOSIT 2w", act360,
        following, DateUtils.periodOfDays(14), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR3Z Curncy"), simpleNameSecurityId("SGD DEPOSIT 3w")), "SGD DEPOSIT 3w", act360,
        following, DateUtils.periodOfDays(21), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRA Curncy"), simpleNameSecurityId("SGD DEPOSIT 1m")), "SGD DEPOSIT 1m", act360,
        following, DateUtils.periodOfMonths(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRB Curncy"), simpleNameSecurityId("SGD DEPOSIT 2m")), "SGD DEPOSIT 2m", act360,
        following, DateUtils.periodOfMonths(2), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRC Curncy"), simpleNameSecurityId("SGD DEPOSIT 3m")), "SGD DEPOSIT 3m", act360,
        following, DateUtils.periodOfMonths(3), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRD Curncy"), simpleNameSecurityId("SGD DEPOSIT 4m")), "SGD DEPOSIT 4m", act360,
        following, DateUtils.periodOfMonths(4), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRE Curncy"), simpleNameSecurityId("SGD DEPOSIT 5m")), "SGD DEPOSIT 5m", act360,
        following, DateUtils.periodOfMonths(5), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRF Curncy"), simpleNameSecurityId("SGD DEPOSIT 6m")), "SGD DEPOSIT 6m", act360,
        following, DateUtils.periodOfMonths(6), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRG Curncy"), simpleNameSecurityId("SGD DEPOSIT 7m")), "SGD DEPOSIT 7m", act360,
        following, DateUtils.periodOfMonths(7), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRH Curncy"), simpleNameSecurityId("SGD DEPOSIT 8m")), "SGD DEPOSIT 8m", act360,
        following, DateUtils.periodOfMonths(8), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRI Curncy"), simpleNameSecurityId("SGD DEPOSIT 9m")), "SGD DEPOSIT 9m", act360,
        following, DateUtils.periodOfMonths(9), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRJ Curncy"), simpleNameSecurityId("SGD DEPOSIT 10m")), "SGD DEPOSIT 10m", act360,
        following, DateUtils.periodOfMonths(10), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDRK Curncy"), simpleNameSecurityId("SGD DEPOSIT 11m")), "SGD DEPOSIT 11m", act360,
        following, DateUtils.periodOfMonths(11), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR1 Curncy"), simpleNameSecurityId("SGD DEPOSIT 1y")), "SGD DEPOSIT 1y", act360,
        following, DateUtils.periodOfYears(1), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR2 Curncy"), simpleNameSecurityId("SGD DEPOSIT 2y")), "SGD DEPOSIT 2y", act360,
        following, DateUtils.periodOfYears(2), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR3 Curncy"), simpleNameSecurityId("SGD DEPOSIT 3y")), "SGD DEPOSIT 3y", act360,
        following, DateUtils.periodOfYears(3), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR4 Curncy"), simpleNameSecurityId("SGD DEPOSIT 4y")), "SGD DEPOSIT 4y", act360,
        following, DateUtils.periodOfYears(4), 2, false, sg);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SDDR5 Curncy"), simpleNameSecurityId("SGD DEPOSIT 5y")), "SGD DEPOSIT 5y", act360,
        following, DateUtils.periodOfYears(5), 2, false, sg);
  }
}

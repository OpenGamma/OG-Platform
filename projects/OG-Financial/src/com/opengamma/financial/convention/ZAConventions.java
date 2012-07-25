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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class ZAConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");

    final ExternalId za = ExternalSchemes.financialRegionId("ZA");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADR1T Curncy"), simpleNameSecurityId("ZAR DEPOSIT 1d")), "ZAR DEPOSIT 1d", act365,
        following, Period.ofDays(1), 0, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADR2T Curncy"), simpleNameSecurityId("ZAR DEPOSIT 2d")), "ZAR DEPOSIT 2d", act365,
        following, Period.ofDays(1), 0, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADR3T Curncy"), simpleNameSecurityId("ZAR DEPOSIT 3d")), "ZAR DEPOSIT 3d", act365,
        following, Period.ofDays(1), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADR1Z Curncy"), simpleNameSecurityId("ZAR DEPOSIT 1w")), "ZAR DEPOSIT 1w", act365,
        following, Period.ofDays(7), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADR2Z Curncy"), simpleNameSecurityId("ZAR DEPOSIT 2w")), "ZAR DEPOSIT 2w", act365,
        following, Period.ofDays(14), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADR3Z Curncy"), simpleNameSecurityId("ZAR DEPOSIT 3w")), "ZAR DEPOSIT 3w", act365,
        following, Period.ofDays(21), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRA Curncy"), simpleNameSecurityId("ZAR DEPOSIT 1m")), "ZAR DEPOSIT 1m", act365,
        following, Period.ofMonths(1), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRB Curncy"), simpleNameSecurityId("ZAR DEPOSIT 2m")), "ZAR DEPOSIT 2m", act365,
        following, Period.ofMonths(2), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRC Curncy"), simpleNameSecurityId("ZAR DEPOSIT 3m")), "ZAR DEPOSIT 3m", act365,
        following, Period.ofMonths(3), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRD Curncy"), simpleNameSecurityId("ZAR DEPOSIT 4m")), "ZAR DEPOSIT 4m", act365,
        following, Period.ofMonths(4), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRE Curncy"), simpleNameSecurityId("ZAR DEPOSIT 5m")), "ZAR DEPOSIT 5m", act365,
        following, Period.ofMonths(5), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRF Curncy"), simpleNameSecurityId("ZAR DEPOSIT 6m")), "ZAR DEPOSIT 6m", act365,
        following, Period.ofMonths(6), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRG Curncy"), simpleNameSecurityId("ZAR DEPOSIT 7m")), "ZAR DEPOSIT 7m", act365,
        following, Period.ofMonths(7), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRH Curncy"), simpleNameSecurityId("ZAR DEPOSIT 8m")), "ZAR DEPOSIT 8m", act365,
        following, Period.ofMonths(8), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRI Curncy"), simpleNameSecurityId("ZAR DEPOSIT 9m")), "ZAR DEPOSIT 9m", act365,
        following, Period.ofMonths(9), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRJ Curncy"), simpleNameSecurityId("ZAR DEPOSIT 10m")), "ZAR DEPOSIT 10m", act365,
        following, Period.ofMonths(10), 2, false, za);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SADRK Curncy"), simpleNameSecurityId("ZAR DEPOSIT 11m")), "ZAR DEPOSIT 11m", act365,
        following, Period.ofMonths(11), 2, false, za);

  }

}

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
public class CNConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final ExternalId cn = ExternalSchemes.financialRegionId("CN");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR1T Curncy"), simpleNameSecurityId("CNY DEPOSIT 1d")), "CNY DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR2T Curncy"), simpleNameSecurityId("CNY DEPOSIT 2d")), "CNY DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR3T Curncy"), simpleNameSecurityId("CNY DEPOSIT 3d")), "CNY DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR1Z Curncy"), simpleNameSecurityId("CNY DEPOSIT 1w")), "CNY DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR2Z Curncy"), simpleNameSecurityId("CNY DEPOSIT 2w")), "CNY DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR3Z Curncy"), simpleNameSecurityId("CNY DEPOSIT 3w")), "CNY DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRA Curncy"), simpleNameSecurityId("CNY DEPOSIT 1m")), "CNY DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRB Curncy"), simpleNameSecurityId("CNY DEPOSIT 2m")), "CNY DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRC Curncy"), simpleNameSecurityId("CNY DEPOSIT 3m")), "CNY DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRD Curncy"), simpleNameSecurityId("CNY DEPOSIT 4m")), "CNY DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRE Curncy"), simpleNameSecurityId("CNY DEPOSIT 5m")), "CNY DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRF Curncy"), simpleNameSecurityId("CNY DEPOSIT 6m")), "CNY DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRG Curncy"), simpleNameSecurityId("CNY DEPOSIT 7m")), "CNY DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRH Curncy"), simpleNameSecurityId("CNY DEPOSIT 8m")), "CNY DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRI Curncy"), simpleNameSecurityId("CNY DEPOSIT 9m")), "CNY DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRJ Curncy"), simpleNameSecurityId("CNY DEPOSIT 10m")), "CNY DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDRK Curncy"), simpleNameSecurityId("CNY DEPOSIT 11m")), "CNY DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CCDR1 Curncy"), simpleNameSecurityId("CNY DEPOSIT 1y")), "CNY DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, cn);


  }

}

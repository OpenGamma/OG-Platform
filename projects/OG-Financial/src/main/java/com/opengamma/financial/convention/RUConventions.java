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
public class RUConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final ExternalId cn = ExternalSchemes.financialRegionId("RU");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1T Curncy"), simpleNameSecurityId("RUB DEPOSIT 1d")), "RUB DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR2T Curncy"), simpleNameSecurityId("RUB DEPOSIT 2d")), "RUB DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR3T Curncy"), simpleNameSecurityId("RUB DEPOSIT 3d")), "RUB DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 1w")), "RUB DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR2Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 2w")), "RUB DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR3Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 3w")), "RUB DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRA Curncy"), simpleNameSecurityId("RUB DEPOSIT 1m")), "RUB DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRB Curncy"), simpleNameSecurityId("RUB DEPOSIT 2m")), "RUB DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRC Curncy"), simpleNameSecurityId("RUB DEPOSIT 3m")), "RUB DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRD Curncy"), simpleNameSecurityId("RUB DEPOSIT 4m")), "RUB DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRE Curncy"), simpleNameSecurityId("RUB DEPOSIT 5m")), "RUB DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRF Curncy"), simpleNameSecurityId("RUB DEPOSIT 6m")), "RUB DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRG Curncy"), simpleNameSecurityId("RUB DEPOSIT 7m")), "RUB DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRH Curncy"), simpleNameSecurityId("RUB DEPOSIT 8m")), "RUB DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRI Curncy"), simpleNameSecurityId("RUB DEPOSIT 9m")), "RUB DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRJ Curncy"), simpleNameSecurityId("RUB DEPOSIT 10m")), "RUB DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRK Curncy"), simpleNameSecurityId("RUB DEPOSIT 11m")), "RUB DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, cn);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1 Curncy"), simpleNameSecurityId("RUB DEPOSIT 1y")), "RUB DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, cn);


  }

}

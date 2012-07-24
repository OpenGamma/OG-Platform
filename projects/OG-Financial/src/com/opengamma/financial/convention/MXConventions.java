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
public class MXConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act252 = DayCountFactory.INSTANCE.getDayCount("Actual/252");
    final ExternalId br = ExternalSchemes.financialRegionId("BR");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR1T Curncy"), simpleNameSecurityId("BRL DEPOSIT 1d")), "BRL DEPOSIT 1d", act252,
        following, Period.ofDays(1), 0, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR2T Curncy"), simpleNameSecurityId("BRL DEPOSIT 2d")), "BRL DEPOSIT 2d", act252,
        following, Period.ofDays(1), 0, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR3T Curncy"), simpleNameSecurityId("BRL DEPOSIT 3d")), "BRL DEPOSIT 3d", act252,
        following, Period.ofDays(1), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR1Z Curncy"), simpleNameSecurityId("BRL DEPOSIT 1w")), "BRL DEPOSIT 1w", act252,
        following, Period.ofDays(7), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR2Z Curncy"), simpleNameSecurityId("BRL DEPOSIT 2w")), "BRL DEPOSIT 2w", act252,
        following, Period.ofDays(14), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR3Z Curncy"), simpleNameSecurityId("BRL DEPOSIT 3w")), "BRL DEPOSIT 3w", act252,
        following, Period.ofDays(21), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRA Curncy"), simpleNameSecurityId("BRL DEPOSIT 1m")), "BRL DEPOSIT 1m", act252,
        following, Period.ofMonths(1), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRB Curncy"), simpleNameSecurityId("BRL DEPOSIT 2m")), "BRL DEPOSIT 2m", act252,
        following, Period.ofMonths(2), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRC Curncy"), simpleNameSecurityId("BRL DEPOSIT 3m")), "BRL DEPOSIT 3m", act252,
        following, Period.ofMonths(3), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRD Curncy"), simpleNameSecurityId("BRL DEPOSIT 4m")), "BRL DEPOSIT 4m", act252,
        following, Period.ofMonths(4), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRE Curncy"), simpleNameSecurityId("BRL DEPOSIT 5m")), "BRL DEPOSIT 5m", act252,
        following, Period.ofMonths(5), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRF Curncy"), simpleNameSecurityId("BRL DEPOSIT 6m")), "BRL DEPOSIT 6m", act252,
        following, Period.ofMonths(6), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRG Curncy"), simpleNameSecurityId("BRL DEPOSIT 7m")), "BRL DEPOSIT 7m", act252,
        following, Period.ofMonths(7), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRH Curncy"), simpleNameSecurityId("BRL DEPOSIT 8m")), "BRL DEPOSIT 8m", act252,
        following, Period.ofMonths(8), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRI Curncy"), simpleNameSecurityId("BRL DEPOSIT 9m")), "BRL DEPOSIT 9m", act252,
        following, Period.ofMonths(9), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRJ Curncy"), simpleNameSecurityId("BRL DEPOSIT 10m")), "BRL DEPOSIT 10m", act252,
        following, Period.ofMonths(10), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDRK Curncy"), simpleNameSecurityId("BRL DEPOSIT 11m")), "BRL DEPOSIT 11m", act252,
        following, Period.ofMonths(11), 2, false, br);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("BCDR1 Curncy"), simpleNameSecurityId("BRL DEPOSIT 12m")), "BRL DEPOSIT 12m", act252,
        following, Period.ofMonths(12), 2, false, br);
  }

}

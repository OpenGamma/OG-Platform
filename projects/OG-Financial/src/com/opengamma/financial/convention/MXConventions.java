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
    final DayCount dc = DayCountFactory.INSTANCE.getDayCount("28/360");
    final ExternalId mx = ExternalSchemes.financialRegionId("MX");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR1T Curncy"), simpleNameSecurityId("MXN DEPOSIT 1d")), "MXN DEPOSIT 1d", dc,
        following, Period.ofDays(1), 0, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR2T Curncy"), simpleNameSecurityId("MXN DEPOSIT 2d")), "MXN DEPOSIT 2d", dc,
        following, Period.ofDays(1), 0, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR3T Curncy"), simpleNameSecurityId("MXN DEPOSIT 3d")), "MXN DEPOSIT 3d", dc,
        following, Period.ofDays(1), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR1Z Curncy"), simpleNameSecurityId("MXN DEPOSIT 1w")), "MXN DEPOSIT 1w", dc,
        following, Period.ofDays(7), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR2Z Curncy"), simpleNameSecurityId("MXN DEPOSIT 2w")), "MXN DEPOSIT 2w", dc,
        following, Period.ofDays(14), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR3Z Curncy"), simpleNameSecurityId("MXN DEPOSIT 3w")), "MXN DEPOSIT 3w", dc,
        following, Period.ofDays(21), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRA Curncy"), simpleNameSecurityId("MXN DEPOSIT 1m")), "MXN DEPOSIT 1m", dc,
        following, Period.ofMonths(1), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRB Curncy"), simpleNameSecurityId("MXN DEPOSIT 2m")), "MXN DEPOSIT 2m", dc,
        following, Period.ofMonths(2), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRC Curncy"), simpleNameSecurityId("MXN DEPOSIT 3m")), "MXN DEPOSIT 3m", dc,
        following, Period.ofMonths(3), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRD Curncy"), simpleNameSecurityId("MXN DEPOSIT 4m")), "MXN DEPOSIT 4m", dc,
        following, Period.ofMonths(4), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRE Curncy"), simpleNameSecurityId("MXN DEPOSIT 5m")), "MXN DEPOSIT 5m", dc,
        following, Period.ofMonths(5), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRF Curncy"), simpleNameSecurityId("MXN DEPOSIT 6m")), "MXN DEPOSIT 6m", dc,
        following, Period.ofMonths(6), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRG Curncy"), simpleNameSecurityId("MXN DEPOSIT 7m")), "MXN DEPOSIT 7m", dc,
        following, Period.ofMonths(7), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRH Curncy"), simpleNameSecurityId("MXN DEPOSIT 8m")), "MXN DEPOSIT 8m", dc,
        following, Period.ofMonths(8), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRI Curncy"), simpleNameSecurityId("MXN DEPOSIT 9m")), "MXN DEPOSIT 9m", dc,
        following, Period.ofMonths(9), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRJ Curncy"), simpleNameSecurityId("MXN DEPOSIT 10m")), "MXN DEPOSIT 10m", dc,
        following, Period.ofMonths(10), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDRK Curncy"), simpleNameSecurityId("MXN DEPOSIT 11m")), "MXN DEPOSIT 11m", dc,
        following, Period.ofMonths(11), 2, false, mx);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPDR1 Curncy"), simpleNameSecurityId("MXN DEPOSIT 12m")), "MXN DEPOSIT 12m", dc,
        following, Period.ofMonths(12), 2, false, mx);
  }

}

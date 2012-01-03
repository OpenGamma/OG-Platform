/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
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
public class CAConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    //TODO looked at BSYM and the codes seem right but need to check
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR O/N")), "CAD LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD00S/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR S/N")), "CAD LIBOR S/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR T/N")), "CAD LIBOR T/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 1w")), "CAD LIBOR 1w", act360,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 2w")), "CAD LIBOR 2w", act360,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 1m")), "CAD LIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 2m")), "CAD LIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 3m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CADLIBORP3M")), "CAD LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 4m")), "CAD LIBOR 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 5m")), "CAD LIBOR 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CADLIBORP6M")), "CAD LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 7m")), "CAD LIBOR 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 8m")), "CAD LIBOR 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 9m")), "CAD LIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 10m")), "CAD LIBOR 10m", act360,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 11m")), "CAD LIBOR 11m", act360,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CD0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD LIBOR 12m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CADLIBORP12M")), "CAD LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, null);

    //TODO need to check that these are right for deposit rates
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 1d")), "CAD DEPOSIT 1d", act365,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 2d")), "CAD DEPOSIT 2d", act365,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 3d")), "CAD DEPOSIT 3d", act365,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 1w")), "CAD DEPOSIT 1w", act365,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 2w")), "CAD DEPOSIT 2w", act365,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 3w")), "CAD DEPOSIT 3w", act365,
        following, Period.ofDays(21), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 1m")), "CAD DEPOSIT 1m", act365,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 2m")), "CAD DEPOSIT 2m", act365,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 3m")), "CAD DEPOSIT 3m", act365,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 4m")), "CAD DEPOSIT 4m", act365,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 5m")), "CAD DEPOSIT 5m", act365,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 6m")), "CAD DEPOSIT 6m", act365,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 7m")), "CAD DEPOSIT 7m", act365,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 8m")), "CAD DEPOSIT 8m", act365,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 9m")), "CAD DEPOSIT 9m", act365,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 10m")), "CAD DEPOSIT 10m", act365,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 11m")), "CAD DEPOSIT 11m", act365,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 1y")), "CAD DEPOSIT 1y", act365,
        following, Period.ofYears(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 2y")), "CAD DEPOSIT 2y", act365,
        following, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 3y")), "CAD DEPOSIT 3y", act365,
        following, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 4y")), "CAD DEPOSIT 4y", act365,
        following, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD DEPOSIT 5y")), "CAD DEPOSIT 5y", act365,
        following, Period.ofYears(5), 2, false, null);
    //TODO check daycount
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDOR01 Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 1m")),
        "CDOR 1m", act365, following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDOR02 Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 2m")),
        "CDOR 2m", act365, following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDOR03 Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 3m")),
        "CDOR 3m", act365, following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDOR06 Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 6m")),
        "CDOR 6m", act365, following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CDOR12 Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 12m")),
        "CDOR 12m", act365, following, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CAONREPO Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "RBC OVERNIGHT REPO")), "RBC OVERNIGHT REPO",
        act365, following, Period.ofDays(1), 0, false, null);

    //TODO holiday associated with CAD swaps is Toronto
    final ExternalId ca = RegionUtils.financialRegionId("CA");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD_SWAP")), "CAD_SWAP", act365, modified, semiAnnual, 0, ca, act365,
        modified, quarterly, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 3m"), ca, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD_1Y_SWAP")), "CAD_1Y_SWAP", act365, modified, annual, 0, ca, act365,
        modified, quarterly, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CDOR 3m"), ca, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD_OIS_SWAP")), "CAD_OIS_SWAP", act365, modified, annual, 0, ca,
        act365, modified, annual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "RBC OVERNIGHT REPO"), ca, true);
    conventionMaster
        .addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD_OIS_CASH")), "CAD_OIS_CASH", act365, following, null, 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CAD_FRA")), "CAD_FRA", act365,
        following, Period.ofMonths(3), 2, false, null);

    //TODO according to my information:
    //"Floating leg compounded quarterly at CDOR Flat paid semi-annually or annually for 1y"
    //Don't know how we're going to put that in
  }
}

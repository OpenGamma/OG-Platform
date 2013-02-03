/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.core.id.ExternalSchemes.tullettPrebonSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Contains information used to construct standard version of RUB instruments.
 */
public class RUConventions {

  /**
   * Adds conventions
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final ExternalId ru = ExternalSchemes.financialRegionId("RU");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1T Curncy"), simpleNameSecurityId("RUB DEPOSIT 1d")), "RUB DEPOSIT 1d", act360,
        following, DateUtils.periodOfDays(1), 0, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR2T Curncy"), simpleNameSecurityId("RUB DEPOSIT 2d")), "RUB DEPOSIT 2d", act360,
        following, DateUtils.periodOfDays(1), 1, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR3T Curncy"), simpleNameSecurityId("RUB DEPOSIT 3d")), "RUB DEPOSIT 3d", act360,
        following, DateUtils.periodOfDays(1), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 1w")), "RUB DEPOSIT 1w", act360,
        following, DateUtils.periodOfDays(7), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR2Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 2w")), "RUB DEPOSIT 2w", act360,
        following, DateUtils.periodOfDays(14), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR3Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 3w")), "RUB DEPOSIT 3w", act360,
        following, DateUtils.periodOfDays(21), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRA Curncy"), simpleNameSecurityId("RUB DEPOSIT 1m")), "RUB DEPOSIT 1m", act360,
        following, DateUtils.periodOfMonths(1), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRB Curncy"), simpleNameSecurityId("RUB DEPOSIT 2m")), "RUB DEPOSIT 2m", act360,
        following, DateUtils.periodOfMonths(2), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRC Curncy"), simpleNameSecurityId("RUB DEPOSIT 3m")), "RUB DEPOSIT 3m", act360,
        following, DateUtils.periodOfMonths(3), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRD Curncy"), simpleNameSecurityId("RUB DEPOSIT 4m")), "RUB DEPOSIT 4m", act360,
        following, DateUtils.periodOfMonths(4), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRE Curncy"), simpleNameSecurityId("RUB DEPOSIT 5m")), "RUB DEPOSIT 5m", act360,
        following, DateUtils.periodOfMonths(5), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRF Curncy"), simpleNameSecurityId("RUB DEPOSIT 6m")), "RUB DEPOSIT 6m", act360,
        following, DateUtils.periodOfMonths(6), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRG Curncy"), simpleNameSecurityId("RUB DEPOSIT 7m")), "RUB DEPOSIT 7m", act360,
        following, DateUtils.periodOfMonths(7), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRH Curncy"), simpleNameSecurityId("RUB DEPOSIT 8m")), "RUB DEPOSIT 8m", act360,
        following, DateUtils.periodOfMonths(8), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRI Curncy"), simpleNameSecurityId("RUB DEPOSIT 9m")), "RUB DEPOSIT 9m", act360,
        following, DateUtils.periodOfMonths(9), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRJ Curncy"), simpleNameSecurityId("RUB DEPOSIT 10m")), "RUB DEPOSIT 10m", act360,
        following, DateUtils.periodOfMonths(10), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRK Curncy"), simpleNameSecurityId("RUB DEPOSIT 11m")), "RUB DEPOSIT 11m", act360,
        following, DateUtils.periodOfMonths(11), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1 Curncy"), simpleNameSecurityId("RUB DEPOSIT 1y")), "RUB DEPOSIT 1y", act360,
        following, DateUtils.periodOfYears(1), 2, false, ru);

    for (final int i : new int[] {1}) {
      final String name = "RUB IMPLIED DEPOSIT " + i + "w";
      final ExternalId tullett = tullettPrebonSecurityId("EMIDPRUBTOM" + i + "W");
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, DateUtils.periodOfDays(i * 7), 2, false, ru);
    }

    for (final int i : new int[] {1, 2, 3, 6, 9, 18}) {
      final String name = "RUB IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullett = tullettPrebonSecurityId("EMIDPRUBTOM" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, DateUtils.periodOfMonths(i), 2, false, ru);
    }

    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7}) {
      final String name = "RUB IMPLIED DEPOSIT " + i + "y";
      ExternalId tullett;
      if (i == 1 || i == 2) {
        tullett = tullettPrebonSecurityId("EMIDPRUBTOM" + i * 12 + "M");
      } else {
        tullett = tullettPrebonSecurityId("EMIDPRUBTOM" + (i < 10 ? "0" : "") + i + "Y");
      }
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, DateUtils.periodOfYears(i), 2, false, ru);
    }
  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.core.id.ExternalSchemes.tullettPrebonSecurityId;
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
 * Contains information used to construct standard version of RUB instruments.
 */
public class RUConventions {

  /**
   * Adds conventions
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final ExternalId ru = ExternalSchemes.financialRegionId("RU");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1T Curncy"), simpleNameSecurityId("RUB DEPOSIT 1d")), "RUB DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR2T Curncy"), simpleNameSecurityId("RUB DEPOSIT 2d")), "RUB DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR3T Curncy"), simpleNameSecurityId("RUB DEPOSIT 3d")), "RUB DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 1w")), "RUB DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR2Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 2w")), "RUB DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR3Z Curncy"), simpleNameSecurityId("RUB DEPOSIT 3w")), "RUB DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRA Curncy"), simpleNameSecurityId("RUB DEPOSIT 1m")), "RUB DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRB Curncy"), simpleNameSecurityId("RUB DEPOSIT 2m")), "RUB DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRC Curncy"), simpleNameSecurityId("RUB DEPOSIT 3m")), "RUB DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRD Curncy"), simpleNameSecurityId("RUB DEPOSIT 4m")), "RUB DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRE Curncy"), simpleNameSecurityId("RUB DEPOSIT 5m")), "RUB DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRF Curncy"), simpleNameSecurityId("RUB DEPOSIT 6m")), "RUB DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRG Curncy"), simpleNameSecurityId("RUB DEPOSIT 7m")), "RUB DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRH Curncy"), simpleNameSecurityId("RUB DEPOSIT 8m")), "RUB DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRI Curncy"), simpleNameSecurityId("RUB DEPOSIT 9m")), "RUB DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRJ Curncy"), simpleNameSecurityId("RUB DEPOSIT 10m")), "RUB DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDRK Curncy"), simpleNameSecurityId("RUB DEPOSIT 11m")), "RUB DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, ru);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("RRDR1 Curncy"), simpleNameSecurityId("RUB DEPOSIT 1y")), "RUB DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, ru);

    for (final int i : new int[] {1}) {
      final String name = "RUB IMPLIED DEPOSIT " + i + "w";
      final ExternalId tullett = tullettPrebonSecurityId("EMIDPRUBTOM" + i + "W");
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, Period.ofDays(i * 7), 2, false, ru);
    }

    for (final int i : new int[] {1, 2, 3, 6, 9, 18}) {
      final String name = "RUB IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullett = tullettPrebonSecurityId("EMIDPRUBTOM" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simple = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, Period.ofMonths(i), 2, false, ru);
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
      utils.addConventionBundle(ExternalIdBundle.of(tullett, simple), name, act360, following, Period.ofYears(i), 2, false, ru);
    }
    
    final String ruTreasuryName = "RU_TREASURY_BOND_CONVENTION";
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(ruTreasuryName)), ruTreasuryName, true, true, 0, 1, true);
    
  }

}

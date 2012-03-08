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
public class NZConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final ExternalId nz = RegionUtils.financialRegionId("NZ");

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR O/N")), "NZD LIBOR O/N", act365,
        following, Period.ofDays(1), 0, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ00S/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR S/N")), "NZD LIBOR S/N", act365,
        following, Period.ofDays(1), 0, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR T/N")), "NZD LIBOR T/N", act365,
        following, Period.ofDays(1), 0, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 1w")), "NZD LIBOR 1w", act365,
        following, Period.ofDays(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 2w")), "NZD LIBOR 2w", act365,
        following, Period.ofDays(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 1m")), "NZD LIBOR 1m", act365,
        following, Period.ofMonths(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 2m")), "NZD LIBOR 2m", act365,
        following, Period.ofMonths(2), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 3m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDLIBORP3M")), "NZD LIBOR 3m", act365, following, Period.ofMonths(3), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 4m")), "NZD LIBOR 4m", act365,
        following, Period.ofMonths(4), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 5m")), "NZD LIBOR 5m", act365,
        following, Period.ofMonths(5), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDLIBORP6M")), "NZD LIBOR 6m", act365, following, Period.ofMonths(6), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 7m")), "NZD LIBOR 7m", act365,
        following, Period.ofMonths(7), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 8m")), "NZD LIBOR 8m", act365,
        following, Period.ofMonths(8), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 9m")), "NZD LIBOR 9m", act365,
        following, Period.ofMonths(9), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 10m")), "NZD LIBOR 10m", act365,
        following, Period.ofMonths(10), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 11m")), "NZD LIBOR 11m", act365,
        following, Period.ofMonths(11), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZ0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 12m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDLIBORP12M")), "NZD LIBOR 12m", act365, following, Period.ofMonths(12), 2, false, nz);

    //TODO need to check that these are right for deposit rates
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 1d")), "NZD DEPOSIT 1d", act365,
        following, Period.ofDays(1), 0, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 2d")), "NZD DEPOSIT 2d", act365,
        following, Period.ofDays(1), 0, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 3d")), "NZD DEPOSIT 3d", act365,
        following, Period.ofDays(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 1w")), "NZD DEPOSIT 1w", act365,
        following, Period.ofDays(7), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 2w")), "NZD DEPOSIT 2w", act365,
        following, Period.ofDays(14), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 3w")), "NZD DEPOSIT 3w", act365,
        following, Period.ofDays(21), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 1m")), "NZD DEPOSIT 1m", act365,
        following, Period.ofMonths(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 2m")), "NZD DEPOSIT 2m", act365,
        following, Period.ofMonths(2), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 3m")), "NZD DEPOSIT 3m", act365,
        following, Period.ofMonths(3), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 4m")), "NZD DEPOSIT 4m", act365,
        following, Period.ofMonths(4), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 5m")), "NZD DEPOSIT 5m", act365,
        following, Period.ofMonths(5), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 6m")), "NZD DEPOSIT 6m", act365,
        following, Period.ofMonths(6), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 7m")), "NZD DEPOSIT 7m", act365,
        following, Period.ofMonths(7), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 8m")), "NZD DEPOSIT 8m", act365,
        following, Period.ofMonths(8), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 9m")), "NZD DEPOSIT 9m", act365,
        following, Period.ofMonths(9), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 10m")), "NZD DEPOSIT 10m", act365,
        following, Period.ofMonths(10), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 11m")), "NZD DEPOSIT 11m", act365,
        following, Period.ofMonths(11), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 1y")), "NZD DEPOSIT 1y", act365,
        following, Period.ofYears(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 2y")), "NZD DEPOSIT 2y", act365,
        following, Period.ofYears(2), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 3y")), "NZD DEPOSIT 3y", act365,
        following, Period.ofYears(3), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 4y")), "NZD DEPOSIT 4y", act365,
        following, Period.ofYears(4), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NDDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD DEPOSIT 5y")), "NZD DEPOSIT 5y", act365,
        following, Period.ofYears(5), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("NZOCRS Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "RBNZ CASH DAILY RATE")), "RBNZ CASH DAILY RATE",
        act365, following, Period.ofDays(1), 0, false, nz);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD_SWAP")), "NZD_SWAP", act365, modified, semiAnnual, 1, nz, act365,
        modified, quarterly, 1, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 3m"), nz, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD_3M_SWAP")), "NZD_3M_SWAP", act365, modified, semiAnnual, 1, nz,
        act365, modified, quarterly, 1, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD LIBOR 3m"), nz, true);
    // Overnight Index Swap Convention have additional flag, publicationLag
    final Integer publicationLag = 0;
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZD_OIS_SWAP")), "NZD_OIS_SWAP", act365, modified, annual, 0, nz,
        act365, modified, annual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "RBNZ CASH DAILY RATE"), nz, true, publicationLag);

    //Identifiers for external data 
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP1D"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP1D")),
        "NZDCASHP1D", act365, following, Period.ofDays(1), 0, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP1M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP1M")),
        "NZDCASHP1M", act365, modified, Period.ofMonths(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP2M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP2M")),
        "NZDCASHP2M", act365, modified, Period.ofMonths(2), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP3M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP3M")),
        "NZDCASHP3M", act365, modified, Period.ofMonths(3), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP4M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP4M")),
        "NZDCASHP4M", act365, modified, Period.ofMonths(4), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP5M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP5M")),
        "NZDCASHP5M", act365, modified, Period.ofMonths(5), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP6M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP6M")),
        "NZDCASHP6M", act365, modified, Period.ofMonths(6), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP7M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP7M")),
        "NZDCASHP7M", act365, modified, Period.ofMonths(7), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP8M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP8M")),
        "NZDCASHP8M", act365, modified, Period.ofMonths(8), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP9M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP9M")),
        "NZDCASHP9M", act365, modified, Period.ofMonths(9), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP10M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP10M")),
        "NZDCASHP10M", act365, modified, Period.ofMonths(10), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP11M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP11M")),
        "NZDCASHP11M", act365, modified, Period.ofMonths(1), 2, false, nz);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "NZDCASHP12M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "NZDCASHP12M")),
        "NZDCASHP12M", act365, modified, Period.ofMonths(12), 2, false, nz);
  }
}

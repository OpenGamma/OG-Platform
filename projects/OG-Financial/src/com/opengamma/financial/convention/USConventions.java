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
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class USConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    // NOTE THESE ONLY APPLY TO US LIBOR RATES
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    //    final Frequency freq = null;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR O/N")), "USD LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR T/N")), "USD LIBOR T/N", act360,
        following, Period.ofDays(1), 1, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 1w")), "USD LIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 2w")), "USD LIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 1m")), "USD LIBOR 1m", act360,
        modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 2m")), "USD LIBOR 2m", act360,
        modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDLIBORP3M")), "USD LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 4m")), "USD LIBOR 4m", act360,
        modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 5m")), "USD LIBOR 5m", act360,
        modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDLIBORP6M")), "USD LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 7m")), "USD LIBOR 7m", act360,
        modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 8m")), "USD LIBOR 8m", act360,
        modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 9m")), "USD LIBOR 9m", act360,
        modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 10m")), "USD LIBOR 10m", act360,
        modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 11m")), "USD LIBOR 11m", act360,
        modified, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 12m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDLIBORP12M")), "USD LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, null);

    //TODO need to check that these are right for deposit rates
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 1d")), "USD DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 2d")), "USD DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 3d")), "USD DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR7D Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 1w")), "USD DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 2w")), "USD DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 3w")), "USD DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 1m")), "USD DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 2m")), "USD DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 3m")), "USD DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 4m")), "USD DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 5m")), "USD DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 6m")), "USD DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 7m")), "USD DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 8m")), "USD DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 9m")), "USD DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 10m")), "USD DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 11m")), "USD DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 1y")), "USD DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 2y")), "USD DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 3y")), "USD DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 4y")), "USD DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD DEPOSIT 5y")), "USD DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, null);

    //TODO check that this is ok for FRA
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR00C Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 3x3")), "USD FRA 3x3", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR0CF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 6x9")), "USD FRA 6x9", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR0FI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 9x12")), "USD FRA 9x12", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR0I1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 12x15")), "USD FRA 12x15", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR011C Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 15x18")), "USD FRA 15x18", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR1C1F Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 18x21")), "USD FRA 18x21", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR1F1I Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 21x24")), "USD FRA 21x24", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR1I2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 24x27")), "USD FRA 24x27", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR012 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 12x24")), "USD FRA 12x24", act360,
        following, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("USFR0F1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FRA 6x12")), "USD FRA 6x12", act360,
        following, Period.ofMonths(6), 2, false, null);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("FEDL01 Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FF EFFECTIVE")), "USD FF EFFECTIVE", act360,
        following, Period.ofDays(1), 2, false, null);

    //TODO with improvement in settlement days definition (i.e. including holiday and adjustment) change this
    // should be 2, LON, following
    // holiday for swap should be NY+LON
    final ExternalId usgb = RegionUtils.financialRegionId("US+GB");
    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = semiAnnual;
    final DayCount swapFloatDayCount = act360;
    final BusinessDayConvention swapFloatBusinessDay = modified;
    final Frequency swapFloatPaymentFrequency = quarterly;
    for (int i = 1; i <= 30; i++) {
      final String bbgId = "USSW" + i + " Curncy";
      final String ogName = "USD SWAP " + i + "y";
      conventionMaster.addConventionBundle(
          ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId(bbgId), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ogName)), ogName, 
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, swapFloatPaymentFrequency, 
          2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"), usgb, true);
    }
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_SWAP")), "USD_SWAP", swapFixedDayCount, swapFixedBusinessDay, 
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, swapFloatPaymentFrequency, 
        2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"), usgb, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_3M_SWAP")), "USD_3M_SWAP", thirty360, modified, semiAnnual, 2, usgb,
        act360, modified, quarterly, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"), usgb, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_6M_SWAP")), "USD_6M_SWAP", thirty360, modified, semiAnnual, 2, usgb,
        act360, modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 6m"), usgb, true);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_IR_FUTURE")), "USD_IR_FUTURE", act360, modified, Period.ofMonths(3), 2, false, null);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_OIS_SWAP")), "USD_OIS_SWAP", thirty360, modified, annual, 2, usgb,
        thirty360, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FF EFFECTIVE"), usgb);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_OIS_CASH")), "USD_OIS_CASH", thirty360, modified, annual, 2, usgb,
        thirty360, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FF EFFECTIVE"), usgb);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_TENOR_SWAP")), "USD_TENOR_SWAP", act360, modified, quarterly, 2,
        ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD FF 3m"), usgb, act360, modified, quarterly, 2,
        ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD LIBOR 3m"), usgb);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_SWAPTION")), "USD_SWAPTION", true);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_GENERIC_CASH")), "USD_GENERIC_CASH", act360, following,
        Period.ofDays(7), 2, true, null);

    //Identifiers for external data 
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP1D"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP1D")),
        "USDCASHP1D", act360, following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP1M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP1M")),
        "USDCASHP1M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP2M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP2M")),
        "USDCASHP2M", act360, modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP3M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP3M")),
        "USDCASHP3M", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP4M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP4M")),
        "USDCASHP4M", act360, modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP5M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP5M")),
        "USDCASHP5M", act360, modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP6M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP6M")),
        "USDCASHP6M", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP7M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP7M")),
        "USDCASHP7M", act360, modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP8M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP8M")),
        "USDCASHP8M", act360, modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP9M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP9M")),
        "USDCASHP9M", act360, modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP10M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP10M")),
        "USDCASHP10M", act360, modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP11M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP11M")),
        "USDCASHP11M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDCASHP12M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDCASHP12M")),
        "USDCASHP12M", act360, modified, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP2Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP2Y")),
        "USDSWAPP2Y", thirty360, modified, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP3Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP3Y")),
        "USDSWAPP3Y", thirty360, modified, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP4Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP4Y")),
        "USDSWAPP4Y", thirty360, modified, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP5Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP5Y")),
        "USDSWAPP5Y", thirty360, modified, Period.ofYears(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP6Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP6Y")),
        "USDSWAPP6Y", thirty360, modified, Period.ofYears(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP7Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP7Y")),
        "USDSWAPP7Y", thirty360, modified, Period.ofYears(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP8Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP8Y")),
        "USDSWAPP8Y", thirty360, modified, Period.ofYears(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP9Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP9Y")),
        "USDSWAPP9Y", thirty360, modified, Period.ofYears(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP10Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP10Y")),
        "USDSWAPP10Y", thirty360, modified, Period.ofYears(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP12Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP12Y")),
        "USDSWAPP12Y", thirty360, modified, Period.ofYears(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP15Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP15Y")),
        "USDSWAPP15Y", thirty360, modified, Period.ofYears(15), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP20Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP20Y")),
        "USDSWAPP20Y", thirty360, modified, Period.ofYears(20), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP25Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP25Y")),
        "USDSWAPP25Y", thirty360, modified, Period.ofYears(25), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP30Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP30Y")),
        "USDSWAPP30Y", thirty360, modified, Period.ofYears(30), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP40Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP40Y")),
        "USDSWAPP40Y", thirty360, modified, Period.ofYears(40), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP50Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP50Y")),
        "USDSWAPP50Y", thirty360, modified, Period.ofYears(50), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USDSWAPP80Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDSWAPP80Y")),
        "USDSWAPP80Y", thirty360, modified, Period.ofYears(80), 2, false, null);
  }

  public static void addCAPMConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM")), "USD_CAPM",
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("US0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "USDLIBORP3M")),
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SPX Index"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "SPX")));
  }

  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "US_TREASURY_BOND_CONVENTION")), "US_TREASURY_BOND_CONVENTION", true,
        true, 0, 1, true);
  }

  //TODO need to get the correct convention
  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "US_CORPORATE_BOND_CONVENTION")), "US_CORPORATE_BOND_CONVENTION", true,
        true, 0, 1, true);
  }

  public static void addBondFutureConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_BOND_FUTURE_DELIVERABLE_CONVENTION")),
        "USD_BOND_FUTURE_DELIVERABLE_CONVENTION", true, true, 0, 0, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        SimpleYieldConvention.MONEY_MARKET);
  }
}

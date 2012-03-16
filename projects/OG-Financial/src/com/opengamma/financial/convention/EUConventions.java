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
 * Standard conventions for EUR.
 */
public class EUConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    //TODO holiday associated with EUR swaps is TARGET
    final ExternalId eu = RegionUtils.financialRegionId("EU");

    //EURO LIBOR
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR O/N")), "EUR LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR T/N")), "EUR LIBOR T/N", act360,
        following, Period.ofDays(1), 1, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 1w")), "EUR LIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 2w")), "EUR LIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 1m")), "EUR LIBOR 1m", act360,
        modified, Period.ofMonths(1), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 2m")), "EUR LIBOR 2m", act360,
        modified, Period.ofMonths(2), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 3m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURLIBORP3M")), "EUR LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 4m")), "EUR LIBOR 4m", act360,
        modified, Period.ofMonths(4), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 5m")), "EUR LIBOR 5m", act360,
        modified, Period.ofMonths(5), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURLIBORP6M")), "EUR LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 7m")), "EUR LIBOR 7m", act360,
        modified, Period.ofMonths(7), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 8m")), "EUR LIBOR 8m", act360,
        modified, Period.ofMonths(8), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 9m")), "EUR LIBOR 9m", act360,
        modified, Period.ofMonths(9), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 10m")), "EUR LIBOR 10m", act360,
        modified, Period.ofMonths(10), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 11m")), "EUR LIBOR 11m", act360,
        modified, Period.ofMonths(11), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 12m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURLIBORP12M")), "EUR LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);
    // EURIBOR
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 1w")),
        "EURIBOR 1w", act360, following, Period.ofDays(7), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 2w")),
        "EURIBOR 2w", act360, following, Period.ofDays(14), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR003W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 3w")),
        "EURIBOR 3w", act360, following, Period.ofDays(21), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 1m")),
        "EURIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 2m")),
        "EURIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 3m")),
        "EURIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 4m")),
        "EURIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 5m")),
        "EURIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 6m")),
        "EURIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 7m")),
        "EURIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 8m")),
        "EURIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, eu);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 9m")),
        "EURIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 10m")), "EURIBOR 10m", act360,
        modified, Period.ofMonths(10), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 11m")), "EURIBOR 11m", act360,
        modified, Period.ofMonths(11), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 12m")), "EURIBOR 12m", act360,
        modified, Period.ofMonths(12), 2, false, eu);

    // Deposit
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1d")), "EUR DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2d")), "EUR DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3d")), "EUR DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1w")), "EUR DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2w")), "EUR DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3w")), "EUR DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1m")), "EUR DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2m")), "EUR DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3m")), "EUR DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 4m")), "EUR DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 5m")), "EUR DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 6m")), "EUR DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 7m")), "EUR DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 8m")), "EUR DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 9m")), "EUR DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 10m")), "EUR DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 11m")), "EUR DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1y")), "EUR DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2y")), "EUR DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3y")), "EUR DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 4y")), "EUR DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, eu);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 5y")), "EUR DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, eu);

    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = annual;
    final DayCount euriborDayCount = act360;
    final int publicationLagON = 0;

    // EURIBOR
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_IBOR_INDEX")), "EUR_IBOR_INDEX", euriborDayCount, modified, 2, true);
    // FRA
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_3M_FRA")), "EUR_3M_FRA", thirty360, modified, quarterly, 2, eu,
        act360, modified, quarterly, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 3m"), eu, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_6M_FRA")), "EUR_6M_FRA", thirty360, modified, annual, 2, eu, act360,
        modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 6m"), eu, true);
    // IRS
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_SWAP")), "EUR_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, euriborDayCount, modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 6m"), eu, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_3M_SWAP")), "EUR_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, act360, modified, quarterly, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 3m"), eu, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_6M_SWAP")), "EUR_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 6m"), eu, true);
    // IR FUTURES
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_IR_FUTURE")), "EUR_IR_FUTURE", euriborDayCount, modified,
        Period.ofMonths(3), 2, true, null);
    // EONIA
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EONIA Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR EONIA")),
        "EUR EONIA", act360, modified, Period.ofDays(1), 0, false, eu, publicationLagON);
    // OIS - EONIA
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_OIS_SWAP")), "EUR_OIS_SWAP", act360, modified, annual, 2, eu,
        act360, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR EONIA"), eu, true, publicationLagON);

    // TODO: Add all ISDA fixing
    final int[] isdaFixTenor = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
    // ISDA fixing Euribor 10.00 Frankfurt
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_ISDAFIX_EURIBOR10_1Y"), SecurityUtils.ricSecurityId("EURSFIXA1Y=")),
        "EUR_ISDAFIX_EURIBOR10_1Y", swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, quarterly, 2,
        ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 3m"), eu, true, Period.ofYears(1));
    for (int looptenor = 0; looptenor < isdaFixTenor.length; looptenor++) {
      final String tenorString = isdaFixTenor[looptenor] + "Y";
      conventionMaster.addConventionBundle(
          ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_ISDAFIX_EURIBOR10_" + tenorString), SecurityUtils.ricSecurityId("EURSFIXA" + tenorString + "=")),
          "EUR_ISDAFIX_EURIBOR10_" + tenorString, swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2,
          ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 6m"), eu, true, Period.ofYears(isdaFixTenor[looptenor]));
    }
    // ISDA fixing Euro Libor 10.00 London
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_ISDAFIX_EUROLIBOR10_1Y"), SecurityUtils.ricSecurityId("EURSFIXB1Y=")), "EUR_ISDAFIX_EUROLIBOR10_1Y",
        swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, quarterly, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 3m"),
        eu, true);
    for (int looptenor = 0; looptenor < isdaFixTenor.length; looptenor++) {
      final String tenorString = isdaFixTenor[looptenor] + "Y";
      conventionMaster.addConventionBundle(
          ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_ISDAFIX_EUROLIBOR10_" + tenorString), SecurityUtils.ricSecurityId("EURSFIXB" + tenorString + "=")),
          "EUR_ISDAFIX_EUROLIBOR10_" + tenorString, swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2,
          ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 6m"), eu, true);
    }

    //Identifiers for external data // TODO where is this used?
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP1D"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP1D")),
        "EURCASHP1D", act360, following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP1M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP1M")),
        "EURCASHP1M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP2M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP2M")),
        "EURCASHP2M", act360, modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP3M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP3M")),
        "EURCASHP3M", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP4M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP4M")),
        "EURCASHP4M", act360, modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP5M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP5M")),
        "EURCASHP5M", act360, modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP6M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP6M")),
        "EURCASHP6M", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP7M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP7M")),
        "EURCASHP7M", act360, modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP8M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP8M")),
        "EURCASHP8M", act360, modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP9M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP9M")),
        "EURCASHP9M", act360, modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP10M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP10M")),
        "EURCASHP10M", act360, modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP11M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP11M")),
        "EURCASHP11M", act360, modified, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP12M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP12M")),
        "EURCASHP12M", act360, modified, Period.ofMonths(12), 2, false, null);
  }

}

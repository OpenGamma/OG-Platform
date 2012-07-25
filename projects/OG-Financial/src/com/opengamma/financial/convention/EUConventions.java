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
    final ExternalId eu = ExternalSchemes.financialRegionId("EU");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    //EURO LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU00O/N Index"), simpleNameSecurityId("EUR LIBOR O/N")),
        "EUR LIBOR O/N", act360, following, Period.ofDays(1), 0, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU00T/N Index"), simpleNameSecurityId("EUR LIBOR T/N")),
        "EUR LIBOR T/N", act360, following, Period.ofDays(1), 1, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0001W Index"), simpleNameSecurityId("EUR LIBOR 1w")),
        "EUR LIBOR 1w", act360, following, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0002W Index"), simpleNameSecurityId("EUR LIBOR 2w")),
        "EUR LIBOR 2w", act360, following, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0001M Index"), simpleNameSecurityId("EUR LIBOR 1m")),
        "EUR LIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0002M Index"), simpleNameSecurityId("EUR LIBOR 2m")),
        "EUR LIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("EU0003M Index"), simpleNameSecurityId("EUR LIBOR 3m")), 
        "EUR LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0004M Index"), simpleNameSecurityId("EUR LIBOR 4m")),
        "EUR LIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0005M Index"), simpleNameSecurityId("EUR LIBOR 5m")),
        "EUR LIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("EU0006M Index"), simpleNameSecurityId("EUR LIBOR 6m")), 
        "EUR LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0007M Index"), simpleNameSecurityId("EUR LIBOR 7m")),
        "EUR LIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0008M Index"), simpleNameSecurityId("EUR LIBOR 8m")),
        "EUR LIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0009M Index"), simpleNameSecurityId("EUR LIBOR 9m")),
        "EUR LIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0010M Index"), simpleNameSecurityId("EUR LIBOR 10m")),
        "EUR LIBOR 10m", act360, modified, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0011M Index"), simpleNameSecurityId("EUR LIBOR 11m")),
        "EUR LIBOR 11m", act360, modified, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("EU0012M Index"), simpleNameSecurityId("EUR LIBOR 12m")), 
        "EUR LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);
    // EURIBOR
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR001W Index"), simpleNameSecurityId("EURIBOR 1w")),
        "EURIBOR 1w", act360, following, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR002W Index"), simpleNameSecurityId("EURIBOR 2w")),
        "EURIBOR 2w", act360, following, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR003W Index"), simpleNameSecurityId("EURIBOR 3w")),
        "EURIBOR 3w", act360, following, Period.ofDays(21), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR001M Index"), simpleNameSecurityId("EURIBOR 1m")),
        "EURIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR002M Index"), simpleNameSecurityId("EURIBOR 2m")),
        "EURIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("EUR003M Index"), ExternalSchemes.ricSecurityId("EURIBOR3MD="),
            simpleNameSecurityId("EURIBOR 3m")), "EURIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR004M Index"), simpleNameSecurityId("EURIBOR 4m")),
        "EURIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR005M Index"), simpleNameSecurityId("EURIBOR 5m")),
        "EURIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("EUR006M Index"), ExternalSchemes.ricSecurityId("EURIBOR6MD="),
            simpleNameSecurityId("EURIBOR 6m")), "EURIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR007M Index"), simpleNameSecurityId("EURIBOR 7m")),
        "EURIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR008M Index"), simpleNameSecurityId("EURIBOR 8m")),
        "EURIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR009M Index"), simpleNameSecurityId("EURIBOR 9m")),
        "EURIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR010M Index"), simpleNameSecurityId("EURIBOR 10m")),
        "EURIBOR 10m", act360, modified, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR011M Index"), simpleNameSecurityId("EURIBOR 11m")),
        "EURIBOR 11m", act360, modified, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR012M Index"), simpleNameSecurityId("EURIBOR 12m")),
        "EURIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);

    // Deposit
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR1T Curncy"), simpleNameSecurityId("EUR DEPOSIT 1d")),
        "EUR DEPOSIT 1d", act360, following, Period.ofDays(1), 0, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR2T Curncy"), simpleNameSecurityId("EUR DEPOSIT 2d")),
        "EUR DEPOSIT 2d", act360, following, Period.ofDays(1), 1, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR3T Curncy"), simpleNameSecurityId("EUR DEPOSIT 3d")),
        "EUR DEPOSIT 3d", act360, following, Period.ofDays(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR1Z Curncy"), simpleNameSecurityId("EUR DEPOSIT 1w")),
        "EUR DEPOSIT 1w", act360, following, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR2Z Curncy"), simpleNameSecurityId("EUR DEPOSIT 2w")),
        "EUR DEPOSIT 2w", act360, following, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR3Z Curncy"), simpleNameSecurityId("EUR DEPOSIT 3w")),
        "EUR DEPOSIT 3w", act360, following, Period.ofDays(21), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRA Curncy"), simpleNameSecurityId("EUR DEPOSIT 1m")),
        "EUR DEPOSIT 1m", act360, following, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRB Curncy"), simpleNameSecurityId("EUR DEPOSIT 2m")),
        "EUR DEPOSIT 2m", act360, following, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRC Curncy"), simpleNameSecurityId("EUR DEPOSIT 3m")),
        "EUR DEPOSIT 3m", act360, following, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRD Curncy"), simpleNameSecurityId("EUR DEPOSIT 4m")),
        "EUR DEPOSIT 4m", act360, following, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRE Curncy"), simpleNameSecurityId("EUR DEPOSIT 5m")),
        "EUR DEPOSIT 5m", act360, following, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRF Curncy"), simpleNameSecurityId("EUR DEPOSIT 6m")),
        "EUR DEPOSIT 6m", act360, following, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRG Curncy"), simpleNameSecurityId("EUR DEPOSIT 7m")),
        "EUR DEPOSIT 7m", act360, following, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRH Curncy"), simpleNameSecurityId("EUR DEPOSIT 8m")),
        "EUR DEPOSIT 8m", act360, following, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRI Curncy"), simpleNameSecurityId("EUR DEPOSIT 9m")),
        "EUR DEPOSIT 9m", act360, following, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRJ Curncy"), simpleNameSecurityId("EUR DEPOSIT 10m")),
        "EUR DEPOSIT 10m", act360, following, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRK Curncy"), simpleNameSecurityId("EUR DEPOSIT 11m")),
        "EUR DEPOSIT 11m", act360, following, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR1 Curncy"), simpleNameSecurityId("EUR DEPOSIT 1y")),
        "EUR DEPOSIT 1y", act360, following, Period.ofYears(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR2 Curncy"), simpleNameSecurityId("EUR DEPOSIT 2y")),
        "EUR DEPOSIT 2y", act360, following, Period.ofYears(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR3 Curncy"), simpleNameSecurityId("EUR DEPOSIT 3y")),
        "EUR DEPOSIT 3y", act360, following, Period.ofYears(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR4 Curncy"), simpleNameSecurityId("EUR DEPOSIT 4y")),
        "EUR DEPOSIT 4y", act360, following, Period.ofYears(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR5 Curncy"), simpleNameSecurityId("EUR DEPOSIT 5y")),
        "EUR DEPOSIT 5y", act360, following, Period.ofYears(5), 2, false, eu);

    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = annual;
    final DayCount euriborDayCount = act360;
    final int publicationLagON = 0;

    // EURIBOR
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_IBOR_INDEX")), "EUR_IBOR_INDEX", euriborDayCount, modified, 2, true);
    // FRA
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_3M_FRA")), "EUR_3M_FRA", thirty360, modified, quarterly, 2, eu, act360,
        modified, quarterly, 2, simpleNameSecurityId("EURIBOR 3m"), eu, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_6M_FRA")), "EUR_6M_FRA", thirty360, modified, annual, 2, eu, act360, modified,
        semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"), eu, true);
    // IRS
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_SWAP")), "EUR_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, euriborDayCount, modified, semiAnnual, 2, simpleNameSecurityId("EUR LIBOR 6m"), eu, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_3M_SWAP")), "EUR_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, act360, modified, quarterly, 2, simpleNameSecurityId("EURIBOR 3m"), eu, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_6M_SWAP")), "EUR_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"), eu, true);
    // IR FUTURES
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_IR_FUTURE")), "EUR_IR_FUTURE", euriborDayCount, modified, Period.ofMonths(3),
        2, true, null);
    // EONIA
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EONIA Index"), simpleNameSecurityId("EUR EONIA")),
        "EUR EONIA", act360, modified, Period.ofDays(1), 0, false, eu, publicationLagON);
    // OIS - EONIA
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_OIS_SWAP")), "EUR_OIS_SWAP", act360, modified, annual, 2, eu, act360, modified,
        annual, 2, simpleNameSecurityId("EUR EONIA"), eu, true, publicationLagON);

    // TODO: Add all ISDA fixing
    final int[] isdaFixTenor = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
    // ISDA fixing Euribor 10.00 Frankfurt
    utils.addConventionBundle(
        ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EURIBOR10_1Y"), ExternalSchemes.ricSecurityId("EURSFIXA1Y="),
            bloombergTickerSecurityId("EIISDA01 Index")), "EUR_ISDAFIX_EURIBOR10_1Y", swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified,
        quarterly, 2, simpleNameSecurityId("EURIBOR 3m"), eu, true, Period.ofYears(1));
    for (int looptenor = 0; looptenor < isdaFixTenor.length; looptenor++) {
      final String tenorString = isdaFixTenor[looptenor] + "Y";
      final String tenorStringBbg = String.format("%02d", isdaFixTenor[looptenor]);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EURIBOR10_" + tenorString),
          ExternalSchemes.ricSecurityId("EURSFIXA" + tenorString + "="), bloombergTickerSecurityId("EIISDA" + tenorStringBbg + " Index")), "EUR_ISDAFIX_EURIBOR10_" + tenorString,
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"),
          eu, true, Period.ofYears(isdaFixTenor[looptenor]));
    }
    // ISDA fixing Euro Libor 10.00 London
    utils.addConventionBundle(
        ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EUROLIBOR10_1Y"), ExternalSchemes.ricSecurityId("EURSFIXB1Y="),
            bloombergTickerSecurityId("ELISDA01 Index")), "EUR_ISDAFIX_EUROLIBOR10_1Y", swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified,
        quarterly, 2, simpleNameSecurityId("EUR LIBOR 3m"), eu, true);
    for (int looptenor = 0; looptenor < isdaFixTenor.length; looptenor++) {
      final String tenorString = isdaFixTenor[looptenor] + "Y";
      final String tenorStringBbg = String.format("%02d", isdaFixTenor[looptenor]);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EUROLIBOR10_" + tenorString),
          ExternalSchemes.ricSecurityId("EURSFIXB" + tenorString + "="), bloombergTickerSecurityId("ELISDA" + tenorStringBbg + " Index")), "EUR_ISDAFIX_EUROLIBOR10_" + tenorString,
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, simpleNameSecurityId("EUR LIBOR 6m"),
          eu, true);
    }
  }

}

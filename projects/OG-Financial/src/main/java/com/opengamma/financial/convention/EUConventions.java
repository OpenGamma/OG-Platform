/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.core.id.ExternalSchemes.icapSecurityId;
import static com.opengamma.core.id.ExternalSchemes.tullettPrebonSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Standard conventions for EUR.
 */
public class EUConventions {

  /**
   * Adds conventions for deposit, Libor and Euribor fixings, swaps, FRAs and IR futures.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    //TODO holiday associated with EUR swaps is TARGET
    final ExternalId eu = ExternalSchemes.financialRegionId("EU");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    //EURO LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU00O/N Index"), simpleNameSecurityId("EUR LIBOR O/N"),
        tullettPrebonSecurityId("ASLIBEULONL")),
        "EUR LIBOR O/N", act360, following, Period.ofDays(1), 0, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU00T/N Index"), simpleNameSecurityId("EUR LIBOR T/N")),
        "EUR LIBOR T/N", act360, following, Period.ofDays(1), 1, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0001W Index"), simpleNameSecurityId("EUR LIBOR 1w"),
        tullettPrebonSecurityId("ASLIBEUL1WL")),
        "EUR LIBOR 1w", act360, following, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0002W Index"), simpleNameSecurityId("EUR LIBOR 2w"),
        tullettPrebonSecurityId("ASLIBEUL2WL")),
        "EUR LIBOR 2w", act360, following, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0001M Index"), simpleNameSecurityId("EUR LIBOR 1m"),
        tullettPrebonSecurityId("ASLIBEUL01L")),
        "EUR LIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0002M Index"), simpleNameSecurityId("EUR LIBOR 2m"),
        tullettPrebonSecurityId("ASLIBEUL02L")),
        "EUR LIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0003M Index"), simpleNameSecurityId("EUR LIBOR 3m"),
        tullettPrebonSecurityId("ASLIBEUL03L")),
        "EUR LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0004M Index"), simpleNameSecurityId("EUR LIBOR 4m"),
        tullettPrebonSecurityId("ASLIBEUL04L")),
        "EUR LIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0005M Index"), simpleNameSecurityId("EUR LIBOR 5m"),
        tullettPrebonSecurityId("ASLIBEUL05L")),
        "EUR LIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0006M Index"), simpleNameSecurityId("EUR LIBOR 6m"),
        tullettPrebonSecurityId("ASLIBEUL06L")),
        "EUR LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0007M Index"), simpleNameSecurityId("EUR LIBOR 7m"),
        tullettPrebonSecurityId("ASLIBEUL07L")),
        "EUR LIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0008M Index"), simpleNameSecurityId("EUR LIBOR 8m"),
        tullettPrebonSecurityId("ASLIBEUL08L")),
        "EUR LIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0009M Index"), simpleNameSecurityId("EUR LIBOR 9m"),
        tullettPrebonSecurityId("ASLIBEUL09L")),
        "EUR LIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0010M Index"), simpleNameSecurityId("EUR LIBOR 10m"),
        tullettPrebonSecurityId("ASLIBEUL10L")),
        "EUR LIBOR 10m", act360, modified, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0011M Index"), simpleNameSecurityId("EUR LIBOR 11m"),
        tullettPrebonSecurityId("ASLIBEUL11L")),
        "EUR LIBOR 11m", act360, modified, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EU0012M Index"), simpleNameSecurityId("EUR LIBOR 12m"),
        tullettPrebonSecurityId("ASLIBEUL12L")),
        "EUR LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);
    // EURIBOR
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR001W Index"), simpleNameSecurityId("EURIBOR 1w"),
        tullettPrebonSecurityId("ASLIBEUR1WL")),
        "EURIBOR 1w", act360, following, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR002W Index"), simpleNameSecurityId("EURIBOR 2w"),
        tullettPrebonSecurityId("ASLIBEUR2WL")),
        "EURIBOR 2w", act360, following, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR003W Index"), simpleNameSecurityId("EURIBOR 3w"),
        tullettPrebonSecurityId("ASLIBEUR3WL")),
        "EURIBOR 3w", act360, following, Period.ofDays(21), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR001M Index"), simpleNameSecurityId("EURIBOR 1m"),
        tullettPrebonSecurityId("ASLIBEUR01L")),
        "EURIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR002M Index"), simpleNameSecurityId("EURIBOR 2m"),
        tullettPrebonSecurityId("ASLIBEUR02L")),
        "EURIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR003M Index"), ExternalSchemes.ricSecurityId("EURIBOR3MD="),
            simpleNameSecurityId("EURIBOR 3m"), tullettPrebonSecurityId("ASLIBEUR03L")),
            "EURIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR004M Index"), simpleNameSecurityId("EURIBOR 4m"),
        tullettPrebonSecurityId("ASLIBEUR04L")),
        "EURIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR005M Index"), simpleNameSecurityId("EURIBOR 5m"),
        tullettPrebonSecurityId("ASLIBEUR05L")),
        "EURIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR006M Index"), ExternalSchemes.ricSecurityId("EURIBOR6MD="),
            simpleNameSecurityId("EURIBOR 6m"), tullettPrebonSecurityId("ASLIBEUR06L")),
            "EURIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR007M Index"), simpleNameSecurityId("EURIBOR 7m"),
        tullettPrebonSecurityId("ASLIBEUR07L")),
        "EURIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR008M Index"), simpleNameSecurityId("EURIBOR 8m"),
        tullettPrebonSecurityId("ASLIBEUR08L")),
        "EURIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR009M Index"), simpleNameSecurityId("EURIBOR 9m"),
        tullettPrebonSecurityId("ASLIBEUR09L")),
        "EURIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR010M Index"), simpleNameSecurityId("EURIBOR 10m"),
        tullettPrebonSecurityId("ASLIBEUR10L")),
        "EURIBOR 10m", act360, modified, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR011M Index"), simpleNameSecurityId("EURIBOR 11m"),
        tullettPrebonSecurityId("ASLIBEUR11L")),
        "EURIBOR 11m", act360, modified, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUR012M Index"), simpleNameSecurityId("EURIBOR 12m"),
        tullettPrebonSecurityId("ASLIBEUR12L")),
        "EURIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);

    // Deposit
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR1T Curncy"), simpleNameSecurityId("EUR DEPOSIT 1d")),
        "EUR DEPOSIT 1d", act360, following, Period.ofDays(1), 0, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR2T Curncy"), simpleNameSecurityId("EUR DEPOSIT 2d")),
        "EUR DEPOSIT 2d", act360, following, Period.ofDays(1), 1, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR3T Curncy"), simpleNameSecurityId("EUR DEPOSIT 3d")),
        "EUR DEPOSIT 3d", act360, following, Period.ofDays(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR1Z Curncy"), simpleNameSecurityId("EUR DEPOSIT 1w"),
        icapSecurityId("EUR_1W"), tullettPrebonSecurityId("MNDEPEURSPT01W")), "EUR DEPOSIT 1w", act360, following, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR2Z Curncy"), simpleNameSecurityId("EUR DEPOSIT 2w"),
        icapSecurityId("EUR_2W"), tullettPrebonSecurityId("MNDEPEURSPT02W")), "EUR DEPOSIT 2w", act360, following, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR3Z Curncy"), simpleNameSecurityId("EUR DEPOSIT 3w"),
        icapSecurityId("EUR_3W"), tullettPrebonSecurityId("MNDEPEURSPT03W")), "EUR DEPOSIT 3w", act360, following, Period.ofDays(21), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRA Curncy"), simpleNameSecurityId("EUR DEPOSIT 1m"),
        icapSecurityId("EUR_1M"), tullettPrebonSecurityId("MNDEPEURSPT01M")), "EUR DEPOSIT 1m", act360, following, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRB Curncy"), simpleNameSecurityId("EUR DEPOSIT 2m"),
        icapSecurityId("EUR_2M"), tullettPrebonSecurityId("MNDEPEURSPT02M")), "EUR DEPOSIT 2m", act360, following, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRC Curncy"), simpleNameSecurityId("EUR DEPOSIT 3m"),
        icapSecurityId("EUR_3M"), tullettPrebonSecurityId("MNDEPEURSPT03M")), "EUR DEPOSIT 3m", act360, following, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRD Curncy"), simpleNameSecurityId("EUR DEPOSIT 4m"),
        icapSecurityId("EUR_4M"), tullettPrebonSecurityId("MNDEPEURSPT04M")), "EUR DEPOSIT 4m", act360, following, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRE Curncy"), simpleNameSecurityId("EUR DEPOSIT 5m"),
        icapSecurityId("EUR_5M"), tullettPrebonSecurityId("MNDEPEURSPT05M")), "EUR DEPOSIT 5m", act360, following, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRF Curncy"), simpleNameSecurityId("EUR DEPOSIT 6m"),
        icapSecurityId("EUR_6M"), tullettPrebonSecurityId("MNDEPEURSPT06M")), "EUR DEPOSIT 6m", act360, following, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRG Curncy"), simpleNameSecurityId("EUR DEPOSIT 7m"),
        icapSecurityId("EUR_7M"), tullettPrebonSecurityId("MNDEPEURSPT07M")), "EUR DEPOSIT 7m", act360, following, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRH Curncy"), simpleNameSecurityId("EUR DEPOSIT 8m"),
        icapSecurityId("EUR_8M"), tullettPrebonSecurityId("MNDEPEURSPT08M")), "EUR DEPOSIT 8m", act360, following, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRI Curncy"), simpleNameSecurityId("EUR DEPOSIT 9m"),
        icapSecurityId("EUR_9M"), tullettPrebonSecurityId("MNDEPEURSPT09M")), "EUR DEPOSIT 9m", act360, following, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRJ Curncy"), simpleNameSecurityId("EUR DEPOSIT 10m"),
        icapSecurityId("EUR_10M"), tullettPrebonSecurityId("MNDEPEURSPT10M")), "EUR DEPOSIT 10m", act360, following, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDRK Curncy"), simpleNameSecurityId("EUR DEPOSIT 11m"),
        icapSecurityId("EUR_11M"), tullettPrebonSecurityId("MNDEPEURSPT11M")), "EUR DEPOSIT 11m", act360, following, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("EUDR1 Curncy"), simpleNameSecurityId("EUR DEPOSIT 1y"),
        icapSecurityId("EUR_12M"), tullettPrebonSecurityId("MNDEPEURSPT12M")), "EUR DEPOSIT 1y", act360, following, Period.ofYears(1), 2, false, eu);
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
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String tenorStringBbg = String.format("%02d", element);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EURIBOR10_" + tenorString),
          ExternalSchemes.ricSecurityId("EURSFIXA" + tenorString + "="), bloombergTickerSecurityId("EIISDA" + tenorStringBbg + " Index")), "EUR_ISDAFIX_EURIBOR10_" + tenorString,
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"),
          eu, true, Period.ofYears(element));
    }
    // ISDA fixing Euro Libor 10.00 London
    utils.addConventionBundle(
        ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EUROLIBOR10_1Y"), ExternalSchemes.ricSecurityId("EURSFIXB1Y="),
            bloombergTickerSecurityId("ELISDA01 Index")), "EUR_ISDAFIX_EUROLIBOR10_1Y", swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified,
        quarterly, 2, simpleNameSecurityId("EUR LIBOR 3m"), eu, true);
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String tenorStringBbg = String.format("%02d", element);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_ISDAFIX_EUROLIBOR10_" + tenorString),
          ExternalSchemes.ricSecurityId("EURSFIXB" + tenorString + "="), bloombergTickerSecurityId("ELISDA" + tenorStringBbg + " Index")), "EUR_ISDAFIX_EUROLIBOR10_" + tenorString,
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, simpleNameSecurityId("EUR LIBOR 6m"),
          eu, true);
    }
  }

}

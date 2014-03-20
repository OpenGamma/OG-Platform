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
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of USD instruments.
 */
public class USConventions {

  /**
   * Adds conventions for deposit, Libor fixings, swaps, FRAs and IR futures.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency annual = PeriodFrequency.ANNUAL;
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId usgb = ExternalSchemes.financialRegionId("US+GB");
    final ExternalId us = ExternalSchemes.financialRegionId("US");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    // LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US00O/N Index"), simpleNameSecurityId("USD LIBOR O/N")),
        "USD LIBOR O/N", act360, following, Period.ofDays(1), 0, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US00T/N Index"), simpleNameSecurityId("USD LIBOR T/N")),
        "USD LIBOR T/N", act360, following, Period.ofDays(1), 1, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0001W Index"), simpleNameSecurityId("USD LIBOR 1w"),
        tullettPrebonSecurityId("ASLIBUSD1WL")), "USD LIBOR 1w", act360, following, Period.ofDays(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0002W Index"), simpleNameSecurityId("USD LIBOR 2w"),
        tullettPrebonSecurityId("ASLIBUSD2WL")), "USD LIBOR 2w", act360, following, Period.ofDays(14), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0001M Index"), simpleNameSecurityId("USD LIBOR 1m"),
        tullettPrebonSecurityId("ASLIBUSD01L")), "USD LIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0002M Index"), simpleNameSecurityId("USD LIBOR 2m"),
        tullettPrebonSecurityId("ASLIBUSD02L")), "USD LIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0003M Index"), simpleNameSecurityId("USD LIBOR 3m"),
        ExternalSchemes.ricSecurityId("USD3MFSR="), tullettPrebonSecurityId("ASLIBUSD03L")),
        "USD LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0004M Index"), simpleNameSecurityId("USD LIBOR 4m"),
        tullettPrebonSecurityId("ASLIBUSD04L")), "USD LIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0005M Index"), simpleNameSecurityId("USD LIBOR 5m"),
        tullettPrebonSecurityId("ASLIBUSD05L")), "USD LIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0006M Index"), simpleNameSecurityId("USD LIBOR 6m"),
        tullettPrebonSecurityId("ASLIBUSD06L")), "USD LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0007M Index"), simpleNameSecurityId("USD LIBOR 7m"),
        tullettPrebonSecurityId("ASLIBUSD07L")), "USD LIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0008M Index"), simpleNameSecurityId("USD LIBOR 8m"),
        tullettPrebonSecurityId("ASLIBUSD08L")), "USD LIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0009M Index"), simpleNameSecurityId("USD LIBOR 9m"),
        tullettPrebonSecurityId("ASLIBUSD09L")), "USD LIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0010M Index"), simpleNameSecurityId("USD LIBOR 10m"),
        tullettPrebonSecurityId("ASLIBUSD10L")), "USD LIBOR 10m", act360, modified, Period.ofMonths(10), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0011M Index"), simpleNameSecurityId("USD LIBOR 11m"),
        tullettPrebonSecurityId("ASLIBUSD11L")), "USD LIBOR 11m", act360, modified, Period.ofMonths(11), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0012M Index"), simpleNameSecurityId("USD LIBOR 12m"),
        tullettPrebonSecurityId("ASLIBUSD12L")), "USD LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, us);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR1T Curncy"), simpleNameSecurityId("USD DEPOSIT 1d")),
        "USD DEPOSIT 1d", act360, following, Period.ofDays(1), 0, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR2T Curncy"), simpleNameSecurityId("USD DEPOSIT 2d")),
        "USD DEPOSIT 2d", act360, following, Period.ofDays(1), 1, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR3T Curncy"), simpleNameSecurityId("USD DEPOSIT 3d")),
        "USD DEPOSIT 3d", act360, following, Period.ofDays(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR1Z Curncy"), simpleNameSecurityId("USD DEPOSIT 1w"),
        tullettPrebonSecurityId("ASDEPUSDSPT01W"), icapSecurityId("USD_1W")), "USD DEPOSIT 1w", act360, following, Period.ofDays(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR2Z Curncy"), simpleNameSecurityId("USD DEPOSIT 2w"),
        tullettPrebonSecurityId("ASDEPUSDSPT02W"), icapSecurityId("USD_2W")), "USD DEPOSIT 2w", act360, following, Period.ofDays(14), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR3Z Curncy"), simpleNameSecurityId("USD DEPOSIT 3w"),
        tullettPrebonSecurityId("ASDEPUSDSPT03W"), icapSecurityId("USD_3W")), "USD DEPOSIT 3w", act360, following, Period.ofDays(21), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRA Curncy"), simpleNameSecurityId("USD DEPOSIT 1m"),
        tullettPrebonSecurityId("ASDEPUSDSPT01M"), icapSecurityId("USD_1M")), "USD DEPOSIT 1m", act360, following, Period.ofMonths(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRB Curncy"), simpleNameSecurityId("USD DEPOSIT 2m"),
        tullettPrebonSecurityId("ASDEPUSDSPT02M"), icapSecurityId("USD_2M")), "USD DEPOSIT 2m", act360, following, Period.ofMonths(2), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRC Curncy"), simpleNameSecurityId("USD DEPOSIT 3m"),
        tullettPrebonSecurityId("ASDEPUSDSPT03M"), icapSecurityId("USD_3M")), "USD DEPOSIT 3m", act360, following, Period.ofMonths(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRD Curncy"), simpleNameSecurityId("USD DEPOSIT 4m"),
        tullettPrebonSecurityId("ASDEPUSDSPT04M"), icapSecurityId("USD_4M")), "USD DEPOSIT 4m", act360, following, Period.ofMonths(4), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRE Curncy"), simpleNameSecurityId("USD DEPOSIT 5m"),
        tullettPrebonSecurityId("ASDEPUSDSPT05M"), icapSecurityId("USD_5M")), "USD DEPOSIT 5m", act360, following, Period.ofMonths(5), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRF Curncy"), simpleNameSecurityId("USD DEPOSIT 6m"),
        tullettPrebonSecurityId("ASDEPUSDSPT06M"), icapSecurityId("USD_6M")), "USD DEPOSIT 6m", act360, following, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRG Curncy"), simpleNameSecurityId("USD DEPOSIT 7m"),
        tullettPrebonSecurityId("ASDEPUSDSPT07M"), icapSecurityId("USD_7M")), "USD DEPOSIT 7m", act360, following, Period.ofMonths(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRH Curncy"), simpleNameSecurityId("USD DEPOSIT 8m"),
        tullettPrebonSecurityId("ASDEPUSDSPT08M"), icapSecurityId("USD_8M")), "USD DEPOSIT 8m", act360, following, Period.ofMonths(8), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRI Curncy"), simpleNameSecurityId("USD DEPOSIT 9m"),
        tullettPrebonSecurityId("ASDEPUSDSPT09M"), icapSecurityId("USD_9M")), "USD DEPOSIT 9m", act360, following, Period.ofMonths(9), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRJ Curncy"), simpleNameSecurityId("USD DEPOSIT 10m"),
        tullettPrebonSecurityId("ASDEPUSDSPT10M"), icapSecurityId("USD_10M")), "USD DEPOSIT 10m", act360, following, Period.ofMonths(10), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRK Curncy"), simpleNameSecurityId("USD DEPOSIT 11m"),
        tullettPrebonSecurityId("ASDEPUSDSPT11M"), icapSecurityId("USD_11M")), "USD DEPOSIT 11m", act360, following, Period.ofMonths(11), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR1 Curncy"), simpleNameSecurityId("USD DEPOSIT 1y"),
        tullettPrebonSecurityId("ASDEPUSDSPT12M"), icapSecurityId("USD_12M")), "USD DEPOSIT 1y", act360, following, Period.ofYears(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR2 Curncy"), simpleNameSecurityId("USD DEPOSIT 2y")),
        "USD DEPOSIT 2y", act360, following, Period.ofYears(2), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR3 Curncy"), simpleNameSecurityId("USD DEPOSIT 3y")),
        "USD DEPOSIT 3y", act360, following, Period.ofYears(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR4 Curncy"), simpleNameSecurityId("USD DEPOSIT 4y")),
        "USD DEPOSIT 4y", act360, following, Period.ofYears(4), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR5 Curncy"), simpleNameSecurityId("USD DEPOSIT 5y")),
        "USD DEPOSIT 5y", act360, following, Period.ofYears(5), 2, false, us);

    //TODO with improvement in settlement days definition (i.e. including holiday and adjustment) change this
    // should be 2, LON, following
    // holiday for swap should be NY+LON

    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = semiAnnual;
    final DayCount swapFloatDayCount = act360;
    final BusinessDayConvention swapFloatBusinessDay = modified;
    final Frequency swapFloatPaymentFrequency = quarterly;

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_SWAP")), "USD_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, swapFloatPaymentFrequency, 2, simpleNameSecurityId("USD LIBOR 3m"),
        usgb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_3M_SWAP")), "USD_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, quarterly, 2, simpleNameSecurityId("USD LIBOR 3m"), usgb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_6M_SWAP")), "USD_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, semiAnnual, 2, simpleNameSecurityId("USD LIBOR 6m"), usgb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_IR_FUTURE")), "USD_IR_FUTURE", act360, modified, Period.ofMonths(3), 2, false,
        null);

    final int publicationLag = 1;
    // Fed Fund effective
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("FEDL01 Index"), simpleNameSecurityId("USD FF EFFECTIVE")),
        "USD FF EFFECTIVE", act360, following, Period.ofDays(1), 2, false, us, publicationLag);
    // OIS swap
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_OIS_SWAP")), "USD_OIS_SWAP", thirty360, modified, annual, 2, usgb, thirty360,
        modified, annual, 2, simpleNameSecurityId("USD FF EFFECTIVE"), usgb, true, publicationLag);

    // FRA conventions are stored as IRS
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_3M_FRA")), "USD_3M_FRA", thirty360, modified, quarterly, 2, usgb, act360,
        modified, quarterly, 2, simpleNameSecurityId("USD LIBOR 3m"), usgb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_6M_FRA")), "USD_6M_FRA", thirty360, modified, semiAnnual, 2, usgb, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("USD LIBOR 6m"), usgb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_TENOR_SWAP")), "USD_TENOR_SWAP", act360, modified, quarterly, 2,
        simpleNameSecurityId("USD FF 3m"), usgb, act360, modified, quarterly, 2,
        simpleNameSecurityId("USD LIBOR 3m"), usgb);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_SWAPTION")), "USD_SWAPTION", true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_GENERIC_CASH")), "USD_GENERIC_CASH", act360, following, Period.ofDays(7), 2,
        true, null);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_USD_P3M")), IndexType.Libor + "_USD_P3M", thirty360, modified,
        null, 2, false, usgb);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId(IndexType.Libor + "_USD_P6M")), IndexType.Libor + "_USD_P6M", thirty360, modified,
        null, 2, false, usgb);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_BASIS_SWAP")), "USD_BASIS_SWAP", act360, modified, quarterly, 2,
        null, usgb, act360, modified, quarterly, 2, null, usgb);

    // Inflation
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CPURNSA Index"), simpleNameSecurityId("USD CPI")),
        "USD CPI", act360, modified, Period.ofMonths(1), 2, false, us);

    // TODO: Add all ISDA fixing
    final int[] isdaFixTenor = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30 };
    // ISDA fixing 11.00 New-York
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String tenorStringBbg = String.format("%02d", element);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_ISDAFIX_USDLIBOR10_" + tenorString),
          ExternalSchemes.ricSecurityId("USDSFIX" + tenorString + "="), bloombergTickerSecurityId("USSW" + tenorStringBbg + " Curncy")),
          "USSW" + tenorString, swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, us, act360, modified, semiAnnual, 2,
          simpleNameSecurityId("USD LIBOR 3m"), us, true, Period.ofYears(element));
    }

    //Identifiers for external data
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.1M").toBundle(), "IR.SWAP.USD.1M", act360, modified, Period.ofMonths(1), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.6M").toBundle(), "IR.SWAP.USD.6M", act360, modified, Period.ofMonths(6), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.12M").toBundle(), "IR.SWAP.USD.12M", act360, modified, Period.ofMonths(12), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.24M").toBundle(), "IR.SWAP.USD.24M", act360, modified, Period.ofMonths(24), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.36M").toBundle(), "IR.SWAP.USD.36M", act360, modified, Period.ofMonths(36), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.60M").toBundle(), "IR.SWAP.USD.60M", act360, modified, Period.ofMonths(60), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.84M").toBundle(), "IR.SWAP.USD.84M", act360, modified, Period.ofMonths(84), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.120M").toBundle(), "IR.SWAP.USD.120M", act360, modified, Period.ofMonths(120), 2, false, null);
    utils.addConventionBundle(ExternalId.of(FactorExposureData.FACTOR_SCHEME, "IR.SWAP.USD.360M").toBundle(), "IR.SWAP.USD.360M", act360, modified, Period.ofMonths(360), 2, false, null);
  }

  /**
   * @param conventionMaster The convention master, not null
   */
  public static void addCAPMConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_CAPM")), "USD_CAPM",
        ExternalIdBundle.of(bloombergTickerSecurityId("US0003M Index")), ExternalIdBundle.of(bloombergTickerSecurityId("SPX Index")));
  }

  /**
   * Adds conventions for US Treasury bonds,
   * @param conventionMaster The convention master, not null
   */
  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_TREASURY_BOND_CONVENTION")), "US_TREASURY_BOND_CONVENTION", true, true, 0, 1,
        true);
  }

  /**
   * Adds conventions for USD-denominated corporate bonds
   * @param conventionMaster The convention master, not null
   */
  //TODO need to get the correct convention
  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_CORPORATE_BOND_CONVENTION")), "US_CORPORATE_BOND_CONVENTION", true, true, 0, 1,
        true);
  }

  /**
   * Adds conventions for GBP government bonds.
   * @param conventionMaster The convention master, not null
   */
  public static void addInflationBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_INFLATION_BOND_CONVENTION")), "US_INFLATION_BOND_CONVENTION", false,
        true, 6, 0, true);
  }

  /**
   * Add conventions for USD bond futures
   * @param conventionMaster The convention master, not null
   */
  public static void addBondFutureConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_BOND_FUTURE_DELIVERABLE_CONVENTION")),
        "USD_BOND_FUTURE_DELIVERABLE_CONVENTION", true, true, 0, 0, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING,
        SimpleYieldConvention.MONEY_MARKET);
  }

}

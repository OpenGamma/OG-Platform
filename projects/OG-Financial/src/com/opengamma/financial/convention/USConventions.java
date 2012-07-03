/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

/**
 * 
 */
public class USConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
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
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0001W Index"), simpleNameSecurityId("USD LIBOR 1w")),
        "USD LIBOR 1w", act360, following, Period.ofDays(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0002W Index"), simpleNameSecurityId("USD LIBOR 2w")),
        "USD LIBOR 2w", act360, following, Period.ofDays(14), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0001M Index"), simpleNameSecurityId("USD LIBOR 1m")),
        "USD LIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0002M Index"), simpleNameSecurityId("USD LIBOR 2m")),
        "USD LIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, us);
    utils.addConventionBundle(
        ExternalIdBundle.of(bloombergTickerSecurityId("US0003M Index"), simpleNameSecurityId("USD LIBOR 3m"),
            ExternalSchemes.ricSecurityId("USD3MFSR=")), "USD LIBOR 3m", act360, modified, Period.ofMonths(3), 2,
        false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0004M Index"), simpleNameSecurityId("USD LIBOR 4m")),
        "USD LIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0005M Index"), simpleNameSecurityId("USD LIBOR 5m")),
        "USD LIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0006M Index"), simpleNameSecurityId("USD LIBOR 6m")),
        "USD LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0007M Index"), simpleNameSecurityId("USD LIBOR 7m")),
        "USD LIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0008M Index"), simpleNameSecurityId("USD LIBOR 8m")),
        "USD LIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0009M Index"), simpleNameSecurityId("USD LIBOR 9m")),
        "USD LIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0010M Index"), simpleNameSecurityId("USD LIBOR 10m")),
        "USD LIBOR 10m", act360, modified, Period.ofMonths(10), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0011M Index"), simpleNameSecurityId("USD LIBOR 11m")),
        "USD LIBOR 11m", act360, modified, Period.ofMonths(11), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("US0012M Index"), simpleNameSecurityId("USD LIBOR 12m")), 
        "USD LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, us);

    //TODO need to check that these are right for deposit rates
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR1T Curncy"), simpleNameSecurityId("USD DEPOSIT 1d")),
        "USD DEPOSIT 1d", act360, following, Period.ofDays(1), 0, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR2T Curncy"), simpleNameSecurityId("USD DEPOSIT 2d")),
        "USD DEPOSIT 2d", act360, following, Period.ofDays(1), 1, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR3T Curncy"), simpleNameSecurityId("USD DEPOSIT 3d")),
        "USD DEPOSIT 3d", act360, following, Period.ofDays(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR7D Curncy"), simpleNameSecurityId("USD DEPOSIT 1w")),
        "USD DEPOSIT 1w", act360, following, Period.ofDays(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR2Z Curncy"), simpleNameSecurityId("USD DEPOSIT 2w")),
        "USD DEPOSIT 2w", act360, following, Period.ofDays(14), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR3Z Curncy"), simpleNameSecurityId("USD DEPOSIT 3w")),
        "USD DEPOSIT 3w", act360, following, Period.ofDays(21), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRA Curncy"), simpleNameSecurityId("USD DEPOSIT 1m")),
        "USD DEPOSIT 1m", act360, following, Period.ofMonths(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRB Curncy"), simpleNameSecurityId("USD DEPOSIT 2m")),
        "USD DEPOSIT 2m", act360, following, Period.ofMonths(2), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRC Curncy"), simpleNameSecurityId("USD DEPOSIT 3m")),
        "USD DEPOSIT 3m", act360, following, Period.ofMonths(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRD Curncy"), simpleNameSecurityId("USD DEPOSIT 4m")),
        "USD DEPOSIT 4m", act360, following, Period.ofMonths(4), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRE Curncy"), simpleNameSecurityId("USD DEPOSIT 5m")),
        "USD DEPOSIT 5m", act360, following, Period.ofMonths(5), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRF Curncy"), simpleNameSecurityId("USD DEPOSIT 6m")),
        "USD DEPOSIT 6m", act360, following, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRG Curncy"), simpleNameSecurityId("USD DEPOSIT 7m")),
        "USD DEPOSIT 7m", act360, following, Period.ofMonths(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRH Curncy"), simpleNameSecurityId("USD DEPOSIT 8m")),
        "USD DEPOSIT 8m", act360, following, Period.ofMonths(8), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRI Curncy"), simpleNameSecurityId("USD DEPOSIT 9m")),
        "USD DEPOSIT 9m", act360, following, Period.ofMonths(9), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRJ Curncy"), simpleNameSecurityId("USD DEPOSIT 10m")),
        "USD DEPOSIT 10m", act360, following, Period.ofMonths(10), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDRK Curncy"), simpleNameSecurityId("USD DEPOSIT 11m")),
        "USD DEPOSIT 11m", act360, following, Period.ofMonths(11), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("USDR1 Curncy"), simpleNameSecurityId("USD DEPOSIT 1y")),
        "USD DEPOSIT 1y", act360, following, Period.ofYears(1), 2, false, us);
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

    // TODO: Add all ISDA fixing
    final int[] isdaFixTenor = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30 };
    // ISDA fixing 11.00 New-York
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String tenorStringBbg = String.format("%02d", element);
      utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_ISDAFIX_USDLIBOR10_" + tenorString),
          ExternalSchemes.ricSecurityId("USDSFIX" + tenorString + "="), bloombergTickerSecurityId("USISDA" + tenorStringBbg + " Index")), 
          "USD_ISDAFIX_USDLIBOR10_" + tenorString, swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, us, act360, modified, semiAnnual, 2, 
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

  public static void addCAPMConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_CAPM")), "USD_CAPM",
        ExternalIdBundle.of(bloombergTickerSecurityId("US0003M Index")), ExternalIdBundle.of(bloombergTickerSecurityId("SPX Index")));
  }

  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_TREASURY_BOND_CONVENTION")), "US_TREASURY_BOND_CONVENTION", true, true, 0, 1,
        true);
  }

  //TODO need to get the correct convention
  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_CORPORATE_BOND_CONVENTION")), "US_CORPORATE_BOND_CONVENTION", true, true, 0, 1,
        true);
  }

  public static void addBondFutureConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_BOND_FUTURE_DELIVERABLE_CONVENTION")),
        "USD_BOND_FUTURE_DELIVERABLE_CONVENTION", true, true, 0, 0, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"),
        SimpleYieldConvention.MONEY_MARKET);
  }

}

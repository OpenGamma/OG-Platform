/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Synthetic US Conventions
 */
public class SyntheticUSConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId usgb = ExternalSchemes.financialRegionId("US+GB");
    final ExternalId us = ExternalSchemes.financialRegionId("US");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    //LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP7D"), simpleNameSecurityId("USD LIBOR 7d")), "USD LIBOR 7d", act360, modified, Period.ofDays(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP14D"), simpleNameSecurityId("USD LIBOR 14d")), "USD LIBOR 14d", act360, modified, Period.ofDays(14), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP1M"), simpleNameSecurityId("USD LIBOR 1m")), "USD LIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP2M"), simpleNameSecurityId("USD LIBOR 2m")), "USD LIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP3M"), simpleNameSecurityId("USD LIBOR 3m")), "USD LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP6M"), simpleNameSecurityId("USD LIBOR 6m")), "USD LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP12M")), "USD LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, us);

    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = semiAnnual;
    final DayCount swapFloatDayCount = act360;
    final BusinessDayConvention swapFloatBusinessDay = modified;
    final Frequency swapFloatPaymentFrequency = quarterly;
    final Frequency annual = PeriodFrequency.ANNUAL;

    final int[] isdaFixTenor = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30 };
    // ISDA fixing 11.00 New-York
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String sytheticID = "USDISDA10P" + tenorString;
      utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId(sytheticID)), "USD_ISDAFIX_USDLIBOR10_" + tenorString,
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, us, act360, modified, semiAnnual, 2,
          simpleNameSecurityId("USD LIBOR 3m"), us, true, Period.ofYears(element));
    }

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP1D")), "USDCASHP1D", act360, following, Period.ofDays(1), 0, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP2D")), "USDCASHP2D", act360, following, Period.ofDays(2), 0, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP1M")), "USDCASHP1M", act360, modified, Period.ofMonths(1), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP2M")), "USDCASHP2M", act360, modified, Period.ofMonths(2), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP3M")), "USDCASHP3M", act360, modified, Period.ofMonths(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP4M")), "USDCASHP4M", act360, modified, Period.ofMonths(4), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP5M")), "USDCASHP5M", act360, modified, Period.ofMonths(5), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP6M")), "USDCASHP6M", act360, modified, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP7M")), "USDCASHP7M", act360, modified, Period.ofMonths(7), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP8M")), "USDCASHP8M", act360, modified, Period.ofMonths(8), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP9M")), "USDCASHP9M", act360, modified, Period.ofMonths(9), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP10M")), "USDCASHP10M", act360, modified, Period.ofMonths(10), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP11M")), "USDCASHP11M", act360, modified, Period.ofMonths(11), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP12M")), "USDCASHP12M", act360, modified, Period.ofMonths(12), 2, false, us);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_SWAP")), "USD_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, swapFloatPaymentFrequency, 2, simpleNameSecurityId("USD LIBOR 3m"),
        usgb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_3M_SWAP")), "USD_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, quarterly, 2, simpleNameSecurityId("USD LIBOR 3m"), usgb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_6M_SWAP")), "USD_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, usgb, swapFloatDayCount, swapFloatBusinessDay, semiAnnual, 2, simpleNameSecurityId("USD LIBOR 6m"), usgb, true);

    final int publicationLag = 1;
    // Fed Fund effective
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDFF"), simpleNameSecurityId("USD FF EFFECTIVE")), "USD FF EFFECTIVE", act360, following, Period.ofDays(1), 2, false, us,
        publicationLag);
    // OIS swap
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_OIS_SWAP")), "USD_OIS_SWAP", thirty360, modified, annual, 2, usgb, thirty360, modified, annual, 2,
        simpleNameSecurityId("USD FF EFFECTIVE"), usgb, true, publicationLag);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USDOVERNIGHT")), "USDOVERNIGHT", act360, following, Period.ofDays(1), 2, false,
        us, publicationLag);
    // FRA conventions are stored as IRS
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_3M_FRA")), "USD_3M_FRA", thirty360, modified, quarterly, 2, usgb, act360,
        modified, quarterly, 2, simpleNameSecurityId("USD LIBOR 3m"), usgb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_6M_FRA")), "USD_6M_FRA", thirty360, modified, semiAnnual, 2, usgb, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("USD LIBOR 6m"), usgb, true);

  }

  public static void addCAPMConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_CAPM")), "USD_CAPM",
        ExternalIdBundle.of(syntheticSecurityId("USDLIBORP3M")), ExternalIdBundle.of(syntheticSecurityId("SPX")));
  }

  /**
   * Adds conventions for US Treasury bonds,
   * 
   * @param conventionMaster The convention master, not null
   */
  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("US_TREASURY_BOND_CONVENTION")), "US_TREASURY_BOND_CONVENTION", true, true, 0, 1,
        true);
  }
}

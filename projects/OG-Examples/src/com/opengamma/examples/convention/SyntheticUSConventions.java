/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.convention;

import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Synthetic US Conventions 
 */
public class SyntheticUSConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final ExternalId us = ExternalSchemes.financialRegionId("US");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    
    //LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP3M")), "USD LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP6M")), "USD LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, us);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDLIBORP12M")), "USD LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, us);
    
    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = semiAnnual;
    // TODO: Add all ISDA fixing
    final int[] isdaFixTenor = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30 };
    // ISDA fixing 11.00 New-York
    for (final int element : isdaFixTenor) {
      final String tenorString = element + "Y";
      final String sytheticID = "USDISDA10P" + tenorString;
      utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId(sytheticID)), "USD_ISDAFIX_USDLIBOR10_" + tenorString, 
          swapFixedDayCount, swapFixedBusinessDay, swapFixedPaymentFrequency, 2, us, act360, modified, semiAnnual, 2, 
          simpleNameSecurityId("USD LIBOR 3m"), us, true, Period.ofYears(element));
    }
    
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("USDCASHP1D")), "USDCASHP1D", act360, following, Period.ofDays(1), 0, false, us);
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
  }
  
  public static void addCAPMConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("USD_CAPM")), "USD_CAPM", 
        ExternalIdBundle.of(syntheticSecurityId("USDLIBORP3M")), ExternalIdBundle.of(syntheticSecurityId("SPX")));
  }
  
  
}

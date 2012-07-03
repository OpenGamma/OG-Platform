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
 * Instrument 
 * 
 */
public class SyntheticGBConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId gb = ExternalSchemes.financialRegionId("GB");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP3M")), "GBP LIBOR 3m", act365, modified, Period.ofMonths(3), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP6M"), simpleNameSecurityId("GBP LIBOR 6m")), "GBP LIBOR 6m", act365, modified, Period.ofMonths(6), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP12M")), "GBP LIBOR 12m", act365, modified, Period.ofMonths(12), 0, false, gb);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP1D")), "GBPCASHP1D", act365, following, Period.ofDays(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP1M")), "GBPCASHP1M", act365, modified, Period.ofMonths(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP2M")), "GBPCASHP2M", act365, modified, Period.ofMonths(2), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP3M")), "GBPCASHP3M", act365, modified, Period.ofMonths(3), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP4M")), "GBPCASHP4M", act365, modified, Period.ofMonths(4), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP5M")), "GBPCASHP5M", act365, modified, Period.ofMonths(5), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP6M")), "GBPCASHP6M", act365, modified, Period.ofMonths(6), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP7M")), "GBPCASHP7M", act365, modified, Period.ofMonths(7), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP8M")), "GBPCASHP8M", act365, modified, Period.ofMonths(8), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP9M")), "GBPCASHP9M", act365, modified, Period.ofMonths(9), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP10M")), "GBPCASHP10M", act365, modified, Period.ofMonths(10), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP11M")), "GBPCASHP11M", act365, modified, Period.ofMonths(11), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP12M")), "GBPCASHP12M", act365, modified, Period.ofMonths(12), 0, false, gb);
    
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_SWAP")), "GBP_SWAP", act365, modified, semiAnnual, 0, gb, act365,
        modified, semiAnnual, 0, simpleNameSecurityId("GBP LIBOR 6m"), gb, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_3M_SWAP")), "GBP_3M_SWAP", act365, modified, annual, 0, gb, act365,
        modified, quarterly, 0, simpleNameSecurityId("GBP LIBOR 3m"), gb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_6M_SWAP")), "GBP_6M_SWAP", act365, modified, semiAnnual, 0, gb,
        act365, modified, semiAnnual, 0, simpleNameSecurityId("GBP LIBOR 6m"), gb, true);
    
  }
  
}

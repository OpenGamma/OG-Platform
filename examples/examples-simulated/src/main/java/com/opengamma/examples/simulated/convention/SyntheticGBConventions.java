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
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    final ExternalId gb = ExternalSchemes.financialRegionId("GB");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP7D")), "GBP LIBOR 7d", act365, modified, Period.ofDays(7), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP14D")), "GBP LIBOR 14d", act365, modified, Period.ofDays(14), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP1M")), "GBP LIBOR 1m", act365, modified, Period.ofMonths(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP2M")), "GBP LIBOR 2m", act365, modified, Period.ofMonths(2), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP3M")), "GBP LIBOR 3m", act365, modified, Period.ofMonths(3), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP6M"), simpleNameSecurityId("GBP LIBOR 6m")), "GBP LIBOR 6m", act365, modified, Period.ofMonths(6), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPLIBORP12M")), "GBP LIBOR 12m", act365, modified, Period.ofMonths(12), 0, false, gb);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP1D")), "GBPCASHP1D", act365, following, Period.ofDays(1), 0, false, gb);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("GBPCASHP2D")), "GBPCASHP2D", act365, following, Period.ofDays(2), 0, false, gb);
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

    final Integer publicationLagON = 0;
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("SONIO"), simpleNameSecurityId("GBP SONIO/N")), "GBP SONIO/N", act365,
        following, Period.ofDays(1), 0, false, gb, publicationLagON);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_OIS_SWAP")), "GBP_OIS_SWAP", act365, modified, annual, 2, gb,
        act365, modified, annual, 2, simpleNameSecurityId("GBP SONIO/N"), gb, true, publicationLagON);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_3M_FRA")), "GBP_3M_FRA", act365, modified, annual, 0, gb, act365,
        modified, quarterly, 0, simpleNameSecurityId("GBP LIBOR 3m"), gb, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("GBP_6M_FRA")), "GBP_6M_FRA", act365, modified, semiAnnual, 0, gb,
        act365, modified, semiAnnual, 0, simpleNameSecurityId("GBP LIBOR 6m"), gb, true);

  }

}

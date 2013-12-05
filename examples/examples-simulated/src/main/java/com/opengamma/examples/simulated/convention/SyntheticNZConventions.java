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
 * 
 */
public class SyntheticNZConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final ExternalId nz = ExternalSchemes.financialRegionId("NZ");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDLIBORP3M"), simpleNameSecurityId("NZD LIBOR 3m")), "NZD LIBOR 3m", act365, following, Period.ofMonths(3), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDLIBORP6M")), "NZD LIBOR 6m", act365, following, Period.ofMonths(6), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDLIBORP12M")), "NZD LIBOR 12m", act365, following, Period.ofMonths(12), 2, false, nz);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP1D")), "NZDCASHP1D", act365, following, Period.ofDays(1), 0, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP1M")), "NZDCASHP1M", act365, modified, Period.ofMonths(1), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP2M")), "NZDCASHP2M", act365, modified, Period.ofMonths(2), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP3M")), "NZDCASHP3M", act365, modified, Period.ofMonths(3), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP4M")), "NZDCASHP4M", act365, modified, Period.ofMonths(4), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP5M")), "NZDCASHP5M", act365, modified, Period.ofMonths(5), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP6M")), "NZDCASHP6M", act365, modified, Period.ofMonths(6), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP7M")), "NZDCASHP7M", act365, modified, Period.ofMonths(7), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP8M")), "NZDCASHP8M", act365, modified, Period.ofMonths(8), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP9M")), "NZDCASHP9M", act365, modified, Period.ofMonths(9), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP10M")), "NZDCASHP10M", act365, modified, Period.ofMonths(10), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP11M")), "NZDCASHP11M", act365, modified, Period.ofMonths(1), 2, false, nz);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("NZDCASHP12M")), "NZDCASHP12M", act365, modified, Period.ofMonths(12), 2, false, nz);
    
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("NZD_SWAP")), "NZD_SWAP", act365, modified, semiAnnual, 2, nz, act365,
        modified, quarterly, 2, simpleNameSecurityId("NZD LIBOR 3m"), nz, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("NZD_3M_SWAP")), "NZD_3M_SWAP", act365, modified, semiAnnual, 2, nz,
        act365, modified, quarterly, 2, simpleNameSecurityId("NZD LIBOR 3m"), nz, true);
  }
}

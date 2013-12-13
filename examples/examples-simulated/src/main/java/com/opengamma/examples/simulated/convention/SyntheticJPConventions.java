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
public class SyntheticJPConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId jp = ExternalSchemes.financialRegionId("JP");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP7D"), simpleNameSecurityId("JPY LIBOR 1w")), "JPY LIBOR 1w", act360, following, Period.ofDays(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP14D"), simpleNameSecurityId("JPY LIBOR 2w")), "JPY LIBOR 2w", act360, following, Period.ofDays(14), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP1M"), simpleNameSecurityId("JPY LIBOR 1m")), "JPY LIBOR 1m", act360, following, Period.ofMonths(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP2M"), simpleNameSecurityId("JPY LIBOR 2m")), "JPY LIBOR 2m", act360, following, Period.ofMonths(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP3M"), simpleNameSecurityId("JPY LIBOR 3m")), "JPY LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP6M"), simpleNameSecurityId("JPY LIBOR 6m")), "JPY LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP12M")), "JPY LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, jp);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP1D")),
        "JPYCASHP1D", act360, following, Period.ofDays(1), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP2D")),
        "JPYCASHP2D", act360, following, Period.ofDays(2), 0, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP1M")),
        "JPYCASHP1M", act360, modified, Period.ofMonths(1), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP2M")),
        "JPYCASHP2M", act360, modified, Period.ofMonths(2), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP3M")),
        "JPYCASHP3M", act360, modified, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP4M")),
        "JPYCASHP4M", act360, modified, Period.ofMonths(4), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP5M")),
        "JPYCASHP5M", act360, modified, Period.ofMonths(5), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP6M")),
        "JPYCASHP6M", act360, modified, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP7M")),
        "JPYCASHP7M", act360, modified, Period.ofMonths(7), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP8M")),
        "JPYCASHP8M", act360, modified, Period.ofMonths(8), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP9M")),
        "JPYCASHP9M", act360, modified, Period.ofMonths(9), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP10M")),
        "JPYCASHP10M", act360, modified, Period.ofMonths(10), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP11M")),
        "JPYCASHP11M", act360, modified, Period.ofMonths(11), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP12M")),
        "JPYCASHP12M", act360, modified, Period.ofMonths(12), 2, false, jp);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_SWAP")), "JPY_SWAP", act365, modified, semiAnnual, 2, jp, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("JPY LIBOR 6m"), jp, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_3M_SWAP")), "JPY_3M_SWAP", act365, modified, semiAnnual, 2, jp,
        act360, modified, quarterly, 2, simpleNameSecurityId("JPY LIBOR 3m"), jp, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_6M_SWAP")), "JPY_6M_SWAP", act365, modified, semiAnnual, 2, jp,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("JPY LIBOR 6m"), jp, true);

    final Integer publicationLag = 0;
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("TONAR"), simpleNameSecurityId("JPY TONAR")),
        "JPY TONAR", act365, following, Period.ofDays(1), 2, false, jp, publicationLag);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_OIS_SWAP")), "JPY_OIS_SWAP", act365, modified, annual, 2, jp,
        act365, modified, annual, 2, simpleNameSecurityId("JPY TONAR"), jp, true, publicationLag);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_3M_FRA")), "JPY_3M_FRA", act365, modified, semiAnnual, 2, jp,
        act360, modified, quarterly, 2, simpleNameSecurityId("JPY LIBOR 3m"), jp, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("JPY_6M_FRA")), "JPY_6M_FRA", act365, modified, semiAnnual, 2, jp,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("JPY LIBOR 6m"), jp, true);
  }
}

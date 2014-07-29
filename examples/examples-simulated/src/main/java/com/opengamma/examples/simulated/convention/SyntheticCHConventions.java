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
public class SyntheticCHConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final ExternalId ch = ExternalSchemes.financialRegionId("CH");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP7D"), simpleNameSecurityId("CHF LIBOR 7d")), "CHF LIBOR 7d",
        act360, following, Period.ofDays(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP14D"), simpleNameSecurityId("CHF LIBOR 14d")), "CHF LIBOR 14d",
        act360, following, Period.ofDays(14), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP1M"), simpleNameSecurityId("CHF LIBOR 1m")), "CHF LIBOR 1m",
        act360, following, Period.ofMonths(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP2M"), simpleNameSecurityId("CHF LIBOR 2m")), "CHF LIBOR 2m",
        act360, following, Period.ofMonths(2), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP3M"), simpleNameSecurityId("CHF LIBOR 3m")), "CHF LIBOR 3m",
        act360, following, Period.ofMonths(3), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP6M"), simpleNameSecurityId("CHF LIBOR 6m")), "CHF LIBOR 6m",
        act360, following, Period.ofMonths(6), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFLIBORP12M"), simpleNameSecurityId("CHF LIBOR 12m")), "CHF LIBOR 6m",
        act360, following, Period.ofMonths(12), 2, false, ch);

    //Identifiers for external data
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP1D")), "CHFCASHP1D", act360, following, Period.ofDays(1), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP2D")), "CHFCASHP2D", act360, following, Period.ofDays(2), 0, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP1M")), "CHFCASHP1M", act360, modified, Period.ofMonths(1), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP2M")), "CHFCASHP2M", act360, modified, Period.ofMonths(2), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP3M")), "CHFCASHP3M", act360, modified, Period.ofMonths(3), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP4M")), "CHFCASHP4M", act360, modified, Period.ofMonths(4), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP5M")), "CHFCASHP5M", act360, modified, Period.ofMonths(5), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP6M")), "CHFCASHP6M", act360, modified, Period.ofMonths(6), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP7M")), "CHFCASHP7M", act360, modified, Period.ofMonths(7), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP8M")), "CHFCASHP8M", act360, modified, Period.ofMonths(8), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP9M")), "CHFCASHP9M", act360, modified, Period.ofMonths(9), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP10M")), "CHFCASHP10M", act360, modified, Period.ofMonths(10), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP11M")), "CHFCASHP11M", act360, modified, Period.ofMonths(11), 2, false, ch);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CHFCASHP12M")), "CHFCASHP12M", act360, modified, Period.ofMonths(12), 2, false, ch);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_SWAP")), "CHF_SWAP", thirty360, modified, annual, 2, ch, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_3M_SWAP")), "CHF_3M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, quarterly, 2, simpleNameSecurityId("CHF LIBOR 3m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_6M_SWAP")), "CHF_6M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);

    final Integer publicationLagON = 0;
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("TOISTOIS"), simpleNameSecurityId("CHF TOISTOIS")), "CHF TOISTOIS", act360,
        following, Period.ofDays(1), 2, false, ch, publicationLagON);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_OIS_SWAP")), "CHF_OIS_SWAP", act360, modified, annual, 2, ch,
        act360, modified, annual, 2, simpleNameSecurityId("CHF TOISTOIS"), ch, true, publicationLagON);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_3M_FRA")), "CHF_3M_FRA", thirty360, modified, annual, 2, ch, act360,
        modified, quarterly, 2, simpleNameSecurityId("CHF LIBOR 3m"), ch, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CHF_6M_FRA")), "CHF_6M_FRA", thirty360, modified, annual, 2, ch, act360,
        modified, semiAnnual, 2, simpleNameSecurityId("CHF LIBOR 6m"), ch, true);

  }
}

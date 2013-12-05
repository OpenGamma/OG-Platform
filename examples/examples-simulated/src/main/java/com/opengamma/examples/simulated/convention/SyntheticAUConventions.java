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
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
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
public class SyntheticAUConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);

    //TODO holiday associated with AUD swaps is Sydney
    final ExternalId au = ExternalSchemes.financialRegionId("AU");
    final Integer overnightPublicationLag = 0;

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP1D")), "AUDCASHP1D", act365, following, Period.ofDays(1), 0, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP1M")), "AUDCASHP1M", act365, modified, Period.ofMonths(1), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP2M")), "AUDCASHP2M", act365, modified, Period.ofMonths(2), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP3M")), "AUDCASHP3M", act365, modified, Period.ofMonths(3), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP4M")), "AUDCASHP4M", act365, modified, Period.ofMonths(4), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP5M")), "AUDCASHP5M", act365, modified, Period.ofMonths(5), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP6M")), "AUDCASHP6M", act365, modified, Period.ofMonths(6), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP7M")), "AUDCASHP7M", act365, modified, Period.ofMonths(7), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP8M")), "AUDCASHP8M", act365, modified, Period.ofMonths(8), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP9M")), "AUDCASHP9M", act365, modified, Period.ofMonths(9), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP10M")), "AUDCASHP10M", act365, modified, Period.ofMonths(10), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP11M")), "AUDCASHP11M", act365, modified, Period.ofMonths(1), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDCASHP12M")), "AUDCASHP12M", act365, modified, Period.ofMonths(12), 2, false, au);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDLIBORP3M"), simpleNameSecurityId("AUD LIBOR 3m")), "AUD LIBOR 3m", act365, following, Period.ofMonths(3), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDLIBORP6M"), simpleNameSecurityId("AUD LIBOR 6m")), "AUD LIBOR 6m", act365, following, Period.ofMonths(6), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDLIBORP12M"), simpleNameSecurityId("AUD LIBOR 12m")), "AUD LIBOR 12m", act365, following, Period.ofMonths(12), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDON"), simpleNameSecurityId("RBA OVERNIGHT CASH RATE")),
        "RBA OVERNIGHT CASH RATE", act365, following, Period.ofDays(1), 0, false, au, overnightPublicationLag);

    final DayCount swapFixedDayCount = act365;
    final BusinessDayConvention swapFixedBusinessDay = modified;

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_SWAP")), "AUD_SWAP", act365, modified, semiAnnual, 0, au, act365,
        modified, semiAnnual, 0, simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M"), au, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_3M_SWAP")), "AUD_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        quarterly, 0, au, act365, modified, quarterly, 0, simpleNameSecurityId(IndexType.BBSW + "_AUD_P3M"), au, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_6M_SWAP")), "AUD_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        semiAnnual, 0, au, act365, modified, semiAnnual, 0, simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M"), au, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_OIS_SWAP")), "AUD_OIS_SWAP", act365, modified, annual, 0, au, act365,
        modified, annual, 0, simpleNameSecurityId("RBA OVERNIGHT CASH RATE"), au, true, overnightPublicationLag);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDBBP3M"), simpleNameSecurityId(IndexType.BBSW  + "_AUD_P3M")),
        "AUD Bank Bill 3m", act365, modified, Period.ofMonths(3), 0, true, au); // "AUD Bank Bill 3m"
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDBBP6M"), simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M")),
        "AUD Bank Bill 6m", act365, modified, Period.ofMonths(6), 0, true, au); // "AUD Bank Bill 6m"
  }

}

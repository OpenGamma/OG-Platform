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
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
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
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    //TODO holiday associated with AUD swaps is Sydney
    final ExternalId au = ExternalSchemes.financialRegionId("AU");

    ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDLIBORP3M")), "AUD LIBOR 3m", act365, following, Period.ofMonths(3), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDLIBORP6M")), "AUD LIBOR 6m", act365, following, Period.ofMonths(6), 2, false, au);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDLIBORP12M")), "AUD LIBOR 12m", act365, following, Period.ofMonths(12), 2, false, au);

    final DayCount swapFixedDayCount = act365;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_SWAP")), "AUD_SWAP", act365, modified, semiAnnual, 0, au, act365,
        modified, semiAnnual, 0, simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M"), au, true);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_3M_SWAP")), "AUD_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        quarterly, 0, au, act365, modified, quarterly, 0, simpleNameSecurityId(IndexType.BBSW + "_AUD_P3M"), au, true);
    // simpleNameSecurityId("AUD Bank Bill 3m")
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("AUD_6M_SWAP")), "AUD_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        semiAnnual, 0, au, act365, modified, semiAnnual, 0, simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M"), au, true);
    // simpleNameSecurityId("AUD Bank Bill 6m")
    
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDBBP3M"), simpleNameSecurityId(IndexType.BBSW  + "_AUD_P3M")), 
        "AUD Bank Bill 3m", act365, modified, Period.ofMonths(3), 0, true, au); // "AUD Bank Bill 3m"
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("AUDBBP6M"), simpleNameSecurityId(IndexType.BBSW + "_AUD_P6M")), 
        "AUD Bank Bill 6m", act365, modified, Period.ofMonths(6), 0, true, au); // "AUD Bank Bill 6m"
  }

}

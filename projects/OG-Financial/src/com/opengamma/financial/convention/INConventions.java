/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
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
public class INConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final ExternalId in = ExternalSchemes.financialRegionId("IN");
    final Integer overnightPublicationLag = 0;
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    // IR FUTURES
    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_IR_FUTURE")), "INR_IR_FUTURE", act365, modified, Period.ofMonths(3),
        0, true, in);
    utils.addConventionBundle(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("IRNI6M Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR SWAP INDEX")),
        "INR SWAP INDEX", act365, modified, Period.ofMonths(6), 0, true, in);

    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_SWAP")), "INR_SWAP", act365, modified, semiAnnual, 0, in, act365,
        modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR SWAP INDEX"), in, true);
    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_6M_SWAP")), "INR_6M_SWAP", act365, modified, semiAnnual, 0, in, act365,
        modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR SWAP INDEX"), in, true);

    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_OIS_SWAP")), "INR_OIS_SWAP", act365, modified, annual, 0, in,
        act365, modified, annual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR OVERNIGHT CASH RATE"), in, true, overnightPublicationLag);

    utils.addConventionBundle(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("NSERO Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
        "INR OVERNIGHT CASH RATE")), "INR OVERNIGHT CASH RATE", act365, following, Period.ofDays(1), 0, false, in, 0);

  }

}

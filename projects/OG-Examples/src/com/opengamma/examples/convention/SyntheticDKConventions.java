/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.convention;

import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * 
 */
public class SyntheticDKConventions {

  public static void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");

    final ExternalId dk = ExternalSchemes.financialRegionId("DK");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKLIBORP3M")), "DKK CIBOR 3m", act360, following, Period.ofMonths(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKLIBORP6M")), "DKK CIBOR 6m", act360, following, Period.ofMonths(6), 2, false, dk);
    
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP1D")), "DKKCASHP1D", act360, following, Period.ofDays(1), 0, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP1M")), "DKKCASHP1M", act360, modified, Period.ofMonths(1), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP2M")), "DKKCASHP2M", act360, modified, Period.ofMonths(2), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP3M")), "DKKCASHP3M", act360, modified, Period.ofMonths(3), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP4M")), "DKKCASHP4M", act360, modified, Period.ofMonths(4), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP5M")), "DKKCASHP5M", act360, modified, Period.ofMonths(5), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP6M")), "DKKCASHP6M", act360, modified, Period.ofMonths(6), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP7M")), "DKKCASHP7M", act360, modified, Period.ofMonths(7), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP8M")), "DKKCASHP8M", act360, modified, Period.ofMonths(8), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP9M")), "DKKCASHP9M", act360, modified, Period.ofMonths(9), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP10M")), "DKKCASHP10M", act360, modified, Period.ofMonths(10), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP11M")), "DKKCASHP11M", act360, modified, Period.ofMonths(11), 2, false, dk);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("DKKCASHP12M")), "DKKCASHP12M", act360, modified, Period.ofMonths(12), 2, false, dk);
  }

}

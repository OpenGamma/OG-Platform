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
public class SyntheticCAConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId ca = ExternalSchemes.financialRegionId("CA");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CADLIBORP3M"), simpleNameSecurityId("CDOR 3m")), "CAD LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CADLIBORP6M")), "CAD LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, ca);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("CADLIBORP12M")), "CAD LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, ca);
    
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_SWAP")), "CAD_SWAP", act365, modified, semiAnnual, 0, ca, act365, modified,
        quarterly, 0, simpleNameSecurityId("CDOR 3m"), ca, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CAD_1Y_SWAP")), "CAD_1Y_SWAP", act365, modified, annual, 0, ca, act365, modified,
        quarterly, 0, simpleNameSecurityId("CDOR 3m"), ca, true);
  }

}

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
public class SyntheticJPConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final ExternalId jp = ExternalSchemes.financialRegionId("JP");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    
    //TODO looked at BSYM and the codes seem right but need to check
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP3M")), "JPY LIBOR 3m", act360, following, Period.ofMonths(3), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP6M")), "JPY LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, jp);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYLIBORP12M")), "JPY LIBOR 12m", act360, following, Period.ofMonths(12), 2, false, jp);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("JPYCASHP1D")),
        "JPYCASHP1D", act360, following, Period.ofDays(1), 0, false, jp);
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
  }
}

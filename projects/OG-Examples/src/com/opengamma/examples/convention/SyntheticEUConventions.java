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
 * Standard conventions for EUR.
 */
public class SyntheticEUConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    
    //TODO holiday associated with EUR swaps is TARGET
    final ExternalId eu = ExternalSchemes.financialRegionId("EU");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    //EURO LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURLIBORP3M")), "EUR LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURLIBORP6M")), "EUR LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURLIBORP12M")), "EUR LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);    

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP1D")),
        "EURCASHP1D", act360, following, Period.ofDays(1), 0, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP1M")),
        "EURCASHP1M", act360, modified, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP2M")),
        "EURCASHP2M", act360, modified, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP3M")),
        "EURCASHP3M", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP4M")),
        "EURCASHP4M", act360, modified, Period.ofMonths(4), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP5M")),
        "EURCASHP5M", act360, modified, Period.ofMonths(5), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP6M")),
        "EURCASHP6M", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP7M")),
        "EURCASHP7M", act360, modified, Period.ofMonths(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP8M")),
        "EURCASHP8M", act360, modified, Period.ofMonths(8), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP9M")),
        "EURCASHP9M", act360, modified, Period.ofMonths(9), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP10M")),
        "EURCASHP10M", act360, modified, Period.ofMonths(10), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP11M")),
        "EURCASHP11M", act360, modified, Period.ofMonths(11), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP12M")),
        "EURCASHP12M", act360, modified, Period.ofMonths(12), 2, false, eu);
  }

}

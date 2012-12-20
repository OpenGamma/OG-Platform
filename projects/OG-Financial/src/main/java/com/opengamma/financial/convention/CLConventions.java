/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import javax.time.calendar.Period;

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
public class CLConventions {
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final ExternalId CL = ExternalSchemes.financialRegionId("CL");
  private static final Frequency SEMI_ANNUAL = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CHIBNOM Index"), simpleNameSecurityId("CLP DEPOSIT O/N")), "CLP DEPOSIT O/N", ACT_360,
        FOLLOWING, Period.ofDays(1), 0, false, CL);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CLICP Index"), simpleNameSecurityId("CLICP Index")), "CLICP Index", ACT_360,
        FOLLOWING, Period.ofDays(1), 0, false, CL, 0);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CLP_OIS_SWAP")), "CLP_OIS_SWAP", ACT_360, FOLLOWING, SEMI_ANNUAL, 0, CL, ACT_360,
        FOLLOWING, SEMI_ANNUAL, 0, simpleNameSecurityId("CLICP Index"), CL, true, 0);
  }
}

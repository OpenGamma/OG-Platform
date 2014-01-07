/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.core.id.ExternalSchemes.tullettPrebonSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of CLP instruments
 */
public class CLConventions {
  /** Following business day convention */
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  /** Act/360 day count convention */
  private static final DayCount ACT_360 = DayCounts.ACT_360;
  /** The region */
  private static final ExternalId CL = ExternalSchemes.financialRegionId("CL");
  /** Semi-annual frequency */
  private static final Frequency SEMI_ANNUAL = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);

  /**
   * Adds conventions for OIS and implied deposits
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    for (int i = 1; i < 12; i++) {
      final String impliedDepositName = "CLP IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullettImpliedDeposit = tullettPrebonSecurityId("LMIDPCLPSPT" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simpleImpliedDeposit = simpleNameSecurityId(impliedDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(tullettImpliedDeposit, simpleImpliedDeposit), impliedDepositName, ACT_360, FOLLOWING, Period.ofMonths(i), 0, false, CL);
    }

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CHIBNOM Index"), simpleNameSecurityId("CLP DEPOSIT O/N")), "CLP DEPOSIT O/N", ACT_360,
        FOLLOWING, Period.ofDays(1), 0, false, CL);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("CLICP Index"), simpleNameSecurityId("CLICP Index")), "CLICP Index", ACT_360,
        FOLLOWING, Period.ofDays(1), 0, false, CL, 0);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("CLP_OIS_SWAP")), "CLP_OIS_SWAP", ACT_360, FOLLOWING, SEMI_ANNUAL, 0, CL, ACT_360,
        FOLLOWING, SEMI_ANNUAL, 0, simpleNameSecurityId("CLICP Index"), CL, true, 0);
  }
}

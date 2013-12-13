/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information use to construct standard versions of HUF instruments
 */
public class HUConventions {
  /** Bloomberg month codes */
  private static final char[] MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};
  /** Bubur name prefix */
  private static final String HUF_BUBOR = "HUF BUBOR ";
  /** Deposit prefix */
  private static final String HUF_DEPOSIT = "HUF_DEPOSIT ";
  /** Day count */
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  /** Business day convention */
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  /** Region */
  private static final ExternalId HU = ExternalSchemes.financialRegionId("HU");

  /**
   * Adds conventions for deposit and Bubor rates.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    for (int i = 1; i <= 12; i++) {
      final String buborName = HUF_BUBOR + i + "m";
      final String depositName = HUF_DEPOSIT + i + "m";
      final ExternalId bloombergBuborId = ExternalSchemes.bloombergTickerSecurityId("HFBUBR" + (i == 12 ? "1" : MONTH_CODES[i - 1]) + " Curncy");
      final ExternalId simpleBuborId = InMemoryConventionBundleMaster.simpleNameSecurityId(buborName);
      final ExternalId bloombergDepositId = ExternalSchemes.bloombergTickerSecurityId("HFDR" + (i == 12 ? "1" : MONTH_CODES[i - 1]) + " Curncy");
      final ExternalId simpleDepositId = InMemoryConventionBundleMaster.simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bloombergBuborId, simpleBuborId), buborName, DAY_COUNT, FOLLOWING, Period.ofMonths(i), 0, false, HU);
      utils.addConventionBundle(ExternalIdBundle.of(bloombergDepositId, simpleDepositId), depositName, DAY_COUNT, FOLLOWING, Period.ofMonths(i), 0, false, HU);
    }
  }
}

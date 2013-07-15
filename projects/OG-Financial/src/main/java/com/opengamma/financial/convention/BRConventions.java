/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Contains information used to construct standard versions of BRL instruments
 */
public class BRConventions {
  /** Month codes used by Bloomberg */
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};

  /**
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act252 = DayCountFactory.INSTANCE.getDayCount("Actual/252");
    final ExternalId br = ExternalSchemes.financialRegionId("BR");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    for (int i = 1; i < 3; i++) {
      final String dayDepositName = "BRL DEPOSIT " + i + "d";
      final ExternalId dayBbgDeposit = bloombergTickerSecurityId("BCDR" + i + "T Curncy");
      final ExternalId daySimpleDeposit = simpleNameSecurityId(dayDepositName);
      final String weekDepositName = "BRL DEPOSIT " + i + "w";
      final ExternalId weekBbgDeposit = bloombergTickerSecurityId("BCDR" + i + "Z Curncy");
      final ExternalId weekSimpleDeposit = simpleNameSecurityId(weekDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(dayBbgDeposit, daySimpleDeposit), dayDepositName, act252, following, Period.ofDays(i), 2, false, br);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgDeposit, weekSimpleDeposit), weekDepositName, act252, following, Period.ofDays(i * 7), 2, false, br);
    }

    for (int i = 1; i < 12; i++) {
      final String depositName = "BRL DEPOSIT " + i + "m";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("BCDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      final String impliedDepositName = "BRL IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullettImpliedDeposit = tullettPrebonSecurityId("LMIDPBRLSPT" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simpleImpliedDeposit = simpleNameSecurityId(impliedDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, act252, following, Period.ofMonths(i), 2, false, br);
      utils.addConventionBundle(ExternalIdBundle.of(tullettImpliedDeposit, simpleImpliedDeposit), impliedDepositName, act252, following, Period.ofMonths(i), 2, false, br);
    }

    for (int i = 1; i < 2; i++) {
      final String depositName = "BRL DEPOSIT " + i + "y";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("BCDR" + i + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, act252, following, Period.ofYears(i), 2, false, br);
    }

  }

}

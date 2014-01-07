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
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of MXN instruments
 */
public class MXConventions {
  /** Month codes used by Bloomberg */
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};

  /**
   * Add conventions for deposits and implied deposits.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount dc = DayCounts.TWENTY_EIGHT_360;
    final ExternalId mx = ExternalSchemes.financialRegionId("MX");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    for (int i = 1; i < 3; i++) {
      final String dayDepositName = "MXN DEPOSIT " + i + "d";
      final ExternalId dayBbgDeposit = bloombergTickerSecurityId("MPDR" + i + "T Curncy");
      final ExternalId daySimpleDeposit = simpleNameSecurityId(dayDepositName);
      final String weekDepositName = "MXN DEPOSIT " + i + "w";
      final ExternalId weekBbgDeposit = bloombergTickerSecurityId("MPDR" + i + "Z Curncy");
      final ExternalId weekSimpleDeposit = simpleNameSecurityId(weekDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(dayBbgDeposit, daySimpleDeposit), dayDepositName, dc, following, Period.ofDays(i), 0, false, mx);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgDeposit, weekSimpleDeposit), weekDepositName, dc, following, Period.ofDays(i * 7), 0, false, mx);
    }

    for (int i = 1; i < 12; i++) {
      final String depositName = "MXN DEPOSIT " + i + "m";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("MPDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      final String impliedDepositName = "MXN IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullettImpliedDeposit = tullettPrebonSecurityId("LMIDPMXNSPT" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simpleImpliedDeposit = simpleNameSecurityId(impliedDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, dc, following, Period.ofMonths(i), 0, false, mx);
      utils.addConventionBundle(ExternalIdBundle.of(tullettImpliedDeposit, simpleImpliedDeposit), impliedDepositName, dc, following, Period.ofMonths(i), 0, false, mx);
    }

    for (int i = 1; i < 2; i++) {
      final String depositName = "MXN DEPOSIT " + i + "y";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("MPDR" + i + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, dc, following, Period.ofYears(i), 0, false, mx);
    }

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("MPSW28T Curncy"), simpleNameSecurityId("MXN LIBOR 28d")),
        "MXN LIBOR 28d", dc, following, Period.ofMonths(3), 2, false, mx);
    final Frequency frequency = PeriodFrequency.TWENTY_EIGHT_DAYS;
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("MXN_28D_SWAP")), "MXN_28D_SWAP", dc, following,
        frequency, 2, mx, dc, following, frequency, 2, simpleNameSecurityId("MXN LIBOR 28d"), mx, true);

  }
}

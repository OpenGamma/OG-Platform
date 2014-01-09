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
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains information used to construct standard versions of KRW instruments
 */
public class KRConventions {
  /** Month codes used by Bloomberg */
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};

  /**
   * Adds conventions for deposits, implied deposits and cash.
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final ExternalId kr = ExternalSchemes.financialRegionId("KR");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    for (int i = 1; i < 3; i++) {
      final String dayDepositName = "KRW DEPOSIT " + i + "d";
      final ExternalId dayBbgDeposit = bloombergTickerSecurityId("KWDR" + i + "T Curncy");
      final ExternalId daySimpleDeposit = simpleNameSecurityId(dayDepositName);
      final String weekDepositName = "KRW DEPOSIT " + i + "w";
      final ExternalId weekBbgDeposit = bloombergTickerSecurityId("KWDR" + i + "Z Curncy");
      final ExternalId weekSimpleDeposit = simpleNameSecurityId(weekDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(dayBbgDeposit, daySimpleDeposit), dayDepositName, act360, following, Period.ofDays(i), 0, false, kr);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgDeposit, weekSimpleDeposit), weekDepositName, act360, following, Period.ofDays(i * 7), 0, false, kr);
    }

    for (int i = 1; i < 12; i++) {
      final String depositName = "KRW DEPOSIT " + i + "m";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("KWDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      final String impliedDepositName = "KRW IMPLIED DEPOSIT " + i + "m";
      final ExternalId tullettImpliedDeposit = tullettPrebonSecurityId("AMIDPKRWSPT" + (i < 10 ? "0" : "") + i + "M");
      final ExternalId simpleImpliedDeposit = simpleNameSecurityId(impliedDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, act360, following, Period.ofMonths(i), 0, false, kr);
      utils.addConventionBundle(ExternalIdBundle.of(tullettImpliedDeposit, simpleImpliedDeposit), impliedDepositName, act360, following, Period.ofMonths(i), 0, false, kr);
    }

    for (int i = 1; i < 5; i++) {
      if (i == 1) {
        final String depositName = "KRW IMPLIED DEPOSIT 1y";
        final ExternalId tullettImpliedDeposit = tullettPrebonSecurityId("AMIDPKRWSPT12M");
        final ExternalId simpleImpliedDeposit = simpleNameSecurityId(depositName);
        utils.addConventionBundle(ExternalIdBundle.of(tullettImpliedDeposit, simpleImpliedDeposit), depositName, act360, following, Period.ofYears(1), 0, false, kr);
      }
      final String depositName = "KRW DEPOSIT " + i + "y";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("KWDR" + i + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, act360, following, Period.ofYears(i), 0, false, kr);
    }

    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("KWCDC Curncy"), bloombergTickerSecurityId("KWCDC Index"), simpleNameSecurityId("KRW SWAP FIXING 3m")),
        "KRW SWAP FIXING 3m", act365, modified, Period.ofMonths(3), 0, false, kr);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("KRW_SWAP")), "KRW_SWAP", act365, modified,
        quarterly, 2, kr, act365, modified, quarterly, 2, simpleNameSecurityId("KRW SWAP FIXING 3m"), kr, true);
  }
}

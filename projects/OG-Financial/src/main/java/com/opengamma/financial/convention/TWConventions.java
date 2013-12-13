/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
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

/**
 *
 */
public class TWConventions {
  private static final char BBG_DAY_CODE = 'T';
  private static final char BBG_WEEK_CODE = 'Z';
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final ExternalId TW = ExternalSchemes.financialRegionId("TW");
  private static final Frequency QUARTERLY = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    for (int i = 1; i < 4; i++) {
      final String name = "TWD Deposit " + i + "d";
      final ExternalId bbgId = bloombergTickerSecurityId("NTDR" + i + BBG_DAY_CODE + " Curncy");
      final ExternalId simpleId = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(bbgId, simpleId), name, ACT_365, FOLLOWING, Period.ofDays(i), 0, false, TW);
    }
    for (int i = 1; i < 4; i++) {
      final String name = "TWD Deposit " + i + "w";
      final ExternalId bbgId = bloombergTickerSecurityId("NTDR" + i + BBG_WEEK_CODE + " Curncy");
      final ExternalId simpleId = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(bbgId, simpleId), name, ACT_365, FOLLOWING, Period.ofDays(i * 7), 0, false, TW);
    }
    for (int i = 1; i < 12; i++) {
      final String name = "TWD Deposit " + i + "m";
      final ExternalId bbgId = bloombergTickerSecurityId("NTDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId simpleId = simpleNameSecurityId(name);
      utils.addConventionBundle(ExternalIdBundle.of(bbgId, simpleId), name, ACT_365, FOLLOWING, Period.ofMonths(i), 0, false, TW);
    }
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("TWD_3M_SWAP")), "TWD_3M_SWAP", ACT_365, FOLLOWING,
        QUARTERLY, 2, TW, ACT_365, FOLLOWING, QUARTERLY, 2, simpleNameSecurityId("TWD Deposit 3m"), TW, true);
  }
}

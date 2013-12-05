/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static com.opengamma.core.id.ExternalSchemes.bloombergTickerSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.apache.commons.lang.Validate;
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
public class ZAConventions {
  private static final char BBG_DAY_CODE = 'T';
  private static final char BBG_WEEK_CODE = 'Z';
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final DayCount ACT_365 = DayCounts.ACT_365;
  private static final ExternalId ZA = ExternalSchemes.financialRegionId("ZA");
  private static final Frequency QUARTERLY = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");

    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    utils.addConventionBundle(ExternalIdBundle.of(bloombergTickerSecurityId("SAONBOR Index"), simpleNameSecurityId("ZAR Jibor O/N")), "ZAR Jibor O/N",
        ACT_365, FOLLOWING, Period.ofDays(1), 0, false, ZA);
    for (int i = 1; i < 4; i++) {
      final String depositName = "ZAR Deposit " + i + "d";
      final ExternalId depositBbgId = bloombergTickerSecurityId("SADR" + i + BBG_DAY_CODE + " Curncy");
      final ExternalId depositSimpleId = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(depositBbgId, depositSimpleId), depositName, ACT_365, FOLLOWING, Period.ofDays(i), 0, false, ZA);
    }
    for (int i = 1; i < 4; i++) {
      final String depositName = "ZAR Deposit " + i + "w";
      final ExternalId depositBbgId = bloombergTickerSecurityId("SADR" + i + BBG_WEEK_CODE + " Curncy");
      final ExternalId depositSimpleId = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(depositBbgId, depositSimpleId), depositName, ACT_365, FOLLOWING, Period.ofDays(i * 7), 0, false, ZA);
    }
    for (int i = 1; i < 12; i++) {
      final String depositName = "ZAR Deposit " + i + "m";
      final ExternalId depositBbgId = bloombergTickerSecurityId("SADR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId depositSimpleId = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(depositBbgId, depositSimpleId), depositName, ACT_365, FOLLOWING, Period.ofMonths(i), 0, false, ZA);
      final String jiborName = "ZAR Jibor " + i + "m";
      final ExternalId jiborBbgId = bloombergTickerSecurityId("JIBA" + i + "M Index");
      final ExternalId jiborSimpleId = simpleNameSecurityId(jiborName);
      utils.addConventionBundle(ExternalIdBundle.of(jiborBbgId, jiborSimpleId), jiborName, ACT_365, FOLLOWING, Period.ofMonths(i), 0, false, ZA);
    }
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("ZAR_3M_SWAP")), "ZAR_3M_SWAP", ACT_365, FOLLOWING,
        QUARTERLY, 2, ZA, ACT_365, FOLLOWING, QUARTERLY, 2, simpleNameSecurityId("ZAR Jibor 3m"), ZA, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("ZAR_3M_FRA")), "ZAR_3M_FRA", ACT_365, FOLLOWING,
        QUARTERLY, 2, ZA, ACT_365, FOLLOWING, QUARTERLY, 2, simpleNameSecurityId("ZAR Jibor 3m"), ZA, true);

  }

}

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class INConventions {
  /** Month codes used by Bloomberg */
  private static final char[] BBG_MONTH_CODES = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K'};

  /**
   * @param conventionMaster The convention master, not null
   */
  public static synchronized void addFixedIncomeInstrumentConventions(final InMemoryConventionBundleMaster conventionMaster) {
    ArgumentChecker.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act365 = DayCounts.ACT_365;
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final ExternalId in = ExternalSchemes.financialRegionId("IN");
    final Integer overnightPublicationLag = 0;
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);

    for (int i = 1; i < 3; i++) {
      final String dayDepositName = "INR DEPOSIT " + i + "d";
      final ExternalId dayBbgDeposit = bloombergTickerSecurityId("IRDR" + i + "T Curncy");
      final ExternalId daySimpleDeposit = simpleNameSecurityId(dayDepositName);
      final String weekDepositName = "INR DEPOSIT " + i + "w";
      final ExternalId weekBbgDeposit = bloombergTickerSecurityId("IRDR" + i + "Z Curncy");
      final ExternalId weekSimpleDeposit = simpleNameSecurityId(weekDepositName);
      utils.addConventionBundle(ExternalIdBundle.of(dayBbgDeposit, daySimpleDeposit), dayDepositName, act365, following, Period.ofDays(i), 0, false, in);
      utils.addConventionBundle(ExternalIdBundle.of(weekBbgDeposit, weekSimpleDeposit), weekDepositName, act365, following, Period.ofDays(i * 7), 0, false, in);
    }

    for (int i = 1; i < 12; i++) {
      final String depositName = "INR DEPOSIT " + i + "m";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("IRDR" + BBG_MONTH_CODES[i - 1] + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, act365, following, Period.ofMonths(i), 0, false, in);
    }

    for (int i = 1; i < 2; i++) {
      final String depositName = "INR DEPOSIT " + i + "y";
      final ExternalId bbgDeposit = bloombergTickerSecurityId("IRDR" + i + " Curncy");
      final ExternalId simpleDeposit = simpleNameSecurityId(depositName);
      utils.addConventionBundle(ExternalIdBundle.of(bbgDeposit, simpleDeposit), depositName, act365, following, Period.ofYears(i), 0, false, in);
    }

    // IR FUTURES
    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_IR_FUTURE")), "INR_IR_FUTURE", act365, modified, Period.ofMonths(3),
        0, true, in);
    utils.addConventionBundle(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("IRNI6M Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR SWAP INDEX")),
        "INR SWAP INDEX", act365, modified, Period.ofMonths(6), 0, true, in);

    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_SWAP")), "INR_SWAP", act365, modified, semiAnnual, 0, in, act365,
        modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR SWAP INDEX"), in, true);
    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_6M_SWAP")), "INR_6M_SWAP", act365, modified, semiAnnual, 0, in, act365,
        modified, semiAnnual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR SWAP INDEX"), in, true);

    utils.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR_OIS_SWAP")), "INR_OIS_SWAP", act365, modified, annual, 0, in,
        act365, modified, annual, 0, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "INR OVERNIGHT CASH RATE"), in, true, overnightPublicationLag);

    utils.addConventionBundle(ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("NSERO Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
        "INR OVERNIGHT CASH RATE")), "INR OVERNIGHT CASH RATE", act365, following, Period.ofDays(1), 0, false, in, 0);

  }

}

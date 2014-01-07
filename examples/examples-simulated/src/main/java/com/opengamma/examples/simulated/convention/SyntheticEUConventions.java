/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import static com.opengamma.core.id.ExternalSchemes.syntheticSecurityId;
import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleMasterUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Standard conventions for EUR.
 */
public class SyntheticEUConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventions.MODIFIED_FOLLOWING;
    final BusinessDayConvention following = BusinessDayConventions.FOLLOWING;
    final DayCount act360 = DayCounts.ACT_360;
    final DayCount thirty360 = DayCounts.THIRTY_U_360;
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);

    //TODO holiday associated with EUR swaps is TARGET
    final ExternalId eu = ExternalSchemes.financialRegionId("EU");
    final ConventionBundleMasterUtils utils = new ConventionBundleMasterUtils(conventionMaster);
    //EURO LIBOR
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURLIBORP3M"), simpleNameSecurityId("EUR LIBOR 3m")), "EUR LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURLIBORP6M"), simpleNameSecurityId("EUR LIBOR 6m")), "EUR LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURLIBORP12M")), "EUR LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP7D"), simpleNameSecurityId("EURIBOR 7d")), "EURIBOR 7d", act360, modified, Period.ofDays(7), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP14D"), simpleNameSecurityId("EURIBOR 14d")), "EURIBOR 14d", act360, modified, Period.ofDays(14), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP1M"), simpleNameSecurityId("EURIBOR 1m")), "EURIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP2M"), simpleNameSecurityId("EURIBOR 2m")), "EURIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP3M"), simpleNameSecurityId("EURIBOR 3m")), "EURIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP6M"), simpleNameSecurityId("EURIBOR 6m")), "EURIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EUREURIBORP12M"), simpleNameSecurityId("EURIBOR 12m")), "EURIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, eu);

    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP1D")),
        "EURCASHP1D", act360, following, Period.ofDays(1), 0, false, eu);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EURCASHP2D")),
        "EURCASHP2D", act360, following, Period.ofDays(2), 0, false, eu);
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

    final DayCount swapFixedDayCount = thirty360;
    final BusinessDayConvention swapFixedBusinessDay = modified;
    final Frequency swapFixedPaymentFrequency = annual;
    final DayCount euriborDayCount = act360;

    // IRS
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_SWAP")), "EUR_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, euriborDayCount, modified, semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"), eu, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_3M_SWAP")), "EUR_3M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, act360, modified, quarterly, 2, simpleNameSecurityId("EURIBOR 3m"), eu, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_6M_SWAP")), "EUR_6M_SWAP", swapFixedDayCount, swapFixedBusinessDay,
        swapFixedPaymentFrequency, 2, eu, act360, modified, semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"), eu, true);

    // IR FUTURES
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_IR_FUTURE")), "EUR_IR_FUTURE", euriborDayCount, modified, Period.ofMonths(3),
        2, true, null);

    final int publicationLagON = 0;
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUROVERNIGHT")), "EUROVERNIGHT", act360, following, Period.ofDays(1), 2, false, eu, publicationLagON);
    utils.addConventionBundle(ExternalIdBundle.of(syntheticSecurityId("EONIA"), simpleNameSecurityId("EUR EONIA")), "EUR EONIA", act360, modified, Period.ofDays(1), 0, false, eu, publicationLagON);
    // OIS - EONIA
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_OIS_SWAP")), "EUR_OIS_SWAP", act360, modified, annual, 2, eu, act360, modified,
        annual, 2, simpleNameSecurityId("EUR EONIA"), eu, true, publicationLagON);

    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_3M_FRA")), "EUR_3M_FRA", thirty360, modified, quarterly, 2, eu, act360,
        modified, quarterly, 2, simpleNameSecurityId("EURIBOR 3m"), eu, true);
    utils.addConventionBundle(ExternalIdBundle.of(simpleNameSecurityId("EUR_6M_FRA")), "EUR_6M_FRA", thirty360, modified, annual, 2, eu, act360, modified,
        semiAnnual, 2, simpleNameSecurityId("EURIBOR 6m"), eu, true);

  }

}

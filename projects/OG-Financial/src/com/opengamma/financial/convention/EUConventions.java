/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
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
public class EUConventions {

  public static synchronized void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    //TODO looked at BSYM and the codes seem right but need to check
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR O/N")), "EUR LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR T/N")), "EUR LIBOR T/N", act360,
        following, Period.ofDays(1), 1, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 1w")), "EUR LIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 2w")), "EUR LIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 1m")), "EUR LIBOR 1m", act360,
        modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 2m")), "EUR LIBOR 2m", act360,
        modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 3m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURLIBORP3M")), "EUR LIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 4m")), "EUR LIBOR 4m", act360,
        modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 5m")), "EUR LIBOR 5m", act360,
        modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURLIBORP6M")), "EUR LIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 7m")), "EUR LIBOR 7m", act360,
        modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 8m")), "EUR LIBOR 8m", act360,
        modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 9m")), "EUR LIBOR 9m", act360,
        modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 10m")), "EUR LIBOR 10m", act360,
        modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 11m")), "EUR LIBOR 11m", act360,
        modified, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EU0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 12m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURLIBORP12M")), "EUR LIBOR 12m", act360, modified, Period.ofMonths(12), 2, false, null);

    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 1w")),
        "EURIBOR 1w", act360, following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 2w")),
        "EURIBOR 2w", act360, following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 1m")),
        "EURIBOR 1m", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 2m")),
        "EURIBOR 2m", act360, modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 3m")),
        "EURIBOR 3m", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 4m")),
        "EURIBOR 4m", act360, modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 5m")),
        "EURIBOR 5m", act360, modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 6m")),
        "EURIBOR 6m", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 7m")),
        "EURIBOR 7m", act360, modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 8m")),
        "EURIBOR 8m", act360, modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 9m")),
        "EURIBOR 9m", act360, modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 10m")), "EURIBOR 10m", act360,
        modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 11m")), "EURIBOR 11m", act360,
        modified, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUR012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURIBOR 12m")), "EURIBOR 12m", act360,
        modified, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EONIA Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR EONIA")),
        "EUR EONIA", act360, modified, Period.ofDays(1), 0, false, null);

    //TODO need to check that these are right for deposit rates
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1d")), "EUR DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2d")), "EUR DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3d")), "EUR DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1w")), "EUR DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2w")), "EUR DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3w")), "EUR DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1m")), "EUR DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2m")), "EUR DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3m")), "EUR DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 4m")), "EUR DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 5m")), "EUR DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 6m")), "EUR DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 7m")), "EUR DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 8m")), "EUR DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 9m")), "EUR DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 10m")), "EUR DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 11m")), "EUR DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 1y")), "EUR DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 2y")), "EUR DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 3y")), "EUR DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 4y")), "EUR DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR DEPOSIT 5y")), "EUR DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, null);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR0CF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 3x6")), "EUR FRA 3x9", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR0FI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 6x9")), "EUR FRA 6x12", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR0F1C Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 9x12")), "EUR FRA 9x15", act360,
        following, Period.ofMonths(3), 2, false, null);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR0CI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 3x9")), "EUR FRA 3x9", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR0F1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 6x12")), "EUR FRA 6x12", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR0I1C Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 9x15")), "EUR FRA 9x15", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("EUFR011F Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR FRA 12x18")), "EUR FRA 12x18", act360,
        following, Period.ofMonths(6), 2, false, null);

    //TODO holiday associated with EUR swaps is TARGET
    final ExternalId eu = RegionUtils.financialRegionId("EU");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_SWAP")), "EUR_SWAP", thirty360, modified, annual, 2, eu, act360,
        modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 6m"), eu, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_3M_SWAP")), "EUR_3M_SWAP", thirty360, modified, annual, 2, eu,
        act360, modified, quarterly, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 3m"), eu, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_6M_SWAP")), "EUR_6M_SWAP", thirty360, modified, annual, 2, eu,
        act360, modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR LIBOR 6m"), eu, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_OIS_SWAP")), "EUR_OIS_SWAP", act360, modified, annual, 2, eu,
        act360, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR EONIA"), eu);
    conventionMaster
        .addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_OIS_CASH")), "EUR_OIS_CASH", act360, following, null, 2, false, null);
    //TODO Check this, it's just copied from the US one.
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EUR_IBOR_INDEX")), "EUR_IBOR_INDEX", act360, following, 2, false);

    //Identifiers for external data 
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP1D"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP1D")),
        "EURCASHP1D", act360, following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP1M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP1M")),
        "EURCASHP1M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP2M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP2M")),
        "EURCASHP2M", act360, modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP3M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP3M")),
        "EURCASHP3M", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP4M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP4M")),
        "EURCASHP4M", act360, modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP5M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP5M")),
        "EURCASHP5M", act360, modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP6M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP6M")),
        "EURCASHP6M", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP7M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP7M")),
        "EURCASHP7M", act360, modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP8M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP8M")),
        "EURCASHP8M", act360, modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP9M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP9M")),
        "EURCASHP9M", act360, modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP10M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP10M")),
        "EURCASHP10M", act360, modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP11M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP11M")),
        "EURCASHP11M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURCASHP12M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURCASHP12M")),
        "EURCASHP12M", act360, modified, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP2Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP2Y")),
        "EURSWAPP2Y", thirty360, modified, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP3Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP3Y")),
        "EURSWAPP3Y", thirty360, modified, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP4Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP4Y")),
        "EURSWAPP4Y", thirty360, modified, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP5Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP5Y")),
        "EURSWAPP5Y", thirty360, modified, Period.ofYears(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP6Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP6Y")),
        "EURSWAPP6Y", thirty360, modified, Period.ofYears(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP7Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP7Y")),
        "EURSWAPP7Y", thirty360, modified, Period.ofYears(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP8Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP8Y")),
        "EURSWAPP8Y", thirty360, modified, Period.ofYears(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP9Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP9Y")),
        "EURSWAPP9Y", thirty360, modified, Period.ofYears(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP10Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP10Y")),
        "EURSWAPP10Y", thirty360, modified, Period.ofYears(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP12Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP12Y")),
        "EURSWAPP12Y", thirty360, modified, Period.ofYears(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP15Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP15Y")),
        "EURSWAPP15Y", thirty360, modified, Period.ofYears(15), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP20Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP20Y")),
        "EURSWAPP20Y", thirty360, modified, Period.ofYears(20), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP25Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP25Y")),
        "EURSWAPP25Y", thirty360, modified, Period.ofYears(25), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP30Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP30Y")),
        "EURSWAPP30Y", thirty360, modified, Period.ofYears(30), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP40Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP40Y")),
        "EURSWAPP40Y", thirty360, modified, Period.ofYears(40), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP50Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP50Y")),
        "EURSWAPP50Y", thirty360, modified, Period.ofYears(50), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "EURSWAPP80Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "EURSWAPP80Y")),
        "EURSWAPP80Y", thirty360, modified, Period.ofYears(80), 2, false, null);
  }

}

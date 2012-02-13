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
public class CHConventions {

  public static synchronized void addFixedIncomeInstrumentConvnetions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    final Frequency quarterly = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.QUARTERLY_NAME);
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);
    //TODO check that it's actually libor that we need
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF00O/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR O/N")), "CHF LIBOR O/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF00S/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR S/N")), "CHF LIBOR S/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF00T/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR T/N")), "CHF LIBOR T/N", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0001W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 1w")), "CHF LIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0002W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 2w")), "CHF LIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0001M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 1m")), "CHF LIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0002M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 2m")), "CHF LIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0003M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 3m")), "CHF LIBOR 3m", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0004M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 4m")), "CHF LIBOR 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0005M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 5m")), "CHF LIBOR 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0006M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 6m"),
            ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFLIBORP6M")), "CHF LIBOR 6m", act360, following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0007M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 7m")), "CHF LIBOR 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0008M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 8m")), "CHF LIBOR 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0009M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 9m")), "CHF LIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0010M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 10m")), "CHF LIBOR 10m", act360,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0011M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 11m")), "CHF LIBOR 11m", act360,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SF0012M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 12m")), "CHF LIBOR 12m", act360,
        following, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("TOISTOIS Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF TOISTOIS")), "CHF TOIS TOIS", act360,
        following, Period.ofDays(1), 2, false, null);

    //TODO need to check that these are right for deposit rates
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 1d")), "CHF DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 2d")), "CHF DEPOSIT 2d", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 3d")), "CHF DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 1w")), "CHF DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 2w")), "CHF DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 3w")), "CHF DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 1m")), "CHF DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 2m")), "CHF DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 3m")), "CHF DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 4m")), "CHF DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 5m")), "CHF DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 6m")), "CHF DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 7m")), "CHF DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 8m")), "CHF DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 9m")), "CHF DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 10m")), "CHF DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 11m")), "CHF DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 1y")), "CHF DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 2y")), "CHF DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 3y")), "CHF DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 4y")), "CHF DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF DEPOSIT 5y")), "CHF DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, null);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFFR0CI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF FRA 3x9")), "CHF FRA 3x9", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFFR0F1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF FRA 6x12")), "CHF FRA 6x12", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("SFFR0I1C Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF FRA 9x15")), "CHF FRA 9x15", act360,
        following, Period.ofMonths(6), 2, false, null);

    //TODO holiday associated with CHF swaps is Zurich
    final ExternalId ch = RegionUtils.financialRegionId("CH");
    //TODO check reference rate
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF_SWAP")), "CHF_SWAP", thirty360, modified, annual, 2, ch, act360,
        modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 6m"), ch, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF_3M_SWAP")), "CHF_3M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, quarterly, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 3m"), ch, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF_6M_SWAP")), "CHF_6M_SWAP", thirty360, modified, annual, 2, ch,
        act360, modified, semiAnnual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF LIBOR 6m"), ch, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF_OIS_SWAP")), "CHF_OIS_SWAP", act360, modified, annual, 2, ch,
        act360, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF TOISTOIS"), ch);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF_OIS_CASH")), "CHF_OIS_CASH", act360, modified, annual, 2, ch,
        act360, modified, annual, 2, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF TOISTOIS"), ch);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHF_IBOR_INDEX")), "CHF_IBOR_INDEX", act360, following, 2, false);

    //Identifiers for external data
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP1D"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP1D")),
        "CHFCASHP1D", act360, following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP1M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP1M")),
        "CHFCASHP1M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP2M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP2M")),
        "CHFCASHP2M", act360, modified, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP3M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP3M")),
        "CHFCASHP3M", act360, modified, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP4M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP4M")),
        "CHFCASHP4M", act360, modified, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP5M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP5M")),
        "CHFCASHP5M", act360, modified, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP6M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP6M")),
        "CHFCASHP6M", act360, modified, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP7M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP7M")),
        "CHFCASHP7M", act360, modified, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP8M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP8M")),
        "CHFCASHP8M", act360, modified, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP9M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP9M")),
        "CHFCASHP9M", act360, modified, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP10M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP10M")),
        "CHFCASHP10M", act360, modified, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP11M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP11M")),
        "CHFCASHP11M", act360, modified, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFCASHP12M"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFCASHP12M")),
        "CHFCASHP12M", act360, modified, Period.ofMonths(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP2Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP2Y")),
        "CHFSWAPP2Y", thirty360, modified, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP3Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP3Y")),
        "CHFSWAPP3Y", thirty360, modified, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP4Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP4Y")),
        "CHFSWAPP4Y", thirty360, modified, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP5Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP5Y")),
        "CHFSWAPP5Y", thirty360, modified, Period.ofYears(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP6Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP6Y")),
        "CHFSWAPP6Y", thirty360, modified, Period.ofYears(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP7Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP7Y")),
        "CHFSWAPP7Y", thirty360, modified, Period.ofYears(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP8Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP8Y")),
        "CHFSWAPP8Y", thirty360, modified, Period.ofYears(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP9Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP9Y")),
        "CHFSWAPP9Y", thirty360, modified, Period.ofYears(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP10Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP10Y")),
        "CHFSWAPP10Y", thirty360, modified, Period.ofYears(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP12Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP12Y")),
        "CHFSWAPP12Y", thirty360, modified, Period.ofYears(12), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP15Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP15Y")),
        "CHFSWAPP15Y", thirty360, modified, Period.ofYears(15), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP20Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP20Y")),
        "CHFSWAPP20Y", thirty360, modified, Period.ofYears(20), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP25Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP25Y")),
        "CHFSWAPP25Y", thirty360, modified, Period.ofYears(25), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP30Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP30Y")),
        "CHFSWAPP30Y", thirty360, modified, Period.ofYears(30), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP40Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP40Y")),
        "CHFSWAPP40Y", thirty360, modified, Period.ofYears(40), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP50Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP50Y")),
        "CHFSWAPP50Y", thirty360, modified, Period.ofYears(50), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CHFSWAPP80Y"), ExternalId.of(InMemoryConventionBundleMaster.OG_SYNTHETIC_TICKER, "CHFSWAPP80Y")),
        "CHFSWAPP80Y", thirty360, modified, Period.ofYears(80), 2, false, null);
  }

  //TODO all of the conventions named treasury need to be changed
  public static void addTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CH_TREASURY_BOND_CONVENTION")), "HU_TREASURY_BOND_CONVENTION", true,
        true, 0, 3, true);
  }

  public static void addCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "conventionMaster");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "CH_CORPORATE_BOND_CONVENTION")), "HU_CORPORATE_BOND_CONVENTION", true,
        true, 0, 3, true);
  }

}

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
public class DKConventions {

  public static void addFixedIncomeInstrumentConventions(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    final BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final DayCount thirty360 = DayCountFactory.INSTANCE.getDayCount("30/360");
    final Frequency semiAnnual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.SEMI_ANNUAL_NAME);
    final Frequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency(Frequency.ANNUAL_NAME);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO01W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 1w")), "DKK CIBOR 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO02W Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 2w")), "DKK CIBOR 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO01M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 1m")), "DKK CIBOR 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO02M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 2m")), "DKK CIBOR 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO03M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 3m")), "DKK CIBOR 3m", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO04M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 4m")), "DKK CIBOR 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO05M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 5m")), "DKK CIBOR 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO06M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 6m")), "DKK CIBOR 6m", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO07M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 7m")), "DKK CIBOR 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO08M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 8m")), "DKK CIBOR 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("CIBO09M Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 9m")), "DKK CIBOR 9m", act360,
        following, Period.ofMonths(9), 2, false, null);

    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR1T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 1d")), "DKK DEPOSIT 1d", act360,
        following, Period.ofDays(1), 0, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR2T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 2d")), "DKK DEPOSIT 2d", act360,
        following, Period.ofDays(1), 1, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR3T Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 3d")), "DKK DEPOSIT 3d", act360,
        following, Period.ofDays(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR1Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 1w")), "DKK DEPOSIT 1w", act360,
        following, Period.ofDays(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR2Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 2w")), "DKK DEPOSIT 2w", act360,
        following, Period.ofDays(14), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR3Z Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 3w")), "DKK DEPOSIT 3w", act360,
        following, Period.ofDays(21), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRA Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 1m")), "DKK DEPOSIT 1m", act360,
        following, Period.ofMonths(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRB Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 2m")), "DKK DEPOSIT 2m", act360,
        following, Period.ofMonths(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRC Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 3m")), "DKK DEPOSIT 3m", act360,
        following, Period.ofMonths(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRD Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 4m")), "DKK DEPOSIT 4m", act360,
        following, Period.ofMonths(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRE Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 5m")), "DKK DEPOSIT 5m", act360,
        following, Period.ofMonths(5), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRF Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 6m")), "DKK DEPOSIT 6m", act360,
        following, Period.ofMonths(6), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRG Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 7m")), "DKK DEPOSIT 7m", act360,
        following, Period.ofMonths(7), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRH Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 8m")), "DKK DEPOSIT 8m", act360,
        following, Period.ofMonths(8), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRI Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 9m")), "DKK DEPOSIT 9m", act360,
        following, Period.ofMonths(9), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRJ Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 10m")), "DKK DEPOSIT 10m", act360,
        following, Period.ofMonths(10), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDRK Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 11m")), "DKK DEPOSIT 11m", act360,
        following, Period.ofMonths(11), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR1 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 1y")), "DKK DEPOSIT 1y", act360,
        following, Period.ofYears(1), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR2 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 2y")), "DKK DEPOSIT 2y", act360,
        following, Period.ofYears(2), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR3 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 3y")), "DKK DEPOSIT 3y", act360,
        following, Period.ofYears(3), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR4 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 4y")), "DKK DEPOSIT 4y", act360,
        following, Period.ofYears(4), 2, false, null);
    conventionMaster.addConventionBundle(
        ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DKDR5 Curncy"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK DEPOSIT 5y")), "DKK DEPOSIT 5y", act360,
        following, Period.ofYears(5), 2, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId("DETNT/N Index"), ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK T/N")),
        "DKK T/N", act360, following, Period.ofDays(1), 1, false, null);

    final ExternalId dk = RegionUtils.financialRegionId("DK");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK_SWAP")), "DKK_SWAP", thirty360, modified, annual, 1, dk, act360,
        modified, semiAnnual, 1, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 6m"), dk, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK_6M_SWAP")), "DKK_6M_SWAP", thirty360, modified, annual, 1, dk,
        act360, modified, semiAnnual, 1, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK CIBOR 6m"), dk, true);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK_OIS_SWAP")), "DKK_OIS_SWAP", act360, modified, annual, 1, dk,
        act360, modified, annual, 1, ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK T/N"), dk, true);
    conventionMaster
        .addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK_OIS_CASH")), "DKK_OIS_CASH", act360, following, null, 1, false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKK_IBOR_INDEX")), "DKK_IBOR_INDEX", act360, following, 1, false);

    //Identifiers for external data 
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP1D")), "DKKCASHP1D", act360, following, Period.ofDays(1), 0,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP1M")), "DKKCASHP1M", act360, modified, Period.ofMonths(1), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP2M")), "DKKCASHP2M", act360, modified, Period.ofMonths(2), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP3M")), "DKKCASHP3M", act360, modified, Period.ofMonths(3), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP4M")), "DKKCASHP4M", act360, modified, Period.ofMonths(4), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP5M")), "DKKCASHP5M", act360, modified, Period.ofMonths(5), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP6M")), "DKKCASHP6M", act360, modified, Period.ofMonths(6), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP7M")), "DKKCASHP7M", act360, modified, Period.ofMonths(7), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP8M")), "DKKCASHP8M", act360, modified, Period.ofMonths(8), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP9M")), "DKKCASHP9M", act360, modified, Period.ofMonths(9), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP10M")), "DKKCASHP10M", act360, modified, Period.ofMonths(10), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP11M")), "DKKCASHP11M", act360, modified, Period.ofMonths(11), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKCASHP12M")), "DKKCASHP12M", act360, modified, Period.ofMonths(12), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP2Y")), "DKKSWAPP2Y", act360, modified, Period.ofYears(2), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP3Y")), "DKKSWAPP3Y", act360, modified, Period.ofYears(3), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP4Y")), "DKKSWAPP4Y", act360, modified, Period.ofYears(4), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP5Y")), "DKKSWAPP5Y", act360, modified, Period.ofYears(5), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP6Y")), "DKKSWAPP6Y", act360, modified, Period.ofYears(6), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP7Y")), "DKKSWAPP7Y", act360, modified, Period.ofYears(7), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP8Y")), "DKKSWAPP8Y", act360, modified, Period.ofYears(8), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP9Y")), "DKKSWAPP9Y", act360, modified, Period.ofYears(9), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP10Y")), "DKKSWAPP10Y", act360, modified, Period.ofYears(10), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP12Y")), "DKKSWAPP12Y", act360, modified, Period.ofYears(12), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP15Y")), "DKKSWAPP15Y", act360, modified, Period.ofYears(15), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP20Y")), "DKKSWAPP20Y", act360, modified, Period.ofYears(20), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP25Y")), "DKKSWAPP25Y", act360, modified, Period.ofYears(25), 2,
        false, null);
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DKKSWAPP30Y")), "DKKSWAPP30Y", act360, modified, Period.ofYears(30), 2,
        false, null);
  }

  //TODO all of the conventions named treasury need to be changed
  //TODO the ex-dividend days is wrong
  public static void addDKTreasuryBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DK_TREASURY_BOND_CONVENTION")), "DK_TREASURY_BOND_CONVENTION", true,
        true, 30, 3, true);
  }

  //TODO all of the conventions named treasury need to be changed
  //TODO the ex-dividend days is wrong
  public static void addDKCorporateBondConvention(final ConventionBundleMaster conventionMaster) {
    Validate.notNull(conventionMaster, "convention master");
    conventionMaster.addConventionBundle(ExternalIdBundle.of(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "DK_CORPORATE_BOND_CONVENTION")), "DK_CORPORATE_BOND_CONVENTION", true,
        true, 30, 3, true);
  }

}

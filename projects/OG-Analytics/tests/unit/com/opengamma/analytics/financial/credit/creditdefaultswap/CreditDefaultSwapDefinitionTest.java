/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CreditRating;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.Region;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.Sector;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.PresentValueCreditDefaultSwapTest.MyCalendar;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 *  Tests to verify the correct construction of a CDS contract 
 */
public class CreditDefaultSwapDefinitionTest {

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // CDS contract parameters (don't need the curves in this case)

  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

  private static final String protectionBuyer = "ABC";
  private static final String protectionSeller = "XYZ";
  private static final String referenceEntityTicker = "MSFT";
  private static final String referenceEntityShortName = "Microsoft";
  private static final String referenceEntityREDCode = "ABC123";

  private static final Currency currency = Currency.USD;

  private static final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
  private static final RestructuringClause restructuringClause = RestructuringClause.NORE;

  private static final CreditRating compositeRating = CreditRating.AA;
  private static final CreditRating impliedRating = CreditRating.A;

  private static final Sector sector = Sector.INDUSTRIALS;
  private static final Region region = Region.NORTHAMERICA;
  private static final String country = "United States";

  private static final Calendar calendar = new MyCalendar();

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2007, 10, 22);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2007, 10, 23);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2012, 12, 20);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2008, 10, 22);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = true;
  private static final boolean adjustMaturityDate = true;

  private static final double notional = 10000000.0;
  private static final double parSpread = 60.0;
  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;
  private static final boolean includeAccruedPremium = true;

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  // Tests of the construction of a CDS contract object

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuySellField() {
    new CreditDefaultSwapDefinition(null, protectionBuyer, protectionSeller, referenceEntityTicker, referenceEntityShortName, referenceEntityREDCode,
        currency, debtSeniority, restructuringClause, compositeRating, impliedRating, sector, region, country, calendar, startDate, effectiveDate,
        maturityDate, valuationDate, stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional,
        parSpread,
        valuationRecoveryRate, curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProtectionBuyer() {
    new CreditDefaultSwapDefinition(buySellProtection, null, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyProtectionBuyer() {
    new CreditDefaultSwapDefinition(buySellProtection, "", protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullProtectionSeller() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, null, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyProtectionSeller() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, "", referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReferenceEntityTicker() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, null,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyReferenceEntityTicker() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, "",
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReferenceEntityShortName() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        null, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyReferenceEntityShortName() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        "", referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullReferenceEntityREDCode() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, null, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyReferenceEntityREDCode() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, "", currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, null, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDebtSeniority() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, null, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRestructuringClause() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, null, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCompositeRating() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, null,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullImpliedRating() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        null, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSector() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, null, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegion() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, null, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCountry() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, null, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyCountry() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, "", calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, null, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDate() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, null, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEffectiveDate() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, null, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturityDate() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, null, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValuationDate() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, null,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartDateBeforeValuationDate() {

    final ZonedDateTime testValuationDate = startDate.minusDays(1);

    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, testValuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartDateBeforeEffectiveDate() {

    final ZonedDateTime testEffectiveDate = startDate.minusDays(1);

    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, testEffectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartDateBeforeMaturityDate() {

    final ZonedDateTime testMaturityDate = startDate.minusDays(1);

    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, testMaturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValuationDateBeforeMaturityDate() {

    final ZonedDateTime testValuationDate = maturityDate.minusDays(-1);

    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, testValuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValuationDateAfterEffectiveDate() {

    final ZonedDateTime testValuationDate = effectiveDate.minusDays(1);

    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, testValuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullstubType() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        null, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCouponFrequency() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, null, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDaycountFractionConvention() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, null, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessdayAdjustmentConvention() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, null, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveNotional() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, -notional, parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveParSpread() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, -parSpread, valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValuationRecoveryRateGreaterThanZero() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, -valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValuationRecoveryRateLessThanOne() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread,
        1.0 + valuationRecoveryRate,
        curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCurveRecoveryRateGreaterThanZero() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        -curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCurveRecoveryRateLessThanOne() {
    new CreditDefaultSwapDefinition(buySellProtection, protectionBuyer, protectionSeller, referenceEntityTicker,
        referenceEntityShortName, referenceEntityREDCode, currency, debtSeniority, restructuringClause, compositeRating,
        impliedRating, sector, region, country, calendar, startDate, effectiveDate, maturityDate, valuationDate,
        stubType, couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustMaturityDate, notional, parSpread, valuationRecoveryRate,
        1.0 + curveRecoveryRate, includeAccruedPremium);
  }

  // --------------------------------------------------------------------------------------------------------------------------------------------------
}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSCoupon;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligor.CreditRating;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligor.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligor.Region;
import com.opengamma.analytics.financial.credit.obligor.Sector;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * @deprecated Uses dummy obligors
 */
@Deprecated
public class CreditDefaultSwapSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<LegacyVanillaCreditDefaultSwapDefinition> {
  private static final Obligor DUMMY_OBLIGOR_A = new Obligor(
      "Dummy_A",
      "Dummy_A",
      "Dummy_A",
      CreditRating.A,
      CreditRating.A,
      CreditRatingMoodys.A,
      CreditRatingStandardAndPoors.A,
      CreditRatingFitch.A,
      false,
      Sector.BASICMATERIALS,
      Region.EUROPE,
      "CA");
  private static final Obligor DUMMY_OBLIGOR_B = new Obligor(
      "Dummy_B",
      "Dummy_B",
      "Dummy_B",
      CreditRating.A,
      CreditRating.A,
      CreditRatingMoodys.A,
      CreditRatingStandardAndPoors.A,
      CreditRatingFitch.A,
      false,
      Sector.BASICMATERIALS,
      Region.ASIA,
      "NY");
  private static final Obligor DUMMY_OBLIGOR_C = new Obligor(
      "Dummy_C",
      "Dummy_C",
      "Dummy_C",
      CreditRating.A,
      CreditRating.A,
      CreditRatingMoodys.A,
      CreditRatingStandardAndPoors.A,
      CreditRatingFitch.A,
      false,
      Sector.BASICMATERIALS,
      Region.NORTHAMERICA,
      "NJ");
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final double _recoveryRate;

  public CreditDefaultSwapSecurityConverterDeprecated(final HolidaySource holidaySource, final RegionSource regionSource, final double recoveryRate) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _recoveryRate = recoveryRate;
  }

  @Override
  public LegacyVanillaCreditDefaultSwapDefinition visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
    final ExternalId regionId = security.getRegionId();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, _regionSource.getHighestLevelRegion(regionId));
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime effectiveDate = security.getEffectiveDate();
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final PeriodFrequency couponFrequency = getPeriodFrequency(security.getCouponFrequency());
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDayConvention = security.getBusinessDayConvention();
    final boolean immAdjustMaturityDate = security.isImmAdjustMaturityDate();
    final boolean adjustEffectiveDate = security.isAdjustEffectiveDate();
    final boolean adjustMaturityDate = security.isAdjustMaturityDate();
    final InterestRateNotional notional = security.getNotional();
    final Currency currency = notional.getCurrency();
    final DebtSeniority debtSeniority = security.getDebtSeniority();
    final RestructuringClause restructuringClause = security.getRestructuringClause();
    final double amount = notional.getAmount();
    final double recoveryRate = _recoveryRate;
    final boolean includeAccruedPremium = security.isIncludeAccruedPremium();
    final boolean protectionStart = security.isProtectionStart();
    final double quotedSpread = security.getQuotedSpread();
    final StandardCDSCoupon premiumLegCoupon = getCoupon(security.getCoupon());
    final double upFrontAmount = security.getUpfrontAmount().getAmount();
    final StubType stubType = security.getStubType().toAnalyticsType();
    final ZonedDateTime cashSettlementDate = security.getCashSettlementDate();
    final boolean adjustCashSettlementDate = security.isAdjustCashSettlementDate();
    final double coupon = security.getCoupon();
    return new LegacyVanillaCreditDefaultSwapDefinition(buySellProtection, DUMMY_OBLIGOR_A, DUMMY_OBLIGOR_B, DUMMY_OBLIGOR_C, currency,
        debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate,
        amount, recoveryRate, includeAccruedPremium, protectionStart, coupon);
  }

  @Override
  public LegacyVanillaCreditDefaultSwapDefinition visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    ArgumentChecker.notNull(security, "security");
    final BuySellProtection buySellProtection = security.isBuy() ? BuySellProtection.BUY : BuySellProtection.SELL;
    final ExternalId regionId = security.getRegionId();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, _regionSource.getHighestLevelRegion(regionId));
    final ZonedDateTime startDate = security.getStartDate();
    final ZonedDateTime effectiveDate = security.getEffectiveDate();
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final PeriodFrequency couponFrequency = getPeriodFrequency(security.getCouponFrequency());
    final DayCount dayCount = security.getDayCount();
    final BusinessDayConvention businessDayConvention = security.getBusinessDayConvention();
    final boolean immAdjustMaturityDate = security.isImmAdjustMaturityDate();
    final boolean adjustEffectiveDate = security.isAdjustEffectiveDate();
    final boolean adjustMaturityDate = security.isAdjustMaturityDate();
    final InterestRateNotional notional = security.getNotional();
    final Currency currency = notional.getCurrency();
    final DebtSeniority debtSeniority = security.getDebtSeniority();
    final RestructuringClause restructuringClause = security.getRestructuringClause();
    final double amount = notional.getAmount();
    final double recoveryRate = _recoveryRate;
    final boolean includeAccruedPremium = security.isIncludeAccruedPremium();
    final boolean protectionStart = security.isProtectionStart();
    final StubType stubType = security.getStubType().toAnalyticsType();
    final double parSpread = security.getParSpread();
    return new LegacyVanillaCreditDefaultSwapDefinition(buySellProtection, DUMMY_OBLIGOR_A, DUMMY_OBLIGOR_B, DUMMY_OBLIGOR_C, currency,
        debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate,
        amount, recoveryRate, includeAccruedPremium, protectionStart, parSpread);
  }

  private PeriodFrequency getPeriodFrequency(final Frequency frequency) {
    if (frequency instanceof PeriodFrequency) {
      return (PeriodFrequency) frequency;
    }
    if (frequency instanceof SimpleFrequency) {
      return ((SimpleFrequency) frequency).toPeriodFrequency();
    }
    throw new OpenGammaRuntimeException("Can only handle PeriodFrequency and SimpleFrequency");
  }

  private StandardCDSCoupon getCoupon(final double coupon) {
    if (Double.compare(coupon, 25) == 0) {
      return StandardCDSCoupon._25bps;
    }
    if (Double.compare(coupon, 100) == 0) {
      return StandardCDSCoupon._100bps;
    }
    if (Double.compare(coupon, 500) == 0) {
      return StandardCDSCoupon._500bps;
    }
    if (Double.compare(coupon, 750) == 0) {
      return StandardCDSCoupon._750bps;
    }
    if (Double.compare(coupon, 1000) == 0) {
      return StandardCDSCoupon._1000bps;
    }
    throw new OpenGammaRuntimeException("Could not identify coupon with value " + coupon);
  }

}

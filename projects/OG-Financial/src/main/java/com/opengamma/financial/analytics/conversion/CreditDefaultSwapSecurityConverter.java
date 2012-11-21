/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSCoupon;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRating;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingFitch;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingMoodys;
import com.opengamma.analytics.financial.credit.obligormodel.CreditRatingStandardAndPoors;
import com.opengamma.analytics.financial.credit.obligormodel.Region;
import com.opengamma.analytics.financial.credit.obligormodel.Sector;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
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
 *
 */
public class CreditDefaultSwapSecurityConverter extends FinancialSecurityVisitorAdapter<CreditDefaultSwapDefinition> {
  private static final Obligor DUMMY_OBLIGOR = new Obligor(
      "",
      "",
      "",
      CreditRating.A,
      CreditRating.A,
      CreditRatingMoodys.A,
      CreditRatingStandardAndPoors.A,
      CreditRatingFitch.A,
      false,
      Sector.BASICMATERIALS,
      Region.NORTHAMERICA,
      "CA");
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;

  public CreditDefaultSwapSecurityConverter(final HolidaySource holidaySource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  @Override
  public StandardCreditDefaultSwapDefinition visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
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
    final double recoveryRate = security.getRecoveryRate();
    final boolean includeAccruedPremium = security.isIncludeAccruedPremium();
    final boolean protectionStart = security.isProtectionStart();
    final double quotedSpread = security.getQuotedSpread();
    final StandardCDSCoupon premiumLegCoupon = getCoupon(security.getCoupon());
    final double upFrontAmount = security.getUpfrontAmount().getAmount();
    final StubType stubType = security.getStubType().toAnalyticsType();
    final ZonedDateTime cashSettlementDate = security.getCashSettlementDate();
    final boolean adjustCashSettlementDate = security.isAdjustCashSettlementDate();
    return new StandardCreditDefaultSwapDefinition(buySellProtection, DUMMY_OBLIGOR, DUMMY_OBLIGOR, DUMMY_OBLIGOR, currency,
        debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType, couponFrequency,
        dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, amount,
        recoveryRate, includeAccruedPremium, protectionStart, quotedSpread, premiumLegCoupon, upFrontAmount, cashSettlementDate,
        adjustCashSettlementDate);
  }

  @Override
  public LegacyCreditDefaultSwapDefinition visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
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
    final double recoveryRate = security.getRecoveryRate();
    final boolean includeAccruedPremium = security.isIncludeAccruedPremium();
    final boolean protectionStart = security.isProtectionStart();
    final StubType stubType = security.getStubType().toAnalyticsType();
    final double parSpread = security.getParSpread();
    return new LegacyCreditDefaultSwapDefinition(buySellProtection, DUMMY_OBLIGOR, DUMMY_OBLIGOR, DUMMY_OBLIGOR, currency,
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

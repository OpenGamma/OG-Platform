/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SwapSecurityConverter implements SwapSecurityVisitor<FixedIncomeInstrumentConverter<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public SwapSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource,
      final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FixedIncomeInstrumentConverter<?> visitSwapSecurity(final SwapSecurity security) {
    Validate.notNull(security, "swap security");
    final InterestRateInstrumentType swapType = SwapSecurityUtils.getSwapType(security);
    switch (swapType) {
      case SWAP_FIXED_IBOR:
        return SwapSecurityUtils.payFixed(security) ? getFixedIborSwapDefinition(security, true, false)
            : getFixedIborSwapDefinition(security, false, false);
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return SwapSecurityUtils.payFixed(security) ? getFixedIborSwapDefinition(security, true, true)
            : getFixedIborSwapDefinition(security, false, true);
      case SWAP_IBOR_IBOR:
        return getIborIborSwapDefinition(security);
      case SWAP_CMS_CMS:
        return getCMSCMSSwapDefinition(security);
      case SWAP_FIXED_CMS:
        return SwapSecurityUtils.payFixed(security) ? getFixedCMSSwapDefinition(security, true) :
            getFixedCMSSwapDefinition(security, false);
      case SWAP_IBOR_CMS:
        return getIborCMSSwapDefinition(security);
      default:
        throw new OpenGammaRuntimeException("Cannot handle swapType " + swapType);
    }
  }

  private SwapDefinition getFixedIborSwapDefinition(final SwapSecurity swapSecurity,
      final boolean payFixed, final boolean hasSpread) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String currencyString = currency.getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyString + "_SWAP"));
    final AnnuityCouponFixedDefinition fixedLegDefinition = getFixedSwapLegDefinition(effectiveDate, maturityDate,
        fixedLeg, calendar, currency, conventions, payFixed);

    final AnnuityDefinition<? extends PaymentDefinition> floatingLegDefinition = hasSpread ?
        getFloatingSwapLegWithSpreadDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, !payFixed)
        : getIborSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, !payFixed);
    return new SwapFixedIborDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private SwapIborIborDefinition getIborIborSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final ExternalId regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final AnnuityCouponIborSpreadDefinition payLegDefinition = getFloatingSwapLegWithSpreadDefinition(effectiveDate,
        maturityDate, floatPayLeg, calendar, currency, true);
    final AnnuityCouponIborSpreadDefinition receiveLegDefinition = getFloatingSwapLegWithSpreadDefinition(effectiveDate,
        maturityDate, floatReceiveLeg, calendar, currency, false);
    return new SwapIborIborDefinition(payLegDefinition, receiveLegDefinition);
  }

  private SwapDefinition getCMSCMSSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final ExternalId regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final AnnuityCouponCMSDefinition cmsPayLeg = getCMSwapLegDefinition(effectiveDate, maturityDate, floatPayLeg, calendar, currency, true);
    final AnnuityCouponCMSDefinition cmsReceiveLeg = getCMSwapLegDefinition(effectiveDate, maturityDate, floatReceiveLeg, calendar, currency, false);
    return new SwapDefinition(cmsPayLeg, cmsReceiveLeg);
  }

  private SwapDefinition getFixedCMSSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAP"));

    final AnnuityCouponFixedDefinition fixedAnnuity = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, currency, conventions, true);
    final AnnuityCouponCMSDefinition cmsReceiveLeg = getCMSwapLegDefinition(effectiveDate, maturityDate, floatingLeg, calendar, currency, false);
    return payFixed ? new SwapDefinition(fixedAnnuity, cmsReceiveLeg) : new SwapDefinition(cmsReceiveLeg, fixedAnnuity);
  }

  private SwapDefinition getIborCMSSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final boolean payIbor = floatPayLeg.getFloatingRateType() == FloatingRateType.IBOR;
    final boolean receiveIbor = floatReceiveLeg.getFloatingRateType() == FloatingRateType.IBOR;
    if (receiveIbor == payIbor) {
      throw new OpenGammaRuntimeException("This should never happen");
    }
    final FloatingInterestRateLeg iborLeg = payIbor ? floatPayLeg : floatReceiveLeg;
    final FloatingInterestRateLeg cmsLeg = payIbor ? floatReceiveLeg : floatPayLeg;
    final ExternalId regionId = payLeg.getRegionIdentifier();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final AnnuityCouponCMSDefinition cmsAnnuity = getCMSwapLegDefinition(effectiveDate, maturityDate, cmsLeg, calendar, currency, !payIbor);
    final AnnuityCouponIborDefinition iborAnnuity = getIborSwapLegDefinition(effectiveDate, maturityDate, iborLeg, calendar, currency, payIbor);
    return payIbor ? new SwapDefinition(iborAnnuity, cmsAnnuity) : new SwapDefinition(cmsAnnuity, iborAnnuity);
  }

  private AnnuityCouponFixedDefinition getFixedSwapLegDefinition(final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate, final FixedInterestRateLeg fixedLeg, final Calendar calendar,
      final Currency currency, final ConventionBundle conventions, final boolean isPayer) {
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double fixedRate = fixedLeg.getRate() / 100; //TODO this should not be hard-coded here
    return AnnuityCouponFixedDefinition.from(((InterestRateNotional) fixedLeg.getNotional()).getCurrency(),
        effectiveDate, maturityDate, fixedLeg.getFrequency(), calendar, fixedLeg.getDayCount(),
        fixedLeg.getBusinessDayConvention(), conventions.isEOMConvention(), notional, fixedRate, isPayer);
  }

  private AnnuityCouponIborDefinition getIborSwapLegDefinition(final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    final Frequency freq = floatLeg.getFrequency();
    final Period tenor = getTenor(freq);

    ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateIdentifier());
    if (indexConvention == null) {
      final ExternalId bbgIdentifier = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, floatLeg.getFloatingReferenceRateIdentifier().getValue());
      indexConvention = _conventionSource.getConventionBundle(bbgIdentifier);
      if (indexConvention == null) {
        throw new OpenGammaRuntimeException("Could not get ibor index convention for " + currency + " using " + floatLeg.getFloatingReferenceRateIdentifier());
      }
    }
    final IborIndex index = new IborIndex(currency, tenor, indexConvention.getSettlementDays(), calendar,
        indexConvention.getDayCount(), indexConvention.getBusinessDayConvention(), indexConvention.isEOMConvention());
    return AnnuityCouponIborDefinition.from(effectiveDate, maturityDate, notional, index, isPayer);
  }

  private AnnuityCouponIborSpreadDefinition getFloatingSwapLegWithSpreadDefinition(final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final double spread = floatLeg.getSpread();
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    final Frequency freq = floatLeg.getFrequency();
    final Period tenor = getTenor(freq);
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateIdentifier());
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor index convention for " + currency);
    }
    final IborIndex index = new IborIndex(currency, tenor, indexConvention.getSettlementDays(), calendar,
        indexConvention.getDayCount(), indexConvention.getBusinessDayConvention(), indexConvention.isEOMConvention());

    return AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, notional, index, spread, isPayer);
  }

  private AnnuityCouponCMSDefinition getCMSwapLegDefinition(final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    final Frequency freq = floatLeg.getFrequency();
    final Period tenor = getTenor(freq);
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateIdentifier());
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor index convention for " + currency);
    }
    final ConventionBundle swapRateConvention = _conventionSource.getConventionBundle(ExternalId.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAP"));
    final IborIndex iborIndex = new IborIndex(currency, tenor, indexConvention.getSettlementDays(), calendar,
        indexConvention.getDayCount(), indexConvention.getBusinessDayConvention(), indexConvention.isEOMConvention());
    //TODO these next three fields aren't right
    final DayCount fixedLegDayCount = swapRateConvention.getSwapFixedLegDayCount();
    final Period fixedLegPeriod = tenor;
    final DayCount dayCount = swapRateConvention.getSwapFloatingLegDayCount();
    final CMSIndex cmsIndex = new CMSIndex(fixedLegPeriod, fixedLegDayCount, iborIndex, tenor);
    return AnnuityCouponCMSDefinition.from(effectiveDate, maturityDate, notional, cmsIndex, tenor, dayCount, isPayer);
  }

  // FIXME: convert frequency to period in a better way
  private Period getTenor(final Frequency freq) {
    Period tenor;
    if (freq.getConventionName() == Frequency.ANNUAL_NAME) {
      tenor = Period.ofMonths(12);
    } else if (freq.getConventionName() == Frequency.SEMI_ANNUAL_NAME) {
      tenor = Period.ofMonths(6);
    } else if (freq.getConventionName() == Frequency.QUARTERLY_NAME) {
      tenor = Period.ofMonths(3);
    } else if (freq.getConventionName() == Frequency.MONTHLY_NAME) {
      tenor = Period.ofMonths(1);
    } else {
      throw new OpenGammaRuntimeException(
          "Can only handle annual, semi-annual, quarterly and monthly frequencies for floating swap legs");
    }
    return tenor;
  }
}

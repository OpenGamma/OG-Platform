/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponOISDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponOISSimplifiedDefinition;
import com.opengamma.financial.instrument.index.GeneratorOIS;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedOISDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedOISSimplifiedDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SwapSecurityConverter implements SwapSecurityVisitor<InstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;
  private final boolean _forCurves;

  public SwapSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource,
      final boolean forCurves) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _forCurves = forCurves;
  }

  @Override
  public InstrumentDefinition<?> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return visitSwapSecurity(security);
  }

  @Override
  public InstrumentDefinition<?> visitSwapSecurity(final SwapSecurity security) {
    ArgumentChecker.notNull(security, "swap security");
    final InterestRateInstrumentType swapType = SwapSecurityUtils.getSwapType(security);
    switch (swapType) {
      case SWAP_FIXED_IBOR:
        return SwapSecurityUtils.payFixed(security) ? getFixedIborSwapDefinition(security, true, false) : getFixedIborSwapDefinition(security, false, false);
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return SwapSecurityUtils.payFixed(security) ? getFixedIborSwapDefinition(security, true, true) : getFixedIborSwapDefinition(security, false, true);
      case SWAP_IBOR_IBOR:
        return getIborIborSwapDefinition(security);
      case SWAP_CMS_CMS:
        return getCMSCMSSwapDefinition(security);
      case SWAP_FIXED_CMS:
        return SwapSecurityUtils.payFixed(security) ? getFixedCMSSwapDefinition(security, true) : getFixedCMSSwapDefinition(security, false);
      case SWAP_IBOR_CMS:
        return getIborCMSSwapDefinition(security);
      case SWAP_FIXED_OIS:
        if (_forCurves) {
          return SwapSecurityUtils.payFixed(security) ? getSimplifiedOISSwapDefinition(security, true) : getSimplifiedOISSwapDefinition(security, false);
        }
        return SwapSecurityUtils.payFixed(security) ? getFixedOISSwapDefinition(security, true) : getFixedOISSwapDefinition(security, false);
      default:
        throw new OpenGammaRuntimeException("Cannot handle swapType " + swapType);
    }
  }

  private SwapDefinition getFixedIborSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed, final boolean hasSpread) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String currencyString = currency.getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyString + "_SWAP"));
    if (conventions == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + swapSecurity);
    }
    final AnnuityCouponFixedDefinition fixedLegDefinition = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, conventions, payFixed);
    if (hasSpread) {
      return new SwapFixedIborSpreadDefinition(fixedLegDefinition, getIborSwapLegWithSpreadDefinition(effectiveDate, maturityDate, (FloatingSpreadIRLeg) floatLeg, calendar, currency, !payFixed));
    }
    return new SwapFixedIborDefinition(fixedLegDefinition, getIborSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, !payFixed));
  }

  private SwapDefinition getFixedOISSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String currencyString = currency.getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyString + "_OIS_SWAP"));
    if (conventions == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + swapSecurity);
    }
    final AnnuityCouponFixedDefinition fixedLegDefinition = getFixedSwapLegDefinitionForOIS(effectiveDate, maturityDate, fixedLeg, calendar, conventions, payFixed);
    final AnnuityCouponOISDefinition floatingLegDefinition = getOISSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, !payFixed);
    return new SwapFixedOISDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private SwapDefinition getSimplifiedOISSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String currencyString = currency.getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyString + "_OIS_SWAP"));
    if (conventions == null) {
      throw new OpenGammaRuntimeException("Could not get convention called " + currencyString + "_OIS_SWAP for " + swapSecurity);
    }
    final AnnuityCouponFixedDefinition fixedLegDefinition = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, conventions, payFixed);
    final AnnuityCouponOISSimplifiedDefinition floatingLegDefinition = getOISSimplifiedSwapLegDefinition(effectiveDate, maturityDate, floatLeg, calendar, currency, !payFixed);
    return new SwapFixedOISSimplifiedDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private SwapIborIborDefinition getIborIborSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingSpreadIRLeg floatPayLeg = (FloatingSpreadIRLeg) payLeg;
    final FloatingSpreadIRLeg floatReceiveLeg = (FloatingSpreadIRLeg) receiveLeg;
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final AnnuityCouponIborSpreadDefinition payLegDefinition = getIborSwapLegWithSpreadDefinition(effectiveDate, maturityDate, floatPayLeg, calendar, currency, true);
    final AnnuityCouponIborSpreadDefinition receiveLegDefinition = getIborSwapLegWithSpreadDefinition(effectiveDate, maturityDate, floatReceiveLeg, calendar, currency, false);
    return new SwapIborIborDefinition(payLegDefinition, receiveLegDefinition);
  }

  private SwapDefinition getCMSCMSSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final ExternalId regionId = payLeg.getRegionId();
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
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAP"));
    if (conventions == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + swapSecurity);
    }
    final AnnuityCouponFixedDefinition fixedAnnuity = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, conventions, true);
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
    final boolean payIbor = floatPayLeg.getFloatingRateType().isIbor();
    final boolean receiveIbor = floatReceiveLeg.getFloatingRateType().isIbor();
    if (receiveIbor == payIbor) {
      throw new OpenGammaRuntimeException("This should never happen");
    }
    final FloatingInterestRateLeg iborLeg = payIbor ? floatPayLeg : floatReceiveLeg;
    final FloatingInterestRateLeg cmsLeg = payIbor ? floatReceiveLeg : floatPayLeg;
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final AnnuityCouponCMSDefinition cmsAnnuity = getCMSwapLegDefinition(effectiveDate, maturityDate, cmsLeg, calendar, currency, !payIbor);
    final AnnuityCouponIborDefinition iborAnnuity = getIborSwapLegDefinition(effectiveDate, maturityDate, iborLeg, calendar, currency, payIbor);
    return payIbor ? new SwapDefinition(iborAnnuity, cmsAnnuity) : new SwapDefinition(cmsAnnuity, iborAnnuity);
  }

  private AnnuityCouponFixedDefinition getFixedSwapLegDefinitionForOIS(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FixedInterestRateLeg fixedLeg,
      final Calendar calendar, final ConventionBundle conventions, final boolean isPayer) {
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final BusinessDayConvention businessDay = fixedLeg.getBusinessDayConvention();
    final boolean isEOM = conventions.isEOMConvention();
    final Frequency frequency = fixedLeg.getFrequency();
    final ZonedDateTime frequencyEndDate = effectiveDate.plus(getTenor(frequency));
    if (frequencyEndDate.isAfter(maturityDate)) {
      return AnnuityCouponFixedDefinition.from(((InterestRateNotional) fixedLeg.getNotional()).getCurrency(), effectiveDate, new ZonedDateTime[] {maturityDate }, fixedLeg.getFrequency(), calendar,
          fixedLeg.getDayCount(), businessDay, isEOM, notional, fixedLeg.getRate(), isPayer);
    }
    return AnnuityCouponFixedDefinition.from(((InterestRateNotional) fixedLeg.getNotional()).getCurrency(), effectiveDate, maturityDate, fixedLeg.getFrequency(), calendar, fixedLeg.getDayCount(),
        businessDay, isEOM, notional, fixedLeg.getRate(), isPayer);
  }

  private AnnuityCouponFixedDefinition getFixedSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FixedInterestRateLeg fixedLeg, final Calendar calendar,
      final ConventionBundle conventions, final boolean isPayer) {
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final BusinessDayConvention businessDay = fixedLeg.getBusinessDayConvention();
    final boolean isEOM = true; //conventions.isEOMConvention();
    return AnnuityCouponFixedDefinition.from(((InterestRateNotional) fixedLeg.getNotional()).getCurrency(), effectiveDate, maturityDate, fixedLeg.getFrequency(), calendar, fixedLeg.getDayCount(),
        businessDay, isEOM, notional, fixedLeg.getRate(), isPayer);
  }

  private AnnuityCouponIborDefinition getIborSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg, final Calendar calendar,
      final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    final Frequency freq = floatLeg.getFrequency();
    final Period tenor = getTenor(freq);
    final ConventionBundle floatingLegConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    if (floatingLegConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + floatLeg.getFloatingReferenceRateId());
    }
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatingLegConvention.getSwapFloatingLegInitialRate());
    final IborIndex index = new IborIndex(currency, tenor, indexConvention.getSettlementDays(), calendar, indexConvention.getDayCount(), indexConvention.getBusinessDayConvention(),
        indexConvention.isEOMConvention());
    return AnnuityCouponIborDefinition.from(effectiveDate, maturityDate, notional, index, isPayer);
  }

  private AnnuityCouponOISDefinition getOISSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg, final Calendar calendar,
      final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    final ConventionBundle swapConvention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_OIS_SWAP"));
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get OIS index convention for " + currency + " using " + floatLeg.getFloatingReferenceRateId());
    }
    final ZonedDateTime maturity = maturityDate;
    final Integer publicationLag = indexConvention.getOvernightIndexSwapPublicationLag();
    final Period paymentFrequency = getTenor(floatLeg.getFrequency());
    final IndexON index = new IndexON(floatLeg.getFloatingReferenceRateId().getValue(), currency, indexConvention.getDayCount(), publicationLag, calendar);
    final GeneratorOIS generator = new GeneratorOIS(currency.getCode() + "_OIS_Convention", index, paymentFrequency, swapConvention.getSwapFloatingLegDayCount(),
        swapConvention.getSwapFloatingLegBusinessDayConvention(), swapConvention.isEOMConvention(), swapConvention.getSwapFloatingLegSettlementDays());
    return AnnuityCouponOISDefinition.from(effectiveDate, maturity, notional, generator, isPayer);
  }

  private AnnuityCouponOISSimplifiedDefinition getOISSimplifiedSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    final ConventionBundle swapConvention = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_OIS_SWAP"));
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get OIS index convention for " + currency + " using " + floatLeg.getFloatingReferenceRateId());
    }
    final ZonedDateTime maturity = maturityDate;
    final int publicationLag = 0; //TODO
    final Period paymentFrequency = getTenor(floatLeg.getFrequency());
    final DayCount dayCount =  indexConvention.getDayCount();
    final IndexON index = new IndexON(floatLeg.getFloatingReferenceRateId().getValue(), currency, dayCount, publicationLag, calendar);
    final GeneratorOIS generator = new GeneratorOIS(currency.getCode() + "_OIS_Convention", index, paymentFrequency, swapConvention.getSwapFloatingLegDayCount(),
        swapConvention.getSwapFloatingLegBusinessDayConvention(),
        true, swapConvention.getSwapFloatingLegSettlementDays());
    return AnnuityCouponOISSimplifiedDefinition.from(effectiveDate, maturity, notional, generator, isPayer);
  }

  private AnnuityCouponIborSpreadDefinition getIborSwapLegWithSpreadDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingSpreadIRLeg floatLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final double spread = floatLeg.getSpread();
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    final Frequency freq = floatLeg.getFrequency();
    final Period tenor = getTenor(freq);
    final ConventionBundle floatingLegConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    if (floatingLegConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for " + floatLeg.getFloatingReferenceRateId());
    }
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatingLegConvention.getSwapFloatingLegInitialRate());
    final IborIndex index = new IborIndex(currency, tenor, indexConvention.getSettlementDays(), calendar, indexConvention.getDayCount(), indexConvention.getBusinessDayConvention(),
        indexConvention.isEOMConvention());
    return AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, notional, index, spread, isPayer);
  }

  private AnnuityCouponCMSDefinition getCMSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg, final Calendar calendar,
      final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    // TODO: index period can be different from leg period
    // FIXME: convert frequency to period in a better way
    final Frequency freq = floatLeg.getFrequency();
    final Period tenor = getTenor(freq);
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(ExternalIdBundle.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_IBOR_INDEX"));
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor index convention for " + currency);
    }
    final ConventionBundle swapRateConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    if (swapRateConvention == null) {
      throw new OpenGammaRuntimeException("Could not get swap rate convention for " + currency);
    }
    final IborIndex iborIndex = new IborIndex(currency, tenor, indexConvention.getSettlementDays(), calendar, indexConvention.getDayCount(),
        indexConvention.getBusinessDayConvention(), indexConvention.isEOMConvention());
    final Period fixedLegPeriod = getTenor(swapRateConvention.getFrequency());
    final DayCount dayCount = swapRateConvention.getDayCount();
    final Period underlyingTenor = getTenor(swapRateConvention.getSwapFixedLegFrequency());
    final IndexSwap cmsIndex = new IndexSwap(fixedLegPeriod, dayCount, iborIndex, underlyingTenor);
    return AnnuityCouponCMSDefinition.from(effectiveDate, maturityDate, notional, cmsIndex, tenor, dayCount, isPayer);
  }

  private Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      return ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

}

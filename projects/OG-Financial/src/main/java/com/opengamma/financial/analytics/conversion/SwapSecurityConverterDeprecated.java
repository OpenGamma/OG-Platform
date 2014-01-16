/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import java.util.ArrayList;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts swaps from {@link SwapSecurity} to the {@link InstrumentDefinition}s.
 * @deprecated Replaced by {@link SwapSecurityConverter}, which does not use curve name information
 */
@Deprecated
public class SwapSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A holiday source */
  private final HolidaySource _holidaySource;
  /** A convention bundle source */
  private final ConventionBundleSource _conventionSource;
  /** A region source */
  private final RegionSource _regionSource;
  /** Is this converter being used in curve construction code */
  private final boolean _forCurves;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   * @param forCurves true if the converter is used in curve construction code
   */
  public SwapSecurityConverterDeprecated(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final RegionSource regionSource, final boolean forCurves) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
    _forCurves = forCurves;
  }

  /**
   * Gets the holiday source.
   * @return The holiday source
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Gets the convention bundle source.
   * @return The convention bundle source
   */
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionSource;
  }

  /**
   * Gets the region source.
   * @return The region source
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return visitSwapSecurity(security);
  }

  @Override
  public InstrumentDefinition<?> visitSwapSecurity(final SwapSecurity security) {
    ArgumentChecker.notNull(security, "swap security");
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (Currency.BRL.equals(currency)) {
      return getBRLSwapDefinition(security, SwapSecurityUtils.payFixed(security));
    }
    final InterestRateInstrumentType swapType = SwapSecurityUtils.getSwapType(security);
    switch (swapType) {
      case SWAP_FIXED_IBOR:
        return getFixedIborSwapDefinition(security, SwapSecurityUtils.payFixed(security), false);
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return getFixedIborSwapDefinition(security, SwapSecurityUtils.payFixed(security), true);
      case SWAP_IBOR_IBOR:
        return getIborIborSwapDefinition(security);
      case SWAP_CMS_CMS:
        return getCMSCMSSwapDefinition(security);
      case SWAP_FIXED_CMS:
        return SwapSecurityUtils.payFixed(security) ? getFixedCMSSwapDefinition(security, true) : getFixedCMSSwapDefinition(security, false);
      case SWAP_IBOR_CMS:
        return getIborCMSSwapDefinition(security);
      case SWAP_FIXED_OIS:
        return getFixedOISSwapDefinition(security, SwapSecurityUtils.payFixed(security), _forCurves);
      case SWAP_CROSS_CURRENCY:
        return getCrossCurrencySwapDefinition(security);
      default:
        throw new OpenGammaRuntimeException("Cannot handle swapType " + swapType);
    }
  }

  private SwapDefinition getBRLSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get OIS index convention for " + currency + " using " + floatLeg.getFloatingReferenceRateId());
    }
    final Integer publicationLag = indexConvention.getOvernightIndexSwapPublicationLag();
    if (publicationLag == null) {
      throw new OpenGammaRuntimeException("Could not get ON Index publication lag for " + indexConvention.getIdentifiers());
    }
    final ConventionBundle brlSwapConvention = _conventionSource.getConventionBundle(simpleNameSecurityId("BRL_DI_SWAP"));
    final IndexON index = new IndexON(floatLeg.getFloatingReferenceRateId().getValue(), currency, indexConvention.getDayCount(), publicationLag);
    final String name = index.getName();
    final DayCount fixedLegDayCount = fixedLeg.getDayCount();
    final BusinessDayConvention businessDayConvention = fixedLeg.getBusinessDayConvention();
    final boolean isEOM = fixedLeg.isEom();
    final int spotLag = brlSwapConvention.getSwapFixedLegSettlementDays();
    final int paymentLag = brlSwapConvention.getSwapFixedLegSettlementDays();
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double fixedRate = fixedLeg.getRate();
    final GeneratorSwapFixedCompoundedONCompounded generator = new GeneratorSwapFixedCompoundedONCompounded(name, index, fixedLegDayCount, businessDayConvention, isEOM, spotLag, paymentLag, calendar);
    return SwapFixedCompoundedONCompoundedDefinition.from(effectiveDate, maturityDate, notional, generator, fixedRate, payFixed);
  }

  private SwapDefinition getFixedIborSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed, final boolean hasSpread) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg iborLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    // Swap data
    final double signFixed = (payFixed ? -1.0 : 1.0);
    int nbNotional = 0;
    nbNotional = (swapSecurity.isExchangeInitialNotional() ? nbNotional + 1 : nbNotional);
    nbNotional = (swapSecurity.isExchangeFinalNotional() ? nbNotional + 1 : nbNotional);
    final double spread;
    if (hasSpread) {
      spread = ((FloatingSpreadIRLeg) iborLeg).getSpread();
    } else {
      spread = 0;
    }
    // Ibor Leg
    final ExternalId regionIdIbor = fixedLeg.getRegionId();
    final Calendar calendarIbor = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionIdIbor);
    final Currency currencyIbor = ((InterestRateNotional) iborLeg.getNotional()).getCurrency();
    final ConventionBundle iborIndexConvention = _conventionSource.getConventionBundle(iborLeg.getFloatingReferenceRateId());
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention for " + currencyIbor + " using " + iborLeg.getFloatingReferenceRateId() + " from swap " +
          swapSecurity.getExternalIdBundle());
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    Period tenorIbor;
    if (Frequency.NEVER_NAME.equals(freqIbor.getName())) { // If NEVER, then treated as a compounded Ibor coupon over the annuity length
      final ConventionBundle conventionIbor = _conventionSource.getConventionBundle(iborLeg.getFloatingReferenceRateId());
      tenorIbor = conventionIbor.getPeriod();
    } else {
      tenorIbor = getTenor(freqIbor);
    }
    final int spotLag = iborIndexConvention.getSettlementDays();
    final IborIndex indexIbor = new IborIndex(currencyIbor, tenorIbor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention(), iborIndexConvention.getName());
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    final AnnuityDefinition<? extends CouponDefinition> iborLegDefinition;
    if (Frequency.NEVER_NAME.equals(freqIbor.getName())) { // If NEVER, then treated as a compounded Ibor coupon over the annuity length
      final CouponDefinition[] payments = new CouponDefinition[nbNotional + 1];
      int loopnot = 0;
      if (swapSecurity.isExchangeInitialNotional()) {
        payments[0] = new CouponFixedDefinition(currencyIbor, effectiveDate, effectiveDate, effectiveDate, 1.0, signFixed * iborLegNotional, 1.0);
        loopnot++;
      }
      payments[loopnot] = CouponIborCompoundingDefinition.from(-signFixed * iborLegNotional, effectiveDate, maturityDate, indexIbor, StubType.SHORT_START,
          indexIbor.getBusinessDayConvention(), indexIbor.isEndOfMonth(), calendarIbor); // TODO: add spread and compounding type
      if (swapSecurity.isExchangeFinalNotional()) {
        payments[loopnot + 1] = new CouponFixedDefinition(currencyIbor, maturityDate, maturityDate, maturityDate, 1.0, -signFixed * iborLegNotional, 1.0);
      }
      iborLegDefinition = new AnnuityDefinition<>(payments, calendarIbor);
    } else {
      iborLegDefinition = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(effectiveDate, maturityDate, iborLegNotional, spread, indexIbor,
          !payFixed, calendarIbor, StubType.SHORT_START, 0, swapSecurity.isExchangeInitialNotional(), swapSecurity.isExchangeFinalNotional());
    }
        // Fixed Leg
    final ExternalId regionIdFixed = fixedLeg.getRegionId();
    final Calendar calendarFixed = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionIdFixed);
    final Frequency freqFixed = fixedLeg.getFrequency();
    final Currency currencyFixed = ((InterestRateNotional) fixedLeg.getNotional()).getCurrency();
    final double fixedLegNotional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final AnnuityCouponFixedDefinition fixedLegDefinition;
    if (Frequency.NEVER_NAME.equals(freqFixed.getName())) { // If NEVER, then treated as a zero-coupon and coupon not used.
      final int nbPayment = Math.max(nbNotional, 1); // Implementation note: If zero-coupon with no notional, create a fake coupon of 0.
      final double accruedEnd = (nbNotional == 0 ? 0.0 : 1.0);
      final CouponFixedDefinition[] notional = new CouponFixedDefinition[nbPayment];
      int loopnot = 0;
      if (swapSecurity.isExchangeInitialNotional()) {
        notional[0] = new CouponFixedDefinition(currencyIbor, effectiveDate, effectiveDate, effectiveDate, 1.0, -signFixed * fixedLegNotional, 1.0);
        loopnot++;
      }
      if (swapSecurity.isExchangeFinalNotional() || (nbNotional == 0)) {
        notional[loopnot] = new CouponFixedDefinition(currencyIbor, maturityDate, maturityDate, maturityDate, accruedEnd, signFixed * fixedLegNotional, 1.0);
      }
      fixedLegDefinition = new AnnuityCouponFixedDefinition(notional, calendarFixed);
    } else {
      final Period tenorFixed = getTenor(freqFixed);
      fixedLegDefinition = AnnuityDefinitionBuilder.couponFixedWithNotional(currencyFixed, effectiveDate, maturityDate, tenorFixed, calendarFixed,
          fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(), fixedLeg.isEom(), fixedLegNotional, fixedLeg.getRate(), payFixed, StubType.SHORT_START, 0,
          swapSecurity.isExchangeInitialNotional(), swapSecurity.isExchangeFinalNotional());
    }
    return new SwapCouponFixedCouponDefinition(fixedLegDefinition, iborLegDefinition);
  }

  private SwapDefinition getFixedOISSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed, final boolean forCurve) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get OIS index convention for " + currency + " using " + floatLeg.getFloatingReferenceRateId());
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegion());
    final String currencyString = currency.getCode();
    final Integer publicationLag = indexConvention.getOvernightIndexSwapPublicationLag();
    if (publicationLag == null) {
      throw new OpenGammaRuntimeException("Could not get ON Index publication lag for " + indexConvention.getIdentifiers());
    }
    final Period paymentFrequency = getTenor(floatLeg.getFrequency());
    final IndexON index = new IndexON(floatLeg.getFloatingReferenceRateId().getValue(), currency, indexConvention.getDayCount(), publicationLag);
    final GeneratorSwapFixedON generator = new GeneratorSwapFixedON(currencyString + "_OIS_Convention", index, paymentFrequency, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(),
        fixedLeg.isEom(), 0, 1 - publicationLag, calendar); // TODO: The payment lag is not available at the security level!
    final double notionalFixed = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double notionalOIS = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    if (forCurve) {
      return SwapFixedONSimplifiedDefinition.from(effectiveDate, maturityDate, notionalFixed, notionalOIS, generator, fixedLeg.getRate(), payFixed);
    }
    return SwapFixedONDefinition.from(effectiveDate, maturityDate, notionalFixed, notionalOIS, generator, fixedLeg.getRate(), payFixed);
  }

  private SwapIborIborDefinition getIborIborSwapDefinition(final SwapSecurity swapSecurity) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    if (floatPayLeg instanceof FloatingSpreadIRLeg) {
      final AnnuityCouponIborSpreadDefinition payLegDefinition = getIborSwapLegDefinition(effectiveDate, maturityDate, (FloatingSpreadIRLeg) floatPayLeg, calendar, currency, true);
      if (floatReceiveLeg instanceof FloatingSpreadIRLeg) {
        final AnnuityCouponIborSpreadDefinition receiveLegDefinition = getIborSwapLegDefinition(effectiveDate, maturityDate, (FloatingSpreadIRLeg) floatReceiveLeg, calendar, currency, false);
        return SwapIborIborDefinition.from(payLegDefinition, receiveLegDefinition);
      }
      final AnnuityCouponIborDefinition receiveLegDefinition = getIborSwapLegDefinition(effectiveDate, maturityDate, floatReceiveLeg, calendar, currency, false);
      return SwapIborIborDefinition.from(payLegDefinition, receiveLegDefinition);
    }
    final AnnuityCouponIborDefinition payLegDefinition = getIborSwapLegDefinition(effectiveDate, maturityDate, floatPayLeg, calendar, currency, true);
    if (floatReceiveLeg instanceof FloatingSpreadIRLeg) {
      final AnnuityCouponIborSpreadDefinition receiveLegDefinition = getIborSwapLegDefinition(effectiveDate, maturityDate, (FloatingSpreadIRLeg) floatReceiveLeg, calendar, currency, false);
      return SwapIborIborDefinition.from(payLegDefinition, receiveLegDefinition);
    }
    final AnnuityCouponIborDefinition receiveLegDefinition = getIborSwapLegDefinition(effectiveDate, maturityDate, floatReceiveLeg, calendar, currency, false);
    return SwapIborIborDefinition.from(payLegDefinition, receiveLegDefinition);
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
    final AnnuityCouponFixedDefinition fixedAnnuity = getFixedSwapLegDefinition(effectiveDate, maturityDate, fixedLeg, calendar, payFixed);
    final AnnuityCouponCMSDefinition cmsAnnuity = getCMSwapLegDefinition(effectiveDate, maturityDate, floatingLeg, calendar, currency, !payFixed);
    return payFixed ? new SwapDefinition(fixedAnnuity, cmsAnnuity) : new SwapDefinition(cmsAnnuity, fixedAnnuity);
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
    final AnnuityDefinition<? extends CouponFloatingDefinition> iborAnnuity = getIborSwapLegDefinition(effectiveDate, maturityDate, iborLeg, calendar, currency, payIbor);
    final AnnuityCouponCMSDefinition cmsAnnuity = getCMSwapLegDefinition(effectiveDate, maturityDate, cmsLeg, calendar, currency, !payIbor);
    return payIbor ? new SwapDefinition(iborAnnuity, cmsAnnuity) : new SwapDefinition(cmsAnnuity, iborAnnuity);
    // Implementation note: In the converter, the pay leg is expected to be first.
  }

  private static AnnuityCouponFixedDefinition getFixedSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FixedInterestRateLeg fixedLeg,
      final Calendar calendar, final boolean isPayer) {
    final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final BusinessDayConvention businessDay = fixedLeg.getBusinessDayConvention();
    if (businessDay == null) {
      throw new OpenGammaRuntimeException("Could not get Business Day for " + fixedLeg);
    }
    final boolean isEOM = fixedLeg.isEom();
    final Frequency freqFixed = fixedLeg.getFrequency();
    final Period tenorFixed = getTenor(freqFixed);
    return AnnuityCouponFixedDefinition.from(((InterestRateNotional) fixedLeg.getNotional()).getCurrency(), effectiveDate, maturityDate, tenorFixed, calendar, fixedLeg.getDayCount(), businessDay,
        isEOM, notional, fixedLeg.getRate(), isPayer);
  }

  private AnnuityCouponIborSpreadDefinition getIborSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingSpreadIRLeg iborLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final ConventionBundle iborIndexConvention = _conventionSource.getConventionBundle(iborLeg.getFloatingReferenceRateId());
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention for " + currency + " using " + iborLeg.getFloatingReferenceRateId());
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    final Period tenorIbor = getTenor(freqIbor);
    final IborIndex iborIndex = new IborIndex(currency, tenorIbor, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    final double spread = iborLeg.getSpread();
    return AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, tenorIbor, iborLegNotional, iborIndex, isPayer, iborLeg.getBusinessDayConvention(), iborLeg.isEom(),
        iborLeg.getDayCount(), spread, calendar);
  }

  private AnnuityCouponIborDefinition getIborSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg iborLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final ConventionBundle iborIndexConvention = _conventionSource.getConventionBundle(iborLeg.getFloatingReferenceRateId());
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention for " + currency + " using " + iborLeg.getFloatingReferenceRateId());
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    final Period tenorIbor = getTenor(freqIbor);
    final IborIndex iborIndex = new IborIndex(currency, tenorIbor, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    return AnnuityCouponIborDefinition.from(effectiveDate, maturityDate, tenorIbor, iborLegNotional, iborIndex, isPayer, iborLeg.getBusinessDayConvention(), iborLeg.isEom(), iborLeg.getDayCount(),
        calendar);
  }

  private AnnuityCouponCMSDefinition getCMSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg floatLeg, final Calendar calendar,
      final Currency currency, final boolean isPayer) {
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    final Frequency freq = floatLeg.getFrequency();
    // FIXME: convert frequency to period in a better way
    final Period tenorPayment = getTenor(freq);
    final ConventionBundle swapIndexConvention = _conventionSource.getConventionBundle(floatLeg.getFloatingReferenceRateId());
    if (swapIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get swap index convention for " + floatLeg.getFloatingReferenceRateId().toString());
    }
    final ConventionBundle iborIndexConvention = _conventionSource.getConventionBundle(swapIndexConvention.getSwapFloatingLegInitialRate());
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get ibor index convention for " + swapIndexConvention.getSwapFloatingLegInitialRate());
    }
    final IborIndex iborIndex = new IborIndex(currency, tenorPayment, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
    final Period fixedLegPaymentPeriod = getTenor(swapIndexConvention.getSwapFixedLegFrequency());
    final IndexSwap swapIndex = new IndexSwap(fixedLegPaymentPeriod, swapIndexConvention.getSwapFixedLegDayCount(), iborIndex, swapIndexConvention.getPeriod(), calendar);
    return AnnuityCouponCMSDefinition.from(effectiveDate, maturityDate, notional, swapIndex, tenorPayment, floatLeg.getDayCount(), isPayer, calendar);
  }

  private SwapDefinition getCrossCurrencySwapDefinition(final SwapSecurity security) {
    final ZonedDateTime settlementDate = security.getEffectiveDate();
    final ZonedDateTime maturityDate = security.getMaturityDate();
    final SwapLeg[] swapLeg = new SwapLeg[2];
    swapLeg[0] = security.getPayLeg();
    swapLeg[1] = security.getReceiveLeg();
    final boolean[] payer = {true, false };
    final double[] notional = new double[2];
    final Currency[] currency = new Currency[2];
    final ExternalId[] regionId = new ExternalId[2];
    final Calendar[] calendar = new Calendar[2];
    // TODO: Calendar need to be merged to have common payment dates
    for (int loopleg = 0; loopleg < 2; loopleg++) {
      notional[loopleg] = ((InterestRateNotional) swapLeg[loopleg].getNotional()).getAmount();
      currency[loopleg] = ((InterestRateNotional) swapLeg[loopleg].getNotional()).getCurrency();
      regionId[loopleg] = swapLeg[loopleg].getRegionId();
      calendar[loopleg] = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId[0]);
    }
    final ArrayList<AnnuityDefinition<? extends PaymentDefinition>> legDefinition = new ArrayList<>();
    for (int loopleg = 0; loopleg < 2; loopleg++) {
      if (swapLeg[loopleg] instanceof FloatingInterestRateLeg) { // Leg is Ibor
        double spread = 0.0;
        if (swapLeg[loopleg] instanceof FloatingSpreadIRLeg) {
          spread = ((FloatingSpreadIRLeg) swapLeg[loopleg]).getSpread();
        }
        final FloatingInterestRateLeg legFloat = (FloatingInterestRateLeg) swapLeg[loopleg];
        final ConventionBundle iborIndexConvention = _conventionSource.getConventionBundle(legFloat.getFloatingReferenceRateId());
        if (iborIndexConvention == null) {
          throw new OpenGammaRuntimeException("Could not get Ibor index convention for " + currency[0] + " using " + legFloat.getFloatingReferenceRateId());
        }
        final Period tenorIbor = iborIndexConvention.getPeriod();
        final IborIndex iborIndex = new IborIndex(currency[loopleg], tenorIbor, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
            iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention());
        legDefinition.add(AnnuityDefinitionBuilder.couponIborSpreadWithNotional(settlementDate, maturityDate, notional[loopleg], spread, iborIndex, payer[loopleg],
            calendar[loopleg], StubType.SHORT_START, 0, security.isExchangeInitialNotional(), security.isExchangeFinalNotional()));
      } else {
        if (swapLeg[loopleg] instanceof FixedInterestRateLeg) { // Leg is Fixed
          final FixedInterestRateLeg legFixed = (FixedInterestRateLeg) swapLeg[loopleg];
          final BusinessDayConvention businessDay = legFixed.getBusinessDayConvention();
          if (businessDay == null) {
            throw new OpenGammaRuntimeException("Could not get Business Day for " + legFixed);
          }
          final boolean isEOM = legFixed.isEom();
          final Frequency freqFixed = legFixed.getFrequency();
          final Period tenorFixed = getTenor(freqFixed);
          legDefinition.add(AnnuityDefinitionBuilder.couponFixedWithNotional(currency[loopleg], settlementDate, maturityDate, tenorFixed,
              calendar[loopleg], legFixed.getDayCount(), businessDay, isEOM, notional[loopleg], legFixed.getRate(), payer[loopleg], StubType.SHORT_START, 0,
              security.isExchangeInitialNotional(), security.isExchangeFinalNotional()));
        } else {
          throw new OpenGammaRuntimeException("X Ccy Swap legs should be Fixed or Floating legs");
        }
      }
    }
    return new SwapXCcyDefinition(legDefinition.get(0), legDefinition.get(1));
  }

  private static Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      return ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

}

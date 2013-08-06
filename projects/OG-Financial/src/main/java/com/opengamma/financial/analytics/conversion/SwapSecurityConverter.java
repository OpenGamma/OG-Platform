/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.percurrency.PerCurrencyConventionHelper.getConventionName;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
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
 *
 */
public class SwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A holiday source */
  private final HolidaySource _holidaySource;
  /** A convention bundle source */
  private final ConventionSource _conventionSource;
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
  public SwapSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource,
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
        return getFixedIborSwapDefinition(security, SwapSecurityUtils.payFixed(security), false);
      case SWAP_FIXED_IBOR_WITH_SPREAD:
        return getFixedIborSwapDefinition(security, SwapSecurityUtils.payFixed(security), true);
      case SWAP_IBOR_IBOR:
        return getIborIborSwapDefinition(security);
      case SWAP_FIXED_OIS:
        return getFixedOISSwapDefinition(security, SwapSecurityUtils.payFixed(security), _forCurves);
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
    final FloatingInterestRateLeg iborLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String liborConventionName = getConventionName(currency, LIBOR); //TODO shouldn't just use Libor
    final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, liborConventionName));
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention with the identifier " + ExternalId.of(SCHEME_NAME, liborConventionName));
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    final Period tenorIbor = getTenor(freqIbor);
    final int spotLag = 0; //TODO
    final IborIndex indexIbor = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    final Frequency freqFixed = fixedLeg.getFrequency();
    final Period tenorFixed = getTenor(freqFixed);
    final double fixedLegNotional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    if (hasSpread) {
      final double spread = ((FloatingSpreadIRLeg) iborLeg).getSpread();
      return SwapFixedIborSpreadDefinition.from(effectiveDate, maturityDate, tenorFixed, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(), fixedLeg.isEom(), fixedLegNotional,
          fixedLeg.getRate(), tenorIbor, iborLeg.getDayCount(), iborLeg.getBusinessDayConvention(), iborLeg.isEom(), iborLegNotional, indexIbor, spread, payFixed, calendar);
    }
    final SwapFixedIborDefinition swap = SwapFixedIborDefinition.from(effectiveDate, maturityDate, tenorFixed, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(), fixedLeg.isEom(),
        fixedLegNotional, fixedLeg.getRate(), tenorIbor, iborLeg.getDayCount(), iborLeg.getBusinessDayConvention(), iborLeg.isEom(), iborLegNotional, indexIbor, payFixed, calendar);
    return swap;
  }

  private SwapDefinition getFixedOISSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed, final boolean forCurve) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String overnightConventionName = getConventionName(currency, OVERNIGHT);
    final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, ExternalId.of(SCHEME_NAME, overnightConventionName));
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get OIS index convention with the identifier " + ExternalId.of(SCHEME_NAME, overnightConventionName));
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final String currencyString = currency.getCode();
    final Integer publicationLag = indexConvention.getPublicationLag();
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

  private AnnuityCouponIborSpreadDefinition getIborSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingSpreadIRLeg iborLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final String liborConventionName = getConventionName(currency, LIBOR);
    final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, liborConventionName));
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention with the identifier " + liborConventionName);
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    final Period tenorIbor = getTenor(freqIbor);
    final int spotLag = 0;
    final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    final double spread = iborLeg.getSpread();
    return AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, tenorIbor, iborLegNotional, iborIndex, isPayer, iborLeg.getBusinessDayConvention(), iborLeg.isEom(),
        iborLeg.getDayCount(), spread, calendar);
  }

  private AnnuityCouponIborDefinition getIborSwapLegDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final FloatingInterestRateLeg iborLeg,
      final Calendar calendar, final Currency currency, final boolean isPayer) {
    final String liborConventionName = getConventionName(currency, LIBOR);
    final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, liborConventionName));
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention with the identifier " + liborConventionName);
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    final Period tenorIbor = getTenor(freqIbor);
    final int spotLag = 0;
    final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    return AnnuityCouponIborDefinition.from(effectiveDate, maturityDate, tenorIbor, iborLegNotional, iborIndex, isPayer, iborLeg.getBusinessDayConvention(), iborLeg.isEom(), iborLeg.getDayCount(),
        calendar);
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

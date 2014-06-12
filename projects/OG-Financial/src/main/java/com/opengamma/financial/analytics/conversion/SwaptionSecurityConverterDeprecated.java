/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts swaptions from {@link SwaptionSecurity} to the {@link InstrumentDefinition}s.
 * @deprecated Replaced by {@link SwaptionSecurityConverter}, which does not use curve name information
 */
@Deprecated
public class SwaptionSecurityConverterDeprecated extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The security source */
  private final SecuritySource _securitySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The convention bundle source */
  private final ConventionBundleSource _conventionSource;

  /**
   * @param securitySource The security source, not null
   * @param swapConverter The underlying swap converter, not null
   */
  public SwaptionSecurityConverterDeprecated(final SecuritySource securitySource, final SwapSecurityConverterDeprecated swapConverter) {
    this(securitySource, swapConverter.getConventionBundleSource(), swapConverter.getHolidaySource(), swapConverter.getRegionSource());
  }

  /**
   * @param securitySource The security source, not null
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   */
  public SwaptionSecurityConverterDeprecated(final SecuritySource securitySource, final ConventionBundleSource conventionSource,
      final HolidaySource holidaySource, final RegionSource regionSource) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  @Override
  public InstrumentDefinition<?> visitSwaptionSecurity(final SwaptionSecurity swaptionSecurity) {
    ArgumentChecker.notNull(swaptionSecurity, "swaption security");
    final ExternalId underlyingIdentifier = swaptionSecurity.getUnderlyingId();
    final ZonedDateTime expiry = swaptionSecurity.getExpiry().getExpiry();
    final SwapSecurity underlyingSecurity = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(underlyingIdentifier));
    final InterestRateInstrumentType swapType = SwapSecurityUtils.getSwapType(underlyingSecurity);
    final boolean isCashSettled = swaptionSecurity.isCashSettled();
    final boolean isLong = swaptionSecurity.isLong();
    final boolean isCall = underlyingSecurity.getPayLeg() instanceof FixedInterestRateLeg;
    if (swaptionSecurity.getCurrency().equals(Currency.BRL)) {
      final SwapFixedCompoundedONCompoundedDefinition swapDefinition = getBRLSwapDefinition(underlyingSecurity, isCall);
      return isCashSettled ? SwaptionCashFixedCompoundedONCompoundingDefinition.from(expiry, swapDefinition, isCall, isLong) :
        SwaptionPhysicalFixedCompoundedONCompoundedDefinition.from(expiry, swapDefinition, isCall, isLong);
    }
    if (swapType != InterestRateInstrumentType.SWAP_FIXED_IBOR) {
      throw new OpenGammaRuntimeException("Underlying swap of a swaption must be a fixed / ibor swap; have " + swapType);
    }
    final SwapFixedIborDefinition underlyingSwap = getFixedIborSwapDefinition(underlyingSecurity, SwapSecurityUtils.payFixed(underlyingSecurity));
    return isCashSettled ? SwaptionCashFixedIborDefinition.from(expiry, underlyingSwap, isCall, isLong)
        : SwaptionPhysicalFixedIborDefinition.from(expiry, underlyingSwap, isCall, isLong);
  }

  private SwapFixedCompoundedONCompoundedDefinition getBRLSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
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

  /**
   * Creates a fixed / ibor swap definition.
   * @param swapSecurity The swap security
   * @param payFixed True if the underlying swap is payer
   * @return The swap definition
   */
  private SwapFixedIborDefinition getFixedIborSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg iborLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final ExternalId regionId = payLeg.getRegionId();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final ConventionBundle iborIndexConvention = _conventionSource.getConventionBundle(iborLeg.getFloatingReferenceRateId());
    if (iborIndexConvention == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index convention for " + currency + " using " + iborLeg.getFloatingReferenceRateId() + " from swap " +
          swapSecurity.getExternalIdBundle());
    }
    final Frequency freqIbor = iborLeg.getFrequency();
    final Period tenorIbor;
    if (freqIbor.getName() == Frequency.NEVER_NAME) {
      tenorIbor = Period.between(effectiveDate.toLocalDate(), maturityDate.toLocalDate());
    } else {
      tenorIbor = ConversionUtils.getTenor(freqIbor);
    }
    final IborIndex indexIbor = new IborIndex(currency, tenorIbor, iborIndexConvention.getSettlementDays(), iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention(), iborIndexConvention.getName());
    final Frequency freqFixed = fixedLeg.getFrequency();
    final Period tenorFixed;
    if (freqIbor.getName() == Frequency.NEVER_NAME) {
      tenorFixed = Period.between(effectiveDate.toLocalDate(), maturityDate.toLocalDate());
    } else {
      tenorFixed = ConversionUtils.getTenor(freqFixed);
    }
    final double fixedLegNotional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    final SwapFixedIborDefinition swap = SwapFixedIborDefinition.from(effectiveDate, maturityDate, tenorFixed, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(), fixedLeg.isEom(),
        fixedLegNotional, fixedLeg.getRate(), tenorIbor, iborLeg.getDayCount(), iborLeg.getBusinessDayConvention(), iborLeg.isEom(), iborLegNotional, indexIbor, payFixed, calendar);
    return swap;
  }

}

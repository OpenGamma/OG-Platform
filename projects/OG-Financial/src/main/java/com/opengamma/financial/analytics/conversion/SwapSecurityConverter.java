/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.initializer.EUConventions.EURIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_FIXED_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IRS_IBOR_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.LIBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OIS_ON_LEG;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.OVERNIGHT;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SWAP_INDEX;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponArithmeticAverageONDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponArithmeticAverageONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
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
  /** A convention bundle TODO: remove */
  private final ConventionBundleSource _conventionBundle;
  /** A region source */
  private final RegionSource _regionSource;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param conventionBundleSource The convention bundle. TODO: remove.
   * @param regionSource The region source, not null
   */
  public SwapSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final ConventionBundleSource conventionBundleSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _conventionBundle = conventionBundleSource;
    _regionSource = regionSource;
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
      case SWAP_FIXED_OIS:
        return getFixedOISSwapDefinition(security, SwapSecurityUtils.payFixed(security));
      default:
        final ZonedDateTime effectiveDate = security.getEffectiveDate();
        final ZonedDateTime maturityDate = security.getMaturityDate();
        final AnnuityDefinition<? extends PaymentDefinition> payLeg = security.getPayLeg().accept(getSwapLegConverter(effectiveDate, maturityDate, true));
        final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = security.getReceiveLeg().accept(getSwapLegConverter(effectiveDate, maturityDate, false));
        return new SwapDefinition(payLeg, receiveLeg);
    }
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
    final IborIndexConvention iborIndexConvention = getIborLegConvention(currencyIbor);
    final Frequency freqIbor = iborLeg.getFrequency();
    Period tenorIbor;
    if (Frequency.NEVER_NAME.equals(freqIbor.getName())) { // If NEVER, then treated as a compounded Ibor coupon over the annuity length
      ConventionBundle conventionIbor = _conventionBundle.getConventionBundle(iborLeg.getFloatingReferenceRateId());
      tenorIbor = conventionIbor.getPeriod();    
      // TODO: Getting the tenor from the old convention: to be changed.
    } else {
      tenorIbor = getTenor(freqIbor);
    }
    final int spotLag = iborIndexConvention.getSettlementDays();
    final IborIndex indexIbor = new IborIndex(currencyIbor, tenorIbor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
    final double iborLegNotional = ((InterestRateNotional) iborLeg.getNotional()).getAmount();
    final AnnuityDefinition<? extends CouponDefinition> iborLegDefinition;
    if (Frequency.NEVER_NAME.equals(freqIbor.getName())) { // If NEVER, then treated as a compounded Ibor coupon over the annuity length
      CouponDefinition[] payments = new CouponDefinition[nbNotional + 1];
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
      if (swapSecurity.isExchangeInitialNotional() || swapSecurity.isExchangeFinalNotional() || hasSpread) {
        iborLegDefinition = AnnuityDefinitionBuilder.couponIborSpreadWithNotional(effectiveDate, maturityDate, iborLegNotional, spread, indexIbor, 
          !payFixed, calendarIbor, StubType.SHORT_START, 0, swapSecurity.isExchangeInitialNotional(), swapSecurity.isExchangeFinalNotional());
      } else {
        iborLegDefinition = AnnuityDefinitionBuilder.couponIbor(effectiveDate, maturityDate, indexIbor.getTenor(), iborLegNotional, indexIbor, 
            !payFixed, indexIbor.getDayCount(), indexIbor.getBusinessDayConvention(), indexIbor.isEndOfMonth(), calendarIbor, StubType.SHORT_START, 0);
      }
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
      CouponFixedDefinition[] notional = new CouponFixedDefinition[nbPayment];
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
    if (swapSecurity.isExchangeInitialNotional() || swapSecurity.isExchangeFinalNotional() || hasSpread) {
      return new SwapCouponFixedCouponDefinition(fixedLegDefinition, iborLegDefinition);
    } else {
      return new SwapFixedIborDefinition(fixedLegDefinition, iborLegDefinition);
    }
  }

  private IborIndexConvention getIborLegConvention(final Currency currency) {
    String iborConventionName = getConventionName(currency, EURIBOR);
    try {
      return _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, iborConventionName), IborIndexConvention.class);
    } catch (DataNotFoundException ex) {
      // continue
    }
    iborConventionName = getConventionName(currency, LIBOR);
    try {
      return _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, iborConventionName), IborIndexConvention.class);
    } catch (DataNotFoundException ex) {
      // continue
    }
    iborConventionName = getConventionName(currency, IBOR);
    try {
      return _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, iborConventionName), IborIndexConvention.class);
    } catch (DataNotFoundException ex) {
      // continue
    }
    throw new OpenGammaRuntimeException("Could not get ibor index convention with the identifier " + ExternalId.of(SCHEME_NAME, iborConventionName));
  }

  private SwapDefinition getFixedOISSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    final String overnightConventionName = getConventionName(currency, OVERNIGHT);
    final OvernightIndexConvention indexConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, overnightConventionName), OvernightIndexConvention.class);
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
    final String currencyString = currency.getCode();
    final Integer publicationLag = indexConvention.getPublicationLag();
    final Period paymentFrequency = getTenor(floatLeg.getFrequency());
    final IndexON index = new IndexON(indexConvention.getName(), currency, indexConvention.getDayCount(), publicationLag);
    final GeneratorSwapFixedON generator = new GeneratorSwapFixedON(currencyString + "_OIS_Convention", index, paymentFrequency, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(),
        fixedLeg.isEom(), 0, 1 - publicationLag, calendar); // TODO: The payment lag is not available at the security level!
    final double notionalFixed = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final double notionalOIS = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
    return SwapFixedONDefinition.from(effectiveDate, maturityDate, notionalFixed, notionalOIS, generator, fixedLeg.getRate(), payFixed);
  }

  private static Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      Period period = ((PeriodFrequency) freq).getPeriod();
      if (period.getYears() == 1) {
        return Period.ofMonths(12);
      }
      return period;
    } else if (freq instanceof SimpleFrequency) {
      Period period =  ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
      if (period.getYears() == 1) {
        return Period.ofMonths(12);
      }
      return period;
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

  private static String getTenorString(final Frequency freq) {
    final Period period;
    if (freq instanceof PeriodFrequency) {
      period = ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      period = ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    } else {
      throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
    }
    return period.toString().substring(1, period.toString().length());
  }

  private SwapLegVisitor<AnnuityDefinition<? extends PaymentDefinition>> getSwapLegConverter(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final boolean isPayer) {
    return new SwapLegVisitor<AnnuityDefinition<? extends PaymentDefinition>>() {

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
        final ExternalId regionId = swapLeg.getRegionId();
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
        final InterestRateNotional interestRateNotional = (InterestRateNotional) swapLeg.getNotional();
        final Currency currency = interestRateNotional.getCurrency();
        final String fixedLegConventionName = getConventionName(currency, IRS_FIXED_LEG);
        final SwapFixedLegConvention fixedLegConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, fixedLegConventionName), SwapFixedLegConvention.class);
        final Frequency freqFixed = swapLeg.getFrequency();
        final Period tenorFixed = getTenor(freqFixed);
        final double notional = interestRateNotional.getAmount();
        final DayCount dayCount = fixedLegConvention.getDayCount();
        final boolean isEOM = fixedLegConvention.isIsEOM();
        final double rate = swapLeg.getRate();
        final BusinessDayConvention businessDayConvention = fixedLegConvention.getBusinessDayConvention();
        return AnnuityCouponFixedDefinition.from(currency, effectiveDate, maturityDate, tenorFixed, calendar, dayCount,
            businessDayConvention, isEOM, notional, rate, isPayer);
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
        final InterestRateNotional interestRateNotional = (InterestRateNotional) swapLeg.getNotional();
        final Currency currency = interestRateNotional.getCurrency();
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapLeg.getRegionId());
        switch (swapLeg.getFloatingRateType()) {
          case IBOR:
            return getIborAnnuity(swapLeg, interestRateNotional, currency, calendar);
          case OIS:
            return getOISAnnuity(swapLeg, interestRateNotional, currency);
          case CMS:
            return getCMSAnnuity(swapLeg, interestRateNotional, currency, calendar);
          case OVERNIGHT_ARITHMETIC_AVERAGE:
            return getOvernightAAverageAnnuity(swapLeg, interestRateNotional, currency);
          default:
            throw new OpenGammaRuntimeException("Cannot handle floating type " + swapLeg.getFloatingRateType());
        }
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
        final InterestRateNotional interestRateNotional = (InterestRateNotional) swapLeg.getNotional();
        final Currency currency = interestRateNotional.getCurrency();
        final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapLeg.getRegionId());
        switch (swapLeg.getFloatingRateType()) {
          case IBOR:
            return getIborAnnuity(swapLeg, interestRateNotional, currency, calendar);
          case OIS:
            return getOISAnnuity(swapLeg, interestRateNotional, currency);
          case CMS:
            return getCMSAnnuity(swapLeg, interestRateNotional, currency, calendar);
          case OVERNIGHT_ARITHMETIC_AVERAGE:
            return getOvernightAAverageAnnuity(swapLeg, interestRateNotional, currency);
          default:
            throw new OpenGammaRuntimeException("Cannot handle floating type " + swapLeg.getFloatingRateType());
        }
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
        throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
        throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
        throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
        throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
      }

      @Override
      public final AnnuityDefinition<? extends PaymentDefinition> visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
        throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
      }

      private AnnuityDefinition<? extends PaymentDefinition> getIborAnnuity(final FloatingInterestRateLeg swapLeg, final InterestRateNotional interestRateNotional,
          final Currency currency, final Calendar calendar) {
        final String tenorString = getTenorString(swapLeg.getFrequency());
        final String iborLegConventionName = getConventionName(currency, tenorString, IRS_IBOR_LEG);
        final VanillaIborLegConvention iborLegConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, iborLegConventionName), VanillaIborLegConvention.class);
        final IborIndexConvention iborIndexConvention = _conventionSource.getSingle(iborLegConvention.getIborIndexConvention(), IborIndexConvention.class);
        final Frequency freqIbor = swapLeg.getFrequency();
        final Period tenorIbor = getTenor(freqIbor);
        final int spotLag = iborIndexConvention.getSettlementDays();
        final DayCount dayCount = swapLeg.getDayCount();
        final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
        final double notional = interestRateNotional.getAmount();
        final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(),
            iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
        if (swapLeg instanceof FloatingSpreadIRLeg) {
          final FloatingSpreadIRLeg spread = (FloatingSpreadIRLeg) swapLeg;
          return AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, tenorIbor, notional, iborIndex, isPayer, businessDayConvention, swapLeg.isEom(), dayCount,
              spread.getSpread(), calendar);
        }
        return AnnuityCouponIborDefinition.from(effectiveDate, maturityDate, tenorIbor, notional, iborIndex, isPayer, businessDayConvention, swapLeg.isEom(), dayCount,
            calendar);
      }

      private AnnuityDefinition<? extends PaymentDefinition> getOISAnnuity(final FloatingInterestRateLeg swapLeg, final InterestRateNotional interestRateNotional,
          final Currency currency) {
        final String oisConventionName = getConventionName(currency, OIS_ON_LEG);
        final OISLegConvention oisConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, oisConventionName), OISLegConvention.class);
        final OvernightIndexConvention indexConvention = _conventionSource.getSingle(oisConvention.getOvernightIndexConvention(), OvernightIndexConvention.class);
        final String currencyString = currency.getCode();
        final Integer publicationLag = indexConvention.getPublicationLag();
        final Period paymentFrequency = getTenor(swapLeg.getFrequency());
        final IndexON index = new IndexON(indexConvention.getName(), currency, indexConvention.getDayCount(), publicationLag);
        final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
        final double notional = interestRateNotional.getAmount();
        final int paymentLag = oisConvention.getPaymentLag();
        final boolean isEOM = oisConvention.isIsEOM();
        final Calendar indexCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
        if (swapLeg instanceof FloatingSpreadIRLeg) {
          final FloatingSpreadIRLeg spread = (FloatingSpreadIRLeg) swapLeg;
          return AnnuityCouponONSpreadDefinition.from(effectiveDate, maturityDate, notional, isPayer, index, paymentLag, indexCalendar, businessDayConvention, paymentFrequency, isEOM,
              spread.getSpread());
        }
        return AnnuityCouponONDefinition.from(effectiveDate, maturityDate, notional, isPayer, index, paymentLag, indexCalendar, businessDayConvention, paymentFrequency, isEOM);
      }

      private AnnuityDefinition<? extends PaymentDefinition> getCMSAnnuity(final FloatingInterestRateLeg swapLeg, final InterestRateNotional interestRateNotional,
          final Currency currency, final Calendar calendar) {
        if (swapLeg instanceof FloatingSpreadIRLeg) {
          throw new OpenGammaRuntimeException("Cannot create an annuity for a CMS leg with a spread");
        }
        final String tenorString = getTenorString(swapLeg.getFrequency());
        final String iborLegConventionName = getConventionName(currency, tenorString, IRS_IBOR_LEG);
        final VanillaIborLegConvention iborLegConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, getConventionName(currency, tenorString, IRS_IBOR_LEG)),
            VanillaIborLegConvention.class);
        final IborIndexConvention iborIndexConvention = _conventionSource.getSingle(iborLegConvention.getIborIndexConvention(), IborIndexConvention.class);
        final String swapIndexConventionName = getConventionName(currency, tenorString, SWAP_INDEX);
        final SwapIndexConvention swapIndexConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, swapIndexConventionName), SwapIndexConvention.class);
        final SwapConvention underlyingSwapConvention = _conventionSource.getSingle(swapIndexConvention.getSwapConvention(), SwapConvention.class);
        final SwapFixedLegConvention payLegConvention = _conventionSource.getSingle(underlyingSwapConvention.getPayLegConvention(), SwapFixedLegConvention.class);
        final VanillaIborLegConvention receiveLegConvention = _conventionSource.getSingle(underlyingSwapConvention.getReceiveLegConvention(), VanillaIborLegConvention.class);
        final Frequency freqIbor = swapLeg.getFrequency();
        final Period tenorIbor = getTenor(freqIbor);
        final int spotLag = iborIndexConvention.getSettlementDays();
        final DayCount dayCount = swapLeg.getDayCount();
        final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
        final double notional = interestRateNotional.getAmount();
        final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(),
            iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
        final Period fixedLegPaymentPeriod = payLegConvention.getPaymentTenor().getPeriod();
        final DayCount fixedLegDayCount = payLegConvention.getDayCount();
        final Period period = Period.ofYears(10); // TODO why is a variable field like this in IndexSwap? It's only used in one place in the entire analytics library.
        final IndexSwap swapIndex = new IndexSwap(fixedLegPaymentPeriod, fixedLegDayCount, iborIndex, period, calendar);
        return AnnuityCouponCMSDefinition.from(effectiveDate, maturityDate, notional, swapIndex, tenorIbor, dayCount, isPayer, calendar);
      }

      private AnnuityDefinition<? extends PaymentDefinition> getOvernightAAverageAnnuity(final FloatingInterestRateLeg swapLeg, final InterestRateNotional interestRateNotional,
          final Currency currency) {
        final String oisConventionName = getConventionName(currency, OIS_ON_LEG);
        final OISLegConvention oisConvention = _conventionSource.getSingle(ExternalId.of(SCHEME_NAME, oisConventionName), OISLegConvention.class);
        final OvernightIndexConvention indexConvention = _conventionSource.getSingle(oisConvention.getOvernightIndexConvention(), OvernightIndexConvention.class);
        final String currencyString = currency.getCode();
        final Integer publicationLag = indexConvention.getPublicationLag();
        final Period paymentFrequency = getTenor(swapLeg.getFrequency());
        final IndexON index = new IndexON(indexConvention.getName(), currency, indexConvention.getDayCount(), publicationLag);
        final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
        final double notional = interestRateNotional.getAmount();
        final int paymentLag = oisConvention.getPaymentLag();
        final boolean isEOM = oisConvention.isIsEOM();
        final Calendar indexCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
        if (swapLeg instanceof FloatingSpreadIRLeg) {
          final FloatingSpreadIRLeg spread = (FloatingSpreadIRLeg) swapLeg;
          return AnnuityCouponArithmeticAverageONSpreadDefinition.from(effectiveDate, maturityDate, notional, spread.getSpread(), isPayer, paymentFrequency, index,
              paymentLag, businessDayConvention, isEOM, indexCalendar);
        }
        return AnnuityCouponArithmeticAverageONDefinition.from(effectiveDate, maturityDate, notional, isPayer, paymentFrequency, index, paymentLag,
            businessDayConvention, isEOM, indexCalendar);
      }
    };
  }
}

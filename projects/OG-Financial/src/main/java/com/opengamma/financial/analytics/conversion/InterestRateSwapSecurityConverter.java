/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collection;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleImpl;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.irs.CompoundingMethod;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.lookup.irs.InterestRateSwapNotionalAmountVisitor;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class InterestRateSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A holiday source */
  private final HolidaySource _holidaySource;

  private final ConventionBundleSource _conventionBundleSource;

  /**
   * @param holidaySource The holiday source, not <code>null</code>
   * @param conventionBundleSource The convention bundle source used to retrieve floating rate index conventions, not <code>null</code>
   */
  public InterestRateSwapSecurityConverter(final HolidaySource holidaySource,
                                           final ConventionBundleSource conventionBundleSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionBundleSource, "convention bundle source");
    _holidaySource = holidaySource;
    _conventionBundleSource = conventionBundleSource;
  }

  @Override
  public InstrumentDefinition<?> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return visitSwapSecurity(security);
  }

  @Override
  public InstrumentDefinition<?> visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    ArgumentChecker.notNull(security, "swap security");
    final InterestRateInstrumentType swapType = InterestRateSwapSecurityUtils.getSwapType(security);
    switch (swapType) {
      case SWAP_FIXED_IBOR:
        return getFixedIborSwapDefinition(security, InterestRateSwapSecurityUtils.payFixed(security), false);
      //case SWAP_FIXED_IBOR_WITH_SPREAD:
      //  return getFixedIborSwapDefinition(security, InterestRateSwapSecurityUtils.payFixed(security), true);
      //case SWAP_FIXED_OIS:
      //  return getFixedOISSwapDefinition(security, InterestRateSwapSecurityUtils.payFixed(security));
      default:
        throw new IllegalArgumentException("Unhandled swap type " + security);
      //  final LocalDate effectiveDate = security.getEffectiveDate();
      //  final LocalDate maturityDate = security.getUnadjustedMaturityDate();
      //  final AnnuityDefinition<? extends PaymentDefinition> payLeg = security.getPayLeg().accept(getSwapLegConverter(effectiveDate, maturityDate, true));
      //  final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = security.getReceiveLeg().accept(getSwapLegConverter(effectiveDate, maturityDate, false));
      //  return new SwapDefinition(payLeg, receiveLeg);
    }
  }

  private SwapDefinition getFixedIborSwapDefinition(final InterestRateSwapSecurity swapSecurity, final boolean payFixed, final boolean hasSpread) {
    final LocalDate effectiveDate = swapSecurity.getEffectiveDate();
    final LocalDate maturityDate = swapSecurity.getUnadjustedMaturityDate();
    final InterestRateSwapLeg payLeg = swapSecurity.getPayLeg();
    final InterestRateSwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final Currency currency = payLeg.getNotional().getCurrency();

    final InterestRateSwapNotionalAmountVisitor notionalVisitor = new InterestRateSwapNotionalAmountVisitor();
    
    // Fixed leg 
    final FixedInterestRateSwapLeg fixedLeg = (FixedInterestRateSwapLeg) (payFixed ? payLeg : receiveLeg);
    final Frequency periodFreqFixed = fixedLeg.getConvention().getCalculationFrequency();
    final Period periodTenorFixed;
    if (Frequency.NEVER_NAME.equals(periodFreqFixed.getName())) {
      periodTenorFixed = Period.between(effectiveDate, maturityDate);
    } else {
      periodTenorFixed = getTenor(periodFreqFixed);
    }
    final double fixedLegNotional = fixedLeg.getNotional().accept(notionalVisitor, 0); //fixedLeg.getNotional().getAmount();
    final boolean fixedIsEOM = RollConvention.EOM == fixedLeg.getConvention().getRollConvention();
    DayCount fixedLegDayCount = fixedLeg.getConvention().getDayCountConvention();
    Collection<ExternalId> fixedLegPaymentCalendarIds = fixedLeg.getConvention().getPaymentCalendars();
    Calendar fixedLegPaymentCalendar = new HolidaySourceCalendarAdapter(_holidaySource, fixedLegPaymentCalendarIds.toArray(new ExternalId[fixedLegPaymentCalendarIds.size()]));
    BusinessDayConvention fixedLegFixingBusinessDayConvention = fixedLeg.getConvention().getCalculationBusinessDayConvention();
    
    // Float leg
    final FloatingInterestRateSwapLeg iborLeg = (FloatingInterestRateSwapLeg) (payFixed ? receiveLeg : payLeg);
//    final Currency currency = payLeg.getNotional().getCurrency();
//    final ExternalId[] calendarIds = iborLeg.getConvention().getFixingCalendars().toArray(new ExternalId[iborLeg.getConvention().getFixingCalendars().size()]);
//    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, calendarIds);
    final double iborLegNotional = iborLeg.getNotional().accept(notionalVisitor, 0); //iborLeg.getNotional().getAmount();
    //TODO: This needs to use the proper calendar lookups e.g. USNY,GBLO
    final ExternalId[] floatFixingCalendarIds = iborLeg.getConvention().getFixingCalendars().toArray(new ExternalId[iborLeg.getConvention().getFixingCalendars().size()]);
    final Calendar floatFixingCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatFixingCalendarIds);
//    final IborIndexConvention iborIndexConvention = getIborLegConvention(iborLeg);
    final boolean floatIsEOM = RollConvention.EOM == iborLeg.getConvention().getRollConvention();
    ConventionBundle iborIndexConvention = _conventionBundleSource.getConventionBundle(iborLeg.getFloatingReferenceRateId());
    if (iborIndexConvention == null) {
      // if no convention loaded pull from swap, TODO: Settle on one method
      iborIndexConvention = new ConventionBundleImpl(iborLeg.getFloatingReferenceRateId().toBundle(), iborLeg.getFloatingReferenceRateId().getValue(),
                                                     iborLeg.getConvention().getDayCountConvention(), iborLeg.getConvention().getFixingBusinessDayConvention(),
                                                     iborLeg.getConvention().getSettlementDays(), floatIsEOM);
    }
    final Frequency paymentFreqIbor = iborLeg.getConvention().getPaymentFrequency();
    final Period paymentTenorIbor;
    if (Frequency.NEVER_NAME.equals(paymentFreqIbor.getName())) {
      paymentTenorIbor = Period.between(effectiveDate, maturityDate);
    } else {
      paymentTenorIbor = getTenor(paymentFreqIbor);
    }
    final int spotLag = iborIndexConvention.getSettlementDays();
    Frequency resetFreqIbor = iborLeg.getConvention().getResetFrequency();
    Period resetTenorIbor = getTenor(resetFreqIbor);
    final IborIndex indexIbor = new IborIndex(currency, resetTenorIbor, spotLag, iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isEOMConvention(), iborIndexConvention.getName());
    DayCount floatDayCount = iborLeg.getConvention().getDayCountConvention();
    BusinessDayConvention floatLegFixingBusinessDayConvention = iborLeg.getConvention().getCalculationBusinessDayConvention();
    
    //if (hasSpread) {
    //  final double spread = ((FloatingSpreadIRLeg) iborLeg).getSpread();
    //  return SwapFixedIborSpreadDefinition.from(effectiveDate, maturityDate, tenorFixed, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(), fixedLeg.isEom(), fixedLegNotional,
    //      fixedLeg.getRate(), tenorIbor, iborLeg.getDayCount(), iborLeg.getBusinessDayConvention(), iborLeg.isEom(), iborLegNotional, indexIbor, spread, payFixed, calendar);
    //}
//    final SwapFixedIborDefinition swap = SwapFixedIborDefinition.from(effectiveDate.atStartOfDay(ZoneId.systemDefault()), maturityDate.atStartOfDay(ZoneId.systemDefault()), tenorFixed, fixedLeg.getConvention().getDayCountConvention(), fixedLeg.getConvention().getCalculationBusinessDayConvention(), fixedIsEOM,
//        fixedLegNotional, fixedLeg.getRate().getInitialRate(), tenorIbor, iborLeg.getConvention().getDayCountConvention(), iborLeg.getConvention().getCalculationBusinessDayConvention(), floatIsEOM, iborLegNotional, indexIbor, payFixed, calendar);
    
    ZonedDateTime effectiveDateTime = effectiveDate.atStartOfDay(ZoneId.systemDefault());
    ZonedDateTime maturityDateTime = maturityDate.atStartOfDay(ZoneId.systemDefault());
    
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = null;
    if (fixedLeg.getConvention().getCompoundingMethod() == CompoundingMethod.NONE) {
      firstLeg = AnnuityDefinitionBuilder.couponFixed(
          currency,
          effectiveDateTime,
          maturityDateTime,
          periodTenorFixed, // period and payment dates are generated from these
          fixedLegPaymentCalendar, // period and payment dates are generated from these
          fixedLegDayCount,
          fixedLegFixingBusinessDayConvention,
          fixedIsEOM,
          fixedLegNotional,
          fixedLeg.getRate().getInitialRate(),
          payFixed,
          StubType.SHORT_START,
          fixedLeg.getConvention().getPaymentLag());
    } else {
      throw new OpenGammaRuntimeException("Unsupported compounding method for fixed leg: " + fixedLeg.getConvention().getCompoundingMethod());
    }
    
    
    
    AnnuityDefinition<? extends PaymentDefinition> secondLeg = null;
    if (iborLeg.getConvention().getCompoundingMethod() == CompoundingMethod.NONE) {
      if (iborLeg.getSpreadSchedule() != null && !Double.isNaN(iborLeg.getSpreadSchedule().getInitialRate())) {
        secondLeg = AnnuityDefinitionBuilder.couponIborSpread(
            effectiveDateTime,
            maturityDateTime,
            paymentTenorIbor,
            iborLegNotional,
            parseFlatSpread(iborLeg),
            indexIbor,
            !payFixed,
            floatDayCount,
            floatLegFixingBusinessDayConvention,
            floatIsEOM,
            floatFixingCalendar,
            StubType.SHORT_START,
            0); // TODO payment lag
      } else {
        secondLeg = AnnuityDefinitionBuilder.couponIbor(
            effectiveDateTime,
            maturityDateTime,
            paymentTenorIbor, // period and payment dates are generated from these
            iborLegNotional,
            indexIbor,
            !payFixed,
            floatDayCount,
            floatLegFixingBusinessDayConvention,
            floatIsEOM,
            floatFixingCalendar, // period and payment dates are generated from these
            StubType.SHORT_START,
            0); // TODO payment lag
      }
    } else if (iborLeg.getConvention().getCompoundingMethod()  == CompoundingMethod.FLAT) {
      secondLeg = AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(
          effectiveDateTime,
          maturityDateTime,
          paymentTenorIbor, // period and payment dates are generated from these
          iborLegNotional,
          parseFlatSpread(iborLeg),
          indexIbor,
          StubType.SHORT_START, // TODO stub compounding period
          !payFixed,
          floatLegFixingBusinessDayConvention,
          floatIsEOM,
          floatFixingCalendar, // period and payment dates are generated from these
          StubType.SHORT_START); // TODO stub periods
    } else if (iborLeg.getConvention().getCompoundingMethod() == CompoundingMethod.SPREAD_EXCLUSIVE) {
      secondLeg = AnnuityDefinitionBuilder.couponIborCompoundingSpread(
          effectiveDateTime,
          maturityDateTime,
          paymentTenorIbor,
          iborLegNotional,
          parseFlatSpread(iborLeg),
          indexIbor,
          StubType.SHORT_START, // TODO stub compounding period
          !payFixed,
          floatLegFixingBusinessDayConvention,
          floatIsEOM,
          floatFixingCalendar,
          StubType.SHORT_START);
    } else if (iborLeg.getConvention().getCompoundingMethod() == CompoundingMethod.STRAIGHT) {
      secondLeg = AnnuityDefinitionBuilder.couponIborCompounding(
          effectiveDateTime,
          maturityDateTime,
          paymentTenorIbor, // period and payment dates are generated from these
          iborLegNotional,
          indexIbor,
          StubType.SHORT_START, // TODO stub compounding period
          !payFixed,
          floatLegFixingBusinessDayConvention,
          floatIsEOM,
          floatFixingCalendar, // period and payment dates are generated from these
          StubType.SHORT_START); // TODO stub compounding period
    } else {
      throw new OpenGammaRuntimeException("Unsupported compounding method for fixed leg: " + iborLeg.getConvention().getCompoundingMethod());
    }
    SwapDefinition swap = new SwapDefinition(firstLeg, secondLeg);
    return swap;
  }

  private IborIndexConvention getIborLegConvention(final FloatingInterestRateSwapLeg leg) {
    FloatingInterestRateSwapLegConvention schedule = leg.getConvention();
    //TODO: Note this needs to be modified to take multiple calendars
    IborIndexConvention convention = new IborIndexConvention(schedule.getName(), schedule.getExternalIdBundle(), schedule.getDayCountConvention(),
                                                             schedule.getCalculationBusinessDayConvention(), schedule.getSettlementDays(),
                                                             RollConvention.EOM.equals(schedule.getRollConvention()), leg.getNotional().getCurrency(),
                                                             LocalTime.of(11, 0), "", schedule.getFixingCalendars().iterator().next(), schedule.getPaymentCalendars().iterator().next(),
                                                             "");
    return convention;
  }
  
  private double parseFlatSpread(FloatingInterestRateSwapLeg iborLeg) {
    if (iborLeg.getSpreadSchedule() != null) {
      double spread = iborLeg.getSpreadSchedule().getInitialRate();
      if (!Double.isNaN(spread)) {
        return spread;
      }
    }
    return 0.0;
  }

  //private IborIndexConvention getIborLegConvention(final Currency currency) {
  //  String iborConventionName = getConventionName(currency, EURIBOR);
  //  IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, iborConventionName));
  //  if (iborIndexConvention != null) {
  //    return iborIndexConvention;
  //  }
  //  iborConventionName = getConventionName(currency, LIBOR);
  //  iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, iborConventionName));
  //  if (iborIndexConvention != null) {
  //    return iborIndexConvention;
  //  }
  //  iborConventionName = getConventionName(currency, IBOR);
  //  iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, ExternalId.of(SCHEME_NAME, iborConventionName));
  //  if (iborIndexConvention != null) {
  //    return iborIndexConvention;
  //  }
  //  throw new OpenGammaRuntimeException("Could not get ibor index convention with the identifier " + ExternalId.of(SCHEME_NAME, iborConventionName));
  //}

  //private SwapDefinition getFixedOISSwapDefinition(final SwapSecurity swapSecurity, final boolean payFixed) {
  //  final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
  //  final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();
  //  final SwapLeg payLeg = swapSecurity.getPayLeg();
  //  final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
  //  final FixedInterestRateSwapLeg fixedLeg = (FixedInterestRateSwapLeg) (payFixed ? payLeg : receiveLeg);
  //  final FloatingInterestRateSwapLeg floatLeg = (FloatingInterestRateSwapLeg) (payFixed ? receiveLeg : payLeg);
  //  final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
  //  final String overnightConventionName = getConventionName(currency, OVERNIGHT);
  //  final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, ExternalId.of(SCHEME_NAME, overnightConventionName));
  //  if (indexConvention == null) {
  //    throw new OpenGammaRuntimeException("Could not get OIS index convention with the identifier " + ExternalId.of(SCHEME_NAME, overnightConventionName));
  //  }
  //  final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
  //  final String currencyString = currency.getCode();
  //  final Integer publicationLag = indexConvention.getPublicationLag();
  //  final Period paymentFrequency = getTenor(floatLeg.getFrequency());
  //  final IndexON index = new IndexON(indexConvention.getName(), currency, indexConvention.getDayCount(), publicationLag);
  //  final GeneratorSwapFixedON generator = new GeneratorSwapFixedON(currencyString + "_OIS_Convention", index, paymentFrequency, fixedLeg.getDayCount(), fixedLeg.getBusinessDayConvention(),
  //      fixedLeg.isEom(), 0, 1 - publicationLag, calendar); // TODO: The payment lag is not available at the security level!
  //  final double notionalFixed = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
  //  final double notionalOIS = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
  //  return SwapFixedONDefinition.from(effectiveDate, maturityDate, notionalFixed, notionalOIS, generator, fixedLeg.getRate(), payFixed);
  //}

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

  //private SwapLegVisitor<AnnuityDefinition<? extends PaymentDefinition>> getSwapLegConverter(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final boolean isPayer) {
  //  return new SwapLegVisitor<AnnuityDefinition<? extends PaymentDefinition>>() {
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFixedInterestRateLeg(final FixedInterestRateSwapLeg swapLeg) {
  //      final ExternalId regionId = swapLeg.getRegionId();
  //      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, regionId);
  //      final InterestRateNotional interestRateNotional = (InterestRateNotional) swapLeg.getNotional();
  //      final Currency currency = interestRateNotional.getCurrency();
  //      final String fixedLegConventionName = getConventionName(currency, IRS_FIXED_LEG);
  //      final SwapFixedLegConvention fixedLegConvention = _conventionSource.getConvention(SwapFixedLegConvention.class, ExternalId.of(SCHEME_NAME, fixedLegConventionName));
  //      if (fixedLegConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get fixed leg convention with the identifier " + ExternalId.of(SCHEME_NAME, fixedLegConventionName));
  //      }
  //      final Frequency freqFixed = swapLeg.getFrequency();
  //      final Period tenorFixed = getTenor(freqFixed);
  //      final double notional = interestRateNotional.getAmount();
  //      final DayCount dayCount = fixedLegConvention.getDayCount();
  //      final boolean isEOM = fixedLegConvention.isIsEOM();
  //      final double rate = swapLeg.getRate();
  //      final BusinessDayConvention businessDayConvention = fixedLegConvention.getBusinessDayConvention();
  //      return AnnuityCouponFixedDefinition.from(currency, effectiveDate, maturityDate, tenorFixed, calendar, dayCount,
  //          businessDayConvention, isEOM, notional, rate, isPayer);
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingInterestRateLeg(final FloatingInterestRateSwapLeg swapLeg) {
  //      final InterestRateNotional interestRateNotional = (InterestRateNotional) swapLeg.getNotional();
  //      final Currency currency = interestRateNotional.getCurrency();
  //      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapLeg.getRegionId());
  //      switch (swapLeg.getFloatingRateType()) {
  //        case IBOR:
  //          return getIborAnnuity(swapLeg, interestRateNotional, currency, calendar);
  //        case OIS:
  //          return getOISAnnuity(swapLeg, interestRateNotional, currency);
  //        case CMS:
  //          return getCMSAnnuity(swapLeg, interestRateNotional, currency, calendar);
  //        case OVERNIGHT_ARITHMETIC_AVERAGE:
  //          return getOvernightAAverageAnnuity(swapLeg, interestRateNotional, currency);
  //        default:
  //          throw new OpenGammaRuntimeException("Cannot handle floating type " + swapLeg.getFloatingRateType());
  //      }
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
  //      final InterestRateNotional interestRateNotional = (InterestRateNotional) swapLeg.getNotional();
  //      final Currency currency = interestRateNotional.getCurrency();
  //      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapLeg.getRegionId());
  //      switch (swapLeg.getFloatingRateType()) {
  //        case IBOR:
  //          return getIborAnnuity(swapLeg, interestRateNotional, currency, calendar);
  //        case OIS:
  //          return getOISAnnuity(swapLeg, interestRateNotional, currency);
  //        case CMS:
  //          return getCMSAnnuity(swapLeg, interestRateNotional, currency, calendar);
  //        case OVERNIGHT_ARITHMETIC_AVERAGE:
  //          return getOvernightAAverageAnnuity(swapLeg, interestRateNotional, currency);
  //        default:
  //          throw new OpenGammaRuntimeException("Cannot handle floating type " + swapLeg.getFloatingRateType());
  //      }
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
  //      throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
  //      throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
  //      throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
  //      throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
  //    }
  //
  //    @Override
  //    public final AnnuityDefinition<? extends PaymentDefinition> visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
  //      throw new OpenGammaRuntimeException("Cannot handle " + swapLeg.getClass());
  //    }
  //
  //    private AnnuityDefinition<? extends PaymentDefinition> getIborAnnuity(final FloatingInterestRateSwapLeg swapLeg, final InterestRateNotional interestRateNotional,
  //        final Currency currency, final Calendar calendar) {
  //      final String tenorString = getTenorString(swapLeg.getFrequency());
  //      final String iborLegConventionName = getConventionName(currency, tenorString, IRS_IBOR_LEG);
  //      final VanillaIborLegConvention iborLegConvention = _conventionSource.getConvention(VanillaIborLegConvention.class, ExternalId.of(SCHEME_NAME, iborLegConventionName));
  //      if (iborLegConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get Ibor leg convention with the identifier " + ExternalId.of(SCHEME_NAME, iborLegConventionName));
  //      }
  //      final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
  //      final Frequency freqIbor = swapLeg.getFrequency();
  //      final Period tenorIbor = getTenor(freqIbor);
  //      final int spotLag = iborIndexConvention.getSettlementDays();
  //      final DayCount dayCount = swapLeg.getDayCount();
  //      final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
  //      final double notional = interestRateNotional.getAmount();
  //      final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(),
  //          iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
  //      if (swapLeg instanceof FloatingSpreadIRLeg) {
  //        final FloatingSpreadIRLeg spread = (FloatingSpreadIRLeg) swapLeg;
  //        return AnnuityCouponIborSpreadDefinition.from(effectiveDate, maturityDate, tenorIbor, notional, iborIndex, isPayer, businessDayConvention, swapLeg.isEom(), dayCount,
  //            spread.getSpread(), calendar);
  //      }
  //      return AnnuityCouponIborDefinition.from(effectiveDate, maturityDate, tenorIbor, notional, iborIndex, isPayer, businessDayConvention, swapLeg.isEom(), dayCount,
  //          calendar);
  //    }
  //
  //    private AnnuityDefinition<? extends PaymentDefinition> getOISAnnuity(final FloatingInterestRateSwapLeg swapLeg, final InterestRateNotional interestRateNotional,
  //        final Currency currency) {
  //      final String oisConventionName = getConventionName(currency, OIS_ON_LEG);
  //      final OISLegConvention oisConvention = _conventionSource.getConvention(OISLegConvention.class, ExternalId.of(SCHEME_NAME, oisConventionName));
  //      if (oisConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get OIS leg convention with the identifier " + ExternalId.of(SCHEME_NAME, oisConventionName));
  //      }
  //      final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, oisConvention.getOvernightIndexConvention());
  //      if (indexConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get OIS index convention with the identifier " + oisConvention.getOvernightIndexConvention());
  //      }
  //      final String currencyString = currency.getCode();
  //      final Integer publicationLag = indexConvention.getPublicationLag();
  //      final Period paymentFrequency = getTenor(swapLeg.getFrequency());
  //      final IndexON index = new IndexON(indexConvention.getName(), currency, indexConvention.getDayCount(), publicationLag);
  //      final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
  //      final double notional = interestRateNotional.getAmount();
  //      final int paymentLag = oisConvention.getPaymentLag();
  //      final boolean isEOM = oisConvention.isIsEOM();
  //      final Calendar indexCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
  //      if (swapLeg instanceof FloatingSpreadIRLeg) {
  //        final FloatingSpreadIRLeg spread = (FloatingSpreadIRLeg) swapLeg;
  //        return AnnuityCouponONSpreadDefinition.from(effectiveDate, maturityDate, notional, isPayer, index, paymentLag, indexCalendar, businessDayConvention, paymentFrequency, isEOM,
  //            spread.getSpread());
  //      }
  //      return AnnuityCouponONDefinition.from(effectiveDate, maturityDate, notional, isPayer, index, paymentLag, indexCalendar, businessDayConvention, paymentFrequency, isEOM);
  //    }
  //
  //    private AnnuityDefinition<? extends PaymentDefinition> getCMSAnnuity(final FloatingInterestRateSwapLeg swapLeg, final InterestRateNotional interestRateNotional,
  //        final Currency currency, final Calendar calendar) {
  //      if (swapLeg instanceof FloatingSpreadIRLeg) {
  //        throw new OpenGammaRuntimeException("Cannot create an annuity for a CMS leg with a spread");
  //      }
  //      final String tenorString = getTenorString(swapLeg.getFrequency());
  //      final String iborLegConventionName = getConventionName(currency, tenorString, IRS_IBOR_LEG);
  //      final VanillaIborLegConvention iborLegConvention = _conventionSource.getConvention(VanillaIborLegConvention.class,
  //          ExternalId.of(SCHEME_NAME, getConventionName(currency, tenorString, IRS_IBOR_LEG)));
  //      if (iborLegConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get Ibor leg convention with the identifier " + ExternalId.of(SCHEME_NAME, iborLegConventionName));
  //      }
  //      final IborIndexConvention iborIndexConvention = _conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
  //      final String swapIndexConventionName = getConventionName(currency, tenorString, SWAP_INDEX);
  //      final SwapIndexConvention swapIndexConvention = _conventionSource.getConvention(SwapIndexConvention.class, ExternalId.of(SCHEME_NAME, swapIndexConventionName));
  //      if (swapIndexConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get swap index convention with the identifier " + ExternalId.of(SCHEME_NAME, swapIndexConventionName));
  //      }
  //      final SwapConvention underlyingSwapConvention = _conventionSource.getConvention(SwapConvention.class, swapIndexConvention.getSwapConvention());
  //      if (underlyingSwapConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get swap convention with the identifier " + swapIndexConvention.getSwapConvention());
  //      }
  //      final SwapFixedLegConvention payLegConvention = _conventionSource.getConvention(SwapFixedLegConvention.class, underlyingSwapConvention.getPayLegConvention());
  //      if (payLegConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get convention with the identifier " + underlyingSwapConvention.getPayLegConvention());
  //      }
  //      final VanillaIborLegConvention receiveLegConvention = _conventionSource.getConvention(VanillaIborLegConvention.class, underlyingSwapConvention.getReceiveLegConvention());
  //      if (receiveLegConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get convention with the identifier " + underlyingSwapConvention.getReceiveLegConvention());
  //      }
  //      final Frequency freqIbor = swapLeg.getFrequency();
  //      final Period tenorIbor = getTenor(freqIbor);
  //      final int spotLag = iborIndexConvention.getSettlementDays();
  //      final DayCount dayCount = swapLeg.getDayCount();
  //      final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
  //      final double notional = interestRateNotional.getAmount();
  //      final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(),
  //          iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
  //      final Period fixedLegPaymentPeriod = payLegConvention.getPaymentTenor().getPeriod();
  //      final DayCount fixedLegDayCount = payLegConvention.getDayCount();
  //      final Period period = Period.ofYears(10); // TODO why is a variable field like this in IndexSwap? It's only used in one place in the entire analytics library.
  //      final IndexSwap swapIndex = new IndexSwap(fixedLegPaymentPeriod, fixedLegDayCount, iborIndex, period, calendar);
  //      return AnnuityCouponCMSDefinition.from(effectiveDate, maturityDate, notional, swapIndex, tenorIbor, dayCount, isPayer, calendar);
  //    }
  //
  //    private AnnuityDefinition<? extends PaymentDefinition> getOvernightAAverageAnnuity(final FloatingInterestRateSwapLeg swapLeg, final InterestRateNotional interestRateNotional,
  //        final Currency currency) {
  //      final String oisConventionName = getConventionName(currency, OIS_ON_LEG);
  //      final OISLegConvention oisConvention = _conventionSource.getConvention(OISLegConvention.class, ExternalId.of(SCHEME_NAME, oisConventionName));
  //      if (oisConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get OIS leg convention with the identifier " + ExternalId.of(SCHEME_NAME, oisConventionName));
  //      }
  //      final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, oisConvention.getOvernightIndexConvention());
  //      if (indexConvention == null) {
  //        throw new OpenGammaRuntimeException("Could not get OIS index convention with the identifier " + oisConvention.getOvernightIndexConvention());
  //      }
  //      final String currencyString = currency.getCode();
  //      final Integer publicationLag = indexConvention.getPublicationLag();
  //      final Period paymentFrequency = getTenor(swapLeg.getFrequency());
  //      final IndexON index = new IndexON(indexConvention.getName(), currency, indexConvention.getDayCount(), publicationLag);
  //      final BusinessDayConvention businessDayConvention = swapLeg.getBusinessDayConvention();
  //      final double notional = interestRateNotional.getAmount();
  //      final int paymentLag = oisConvention.getPaymentLag();
  //      final boolean isEOM = oisConvention.isIsEOM();
  //      final Calendar indexCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
  //      if (swapLeg instanceof FloatingSpreadIRLeg) {
  //        final FloatingSpreadIRLeg spread = (FloatingSpreadIRLeg) swapLeg;
  //        return AnnuityCouponArithmeticAverageONSpreadDefinition.from(effectiveDate, maturityDate, notional, spread.getSpread(), isPayer, paymentFrequency, index,
  //            paymentLag, businessDayConvention, isEOM, indexCalendar);
  //      }
  //      return AnnuityCouponArithmeticAverageONDefinition.from(effectiveDate, maturityDate, notional, isPayer, paymentFrequency, index, paymentLag,
  //          businessDayConvention, isEOM, indexCalendar);
  //    }
  //  };
  //}
}

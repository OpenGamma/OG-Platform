/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Collection;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.irs.CompoundingMethod;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.PeriodRelationship;
import com.opengamma.financial.security.lookup.irs.InterestRateSwapNotionalAmountVisitor;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class InterestRateSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A holiday source */
  private final HolidaySource _holidaySource;

  /**
   * @param holidaySource The holiday source, not <code>null</code>
   */
  public InterestRateSwapSecurityConverter(final HolidaySource holidaySource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    _holidaySource = holidaySource;
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
        return getFixedIborSwapDefinition(security, InterestRateSwapSecurityUtils.payFixed(security));
      //case SWAP_FIXED_IBOR_WITH_SPREAD:
      //  return getFixedIborSwapDefinition(security, InterestRateSwapSecurityUtils.payFixed(security), true);
      case SWAP_FIXED_OIS:
        return getFixedOISSwapDefinition(security, InterestRateSwapSecurityUtils.payFixed(security));
      default:
        throw new IllegalArgumentException("Unhandled swap type " + security);
      //  final LocalDate effectiveDate = security.getEffectiveDate();
      //  final LocalDate maturityDate = security.getUnadjustedMaturityDate();
      //  final AnnuityDefinition<? extends PaymentDefinition> payLeg = security.getPayLeg().accept(getSwapLegConverter(effectiveDate, maturityDate, true));
      //  final AnnuityDefinition<? extends PaymentDefinition> receiveLeg = security.getReceiveLeg().accept(getSwapLegConverter(effectiveDate, maturityDate, false));
      //  return new SwapDefinition(payLeg, receiveLeg);
    }
  }

  private SwapDefinition getFixedIborSwapDefinition(final InterestRateSwapSecurity swapSecurity, final boolean payFixed) {
    final LocalDate effectiveDate = swapSecurity.getEffectiveDate();
    final LocalDate maturityDate = swapSecurity.getUnadjustedMaturityDate();
    final InterestRateSwapLeg payLeg = swapSecurity.getPayLeg();
    final InterestRateSwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final Currency currency = payLeg.getNotional().getCurrency();

    // Swap data
    final double signFixed = (payFixed ? -1.0 : 1.0);
    int nbNotional = 0;
    nbNotional = (swapSecurity.getNotionalExchange().isExchangeInitialNotional() ? nbNotional + 1 : nbNotional);
    nbNotional = (swapSecurity.getNotionalExchange().isExchangeFinalNotional() ? nbNotional + 1 : nbNotional);

    final InterestRateSwapNotionalAmountVisitor notionalVisitor = new InterestRateSwapNotionalAmountVisitor();

    // Fixed leg
    final FixedInterestRateSwapLeg fixedLeg = (FixedInterestRateSwapLeg) (payFixed ? payLeg : receiveLeg);
    final Frequency periodFreqFixed = fixedLeg.getConvention().getCalculationFrequency();
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
    final ExternalId[] floatResetCalendarIds = iborLeg.getConvention().getResetCalendars().toArray(new ExternalId[iborLeg.getConvention().getResetCalendars().size()]);
    final Calendar floatResetCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatResetCalendarIds);
    final boolean floatIsEOM = RollConvention.EOM == iborLeg.getConvention().getRollConvention();
    final Frequency paymentFreqIbor = iborLeg.getConvention().getPaymentFrequency();
    final Period paymentTenorIbor;
    if (Frequency.NEVER_NAME.equals(paymentFreqIbor.getName())) {
      paymentTenorIbor = Period.between(effectiveDate, maturityDate);
    } else {
      paymentTenorIbor = getTenor(paymentFreqIbor);
    }
    final int spotLag = iborLeg.getConvention().getSettlementDays();
    Frequency resetFreqIbor = iborLeg.getConvention().getResetFrequency();
    Period resetTenorIbor = getTenor(resetFreqIbor);
    DayCount floatDayCount = iborLeg.getConvention().getDayCountConvention();
    final IborIndex indexIbor = new IborIndex(iborLeg.getNotional().getCurrency(), resetTenorIbor, spotLag, floatDayCount,
                                               iborLeg.getConvention().getPaymentDayConvention(),
                                               floatIsEOM, iborLeg.getFloatingReferenceRateId().getValue());
    BusinessDayConvention floatLegFixingBusinessDayConvention = iborLeg.getConvention().getFixingBusinessDayConvention();

    ZonedDateTime effectiveDateTime = effectiveDate.atStartOfDay(ZoneId.systemDefault());
    ZonedDateTime maturityDateTime = maturityDate.atStartOfDay(ZoneId.systemDefault());

    AnnuityDefinition<? extends PaymentDefinition> firstLeg = generateFixedAnnuity(swapSecurity,
                                                                                   payFixed,
                                                                                   currency,
                                                                                   signFixed,
                                                                                   nbNotional,
                                                                                   fixedLeg,
                                                                                   periodFreqFixed,
                                                                                   fixedLegNotional,
                                                                                   fixedIsEOM,
                                                                                   fixedLegDayCount,
                                                                                   fixedLegPaymentCalendar,
                                                                                   fixedLegFixingBusinessDayConvention,
                                                                                   effectiveDateTime,
                                                                                   maturityDateTime);


    RollDateAdjuster rollDateAdjuster = getRollDateAdjuster(iborLeg.getConvention().getRollConvention());
    AnnuityDefinition<? extends PaymentDefinition> secondLeg = null;
    if (Frequency.NEVER_NAME.equals(paymentFreqIbor.getName())) {
      CouponDefinition[] payments = new CouponDefinition[nbNotional + 1];
      int loopnot = 0;
      if (swapSecurity.getNotionalExchange().isExchangeInitialNotional()) {
        payments[0] = new CouponFixedDefinition(currency, effectiveDateTime, effectiveDateTime, effectiveDateTime, 1.0, signFixed * iborLegNotional, 1.0);
        loopnot++;
      }
      payments[loopnot] = CouponIborCompoundingDefinition.from(-signFixed * iborLegNotional, effectiveDateTime, maturityDateTime, indexIbor, StubType.SHORT_START,
                                                               indexIbor.getBusinessDayConvention(), indexIbor.isEndOfMonth(), floatResetCalendar, rollDateAdjuster); // TODO: add spread and compounding type
      if (swapSecurity.getNotionalExchange().isExchangeFinalNotional()) {
        payments[loopnot + 1] = new CouponFixedDefinition(currency, maturityDateTime, maturityDateTime, maturityDateTime, 1.0, -signFixed * iborLegNotional, 1.0);
      }
      secondLeg = new AnnuityDefinition<>(payments, floatResetCalendar);
    } else {
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
              floatResetCalendar,
              fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START,
              0, rollDateAdjuster); // TODO payment lag
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
              floatResetCalendar, // period and payment dates are generated from these
              fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START,
              0, rollDateAdjuster); // TODO payment lag
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
            floatResetCalendar, // period and payment dates are generated from these
            fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START,
            rollDateAdjuster); // TODO stub periods
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
            floatResetCalendar,
            fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START,
            rollDateAdjuster);
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
            floatResetCalendar, // period and payment dates are generated from these
            fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START,
            rollDateAdjuster); // TODO stub compounding period
      } else {
        throw new OpenGammaRuntimeException("Unsupported compounding method for fixed leg: " + iborLeg.getConvention().getCompoundingMethod());
      }
    }
    SwapDefinition swap = new SwapDefinition(firstLeg, secondLeg);
    return swap;
  }

  private AnnuityDefinition<? extends PaymentDefinition> generateFixedAnnuity(InterestRateSwapSecurity swapSecurity,
                                                                              boolean payFixed,
                                                                              Currency currency,
                                                                              double signFixed,
                                                                              int nbNotional,
                                                                              FixedInterestRateSwapLeg fixedLeg,
                                                                              Frequency periodFreqFixed,
                                                                              double fixedLegNotional,
                                                                              boolean fixedIsEOM,
                                                                              DayCount fixedLegDayCount,
                                                                              Calendar fixedLegPaymentCalendar,
                                                                              BusinessDayConvention fixedLegFixingBusinessDayConvention,
                                                                              ZonedDateTime effectiveDateTime,
                                                                              ZonedDateTime maturityDateTime) {
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = null;
    if (Frequency.NEVER_NAME.equals(periodFreqFixed.getName())) {
      firstLeg = generateZCFixedAnnuity(swapSecurity,
                                        currency,
                                        signFixed,
                                        nbNotional,
                                        fixedLegNotional,
                                        fixedLegPaymentCalendar,
                                        effectiveDateTime,
                                        maturityDateTime);
    } else {
      if (fixedLeg.getConvention().getCompoundingMethod() == CompoundingMethod.NONE) {

        // TODO: Check calc frequency is the correct frequency & not index frequency
        Period freq = getTenor(periodFreqFixed);
        RollDateAdjuster rollDateAdjuster = getRollDateAdjuster(fixedLeg.getConvention().getRollConvention());
        final StubType stub = fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START;

        // just using this to get number of periods - should be refactored
        ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDateTime, maturityDateTime, freq, stub);
        final int numPeriods = unadjustedDates.length;

        //TODO: Is 1 the correct start or should it be zero indexed?
        //firstLeg = AnnuityDefinitionBuilder.couponFixedRollDate(currency, effectiveDateTime, 0, numPeriods, rollDateAdjuster,
        //                                                        freq, fixedLegNotional, fixedLeg.getRate().getInitialRate(),
        //                                                        payFixed, fixedLegDayCount, fixedLegPaymentCalendar, stub, fixedLeg.getConvention().getPaymentLag());
        firstLeg = AnnuityDefinitionBuilder.couponFixed(
            currency,
            effectiveDateTime,
            maturityDateTime,
            getTenor(periodFreqFixed), // period and payment dates are generated from these
            fixedLegPaymentCalendar, // period and payment dates are generated from these
            fixedLegDayCount,
            fixedLegFixingBusinessDayConvention,
            fixedIsEOM,
            fixedLegNotional,
            fixedLeg.getRate().getInitialRate(),
            payFixed,
            fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START,
            fixedLeg.getConvention().getPaymentLag(),
            rollDateAdjuster);

        if (firstLeg.getNumberOfPayments() == 0) {
          throw new OpenGammaRuntimeException("");
        }
      } else {
        throw new OpenGammaRuntimeException("Unsupported compounding method for fixed leg: " + fixedLeg.getConvention().getCompoundingMethod());
      }
    }
    return firstLeg;
  }

  private RollDateAdjuster getRollDateAdjuster(RollConvention rollConvention) {
    if (rollConvention == RollConvention.NONE) {
      return null;
    }
    return rollConvention.getRollDateAdjuster(0);
  }

  private AnnuityDefinition<? extends PaymentDefinition> generateZCFixedAnnuity(InterestRateSwapSecurity swapSecurity,
                                                                                Currency currency,
                                                                                double signFixed,
                                                                                int nbNotional,
                                                                                double fixedLegNotional,
                                                                                Calendar fixedLegPaymentCalendar,
                                                                                ZonedDateTime effectiveDateTime,
                                                                                ZonedDateTime maturityDateTime) {
    AnnuityDefinition<? extends PaymentDefinition> firstLeg;
    final int nbPayment = Math.max(nbNotional, 1); // Implementation note: If zero-coupon with no notional, create a fake coupon of 0.
    final double accruedEnd = (nbNotional == 0 ? 0.0 : 1.0);
    CouponFixedDefinition[] notional = new CouponFixedDefinition[nbPayment];
    int loopnot = 0;
    if (swapSecurity.getNotionalExchange().isExchangeInitialNotional()) {
      notional[0] = new CouponFixedDefinition(currency, effectiveDateTime, effectiveDateTime, effectiveDateTime, 1.0, -signFixed * fixedLegNotional, 1.0);
      loopnot++;
    }
    if (swapSecurity.getNotionalExchange().isExchangeFinalNotional() || (nbNotional == 0)) {
      notional[loopnot] = new CouponFixedDefinition(currency, maturityDateTime, maturityDateTime, maturityDateTime, accruedEnd, signFixed * fixedLegNotional, 1.0);
    }
    firstLeg = new AnnuityCouponFixedDefinition(notional, fixedLegPaymentCalendar);
    return firstLeg;
  }

  private SwapDefinition getFixedOISSwapDefinition(final InterestRateSwapSecurity swapSecurity, final boolean payFixed) {
    final LocalDate effectiveDate = swapSecurity.getEffectiveDate();
    final LocalDate maturityDate = swapSecurity.getUnadjustedMaturityDate();
    ZonedDateTime effectiveDateTime = effectiveDate.atStartOfDay(ZoneId.systemDefault());
    ZonedDateTime maturityDateTime = maturityDate.atStartOfDay(ZoneId.systemDefault());

    final InterestRateSwapLeg payLeg = swapSecurity.getPayLeg();
    final InterestRateSwapLeg receiveLeg = swapSecurity.getReceiveLeg();

    // Fixed leg
    final FixedInterestRateSwapLeg fixedLeg = (FixedInterestRateSwapLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateSwapLeg floatLeg = (FloatingInterestRateSwapLeg) (payFixed ? receiveLeg : payLeg);

    final Currency currency = payLeg.getNotional().getCurrency();
    final boolean isEOM = RollConvention.EOM == fixedLeg.getConvention().getRollConvention();
    final int publicationLag = PeriodRelationship.BEGINNING == floatLeg.getConvention().getResetRelativeTo() ? 0 : 1; // end of period == 1 day
    final IndexON index = new IndexON(floatLeg.getConvention().getName(), currency, floatLeg.getConvention().getDayCountConvention(), publicationLag);
    final ExternalId[] floatFixingCalendarIds = floatLeg.getConvention().getFixingCalendars().toArray(new ExternalId[floatLeg.getConvention().getFixingCalendars().size()]);
    final Calendar floatFixingCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatFixingCalendarIds);
    final Period paymentTenor = PeriodFrequency.convertToPeriodFrequency(floatLeg.getConvention().getPaymentFrequency()).getPeriod();
    final GeneratorSwapFixedON generator = new GeneratorSwapFixedON(currency.getCode() + "_OIS_Convention", index, paymentTenor,
        fixedLeg.getConvention().getDayCountConvention(), floatLeg.getConvention().getFixingBusinessDayConvention(),
        isEOM, floatLeg.getConvention().getSettlementDays(), publicationLag, floatFixingCalendar);
    final double notionalFixed = fixedLeg.getNotional().getAmount();
    final double notionalOIS = floatLeg.getNotional().getAmount();
    return SwapFixedONDefinition.from(effectiveDateTime, maturityDateTime, notionalFixed, notionalOIS,
                                      generator, fixedLeg.getRate().getInitialRate(), payFixed);
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
}

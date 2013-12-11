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

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborONDefinition;
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
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapNotionalVisitor;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.PeriodRelationship;
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
        return getFixedIborSwapDefinition(security);
      case SWAP_FIXED_OIS:
        return getFixedOISSwapDefinition(security);
      case SWAP_IBOR_OIS:
        return getIborOISSwapDefinition(security);
      case SWAP_IBOR_IBOR:
        return getIborIborSwapDefinition(security);
      default:
        throw new IllegalArgumentException("Unhandled swap type " + swapType);
    }
  }

  private SwapDefinition getFixedIborSwapDefinition(final InterestRateSwapSecurity swapSecurity) {
    final FixedInterestRateSwapLeg fixedLeg = Iterables.getOnlyElement(swapSecurity.getLegs(FixedInterestRateSwapLeg.class));
    final FloatingInterestRateSwapLeg floatLeg = Iterables.getOnlyElement(swapSecurity.getLegs(FloatingInterestRateSwapLeg.class));
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = generateFixedAnnuity(swapSecurity, fixedLeg);
    AnnuityDefinition<? extends PaymentDefinition> secondLeg = getIborAnnuity(swapSecurity, floatLeg);
    SwapDefinition swap = new SwapDefinition(firstLeg, secondLeg);
    return swap;
  }

  private SwapDefinition getIborIborSwapDefinition(final InterestRateSwapSecurity swapSecurity) {
    final FloatingInterestRateSwapLeg iborLeg1 = (FloatingInterestRateSwapLeg) swapSecurity.getPayLeg();
    final FloatingInterestRateSwapLeg iborLeg2 = (FloatingInterestRateSwapLeg) swapSecurity.getReceiveLeg();
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = getIborAnnuity(swapSecurity, iborLeg1);
    AnnuityDefinition<? extends PaymentDefinition> secondLeg = getIborAnnuity(swapSecurity, iborLeg2);
    SwapDefinition swap = new SwapDefinition(firstLeg, secondLeg);
    return swap;
  }

  private int getNbNotionalPayments(InterestRateSwapSecurity swapSecurity) {
    int nbNotional = 0;
    nbNotional = (swapSecurity.getNotionalExchange().isExchangeInitialNotional() ? nbNotional + 1 : nbNotional);
    nbNotional = (swapSecurity.getNotionalExchange().isExchangeFinalNotional() ? nbNotional + 1 : nbNotional);
    return nbNotional;
  }

  private AnnuityDefinition<? extends PaymentDefinition> getIborAnnuity(InterestRateSwapSecurity swapSecurity,
                                                                        FloatingInterestRateSwapLeg iborLeg) {
    AnnuityDefinition<? extends PaymentDefinition> secondLeg;
    final boolean isPay = iborLeg.getPayReceiveType() == PayReceiveType.PAY;
    final double signFixed = (isPay ? -1.0 : 1.0);
    final Currency currency = iborLeg.getNotional().getCurrency();
    int nbNotional = getNbNotionalPayments(swapSecurity);
    final StubType stubType = iborLeg.getStubCalculationMethod() != null ? iborLeg.getStubCalculationMethod().getType() : StubType.SHORT_START;
    final RollDateAdjuster rollDateAdjuster = getRollDateAdjuster(iborLeg.getConvention().getRollConvention());
    final boolean floatIsEOM = iborLeg.getConvention().getRollConvention() == RollConvention.EOM;
    final Frequency paymentFreqIbor = iborLeg.getConvention().getPaymentFrequency();
    final Period paymentTenorIbor;
    if (Frequency.NEVER_NAME.equals(paymentFreqIbor.getName())) {
      paymentTenorIbor = Period.between(swapSecurity.getEffectiveDate(), swapSecurity.getUnadjustedMaturityDate());
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
    BusinessDayConvention floatLegAccrualPeriodBusinessDayConvention = iborLeg.getConvention().getCalculationBusinessDayConvention();
    ZonedDateTime effectiveDateTime = getZonedDateTime(swapSecurity.getEffectiveDate());
    ZonedDateTime maturityDateTime = getZonedDateTime(swapSecurity.getUnadjustedMaturityDate());
    final ExternalId[] floatResetCalendarIds = iborLeg.getConvention().getResetCalendars().toArray(new ExternalId[iborLeg.getConvention().getResetCalendars().size()]);
    final Calendar floatResetCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatResetCalendarIds);
    NotionalProvider notional = getNotionalProvider(iborLeg.getNotional());

    if (Frequency.NEVER_NAME.equals(paymentFreqIbor.getName())) {
      CouponDefinition[] payments = new CouponDefinition[nbNotional + 1];
      int loopnot = 0;
      if (swapSecurity.getNotionalExchange().isExchangeInitialNotional()) {
        payments[0] = new CouponFixedDefinition(currency, effectiveDateTime, effectiveDateTime, effectiveDateTime, 1.0, -signFixed * iborLeg.getNotional().getInitialAmount(), 1.0);
        loopnot++;
      }
      payments[loopnot] = CouponIborCompoundingDefinition.from(signFixed * notional.getAmount(maturityDateTime.toLocalDate()),
                                                               effectiveDateTime,
                                                               maturityDateTime,
                                                               indexIbor,
                                                               StubType.SHORT_START,
                                                               indexIbor.getBusinessDayConvention(),
                                                               indexIbor.isEndOfMonth(),
                                                               floatResetCalendar,
                                                               rollDateAdjuster); // TODO: add spread and compounding type
      if (swapSecurity.getNotionalExchange().isExchangeFinalNotional()) {
        payments[loopnot + 1] = new CouponFixedDefinition(currency, maturityDateTime, maturityDateTime, maturityDateTime, 1.0, signFixed * notional.getAmount(maturityDateTime.toLocalDate()), 1.0);
      }
      secondLeg = new AnnuityDefinition<>(payments, floatResetCalendar);
    } else {
      if (iborLeg.getConvention().getCompoundingMethod() == CompoundingMethod.NONE) {
        if (iborLeg.getSpreadSchedule() != null && !Double.isNaN(iborLeg.getSpreadSchedule().getInitialRate())) {
          secondLeg = AnnuityDefinitionBuilder.couponIborSpread(effectiveDateTime, maturityDateTime, paymentTenorIbor,
              notional, parseFlatSpread(iborLeg), indexIbor, isPay, floatDayCount,
              floatLegAccrualPeriodBusinessDayConvention, floatIsEOM, floatResetCalendar, stubType,
              0, rollDateAdjuster); // TODO payment lag
        } else {
          secondLeg = AnnuityDefinitionBuilder.couponIbor(effectiveDateTime, maturityDateTime,
              paymentTenorIbor, // period and payment dates are generated from these
              notional, indexIbor, isPay, floatDayCount, floatLegAccrualPeriodBusinessDayConvention, floatIsEOM,
              floatResetCalendar, // period and payment dates are generated from these
              stubType, 0, rollDateAdjuster); // TODO payment lag
        }
      } else if (iborLeg.getConvention().getCompoundingMethod()  == CompoundingMethod.FLAT) {
        secondLeg = AnnuityDefinitionBuilder.couponIborCompoundingFlatSpread(effectiveDateTime, maturityDateTime,
            paymentTenorIbor, // period and payment dates are generated from these
            notional, parseFlatSpread(iborLeg), indexIbor,
            StubType.SHORT_START, // TODO stub compounding period
            isPay, floatLegAccrualPeriodBusinessDayConvention,
            floatIsEOM, floatResetCalendar, // period and payment dates are generated from these
            stubType, rollDateAdjuster); // TODO stub periods
      } else if (iborLeg.getConvention().getCompoundingMethod() == CompoundingMethod.SPREAD_EXCLUSIVE) {
        secondLeg = AnnuityDefinitionBuilder.couponIborCompoundingSpread(effectiveDateTime, maturityDateTime,
            paymentTenorIbor, notional, parseFlatSpread(iborLeg), indexIbor,
            StubType.SHORT_START, // TODO stub compounding period
            isPay, floatLegAccrualPeriodBusinessDayConvention, floatIsEOM, floatResetCalendar, stubType, rollDateAdjuster);
      } else if (iborLeg.getConvention().getCompoundingMethod() == CompoundingMethod.STRAIGHT) {
        secondLeg = AnnuityDefinitionBuilder.couponIborCompounding(effectiveDateTime, maturityDateTime,
            paymentTenorIbor, // period and payment dates are generated from these
            notional, indexIbor, StubType.SHORT_START, // TODO stub compounding period
            isPay, floatLegAccrualPeriodBusinessDayConvention, floatIsEOM,
            floatResetCalendar, // period and payment dates are generated from these
            stubType, rollDateAdjuster); // TODO stub compounding period
      } else {
        throw new OpenGammaRuntimeException("Unsupported compounding method for fixed leg: " + iborLeg.getConvention().getCompoundingMethod());
      }
    }
    return secondLeg;
  }

  private AnnuityDefinition<? extends PaymentDefinition> generateFixedAnnuity(InterestRateSwapSecurity swapSecurity,
                                                                              FixedInterestRateSwapLeg fixedLeg) {
    AnnuityDefinition<? extends PaymentDefinition> firstLeg = null;

    final Frequency periodFreqFixed = fixedLeg.getConvention().getCalculationFrequency();
    final Currency currency = fixedLeg.getNotional().getCurrency();
    final boolean isPay = fixedLeg.getPayReceiveType() == PayReceiveType.PAY;
    final double signFixed = (isPay ? -1.0 : 1.0);
    int nbNotional = getNbNotionalPayments(swapSecurity);
    final boolean fixedIsEOM = RollConvention.EOM == fixedLeg.getConvention().getRollConvention();
    DayCount fixedLegDayCount = fixedLeg.getConvention().getDayCountConvention();
    Collection<ExternalId> fixedLegPaymentCalendarIds = fixedLeg.getConvention().getPaymentCalendars();
    Calendar fixedLegPaymentCalendar = new HolidaySourceCalendarAdapter(_holidaySource, fixedLegPaymentCalendarIds.toArray(new ExternalId[fixedLegPaymentCalendarIds.size()]));
    BusinessDayConvention fixedLegFixingBusinessDayConvention = fixedLeg.getConvention().getCalculationBusinessDayConvention();
    ZonedDateTime effectiveDateTime = getZonedDateTime(swapSecurity.getEffectiveDate());
    ZonedDateTime maturityDateTime = getZonedDateTime(swapSecurity.getUnadjustedMaturityDate());

    if (Frequency.NEVER_NAME.equals(periodFreqFixed.getName())) {
      firstLeg = generateZCFixedAnnuity(swapSecurity, currency, signFixed, nbNotional, fixedLeg.getNotional().getInitialAmount(),
                                        fixedLegPaymentCalendar, effectiveDateTime, maturityDateTime);
    } else {
      if (fixedLeg.getConvention().getCompoundingMethod() == CompoundingMethod.NONE) {
        RollDateAdjuster rollDateAdjuster = getRollDateAdjuster(fixedLeg.getConvention().getRollConvention());
        final StubType stub = fixedLeg.getStubCalculationMethod() != null ? fixedLeg.getStubCalculationMethod().getType() : StubType.SHORT_START;
        firstLeg = AnnuityDefinitionBuilder.couponFixed(currency, effectiveDateTime, maturityDateTime,
            getTenor(periodFreqFixed), // period and payment dates are generated from these
            fixedLegPaymentCalendar, // period and payment dates are generated from these
            fixedLegDayCount, fixedLegFixingBusinessDayConvention, fixedIsEOM, getNotionalProvider(fixedLeg.getNotional()),
            fixedLeg.getRate().getInitialRate(), isPay,
            stub,
            fixedLeg.getConvention().getPaymentLag(), rollDateAdjuster);
      } else {
        throw new OpenGammaRuntimeException("Unsupported compounding method for fixed leg: " + fixedLeg.getConvention().getCompoundingMethod());
      }
    }
    return firstLeg;
  }

  private RollDateAdjuster getRollDateAdjuster(RollConvention rollConvention) {
    if (rollConvention == RollConvention.NONE) {
      return null; // return null so ScheduleCalculator code can detect it should use default EOM code if applicable
    }
    return rollConvention.getRollDateAdjuster(0); // Period adjustment still handled in ScheduleCalculator
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

  private SwapDefinition getFixedOISSwapDefinition(final InterestRateSwapSecurity swapSecurity) {
    final LocalDate effectiveDate = swapSecurity.getEffectiveDate();
    final LocalDate maturityDate = swapSecurity.getUnadjustedMaturityDate();
    ZonedDateTime effectiveDateTime = getZonedDateTime(effectiveDate);
    ZonedDateTime maturityDateTime = getZonedDateTime(maturityDate);

    final FixedInterestRateSwapLeg fixedLeg = Iterables.getOnlyElement(swapSecurity.getLegs(FixedInterestRateSwapLeg.class));
    final FloatingInterestRateSwapLeg floatLeg = Iterables.getOnlyElement(swapSecurity.getLegs(FloatingInterestRateSwapLeg.class));

    final Currency currency = floatLeg.getNotional().getCurrency();
    final boolean isEOM = RollConvention.EOM == fixedLeg.getConvention().getRollConvention();
    final int publicationLag = PeriodRelationship.BEGINNING == floatLeg.getConvention().getResetRelativeTo() ? 0 : 1; // end of period == 1 day
    final IndexON index = new IndexON(floatLeg.getConvention().getName(), currency, floatLeg.getConvention().getDayCountConvention(), publicationLag);
    final ExternalId[] floatFixingCalendarIds = floatLeg.getConvention().getFixingCalendars().toArray(new ExternalId[floatLeg.getConvention().getFixingCalendars().size()]);
    final Calendar floatFixingCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatFixingCalendarIds);
    Period paymentTenor = PeriodFrequency.convertToPeriodFrequency(floatLeg.getConvention().getPaymentFrequency()).getPeriod();
    // If calc period == Term (aka 0D) - replace with length of swap  -- possible this should be 1Y for compounding OIS
    if (Period.ZERO.equals(paymentTenor)) {
      paymentTenor = Period.between(swapSecurity.getEffectiveDate(), swapSecurity.getUnadjustedMaturityDate());
    }
    final GeneratorSwapFixedON generator = new GeneratorSwapFixedON(currency.getCode() + "_OIS_Convention", index, paymentTenor,
        fixedLeg.getConvention().getDayCountConvention(), floatLeg.getConvention().getFixingBusinessDayConvention(),
        isEOM, floatLeg.getConvention().getSettlementDays(), publicationLag, floatFixingCalendar);
    final NotionalProvider notionalFixed = getNotionalProvider(fixedLeg.getNotional());
    final NotionalProvider notionalOIS = getNotionalProvider(floatLeg.getNotional());
    return SwapFixedONDefinition.from(effectiveDateTime, maturityDateTime, notionalFixed, notionalOIS,
                                      generator, fixedLeg.getRate().getInitialRate(), fixedLeg.getPayReceiveType() == PayReceiveType.PAY);
  }

  private SwapDefinition getIborOISSwapDefinition(final InterestRateSwapSecurity swapSecurity) {
    final LocalDate effectiveDate = swapSecurity.getEffectiveDate();
    final LocalDate maturityDate = swapSecurity.getUnadjustedMaturityDate();
    ZonedDateTime effectiveDateTime = getZonedDateTime(effectiveDate);
    ZonedDateTime maturityDateTime = getZonedDateTime(maturityDate);

    FloatingInterestRateSwapLeg iborLeg = null;
    FloatingInterestRateSwapLeg oisLeg = null;
    for (FloatingInterestRateSwapLeg leg : swapSecurity.getLegs(FloatingInterestRateSwapLeg.class)) {
      if (leg.getConvention().getRateType().isIbor()) {
        iborLeg = leg;
      } else if (leg.getConvention().getRateType().isOis()) {
        oisLeg = leg;
      } else {
        throw new OpenGammaRuntimeException("Unexpected leg type in IBOR_OIS swap: " + leg);
      }
    }
    ArgumentChecker.notNull(iborLeg, "ibor leg in IBOR_OIS swap");
    ArgumentChecker.notNull(oisLeg, "ois leg in IBOR_OIS swap");

    // Ibor
    final boolean floatIsEOM = iborLeg.getConvention().getRollConvention() == RollConvention.EOM;
    final boolean isPay = iborLeg.getPayReceiveType() == PayReceiveType.PAY;
    final int spotLag = iborLeg.getConvention().getSettlementDays();
    Frequency resetFreqIbor = iborLeg.getConvention().getResetFrequency();
    Period resetTenorIbor = getTenor(resetFreqIbor);
    DayCount floatDayCount = iborLeg.getConvention().getDayCountConvention();
    final Currency currency = iborLeg.getNotional().getCurrency();
    final IborIndex indexIbor = new IborIndex(currency, resetTenorIbor, spotLag, floatDayCount,
                                              iborLeg.getConvention().getPaymentDayConvention(),
                                              floatIsEOM, iborLeg.getFloatingReferenceRateId().getValue());
    // OIS
    final boolean isEOM = RollConvention.EOM == iborLeg.getConvention().getRollConvention();
    final int publicationLag = PeriodRelationship.BEGINNING == oisLeg.getConvention().getResetRelativeTo() ? 0 : 1; // end of period == 1 day
    final IndexON indexON = new IndexON(oisLeg.getConvention().getName(), currency, oisLeg.getConvention().getDayCountConvention(), publicationLag);
    final ExternalId[] oisFixingCalendarIds = oisLeg.getConvention().getFixingCalendars().toArray(new ExternalId[oisLeg.getConvention().getFixingCalendars().size()]);
    final Calendar oisFixingCalendar = new HolidaySourceCalendarAdapter(_holidaySource, oisFixingCalendarIds);
    final ExternalId[] iborFixingCalendarIds = iborLeg.getConvention().getFixingCalendars().toArray(new ExternalId[iborLeg.getConvention().getFixingCalendars().size()]);
    final Calendar iborFixingCalendar = new HolidaySourceCalendarAdapter(_holidaySource, iborFixingCalendarIds);
    final GeneratorSwapIborON generator = new GeneratorSwapIborON(currency.getCode() + "_OIS_Convention", indexIbor, indexON,
      oisLeg.getConvention().getFixingBusinessDayConvention(), isEOM, spotLag, publicationLag, iborFixingCalendar, oisFixingCalendar);
    final NotionalProvider notionalIbor = getNotionalProvider(iborLeg.getNotional());
    final NotionalProvider notionalOIS = getNotionalProvider(oisLeg.getNotional());
    return SwapIborONDefinition.from(effectiveDateTime, maturityDateTime, notionalIbor, notionalOIS, generator,
                                     parseFlatSpread(iborLeg), isPay);
  }

  private ZonedDateTime getZonedDateTime(LocalDate maturityDate) {
    //Impl Note: If using a time during the day we can get -0.0 fixing times
    return maturityDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());
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

  //TODO: Would be nice to make this support Notional
  private static NotionalProvider getNotionalProvider(final InterestRateSwapNotional notional) {
    final InterestRateSwapNotionalVisitor<LocalDate,  Double> visitor = new InterestRateSwapNotionalAmountVisitor();
    return new NotionalProvider() {
      @Override
      public double getAmount(LocalDate date) {
        return notional.accept(visitor, date);
      }
    };
  }
}

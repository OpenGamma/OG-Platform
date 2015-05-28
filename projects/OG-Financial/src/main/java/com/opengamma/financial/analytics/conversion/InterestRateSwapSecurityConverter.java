/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.payment.*;
import com.opengamma.analytics.financial.instrument.swap.*;
import com.opengamma.financial.convention.*;
import com.opengamma.financial.security.swap.*;
import com.opengamma.util.money.Currency;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AbstractAnnuityDefinitionBuilder.CouponStub;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapNotionalVisitor;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.lookup.irs.InterestRateSwapNotionalAmountVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

/**
 *
 */
public class InterestRateSwapSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** A holiday source */
  private final HolidaySource _holidaySource;
  /** A convention source */
  private final ConventionSource _conventionSource;
  /** A security source */
  private final SecuritySource _securitySource;

  /**
   * @param holidaySource The holiday source, not <code>null</code>
   * @param conventionSource The convention source, not <code>null</code>
   * @param securitySource The security source, not <code>null</code>
   */
  public InterestRateSwapSecurityConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _securitySource = securitySource;
  }

  @Override
  public InstrumentDefinition<?> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    return visitSwapSecurity(security);
  }

  @Override
  public InstrumentDefinition<?> visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    ArgumentChecker.notNull(security, "swap security");
    if (security.getLegs().get(0).getNotional().getCurrency().equals(Currency.BRL)) {
      return getBrazilianSwapDefinition(security);
    }
    LocalDate startDate = security.getEffectiveDate();
    LocalDate endDate = security.getUnadjustedMaturityDate();
    AnnuityDefinition<?> payLeg = getAnnuityDefinition(true, startDate, endDate, security.getNotionalExchange(), security.getPayLeg());
    AnnuityDefinition<?> receiveLeg = getAnnuityDefinition(false, startDate, endDate, security.getNotionalExchange(), security.getReceiveLeg());
    return getDefinition(security, payLeg, receiveLeg);
  }

  private class FixedFloatSwapHelper {
    AnnuityCouponFixedDefinition fixedLegAnnuity;
    FloatingRateType floatingRateType;
    AnnuityDefinition<?> floatingLegAnnuity;
  }
 
  private boolean checkPaymentDefinitionType(AnnuityDefinition<?> floatingLeg) {
    if (floatingLeg.getPayments() instanceof CouponIborDefinition[] ||
        floatingLeg.getPayments() instanceof CouponONDefinition[]) {
      return true;
    } else {
      return false;
    }
  }
  
  private FixedFloatSwapHelper getFixedFloatLeg(final InterestRateSwapSecurity swap, final AnnuityDefinition<?> payLeg, 
      final AnnuityDefinition<?> receiveLeg) {
    FixedFloatSwapHelper fixedFloatSwap = new FixedFloatSwapHelper();
    final boolean payLegFixed = isLegFixed(swap.getPayLeg(), payLeg);
    final boolean receiveLegFixed = isLegFixed(swap.getReceiveLeg(), receiveLeg);

    InterestRateSwapLeg swapSecurityFloatLeg = null;

    if (payLegFixed && !receiveLegFixed) {
      fixedFloatSwap.fixedLegAnnuity = getFixedLegAnnuity(payLeg);
      fixedFloatSwap.floatingLegAnnuity = receiveLeg;
      swapSecurityFloatLeg = swap.getReceiveLeg();
    } else if (!payLegFixed && receiveLegFixed) {
      fixedFloatSwap.fixedLegAnnuity = getFixedLegAnnuity(receiveLeg);
      fixedFloatSwap.floatingLegAnnuity = payLeg;
      swapSecurityFloatLeg = swap.getPayLeg();
    } else {
      return null;
    }
    
    if (checkPaymentDefinitionType(fixedFloatSwap.floatingLegAnnuity)) {
      fixedFloatSwap.floatingRateType = ((FloatingInterestRateSwapLeg) swapSecurityFloatLeg).getFloatingRateType();
    }
    return fixedFloatSwap;
  }
  
  private SwapDefinition getDefinition(final InterestRateSwapSecurity swap, final AnnuityDefinition<?> payLeg, 
      final AnnuityDefinition<?> receiveLeg) {
    FixedFloatSwapHelper fixedFloatSwap = getFixedFloatLeg(swap, payLeg, receiveLeg);
    
    if (fixedFloatSwap != null) {
      if (fixedFloatSwap.floatingRateType == null) {
        return new SwapCouponFixedCouponDefinition(fixedFloatSwap.fixedLegAnnuity, (AnnuityDefinition<? extends CouponDefinition>) fixedFloatSwap.floatingLegAnnuity);
      } else if (fixedFloatSwap.floatingRateType.isIbor()) {
        return new SwapFixedIborDefinition(fixedFloatSwap.fixedLegAnnuity, getIborLegAnnuity(fixedFloatSwap.floatingLegAnnuity));
      } else if (fixedFloatSwap.floatingRateType.isOis()) {
        return new SwapFixedONDefinition(fixedFloatSwap.fixedLegAnnuity, getONLegAnnuity(fixedFloatSwap.floatingLegAnnuity));
      }
    } 
    return new SwapDefinition(payLeg, receiveLeg);
  }

  private boolean isLegFixed(final InterestRateSwapLeg leg, final AnnuityDefinition<?> annuity) {
    return leg instanceof FixedInterestRateSwapLeg && annuity.getPayments() instanceof CouponFixedDefinition[];
  }

  private AnnuityCouponFixedDefinition getFixedLegAnnuity(final AnnuityDefinition<?> leg) {
    return new AnnuityCouponFixedDefinition((CouponFixedDefinition[]) leg.getPayments(), leg.getCalendar());
  }

  private AnnuityCouponIborDefinition getIborLegAnnuity(final AnnuityDefinition<?> leg) {
    return new AnnuityCouponIborDefinition((CouponIborDefinition[]) leg.getPayments(), ((CouponIborDefinition) leg.getNthPayment(0)).getIndex(), leg.getCalendar());
  }

  private AnnuityCouponONDefinition getONLegAnnuity(final AnnuityDefinition<?> leg) {
    return new AnnuityCouponONDefinition((CouponONDefinition[]) leg.getPayments(), 
        ((CouponONDefinition) leg.getNthPayment(0)).getIndex(), leg.getCalendar());
  }

  private AnnuityDefinition<?> getAnnuityDefinition(boolean payer, LocalDate startDate, LocalDate endDate, NotionalExchange notionalExchange, InterestRateSwapLeg leg) {
    if (leg instanceof FixedInterestRateSwapLeg) {
      return buildFixedAnnuityDefinition(payer, startDate, endDate, notionalExchange, leg);
    } else if (leg instanceof FloatingInterestRateSwapLeg) {
      return buildFloatingAnnuityDefinition(payer, startDate, endDate, notionalExchange, leg);
    } else {
      throw new OpenGammaRuntimeException("Unsupported leg type");
    }
  }

  private AnnuityDefinition<?> buildFloatingAnnuityDefinition(boolean payer, LocalDate startDate, LocalDate endDate, NotionalExchange notionalExchange, InterestRateSwapLeg leg) {
    FloatingInterestRateSwapLeg floatLeg = (FloatingInterestRateSwapLeg) leg;

    AdjustedDateParameters maturityDateParameters = null;
    if (leg.getMaturityDateCalendars() != null && leg.getMaturityDateBusinessDayConvention() != null) {
      Calendar maturityDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getMaturityDateCalendars().toArray(new ExternalId[leg.getMaturityDateCalendars().size()]));
      maturityDateParameters = new AdjustedDateParameters(maturityDateCalendar, leg.getMaturityDateBusinessDayConvention());
    }

    AdjustedDateParameters accrualPeriodParameters = null;
    if (leg.getAccrualPeriodCalendars() != null && leg.getAccrualPeriodBusinessDayConvention() != null) {
      Calendar accrualPeriodCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()]));
      accrualPeriodParameters = new AdjustedDateParameters(accrualPeriodCalendar, leg.getAccrualPeriodBusinessDayConvention());
    }
    final RollDateAdjuster rollDateAdjuster = getRollDateAdjuster(leg.getRollConvention());

    AdjustedDateParameters resetDateParameters = null;
    if (floatLeg.getResetPeriodCalendars() != null && floatLeg.getResetPeriodBusinessDayConvention() != null) {
      Calendar resetCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatLeg.getResetPeriodCalendars().toArray(new ExternalId[floatLeg.getResetPeriodCalendars().size()]));
      resetDateParameters = new AdjustedDateParameters(resetCalendar, floatLeg.getResetPeriodBusinessDayConvention());
    }

    double spread = Double.NaN;
    if (floatLeg.getSpreadSchedule() != null) {
      spread = floatLeg.getSpreadSchedule().getInitialRate();
    }

    final int paymentOffset = floatLeg.getPaymentOffset();

    CompoundingMethod compoundingMethod;

    final IndexDeposit index;
    if (FloatingRateType.IBOR == floatLeg.getFloatingRateType()) {
      index = getIborIndex(floatLeg);

      /* The compounding method is only relevant for Ibor */
      switch (floatLeg.getCompoundingMethod()) {
        case FLAT:
          compoundingMethod = CompoundingMethod.FLAT;
          break;
        case STRAIGHT:
          compoundingMethod = CompoundingMethod.STRAIGHT;
          break;
        case SPREAD_EXCLUSIVE:
          compoundingMethod = CompoundingMethod.SPREAD_EXCLUSIVE;
          break;
        case NONE:
          compoundingMethod = null;
          break;
        default:
          throw new OpenGammaRuntimeException("Unsupported compounding method");
      }

    } else if (FloatingRateType.OIS == floatLeg.getFloatingRateType()) {
      index = getONIndex(floatLeg);
      // TODO PLAT-6729 compounding is incorrectly set here to distinguish between ON Arithmetic Average
      // when used in the FloatingAnnuityDefinitionBuilder
      compoundingMethod = CompoundingMethod.FLAT;

    } else if (FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE == floatLeg.getFloatingRateType()) {
      index = getONIndex(floatLeg);
      // TODO PLAT-6729 compounding is incorrectly set here to distinguish between OIS
      // when used in the FloatingAnnuityDefinitionBuilder
      compoundingMethod = null;

    } else {
      throw new OpenGammaRuntimeException("Unsupported floating rate type " + floatLeg.getFloatingRateType());
    }

    OffsetAdjustedDateParameters paymentDateParameters = null;
    if (leg.getPaymentDateCalendars() != null && leg.getPaymentDateBusinessDayConvention() != null) {
      Calendar paymentDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getPaymentDateCalendars().toArray(new ExternalId[leg.getPaymentDateCalendars().size()]));
      paymentDateParameters = new OffsetAdjustedDateParameters(paymentOffset, OffsetType.BUSINESS, paymentDateCalendar, leg.getPaymentDateBusinessDayConvention());
    }

    OffsetAdjustedDateParameters fixingDateParameters = null;
    if (floatLeg.getFixingDateCalendars() != null && floatLeg.getFixingDateBusinessDayConvention() != null) {
      Calendar fixingDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, floatLeg.getFixingDateCalendars().toArray(new ExternalId[floatLeg.getFixingDateCalendars().size()]));
      fixingDateParameters = new OffsetAdjustedDateParameters(floatLeg.getFixingDateOffset(), floatLeg.getFixingDateOffsetType(), fixingDateCalendar, floatLeg.getFixingDateBusinessDayConvention());
    }

    CouponStub startStub = null;
    CouponStub endStub = null;
    StubCalculationMethod stubCalcMethod = leg.getStubCalculationMethod();
    if (stubCalcMethod != null) {
      final Pair<CouponStub, CouponStub> stubs = parseStubs(stubCalcMethod);
      startStub = stubs.getFirst();
      endStub = stubs.getSecond();      
    }

    List<Double> notionalList = leg.getNotional().getNotionals();
    double[] notionalSchedule;
    if (notionalList.isEmpty()) {
      notionalSchedule = new double[] {(payer ? -1 : 1) * leg.getNotional().getInitialAmount() };
    } else {
      notionalSchedule = new double[notionalList.size()];
      for (int i = 0; i < notionalSchedule.length; i++) {
        notionalSchedule[i] = (payer ? -1 : 1) * notionalList.get(i);
      }
    }

    return new FloatingAnnuityDefinitionBuilder().
        payer(payer).
        currency(leg.getNotional().getCurrency()).
        notional(getNotionalProvider(leg.getNotional(), leg.getAccrualPeriodBusinessDayConvention(),
            new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()])))).
        startDate(startDate).
        endDate(endDate).
        endDateAdjustmentParameters(maturityDateParameters).
        startDateAdjustmentParameters(accrualPeriodParameters).
        dayCount(leg.getDayCountConvention()).
        rollDateAdjuster(rollDateAdjuster).
        exchangeInitialNotional(notionalExchange.isExchangeInitialNotional()).
        exchangeFinalNotional(notionalExchange.isExchangeFinalNotional()).
        accrualPeriodFrequency(getTenor(leg.getPaymentDateFrequency())).
        accrualPeriodParameters(accrualPeriodParameters).
        paymentDateRelativeTo(leg.getPaymentDateRelativeTo()).
        paymentDateAdjustmentParameters(paymentDateParameters).
        spread(spread).
        index(index).
        resetDateAdjustmentParameters(resetDateParameters).
        resetRelativeTo(floatLeg.getResetDateRelativeTo()).
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
            // added the reset frequency
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            resetPeriodFrequency(getTenor(((FloatingInterestRateSwapLeg) leg).getResetPeriodFrequency())).
        fixingDateAdjustmentParameters(fixingDateParameters).
        compoundingMethod(compoundingMethod).
        startStub(startStub).
        endStub(endStub).
        initialRate(floatLeg.getCustomRates() != null ? floatLeg.getCustomRates().getInitialRate() : Double.NaN).
        build();
  }

  private IndexDeposit getONIndex(FloatingInterestRateSwapLeg floatLeg) {
    // try security lookup
    final Security sec = _securitySource.getSingle(floatLeg.getFloatingReferenceRateId().toBundle());
    if (sec != null) {
      final OvernightIndex indexSecurity = (OvernightIndex) sec;
      OvernightIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), OvernightIndexConvention.class);
      return ConverterUtils.indexON(indexSecurity.getName(), indexConvention);
    }

    // Fallback to convention lookup for old behaviour
    Convention convention = _conventionSource.getSingle(floatLeg.getFloatingReferenceRateId());
    if (!(convention instanceof OvernightIndexConvention)) {
      throw new OpenGammaRuntimeException("Mis-match between floating rate type " + floatLeg.getFloatingRateType() + " and convention " + convention.getClass());
    }
    OvernightIndexConvention onIndexConvention = (OvernightIndexConvention) convention;
    return new IndexON(
        floatLeg.getFloatingReferenceRateId().getValue(),
        floatLeg.getNotional().getCurrency(),
        onIndexConvention.getDayCount(),
        onIndexConvention.getPublicationLag());
  }

  private IndexDeposit getIborIndex(FloatingInterestRateSwapLeg floatLeg) {
    // try security lookup
    final Security sec = _securitySource.getSingle(floatLeg.getFloatingReferenceRateId().toBundle());
    if (sec != null) {
      final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
      IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
      return ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    }

    // Fallback to convention lookup for old behaviour
    Convention iborLegConvention = _conventionSource.getSingle(floatLeg.getFloatingReferenceRateId());
    if (iborLegConvention == null) {
    }
    if (!(iborLegConvention instanceof VanillaIborLegConvention)) {
      throw new OpenGammaRuntimeException("Mis-match between floating rate type " + floatLeg.getFloatingRateType() + " and convention " + iborLegConvention.getClass());
    }
    Convention iborConvention = _conventionSource.getSingle(((VanillaIborLegConvention) iborLegConvention).getIborIndexConvention());
    if (iborConvention == null) {
      throw new OpenGammaRuntimeException("Convention not found for " + ((VanillaIborLegConvention) iborLegConvention).getIborIndexConvention());
    }
    IborIndexConvention iborIndexConvention = (IborIndexConvention) iborConvention;

    return new IborIndex(iborIndexConvention.getCurrency(),
        ((VanillaIborLegConvention) iborLegConvention).getResetTenor().getPeriod(),
        iborIndexConvention.getSettlementDays(),  // fixing lag
        iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(),
        ((IborIndexConvention) iborConvention).isIsEOM(),
        floatLeg.getFloatingReferenceRateId().getValue());
  }
  
  private IndexDeposit getIborIndex(ExternalId indexId) {
    // try security lookup
    final Security sec = _securitySource.getSingle(indexId.toBundle());
    if (sec != null) {
      final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
      IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
      return ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
    }
    
    // Fallback to convention lookup for old behaviour
    Convention iborLegConvention = _conventionSource.getSingle(indexId);
    if (!(iborLegConvention instanceof VanillaIborLegConvention)) {
      throw new OpenGammaRuntimeException("Could not resolve an index convention for rate reference id: " + indexId.getValue());
    }
    Convention iborConvention = _conventionSource.getSingle(((VanillaIborLegConvention) iborLegConvention).getIborIndexConvention());
    if (iborConvention == null) {
      throw new OpenGammaRuntimeException("Convention not found for " + ((VanillaIborLegConvention) iborLegConvention).getIborIndexConvention());
    }
    IborIndexConvention iborIndexConvention = (IborIndexConvention) iborConvention;

    return new IborIndex(iborIndexConvention.getCurrency(),
        ((VanillaIborLegConvention) iborLegConvention).getResetTenor().getPeriod(),
        iborIndexConvention.getSettlementDays(),  // fixing lag
        iborIndexConvention.getDayCount(),
        iborIndexConvention.getBusinessDayConvention(),
        ((IborIndexConvention) iborConvention).isIsEOM(),
        indexId.getValue());
  }

  private RollDateAdjuster getRollDateAdjuster(InterestRateSwapLeg leg) {
    //Impl note: We set months to adjust to 0 as the roll period is set by the specific period variables.
    //TODO: We ignore day of month and day of week adjustments, check this
    RollDateAdjuster rollDateAdjuster;
    switch (leg.getRollConvention()) {
      case EOM:
      case IMM:
      case IMM_AUD:
      case IMM_CAD:
      case IMM_NZD:
      case FRN:
      case SFE:
      case TBILL:
        rollDateAdjuster = leg.getRollConvention().getRollDateAdjuster(0);
        break;
      case NONE:
      default:
        rollDateAdjuster = RollConvention.NONE.getRollDateAdjuster(0);
    }
    return rollDateAdjuster;
  }

  private AnnuityDefinition<?> buildFixedAnnuityDefinition(boolean payer, LocalDate startDate, LocalDate endDate, NotionalExchange notionalExchange, InterestRateSwapLeg leg) {
    FixedInterestRateSwapLeg fixedLeg = (FixedInterestRateSwapLeg) leg;

    AdjustedDateParameters maturityDateParameters = null;
    if (leg.getMaturityDateCalendars() != null && leg.getMaturityDateBusinessDayConvention() != null) {
      Calendar maturityDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getMaturityDateCalendars().toArray(new ExternalId[leg.getMaturityDateCalendars().size()]));
      maturityDateParameters = new AdjustedDateParameters(maturityDateCalendar, leg.getMaturityDateBusinessDayConvention());
    }

    AdjustedDateParameters accrualPeriodParameters = null;
    if (leg.getAccrualPeriodCalendars() != null && leg.getAccrualPeriodBusinessDayConvention() != null) {
      Calendar accrualPeriodCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()]));
      accrualPeriodParameters = new AdjustedDateParameters(accrualPeriodCalendar, leg.getAccrualPeriodBusinessDayConvention());
    }

    OffsetAdjustedDateParameters paymentDateParameters = null;
    if (leg.getPaymentDateCalendars() != null && leg.getPaymentDateBusinessDayConvention() != null) {
      Calendar paymentDateCalendar = new HolidaySourceCalendarAdapter(_holidaySource, leg.getPaymentDateCalendars().toArray(new ExternalId[leg.getPaymentDateCalendars().size()]));
      paymentDateParameters = new OffsetAdjustedDateParameters(
          leg.getPaymentOffset(),
          OffsetType.BUSINESS,
          paymentDateCalendar,
          leg.getPaymentDateBusinessDayConvention());
    }

    final RollDateAdjuster rollDateAdjuster = getRollDateAdjuster(leg.getRollConvention());

    CouponStub startStub = null;
    CouponStub endStub = null;
    StubCalculationMethod stubCalcMethod = leg.getStubCalculationMethod();
    if (stubCalcMethod != null) {
      final Pair<CouponStub, CouponStub> stubs = parseStubs(stubCalcMethod);
      startStub = stubs.getFirst();
      endStub = stubs.getSecond();      
    }

    final List<Double> notionalList = leg.getNotional().getNotionals();
    double[] notionalSchedule;
    if (notionalList.isEmpty()) {
      notionalSchedule = new double[] {(payer ? -1 : 1) * leg.getNotional().getInitialAmount() };
    } else {
      notionalSchedule = new double[notionalList.size()];
      for (int i = 0; i < notionalSchedule.length; i++) {
        notionalSchedule[i] = (payer ? -1 : 1) * notionalList.get(i);
      }
    }

    CompoundingMethod compoundingMethod = leg.getCompoundingMethod();


    return new FixedAnnuityDefinitionBuilder().
        payer(payer).
        currency(leg.getNotional().getCurrency()).
        notional(getNotionalProvider(leg.getNotional(), leg.getAccrualPeriodBusinessDayConvention(),
            new HolidaySourceCalendarAdapter(_holidaySource, leg.getAccrualPeriodCalendars().toArray(new ExternalId[leg.getAccrualPeriodCalendars().size()])))).
        startDate(startDate).
        endDate(endDate).
        endDateAdjustmentParameters(maturityDateParameters).
        startDateAdjustmentParameters(accrualPeriodParameters).
        dayCount(leg.getDayCountConvention()).
        rollDateAdjuster(rollDateAdjuster).
        exchangeInitialNotional(notionalExchange.isExchangeInitialNotional()).
        exchangeFinalNotional(notionalExchange.isExchangeFinalNotional()).
        accrualPeriodFrequency(getTenor(leg.getPaymentDateFrequency())).
        accrualPeriodParameters(accrualPeriodParameters).
        paymentDateRelativeTo(leg.getPaymentDateRelativeTo()).
        paymentDateAdjustmentParameters(paymentDateParameters).
        rate(fixedLeg.getRate().getInitialRate()).
        startStub(startStub).
        endStub(endStub).
        compoundingMethod(compoundingMethod).
        build();
  }

  
  /**
   * Converts the StubCalculationMethod to CouponStubs.
   */
   /* Testing >> private to package protected*/
  Pair<CouponStub, CouponStub> parseStubs(final StubCalculationMethod stubCalcMethod) {
    CouponStub startStub = null;
    CouponStub endStub = null;
    
    if (stubCalcMethod != null) {
      stubCalcMethod.validate();
      StubType stubType = stubCalcMethod.getType();

      // first stub
      double firstStubRate = stubCalcMethod.hasFirstStubRate() ? stubCalcMethod.getFirstStubRate() : Double.NaN;
      LocalDate firstStubDate = stubCalcMethod.getFirstStubEndDate();      
      ExternalId firstStubStartReferenceRateId = stubCalcMethod.getFirstStubStartReferenceRateId();
      
      IborIndex firstStubStartIndex = null;
      if (stubCalcMethod.hasFirstStubStartReferenceRateId()) {
        firstStubStartIndex = (IborIndex) getIborIndex(firstStubStartReferenceRateId);
      }
      IborIndex firstStubEndIndex = null;
      ExternalId firstStubEndReferenceRateId = stubCalcMethod.getFirstStubEndReferenceRateId();
      if (stubCalcMethod.hasFirstStubEndReferenceRateId()) {
        firstStubEndIndex = (IborIndex) getIborIndex(firstStubEndReferenceRateId);
      }
      
      // last stub
      double finalStubRate = stubCalcMethod.hasLastStubRate() ? stubCalcMethod.getLastStubRate() : Double.NaN;
      LocalDate finalStubDate = stubCalcMethod.getLastStubEndDate();
      ExternalId lastStubStartReferenceRateId = stubCalcMethod.getLastStubStartReferenceRateId();
      IborIndex lastStubStartIndex = null;
      if (stubCalcMethod.hasLastStubStartReferenceRateId()) {
        lastStubStartIndex = (IborIndex) getIborIndex(lastStubStartReferenceRateId);  
      }      
      IborIndex lastStubEndIndex = null;
      ExternalId lastStubEndReferenceRateId = stubCalcMethod.getLastStubEndReferenceRateId();
      if (stubCalcMethod.hasLastStubEndReferenceRateId()) {
        lastStubEndIndex = (IborIndex) getIborIndex(lastStubEndReferenceRateId);  
      }      

      if (StubType.BOTH == stubType) {
        if (!Double.isNaN(firstStubRate)) {
          startStub = new CouponStub(stubType, firstStubDate, stubCalcMethod.getFirstStubRate());
        } else if (firstStubStartIndex != null && firstStubEndIndex != null) {
          startStub = new CouponStub(stubType, firstStubDate, firstStubStartIndex, firstStubEndIndex);
        } else {
          startStub = new CouponStub(stubType, firstStubDate);
        }

        if (!Double.isNaN(finalStubRate)) {
          endStub = new CouponStub(stubType, finalStubDate, stubCalcMethod.getLastStubRate());
        } else if (lastStubStartIndex != null && lastStubEndIndex != null) {
          endStub = new CouponStub(stubType, finalStubDate, lastStubStartIndex, lastStubEndIndex);
        } else {
          endStub = new CouponStub(stubType, finalStubDate);
        }

      } else if (StubType.LONG_START == stubType || StubType.SHORT_START == stubType) {
        if (!Double.isNaN(firstStubRate)) {
          startStub = new CouponStub(stubType, firstStubDate, firstStubRate);
        } else if (firstStubStartIndex != null && firstStubEndIndex != null) {
          startStub = new CouponStub(stubType, firstStubStartIndex, firstStubEndIndex);
        } else {
          startStub = new CouponStub(stubType);
        }
      } else if (StubType.LONG_END == stubType || StubType.SHORT_END == stubType) {
        if (!Double.isNaN(finalStubRate)) {
          endStub = new CouponStub(stubType, finalStubDate, finalStubRate);
        } else if (lastStubStartIndex != null && lastStubEndIndex != null) {
          endStub = new CouponStub(stubType, lastStubStartIndex, lastStubEndIndex);
        } else {
          endStub = new CouponStub(stubType);
        }
      } else if (stubType != null) {
        startStub = new CouponStub(stubType);
        endStub = new CouponStub(stubType);
      }      
    }
    return Pairs.of(startStub, endStub);
  }

  private static Period getTenor(final Frequency freq) {
    if (Frequency.NEVER_NAME.equals(freq.getName())) {
      return Period.ZERO;
    } else if (freq instanceof PeriodFrequency) {
      Period period = ((PeriodFrequency) freq).getPeriod();
      if (period.getYears() == 1) {
        return Period.ofMonths(12);
      }
      return period;
    } else if (freq instanceof SimpleFrequency) {
      Period period = ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
      if (period.getYears() == 1) {
        return Period.ofMonths(12);
      }
      return period;
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

  //TODO: Would be nice to make this support Notional
  private static NotionalProvider getNotionalProvider(InterestRateSwapNotional notional, BusinessDayConvention convention, Calendar calendar) {
    final InterestRateSwapNotionalVisitor<LocalDate, Double> visitor = new InterestRateSwapNotionalAmountVisitor();
    final List<LocalDate> dates = notional.getDates();
    if (!dates.isEmpty()) {
      for (int i = 0; i < dates.size(); i++) {
        LocalDate date = dates.remove(i);
        date = convention.adjustDate(calendar, date);
        dates.add(i, date);
      }
      notional = InterestRateSwapNotional.of(notional.getCurrency(), dates, notional.getNotionals(), notional.getShiftTypes());
    }
    final InterestRateSwapNotional adjustednotional = notional;
    return new NotionalProvider() {
      @Override
      public double getAmount(LocalDate date) {
        return adjustednotional.accept(visitor, date);
      }
    };
  }

  private RollDateAdjuster getRollDateAdjuster(final RollConvention rollConvention) {
    //Impl note: We set months to adjust to 0 as the roll period is set by the specific period variables.
    //TODO: We ignore day of month and day of week adjustments, check this
    RollDateAdjuster rollDateAdjuster;
    switch (rollConvention) {
      case EOM:
      case IMM:
      case IMM_AUD:
      case IMM_CAD:
      case IMM_NZD:
      case FRN:
      case SFE:
      case TBILL:
        rollDateAdjuster = rollConvention.getRollDateAdjuster(0);
        break;
      case NONE:
      default:
        rollDateAdjuster = RollConvention.NONE.getRollDateAdjuster(0);
    }
    return rollDateAdjuster;
  }

  private CouponDefinition createNotionalExchangeCoupon(Currency currency, ZonedDateTime date, double notional) {
    return new CouponFixedDefinition(currency, date, date, date, 1.0, notional, 1.0);
  }

  private SwapDefinition getBrazilianSwapDefinition(final InterestRateSwapSecurity swapSecurity) {

    final LocalDate 					effectiveDate	= swapSecurity.getEffectiveDate();
    final LocalDate 					maturityDate 	= swapSecurity.getUnadjustedMaturityDate();
    final InterestRateSwapLeg 			payLeg 			= swapSecurity.getPayLeg();
    final InterestRateSwapLeg 			receiveLeg 		= swapSecurity.getReceiveLeg();
    final boolean 						payFixed		= payLeg instanceof FixedInterestRateSwapLeg;
    final FixedInterestRateSwapLeg 		fixedLeg 		= (FixedInterestRateSwapLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateSwapLeg	floatLeg 		= (FloatingInterestRateSwapLeg) (payFixed ? receiveLeg : payLeg);

    final IndexDeposit 	index 		= getONIndex(floatLeg);
    final Calendar 		calendar 	= new HolidaySourceCalendarAdapter(_holidaySource, floatLeg.getAccrualPeriodCalendars().toArray(new ExternalId[floatLeg.getAccrualPeriodCalendars().size()]));
    final double 		notional 	= ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
    final ZonedDateTime effective	= effectiveDate.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturity 	= maturityDate.atStartOfDay(ZoneOffset.UTC);
    final double 		fixedRate 	= fixedLeg.getRate().getInitialRate();


    final GeneratorSwapFixedCompoundedONCompounded generator = new GeneratorSwapFixedCompoundedONCompounded(
        index.getName(),
        (IndexON)index,
        fixedLeg.getDayCountConvention(),
        fixedLeg.getAccrualPeriodBusinessDayConvention(),
        false,
        0, // ??
        0, // ??
        calendar);

    List<CouponDefinition> fixedCoupons = new ArrayList<>();
    List<CouponDefinition> floatCoupons = new ArrayList<>();

    NotionalExchange notionalExchange  = swapSecurity.getNotionalExchange();

    ZonedDateTime notionalExchangeDate = notionalExchange.isExchangeInitialNotional() ? effective : (notionalExchange.isExchangeFinalNotional() ? maturity : null);

    if (notionalExchangeDate != null) {
      fixedCoupons.add(createNotionalExchangeCoupon(generator.getIndex().getCurrency(), notionalExchangeDate, (payFixed ? -1.0 : 1.0) * notional));
      floatCoupons.add(createNotionalExchangeCoupon(generator.getIndex().getCurrency(), notionalExchangeDate, (payFixed ? 1.0 : -1.0) * notional));
    }

    final CouponONCompoundedDefinition couponON = CouponONCompoundedDefinition.from(
        generator,
        effective,
        maturity,
        (payFixed ? 1.0 : -1.0) * notional);

    final CouponFixedAccruedCompoundingDefinition couponFixed = new CouponFixedAccruedCompoundingDefinition(
        couponON.getCurrency(),
        couponON.getPaymentDate(),
        couponON.getAccrualStartDate(),
        couponON.getAccrualEndDate(),
        couponON.getPaymentYearFraction(),
        (payFixed ? -1.0 : 1.0) * notional,
        fixedRate,
        calendar);

    fixedCoupons.add(couponFixed);

    floatCoupons.add(couponON);

    return new SwapDefinition(
        new AnnuityDefinition<>(fixedCoupons.toArray(new CouponDefinition[fixedCoupons.size()]), calendar),
        new AnnuityDefinition<>(floatCoupons.toArray(new CouponDefinition[floatCoupons.size()]), calendar)
    );
  }
}

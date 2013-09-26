/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class SwaptionUtils {

  public static GeneratorInstrument<GeneratorAttributeIR> getSwapGenerator(final SwaptionSecurity security, final InstrumentDefinition<?> swaption, final SecuritySource securitySource) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(securitySource, "security source");
    SwapDefinition swap;
    if (swaption instanceof SwaptionPhysicalFixedIborDefinition) {
      swap = ((SwaptionPhysicalFixedIborDefinition) swaption).getUnderlyingSwap();
    } else if (swaption instanceof SwaptionCashFixedIborDefinition) {
      swap = ((SwaptionCashFixedIborDefinition) swaption).getUnderlyingSwap();
    } else if (swaption instanceof SwaptionPhysicalFixedCompoundedONCompoundedDefinition) {
      swap = ((SwaptionPhysicalFixedCompoundedONCompoundedDefinition) swaption).getUnderlyingSwap();
    } else if (swaption instanceof SwaptionCashFixedCompoundedONCompoundingDefinition) {
      swap = ((SwaptionCashFixedCompoundedONCompoundingDefinition) swaption).getUnderlyingSwap();
    } else {
      throw new OpenGammaRuntimeException("Can only handle cash- and physically-settled ibor swaptions");
    }
    final SwapSecurity underlyingSecurity = (SwapSecurity) securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    FixedInterestRateLeg fixedLeg;
    FloatingInterestRateLeg floatLeg;
    if (underlyingSecurity.getPayLeg() instanceof FixedInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) underlyingSecurity.getPayLeg();
      floatLeg = (FloatingInterestRateLeg) underlyingSecurity.getReceiveLeg();
    } else {
      fixedLeg = (FixedInterestRateLeg) underlyingSecurity.getReceiveLeg();
      floatLeg = (FloatingInterestRateLeg) underlyingSecurity.getPayLeg();
    }
    switch (floatLeg.getFloatingRateType()) {
      case IBOR: {
        AnnuityCouponIborDefinition iborLeg;
        if (swap.getFirstLeg() instanceof AnnuityCouponIborDefinition) {
          iborLeg = (AnnuityCouponIborDefinition) swap.getFirstLeg();
        } else if (swap.getSecondLeg() instanceof AnnuityCouponIborDefinition) {
          iborLeg = (AnnuityCouponIborDefinition) swap.getSecondLeg();
        } else {
          throw new OpenGammaRuntimeException("Could not find ibor leg for " + underlyingSecurity);
        }
        final IborIndex iborIndex = iborLeg.getIborIndex();
        final Calendar calendar = iborLeg.getIborCalendar();
        final DayCount fixedLegDayCount = fixedLeg.getDayCount();
        final Frequency frequency = fixedLeg.getFrequency();
        final Period fixedLegPeriod;
        if (frequency instanceof PeriodFrequency) {
          fixedLegPeriod = ((PeriodFrequency) frequency).getPeriod();
        } else if (frequency instanceof SimpleFrequency) {
          fixedLegPeriod = ((SimpleFrequency) frequency).toPeriodFrequency().getPeriod();
        } else {
          throw new OpenGammaRuntimeException("Can only handle PeriodFrequency or SimpleFrequency");
        }
        return new GeneratorSwapFixedIbor("Swap Generator", fixedLegPeriod, fixedLegDayCount, iborIndex, calendar);
      }
      case OIS: {
        IndexON onIndex;
        Calendar calendar;
        if (swap.getFirstLeg() instanceof AnnuityCouponONDefinition) {
          final AnnuityCouponONDefinition annuityCouponONDefinition = (AnnuityCouponONDefinition) swap.getFirstLeg();
          onIndex = annuityCouponONDefinition.getOvernightIndex();
          calendar = annuityCouponONDefinition.getCalendar();
        } else if (swap.getSecondLeg() instanceof AnnuityCouponONDefinition) {
          final AnnuityCouponONDefinition annuityCouponONDefinition = (AnnuityCouponONDefinition) swap.getSecondLeg();
          onIndex = annuityCouponONDefinition.getOvernightIndex();
          calendar = annuityCouponONDefinition.getCalendar();
        } else if (swap.getFirstLeg().getNthPayment(0) instanceof CouponONCompoundedDefinition) {
          final CouponONCompoundedDefinition couponONDefinition = (CouponONCompoundedDefinition) swap.getFirstLeg().getNthPayment(0);
          onIndex = couponONDefinition.getIndex();
          calendar = couponONDefinition.getCalendar();
        } else if (swap.getSecondLeg().getNthPayment(0) instanceof CouponONCompoundedDefinition) {
          final CouponONCompoundedDefinition couponONDefinition = (CouponONCompoundedDefinition) swap.getSecondLeg().getNthPayment(0);
          onIndex = couponONDefinition.getIndex();
          calendar = couponONDefinition.getCalendar();
        } else {
          throw new OpenGammaRuntimeException("Could not find overnight leg for " + underlyingSecurity);
        }
        final DayCount fixedLegDayCount = fixedLeg.getDayCount();
        final Frequency frequency = fixedLeg.getFrequency();
        final Period fixedLegPeriod;
        if (frequency instanceof PeriodFrequency) {
          fixedLegPeriod = ((PeriodFrequency) frequency).getPeriod();
        } else if (frequency instanceof SimpleFrequency) {
          fixedLegPeriod = ((SimpleFrequency) frequency).toPeriodFrequency().getPeriod();
        } else {
          throw new OpenGammaRuntimeException("Can only handle PeriodFrequency or SimpleFrequency");
        }
        final BusinessDayConvention businessDayConvention = fixedLeg.getBusinessDayConvention();
        final boolean isEOM = fixedLeg.isEom();
        final int spotLag = 0; //TODO
        if (FinancialSecurityUtils.getCurrency(underlyingSecurity).getCode().equals("BRL")) {
          return new GeneratorSwapFixedCompoundedONCompounded("Swap Generator", onIndex, fixedLegDayCount, businessDayConvention, isEOM, spotLag, calendar);
        }
        return new GeneratorSwapFixedON("Swap Generator", onIndex, fixedLegPeriod, fixedLegDayCount, businessDayConvention, isEOM, spotLag, calendar);
      }
      default:
        throw new OpenGammaRuntimeException("Cannot handle floating leg type " + floatLeg.getFloatingRateType());
    }
  }
}

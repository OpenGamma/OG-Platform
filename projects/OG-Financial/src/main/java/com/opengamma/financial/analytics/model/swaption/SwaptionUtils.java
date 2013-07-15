/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SwaptionUtils {

  public static GeneratorSwapFixedIbor getSwapGenerator(final SwaptionSecurity security, final InstrumentDefinition<?> swaption, final SecuritySource securitySource) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(securitySource, "security source");
    SwapFixedIborDefinition swap;
    if (swaption instanceof SwaptionPhysicalFixedIborDefinition) {
      swap = ((SwaptionPhysicalFixedIborDefinition) swaption).getUnderlyingSwap();
    } else if (swaption instanceof SwaptionCashFixedIborDefinition) {
      swap = ((SwaptionCashFixedIborDefinition) swaption).getUnderlyingSwap();
    } else {
      throw new OpenGammaRuntimeException("Can only handle cash- and physically-settled ibor swaptions");
    }
    final SwapSecurity underlyingSecurity = (SwapSecurity) securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    FixedInterestRateLeg fixedLeg;
    if (underlyingSecurity.getPayLeg() instanceof FixedInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) underlyingSecurity.getPayLeg();
    } else {
      fixedLeg = (FixedInterestRateLeg) underlyingSecurity.getReceiveLeg();
    }
    final IborIndex iborIndex = swap.getIborLeg().getIborIndex();
    final Calendar calendar = swap.getIborLeg().getIborCalendar();
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
}

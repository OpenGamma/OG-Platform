/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for security conversion.
 */
public class ConversionUtils {

  /**
   * Converts a frequency to a period. If the frequency is called {@link Frequency#NEVER_NAME},
   * a period of length zero is returned. If the underlying period is of one year, returns
   * a period of twelve months.
   * @param freq The frequency, not null
   * @return The period
   * @throws OpenGammaRuntimeException if the frequency type is not a {@link SimpleFrequency} or
   * {@link PeriodFrequency}
   */
  public static Period getTenor(final Frequency freq) {
    ArgumentChecker.notNull(freq, "freq");
    if (Frequency.NEVER_NAME.equals(freq.getName())) {
      return Period.ZERO;
    } else if (freq instanceof PeriodFrequency) {
      final Period period = ((PeriodFrequency) freq).getPeriod();
      if (period.getYears() == 1 && period.getMonths() == 0 && period.getDays() == 0) {
        return Period.ofMonths(12);
      }
      return period;
    } else if (freq instanceof SimpleFrequency) {
      final Period period =  ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
      if (period.getYears() == 1 && period.getMonths() == 0 && period.getDays() == 0) {
        return Period.ofMonths(12);
      }
      return period;
    }
    throw new OpenGammaRuntimeException("Can only handle PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }
}

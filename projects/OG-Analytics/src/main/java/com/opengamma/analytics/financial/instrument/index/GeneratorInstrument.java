/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;

/**
 * Generic class for instrument generators (deposit, ois, irs, ...).
 */
public abstract class GeneratorInstrument {

  /**
   * Name of the generator.
   */
  private final String _name;

  /**
   * Constructor.
   * @param name The generator name.
   */
  public GeneratorInstrument(String name) {
    Validate.notNull(name, "Name");
    _name = name;
  }

  /**
   * Gets the generator name.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Generate an instrument compatible with the generator from a reference date, one period and a market quote.
   * @param date The reference date. In general it is "today" or the trade date.
   * @param tenor The instrument tenor. When only one tenor is provided, the instrument will be spot starting or something similar (instrument dependent).
   * @param marketQuote The instrument market quote.
   * @param notional The instrument notional.
   * @param objects The instrument specific extra data (like FX rates, ...)
   * @return The instrument.
   */
  public abstract InstrumentDefinition<?> generateInstrument(final ZonedDateTime date, final Period tenor, final double marketQuote, final double notional, final Object... objects);

  /**
   * Generate an instrument compatible with the generator from a reference date, two periods and a market quote.
   * @param date The reference date. In general it is "today" or the trade date.
   * @param startTenor The instrument start tenor. The exact meaning is instrument dependent. In general it is the period from spot to the effective date.
   * @param endTenor The instrument end tenor. The exact meaning is instrument dependent. In general it is the period from spot to the maturity date.
   * @param marketQuote The instrument market quote.
   * @param notional The instrument notional.
   * @param objects The instrument specific extra data (like FX rates, ...)
   * @return The instrument.
   */
  public abstract InstrumentDefinition<?> generateInstrument(final ZonedDateTime date, final Period startTenor, final Period endTenor, final double marketQuote, final double notional,
      final Object... objects);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GeneratorInstrument other = (GeneratorInstrument) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}

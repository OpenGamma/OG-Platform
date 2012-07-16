/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;

/**
 * Generic class for generators (deposit, ois, irs, ...).
 */
public abstract class Generator {

  /**
   * Name of the generator.
   */
  private final String _name;

  /**
   * Constructor.
   * @param name The generator name.
   */
  public Generator(String name) {
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

  public abstract InstrumentDefinition<?> generateInstrument(final ZonedDateTime date, final Period tenor, final double marketQuote,
      final double notional, final Object... objects);

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
    Generator other = (Generator) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import org.threeten.bp.Period;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.time.Tenor;

/**
 * Hibernate bean for storing tenor.
 */
public class TenorBean extends EnumBean {

  protected TenorBean() {
  }

  public TenorBean(final String tenor) {
    super(tenor);
  }

  /* package */ Tenor toFrequency() {
    try {
      final Period period = Period.parse(getName());
      return Tenor.of(period);
    } catch (DateTimeParseException dpe) {
      throw new OpenGammaRuntimeException("Bad value for tenorBean (" + getName() + ")");
    }
  }

}

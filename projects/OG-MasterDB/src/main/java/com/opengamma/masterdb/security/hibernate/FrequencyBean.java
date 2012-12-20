/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;

/**
 * Hibernate bean for storing frequency.
 */
public class FrequencyBean extends EnumBean {

  protected FrequencyBean() {
  }

  public FrequencyBean(String frequency) {
    super(frequency);
  }

  /* package */ Frequency toFrequency() {
    final Frequency f = SimpleFrequencyFactory.INSTANCE.getFrequency(getName());
    if (f == null) {
      throw new OpenGammaRuntimeException("Bad value for frequencyBean (" + getName() + ")");
    }
    return f;
  }

}

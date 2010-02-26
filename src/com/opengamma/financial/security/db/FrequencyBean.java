/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import javax.persistence.Entity;

@Entity
public class FrequencyBean extends EnumBean {
  protected FrequencyBean() {
  }

  public FrequencyBean(String frequency) {
    super(frequency);
  }
}

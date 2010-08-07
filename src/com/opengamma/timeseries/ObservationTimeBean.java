/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.financial.security.db.EnumWithDescriptionBean;

/**
 * 
 */
public class ObservationTimeBean extends EnumWithDescriptionBean {
  protected ObservationTimeBean() {
  }

  public ObservationTimeBean(String exchangeName, String description) {
    super(exchangeName, description);
  }
}

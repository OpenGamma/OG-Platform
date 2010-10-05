/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import com.opengamma.financial.security.master.db.hibernate.EnumWithDescriptionBean;

/**
 * 
 */
public class DataFieldBean extends EnumWithDescriptionBean {
  protected DataFieldBean() {
  }

  public DataFieldBean(String exchangeName, String description) {
    super(exchangeName, description);
  }
}

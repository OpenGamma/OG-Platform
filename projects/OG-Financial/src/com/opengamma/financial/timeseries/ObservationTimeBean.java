/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import com.opengamma.financial.security.master.db.hibernate.EnumWithDescriptionBean;

/**
 * Hibernate bean for storing an observation time.
 */
public class ObservationTimeBean extends EnumWithDescriptionBean {

  protected ObservationTimeBean() {
  }

  public ObservationTimeBean(String exchangeName, String description) {
    super(exchangeName, description);
  }

}

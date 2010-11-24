/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import com.opengamma.financial.security.master.db.hibernate.EnumWithDescriptionBean;

/**
 * Hibernate bean for storing a data provider.
 */
public class DataProviderBean extends EnumWithDescriptionBean {

  protected DataProviderBean() {
  }

  public DataProviderBean(String name, String description) {
    super(name, description);
  }

}

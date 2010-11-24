/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

import com.opengamma.financial.security.master.db.hibernate.EnumWithDescriptionBean;

/**
 * Hibernate bean for storing a data source.
 */
public class DataSourceBean extends EnumWithDescriptionBean {

  protected DataSourceBean() {
  }

  public DataSourceBean(String name, String description) {
    super(name, description);
  }

}

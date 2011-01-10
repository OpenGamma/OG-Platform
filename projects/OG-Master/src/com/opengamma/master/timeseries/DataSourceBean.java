/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

/**
 * Database bean for storing a data source.
 */
public class DataSourceBean extends NamedDescriptionBean {

  protected DataSourceBean() {
  }

  public DataSourceBean(String name, String description) {
    super(name, description);
  }

}

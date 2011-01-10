/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

/**
 * Database bean for storing a data provider.
 */
public class DataProviderBean extends NamedDescriptionBean {

  protected DataProviderBean() {
  }

  public DataProviderBean(String name, String description) {
    super(name, description);
  }

}

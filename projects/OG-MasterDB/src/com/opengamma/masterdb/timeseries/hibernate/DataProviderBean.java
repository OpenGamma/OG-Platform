/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.timeseries.hibernate;

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

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

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

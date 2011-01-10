/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

/**
 * Database bean for storing a data field.
 */
public class DataFieldBean extends NamedDescriptionBean {

  protected DataFieldBean() {
  }

  public DataFieldBean(String exchangeName, String description) {
    super(exchangeName, description);
  }

}

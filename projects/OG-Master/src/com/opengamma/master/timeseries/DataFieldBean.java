/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

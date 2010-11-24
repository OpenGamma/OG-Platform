/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries;

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

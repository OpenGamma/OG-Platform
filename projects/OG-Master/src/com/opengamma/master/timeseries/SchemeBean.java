/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

/**
 * Database bean for storing a scheme.
 */
public class SchemeBean extends NamedDescriptionBean {

  protected SchemeBean() {
  }

  public SchemeBean(String name, String description) {
    super(name, description);
  }

}

/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

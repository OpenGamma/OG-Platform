/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

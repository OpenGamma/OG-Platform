/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.financial.security.db.EnumWithDescriptionBean;

/**
 * Representation of a scheme in the datastore
 */
public class SchemeBean extends EnumWithDescriptionBean {
  protected SchemeBean() {
  }

  public SchemeBean(String name, String description) {
    super(name, description);
  }

}

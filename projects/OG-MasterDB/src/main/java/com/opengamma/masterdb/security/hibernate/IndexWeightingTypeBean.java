/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.index.IndexWeightingType;

/**
 * Hibernate bean for storing tenor.
 */
public class IndexWeightingTypeBean extends EnumBean {

  protected IndexWeightingTypeBean() {
  }

  public IndexWeightingTypeBean(final String tenor) {
    super(tenor);
  }

  /* package */ IndexWeightingType toIndexWeightingType() {
    try {
      return IndexWeightingType.valueOf(getName());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new OpenGammaRuntimeException("Bad value for indexWeightingTypeBean (" + getName() + ")", e);
    }
  }

}

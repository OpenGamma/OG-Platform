/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibernate bean for storing a guarantee type.
 */
public class GuaranteeTypeBean extends EnumBean {

  protected GuaranteeTypeBean() {
  }

  public GuaranteeTypeBean(String guaranteeType) {
    super(guaranteeType);
  }

}

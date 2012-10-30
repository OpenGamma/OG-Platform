/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.bond;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibernate bean for storing the yield convention.
 */
public class YieldConventionBean extends EnumBean {

  protected YieldConventionBean() {
  }

  public YieldConventionBean(String yieldConvention) {
    super(yieldConvention);
  }

  /* package */ YieldConvention toYieldConvention() {
    final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(getName());
    if (yieldConvention == null) {
      throw new OpenGammaRuntimeException("Bad value for yieldConventionBean (" + getName() + ")");
    }
    return yieldConvention;
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.bond;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.master.db.hibernate.EnumBean;

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

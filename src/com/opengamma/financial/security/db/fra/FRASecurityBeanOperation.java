/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.fra;

import com.opengamma.financial.security.db.AbstractBeanOperation;
import com.opengamma.financial.security.db.HibernateSecurityMasterDao;
import com.opengamma.financial.security.fra.FRASecurity;

/**
 * Bean/security conversion operations.
 */
public final class FRASecurityBeanOperation extends AbstractBeanOperation<FRASecurity, FRASecurityBean> {

  /**
   * Singleton instance.
   */
  public static final FRASecurityBeanOperation INSTANCE = new FRASecurityBeanOperation();

  private FRASecurityBeanOperation() {
    super("FRA", FRASecurity.class, FRASecurityBean.class);
  }

  @Override
  public boolean beanEquals(FRASecurityBean bean, FRASecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public FRASecurityBean createBean(HibernateSecurityMasterDao secMasterSession, FRASecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public FRASecurity createSecurity(FRASecurityBean bean) {
    // TODO
    throw new UnsupportedOperationException();
  }

}

/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.swap;

import com.opengamma.financial.security.db.AbstractBeanOperation;
import com.opengamma.financial.security.db.HibernateSecurityMasterDao;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Bean/security conversion operations.
 */
public final class SwapSecurityBeanOperation extends AbstractBeanOperation<SwapSecurity, SwapSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final SwapSecurityBeanOperation INSTANCE = new SwapSecurityBeanOperation();

  private SwapSecurityBeanOperation() {
    super("SWAP", SwapSecurity.class, SwapSecurityBean.class);
  }

  @Override
  public boolean beanEquals(SwapSecurityBean bean, SwapSecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public SwapSecurityBean createBean(HibernateSecurityMasterDao secMasterSession, SwapSecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public SwapSecurity createSecurity(SwapSecurityBean bean) {
    // TODO
    throw new UnsupportedOperationException();
  }

}

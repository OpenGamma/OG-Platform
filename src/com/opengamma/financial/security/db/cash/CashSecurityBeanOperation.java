/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.cash;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.db.AbstractBeanOperation;
import com.opengamma.financial.security.db.HibernateSecurityMasterDao;

/**
 * Bean/security conversion operations.
 */
public final class CashSecurityBeanOperation extends AbstractBeanOperation<CashSecurity, CashSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CashSecurityBeanOperation INSTANCE = new CashSecurityBeanOperation();

  private CashSecurityBeanOperation() {
    super("CASH", CashSecurity.class, CashSecurityBean.class);
  }

  @Override
  public boolean beanEquals(CashSecurityBean bean, CashSecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public CashSecurityBean createBean(HibernateSecurityMasterDao secMasterSession, CashSecurity security) {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public CashSecurity createSecurity(CashSecurityBean bean) {
    // TODO
    throw new UnsupportedOperationException();
  }

}

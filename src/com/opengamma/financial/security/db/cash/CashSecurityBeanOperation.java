/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.cash;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.db.AbstractBeanOperation;
import com.opengamma.financial.security.db.CurrencyBean;
import com.opengamma.financial.security.db.HibernateSecurityMasterDao;
import com.opengamma.financial.security.db.IdentifierBean;
import com.opengamma.financial.security.db.OperationContext;
import static com.opengamma.financial.security.db.Converters.*;

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
  public boolean beanEquals(final OperationContext context, CashSecurityBean bean, CashSecurity security) {
    return true;
  }

  @Override
  public CashSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, CashSecurity security) {
    CurrencyBean currencyBean = secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getISOCode());
    IdentifierBean regionIdentifier = identifierToIdentifierBean(security.getRegion());
    final CashSecurityBean bean = new CashSecurityBean();
    bean.setCurrency(currencyBean);
    bean.setRegion(regionIdentifier);
    return bean;
  }

  @Override
  public CashSecurity createSecurity(final OperationContext context, CashSecurityBean bean) {
    final CashSecurity security = new CashSecurity(currencyBeanToCurrency(bean.getCurrency()), identifierBeanToIdentifier(bean.getRegion()));
    return security;
  }

}

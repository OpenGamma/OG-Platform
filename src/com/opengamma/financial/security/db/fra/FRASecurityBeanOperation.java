/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.fra;

import static com.opengamma.financial.security.db.Converters.dateToLocalDate;
import static com.opengamma.financial.security.db.Converters.localDateToDate;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.db.AbstractBeanOperation;
import com.opengamma.financial.security.db.HibernateSecurityMasterDao;
import com.opengamma.financial.security.db.OperationContext;
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
  public boolean beanEquals(final OperationContext context, FRASecurityBean bean, FRASecurity security) {
    return ObjectUtils.equals(dateToLocalDate(bean.getStartDate()), security.getStartDate()) && ObjectUtils.equals(dateToLocalDate(bean.getEndDate()), security.getEndDate());
  }

  @Override
  public FRASecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, FRASecurity security) {
    final FRASecurityBean bean = new FRASecurityBean();
    bean.setStartDate(localDateToDate(security.getStartDate()));
    bean.setEndDate(localDateToDate(security.getEndDate()));
    return bean;
  }

  @Override
  public FRASecurity createSecurity(final OperationContext context, FRASecurityBean bean) {
    return new FRASecurity(dateToLocalDate(bean.getStartDate()).atMidnight(), dateToLocalDate(bean.getEndDate()).atMidnight());
  }

}

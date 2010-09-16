/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.master.db.hibernate.fra;

import static com.opengamma.financial.security.master.db.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.financial.security.master.db.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.financial.security.master.db.hibernate.Converters.identifierBeanToIdentifier;
import static com.opengamma.financial.security.master.db.hibernate.Converters.identifierToIdentifierBean;
import static com.opengamma.financial.security.master.db.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.master.db.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.financial.security.master.db.hibernate.HibernateSecurityMasterDao;
import com.opengamma.financial.security.master.db.hibernate.OperationContext;

/**
 * Bean/security conversion operations.
 */
public final class FRASecurityBeanOperation extends AbstractSecurityBeanOperation<FRASecurity, FRASecurityBean> {

  /**
   * Singleton instance.
   */
  public static final FRASecurityBeanOperation INSTANCE = new FRASecurityBeanOperation();

  private FRASecurityBeanOperation() {
    super("FRA", FRASecurity.class, FRASecurityBean.class);
  }

  @Override
  public boolean beanEquals(final OperationContext context, FRASecurityBean bean, FRASecurity security) {
    return ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency()), security.getCurrency())
        && ObjectUtils.equals(identifierBeanToIdentifier(bean.getRegion()), security.getRegion())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()), security.getStartDate())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getEndDate()), security.getEndDate());
  }

  @Override
  public FRASecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, FRASecurity security) {
    final FRASecurityBean bean = new FRASecurityBean();
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getISOCode()));
    bean.setRegion(identifierToIdentifierBean(security.getRegion()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setEndDate(dateTimeWithZoneToZonedDateTimeBean(security.getEndDate()));
    return bean;
  }

  @Override
  public FRASecurity createSecurity(final OperationContext context, FRASecurityBean bean) {
    return new FRASecurity(currencyBeanToCurrency(bean.getCurrency()), identifierBeanToIdentifier(bean.getRegion()), 
                           zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getEndDate()));
  }

}

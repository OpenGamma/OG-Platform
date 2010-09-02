/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.fra;

import static com.opengamma.financial.security.db.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.financial.security.db.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.db.AbstractSecurityBeanOperation;
import com.opengamma.financial.security.db.HibernateSecurityMasterDao;
import com.opengamma.financial.security.db.OperationContext;
import com.opengamma.financial.security.fra.FRASecurity;

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
    return ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()), security.getStartDate())
        && ObjectUtils.equals(zonedDateTimeBeanToDateTimeWithZone(bean.getEndDate()), security.getEndDate());
  }

  @Override
  public FRASecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, FRASecurity security) {
    final FRASecurityBean bean = new FRASecurityBean();
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setEndDate(dateTimeWithZoneToZonedDateTimeBean(security.getEndDate()));
    return bean;
  }

  @Override
  public FRASecurity createSecurity(final OperationContext context, FRASecurityBean bean) {
    return new FRASecurity(zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getEndDate()));
  }

}

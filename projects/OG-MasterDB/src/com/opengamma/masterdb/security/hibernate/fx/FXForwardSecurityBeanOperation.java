/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Bean/security conversion operations.
 */
public final class FXForwardSecurityBeanOperation extends AbstractSecurityBeanOperation<FXForwardSecurity, FXForwardSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final FXForwardSecurityBeanOperation INSTANCE = new FXForwardSecurityBeanOperation();

  private FXForwardSecurityBeanOperation() {
    super(FXForwardSecurity.SECURITY_TYPE, FXForwardSecurity.class, FXForwardSecurityBean.class);
  }

  @Override
  public FXForwardSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, FXForwardSecurity security) {
    final FXForwardSecurityBean bean = new FXForwardSecurityBean();
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setForwardDate(dateTimeWithZoneToZonedDateTimeBean(security.getForwardDate()));
    bean.setRegion(externalIdToExternalIdBean(security.getRegionId()));
    return bean;
  }

  @Override
  public FXForwardSecurity createSecurity(final OperationContext context, FXForwardSecurityBean bean) {
    ZonedDateTime forwardDate = zonedDateTimeBeanToDateTimeWithZone(bean.getForwardDate());
    ExternalId region = externalIdBeanToExternalId(bean.getRegion());
    ExternalId underlyingIdentifier = externalIdBeanToExternalId(bean.getUnderlying());
    return new FXForwardSecurity(underlyingIdentifier, forwardDate, region);
  }

}

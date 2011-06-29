/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierBeanToIdentifier;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierToIdentifierBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.Identifier;
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
    bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentifier()));
    bean.setForwardDate(dateTimeWithZoneToZonedDateTimeBean(security.getForwardDate()));
    bean.setRegion(identifierToIdentifierBean(security.getRegion()));
    return bean;
  }

  @Override
  public FXForwardSecurity createSecurity(final OperationContext context, FXForwardSecurityBean bean) {
    ZonedDateTime forwardDate = zonedDateTimeBeanToDateTimeWithZone(bean.getForwardDate());
    Identifier region = identifierBeanToIdentifier(bean.getRegion());
    Identifier underlyingIdentifier = identifierBeanToIdentifier(bean.getUnderlying());
    return new FXForwardSecurity(underlyingIdentifier, forwardDate, region);
  }

}

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

import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Bean/security conversion operations.
 */
public final class NonDeliverableFXForwardSecurityBeanOperation extends AbstractSecurityBeanOperation<NonDeliverableFXForwardSecurity, NonDeliverableFXForwardSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final NonDeliverableFXForwardSecurityBeanOperation INSTANCE = new NonDeliverableFXForwardSecurityBeanOperation();

  private NonDeliverableFXForwardSecurityBeanOperation() {
    super(NonDeliverableFXForwardSecurity.SECURITY_TYPE, NonDeliverableFXForwardSecurity.class, NonDeliverableFXForwardSecurityBean.class);
  }

  @Override
  public NonDeliverableFXForwardSecurityBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, NonDeliverableFXForwardSecurity security) {
    final NonDeliverableFXForwardSecurityBean bean = new NonDeliverableFXForwardSecurityBean();
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setForwardDate(dateTimeWithZoneToZonedDateTimeBean(security.getForwardDate()));
    bean.setRegion(externalIdToExternalIdBean(security.getRegionId()));
    bean.setIsDeliveryInReceiveCurrency(security.isDeliveryInReceiveCurrency());
    return bean;
  }

  @Override
  public NonDeliverableFXForwardSecurity createSecurity(final OperationContext context, NonDeliverableFXForwardSecurityBean bean) {
    ZonedDateTime forwardDate = zonedDateTimeBeanToDateTimeWithZone(bean.getForwardDate());
    ExternalId region = externalIdBeanToExternalId(bean.getRegion());
    ExternalId underlyingIdentifier = externalIdBeanToExternalId(bean.getUnderlying());
    Boolean isDeliveryInReceiveCurrency = bean.getIsDeliveryInReceiveCurrency();
    return new NonDeliverableFXForwardSecurity(underlyingIdentifier, forwardDate, region, isDeliveryInReceiveCurrency);
  }

}

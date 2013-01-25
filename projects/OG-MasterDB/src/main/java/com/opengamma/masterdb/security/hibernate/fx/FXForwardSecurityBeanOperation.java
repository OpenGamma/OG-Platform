/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

/**
 * Hibernate bean/security conversion operations.
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
    bean.setPayCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPayCurrency().getCode()));
    bean.setPayAmount(security.getPayAmount());
    bean.setReceiveCurrency(secMasterSession.getOrCreateCurrencyBean(security.getReceiveCurrency().getCode()));
    bean.setReceiveAmount(security.getReceiveAmount());
    bean.setForwardDate(dateTimeWithZoneToZonedDateTimeBean(security.getForwardDate()));
    bean.setRegion(externalIdToExternalIdBean(security.getRegionId()));
    return bean;
  }

  @Override
  public FXForwardSecurity createSecurity(final OperationContext context, FXForwardSecurityBean bean) {
    ZonedDateTime forwardDate = zonedDateTimeBeanToDateTimeWithZone(bean.getForwardDate());
    ExternalId region = externalIdBeanToExternalId(bean.getRegion());
    Currency payCurrency = currencyBeanToCurrency(bean.getPayCurrency());
    double payAmount = bean.getPayAmount();
    Currency receiveCurrency = currencyBeanToCurrency(bean.getReceiveCurrency());
    double receiveAmount = bean.getReceiveAmount();
    return new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region);
  }

}

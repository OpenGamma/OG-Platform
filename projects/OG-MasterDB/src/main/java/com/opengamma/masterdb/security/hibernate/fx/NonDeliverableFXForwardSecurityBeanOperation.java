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

import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

/**
 * Hibernate bean/security conversion operations.
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
    bean.setPayCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPayCurrency().getCode()));
    bean.setPayAmount(security.getPayAmount());
    bean.setReceiveCurrency(secMasterSession.getOrCreateCurrencyBean(security.getReceiveCurrency().getCode()));
    bean.setReceiveAmount(security.getReceiveAmount());
    bean.setForwardDate(dateTimeWithZoneToZonedDateTimeBean(security.getForwardDate()));
    bean.setRegion(externalIdToExternalIdBean(security.getRegionId()));
    bean.setDeliverInReceiveCurrency(security.isDeliverInReceiveCurrency());
    return bean;
  }

  @Override
  public NonDeliverableFXForwardSecurity createSecurity(final OperationContext context, NonDeliverableFXForwardSecurityBean bean) {
    ZonedDateTime forwardDate = zonedDateTimeBeanToDateTimeWithZone(bean.getForwardDate());
    ExternalId region = externalIdBeanToExternalId(bean.getRegion());
    Currency payCurrency = currencyBeanToCurrency(bean.getPayCurrency());
    double payAmount = bean.getPayAmount();
    Currency receiveCurrency = currencyBeanToCurrency(bean.getReceiveCurrency());
    double receiveAmount = bean.getReceiveAmount();
    boolean deliverInReceiveCurrency = bean.isDeliverInReceiveCurrency();
    return new NonDeliverableFXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region, deliverInReceiveCurrency);
  }

}

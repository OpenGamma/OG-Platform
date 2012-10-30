/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * EquityIndexOptionSecurityBeanOperation
 */
public final class EquityIndexOptionSecurityBeanOperation  extends AbstractSecurityBeanOperation<EquityIndexOptionSecurity, EquityIndexOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final EquityIndexOptionSecurityBeanOperation INSTANCE = new EquityIndexOptionSecurityBeanOperation();
  
  private EquityIndexOptionSecurityBeanOperation() {
    super(EquityIndexOptionSecurity.SECURITY_TYPE, EquityIndexOptionSecurity.class, EquityIndexOptionSecurityBean.class);
  }

  @Override
  public EquityIndexOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final EquityIndexOptionSecurity security) {
    final EquityIndexOptionSecurityBean bean = new EquityIndexOptionSecurityBean();
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setOptionType(security.getOptionType());
    bean.setStrike(security.getStrike());
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setExchange(secMasterSession.getOrCreateExchangeBean(security.getExchange(), ""));
    bean.setPointValue(security.getPointValue());
    return bean;
  }

  @Override
  public EquityIndexOptionSecurity createSecurity(OperationContext context, EquityIndexOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());

    EquityIndexOptionSecurity sec = new EquityIndexOptionSecurity(bean.getOptionType(), 
        bean.getStrike(), 
        currencyBeanToCurrency(bean.getCurrency()), 
        externalIdBeanToExternalId(bean.getUnderlying()), 
        exerciseType, 
        expiryBeanToExpiry(bean.getExpiry()), 
        bean.getPointValue(), 
        bean.getExchange().getName());
    return sec;
  }

}

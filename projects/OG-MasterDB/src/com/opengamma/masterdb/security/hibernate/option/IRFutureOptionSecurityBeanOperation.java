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

import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * EquityIndexOptionSecurityBeanOperation
 */
public final class IRFutureOptionSecurityBeanOperation  extends AbstractSecurityBeanOperation<IRFutureOptionSecurity, IRFutureOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final IRFutureOptionSecurityBeanOperation INSTANCE = new IRFutureOptionSecurityBeanOperation();
  
  private IRFutureOptionSecurityBeanOperation() {
    super(IRFutureOptionSecurity.SECURITY_TYPE, IRFutureOptionSecurity.class, IRFutureOptionSecurityBean.class);
  }

  @Override
  public IRFutureOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final IRFutureOptionSecurity security) {
    final IRFutureOptionSecurityBean bean = new IRFutureOptionSecurityBean();
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setOptionType(security.getOptionType());
    bean.setStrike(security.getStrike());
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setExchange(secMasterSession.getOrCreateExchangeBean(security.getExchange(), ""));
    bean.setPointValue(security.getPointValue());
    bean.setMargined(security.isMargined());
    return bean;
  }

  @Override
  public IRFutureOptionSecurity createSecurity(OperationContext context, IRFutureOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());
    
    IRFutureOptionSecurity sec = new IRFutureOptionSecurity(bean.getExchange().getName(), 
        expiryBeanToExpiry(bean.getExpiry()), 
        exerciseType, 
        externalIdBeanToExternalId(bean.getUnderlying()), 
        bean.getPointValue(), 
        bean.getMargined(), 
        currencyBeanToCurrency(bean.getCurrency()), 
        bean.getStrike(), bean.getOptionType());
    return sec;
  }

}

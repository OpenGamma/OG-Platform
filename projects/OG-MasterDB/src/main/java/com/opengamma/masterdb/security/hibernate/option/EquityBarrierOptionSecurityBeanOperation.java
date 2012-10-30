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

import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * EquityIndexOptionSecurityBeanOperation
 */
public final class EquityBarrierOptionSecurityBeanOperation  extends AbstractSecurityBeanOperation<EquityBarrierOptionSecurity, EquityBarrierOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final EquityBarrierOptionSecurityBeanOperation INSTANCE = new EquityBarrierOptionSecurityBeanOperation();
  
  private EquityBarrierOptionSecurityBeanOperation() {
    super(EquityBarrierOptionSecurity.SECURITY_TYPE, EquityBarrierOptionSecurity.class, EquityBarrierOptionSecurityBean.class);
  }

  @Override
  public EquityBarrierOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final EquityBarrierOptionSecurity security) {
    final EquityBarrierOptionSecurityBean bean = new EquityBarrierOptionSecurityBean();
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setOptionType(security.getOptionType());
    bean.setStrike(security.getStrike());
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setExchange(secMasterSession.getOrCreateExchangeBean(security.getExchange(), ""));
    bean.setPointValue(security.getPointValue());
    
    bean.setBarrierType(security.getBarrierType());
    bean.setBarrierDirection(security.getBarrierDirection());
    bean.setMonitoringType(security.getMonitoringType());
    bean.setSamplingFrequency(security.getSamplingFrequency());
    bean.setBarrierLevel(security.getBarrierLevel());
    
    return bean;
  }

  @Override
  public EquityBarrierOptionSecurity createSecurity(OperationContext context, EquityBarrierOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());

    EquityBarrierOptionSecurity sec = new EquityBarrierOptionSecurity(bean.getOptionType(), 
        bean.getStrike(), 
        currencyBeanToCurrency(bean.getCurrency()), 
        externalIdBeanToExternalId(bean.getUnderlying()), 
        exerciseType, 
        expiryBeanToExpiry(bean.getExpiry()), 
        bean.getPointValue(), 
        bean.getExchange().getName(),
        bean.getBarrierType(), 
        bean.getBarrierDirection(), 
        bean.getMonitoringType(), 
        bean.getSamplingFrequency(),
        bean.getBarrierLevel());
    return sec;
  }

}

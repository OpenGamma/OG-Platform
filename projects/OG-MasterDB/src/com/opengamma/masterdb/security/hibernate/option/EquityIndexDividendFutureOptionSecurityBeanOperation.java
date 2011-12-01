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

import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * EquityIndexOptionSecurityBeanOperation
 */
public final class EquityIndexDividendFutureOptionSecurityBeanOperation  extends AbstractSecurityBeanOperation<EquityIndexDividendFutureOptionSecurity, EquityIndexDividendFutureOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final EquityIndexDividendFutureOptionSecurityBeanOperation INSTANCE = new EquityIndexDividendFutureOptionSecurityBeanOperation();
  
  private EquityIndexDividendFutureOptionSecurityBeanOperation() {
    super(EquityIndexDividendFutureOptionSecurity.SECURITY_TYPE, EquityIndexDividendFutureOptionSecurity.class, EquityIndexDividendFutureOptionSecurityBean.class);
  }

  @Override
  public EquityIndexDividendFutureOptionSecurityBean createBean(final OperationContext context,
      final HibernateSecurityMasterDao secMasterSession, final EquityIndexDividendFutureOptionSecurity security) {
    final EquityIndexDividendFutureOptionSecurityBean bean = new EquityIndexDividendFutureOptionSecurityBean();
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
  public EquityIndexDividendFutureOptionSecurity createSecurity(OperationContext context, EquityIndexDividendFutureOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());
    
    EquityIndexDividendFutureOptionSecurity sec = new EquityIndexDividendFutureOptionSecurity(bean.getExchange().getName(), 
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

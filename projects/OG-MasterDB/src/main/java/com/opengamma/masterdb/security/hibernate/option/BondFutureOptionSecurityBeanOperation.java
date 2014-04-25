/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * BondOptionSecurityBeanOperation
 */
public final class BondFutureOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<BondFutureOptionSecurity, BondFutureOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final BondFutureOptionSecurityBeanOperation INSTANCE = new BondFutureOptionSecurityBeanOperation();

  private BondFutureOptionSecurityBeanOperation() {
    super(BondFutureOptionSecurity.SECURITY_TYPE, BondFutureOptionSecurity.class, BondFutureOptionSecurityBean.class);
  }

  @Override
  public BondFutureOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final BondFutureOptionSecurity security) {
    final BondFutureOptionSecurityBean bean = new BondFutureOptionSecurityBean();
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setOptionType(security.getOptionType());
    bean.setStrike(security.getStrike());
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setTradingExchange(secMasterSession.getOrCreateExchangeBean(security.getTradingExchange(), ""));
    bean.setSettlementExchange(secMasterSession.getOrCreateExchangeBean(security.getSettlementExchange(), ""));
    bean.setPointValue(security.getPointValue());
    bean.setMargined(security.isMargined());
    return bean;
  }

  @Override
  public BondFutureOptionSecurity createSecurity(OperationContext context, BondFutureOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());

    BondFutureOptionSecurity sec = new BondFutureOptionSecurity(
      bean.getTradingExchange().getName(),
      bean.getSettlementExchange().getName(),
      expiryBeanToExpiry(bean.getExpiry()),
      exerciseType,
      externalIdBeanToExternalId(bean.getUnderlying()),
      bean.getPointValue(),
      bean.isMargined(),
      currencyBeanToCurrency(bean.getCurrency()), bean.getStrike(), bean.getOptionType());
    return sec;
  }

}

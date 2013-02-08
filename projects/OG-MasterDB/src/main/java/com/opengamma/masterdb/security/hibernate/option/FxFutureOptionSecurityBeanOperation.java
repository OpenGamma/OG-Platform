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

import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * FxOptionSecurityBeanOperation
 */
public final class FxFutureOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<FxFutureOptionSecurity, FxFutureOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final FxFutureOptionSecurityBeanOperation INSTANCE = new FxFutureOptionSecurityBeanOperation();

  private FxFutureOptionSecurityBeanOperation() {
    super(FxFutureOptionSecurity.SECURITY_TYPE, FxFutureOptionSecurity.class, FxFutureOptionSecurityBean.class);
  }

  @Override
  public FxFutureOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final FxFutureOptionSecurity security) {
    final FxFutureOptionSecurityBean bean = new FxFutureOptionSecurityBean();
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setOptionType(security.getOptionType());
    bean.setStrike(security.getStrike());
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setTradingExchange(secMasterSession.getOrCreateExchangeBean(security.getTradingExchange(), ""));
    bean.setSettlementExchange(secMasterSession.getOrCreateExchangeBean(security.getSettlementExchange(), ""));
    bean.setPointValue(security.getPointValue());
    return bean;
  }

  @Override
  public FxFutureOptionSecurity createSecurity(OperationContext context, FxFutureOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());

    return new FxFutureOptionSecurity(
      bean.getTradingExchange().getName(),
      bean.getSettlementExchange().getName(),
      expiryBeanToExpiry(bean.getExpiry()),
      exerciseType,
      externalIdBeanToExternalId(bean.getUnderlying()),
      bean.getPointValue(),
      currencyBeanToCurrency(bean.getCurrency()),
      bean.getStrike(), bean.getOptionType());
  }

}

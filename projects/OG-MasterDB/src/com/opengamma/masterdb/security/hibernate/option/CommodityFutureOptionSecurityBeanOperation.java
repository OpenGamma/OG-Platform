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

import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * CommodityOptionSecurityBeanOperation
 */
public final class CommodityFutureOptionSecurityBeanOperation extends AbstractSecurityBeanOperation<CommodityFutureOptionSecurity, CommodityFutureOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final CommodityFutureOptionSecurityBeanOperation INSTANCE = new CommodityFutureOptionSecurityBeanOperation();

  private CommodityFutureOptionSecurityBeanOperation() {
    super(CommodityFutureOptionSecurity.SECURITY_TYPE, CommodityFutureOptionSecurity.class, CommodityFutureOptionSecurityBean.class);
  }

  @Override
  public CommodityFutureOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final CommodityFutureOptionSecurity security) {
    final CommodityFutureOptionSecurityBean bean = new CommodityFutureOptionSecurityBean();
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
  public CommodityFutureOptionSecurity createSecurity(OperationContext context, CommodityFutureOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());

    CommodityFutureOptionSecurity sec = new CommodityFutureOptionSecurity(
      bean.getTradingExchange().getName(),
      bean.getSettlementExchange().getName(),
      expiryBeanToExpiry(bean.getExpiry()),
      exerciseType,
      externalIdBeanToExternalId(bean.getUnderlying()),
      bean.getPointValue(),
      currencyBeanToCurrency(bean.getCurrency()),
      bean.getStrike(), bean.getOptionType());
    return sec;
  }

}

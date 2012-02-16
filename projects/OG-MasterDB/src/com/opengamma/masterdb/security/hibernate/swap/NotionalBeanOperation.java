/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.uniqueIdBeanToUniqueId;
import static com.opengamma.masterdb.security.hibernate.Converters.uniqueIdToUniqueIdBean;

import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;

/**
 * Bean conversion operations.
 */
public final class NotionalBeanOperation {

  private NotionalBeanOperation() {
  }

  public static NotionalBean createBean(final HibernateSecurityMasterDao secMasterSession, final Notional notional) {
    return notional.accept(new NotionalVisitor<NotionalBean>() {

      private NotionalBean createNotionalBean(Notional notional) {
        final NotionalBean bean = new NotionalBean();
        bean.setNotionalType(NotionalType.identify(notional));
        return bean;
      }

      @Override
      public NotionalBean visitCommodityNotional(CommodityNotional notional) {
        return createNotionalBean(notional);
      }

      @Override
      public NotionalBean visitInterestRateNotional(InterestRateNotional notional) {
        final NotionalBean bean = createNotionalBean(notional);
        bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(notional.getCurrency().getCode()));
        bean.setAmount(notional.getAmount());
        return bean;
      }

      @Override
      public NotionalBean visitSecurityNotional(SecurityNotional notional) {
        final NotionalBean bean = createNotionalBean(notional);
        bean.setIdentifier(uniqueIdToUniqueIdBean(notional.getNotionalId()));
        return bean;
      }

      @Override
      public NotionalBean visitVarianceSwapNotional(VarianceSwapNotional notional) {
        NotionalBean bean = createNotionalBean(notional);
        bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(notional.getCurrency().getCode()));
        bean.setAmount(notional.getAmount());
        return bean;
      }

    });
  }

  public static Notional createNotional(final NotionalBean bean) {
    return bean.getNotionalType().accept(new NotionalVisitor<Notional>() {

      @Override
      public Notional visitCommodityNotional(final CommodityNotional ignore) {
        return new CommodityNotional();
      }

      @Override
      public Notional visitInterestRateNotional(InterestRateNotional ignore) {
        return new InterestRateNotional(currencyBeanToCurrency(bean.getCurrency()), bean.getAmount());
      }

      @Override
      public Notional visitSecurityNotional(SecurityNotional ignore) {
        UniqueId uniqueId = uniqueIdBeanToUniqueId(bean.getIdentifier());
        return new SecurityNotional(uniqueId);
      }

      @Override
      public Notional visitVarianceSwapNotional(VarianceSwapNotional notional) {
        return new VarianceSwapNotional(currencyBeanToCurrency(bean.getCurrency()), bean.getAmount());
      }

    });
  }

}

/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierBeanToIdentifier;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierToIdentifierBean;

import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
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
        final NotionalBean bean = createNotionalBean(notional);
        return bean;
      }

      @Override
      public NotionalBean visitInterestRateNotional(InterestRateNotional notional) {
        final NotionalBean bean = createNotionalBean(notional);
        bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(notional.getCurrency().getISOCode()));
        bean.setAmount(notional.getAmount());
        return bean;
      }

      @Override
      public NotionalBean visitSecurityNotional(SecurityNotional notional) {
        final NotionalBean bean = createNotionalBean(notional);
        bean.setIdentifier(identifierToIdentifierBean(notional.getNotionalIdentifier().toIdentifier()));
        return bean;
      }

    });
  }

  public static Notional createNotional(final NotionalBean bean) {
    return bean.getNotionalType().accept(new NotionalVisitor<Notional>() {

      @Override
      public Notional visitCommodityNotional(final CommodityNotional ignore) {
        final CommodityNotional notional = new CommodityNotional();
        return notional;
      }

      @Override
      public Notional visitInterestRateNotional(InterestRateNotional ignore) {
        final InterestRateNotional notional = new InterestRateNotional(currencyBeanToCurrency(bean.getCurrency()), bean.getAmount());
        return notional;
      }

      @Override
      public Notional visitSecurityNotional(SecurityNotional ignore) {
        final SecurityNotional notional = new SecurityNotional(identifierBeanToIdentifier(bean.getIdentifier()).toUniqueIdentifier());
        return notional;
      }

    });
  }

}

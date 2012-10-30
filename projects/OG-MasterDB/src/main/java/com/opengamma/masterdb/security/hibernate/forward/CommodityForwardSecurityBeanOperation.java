/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.forward;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import java.util.Date;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.CommodityForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Hibernate bean for storage.
 */
public final class CommodityForwardSecurityBeanOperation extends
  AbstractSecurityBeanOperation<CommodityForwardSecurity, CommodityForwardSecurityBean> {

  /**
   * Singleton.
   * */
  public static final CommodityForwardSecurityBeanOperation INSTANCE = new CommodityForwardSecurityBeanOperation();

  private CommodityForwardSecurityBeanOperation() {
    super(CommodityForwardSecurity.SECURITY_TYPE, CommodityForwardSecurity.class, CommodityForwardSecurityBean.class);
  }

  @Override
  public CommodityForwardSecurity createSecurity(final OperationContext context,
                                                 final CommodityForwardSecurityBean bean) {
    CommodityForwardSecurity sec = bean.accept(
      new CommodityForwardSecurityBean.Visitor<CommodityForwardSecurity>() {

        @Override
        public CommodityForwardSecurity visitAgricultureForwardType(AgricultureForwardSecurityBean bean) {
          final AgricultureForwardSecurity security = new AgricultureForwardSecurity(
            bean.getUnitName().getName(),
            bean.getUnitNumber(),
            expiryBeanToExpiry(bean.getExpiry()),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnitNumber(bean.getUnitNumber());
          if (bean.getUnitName() != null) {
            security.setUnitName(bean.getUnitName().getName());
          }
          return security;
        }

        @Override
        public CommodityForwardSecurity visitEnergyForwardType(EnergyForwardSecurityBean bean) {
          final EnergyForwardSecurity security = new EnergyForwardSecurity(
            bean.getUnitName().getName(),
            bean.getUnitNumber(),
            expiryBeanToExpiry(bean.getExpiry()),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnitNumber(bean.getUnitNumber());
          if (bean.getUnitName() != null) {
            security.setUnitName(bean.getUnitName().getName());
          }
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public CommodityForwardSecurity visitMetalForwardType(MetalForwardSecurityBean bean) {
          final MetalForwardSecurity security = new MetalForwardSecurity(
            bean.getUnitName().getName(),
            bean.getUnitNumber(),
            expiryBeanToExpiry(bean.getExpiry()),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnitNumber(bean.getUnitNumber());
          if (bean.getUnitName() != null) {
            security.setUnitName(bean.getUnitName().getName());
          }
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

      });
    return sec;
  }

  @Override
  public CommodityForwardSecurityBean resolve(final OperationContext context,
                                              final HibernateSecurityMasterDao secMasterSession, final Date now,
                                              final CommodityForwardSecurityBean bean) {
    return bean.accept(
      new CommodityForwardSecurityBean.Visitor<CommodityForwardSecurityBean>() {

        @Override
        public CommodityForwardSecurityBean visitAgricultureForwardType(AgricultureForwardSecurityBean bean) {
          return bean;
        }

        @Override
        public CommodityForwardSecurityBean visitEnergyForwardType(EnergyForwardSecurityBean bean) {
          return bean;
        }

        @Override
        public CommodityForwardSecurityBean visitMetalForwardType(MetalForwardSecurityBean bean) {
          return bean;
        }

      });
  }

  @Override
  public void postPersistBean(final OperationContext context,
                              final HibernateSecurityMasterDao secMasterSession, final Date now,
                              final CommodityForwardSecurityBean bean) {
    bean.accept(new CommodityForwardSecurityBean.Visitor<Object>() {

      private void postPersistForward() {
        // No action
      }

      private void postPersistCommodityForward() {
        postPersistForward();
      }

      @Override
      public Object visitAgricultureForwardType(AgricultureForwardSecurityBean bean) {
        postPersistCommodityForward();
        return null;
      }

      @Override
      public Object visitEnergyForwardType(EnergyForwardSecurityBean bean) {
        postPersistCommodityForward();
        return null;
      }

      @Override
      public Object visitMetalForwardType(MetalForwardSecurityBean bean) {
        postPersistCommodityForward();
        return null;
      }

    });
  }

  @Override
  public CommodityForwardSecurityBean createBean(final OperationContext context,
                                                 final HibernateSecurityMasterDao secMasterSession,
                                                 final CommodityForwardSecurity security) {
    return security.accept(new FinancialSecurityVisitorAdapter<CommodityForwardSecurityBean>() {

      private <F extends CommodityForwardSecurityBean> F createForwardBean(final F bean, final CommodityForwardSecurity security) {
        bean.setExpiry(expiryToExpiryBean(security.getExpiry()));

        bean.setCurrency(secMasterSession
          .getOrCreateCurrencyBean(security.getCurrency()
            .getCode()));
        bean.setUnitAmount(security.getUnitAmount());
        bean.setCategory(secMasterSession.getOrCreateContractCategoryBean(security.getContractCategory()));
        return bean;
      }

      private <F extends CommodityForwardSecurityBean> F createCommodityForwardSecurityBean(
        final F commodityForwardSecurityBean, final CommodityForwardSecurity security) {
        final F bean = createForwardBean(commodityForwardSecurityBean, security);
        if (security.getUnitName() != null) {
          bean.setUnitName(secMasterSession
            .getOrCreateUnitNameBean(security.getUnitName()));
        }
        if (security.getUnitNumber() != null) {
          bean.setUnitNumber(security.getUnitNumber());
        }
        return bean;
      }

      @Override
      public AgricultureForwardSecurityBean visitAgricultureForwardSecurity(
        AgricultureForwardSecurity security) {
        return createCommodityForwardSecurityBean(new AgricultureForwardSecurityBean(), security);
      }

      @Override
      public EnergyForwardSecurityBean visitEnergyForwardSecurity(
        EnergyForwardSecurity security) {
        final EnergyForwardSecurityBean bean = createCommodityForwardSecurityBean(new EnergyForwardSecurityBean(), security);
        ExternalId underlying = security.getUnderlyingId();
        if (underlying != null) {
          bean.setUnderlying(externalIdToExternalIdBean(underlying));
        }
        return bean;
      }

      @Override
      public MetalForwardSecurityBean visitMetalForwardSecurity(
        MetalForwardSecurity security) {
        final MetalForwardSecurityBean bean = createCommodityForwardSecurityBean(new MetalForwardSecurityBean(), security);
        ExternalId underlying = security.getUnderlyingId();
        if (underlying != null) {
          bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
        }
        return bean;
      }
    });
  }

}

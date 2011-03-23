/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierBeanToIdentifier;
import static com.opengamma.masterdb.security.hibernate.Converters.identifierToIdentifierBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.CommodityFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Hibernate bean for storage.
 */
public final class FutureSecurityBeanOperation extends AbstractSecurityBeanOperation<FutureSecurity, FutureSecurityBean> {

  /**
   * Singleton.
   * */
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation();
  
  private FutureSecurityBeanOperation() {
    super("FUTURE", FutureSecurity.class, FutureSecurityBean.class);
  }
  
  private static BondFutureDeliverable futureBundleBeanToBondFutureDeliverable(final FutureBundleBean futureBundleBean) {
    final Set<IdentifierBean> identifierBeans = futureBundleBean.getIdentifiers();
    final Set<Identifier> identifiers = new HashSet<Identifier>(identifierBeans.size());
    for (IdentifierBean identifierBean : identifierBeans) {
      identifiers.add(identifierBeanToIdentifier(identifierBean));
    }
    return new BondFutureDeliverable(IdentifierBundle.of(identifiers), futureBundleBean.getConversionFactor());
  }

  @Override
  public FutureSecurity createSecurity(final OperationContext context, final FutureSecurityBean bean) {
    FutureSecurity sec = bean.getFutureType().accept(new FutureType.Visitor<FutureSecurity>() {

      @Override
      public FutureSecurity visitBondFutureType() {
        final Set<FutureBundleBean> basketBeans = bean.getBasket();
        final Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>(basketBeans.size());
        for (FutureBundleBean basketBean : basketBeans) {
          basket.add(futureBundleBeanToBondFutureDeliverable(basketBean));
        }
        return new BondFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(), currencyBeanToCurrency(bean.getCurrency1()),
            basket, bean.getBondType().getName(), zonedDateTimeBeanToDateTimeWithZone(bean.getFirstDeliveryDate()), zonedDateTimeBeanToDateTimeWithZone(bean.getLastDeliveryDate()));
      }

      @Override
      public FutureSecurity visitFXFutureType() {
        final FXFutureSecurity security = new FXFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean
            .getCurrency1()), currencyBeanToCurrency(bean.getCurrency2()), currencyBeanToCurrency(bean.getCurrency3()));
        security.setMultiplicationFactor(bean.getUnitNumber());
        return security;
      }

      @Override
      public FutureSecurity visitInterestRateFutureType() {
        return new InterestRateFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(), currencyBeanToCurrency(bean
            .getCurrency1()), bean.getCashRateType().getName());
      }

      @Override
      public FutureSecurity visitAgricultureFutureType() {
        final AgricultureFutureSecurity security = new AgricultureFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()), bean.getCommodityType().getName());
        security.setUnitNumber(bean.getUnitNumber());
        if (bean.getUnitName() != null) {
          security.setUnitName(bean.getUnitName().getName());
        }
        return security;
      }

      @Override
      public FutureSecurity visitEnergyFutureType() {
        final EnergyFutureSecurity security = new EnergyFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()), bean.getCommodityType().getName());
        security.setUnitNumber(bean.getUnitNumber());
        if (bean.getUnitName() != null) {
          security.setUnitName(bean.getUnitName().getName());
        }
        security.setUnderlyingIdentifier(identifierBeanToIdentifier(bean.getUnderlying()));
        return security;
      }

      @Override
      public FutureSecurity visitMetalFutureType() {
        final MetalFutureSecurity security = new MetalFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()), bean.getCommodityType().getName());
        security.setUnitNumber(bean.getUnitNumber());
        if (bean.getUnitName() != null) {
          security.setUnitName(bean.getUnitName().getName());
        }
        security.setUnderlyingIdentifier(identifierBeanToIdentifier(bean.getUnderlying()));
        return security;
      }

      @Override
      public FutureSecurity visitIndexFutureType() {
        final IndexFutureSecurity security = new IndexFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()));
        security.setUnderlyingIdentifier(identifierBeanToIdentifier(bean.getUnderlying()));
        return security;
      }

      @Override
      public FutureSecurity visitStockFutureType() {
        final StockFutureSecurity security = new StockFutureSecurity(expiryBeanToExpiry(bean.getExpiry()), bean.getTradingExchange().getName(), bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()));
        security.setUnderlyingIdentifier(identifierBeanToIdentifier(bean.getUnderlying()));
        return security;
      }

    });
    return sec;
  }

  @Override
  public FutureSecurityBean resolve(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final Date now, final FutureSecurityBean bean) {
    return bean.getFutureType().accept(new FutureType.Visitor<FutureSecurityBean>() {
      
      private FutureSecurityBean resolveFutureType() {
        return bean;
      }
      
      private FutureSecurityBean resolveCommodityFutureType() {
        return resolveFutureType();
      }

      @Override
      public FutureSecurityBean visitAgricultureFutureType() {
        return resolveCommodityFutureType();
      }

      @Override
      public FutureSecurityBean visitBondFutureType() {
        FutureSecurityBean bean = resolveFutureType();
        final List<FutureBundleBean> basket = secMasterSession.getFutureBundleBeans(now, bean);
        bean.setBasket(new HashSet<FutureBundleBean>(basket));
        return bean;
      }

      @Override
      public FutureSecurityBean visitEnergyFutureType() {
        return resolveCommodityFutureType();
      }

      @Override
      public FutureSecurityBean visitFXFutureType() {
        return resolveFutureType();
      }

      @Override
      public FutureSecurityBean visitIndexFutureType() {
        return resolveFutureType();
      }

      @Override
      public FutureSecurityBean visitInterestRateFutureType() {
        return resolveFutureType();
      }

      @Override
      public FutureSecurityBean visitMetalFutureType() {
        return resolveCommodityFutureType();
      }

      @Override
      public FutureSecurityBean visitStockFutureType() {
        return resolveFutureType();
      }
      
    });
  }
  
  @Override
  public void postPersistBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final Date now, final FutureSecurityBean bean) {
    bean.getFutureType().accept(new FutureType.Visitor<Object>() {
      
      private void postPersistFuture() {
        // No action
      }
      
      private void postPersistCommodityFuture() {
        postPersistFuture();
      }
      
      @Override
      public Object visitAgricultureFutureType() {
        postPersistCommodityFuture();
        return null;
      }
      
      @Override
      public Object visitBondFutureType() {
        postPersistFuture();
        secMasterSession.persistFutureBundleBeans(now, bean);
        return null;
      }

      @Override
      public Object visitEnergyFutureType() {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitFXFutureType() {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitIndexFutureType() {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitInterestRateFutureType() {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitMetalFutureType() {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitStockFutureType() {
        postPersistFuture();
        return null;
      }
    });
  }
  
  @Override
  public boolean beanEquals(final OperationContext context, final FutureSecurityBean bean, final FutureSecurity security) {
    return security.accept(new FutureSecurityVisitor<Boolean>() {
      
      private boolean beanEquals(final FutureSecurity security) {
        return ObjectUtils.equals(bean.getFutureType(), FutureType.identify(security)) && ObjectUtils.equals(expiryBeanToExpiry(bean.getExpiry()), security.getExpiry())
            && ObjectUtils.equals(bean.getTradingExchange().getName(), security.getTradingExchange())
            && ObjectUtils.equals(bean.getSettlementExchange().getName(), security.getSettlementExchange());
      }
      
      private boolean beanEquals(final CommodityFutureSecurity security) {
        return beanEquals((FutureSecurity) security)
            && ObjectUtils.equals(bean.getCommodityType().getName(), security.getCommodityType())
            && ObjectUtils.equals((bean.getUnitName() != null) ? bean.getUnitName().getName() : null, security.getUnitName())
            && ObjectUtils.equals(bean.getUnitNumber(), security.getUnitNumber());
      }

      @Override
      public Boolean visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        return beanEquals(security);
      }

      @Override
      public Boolean visitBondFutureSecurity(BondFutureSecurity security) {
        if (!beanEquals(security)) {
          return false;
        }
        if (!ObjectUtils.equals(bean.getBondType().getName(), security.getBondType())) {
          return false;
        }
        final Collection<BondFutureDeliverable> basket = security.getBasket();
        final Set<FutureBundleBean> beanBasket = bean.getBasket();
        if (basket == null) {
          if (beanBasket != null) {
            return false;
          }
        } else if (beanBasket == null) {
          return false;
        } else {
          if (basket.size() != beanBasket.size()) {
            return false;
          }
          for (FutureBundleBean basketBean : beanBasket) {
            if (!basket.contains(futureBundleBeanToBondFutureDeliverable(basketBean))) {
              return false;
            }
          }
        }
        return true;
      }

      @Override
      public Boolean visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return beanEquals(security) && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentifier());
      }

      @Override
      public Boolean visitFXFutureSecurity(FXFutureSecurity security) {
        return beanEquals(security)
            && ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency1()), security.getCurrency())
            && ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency2()), security.getNumerator())
            && ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency3()), security.getDenominator())
            && ObjectUtils.equals(bean.getUnitNumber(), security.getMultiplicationFactor());
      }

      @Override
      public Boolean visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        return beanEquals(security)
            && ObjectUtils.equals(currencyBeanToCurrency(bean.getCurrency1()), security.getCurrency())
            && ObjectUtils.equals(bean.getCashRateType().getName(), security.getCashRateType());
      }

      @Override
      public Boolean visitMetalFutureSecurity(MetalFutureSecurity security) {
        return beanEquals(security) && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentifier());
      }

      @Override
      public Boolean visitIndexFutureSecurity(IndexFutureSecurity security) {
        return beanEquals(security) && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentifier());
      }

      @Override
      public Boolean visitStockFutureSecurity(StockFutureSecurity security) {
        return beanEquals(security) && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentifier());
      }
      
    });
  }

  @Override
  public FutureSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final FutureSecurity security) {
    return security.accept(new FutureSecurityVisitor<FutureSecurityBean>() {
      
      private FutureSecurityBean createFutureBean(final FutureSecurity security) {
        final FutureSecurityBean bean = new FutureSecurityBean();
        bean.setFutureType(FutureType.identify(security));
        bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
        bean.setTradingExchange(secMasterSession.getOrCreateExchangeBean(security.getTradingExchange(), null));
        bean.setSettlementExchange(secMasterSession.getOrCreateExchangeBean(security.getSettlementExchange(), null));
        bean.setCurrency1(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
        return bean;
      }
      
      private FutureSecurityBean createCommodityFutureBean(final CommodityFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setCommodityType(secMasterSession.getOrCreateCommodityFutureTypeBean(security.getCommodityType()));
        if (security.getUnitName() != null) {
          bean.setUnitName(secMasterSession.getOrCreateUnitNameBean(security.getUnitName()));
        }
        if (security.getUnitNumber() != null) {
          bean.setUnitNumber(security.getUnitNumber());
        }
        return bean;
      }

      @Override
      public FutureSecurityBean visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        final FutureSecurityBean bean = createCommodityFutureBean(security);
        return bean;
      }
      
      @Override
      public FutureSecurityBean visitBondFutureSecurity(
          BondFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setBondType(secMasterSession.getOrCreateBondFutureTypeBean(security.getBondType()));
        bean.setFirstDeliveryDate(dateTimeWithZoneToZonedDateTimeBean(security.getFirstDeliveryDate()));
        bean.setLastDeliveryDate(dateTimeWithZoneToZonedDateTimeBean(security.getLastDeliveryDate()));
        final Collection<BondFutureDeliverable> basket = security.getBasket();
        final Set<FutureBundleBean> basketBeans = new HashSet<FutureBundleBean>(basket.size());
        for (BondFutureDeliverable deliverable : basket) {
          final FutureBundleBean deliverableBean = new FutureBundleBean();
          deliverableBean.setFuture(bean);
          deliverableBean.setConversionFactor(deliverable.getConversionFactor());
          final Set<Identifier> identifiers = deliverable.getIdentifiers().getIdentifiers();
          final Set<IdentifierBean> identifierBeans = new HashSet<IdentifierBean>();
          for (Identifier identifier : identifiers) {
            identifierBeans.add(identifierToIdentifierBean(identifier));
          }
          deliverableBean.setIdentifiers(identifierBeans);
          basketBeans.add(deliverableBean);
        }
        bean.setBasket(basketBeans);
        return bean;
      }

      @Override
      public FutureSecurityBean visitEnergyFutureSecurity(
          EnergyFutureSecurity security) {
        final FutureSecurityBean bean = createCommodityFutureBean(security);
        Identifier underlying = security.getUnderlyingIdentifier();
        if (underlying != null) {
          bean.setUnderlying(identifierToIdentifierBean(underlying));
        }
        return bean;
      }

      @Override
      public FutureSecurityBean visitFXFutureSecurity(FXFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setCurrency2(secMasterSession.getOrCreateCurrencyBean(security.getNumerator().getCode()));
        bean.setCurrency3(secMasterSession.getOrCreateCurrencyBean(security.getDenominator().getCode()));
        bean.setUnitNumber(security.getMultiplicationFactor());
        return bean;
      }

      @Override
      public FutureSecurityBean visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setCashRateType(secMasterSession.getOrCreateCashRateTypeBean(security.getCashRateType()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitMetalFutureSecurity(
          MetalFutureSecurity security) {
        final FutureSecurityBean bean = createCommodityFutureBean(security);
        Identifier underlying = security.getUnderlyingIdentifier();
        if (underlying != null) {
          bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentifier()));
        }
        return bean;
      }

      @Override
      public FutureSecurityBean visitIndexFutureSecurity(IndexFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentifier()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitStockFutureSecurity(StockFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentifier()));
        return bean;
      }
    });
  }

}

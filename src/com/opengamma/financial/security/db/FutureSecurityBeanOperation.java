/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import static com.opengamma.financial.security.db.Converters.currencyBeanToCurrency;
import static com.opengamma.financial.security.db.Converters.dateToExpiry;
import static com.opengamma.financial.security.db.Converters.expiryToDate;
import static com.opengamma.financial.security.db.Converters.futureBundleBeanToBondFutureDeliverable;
import static com.opengamma.financial.security.db.Converters.identifierBeanToIdentifier;
import static com.opengamma.financial.security.db.Converters.identifierToIdentifierBean;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureDeliverable;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.CommodityFutureSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.FutureSecurity;
import com.opengamma.financial.security.FutureSecurityVisitor;
import com.opengamma.financial.security.IndexFutureSecurity;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;
import com.opengamma.financial.security.StockFutureSecurity;
import com.opengamma.id.Identifier;

/* package */ class FutureSecurityBeanOperation extends AbstractBeanOperation<FutureSecurity, FutureSecurityBean> {
  
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation();
  
  private FutureSecurityBeanOperation() {
    super("FUTURE", FutureSecurity.class, FutureSecurityBean.class);
  }
  
  @Override
  public FutureSecurity createSecurity(final FutureSecurityBean bean) {
    FutureSecurity sec = bean.getFutureType().accept(new FutureType.Visitor<FutureSecurity>() {

      @Override
      public FutureSecurity visitBondFutureType() {
        final Set<FutureBundleBean> basketBeans = bean.getBasket();
        final Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>(basketBeans.size());
        if (basketBeans != null) {
          for (FutureBundleBean basketBean : basketBeans) {
            basket.add(futureBundleBeanToBondFutureDeliverable(basketBean));
          }
        }
        return new BondFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            bean.getBondType().getName(),
            basket);
      }

      @Override
      public FutureSecurity visitFXFutureType() {
        return new FXFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            currencyBeanToCurrency(bean.getCurrency2()),
            currencyBeanToCurrency(bean.getCurrency3()),
            bean.getUnitNumber());
      }

      @Override
      public FutureSecurity visitInterestRateFutureType() {
        return new InterestRateFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            bean.getCashRateType().getName());
      }

      @Override
      public FutureSecurity visitAgricultureFutureType() {
        return new AgricultureFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            bean.getCommodityType().getName(),
            bean.getUnitNumber(),
            (bean.getUnitName() != null) ? bean.getUnitName().getName() : null);
      }

      @Override
      public FutureSecurity visitEnergyFutureType() {
        return new EnergyFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            bean.getCommodityType().getName(),
            bean.getUnitNumber(),
           (bean.getUnitName() != null) ? bean.getUnitName().getName() : null,
            identifierBeanToIdentifier(bean.getUnderlying()));
      }

      @Override
      public FutureSecurity visitMetalFutureType() {
        return new MetalFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            bean.getCommodityType().getName(),
            bean.getUnitNumber(),
           (bean.getUnitName() != null) ? bean.getUnitName().getName() : null,               
            identifierBeanToIdentifier(bean.getUnderlying()));
      }

      @Override
      public FutureSecurity visitIndexFutureType() {
        return new IndexFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            identifierBeanToIdentifier(bean.getUnderlying()));
      }

      @Override
      public FutureSecurity visitStockFutureType() {
        return new StockFutureSecurity(
            dateToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency1()),
            identifierBeanToIdentifier(bean.getUnderlying()));
      }

    });
    return sec;
  }

  @Override
  public FutureSecurityBean resolve(final HibernateSecurityMasterSession secMasterSession, final Date now, final FutureSecurityBean bean) {
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
  public void postPersistBean(final HibernateSecurityMasterSession secMasterSession, final Date now, final FutureSecurityBean bean) {
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
  public boolean beanEquals(final FutureSecurityBean bean, final FutureSecurity security) {
    return security.accept(new FutureSecurityVisitor<Boolean>() {
      
      private boolean beanEquals(final FutureSecurity security) {
        return ObjectUtils.equals(bean.getFutureType(), FutureType.identify(security))
            && ObjectUtils.equals(dateToExpiry(bean.getExpiry()), security.getExpiry())
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
        final Set<BondFutureDeliverable> basket = security.getBasket();
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
        return beanEquals(security)
            && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentityKey());
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
        return beanEquals(security)
            && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentityKey());
      }

      @Override
      public Boolean visitIndexFutureSecurity(IndexFutureSecurity security) {
        return beanEquals(security)
            && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentityKey());
      }

      @Override
      public Boolean visitStockFutureSecurity(StockFutureSecurity security) {
        return beanEquals(security)
          && ObjectUtils.equals(identifierBeanToIdentifier(bean.getUnderlying()), security.getUnderlyingIdentityKey());
      }
      
    });
  }

  @Override
  public FutureSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final FutureSecurity security) {
    return security.accept(new FutureSecurityVisitor<FutureSecurityBean>() {
      
      private FutureSecurityBean createFutureBean(final FutureSecurity security) {
        final FutureSecurityBean bean = new FutureSecurityBean();
        bean.setFutureType(FutureType.identify(security));
        bean.setExpiry(expiryToDate(security.getExpiry()));
        bean.setTradingExchange(secMasterSession.getOrCreateExchangeBean(security.getTradingExchange(), null));
        bean.setSettlementExchange(secMasterSession.getOrCreateExchangeBean(security.getSettlementExchange(), null));
        bean.setCurrency1(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getISOCode()));
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
        final Set<BondFutureDeliverable> basket = security.getBasket();
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
        bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentityKey()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitFXFutureSecurity(FXFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setCurrency2(secMasterSession.getOrCreateCurrencyBean(security.getNumerator().getISOCode()));
        bean.setCurrency3(secMasterSession.getOrCreateCurrencyBean(security.getDenominator().getISOCode()));
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
        bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentityKey()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitIndexFutureSecurity(IndexFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentityKey()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitStockFutureSecurity(StockFutureSecurity security) {
        final FutureSecurityBean bean = createFutureBean(security);
        bean.setUnderlying(identifierToIdentifierBean(security.getUnderlyingIdentityKey()));
        return bean;
      }
    });
  }

}

/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.security.AgricultureFutureSecurity;
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
import com.opengamma.id.DomainSpecificIdentifier;

/* package */ class FutureSecurityBeanOperation extends Converters implements BeanOperation<FutureSecurity,FutureSecurityBean> {
  
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation ();
  
  private FutureSecurityBeanOperation () {
  }
  
  @Override
  public FutureSecurity createSecurity (final DomainSpecificIdentifier identifier, final FutureSecurityBean bean) {
    return bean.getFutureType ().accept (new FutureType.Visitor<FutureSecurity> () {

      @Override
      public FutureSecurity visitBondFutureType() {
        final Set<FutureBasketAssociationBean> basketBeans = bean.getBasket ();
        final Set<DomainSpecificIdentifier> basket = new HashSet<DomainSpecificIdentifier> (basketBeans.size ());
        if (basketBeans != null) {
          for (FutureBasketAssociationBean basketBean : basketBeans) {
            basket.add (domainSpecificIdentifierBeanToDomainSpecificIdentifier (basketBean.getDomainSpecificIdentifier ()));
          }
        }
        return new BondFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            bean.getBondType ().getName (),
            basket
            );
      }

      @Override
      public FutureSecurity visitCurrencyFutureType() {
        return new FXFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            currencyBeanToCurrency (bean.getCurrency2 ()),
            bean.getUnitNumber ()
            );
      }

      @Override
      public FutureSecurity visitInterestRateFutureType() {
        return new InterestRateFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            currencyBeanToCurrency (bean.getCurrency1 ()),
            bean.getCashRateType ().getName ()
            );
      }

      @Override
      public FutureSecurity visitAgricultureFutureType() {
        return new AgricultureFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            bean.getCommodityType ().getName (),
            bean.getUnitNumber (),
            (bean.getUnitName () != null) ? bean.getUnitName ().getName () : null
            );
      }

      @Override
      public FutureSecurity visitEnergyFutureType() {
        return new EnergyFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            bean.getCommodityType ().getName (),
            bean.getUnitNumber (),
            (bean.getUnitName () != null) ? bean.getUnitName ().getName () : null,
            null);
        //TODO added null as the underlying identifier because it isn't in the bean
      }

      @Override
      public FutureSecurity visitMetalFutureType() {
        return new MetalFutureSecurity (
            dateToExpiry (bean.getExpiry ()),
            bean.getTradingExchange ().getName (),
            bean.getSettlementExchange ().getName (),
            bean.getCommodityType ().getName (),
            bean.getUnitNumber (),
            (bean.getUnitName () != null) ? bean.getUnitName ().getName () : null,               
            null);
        //TODO added null as the underlying identifier because it isn't in the bean
      }

      @Override
      public FutureSecurity visitIndexFutureType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public FutureSecurity visitStockFutureType() {
        // TODO Auto-generated method stub
        return null;
      }

    });
  }

  @Override
  public boolean beanEquals(final FutureSecurityBean bean, final FutureSecurity security) {
    return security.accept (new FutureSecurityVisitor<Boolean> () {
      
      private boolean beanEquals (final FutureSecurity security) {
        return ObjectUtils.equals (bean.getFutureType (), FutureType.identify (security))
            && ObjectUtils.equals (dateToExpiry(bean.getExpiry ()), security.getExpiry ())
            && ObjectUtils.equals (bean.getTradingExchange ().getName (), security.getTradingExchange ())
            && ObjectUtils.equals (bean.getSettlementExchange ().getName (), security.getSettlementExchange ());
      }
      
      private boolean beanEquals (final CommodityFutureSecurity security) {
        return beanEquals ((FutureSecurity)security)
            && ObjectUtils.equals (bean.getCommodityType ().getName (), security.getCommodityType ())
            && ObjectUtils.equals ((bean.getUnitName () != null) ? bean.getUnitName ().getName () : null, security.getUnitName ())
            && ObjectUtils.equals (bean.getUnitNumber (), security.getUnitNumber ());
      }

      @Override
      public Boolean visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        return beanEquals (security);
      }

      @Override
      public Boolean visitBondFutureSecurity(BondFutureSecurity security) {
        if (!beanEquals (security)) return false;
        if (!ObjectUtils.equals (bean.getBondType ().getName (), security.getBondType ())) return false;
        final Set<DomainSpecificIdentifier> basket = security.getBasket ();
        final Set<FutureBasketAssociationBean> beanBasket = bean.getBasket ();
        if (basket == null) {
          if (beanBasket != null) {
            return false;
          }
        } else if (beanBasket == null) {
          return false;
        } else {
          if (basket.size () != beanBasket.size ()) return false;
          for (FutureBasketAssociationBean basketBean : beanBasket) {
            if (!basket.contains (domainSpecificIdentifierBeanToDomainSpecificIdentifier (basketBean.getDomainSpecificIdentifier ()))) return false;
          }
        }
        return true;
      }

      @Override
      public Boolean visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return beanEquals (security);
      }

      @Override
      public Boolean visitFXFutureSecurity(FXFutureSecurity security) {
        return beanEquals (security)
            && ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency1 ()), security.getNumerator ())
            && ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency2 ()), security.getDenominator ())
            && ObjectUtils.equals (bean.getUnitNumber (), security.getMultiplicationFactor ());
      }

      @Override
      public Boolean visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        return beanEquals (security)
            && ObjectUtils.equals (currencyBeanToCurrency (bean.getCurrency1 ()), security.getCurrency ())
            && ObjectUtils.equals (bean.getCashRateType ().getName (), security.getCashRateType ());
      }

      @Override
      public Boolean visitMetalFutureSecurity(MetalFutureSecurity security) {
        return beanEquals (security);
      }

      @Override
      public Boolean visitIndexFutureSecurity(IndexFutureSecurity security) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Boolean visitStockFutureSecurity(StockFutureSecurity security) {
        // TODO Auto-generated method stub
        return null;
      }
      
    });
  }

  @Override
  public FutureSecurityBean createBean(final HibernateSecurityMasterSession secMasterSession, final FutureSecurity security) {
    return security.accept (new FutureSecurityVisitor<FutureSecurityBean> () {
      
      private FutureSecurityBean createBean (final FutureSecurity security) {
        return new FutureSecurityBean (
            FutureType.identify (security),
            expiryToDate (security.getExpiry ()),
            secMasterSession.getOrCreateExchangeBean (security.getTradingExchange (), null),
            secMasterSession.getOrCreateExchangeBean (security.getSettlementExchange (), null));
      }
      
      private FutureSecurityBean createBean (final CommodityFutureSecurity security) {
        final FutureSecurityBean bean = createBean ((FutureSecurity)security);
        bean.setCommodityType (secMasterSession.getOrCreateCommodityFutureTypeBean (security.getCommodityType ()));
        if (security.getUnitName () != null) {
          bean.setUnitName (secMasterSession.getOrCreateUnitNameBean (security.getUnitName ()));
        }
        if (security.getUnitNumber () != null) {
          bean.setUnitNumber (security.getUnitNumber ());
        }
        return bean;
      }

      @Override
      public FutureSecurityBean visitAgricultureFutureSecurity(
          AgricultureFutureSecurity security) {
        return createBean (security);
      }

      @Override
      public FutureSecurityBean visitBondFutureSecurity(
          BondFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setBondType (secMasterSession.getOrCreateBondFutureTypeBean (security.getBondType ()));
        final Set<DomainSpecificIdentifier> basket = security.getBasket ();
        final Set<FutureBasketAssociationBean> basketBeans = new HashSet<FutureBasketAssociationBean> (basket.size ());
        for (DomainSpecificIdentifier identifier : basket) {
          basketBeans.add (new FutureBasketAssociationBean (bean, new DomainSpecificIdentifierBean (identifier.getDomain ().getDomainName (), identifier.getValue ())));
        }
        bean.setBasket (basketBeans);
        return bean;
      }

      @Override
      public FutureSecurityBean visitEnergyFutureSecurity(
          EnergyFutureSecurity security) {
        return createBean (security);
      }

      @Override
      public FutureSecurityBean visitFXFutureSecurity(FXFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setCurrency1 (secMasterSession.getOrCreateCurrencyBean (security.getNumerator ().getISOCode ()));
        bean.setCurrency2 (secMasterSession.getOrCreateCurrencyBean (security.getDenominator ().getISOCode ()));
        bean.setUnitNumber (security.getMultiplicationFactor ());
        return bean;
      }

      @Override
      public FutureSecurityBean visitInterestRateFutureSecurity(
          InterestRateFutureSecurity security) {
        final FutureSecurityBean bean = createBean (security);
        bean.setCurrency1 (secMasterSession.getOrCreateCurrencyBean (security.getCurrency ().getISOCode ()));
        bean.setCashRateType (secMasterSession.getOrCreateCashRateTypeBean (security.getCashRateType ()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitMetalFutureSecurity(
          MetalFutureSecurity security) {
        return createBean (security);
      }

      @Override
      public FutureSecurityBean visitIndexFutureSecurity(IndexFutureSecurity security) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public FutureSecurityBean visitStockFutureSecurity(StockFutureSecurity security) {
        // TODO Auto-generated method stub
        return null;
      }
    });
  }

  @Override
  public Class<? extends FutureSecurityBean> getBeanClass() {
    return FutureSecurityBean.class;
  }

  @Override
  public Class<? extends FutureSecurity> getSecurityClass() {
    return FutureSecurity.class;
  }

  @Override
  public String getSecurityType() {
    return "FUTURE";
  }
  
}
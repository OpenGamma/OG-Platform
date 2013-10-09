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
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.CommodityFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * A Hibernate bean for storage.
 */
public final class FutureSecurityBeanOperation
    extends AbstractSecurityBeanOperation<FutureSecurity, FutureSecurityBean> {

  /**
   * Singleton.
   * */
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation();

  private FutureSecurityBeanOperation() {
    super(FutureSecurity.SECURITY_TYPE, FutureSecurity.class, FutureSecurityBean.class);
  }

  private static BondFutureDeliverable futureBundleBeanToBondFutureDeliverable(
    final FutureBundleBean futureBundleBean) {
    final Set<ExternalIdBean> identifierBeans = futureBundleBean
      .getIdentifiers();
    final Set<ExternalId> identifiers = new HashSet<ExternalId>(
      identifierBeans.size());
    for (ExternalIdBean identifierBean : identifierBeans) {
      identifiers.add(externalIdBeanToExternalId(identifierBean));
    }
    return new BondFutureDeliverable(ExternalIdBundle.of(identifiers),
      futureBundleBean.getConversionFactor());
  }

  @Override
  public FutureSecurity createSecurity(final OperationContext context,
                                       final FutureSecurityBean bean) {
    FutureSecurity sec = bean.accept(
      new FutureSecurityBean.Visitor<FutureSecurity>() {

        @Override
        public FutureSecurity visitBondFutureType(BondFutureBean bean) {
          final Set<FutureBundleBean> basketBeans = bean
            .getBasket();
          final Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>(
            basketBeans.size());
          for (FutureBundleBean basketBean : basketBeans) {
            basket.add(futureBundleBeanToBondFutureDeliverable(basketBean));
          }
          return new BondFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            basket,
            zonedDateTimeBeanToDateTimeWithZone(bean.getFirstDeliveryDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getLastDeliveryDate()),
            bean.getCategory().getName()
          );
        }

        @Override
        public FutureSecurity visitFXFutureType(ForeignExchangeFutureBean bean) {
          final FXFutureSecurity security = new FXFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()), bean
            .getTradingExchange().getName(), bean
            .getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            currencyBeanToCurrency(bean.getNumerator()),
            currencyBeanToCurrency(bean.getDenominator()),
            bean.getCategory().getName());
          security.setMultiplicationFactor(bean.getUnitNumber());
          return security;
        }

        @Override
        public FutureSecurity visitInterestRateFutureType(InterestRateFutureBean bean) {
          return new InterestRateFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            externalIdBeanToExternalId(bean.getUnderlying()),
            bean.getCategory().getName());
        }

        @Override
        public FutureSecurity visitAgricultureFutureType(AgricultureFutureBean bean) {
          final AgricultureFutureSecurity security = new AgricultureFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
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
        public FutureSecurity visitEnergyFutureType(EnergyFutureBean bean) {
          final EnergyFutureSecurity security = new EnergyFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
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
        public FutureSecurity visitMetalFutureType(MetalFutureBean bean) {
          final MetalFutureSecurity security = new MetalFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
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
        public FutureSecurity visitIndexFutureType(IndexFutureBean bean) {
          final IndexFutureSecurity security = new IndexFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public FutureSecurity visitStockFutureType(StockFutureBean bean) {
          final StockFutureSecurity security = new StockFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public FutureSecurity visitEquityFutureType(EquityFutureBean bean) {
          final EquityFutureSecurity security = new EquityFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            expiryBeanToExpiry(bean.getExpiry()).getExpiry(), // TODO: this is a temporary hack as settlementDate isn't being stored in database
            externalIdBeanToExternalId(bean.getUnderlying()),
            bean.getCategory().getName());
          return security;
        }

        @Override
        public FutureSecurity visitEquityIndexDividendFutureType(EquityIndexDividendFutureBean bean) {
          final EquityIndexDividendFutureSecurity security = new EquityIndexDividendFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            expiryBeanToExpiry(bean.getExpiry()).getExpiry(), // TODO: this is a temporary hack as settlementDate isn't being stored in database
            externalIdBeanToExternalId(bean.getUnderlying()),
            bean.getCategory().getName());
          return security;
        }

        @Override
        public FutureSecurity visitFederalFundsFutureType(FederalFundsFutureBean bean) {
          final FederalFundsFutureSecurity security = new FederalFundsFutureSecurity(
              expiryBeanToExpiry(bean.getExpiry()),
              bean.getTradingExchange().getName(),
              bean.getSettlementExchange().getName(),
              currencyBeanToCurrency(bean.getCurrency()),
              bean.getUnitAmount(),
              externalIdBeanToExternalId(bean.getUnderlying()),
              bean.getCategory().getName());
          return security;
        }
      });
    return sec;
  }

  @Override
  public FutureSecurityBean resolve(final OperationContext context,
                                    final HibernateSecurityMasterDao secMasterSession, final Date now,
                                    final FutureSecurityBean bean) {
    return bean.accept(
      new FutureSecurityBean.Visitor<FutureSecurityBean>() {

        @Override
        public FutureSecurityBean visitAgricultureFutureType(AgricultureFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitBondFutureType(BondFutureBean bean) {
          final List<FutureBundleBean> basket = secMasterSession.getFutureBundleBeans(now, bean);
          bean.setBasket(new HashSet<FutureBundleBean>(basket));
          return bean;
        }

        @Override
        public FutureSecurityBean visitEnergyFutureType(EnergyFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitFXFutureType(ForeignExchangeFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitIndexFutureType(IndexFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitInterestRateFutureType(InterestRateFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitMetalFutureType(MetalFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitStockFutureType(StockFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitEquityFutureType(EquityFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitEquityIndexDividendFutureType(EquityIndexDividendFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitFederalFundsFutureType(FederalFundsFutureBean bean) {
          return bean;
        }
      });
  }

  @Override
  public void postPersistBean(final OperationContext context,
                              final HibernateSecurityMasterDao secMasterSession, final Date now,
                              final FutureSecurityBean bean) {
    bean.accept(new FutureSecurityBean.Visitor<Object>() {

      private void postPersistFuture() {
        // No action
      }

      private void postPersistCommodityFuture() {
        postPersistFuture();
      }

      @Override
      public Object visitAgricultureFutureType(AgricultureFutureBean bean) {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitBondFutureType(BondFutureBean bean) {
        postPersistFuture();
        secMasterSession.persistFutureBundleBeans(now, bean);
        return null;
      }

      @Override
      public Object visitEnergyFutureType(EnergyFutureBean bean) {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitEquityFutureType(EquityFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitEquityIndexDividendFutureType(EquityIndexDividendFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitFXFutureType(ForeignExchangeFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitIndexFutureType(IndexFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitInterestRateFutureType(InterestRateFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitMetalFutureType(MetalFutureBean bean) {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitStockFutureType(StockFutureBean bean) {
        postPersistFuture();
        return null;
      }
      
      @Override
      public Object visitFederalFundsFutureType(FederalFundsFutureBean bean) {
        postPersistFuture();
        return null;
      }
    });
  }

  @Override
  public FutureSecurityBean createBean(final OperationContext context,
                                       final HibernateSecurityMasterDao secMasterSession,
                                       final FutureSecurity security) {
    return security.accept(new FinancialSecurityVisitorAdapter<FutureSecurityBean>() {

      private <F extends FutureSecurityBean> F createFutureBean(final F bean, final FutureSecurity security) {
        bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
        bean.setTradingExchange(secMasterSession
          .getOrCreateExchangeBean(security.getTradingExchange(),
            null));
        bean.setSettlementExchange(secMasterSession
          .getOrCreateExchangeBean(
            security.getSettlementExchange(), null));
        bean.setCurrency(secMasterSession
          .getOrCreateCurrencyBean(security.getCurrency()
            .getCode()));
        bean.setUnitAmount(security.getUnitAmount());
        bean.setCategory(secMasterSession.getOrCreateContractCategoryBean(security.getContractCategory()));
        return bean;
      }

      private <F extends CommodityFutureBean> F createCommodityFutureBean(
        final F futureSecurityBean, final CommodityFutureSecurity security) {
        final F bean = createFutureBean(futureSecurityBean, security);
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
      public AgricultureFutureBean visitAgricultureFutureSecurity(
        AgricultureFutureSecurity security) {
        return createCommodityFutureBean(new AgricultureFutureBean(), security);
      }

      @Override
      public BondFutureBean visitBondFutureSecurity(
        BondFutureSecurity security) {
        final BondFutureBean bean = createFutureBean(new BondFutureBean(), security);
        bean.setFirstDeliveryDate(dateTimeWithZoneToZonedDateTimeBean(security
          .getFirstDeliveryDate()));
        bean.setLastDeliveryDate(dateTimeWithZoneToZonedDateTimeBean(security
          .getLastDeliveryDate()));
        final Collection<BondFutureDeliverable> basket = security
          .getBasket();
        final Set<FutureBundleBean> basketBeans = new HashSet<FutureBundleBean>(
          basket.size());
        for (BondFutureDeliverable deliverable : basket) {
          final FutureBundleBean deliverableBean = new FutureBundleBean();
          deliverableBean.setFuture(bean);
          deliverableBean.setConversionFactor(deliverable
            .getConversionFactor());
          final Set<ExternalId> identifiers = deliverable
            .getIdentifiers().getExternalIds();
          final Set<ExternalIdBean> identifierBeans = new HashSet<ExternalIdBean>();
          for (ExternalId identifier : identifiers) {
            identifierBeans
              .add(externalIdToExternalIdBean(identifier));
          }
          deliverableBean.setIdentifiers(identifierBeans);
          basketBeans.add(deliverableBean);
        }
        bean.setBasket(basketBeans);
        return bean;
      }

      @Override
      public EnergyFutureBean visitEnergyFutureSecurity(
        EnergyFutureSecurity security) {
        final EnergyFutureBean bean = createCommodityFutureBean(new EnergyFutureBean(), security);
        ExternalId underlying = security.getUnderlyingId();
        if (underlying != null) {
          bean.setUnderlying(externalIdToExternalIdBean(underlying));
        }
        return bean;
      }

      @Override
      public ForeignExchangeFutureBean visitFXFutureSecurity(
        FXFutureSecurity security) {
        final ForeignExchangeFutureBean bean = createFutureBean(new ForeignExchangeFutureBean(), security);
        bean.setNumerator(secMasterSession
          .getOrCreateCurrencyBean(security.getNumerator()
            .getCode()));
        bean.setDenominator(secMasterSession
          .getOrCreateCurrencyBean(security.getDenominator()
            .getCode()));
        bean.setUnitNumber(security.getMultiplicationFactor());
        return bean;
      }

      @Override
      public InterestRateFutureBean visitInterestRateFutureSecurity(
        InterestRateFutureSecurity security) {
        final InterestRateFutureBean bean = createFutureBean(new InterestRateFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
        return bean;
      }

      @Override
      public MetalFutureBean visitMetalFutureSecurity(
        MetalFutureSecurity security) {
        final MetalFutureBean bean = createCommodityFutureBean(new MetalFutureBean(), security);
        ExternalId underlying = security.getUnderlyingId();
        if (underlying != null) {
          bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
        }
        return bean;
      }

      @Override
      public IndexFutureBean visitIndexFutureSecurity(
        IndexFutureSecurity security) {
        final IndexFutureBean bean = createFutureBean(new IndexFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public StockFutureBean visitStockFutureSecurity(
        StockFutureSecurity security) {
        final StockFutureBean bean = createFutureBean(new StockFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public EquityFutureBean visitEquityFutureSecurity(
        EquityFutureSecurity security) {
        // TODO Case: Confirm this add is correct
        final EquityFutureBean bean = createFutureBean(new EquityFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public EquityIndexDividendFutureBean visitEquityIndexDividendFutureSecurity(
        EquityIndexDividendFutureSecurity security) {
        // TODO Case: Confirm this add is correct
        final EquityIndexDividendFutureBean bean = createFutureBean(new EquityIndexDividendFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }
      
      @Override
      public FutureSecurityBean visitFederalFundsFutureSecurity(FederalFundsFutureSecurity security) {
        final FederalFundsFutureBean bean = createFutureBean(new FederalFundsFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
            .getUnderlyingId()));
        return bean;
      }
    });
  }

}

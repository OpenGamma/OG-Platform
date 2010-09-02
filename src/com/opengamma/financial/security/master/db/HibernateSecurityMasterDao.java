/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import java.util.Date;
import java.util.List;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.financial.security.db.BusinessDayConventionBean;
import com.opengamma.financial.security.db.CurrencyBean;
import com.opengamma.financial.security.db.DayCountBean;
import com.opengamma.financial.security.db.ExchangeBean;
import com.opengamma.financial.security.db.FrequencyBean;
import com.opengamma.financial.security.db.IdentifierAssociationBean;
import com.opengamma.financial.security.db.OperationContext;
import com.opengamma.financial.security.db.SecurityBean;
import com.opengamma.financial.security.db.SecurityBeanOperation;
import com.opengamma.financial.security.db.bond.CouponTypeBean;
import com.opengamma.financial.security.db.bond.GuaranteeTypeBean;
import com.opengamma.financial.security.db.bond.IssuerTypeBean;
import com.opengamma.financial.security.db.bond.MarketBean;
import com.opengamma.financial.security.db.bond.YieldConventionBean;
import com.opengamma.financial.security.db.equity.GICSCodeBean;
import com.opengamma.financial.security.db.future.BondFutureTypeBean;
import com.opengamma.financial.security.db.future.CashRateTypeBean;
import com.opengamma.financial.security.db.future.CommodityFutureTypeBean;
import com.opengamma.financial.security.db.future.FutureBundleBean;
import com.opengamma.financial.security.db.future.FutureSecurityBean;
import com.opengamma.financial.security.db.future.UnitBean;
import com.opengamma.id.Identifier;

/**
 * HibernateSecurityMaster session and utility methods
 */
public interface HibernateSecurityMasterDao {

  // Main security load/save
  SecurityBean getSecurityBean(DefaultSecurity base);

  <S extends DefaultSecurity, SBean extends SecurityBean> SBean createSecurityBean(
      OperationContext context, SecurityBeanOperation<S, SBean> beanOperation, Date effectiveDateTime, S security);

  SecurityBean persistSecurityBean(OperationContext context, final SecurityBean bean);

  // SESSION LEVEL METHODS
  // Exchanges
  ExchangeBean getOrCreateExchangeBean(String name, String description);

  List<ExchangeBean> getExchangeBeans();

  // Currencies
  CurrencyBean getOrCreateCurrencyBean(String name);

  List<CurrencyBean> getCurrencyBeans();

  // GICS codes
  GICSCodeBean getOrCreateGICSCodeBean(final String name, final String description);

  List<GICSCodeBean> getGICSCodeBeans();

  // Daycount conventions
  DayCountBean getOrCreateDayCountBean(final String convention);

  List<DayCountBean> getDayCountBeans();

  // Business day conventions
  BusinessDayConventionBean getOrCreateBusinessDayConventionBean(final String convention);

  List<BusinessDayConventionBean> getBusinessDayConventionBeans();

  // Frequencies
  FrequencyBean getOrCreateFrequencyBean(final String convention);

  List<FrequencyBean> getFrequencyBeans();

  // CommodityFutureTypes
  CommodityFutureTypeBean getOrCreateCommodityFutureTypeBean(final String type);
  
  List<CommodityFutureTypeBean> getCommodityFutureTypeBeans();

  // BondFutureType
  BondFutureTypeBean getOrCreateBondFutureTypeBean(final String type);
  
  List<BondFutureTypeBean> getBondFutureTypeBeans();

  // UnitName
  UnitBean getOrCreateUnitNameBean(final String unitName);
  
  List<UnitBean> getUnitNameBeans();

  // CashRateType
  CashRateTypeBean getOrCreateCashRateTypeBean(final String type);
  
  List<CashRateTypeBean> getCashRateTypeBeans();

  // IssuerTypeBean
  IssuerTypeBean getOrCreateIssuerTypeBean(final String type);
  
  List<IssuerTypeBean> getIssuerTypeBeans();

  // MarketBean
  MarketBean getOrCreateMarketBean(final String market);
  
  List<MarketBean> getMarketBeans();

  // YieldConventionBean
  YieldConventionBean getOrCreateYieldConventionBean(final String convention);
  
  List<YieldConventionBean> getYieldConventionBeans();

  // GuaranteeTypeBean
  GuaranteeTypeBean getOrCreateGuaranteeTypeBean(final String type);
  
  List<GuaranteeTypeBean> getGuaranteeTypeBeans();

  // CouponTypeBean
  CouponTypeBean getOrCreateCouponTypeBean(final String type);
  
  List<CouponTypeBean> getCouponTypeBeans();

  IdentifierAssociationBean getCreateOrUpdateIdentifierAssociationBean(Date now, String scheme,
      String identifier, SecurityBean security);

  void associateOrUpdateIdentifierWithSecurity(Date now, Identifier identifier, SecurityBean security);

  // Debug/testing
  <T extends SecurityBean> List<T> getAllSecurityBeans(Class<T> beanClass);

  // Helpers for Futures

  List<FutureBundleBean> getFutureBundleBeans(Date now, FutureSecurityBean future);

  FutureBundleBean nextFutureBundleBean(Date now, FutureSecurityBean future);

  void persistFutureBundleBeans(final Date now, final FutureSecurityBean future);

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;
import java.util.List;

import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * HibernateSecurityMaster session and utility methods
 *
 * 
 */
public interface HibernateSecurityMasterDao {

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

  // Generic Securities
  SecurityBean getSecurityBean(final UniqueIdentifier uid);

  SecurityBean getSecurityBean(Date now, final UniqueIdentifier uid);

  SecurityBean getSecurityBean(Date now, IdentifierBundle bundle);

  // Specific securities through BeanOperation
  <S extends Security, SBean extends SecurityBean> SBean createSecurityBean(
      final BeanOperation<S, SBean> beanOperation, final Date effectiveDateTime, final boolean deleted,
      final Date lastModified, final String modifiedBy, final SBean firstVersion, final S security);

  SecurityBean persistSecurityBean(final SecurityBean bean);

  // Equities
  // Internal query methods for equities
  List<EquitySecurityBean> getEquitySecurityBeans();

  List<EquitySecurityBean> getAllVersionsOfEquitySecurityBean(EquitySecurityBean firstVersion);

  EquitySecurityBean getCurrentEquitySecurityBean(Date now, ExchangeBean exchange, String companyName,
      CurrencyBean currency);

  EquitySecurityBean getCurrentEquitySecurityBean(Date now, EquitySecurityBean firstVersion);

  EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now, ExchangeBean exchange,
      String companyName, CurrencyBean currency);

  EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now, EquitySecurityBean firstVersion);

  // Equity options
  List<OptionSecurityBean> getEquityOptionSecurityBeans();
  
  //Options
  List<OptionSecurityBean> getOptionSecurityBeans();

  List<FutureBundleBean> getFutureBundleBeans(Date now, FutureSecurityBean future);

  FutureBundleBean nextFutureBundleBean(Date now, FutureSecurityBean future);

  void persistFutureBundleBeans(final Date now, final FutureSecurityBean future);

}

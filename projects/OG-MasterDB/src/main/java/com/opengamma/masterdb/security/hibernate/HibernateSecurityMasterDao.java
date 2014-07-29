/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Date;
import java.util.List;

import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.masterdb.security.hibernate.bond.CouponTypeBean;
import com.opengamma.masterdb.security.hibernate.bond.GuaranteeTypeBean;
import com.opengamma.masterdb.security.hibernate.bond.IssuerTypeBean;
import com.opengamma.masterdb.security.hibernate.bond.MarketBean;
import com.opengamma.masterdb.security.hibernate.bond.YieldConventionBean;
import com.opengamma.masterdb.security.hibernate.equity.GICSCodeBean;
import com.opengamma.masterdb.security.hibernate.future.FutureBundleBean;
import com.opengamma.masterdb.security.hibernate.future.FutureSecurityBean;
import com.opengamma.masterdb.security.hibernate.index.BondIndexBean;
import com.opengamma.masterdb.security.hibernate.index.BondIndexComponentBean;
import com.opengamma.masterdb.security.hibernate.index.EquityIndexBean;
import com.opengamma.masterdb.security.hibernate.index.EquityIndexComponentBean;

/**
 * HibernateSecurityMaster session and utility methods.
 */
public interface HibernateSecurityMasterDao {

  // Main security load/save
  SecurityBean getSecurityBean(ManageableSecurity base, SecurityBeanOperation<?, ?> beanOperation);

  <S extends ManageableSecurity, SBean extends SecurityBean> SBean createSecurityBean(
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

  // UnitName
  UnitBean getOrCreateUnitNameBean(final String unitName);

  List<UnitBean> getUnitNameBeans();

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

  void associateOrUpdateExternalIdWithSecurity(Date now, ExternalId externalId, SecurityBean security);

  // Debug/testing
  <T extends SecurityBean> List<T> getAllSecurityBeans(Class<T> beanClass);

  // Helpers for Futures

  List<FutureBundleBean> getFutureBundleBeans(Date now, FutureSecurityBean future);

  FutureBundleBean nextFutureBundleBean(Date now, FutureSecurityBean future);

  void persistFutureBundleBeans(final Date now, final FutureSecurityBean future);

  ContractCategoryBean getOrCreateContractCategoryBean(String name);

  StubTypeBean getOrCreateStubTypeBean(String name);

  DebtSeniorityBean getOrCreateDebtSeniorityBean(String name);

  RestructuringClauseBean getOrCreateRestructuringCleanBean(String name);

  // CDSI family
  CDSIndexFamilyBean getOrCreateCDSIFamilyBean(String family);
  
  List<CDSIndexFamilyBean> getCDSIFamilyBeans();
  
  //Tenors
  TenorBean getOrCreateTenorBean(String tenor);
  
  List<TenorBean> getTenorBeans();

  IndexWeightingTypeBean getOrCreateIndexWeightingTypeBean(String name);

  List<IndexWeightingTypeBean> getIndexWeightingTypeBeans();

  List<BondIndexComponentBean> getBondIndexComponentBeans(BondIndexBean bondIndex);

  List<EquityIndexComponentBean> getEquityIndexComponentBeans(EquityIndexBean equityIndex);

  void persistBondIndexComponentBeans(BondIndexBean bondIndex);

  void persistEquityIndexComponentBeans(EquityIndexBean equityIndex);
  
}

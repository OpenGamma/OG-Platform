/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.masterdb.security.hibernate.bond.BondSecurityBean;
import com.opengamma.masterdb.security.hibernate.bond.CouponTypeBean;
import com.opengamma.masterdb.security.hibernate.bond.GuaranteeTypeBean;
import com.opengamma.masterdb.security.hibernate.bond.IssuerTypeBean;
import com.opengamma.masterdb.security.hibernate.bond.MarketBean;
import com.opengamma.masterdb.security.hibernate.bond.YieldConventionBean;
import com.opengamma.masterdb.security.hibernate.capfloor.CapFloorCMSSpreadSecurityBean;
import com.opengamma.masterdb.security.hibernate.capfloor.CapFloorSecurityBean;
import com.opengamma.masterdb.security.hibernate.cash.CashSecurityBean;
import com.opengamma.masterdb.security.hibernate.cashflow.CashFlowSecurityBean;
import com.opengamma.masterdb.security.hibernate.cds.CDSSecurityBean;
import com.opengamma.masterdb.security.hibernate.cds.CreditDefaultSwapIndexDefinitionSecurityBean;
import com.opengamma.masterdb.security.hibernate.cds.CreditDefaultSwapIndexSecurityBean;
import com.opengamma.masterdb.security.hibernate.cds.CreditDefaultSwapSecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.EquitySecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.EquityVarianceSwapSecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.GICSCodeBean;
import com.opengamma.masterdb.security.hibernate.forward.CommodityForwardSecurityBean;
import com.opengamma.masterdb.security.hibernate.fra.FRASecurityBean;
import com.opengamma.masterdb.security.hibernate.future.FutureBundleBean;
import com.opengamma.masterdb.security.hibernate.future.FutureSecurityBean;
import com.opengamma.masterdb.security.hibernate.fx.FXForwardSecurityBean;
import com.opengamma.masterdb.security.hibernate.fx.NonDeliverableFXForwardSecurityBean;
import com.opengamma.masterdb.security.hibernate.index.BondIndexBean;
import com.opengamma.masterdb.security.hibernate.index.BondIndexComponentBean;
import com.opengamma.masterdb.security.hibernate.index.EquityIndexBean;
import com.opengamma.masterdb.security.hibernate.index.EquityIndexComponentBean;
import com.opengamma.masterdb.security.hibernate.index.IborIndexBean;
import com.opengamma.masterdb.security.hibernate.index.IndexFamilyBean;
import com.opengamma.masterdb.security.hibernate.index.OvernightIndexBean;
import com.opengamma.masterdb.security.hibernate.option.BondFutureOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.CommodityFutureOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.CreditDefaultSwapOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityBarrierOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityIndexDividendFutureOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityIndexFutureOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityIndexOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.FXBarrierOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.FXDigitalOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.FXOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.FxFutureOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.IRFutureOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.NonDeliverableFXDigitalOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.NonDeliverableFXOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.SwaptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.swap.SwapSecurityBean;
import com.opengamma.util.db.HibernateMappingFiles;

/**
 * HibernateSecurityMaster configuration.
 */
public final class HibernateSecurityMasterFiles implements HibernateMappingFiles {

  @Override
  public Class<?>[] getHibernateMappingFiles() {
    return new Class<?>[] {
      BusinessDayConventionBean.class,
      CurrencyBean.class,
      DayCountBean.class,
      ExchangeBean.class,
      FrequencyBean.class,
      StubTypeBean.class,
      IdentifierAssociationBean.class,
      DebtSeniorityBean.class,
      RestructuringClauseBean.class,
      SecurityBean.class,
      
      BondSecurityBean.class,
      CouponTypeBean.class,
      GuaranteeTypeBean.class,
      IssuerTypeBean.class,
      MarketBean.class,
      YieldConventionBean.class,
      
      CashSecurityBean.class,
      
      EquitySecurityBean.class,
      GICSCodeBean.class,
      EquityVarianceSwapSecurityBean.class,
      
      FRASecurityBean.class,
      
      CommodityForwardSecurityBean.class,
      
      FutureSecurityBean.class,      
      FutureBundleBean.class,
      UnitBean.class,
      ContractCategoryBean.class,
      
      EquityIndexOptionSecurityBean.class,
      EquityOptionSecurityBean.class,
      EquityBarrierOptionSecurityBean.class,
      FXOptionSecurityBean.class,
      NonDeliverableFXOptionSecurityBean.class,
      SwaptionSecurityBean.class,
      IRFutureOptionSecurityBean.class,
      CommodityFutureOptionSecurityBean.class,
      FxFutureOptionSecurityBean.class,
      BondFutureOptionSecurityBean.class,
      EquityIndexFutureOptionSecurityBean.class,
      EquityIndexDividendFutureOptionSecurityBean.class,
      FXBarrierOptionSecurityBean.class,
      
      SwapSecurityBean.class,
      FXDigitalOptionSecurityBean.class,
      NonDeliverableFXDigitalOptionSecurityBean.class,
      FXForwardSecurityBean.class,
      NonDeliverableFXForwardSecurityBean.class,
      CapFloorSecurityBean.class,
      CapFloorCMSSpreadSecurityBean.class,
      
      CDSSecurityBean.class,
      CreditDefaultSwapSecurityBean.class,

      CashFlowSecurityBean.class,
      
      TenorBean.class,
      CDSIndexFamilyBean.class,
      CreditDefaultSwapIndexDefinitionSecurityBean.class,
      CreditDefaultSwapIndexSecurityBean.class,
      CreditDefaultSwapOptionSecurityBean.class,
      
      IndexWeightingTypeBean.class,
      BondIndexBean.class,
      BondIndexComponentBean.class,
      EquityIndexBean.class,
      EquityIndexComponentBean.class,
      IborIndexBean.class,
      OvernightIndexBean.class,
      IndexFamilyBean.class
    };
  }

}

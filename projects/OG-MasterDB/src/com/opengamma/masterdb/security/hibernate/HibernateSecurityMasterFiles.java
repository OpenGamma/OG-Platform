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
import com.opengamma.masterdb.security.hibernate.equity.EquitySecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.EquityVarianceSwapSecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.GICSCodeBean;
import com.opengamma.masterdb.security.hibernate.fra.FRASecurityBean;
import com.opengamma.masterdb.security.hibernate.future.BondFutureTypeBean;
import com.opengamma.masterdb.security.hibernate.future.CashRateTypeBean;
import com.opengamma.masterdb.security.hibernate.future.CommodityFutureTypeBean;
import com.opengamma.masterdb.security.hibernate.future.FutureBundleBean;
import com.opengamma.masterdb.security.hibernate.future.FutureSecurityBean;
import com.opengamma.masterdb.security.hibernate.future.UnitBean;
import com.opengamma.masterdb.security.hibernate.fx.FXForwardSecurityBean;
import com.opengamma.masterdb.security.hibernate.fx.FXSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityBarrierOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityIndexOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.EquityOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.FXBarrierOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.FXOptionSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.IRFutureOptionSecurityBean;
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
      IdentifierAssociationBean.class,
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
      
      FutureSecurityBean.class,
      BondFutureTypeBean.class,
      CashRateTypeBean.class,
      CommodityFutureTypeBean.class,
      FutureBundleBean.class,
      UnitBean.class,
      
      EquityIndexOptionSecurityBean.class,
      EquityOptionSecurityBean.class,
      EquityBarrierOptionSecurityBean.class,
      FXOptionSecurityBean.class,
      NonDeliverableFXOptionSecurityBean.class,
      SwaptionSecurityBean.class,
      IRFutureOptionSecurityBean.class,
      FXBarrierOptionSecurityBean.class,
      
      SwapSecurityBean.class,
      FXSecurityBean.class,
      FXForwardSecurityBean.class,
      CapFloorSecurityBean.class,
      CapFloorCMSSpreadSecurityBean.class,
    };
  }

}

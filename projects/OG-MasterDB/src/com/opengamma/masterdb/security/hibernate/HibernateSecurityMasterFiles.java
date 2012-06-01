/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.masterdb.security.hibernate.bond.*;
import com.opengamma.masterdb.security.hibernate.capfloor.CapFloorCMSSpreadSecurityBean;
import com.opengamma.masterdb.security.hibernate.capfloor.CapFloorSecurityBean;
import com.opengamma.masterdb.security.hibernate.cash.CashSecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.EquitySecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.EquityVarianceSwapSecurityBean;
import com.opengamma.masterdb.security.hibernate.equity.GICSCodeBean;
import com.opengamma.masterdb.security.hibernate.fra.FRASecurityBean;
import com.opengamma.masterdb.security.hibernate.future.*;
import com.opengamma.masterdb.security.hibernate.fx.FXForwardSecurityBean;
import com.opengamma.masterdb.security.hibernate.fx.NonDeliverableFXForwardSecurityBean;
import com.opengamma.masterdb.security.hibernate.option.*;
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
      FutureBundleBean.class,
      UnitBean.class,
      
      EquityIndexOptionSecurityBean.class,
      EquityOptionSecurityBean.class,
      EquityBarrierOptionSecurityBean.class,
      FXOptionSecurityBean.class,
      NonDeliverableFXOptionSecurityBean.class,
      SwaptionSecurityBean.class,
      IRFutureOptionSecurityBean.class,
      EquityIndexDividendFutureOptionSecurityBean.class,
      FXBarrierOptionSecurityBean.class,
      
      SwapSecurityBean.class,
      FXDigitalOptionSecurityBean.class,
      NonDeliverableFXDigitalOptionSecurityBean.class,
      FXForwardSecurityBean.class,
      NonDeliverableFXForwardSecurityBean.class,
      CapFloorSecurityBean.class,
      CapFloorCMSSpreadSecurityBean.class,
    };
  }

}

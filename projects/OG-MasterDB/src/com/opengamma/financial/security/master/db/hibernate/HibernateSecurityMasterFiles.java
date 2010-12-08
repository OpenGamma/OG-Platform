/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate;

import com.opengamma.financial.security.master.db.hibernate.bond.BondSecurityBean;
import com.opengamma.financial.security.master.db.hibernate.bond.CouponTypeBean;
import com.opengamma.financial.security.master.db.hibernate.bond.GuaranteeTypeBean;
import com.opengamma.financial.security.master.db.hibernate.bond.IssuerTypeBean;
import com.opengamma.financial.security.master.db.hibernate.bond.MarketBean;
import com.opengamma.financial.security.master.db.hibernate.bond.YieldConventionBean;
import com.opengamma.financial.security.master.db.hibernate.cash.CashSecurityBean;
import com.opengamma.financial.security.master.db.hibernate.equity.EquitySecurityBean;
import com.opengamma.financial.security.master.db.hibernate.equity.GICSCodeBean;
import com.opengamma.financial.security.master.db.hibernate.fra.FRASecurityBean;
import com.opengamma.financial.security.master.db.hibernate.future.BondFutureTypeBean;
import com.opengamma.financial.security.master.db.hibernate.future.CashRateTypeBean;
import com.opengamma.financial.security.master.db.hibernate.future.CommodityFutureTypeBean;
import com.opengamma.financial.security.master.db.hibernate.future.FutureBundleBean;
import com.opengamma.financial.security.master.db.hibernate.future.FutureSecurityBean;
import com.opengamma.financial.security.master.db.hibernate.future.UnitBean;
import com.opengamma.financial.security.master.db.hibernate.option.OptionSecurityBean;
import com.opengamma.financial.security.master.db.hibernate.swap.SwapSecurityBean;
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
      
      FRASecurityBean.class,
      
      FutureSecurityBean.class,
      BondFutureTypeBean.class,
      CashRateTypeBean.class,
      CommodityFutureTypeBean.class,
      FutureBundleBean.class,
      UnitBean.class,
      
      OptionSecurityBean.class,
      
      SwapSecurityBean.class,
    };
  }

}

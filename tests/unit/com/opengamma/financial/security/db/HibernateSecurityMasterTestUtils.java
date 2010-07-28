/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import com.opengamma.financial.security.db.equity.EquitySecurityBean;
import com.opengamma.financial.security.db.equity.GICSCodeBean;

/**
 * Utility class that creates Test Objects 
 *
 */
/* package */class HibernateSecurityMasterTestUtils {

  private HibernateSecurityMasterTestUtils() {
  }

  public static CurrencyBean makeCurrencyBean(String name) {
    CurrencyBean currencyBean = new CurrencyBean();
    currencyBean.setName(name);
    return currencyBean;
  }

  public static ExchangeBean makeExchangeBean(String name, String description) {
    ExchangeBean exchangeBean = new ExchangeBean();
    exchangeBean.setName(name);
    exchangeBean.setDescription(description);
    return exchangeBean;
  }

  public static GICSCodeBean makeGICSCodeBean(String name, String description) {
    GICSCodeBean gicsCodeBean = new GICSCodeBean(name, description);
    return gicsCodeBean;
  }

  public static EquitySecurityBean makeAAPLEquitySecurityBean(HibernateSecurityMasterDao hibernateSecurityMasterDao, EquitySecurityBean firstVersion, String modifiedBy, Date effectiveDate,
      boolean deleted, Date lastModifiedDate) {
    EquitySecurityBean equityBean = new EquitySecurityBean();
    // equitySecurityBean.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, AAPL_EQUITY_TICKER));
    // equitySecurityBean.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, AAPL_BUID));
    // equitySecurityBean.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "037833100"));
    // equitySecurityBean.addIdentifier(new Identifier(IdentificationScheme.ISIN, "US0378331005"));
    // equitySecurityBean.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "2046251"));
    // equitySecurityBean.setUniqueIdentifier(BloombergSecurityMaster.createUniqueIdentifier(AAPL_BUID));
    equityBean.setCompanyName("APPLE INC");
    ExchangeBean exchangeBean = hibernateSecurityMasterDao.getOrCreateExchangeBean("XNGS", "NASDAQ/NGS (GLOBAL SELECT MARKET)");
    equityBean.setExchange(exchangeBean);
    CurrencyBean currencyBean = hibernateSecurityMasterDao.getOrCreateCurrencyBean("USD");
    equityBean.setCurrency(currencyBean);
    GICSCodeBean gicsCodeBean = hibernateSecurityMasterDao.getOrCreateGICSCodeBean("45202010", "Technology");
    equityBean.setGICSCode(gicsCodeBean);
    equityBean.setDisplayName("APPLE INC");

    equityBean.setFirstVersion(firstVersion);
    equityBean.setLastModifiedBy(modifiedBy);
    equityBean.setEffectiveDateTime(effectiveDate);
    equityBean.setDeleted(deleted);
    equityBean.setLastModifiedDateTime(lastModifiedDate);

    return equityBean;
  }

  public static EquitySecurityBean makeATTEquitySecurityBean(EquitySecurityBean firstVersion, String modifiedBy, Date effectiveDate, boolean deleted, Date lastModifiedDate) {
    EquitySecurityBean equityBean = new EquitySecurityBean();
    equityBean.setCompanyName("AT&T INC");
    equityBean.setExchange(makeExchangeBean("XNYS", "NEW YORK STOCK EXCHANGE INC."));
    equityBean.setCurrency(makeCurrencyBean("USD"));
    equityBean.setGICSCode(makeGICSCodeBean("50101020", "Technology"));
    equityBean.setDisplayName("AT&T INC");

    equityBean.setFirstVersion(firstVersion);
    equityBean.setLastModifiedBy(modifiedBy);
    equityBean.setEffectiveDateTime(effectiveDate);
    equityBean.setDeleted(deleted);
    equityBean.setLastModifiedDateTime(lastModifiedDate);

    return equityBean;
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.security.WritableSecuritySource;

/**
 * 
 *
 * 
 */
public class HibernateSecurityMasterTest extends WritableSecurityMasterTestCase {

  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterTest.class);

  /**
   * @param databaseType
   * @param databaseVersion
   */
  public HibernateSecurityMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running test for database={} version={}", databaseType, databaseVersion);
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] { BondFutureTypeBean.class, BondSecurityBean.class, BusinessDayConventionBean.class, CashRateTypeBean.class, CommodityFutureTypeBean.class,
        CouponTypeBean.class, CurrencyBean.class, DayCountBean.class, EquitySecurityBean.class, ExchangeBean.class, FrequencyBean.class, FutureBundleBean.class,
        FutureSecurityBean.class, GICSCodeBean.class, GuaranteeTypeBean.class, IdentifierAssociationBean.class, IssuerTypeBean.class, MarketBean.class, OptionSecurityBean.class,
        SecurityBean.class, UnitBean.class, YieldConventionBean.class };
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected WritableSecuritySource createSecurityMaster() {
    HibernateSecurityMaster secMaster = new HibernateSecurityMaster();
    secMaster.setSessionFactory(getSessionFactory());
    s_logger.debug("SecMaster initialization complete {}", secMaster);
    return secMaster;
  }

}

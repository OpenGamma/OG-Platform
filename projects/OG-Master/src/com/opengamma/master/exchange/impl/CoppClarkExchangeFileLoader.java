/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;

/**
 * Loads the exchange data from the Copp-Clark data source.
 * <p>
 * This will merge the input with the data already in the database.
 */
public class CoppClarkExchangeFileLoader {

  /**
   * Loads the data into the database.
   * @param args  empty arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    PlatformConfigUtils.configureSystemProperties(RunMode.SHAREDDEV);
    ApplicationContext context = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    ExchangeMaster master = context.getBean("dbExchangeMaster", ExchangeMaster.class);
    CoppClarkExchangeFileReader.createPopulated(master);
  }

}

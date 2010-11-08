/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.loader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.financial.world.holiday.master.HolidayMaster;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.PlatformConfigUtils.RunMode;

/**
 * Loads the holiday data from the Copp-Clark data source.
 * <p>
 * This will merge the input with the data already in the database.
 */
public class CoppClarkHolidayFileLoader {

  /**
   * Loads the data into the database.
   * @param args  empty arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    PlatformConfigUtils.configureSystemProperties(RunMode.SHAREDDEV);
    ApplicationContext context = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    HolidayMaster master = context.getBean("dbHolidayMaster", HolidayMaster.class);
    CoppClarkHolidayFileReader.createPopulated(master);
  }

}

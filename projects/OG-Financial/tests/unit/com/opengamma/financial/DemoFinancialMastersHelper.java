/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Helper for testing loading elements from {@code demoFinancialMasters.xml}.
 */
public class DemoFinancialMastersHelper {

  private final ClassPathXmlApplicationContext _applicationContext;
  private final ConfigMaster _configMaster;
  private final MasterConfigSource _configSource;
  private final RegionSource _regionSource;
  private final SecuritySource _secSource;

  public DemoFinancialMastersHelper() {
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    _applicationContext = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    
    InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    YieldCurveConfigPopulator.populateCurveConfigMaster(cfgMaster);
    _configMaster = cfgMaster;
    _configSource = new MasterConfigSource(cfgMaster);
    
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populate(regionMaster);
    RegionSource regionSource = new MasterRegionSource(regionMaster);
    _regionSource = regionSource;
    
    _secSource = _applicationContext.getBean("sharedSecuritySource", SecuritySource.class);
  }

  public void tearDown() {
    _applicationContext.close();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the config master, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Gets the config source.
   * @return the config source, not null
   */
  public MasterConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Gets the region source field.
   * @return the region source, not null
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  /**
   * Gets the security source field.
   * @return the security source, not null
   */
  public SecuritySource getSecuritySource() {
    return _secSource;
  }

}

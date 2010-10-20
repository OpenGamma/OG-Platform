/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.test;

import java.io.File;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.config.ConfigMaster;
import com.opengamma.config.memory.InMemoryConfigMaster;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.config.MasterConfigSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.world.region.DefaultRegionSource;
import com.opengamma.financial.world.region.InMemoryRegionMaster;
import com.opengamma.financial.world.region.RegionFileReader;
import com.opengamma.financial.world.region.RegionMaster;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Helper for testing.
 */
public class CurveConfigurationSetupHelper {

  private final ClassPathXmlApplicationContext _applicationContext;
  private final ConfigMaster _configMaster;
  private final ConfigSource _configSource;
  private final RegionSource _regionSource;
  private final SecuritySource _secSource;

  public CurveConfigurationSetupHelper() {
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    _applicationContext = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    
    InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    YieldCurveConfigPopulator.populateCurveConfigMaster(cfgMaster);
    _configMaster = cfgMaster;
    _configSource = new MasterConfigSource(cfgMaster);
    
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populateMaster(regionMaster, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSource regionSource = new DefaultRegionSource(regionMaster);
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
  public ConfigSource getConfigSource() {
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

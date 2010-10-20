/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.test;

import java.io.File;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.config.InMemoryMasterConfigSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
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
  private final ConfigSource _configSource;
  private final RegionSource _regionSource;
  private final SecuritySource _secSource;

  public CurveConfigurationSetupHelper() {
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    _applicationContext = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    
    InMemoryMasterConfigSource cfgSource = new InMemoryMasterConfigSource();
    YieldCurveConfigPopulator.populateCurveDefinitionConfigMaster(cfgSource.getMaster(YieldCurveDefinition.class));
    YieldCurveConfigPopulator.populateCurveSpecificationBuilderConfigMaster(cfgSource.getMaster(CurveSpecificationBuilderConfiguration.class));
    _configSource = cfgSource;
    
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populateMaster(regionMaster, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSource regionSource = new DefaultRegionSource(regionMaster);
    _regionSource = regionSource;
    
    _secSource = _applicationContext.getBean("sharedSecuritySource", SecuritySource.class);
  }

  public void tearDown() {
    _applicationContext.close();
  }

  /**
   * Gets the configSource field.
   * @return the configSource
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Gets the regionSource field.
   * @return the regionSource
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  /**
   * Gets the secSource field.
   * @return the secSource
   */
  public SecuritySource getSecSource() {
    return _secSource;
  }

}

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.test;

import java.io.File;
import java.net.UnknownHostException;

import org.fudgemsg.FudgeContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.config.MongoDBMasterConfigSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveConfigPopulator;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.world.region.DefaultRegionSource;
import com.opengamma.financial.world.region.InMemoryRegionMaster;
import com.opengamma.financial.world.region.RegionFileReader;
import com.opengamma.financial.world.region.RegionMaster;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * 
 */
public class CurveConfigurationSetupHelper {
  private ConfigSource _configSource;
  private RegionSource _regionSource;
  private SecuritySource _secSource;

  private ClassPathXmlApplicationContext _applicationContext;
  
  private MongoDBConnectionSettings _yieldCurveDefinitionSettings;
  private MongoDBConnectionSettings _curveSpecificationBuilderConfigurationSettings;
  
  public CurveConfigurationSetupHelper() {
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
       
    _yieldCurveDefinitionSettings = MongoDBTestUtils.makeTestSettings("YieldCurveDefinition", true);
    _curveSpecificationBuilderConfigurationSettings = MongoDBTestUtils.makeTestSettings("CurveSpecificationBuilderConfiguration", true);
    
    MongoDBConfigMaster<YieldCurveDefinition> yieldCurveDefinitionConfigMaster = new MongoDBConfigMaster<YieldCurveDefinition>(YieldCurveDefinition.class, 
                                                                                                                               _yieldCurveDefinitionSettings, 
                                                                                                                               fudgeContext, true, null);
    YieldCurveConfigPopulator.populateCurveDefinitionConfigMaster(yieldCurveDefinitionConfigMaster);
    
    MongoDBMasterConfigSource mongoDBMasterConfigSource = new MongoDBMasterConfigSource();
    mongoDBMasterConfigSource.addConfigMaster(YieldCurveDefinition.class, yieldCurveDefinitionConfigMaster);
    MongoDBConfigMaster<CurveSpecificationBuilderConfiguration> curveSpecificationBuilderConfigMaster = 
      new MongoDBConfigMaster<CurveSpecificationBuilderConfiguration>(CurveSpecificationBuilderConfiguration.class, 
                                                                      _curveSpecificationBuilderConfigurationSettings, 
                                                                      fudgeContext, true, null);
    YieldCurveConfigPopulator.populateCurveSpecificationBuilderConfigMaster(curveSpecificationBuilderConfigMaster);
    mongoDBMasterConfigSource.addConfigMaster(CurveSpecificationBuilderConfiguration.class, curveSpecificationBuilderConfigMaster);
    _configSource = mongoDBMasterConfigSource;
    
    RegionMaster regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populateMaster(regionMaster, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSource regionSource = new DefaultRegionSource(regionMaster);
    _regionSource = regionSource;
        
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    _applicationContext = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    _secSource = (SecuritySource) _applicationContext.getBean("sharedSecuritySource");
  }
  
  public void tearDown() throws UnknownHostException, MongoException {
    Mongo mongo = new Mongo(_yieldCurveDefinitionSettings.getHost(), _yieldCurveDefinitionSettings.getPort());
    DB db = mongo.getDB(_yieldCurveDefinitionSettings.getDatabase());
    DBCollection dbCollection = db.getCollection(_yieldCurveDefinitionSettings.getCollectionName());
    dbCollection.drop();
    dbCollection = db.getCollection(_curveSpecificationBuilderConfigurationSettings.getCollectionName());
    dbCollection.drop();
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

  /**
   * Gets the yieldCurveDefinitionSettings field.
   * @return the yieldCurveDefinitionSettings
   */
  public MongoDBConnectionSettings getYieldCurveDefinitionSettings() {
    return _yieldCurveDefinitionSettings;
  }

  /**
   * Gets the curveSpecificationBuilderConfigurationSettings field.
   * @return the curveSpecificationBuilderConfigurationSettings
   */
  public MongoDBConnectionSettings getCurveSpecificationBuilderConfigurationSettings() {
    return _curveSpecificationBuilderConfigurationSettings;
  }
}

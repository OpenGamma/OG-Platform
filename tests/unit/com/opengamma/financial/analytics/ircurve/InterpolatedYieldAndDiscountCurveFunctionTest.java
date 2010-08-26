/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.engine.config.MongoDBMasterConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.world.RegionSource;
import com.opengamma.financial.Currency;
import com.opengamma.financial.DefaultRegionSource;
import com.opengamma.financial.InMemoryRegionRepository;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.RegionFileReader;
import com.opengamma.financial.RegionRepository;
import com.opengamma.financial.fudgemsg.FinancialFudgeContextConfiguration;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.test.MongoDBTestUtils;
import com.opengamma.util.time.DateUtil;

/**
 * Test InterpolatedYieldAndDiscountCurveFunction.
 */
public class InterpolatedYieldAndDiscountCurveFunctionTest {
  private final Logger s_logger = LoggerFactory.getLogger(this.getClass());
  
  private ConfigSource _configSource;
  private RegionSource _regionSource;
  private SecuritySource _secSource;

  private ClassPathXmlApplicationContext _applicationContext;
  
  private MongoDBConnectionSettings _yieldCurveDefinitionSettings;
  private MongoDBConnectionSettings _curveSpecificationBuilderConfigurationSettings;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    FudgeContext fudgeContext = new FudgeContext();
    final FinancialFudgeContextConfiguration fudgeConfiguration = new FinancialFudgeContextConfiguration();
    fudgeConfiguration.configureFudgeContext(fudgeContext);
    
    _yieldCurveDefinitionSettings = MongoDBTestUtils.makeTestSettings("YieldCurveDefinition", true);
    _curveSpecificationBuilderConfigurationSettings = MongoDBTestUtils.makeTestSettings("CurveSpecificationBuilderConfiguration", true);
    
    MongoDBConfigMaster<YieldCurveDefinition> yieldCurveDefinitionConfigMaster = new MongoDBConfigMaster<YieldCurveDefinition>(YieldCurveDefinition.class, _yieldCurveDefinitionSettings, fudgeContext, true, null);
    YieldCurveConfigPopulator.populateCurveDefinitionConfigRepository(yieldCurveDefinitionConfigMaster);
    
    MongoDBMasterConfigSource mongoDBMasterConfigSource = new MongoDBMasterConfigSource();
    mongoDBMasterConfigSource.addConfigMaster(YieldCurveDefinition.class, yieldCurveDefinitionConfigMaster);
    MongoDBConfigMaster<CurveSpecificationBuilderConfiguration> curveSpecificationBuilderConfigMaster = new MongoDBConfigMaster<CurveSpecificationBuilderConfiguration>(CurveSpecificationBuilderConfiguration.class, _curveSpecificationBuilderConfigurationSettings, fudgeContext, true, null);
    YieldCurveConfigPopulator.populateCurveSpecificationBuilderConfigRepository(curveSpecificationBuilderConfigMaster);
    ConfigSearchRequest search = new ConfigSearchRequest();
    search.setName("DEFAULT_USD");
    ConfigSearchResult<CurveSpecificationBuilderConfiguration> results = curveSpecificationBuilderConfigMaster.search(search);
    ConfigDocument<CurveSpecificationBuilderConfiguration> configDocument = results.getDocuments().get(0);
    CurveSpecificationBuilderConfiguration value = configDocument.getValue();
    mongoDBMasterConfigSource.addConfigMaster(CurveSpecificationBuilderConfiguration.class, curveSpecificationBuilderConfigMaster);
    _configSource = mongoDBMasterConfigSource;
    
    RegionRepository regionMaster = new InMemoryRegionRepository();
    RegionFileReader.populateMaster(regionMaster, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSource regionSource = new DefaultRegionSource(regionMaster);
    _regionSource = regionSource;
        
    PlatformConfigUtils.configureSystemProperties(PlatformConfigUtils.RunMode.SHAREDDEV);
    _applicationContext = new ClassPathXmlApplicationContext("demoFinancialMasters.xml");
    _secSource = (SecuritySource) _applicationContext.getBean("sharedSecuritySource");
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    Mongo mongo = new Mongo(_yieldCurveDefinitionSettings.getHost(), _yieldCurveDefinitionSettings.getPort());
    DB db = mongo.getDB(_yieldCurveDefinitionSettings.getDatabase());
    DBCollection dbCollection = db.getCollection(_yieldCurveDefinitionSettings.getCollectionName());
    dbCollection.drop();
    dbCollection = db.getCollection(_curveSpecificationBuilderConfigurationSettings.getCollectionName());
    dbCollection.drop();
    _applicationContext.close();
  }
  
  @Test
  public void discountCurveRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    final LocalDate curveDate = DateUtil.previousWeekDay();
    
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveDate,
        curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put(OpenGammaCompilationContext.CONFIG_SOURCE_NAME, _configSource);
    context.put(OpenGammaCompilationContext.REGION_SOURCE_NAME, _regionSource);
    context.setSecuritySource(_secSource);
    function.init(context);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("USD")));
    s_logger.info(requirements.toString());
    assertNotNull(requirements);
    assertEquals(11, requirements.size());
    Set<Identifier> foundKeys = new TreeSet<Identifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getIdentifier());
    }
    assertEquals(11, foundKeys.size());
    
    ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(_configSource);
    YieldCurveDefinition curveDefinition = curveDefinitionSource.getDefinition(Currency.getInstance("USD"),curveName);
    ConfigDBInterpolatedYieldCurveSpecificationBuilder curveSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(_configSource);
    InterpolatedYieldCurveSpecification curveSpecification = curveSpecBuilder.buildCurve(curveDate, curveDefinition);
    for (ResolvedFixedIncomeStrip strip : curveSpecification.getStrips()) {
      if (!foundKeys.contains(strip.getSecurity())) {
        s_logger.info(strip.getSecurity().toString());
      }
      assertTrue(foundKeys.contains(strip.getSecurity()));
    }
  }

  @Test
  public void yieldCurveRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    final LocalDate curveDate = DateUtil.previousWeekDay();
    
    YieldCurveConfigPopulator.populateCurveDefinitionConfigRepository((MongoDBMasterConfigSource)_configSource);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveDate,
        curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put(OpenGammaCompilationContext.CONFIG_SOURCE_NAME, _configSource);
    function.init(context);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency
        .getInstance("USD")));
    assertNotNull(requirements);
    assertEquals(11, requirements.size());
    Set<Identifier> foundKeys = new TreeSet<Identifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getIdentifier());
    }
    assertEquals(11, foundKeys.size());

    ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(_configSource);
    YieldCurveDefinition curveDefinition = curveDefinitionSource.getDefinition(Currency.getInstance("USD"),curveName);
    ConfigDBInterpolatedYieldCurveSpecificationBuilder curveSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(_configSource);
    InterpolatedYieldCurveSpecification curveSpecification = curveSpecBuilder.buildCurve(curveDate, curveDefinition);
    
    for (ResolvedFixedIncomeStrip strip : curveSpecification.getStrips()) {
      assertTrue(foundKeys.contains(strip.getSecurity()));
    }
  }

  @Test
  public void discountCurveNotMatchingRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    final LocalDate curveDate = DateUtil.previousWeekDay();
    
    YieldCurveConfigPopulator.populateCurveDefinitionConfigRepository((MongoDBMasterConfigSource)_configSource);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveDate,
        curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put(OpenGammaCompilationContext.CONFIG_SOURCE_NAME, _configSource);
    context.put(OpenGammaCompilationContext.REGION_SOURCE_NAME, _regionSource);
    function.init(context);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("EUR")));
    assertNull(requirements);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioNodeImpl()));
    assertNull(requirements);
  }

  @Test
  public void yieldCurveNotMatchingRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    final LocalDate curveDate = LocalDate.nowSystemClock();
    
    YieldCurveConfigPopulator.populateCurveDefinitionConfigRepository((MongoDBMasterConfigSource)_configSource);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveDate,
        curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put(OpenGammaCompilationContext.CONFIG_SOURCE_NAME, _configSource);
    context.put(OpenGammaCompilationContext.REGION_SOURCE_NAME, _regionSource);
    function.init(context);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency
        .getInstance("EUR")));
    assertNull(requirements);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE,
        new PortfolioNodeImpl()));
    assertNull(requirements);
  }

}

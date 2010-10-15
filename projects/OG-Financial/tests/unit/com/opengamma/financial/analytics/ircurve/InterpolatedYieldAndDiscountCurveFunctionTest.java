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

import java.util.Set;
import java.util.TreeSet;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.config.MongoDBMasterConfigSource;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.test.CurveConfigurationSetupHelper;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.DateUtil;

/**
 * Test InterpolatedYieldAndDiscountCurveFunction.
 */
public class InterpolatedYieldAndDiscountCurveFunctionTest {
  private final Logger s_logger = LoggerFactory.getLogger(this.getClass());
  
  private CurveConfigurationSetupHelper _configHelper;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
     _configHelper = new CurveConfigurationSetupHelper();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    _configHelper.tearDown();
  }
  
  @Test
  public void discountCurveRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    final LocalDate curveDate = DateUtil.previousWeekDay();
    
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, _configHelper.getConfigSource());
    OpenGammaCompilationContext.setRegionSource(context, _configHelper.getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, new DefaultConventionBundleSource(new InMemoryConventionBundleMaster()));
    context.setSecuritySource(_configHelper.getSecSource());

    function.init(context);
    CompiledFunctionDefinition compiledFunction = function.compile(context, Instant.nowSystemClock());

    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("USD")));
    s_logger.info(requirements.toString());
    assertNotNull(requirements);
    assertEquals(18, requirements.size());
    Set<Identifier> foundKeys = new TreeSet<Identifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getIdentifier());
    }
    assertEquals(18, foundKeys.size());
    
    ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(_configHelper.getConfigSource());
    YieldCurveDefinition curveDefinition = curveDefinitionSource.getDefinition(Currency.getInstance("USD"),curveName);
    ConfigDBInterpolatedYieldCurveSpecificationBuilder curveSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(_configHelper.getConfigSource());
    InterpolatedYieldCurveSpecification curveSpecification = curveSpecBuilder.buildCurve(curveDate, curveDefinition);
    for (FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
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
    
    YieldCurveConfigPopulator.populateCurveConfigSource((MongoDBMasterConfigSource)_configHelper.getConfigSource());
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, _configHelper.getConfigSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, new DefaultConventionBundleSource(new InMemoryConventionBundleMaster()));

    function.init(context);
    CompiledFunctionDefinition compiledFunction = function.compile(context, Instant.nowSystemClock());

    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency
        .getInstance("USD")));
    assertNotNull(requirements);
    assertEquals(18, requirements.size());
    Set<Identifier> foundKeys = new TreeSet<Identifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getIdentifier());
    }
    assertEquals(18, foundKeys.size());

    ConfigDBInterpolatedYieldCurveDefinitionSource curveDefinitionSource = new ConfigDBInterpolatedYieldCurveDefinitionSource(_configHelper.getConfigSource());
    YieldCurveDefinition curveDefinition = curveDefinitionSource.getDefinition(Currency.getInstance("USD"),curveName);
    ConfigDBInterpolatedYieldCurveSpecificationBuilder curveSpecBuilder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(_configHelper.getConfigSource());
    InterpolatedYieldCurveSpecification curveSpecification = curveSpecBuilder.buildCurve(curveDate, curveDefinition);
    
    for (FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      assertTrue(foundKeys.contains(strip.getSecurity()));
    }
  }

  @Test
  public void discountCurveNotMatchingRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    
    YieldCurveConfigPopulator.populateCurveConfigSource((MongoDBMasterConfigSource)_configHelper.getConfigSource());
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, _configHelper.getConfigSource());
    OpenGammaCompilationContext.setRegionSource(context, _configHelper.getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, new DefaultConventionBundleSource(new InMemoryConventionBundleMaster()));

    function.init(context);
    CompiledFunctionDefinition compiledFunction = function.compile(context, Instant.nowSystemClock());
    
    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("EUR")));
    assertNull(requirements);
    
    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioNodeImpl()));
    assertNull(requirements);
  }

  @Test
  public void yieldCurveNotMatchingRequirements() {
    final Currency curveCurrency = Currency.getInstance("USD");
    final String curveName = "FUNDING";
    
    YieldCurveConfigPopulator.populateCurveConfigSource((MongoDBMasterConfigSource)_configHelper.getConfigSource());
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(curveCurrency, curveName, false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, _configHelper.getConfigSource());
    OpenGammaCompilationContext.setRegionSource(context, _configHelper.getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, new DefaultConventionBundleSource(new InMemoryConventionBundleMaster()));

    function.init(context);
    CompiledFunctionDefinition compiledFunction = function.compile(context, Instant.nowSystemClock());

    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency
        .getInstance("EUR")));
    assertNull(requirements);

    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE,
        new PortfolioNodeImpl()));
    assertNull(requirements);
  }

}

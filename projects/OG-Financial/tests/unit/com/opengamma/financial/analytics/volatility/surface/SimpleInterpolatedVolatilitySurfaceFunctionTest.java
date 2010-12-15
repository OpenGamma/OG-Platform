/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;
import java.util.TreeSet;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.common.Currency;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.DemoFinancialMastersHelper;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

/**
 * Test SimpleInterpolatedVolatilitySurfaceFunction.
 */
public class SimpleInterpolatedVolatilitySurfaceFunctionTest {

  /** Logger. */
  private final Logger s_logger = LoggerFactory.getLogger(this.getClass());

  private DemoFinancialMastersHelper _configHelper;

  @Before
  public void setUp() throws Exception {
     _configHelper = new DemoFinancialMastersHelper();
     SwaptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(_configHelper.getConfigMaster());
  }

  @After
  public void tearDown() throws Exception {
    _configHelper.tearDown();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Test
  public void volatilitySurfaceRequirements() {
    final Currency currency = Currency.getInstance("USD");
    final String definitionName = "TEST";
    final String specificationName = "DEFAULT";
    final LocalDate curveDate = DateUtil.previousWeekDay();
        
    SimpleInterpolatedVolatilitySurfaceFunction function = new SimpleInterpolatedVolatilitySurfaceFunction(currency, definitionName, specificationName);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, _configHelper.getConfigSource());
    context.setSecuritySource(_configHelper.getSecuritySource());
    
    function.init(context);
    CompiledFunctionDefinition compiledFunction = function.compile(context, curveDate.atStartOfDayInZone(TimeZone.UTC));

    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("USD")), null);
    s_logger.info(requirements.toString());
    assertNotNull(requirements);
    //assertEquals(EXPECTED_SIZE, requirements.size());
    Set<Identifier> foundKeys = new TreeSet<Identifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getIdentifier());
    }
    // assertEquals(EXPECTED, foundKeys.size());
    
    ConfigDBVolatilitySurfaceDefinitionSource volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(_configHelper.getConfigSource());
    VolatilitySurfaceDefinition<Tenor, Tenor> definition = (VolatilitySurfaceDefinition<Tenor, Tenor>) volSurfaceDefinitionSource.getDefinition(currency, definitionName);
    ConfigDBVolatilitySurfaceSpecificationSource volSurfaceSpecSource = new ConfigDBVolatilitySurfaceSpecificationSource(_configHelper.getConfigSource());
    VolatilitySurfaceSpecification specification = volSurfaceSpecSource.getSpecification(currency, specificationName);
    
    Set<Identifier> expectedKeys = new TreeSet<Identifier>();
    for (Tenor timeToExpiry : definition.getXs()) {
      for (Tenor swapLength : definition.getYs()) {
        Identifier identifier = ((SurfaceInstrumentProvider<Tenor, Tenor>)specification.getSurfaceInstrumentProvider()).getInstrument(timeToExpiry, swapLength);
        expectedKeys.add(identifier);
      }
    }
    assertEquals(expectedKeys, foundKeys);
  }


  @Test
  public void volatilitySurfaceNotMatchingRequirements() {
    final Currency currency = Currency.getInstance("USD");
    final String definitionName = "TEST";
    final String specificationName = "DEFAULT";
        
    SimpleInterpolatedVolatilitySurfaceFunction function = new SimpleInterpolatedVolatilitySurfaceFunction(currency, definitionName, specificationName);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    OpenGammaCompilationContext.setConfigSource(context, _configHelper.getConfigSource());
    OpenGammaCompilationContext.setRegionSource(context, _configHelper.getRegionSource());
    OpenGammaCompilationContext.setConventionBundleSource(context, new DefaultConventionBundleSource(new InMemoryConventionBundleMaster()));
    
    function.init(context);
    CompiledFunctionDefinition compiledFunction = function.compile(context, Instant.nowSystemClock());
    
    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("EUR")), null);
    assertNull(requirements);
    
    requirements = compiledFunction.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioNodeImpl()), null);
    assertNull(requirements);
  }


}

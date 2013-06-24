package com.opengamma.financial.depgraph.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilder;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Test for {@link LocalDependencyGraphTraceProvider}
 */
public class LocalDependencyGraphTraceProviderTest {

  @Test
  public void LocalDependencyGraphTraceProvider() {
    assertEquals(_builder, _provider.getTraceBuilder());
  }
  
  private DependencyGraphTraceBuilder _builder;
  private LocalDependencyGraphTraceProvider _provider;
  private DependencyGraphBuildTrace _sampleResult;
  
  @BeforeMethod
  public void beforeTest() {
    _builder = mock(DependencyGraphTraceBuilder.class);
    _provider = new LocalDependencyGraphTraceProvider(_builder);
    _sampleResult = DependencyGraphBuildTrace.of(null, null, null, null);
  }
  
  @Test
  public void getTraceWithCalculationConfigurationName() {
    String configName = "test";
    when(_builder.calculationConfigurationName(configName)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithCalculationConfigurationName("test");
    
    verify(_builder).calculationConfigurationName(configName);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
    
  }

  @Test
  public void getTraceWithDefaultProperties() {
    ValueProperties props = mock(ValueProperties.class);
    when(_builder.defaultProperties(props)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithDefaultProperties(props);
    
    verify(_builder).defaultProperties(props);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
    
  }

  @Test
  public void getTraceWithMarketData() {
    UserMarketDataSpecification mdSpec = mock(UserMarketDataSpecification.class);
    when(_builder.marketData(mdSpec)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithMarketData(mdSpec);
    
    verify(_builder).marketData(mdSpec);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
  }

  @Test
  public void getTraceWithResolutionTime() {
    VersionCorrection resolutionTime = VersionCorrection.LATEST;
    when(_builder.resolutionTime(resolutionTime)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithResolutionTime(resolutionTime);
    
    verify(_builder).resolutionTime(resolutionTime);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
  }

  @Test
  public void getTraceWithValuationTime() {
    Instant valuationTime = Instant.now();
    when(_builder.valuationTime(valuationTime)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithValuationTime(valuationTime);
    
    verify(_builder).valuationTime(valuationTime);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
  }

  @Test
  public void getTraceWithValueRequirementByExternalId() {
    //inputs
    String valueName = MarketDataRequirementNames.MARKET_VALUE;
    String targetType = "PRIMITIVE";
    ExternalId externalId = ExternalId.parse("Bar~1");
    //expected arg to builder
    ValueRequirement req = new ValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.parse(targetType), externalId));
    
    when(_builder.addRequirement(req)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithValueRequirementByExternalId(valueName, targetType, externalId);
    
    verify(_builder).addRequirement(req);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
  }

  @Test
  public void getTraceWithValueRequirementByUniqueId() {
    //inputs
    String valueName = MarketDataRequirementNames.MARKET_VALUE;
    String targetType = "PRIMITIVE";
    UniqueId externalId = UniqueId.parse("Bar~2");
    //expected arg to builder
    ValueRequirement req = new ValueRequirement(valueName, new ComputationTargetSpecification(ComputationTargetType.parse(targetType), externalId));
    
    when(_builder.addRequirement(req)).thenReturn(_builder);
    when(_builder.build()).thenReturn(_sampleResult);
    
    DependencyGraphBuildTrace result = _provider.getTraceWithValueRequirementByUniqueId(valueName, targetType, externalId);
    
    verify(_builder).addRequirement(req);
    verify(_builder).build();
    assertEquals(_sampleResult, result);
  }
  

}

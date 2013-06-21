package com.opengamma.financial.depgraph.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.financial.depgraph.provider.DependencyGraphTraceProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Test for {@link DependencyGraphTraceProviderResource}
 */
public class DependencyGraphTraceProviderResourceTest {
  
  private DependencyGraphTraceProviderResource _resource;
  private FudgeContext _fudgeContext;
  private DependencyGraphTraceProvider _provider;
  private DependencyGraphBuildTrace _sampleResult;
  
  @BeforeMethod
  public void beforeTest() {
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
    _provider = mock(DependencyGraphTraceProvider.class);
    _sampleResult = DependencyGraphBuildTrace.of(null, null, null, null);
    _resource = new DependencyGraphTraceProviderResource(_provider, _fudgeContext);
  }

  @Test
  public void DependencyGraphTraceProviderResource() {
    assertEquals(_provider, _resource.getProvider());
    assertEquals(_fudgeContext, _resource.getFudgeContext());
  }

  @Test
  public void getTraceWithCalculationConfigurationName() {
    String calcConfigName = "test";
    
    when(_provider.getTraceWithCalculationConfigurationName(calcConfigName)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithCalculationConfigurationName(calcConfigName);
    
    verify(_provider).getTraceWithCalculationConfigurationName(calcConfigName);
    assertNotNull(result);
    
  }

  @Test
  public void getTraceWithDefaultProperties() {
    //input
    String defaultProperties = "A=[foo,bar],B=*";
    
    //expected arg
    ValueProperties props = ValueProperties.parse(defaultProperties);
    
    when(_provider.getTraceWithDefaultProperties(props)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithDefaultProperties(defaultProperties);
    
    verify(_provider).getTraceWithDefaultProperties(props);
    assertNotNull(result);
  }

  @Test
  public void getTraceWithMarketData() {
    //input
    String snapshotId = "Foo~1";
    
    //expected arg
    UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    
    when(_provider.getTraceWithMarketData(marketData)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithMarketData(snapshotId);
    
    verify(_provider).getTraceWithMarketData(marketData);
    assertNotNull(result);
  }

  @Test
  public void getTraceWithResolutionTime() {
    //input
    String resolutionTime = "V1970-01-01T00:00:01Z.CLATEST";
    
    //expected arg
    VersionCorrection parsed = VersionCorrection.parse(resolutionTime);
    
    when(_provider.getTraceWithResolutionTime(parsed)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithResolutionTime(resolutionTime);
    
    verify(_provider).getTraceWithResolutionTime(parsed);
    assertNotNull(result);
  }

  @Test
  public void getTraceWithValuationTime() {
    //input
    String resolutionTime = "V1970-01-01T00:00:01Z.CLATEST";
    
    //expected arg
    VersionCorrection parsed = VersionCorrection.parse(resolutionTime);
    
    when(_provider.getTraceWithResolutionTime(parsed)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithResolutionTime(resolutionTime);
    
    verify(_provider).getTraceWithResolutionTime(parsed);
    assertNotNull(result);
  }

  @Test
  public void getTraceWithValueRequirementByExternalId() {
    //input
    String valueName = "name";
    String targetType = "target";
    String externalId = "Foo~1";
    
    //expected arg
    ExternalId expectedExternalId = ExternalId.parse(externalId);
    
    when(_provider.getTraceWithValueRequirementByExternalId(valueName, targetType, expectedExternalId)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithValueRequirementByExternalId(valueName, targetType, externalId);
    
    verify(_provider).getTraceWithValueRequirementByExternalId(valueName, targetType, expectedExternalId);
    assertNotNull(result);
  }

  @Test
  public void getTraceWithValueRequirementByUniqueId() {
    //input
    String valueName = "name";
    String targetType = "target";
    String uniqueId = "Foo~1";
    
    //expected arg
    UniqueId expectedUniqueId = UniqueId.parse(uniqueId);
    
    when(_provider.getTraceWithValueRequirementByUniqueId(valueName, targetType, expectedUniqueId)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithValueRequirementByUniqueId(valueName, targetType, uniqueId);
    
    verify(_provider).getTraceWithValueRequirementByUniqueId(valueName, targetType, expectedUniqueId);
    assertNotNull(result);
  }

}

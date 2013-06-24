package com.opengamma.financial.depgraph.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Throwables;
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
  
  private static final String s_testUrl = "http://testurl.com/";
  
  private DependencyGraphTraceProviderResource _resource;
  private FudgeContext _fudgeContext;
  private DependencyGraphTraceProvider _provider;
  private DependencyGraphBuildTrace _sampleResult;
  private URI _baseUri;
  
  @BeforeMethod
  public void beforeTest() {
    _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
    _provider = mock(DependencyGraphTraceProvider.class);
    _sampleResult = DependencyGraphBuildTrace.of(null, null, null, null);
    _resource = new DependencyGraphTraceProviderResource(_provider, _fudgeContext);
    try {
      _baseUri = new URI(s_testUrl);
    } catch (URISyntaxException ex) {
     Throwables.propagate(ex);
    }
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
    String valuationTimeStr = "2013-06-24T12:18:01.094Z";

    //expected arg
    Instant valuationTime = Instant.parse(valuationTimeStr);
    
    when(_provider.getTraceWithValuationTime(valuationTime)).thenReturn(_sampleResult);
    
    FudgeMsgEnvelope result = _resource.getTraceWithValuationTime(valuationTimeStr);
    
    verify(_provider).getTraceWithValuationTime(valuationTime);
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

  
  //-----------------------------------------------------------
  
  
  @Test
  public void uriCalculationConfigurationName() throws UnsupportedEncodingException {
    String testStr = "test";
    URI uriCalculationConfigurationName = DependencyGraphTraceProviderResource.uriCalculationConfigurationName(_baseUri, testStr);
    String url = decode(uriCalculationConfigurationName);
    assertEquals(s_testUrl + "calculationConfigurationName/" + testStr, url);
  }

  @Test
  public void uriDefaultProperties() throws UnsupportedEncodingException {
    String defaultPropertiesStr = "{A=[foo,bar],B=[*]}";
    ValueProperties parsed = ValueProperties.parse(defaultPropertiesStr);
    URI uri = DependencyGraphTraceProviderResource.uriDefaultProperties(_baseUri, parsed);
    String url = decode(uri);
    assertEquals(s_testUrl + "defaultProperties/" + defaultPropertiesStr, url);
  }

  
  @Test
  public void uriMarketData() throws UnsupportedEncodingException {
    String snapshotId = "Foo~1";
    UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    URI uri = DependencyGraphTraceProviderResource.uriMarketData(_baseUri, marketData);
    String url = decode(uri);
    assertEquals(s_testUrl + "marketDataSnapshot/" + snapshotId, url);
  }

  @Test
  public void uriResolutionTime() throws UnsupportedEncodingException {
    String rtStr = "V1970-01-01T00:00:01Z.CLATEST";
    VersionCorrection rt = VersionCorrection.parse(rtStr);
    URI uri = DependencyGraphTraceProviderResource.uriResolutionTime(_baseUri, rt);
    String url = decode(uri);
    assertEquals(s_testUrl + "resolutionTime/" + rtStr, url);
  }

  @Test
  public void uriValuationTime() throws UnsupportedEncodingException {
    String instantStr = "2013-06-24T12:18:01.094Z";
    Instant instant = Instant.parse(instantStr);
    URI uri = DependencyGraphTraceProviderResource.uriValuationTime(_baseUri, instant);
    String url = decode(uri);
    assertEquals(s_testUrl + "valuationTime/" + instantStr, url);
  }

  @Test
  public void uriValueRequirementByExternalId() throws UnsupportedEncodingException {
    String valueName = "test1";
    String targetType = "test2";
    String idStr = "GOLDMAN~Foo1";
    ExternalId id = ExternalId.parse(idStr);
    URI uri = DependencyGraphTraceProviderResource.uriValueRequirementByExternalId(_baseUri, valueName, targetType, id);
    String url = decode(uri);
    assertEquals(s_testUrl + "requirement/" + valueName + "/" + targetType + "/" + idStr, url);
  }

  @Test
  public void uriValueRequirementByUniqueId() throws UnsupportedEncodingException {
    String valueName = "test1";
    String targetType = "test2";
    String idStr = "GOLDMAN~Foo1";
    UniqueId id = UniqueId.parse(idStr);
    URI uri = DependencyGraphTraceProviderResource.uriValueRequirementByUniqueId(_baseUri, valueName, targetType, id);
    String url = decode(uri);
    assertEquals(s_testUrl + "value/" + valueName + "/" + targetType + "/" + idStr, url);
  }

  private String decode(URI uriDefaultProperties) throws UnsupportedEncodingException {
    String urlStr = uriDefaultProperties.toString();
    String decoded = URLDecoder.decode(urlStr, "UTF-8");
    return decoded;
  }
  
}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.provider.DependencyGraphTraceProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link DependencyGraphTraceProviderResource}
 */
@Test(groups = TestGroup.UNIT)
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
    String originalName = _resource.getProperties().getCalculationConfigurationName();

    DependencyGraphTraceProviderResource newResource = _resource.setCalculationConfigurationName(calcConfigName);

    assertEquals(originalName, _resource.getProperties().getCalculationConfigurationName());
    assertEquals(calcConfigName, newResource.getProperties().getCalculationConfigurationName());

  }

  @Test
  public void getTraceWithDefaultProperties() {
    //input
    String defaultProperties = "A=[foo,bar],B=*";

    //expected arg
    ValueProperties props = ValueProperties.parse(defaultProperties);
    ValueProperties originalProps = _resource.getProperties().getDefaultProperties();

    DependencyGraphTraceProviderResource newResource = _resource.setDefaultProperties(defaultProperties);

    assertEquals(props, newResource.getProperties().getDefaultProperties());
    assertEquals(originalProps, _resource.getProperties().getDefaultProperties());

  }

  @Test
  public void getTraceWithMarketData() {
    //input
    String snapshotId = "Foo~1";

    //expected arg
    List<MarketDataSpecification> marketData = Lists.<MarketDataSpecification>newArrayList(MarketData.user(UniqueId.parse(snapshotId)));
    List<MarketDataSpecification> originalMD = _resource.getProperties().getMarketData();

    DependencyGraphTraceProviderResource newResource = _resource.setMarketDataSnapshot(snapshotId);

    assertEquals(marketData, newResource.getProperties().getMarketData());
    assertEquals(originalMD, _resource.getProperties().getMarketData());
  }

  @Test
  public void getTraceWithResolutionTime() {
    //input
    String resolutionTime = "V1970-01-01T00:00:01Z.CLATEST";

    //expected arg
    VersionCorrection parsed = VersionCorrection.parse(resolutionTime);
    VersionCorrection originalRT = _resource.getProperties().getResolutionTime();

    DependencyGraphTraceProviderResource newResource = _resource.setResolutionTime(resolutionTime);

    assertEquals(parsed, newResource.getProperties().getResolutionTime());
    assertEquals(originalRT, _resource.getProperties().getResolutionTime());
  }

  @Test
  public void getTraceWithValuationTime() {
    //input
    String valuationTimeStr = "2013-06-24T12:18:01.094Z";

    //expected arg
    Instant valuationTime = Instant.parse(valuationTimeStr);
    Instant originalVT = _resource.getProperties().getValuationTime();

    DependencyGraphTraceProviderResource newResource = _resource.setValuationTime(valuationTimeStr);

    assertEquals(valuationTime, newResource.getProperties().getValuationTime());
    assertEquals(originalVT, _resource.getProperties().getValuationTime());

  }

  @Test
  public void getTraceWithValueRequirementByExternalId() {
    //input
    String valueName = "name";
    String targetType = "POSITION";
    String externalId = "Foo~1";

    //expected arg
    ComputationTargetType expectedTargetType = ComputationTargetType.POSITION;
    ExternalId expectedExternalId = ExternalId.parse(externalId);
    ValueRequirement valueRequirement = new ValueRequirement(valueName, new ComputationTargetRequirement(expectedTargetType, expectedExternalId));

    DependencyGraphTraceProviderResource newResource = _resource.setValueRequirementByExternalId(valueName, targetType, externalId);

    assertTrue(newResource.getProperties().getRequirements().contains(valueRequirement));

  }

  @Test
  public void getTraceWithValueRequirementByUniqueId() {
    //input
    String valueName = "name";
    String targetType = "POSITION";
    String uniqueId = "Foo~1";

    //expected arg
    UniqueId expectedUniqueId = UniqueId.parse(uniqueId);
    ComputationTargetType expectedTargetType = ComputationTargetType.POSITION;
    ValueRequirement valueRequirement = new ValueRequirement(valueName, new ComputationTargetSpecification(expectedTargetType, expectedUniqueId));

    DependencyGraphTraceProviderResource newResource = _resource.setValueRequirementByUniqueId(valueName, targetType, uniqueId);

    assertTrue(newResource.getProperties().getRequirements().contains(valueRequirement));
  }

  //-----------------------------------------------------------

  @Test
  public void build() {

    when(_provider.getTrace(_resource.getProperties())).thenReturn(_sampleResult);

    FudgeMsgEnvelope result = _resource.build();

    verify(_provider).getTrace(_resource.getProperties());
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
    String defaultPropertiesStr1 = "{A=[foo,bar],B=[*]}";
    String defaultPropertiesStr2 = "{A=[bar,foo],B=[*]}";
    ValueProperties parsed = ValueProperties.parse(defaultPropertiesStr1);
    URI uri = DependencyGraphTraceProviderResource.uriDefaultProperties(_baseUri, parsed);
    String url = decode(uri);
    assertTrue(url.equals(s_testUrl + "defaultProperties/" + defaultPropertiesStr1) ||
        url.equals(s_testUrl + "defaultProperties/" + defaultPropertiesStr2));
  }

  @Test
  public void uriMarketDataSnapshot() throws UnsupportedEncodingException {
    String snapshotId = "Foo~1";
    MarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    URI uri = DependencyGraphTraceProviderResource.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    String url = decode(uri);
    assertEquals(s_testUrl + "marketDataSnapshot/" + snapshotId, url);
  }
  @Test
  public void uriMarketDataLiveDefault() throws UnsupportedEncodingException {
    MarketDataSpecification marketData = MarketData.live();
    URI uri = DependencyGraphTraceProviderResource.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    String url = decode(uri);
    assertEquals(s_testUrl + "marketDataLiveDefault", url);
  }

  @Test
  public void uriMarketDataLive() throws UnsupportedEncodingException {
    MarketDataSpecification marketData = MarketData.live("BB");
    URI uri = DependencyGraphTraceProviderResource.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    String url = decode(uri);
    assertEquals(s_testUrl + "marketDataLive/BB", url);
  }
  @Test
  public void uriMarketDataHistorical() throws UnsupportedEncodingException {
    LocalDate now = LocalDate.now();
    MarketDataSpecification marketData = MarketData.historical(now, "ts");
    URI uri = DependencyGraphTraceProviderResource.uriMarketData(_baseUri, Lists.newArrayList(marketData));
    String url = decode(uri);
    assertEquals(s_testUrl + "marketDataHistorical/" + now + "/ts", url);
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

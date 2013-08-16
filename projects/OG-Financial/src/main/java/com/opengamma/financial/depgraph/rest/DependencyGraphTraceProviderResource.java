/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.annotations.VisibleForTesting;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.depgraph.provider.DependencyGraphTraceProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Expose a simple dependency graph building service over the network for debugging/diagnostic purposes. This is intended to be simple to access using hand written URLs - there is not currently a
 * corresponding programatic interface to the service this provides.
 * <p>
 * For example to find out why a graph building configuration can't satisfy a requirement, a URL such as "/value/Present Value/SECURITY/SecDb~1234" will return the failure trace (or the graph if
 * successful).
 */
public final class DependencyGraphTraceProviderResource extends AbstractDataResource {

  private final DependencyGraphTraceProvider _provider;

  private final FudgeContext _fudgeContext;

  private final DependencyGraphTraceBuilderProperties _properties;

  public DependencyGraphTraceProviderResource(final DependencyGraphTraceProvider provider, final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
    _provider = provider;
    _properties = new DependencyGraphTraceBuilderProperties();
  }

  private DependencyGraphTraceProviderResource(DependencyGraphTraceProvider provider, FudgeContext fudgeContext, DependencyGraphTraceBuilderProperties properties) {
    _fudgeContext = fudgeContext;
    _provider = provider;
    _properties = properties;
  }

  @VisibleForTesting
  FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @VisibleForTesting
  DependencyGraphTraceProvider getProvider() {
    return _provider;
  }

  @VisibleForTesting
  DependencyGraphTraceBuilderProperties getProperties() {
    return _properties;
  }

  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @Path("valuationTime/{valuationTime}")
  public DependencyGraphTraceProviderResource setValuationTime(@PathParam("valuationTime") final String valuationTime) {
    Instant parsedValuationTime = Instant.parse(valuationTime);
    DependencyGraphTraceBuilderProperties properties = _properties.valuationTime(parsedValuationTime);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("resolutionTime/{resolutionTime}")
  public DependencyGraphTraceProviderResource setResolutionTime(@PathParam("resolutionTime") final String resolutionTime) {
    VersionCorrection parsedResolutionTime = VersionCorrection.parse(resolutionTime);
    DependencyGraphTraceBuilderProperties properties = _properties.resolutionTime(parsedResolutionTime);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("calculationConfigurationName/{calculationConfigurationName}")
  public DependencyGraphTraceProviderResource setCalculationConfigurationName(@PathParam("calculationConfigurationName") final String calculationConfigurationName) {
    DependencyGraphTraceBuilderProperties properties = _properties.calculationConfigurationName(calculationConfigurationName);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("defaultProperties/{defaultProperties}")
  public DependencyGraphTraceProviderResource setDefaultProperties(@PathParam("defaultProperties") final String defaultProperties) {
    ValueProperties valueProperties = ValueProperties.parse(defaultProperties);
    DependencyGraphTraceBuilderProperties properties = _properties.defaultProperties(valueProperties);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("value/{valueName}/{targetType}/{targetId}")
  public DependencyGraphTraceProviderResource setValueRequirementByUniqueId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    UniqueId uniqueId = UniqueId.parse(targetId);
    ValueRequirement valueRequirement = toValueRequirement(valueName, new ComputationTargetSpecification(ComputationTargetType.parse(targetType), uniqueId));
    DependencyGraphTraceBuilderProperties properties = _properties.addRequirement(valueRequirement);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("requirement/{valueName}/{targetType}/{targetId}")
  public DependencyGraphTraceProviderResource setValueRequirementByExternalId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    ExternalId externalId = ExternalId.parse(targetId);
    ValueRequirement valueRequirement = toValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.parse(targetType), externalId));
    DependencyGraphTraceBuilderProperties properties = _properties.addRequirement(valueRequirement);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("marketDataSnapshot/{snapshotId}")
  public DependencyGraphTraceProviderResource setMarketDataSnapshot(@PathParam("snapshotId") final String snapshotId) {
    UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }
  
  @Path("marketDataLiveDefault")
  public DependencyGraphTraceProviderResource setMarketDataLiveDefault() {
    MarketDataSpecification marketData = MarketData.live();
    DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }
  
  @Path("marketDataLive/{dataSource}")
  public DependencyGraphTraceProviderResource setMarketDataLive(@PathParam("dataSource") final String dataSource) {
    MarketDataSpecification marketData = MarketData.live(dataSource);
    DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @Path("marketDataHistorical/{localDate}/{timeSeriesResolverKey}")
  public DependencyGraphTraceProviderResource setMarketDataHistorical(@PathParam("localDate") final String localDateStr, @PathParam("timeSeriesResolverKey") final String timeSeriesResolverKey) {
    LocalDate localDate = LocalDate.parse(localDateStr);
    MarketDataSpecification marketData = MarketData.historical(localDate, timeSeriesResolverKey);
    DependencyGraphTraceBuilderProperties properties = _properties.addMarketData(marketData);
    return new DependencyGraphTraceProviderResource(_provider, _fudgeContext, properties);
  }

  @GET
  @Path("build")
  public FudgeMsgEnvelope build() {
    DependencyGraphBuildTrace result = _provider.getTrace(_properties);
    MutableFudgeMsg fudgeMsg = new FudgeSerializer(_fudgeContext).objectToFudgeMsg(result);
    return new FudgeMsgEnvelope(fudgeMsg);
  }

  private ValueRequirement toValueRequirement(final String valueName, final ComputationTargetReference target) {
    final String name;
    final ValueProperties constraints;
    final int i = valueName.indexOf('{');
    if ((i > 0) && (valueName.charAt(valueName.length() - 1) == '}')) {
      name = valueName.substring(0, i);
      constraints = ValueProperties.parse(valueName.substring(i));
    } else {
      name = valueName;
      constraints = ValueProperties.none();
    }
    return new ValueRequirement(name, target, constraints);
  }

  // ------------------------------------------

  /**
   * Builds URI for remote access to getTraceWithCalculationConfigurationName.
   * @param baseUri the base uri
   * @param calculationConfigurationName the calculation configuration name
   * @return the URI
   */
  public static URI uriCalculationConfigurationName(URI baseUri, String calculationConfigurationName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("calculationConfigurationName/{calculationConfigurationName}");
    return bld.build(calculationConfigurationName);
  }

  /**
   * Builds URI for remote access to getTraceWithValuationTime.
   * @param baseUri the base uri
   * @param valuationInstant the valuation time
   * @return the URI
   */
  public static URI uriValuationTime(URI baseUri, Instant valuationInstant) {
    String valuationInstantStr = valuationInstant.toString();
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("valuationTime/{valuationTime}");
    return bld.build(valuationInstantStr);
  }

  /**
   * Builds URI for remote access to getTraceWithResolutionTime.
   * @param baseUri the base uri
   * @param resolutionTime the resolution time
   * @return the URI
   */
  public static URI uriResolutionTime(URI baseUri, VersionCorrection resolutionTime) {
    String resolutionTimeStr = resolutionTime.toString();
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("resolutionTime/{resolutionTime}");
    return bld.build(resolutionTimeStr);
  }

  /**
   * Builds URI for remote access to getTraceWithDefaultProperties.
   * @param baseUri the base uri
   * @param defaultProperties the default properties
   * @return the URI
   */
  public static URI uriDefaultProperties(URI baseUri, ValueProperties defaultProperties) {
    String defaultPropertiesStr = defaultProperties.toString();
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("defaultProperties/{defaultProperties}");
    return bld.build(defaultPropertiesStr);
  }

  /**
   * Builds URI for remote access to getTraceWithMarketData.
   * @param baseUri the base uri
   * @param marketData the market data
   * @return the URI
   */
  public static URI uriMarketData(URI baseUri, List<MarketDataSpecification> marketData) {
    for (MarketDataSpecification mdSpec : marketData) {
      if (mdSpec instanceof UserMarketDataSpecification) {
        String snapshotId = ((UserMarketDataSpecification) mdSpec).getUserSnapshotId().toString();
        UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataSnapshot/{snapshotId}");
        baseUri = bld.build(snapshotId);
      } else if (mdSpec instanceof LiveMarketDataSpecification) {
        String dataSource = ((LiveMarketDataSpecification) mdSpec).getDataSource();
        if (dataSource == null) {
          UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataLiveDefault");
          baseUri = bld.build();
        } else {
          UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataLive/{dataSource}");
          baseUri = bld.build(dataSource);
        }
      } else if (mdSpec instanceof FixedHistoricalMarketDataSpecification) {
        String snapshotDate = ((FixedHistoricalMarketDataSpecification) mdSpec).getSnapshotDate().toString();
        String timeSeriesResolverKey = ((FixedHistoricalMarketDataSpecification) mdSpec).getTimeSeriesResolverKey();
        UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataHistorical/{localDate}/{timeSeriesResolverKey}");
        baseUri = bld.build(snapshotDate, timeSeriesResolverKey);
      }
      
    }
    return baseUri;
  }

  /**
   * Builds URI for remote access to getTraceWithValueRequirementByUniqueId
   * @param baseUri the base uri
   * @param valueName the value name
   * @param targetType the target type
   * @param uniqueId the unique id
   * @return the URI
   */
  public static URI uriValueRequirementByUniqueId(URI baseUri, String valueName, String targetType, UniqueId uniqueId) {
    String uniqueIdStr = uniqueId.toString();
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("value/{valueName}/{targetType}/{targetId}");
    return bld.build(valueName, targetType, uniqueIdStr);
  }

  /**
   * Builds URI for remote access to getTraceWithValueRequirementByExternalId
   * @param baseUri the base uri
   * @param valueName the value name
   * @param targetType the target type
   * @param externalId the external id
   * @return the URI
   */
  public static URI uriValueRequirementByExternalId(URI baseUri, String valueName, String targetType, ExternalId externalId) {
    String externalIdStr = externalId.toString();
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("requirement/{valueName}/{targetType}/{targetId}");
    return bld.build(valueName, targetType, externalIdStr);
  }

  /**
   * The build call. This must be called after all of the other parameters have been added.
   * @param baseUri the uri with all params added
   * @return the uri complete with build call
   */
  public static URI uriBuild(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("build");
    return bld.build();
  }

}

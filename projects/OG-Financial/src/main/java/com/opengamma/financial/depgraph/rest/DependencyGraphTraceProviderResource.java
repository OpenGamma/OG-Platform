/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.net.URI;

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

import com.google.common.annotations.VisibleForTesting;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
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

  private final FudgeContext _fudgeContext;

  private final DependencyGraphTraceProvider _provider;

  public DependencyGraphTraceProviderResource(final DependencyGraphTraceProvider provider, final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
    _provider = provider;
  }

  @VisibleForTesting
  FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @VisibleForTesting
  DependencyGraphTraceProvider getProvider() {
    return _provider;
  }

  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @Path("valuationTime/{valuationTime}")
  @GET
  public FudgeMsgEnvelope getTraceWithValuationTime(@PathParam("valuationTime") final String valuationTime) {
    Instant parsedValuationTime = Instant.parse(valuationTime);
    DependencyGraphBuildTrace trace = _provider.getTraceWithValuationTime(parsedValuationTime);
    return serialize(trace);
  }

  @Path("resolutionTime/{resolutionTime}")
  @GET
  public FudgeMsgEnvelope getTraceWithResolutionTime(@PathParam("resolutionTime") final String resolutionTime) {
    VersionCorrection parsedResolutionTime = VersionCorrection.parse(resolutionTime);
    DependencyGraphBuildTrace trace = _provider.getTraceWithResolutionTime(parsedResolutionTime);
    return serialize(trace);
  }

  @Path("calculationConfigurationName/{calculationConfigurationName}")
  @GET
  public FudgeMsgEnvelope getTraceWithCalculationConfigurationName(@PathParam("calculationConfigurationName") final String calculationConfigurationName) {
    DependencyGraphBuildTrace trace = _provider.getTraceWithCalculationConfigurationName(calculationConfigurationName);
    return serialize(trace);
  }

  @Path("defaultProperties/{defaultProperties}")
  @GET
  public FudgeMsgEnvelope getTraceWithDefaultProperties(@PathParam("defaultProperties") final String defaultProperties) {
    ValueProperties properties = ValueProperties.parse(defaultProperties);
    DependencyGraphBuildTrace trace = _provider.getTraceWithDefaultProperties(properties);
    return serialize(trace);
  }

  @Path("value/{valueName}/{targetType}/{targetId}")
  @GET
  public FudgeMsgEnvelope getTraceWithValueRequirementByUniqueId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    DependencyGraphBuildTrace trace = _provider.getTraceWithValueRequirementByUniqueId(valueName, targetType, UniqueId.parse(targetId));
    return serialize(trace);
  }

  @Path("requirement/{valueName}/{targetType}/{targetId}")
  @GET
  public FudgeMsgEnvelope getTraceWithValueRequirementByExternalId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    DependencyGraphBuildTrace trace = _provider.getTraceWithValueRequirementByExternalId(valueName, targetType, ExternalId.parse(targetId));
    return serialize(trace);
  }

  @Path("marketDataSnapshot/{snapshotId}")
  @GET
  public FudgeMsgEnvelope getTraceWithMarketData(@PathParam("snapshotId") final String snapshotId) {
    UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    DependencyGraphBuildTrace trace = _provider.getTraceWithMarketData(marketData);
    return serialize(trace);
  }

  private FudgeMsgEnvelope serialize(DependencyGraphBuildTrace trace) {
    MutableFudgeMsg fudgeMsg = new FudgeSerializer(_fudgeContext).objectToFudgeMsg(trace);
    return new FudgeMsgEnvelope(fudgeMsg);
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
  public static URI uriMarketData(URI baseUri, UserMarketDataSpecification marketData) {
    String snapshotId = marketData.getUserSnapshotId().toString();
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("marketDataSnapshot/{snapshotId}");
    return bld.build(snapshotId);
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

}

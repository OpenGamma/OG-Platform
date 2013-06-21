/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

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
    Instant valuationTimeInstant = ZonedDateTime.parse(valuationTime).toInstant();
    DependencyGraphBuildTrace trace = _provider.getTraceWithValuationTime(valuationTimeInstant);
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


}

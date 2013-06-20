/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
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
public final class DependencyGraphBuilderResource extends AbstractDataResource {

  private final FudgeContext _fudgeContext;

  private final DependencyGraphTraceBuilder _traceBuilder;
  
  public DependencyGraphBuilderResource(final DependencyGraphBuilderResourceContextBean builderContext, final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
    _traceBuilder = new DependencyGraphTraceBuilder(builderContext);
  }

  @Path("valuationTime/{valuationTime}")
  @GET
  public FudgeMsgEnvelope getTraceWithValuationTime(@PathParam("valuationTime") final String valuationTime) {
    Instant valuationTimeInstant = ZonedDateTime.parse(valuationTime).toInstant();
    DependencyGraphBuildTrace trace = _traceBuilder.valuationTime(valuationTimeInstant).build();
    return serialize(trace);
  }

  @Path("resolutionTime/{resolutionTime}")
  @GET
  public FudgeMsgEnvelope getTraceWithResolutionTime(@PathParam("resolutionTime") final String resolutionTime) {
    VersionCorrection parsedResolutionTime = VersionCorrection.parse(resolutionTime);
    DependencyGraphBuildTrace trace = _traceBuilder.resolutionTime(parsedResolutionTime).build();
    return serialize(trace);
  }
  
  @Path("calculationConfigurationName/{calculationConfigurationName}")
  @GET
  public FudgeMsgEnvelope getTraceWithCalculationConfigurationName(@PathParam("calculationConfigurationName") final String calculationConfigurationName) {
    DependencyGraphBuildTrace trace = _traceBuilder.calculationConfigurationName(calculationConfigurationName).build();
    return serialize(trace);
  }

  @Path("defaultProperties/{defaultProperties}")
  @GET
  public FudgeMsgEnvelope getTraceWithDefaultProperties(@PathParam("defaultProperties") final String defaultProperties) {
    ValueProperties properties = ValueProperties.parse(defaultProperties);
    DependencyGraphBuildTrace trace = _traceBuilder.defaultProperties(properties).build();
    return serialize(trace);
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

  @Path("value/{valueName}/{targetType}/{targetId}")
  @GET
  public FudgeMsgEnvelope getTraceWithValueRequirementByUniqueId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    ValueRequirement requirement = toValueRequirement(valueName, new ComputationTargetSpecification(ComputationTargetType.parse(targetType.replace('-', '/')), UniqueId.parse(targetId)));
    DependencyGraphBuildTrace trace = _traceBuilder.addRequirement(requirement).build();
    return serialize(trace);
  }

  @Path("requirement/{valueName}/{targetType}/{targetId}")
  @GET
  public FudgeMsgEnvelope getTraceWithValueRequirementByExternalId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    ValueRequirement requirement = toValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.parse(targetType.replace('-', '/')), ExternalId.parse(targetId)));
    DependencyGraphBuildTrace trace = _traceBuilder.addRequirement(requirement).build();
    return serialize(trace);
  }

  @Path("marketDataSnapshot/{snapshotId}")
  @GET
  public FudgeMsgEnvelope getTraceWithMarketData(@PathParam("snapshotId") final String snapshotId) {
    UserMarketDataSpecification marketData = MarketData.user(UniqueId.parse(snapshotId));
    DependencyGraphBuildTrace trace = _traceBuilder.marketData(marketData).build();
    return serialize(trace);
  }
  
  private FudgeMsgEnvelope serialize(DependencyGraphBuildTrace trace) {
    MutableFudgeMsg fudgeMsg = new FudgeSerializer(_fudgeContext).objectToFudgeMsg(trace);
    return new FudgeMsgEnvelope(fudgeMsg);
  }


}

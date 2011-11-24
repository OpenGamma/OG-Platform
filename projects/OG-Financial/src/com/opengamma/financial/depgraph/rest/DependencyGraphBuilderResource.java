/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.ZonedDateTime;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.ResolutionFailureGatherer;
import com.opengamma.engine.fudgemsg.ResolutionFailureFudgeBuilder;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public final class DependencyGraphBuilderResource {

  private final DependencyGraphBuilderResourceContextBean _builderContext;
  private final FudgeContext _fudgeContext;

  private String _calculationConfigurationName;
  private Instant _valuationTime;
  private ValueProperties _defaultProperties;
  private Collection<ValueRequirement> _requirements;
  private MarketDataSpecification _marketData;

  public DependencyGraphBuilderResource(final DependencyGraphBuilderResourceContextBean builderContext, final FudgeContext fudgeContext) {
    _builderContext = builderContext;
    _fudgeContext = fudgeContext;
    _calculationConfigurationName = "Default";
    _valuationTime = Instant.now();
    _defaultProperties = ValueProperties.none();
    _requirements = Collections.emptyList();
    _marketData = MarketData.live();
  }

  protected DependencyGraphBuilderResource(final DependencyGraphBuilderResource copyFrom) {
    _builderContext = copyFrom.getBuilderContext();
    _fudgeContext = copyFrom.getFudgeContext();
    _valuationTime = copyFrom.getValuationTime();
    _calculationConfigurationName = copyFrom.getCalculationConfigurationName();
    _defaultProperties = copyFrom.getDefaultProperties();
    _requirements = new ArrayList<ValueRequirement>(copyFrom.getRequirements());
    _marketData = copyFrom.getMarketData();
  }

  protected DependencyGraphBuilderResourceContextBean getBuilderContext() {
    return _builderContext;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  protected Instant getValuationTime() {
    return _valuationTime;
  }

  protected ValueProperties getDefaultProperties() {
    return _defaultProperties;
  }

  protected Collection<ValueRequirement> getRequirements() {
    return _requirements;
  }

  protected MarketDataSpecification getMarketData() {
    return _marketData;
  }

  @Path("valuationTime/{valuationTime}")
  public DependencyGraphBuilderResource setValuationTime(@PathParam("valuationTime") final String valuationTime) {
    final DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(this);
    resource._valuationTime = Instant.of(ZonedDateTime.parse(valuationTime));
    return resource;
  }

  @Path("calculationConfigurationName/{calculationConfigurationName}")
  public DependencyGraphBuilderResource setCalculationConfigurationName(@PathParam("calculationConfigurationName") final String calculationConfigurationName) {
    final DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(this);
    resource._calculationConfigurationName = calculationConfigurationName;
    return resource;
  }

  @Path("defaultProperties/{defaultProperties}")
  public DependencyGraphBuilderResource setDefaultProperties(@PathParam("defaultProperties") final String defaultProperties) {
    final DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(this);
    resource._defaultProperties = ValueProperties.parse(defaultProperties);
    return resource;
  }

  @Path("value/{valueName}/{targetType}/{targetId}")
  public DependencyGraphBuilderResource addValueRequirement(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    final DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(this);
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
    final ValueRequirement requirement = new ValueRequirement(name, new ComputationTargetSpecification(ComputationTargetType.valueOf(targetType), UniqueId.parse(targetId)), constraints);
    resource.getRequirements().add(requirement);
    return resource;
  }

  @Path("marketDataSnapshot/{snapshotId}")
  public DependencyGraphBuilderResource setMarketData(@PathParam("snapshotId") final String snapshotId) {
    final DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(this);
    resource._marketData = MarketData.user(UniqueId.parse(snapshotId));
    return resource;
  }

  @GET
  public FudgeMsgEnvelope build() {
    final DependencyGraphBuilder builder = getBuilderContext().getDependencyGraphBuilderFactory().newInstance();
    builder.setCalculationConfigurationName(getCalculationConfigurationName());
    final FunctionCompilationContext context = getBuilderContext().getFunctionCompilationContext().clone();
    final ViewDefinition definition = new ViewDefinition("Mock View", "Test");
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(definition, getCalculationConfigurationName());
    calcConfig.setDefaultProperties(getDefaultProperties());
    context.setViewCalculationConfiguration(calcConfig);
    builder.setCompilationContext(context);
    final Collection<ResolutionRule> rules = getBuilderContext().getFunctionResolver().compile(getValuationTime()).getAllResolutionRules();
    // TODO: allow transformation rules
    builder.setFunctionResolver(new DefaultCompiledFunctionResolver(context, rules));
    builder.setMarketDataAvailabilityProvider(getBuilderContext().getMarketDataProviderResolver().resolve(getMarketData()).getAvailabilityProvider());
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final ResolutionFailureGatherer<MutableFudgeMsg> failures = new ResolutionFailureGatherer<MutableFudgeMsg>(new ResolutionFailureFudgeBuilder.Visitor(serializer));
    builder.setResolutionFailureVisitor(failures);
    builder.setTargetResolver(getBuilderContext().getComputationTargetResolver());
    for (ValueRequirement requirement : getRequirements()) {
      builder.addTarget(requirement);
    }
    final MutableFudgeMsg result = serializer.newMessage();
    final DependencyGraph graph = builder.getDependencyGraph();
    serializer.addToMessage(result, "dependencyGraph", null, graph);
    final Map<Throwable, Integer> exceptions = builder.getExceptions();
    for (Map.Entry<Throwable, Integer> exception : exceptions.entrySet()) {
      final MutableFudgeMsg submessage = serializer.newMessage();
      submessage.add("class", exception.getKey().getClass().getName());
      submessage.add("message", exception.getKey().getMessage());
      if (exception.getValue() > 1) {
        submessage.add("repeat", exception.getValue());
      }
      result.add("exception", submessage);
    }
    for (MutableFudgeMsg failure : failures.getResults()) {
      result.add("failure", failure);
    }
    return new FudgeMsgEnvelope(result);
  }

}

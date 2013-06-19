/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureGatherer;
import com.opengamma.engine.depgraph.SimpleResolutionFailureVisitor;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.DefaultCompiledFunctionResolver;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Expose a simple dependency graph building service over the network for debugging/diagnostic purposes. This is intended to be simple to access using hand written URLs - there is not currently a
 * corresponding programatic interface to the service this provides.
 * <p>
 * For example to find out why a graph building configuration can't satisfy a requirement, a URL such as "/value/Present Value/SECURITY/SecDb~1234" will return the failure trace (or the graph if
 * successful).
 */
public final class DependencyGraphBuilderResource extends AbstractDataResource {

  private final DependencyGraphBuilderResourceContextBean _builderContext;
  private final FudgeContext _fudgeContext;

  private String _calculationConfigurationName;
  private Instant _valuationTime;
  private final VersionCorrection _resolutionTime;
  private ValueProperties _defaultProperties;
  private final Collection<ValueRequirement> _requirements;
  private MarketDataSpecification _marketData;

  public DependencyGraphBuilderResource(final DependencyGraphBuilderResourceContextBean builderContext, final FudgeContext fudgeContext) {
    _builderContext = builderContext;
    _fudgeContext = fudgeContext;
    _calculationConfigurationName = "Default";
    _valuationTime = null;
    _resolutionTime = VersionCorrection.LATEST;
    _defaultProperties = ValueProperties.none();
    _requirements = Collections.emptyList();
    _marketData = MarketData.live();
  }

  protected DependencyGraphBuilderResource(final DependencyGraphBuilderResource copyFrom) {
    _builderContext = copyFrom._builderContext;
    _fudgeContext = copyFrom._fudgeContext;
    _valuationTime = copyFrom._valuationTime;
    _resolutionTime = copyFrom._resolutionTime;
    _calculationConfigurationName = copyFrom._calculationConfigurationName;
    _defaultProperties = copyFrom._defaultProperties;
    _requirements = new ArrayList<ValueRequirement>(copyFrom._requirements);
    _marketData = copyFrom._marketData;
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

  protected VersionCorrection getResolutionTime() {
    return _resolutionTime;
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
    resource._valuationTime = ZonedDateTime.parse(valuationTime).toInstant();
    return resource;
  }

  // TODO: set resolutionTime method

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

  protected DependencyGraphBuilderResource addValueRequirement(final String valueName, final ComputationTargetReference target) {
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
    final ValueRequirement requirement = new ValueRequirement(name, target, constraints);
    resource.getRequirements().add(requirement);
    return resource;
  }

  @Path("value/{valueName}/{targetType}/{targetId}")
  public DependencyGraphBuilderResource addValueRequirementByUniqueId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    return addValueRequirement(valueName, new ComputationTargetSpecification(ComputationTargetType.parse(targetType.replace('-', '/')), UniqueId.parse(targetId)));
  }

  @Path("requirement/{valueName}/{targetType}/{targetId}")
  public DependencyGraphBuilderResource addValueRequirementByExternalId(@PathParam("valueName") final String valueName, @PathParam("targetType") final String targetType,
      @PathParam("targetId") final String targetId) {
    return addValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.parse(targetType.replace('-', '/')), ExternalId.parse(targetId)));
  }

  @Path("marketDataSnapshot/{snapshotId}")
  public DependencyGraphBuilderResource setMarketData(@PathParam("snapshotId") final String snapshotId) {
    final DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(this);
    resource._marketData = MarketData.user(UniqueId.parse(snapshotId));
    return resource;
  }

  @GET
  public FudgeMsgEnvelope build() {
    final DependencyGraphBuilder builder = _builderContext.getDependencyGraphBuilderFactory().newInstance();
    builder.setCalculationConfigurationName(_calculationConfigurationName);
    final FunctionCompilationContext context = _builderContext.getFunctionCompilationContext().clone();
    final ViewDefinition definition = new ViewDefinition("Mock View", "Test");
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(definition, _calculationConfigurationName);
    calcConfig.setDefaultProperties(_defaultProperties);
    context.setViewCalculationConfiguration(calcConfig);
    context.setComputationTargetResolver(context.getRawComputationTargetResolver().atVersionCorrection(_resolutionTime));
    builder.setCompilationContext(context);
    final Collection<ResolutionRule> rules = _builderContext.getFunctionResolver().compile((_valuationTime != null) ? _valuationTime : Instant.now()).getAllResolutionRules();
    // TODO: allow transformation rules
    final DefaultCompiledFunctionResolver functions = new DefaultCompiledFunctionResolver(context, rules);
    functions.compileRules();
    builder.setFunctionResolver(functions);
    builder.setFunctionExclusionGroups(_builderContext.getFunctionExclusionGroups());
    // TODO this isn't used. is this OK?
    // TODO it's a bit nasty to build a MarketDataProvider just to get its availability provider
    final UserPrincipal marketDataUser = UserPrincipal.getLocalUser();
    final MarketDataProviderResolver resolver = _builderContext.getMarketDataProviderResolver();
    final MarketDataProvider marketDataProvider = resolver.resolve(marketDataUser, _marketData);
    builder.setMarketDataAvailabilityProvider(marketDataProvider.getAvailabilityProvider(_marketData));
    final ResolutionFailureGatherer<List<ResolutionFailure>> failures = new ResolutionFailureGatherer<>(new SimpleResolutionFailureVisitor());
    builder.setResolutionFailureVisitor(failures);
    builder.setDisableFailureReporting(false);
    for (final ValueRequirement requirement : _requirements) {
      builder.addTarget(requirement);
    }
    DependencyGraph dependencyGraph = builder.getDependencyGraph();
    List<ResolutionFailure> resolutionFailures = ImmutableList.copyOf(Iterables.concat(failures.getResults()));
    
    DependencyGraphBuildTrace graphBuildTrace = DependencyGraphBuildTrace.of(
                                      dependencyGraph, 
                                      builder.getExceptions(), 
                                      resolutionFailures, 
                                      builder.getValueRequirementMapping());
    
    MutableFudgeMsg mutableFudgeMsg = new FudgeSerializer(getFudgeContext()).objectToFudgeMsg(graphBuildTrace);
    
    return new FudgeMsgEnvelope(mutableFudgeMsg);
  }
  

}

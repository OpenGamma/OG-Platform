/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import org.fudgemsg.FudgeContext;
import org.threeten.bp.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.calcnode.CalculationNodeLogEventListener;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.calcnode.SimpleCalculationNode;
import com.opengamma.engine.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.exec.DependencyGraphExecutorFactory;
import com.opengamma.engine.exec.SingleNodeExecutorFactory;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryNamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.SingleMarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessorFactoryBean;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.engine.view.listener.ViewResultListenerFactory;
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.engine.view.worker.ViewProcessWorker;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.log.LogBridge;
import com.opengamma.util.log.ThreadLocalLogEventListener;
import com.opengamma.util.test.TestLifecycle;

/**
 * Provides access to a ready-made and customisable view processing environment for testing.
 */
public class ViewProcessorTestEnvironment {

  public static final UserPrincipal TEST_USER = UserPrincipal.getLocalUser();

  public static final String TEST_VIEW_DEFINITION_NAME = "Test View";
  public static final String TEST_CALC_CONFIG_NAME = "Test Calc Config";

  private static final ComputationTargetSpecification s_primitiveTarget = ComputationTargetSpecification.of(UniqueId.of("Scheme", "PrimitiveValue"));
  private static final ValueRequirement s_primitive1 = new ValueRequirement("Value1", s_primitiveTarget);
  private static final ValueRequirement s_primitive2 = new ValueRequirement("Value2", s_primitiveTarget);

  // Settings
  private MarketDataProvider _marketDataProvider;
  private MarketDataProviderResolver _marketDataProviderResolver;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private FunctionExecutionContext _functionExecutionContext;
  private FunctionCompilationContext _functionCompilationContext;
  private ViewDefinition _viewDefinition;
  private FunctionRepository _functionRepository;
  private DependencyGraphExecutorFactory _dependencyGraphExecutorFactory;
  private DependencyGraphBuilderFactory _dependencyGraphBuilderFactory;

  // Environment
  private ViewProcessorImpl _viewProcessor;
  private FunctionResolver _functionResolver;
  private ConfigSource _configSource;
  private ViewResultListenerFactory _viewResultListenerFactory;

  public void init() {
    final ViewProcessorFactoryBean vpFactBean = new ViewProcessorFactoryBean();
    vpFactBean.setName("test");

    final FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    final SecuritySource securitySource = getSecuritySource() != null ? getSecuritySource() : generateSecuritySource();
    final FunctionCompilationContext functionCompilationContext = getFunctionCompilationContext() != null ? getFunctionCompilationContext() : generateFunctionCompilationContext();

    final ConfigSource configSource = getConfigSource() != null ? getConfigSource() : generateConfigSource();

    final InMemoryViewComputationCacheSource cacheSource = new InMemoryViewComputationCacheSource(fudgeContext);
    vpFactBean.setComputationCacheSource(cacheSource);

    final DependencyGraphExecutorFactory dependencyGraphExecutorFactory = getDependencyGraphExecutorFactory() != null ? getDependencyGraphExecutorFactory()
        : generateDependencyGraphExecutorFactory();
    vpFactBean.setDependencyGraphExecutorFactory(dependencyGraphExecutorFactory);

    final FunctionRepository functionRepository = getFunctionRepository() != null ? getFunctionRepository() : generateFunctionRepository();
    final CompiledFunctionService compiledFunctions = new CompiledFunctionService(functionRepository, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
    TestLifecycle.register(compiledFunctions);
    compiledFunctions.initialize();
    vpFactBean.setFunctionCompilationService(compiledFunctions);

    final MarketDataProviderResolver marketDataProviderResolver = getMarketDataProviderResolver() != null ? getMarketDataProviderResolver() : generateMarketDataProviderResolver();
    vpFactBean.setMarketDataProviderResolver(marketDataProviderResolver);

    vpFactBean.setConfigSource(configSource);
    vpFactBean.setViewPermissionProvider(new DefaultViewPermissionProvider());
    vpFactBean.setNamedMarketDataSpecificationRepository(new InMemoryNamedMarketDataSpecificationRepository());
    _configSource = configSource;

    final FunctionExecutionContext functionExecutionContext = getFunctionExecutionContext() != null ? getFunctionExecutionContext() : generateFunctionExecutionContext();
    functionExecutionContext.setSecuritySource(securitySource);

    final ThreadLocalLogEventListener threadLocalLogListener = new ThreadLocalLogEventListener();
    LogBridge.getInstance().addListener(threadLocalLogListener);
    final SimpleCalculationNode localCalcNode = new SimpleCalculationNode(cacheSource, compiledFunctions, functionExecutionContext, "node", null,
        new DiscardingInvocationStatisticsGatherer(), new CalculationNodeLogEventListener(threadLocalLogListener));
    final LocalNodeJobInvoker jobInvoker = new LocalNodeJobInvoker(localCalcNode);
    TestLifecycle.register(jobInvoker);
    vpFactBean.setComputationJobDispatcher(new JobDispatcher(jobInvoker));
    vpFactBean.setFunctionResolver(generateFunctionResolver(compiledFunctions));
    vpFactBean.setViewResultListenerFactory(_viewResultListenerFactory);
    _viewProcessor = (ViewProcessorImpl) vpFactBean.createObject();
    _viewProcessor.start();
    TestLifecycle.register(_viewProcessor);
  }

  public CompiledViewDefinitionWithGraphsImpl compileViewDefinition(final Instant valuationTime, final VersionCorrection versionCorrection) {
    if (getViewProcessor() == null) {
      throw new IllegalStateException(ViewProcessorTestEnvironment.class.getName() + " has not been initialised");
    }
    final ViewCompilationServices compilationServices = new ViewCompilationServices(getMarketDataProvider().getAvailabilityProvider(MarketData.live()), getFunctionResolver(),
        getFunctionCompilationContext(), getViewProcessor().getFunctionCompilationService().getExecutorService(),
        (getDependencyGraphBuilderFactory() != null) ? getDependencyGraphBuilderFactory() : generateDependencyGraphBuilderFactory());
    return ViewDefinitionCompiler.compile(getViewDefinition(), compilationServices, valuationTime, versionCorrection);
  }

  // Environment
  // -------------------------------------------------------------------------
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public void setViewDefinition(final ViewDefinition viewDefinition) {
    _viewDefinition = viewDefinition;
  }

  private ViewDefinition generateViewDefinition() {
    final ViewDefinition testDefinition = new ViewDefinition(UniqueId.of("boo", "far"), TEST_VIEW_DEFINITION_NAME, TEST_USER);
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(testDefinition, TEST_CALC_CONFIG_NAME);
    calcConfig.addSpecificRequirement(getPrimitive1());
    calcConfig.addSpecificRequirement(getPrimitive2());
    testDefinition.addViewCalculationConfiguration(calcConfig);

    setViewDefinition(testDefinition);
    return testDefinition;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public MockConfigSource getMockViewDefinitionRepository() {
    return (MockConfigSource) _configSource;
  }

  public void setConfigSource(final ConfigSource configSource) {
    _configSource = configSource;
  }

  public void setViewResultListenerFactory(final ViewResultListenerFactory viewResultListenerFactory) {
    _viewResultListenerFactory = viewResultListenerFactory;
  }

  private ConfigSource generateConfigSource() {
    final MockConfigSource repository = new MockConfigSource();
    final ViewDefinition defaultDefinition = getViewDefinition() != null ? getViewDefinition() : generateViewDefinition();
    repository.put(defaultDefinition);
    setConfigSource(repository);
    return repository;
  }

  public MarketDataProvider getMarketDataProvider() {
    return _marketDataProvider;
  }

  public void setMarketDataProvider(final MarketDataProvider marketDataProvider) {
    ArgumentChecker.notNull(marketDataProvider, "marketDataProvider");
    _marketDataProvider = marketDataProvider;
  }

  private MarketDataProvider generateMarketDataProvider() {
    final InMemoryLKVMarketDataProvider provider = new InMemoryLKVMarketDataProvider();
    provider.addValue(getPrimitive1(), 0);
    provider.addValue(getPrimitive2(), 0);
    setMarketDataProvider(provider);
    return provider;
  }

  public MarketDataProviderResolver getMarketDataProviderResolver() {
    return _marketDataProviderResolver;
  }

  public void setMarketDataProviderResolver(final MarketDataProviderResolver marketDataProviderResolver) {
    ArgumentChecker.notNull(marketDataProviderResolver, "marketDataProviderResolver");
    _marketDataProviderResolver = marketDataProviderResolver;
  }

  private MarketDataProviderResolver generateMarketDataProviderResolver() {
    final MarketDataProvider marketDataProvider = getMarketDataProvider() != null ? getMarketDataProvider() : generateMarketDataProvider();
    final MarketDataProviderResolver resolver = new SingleMarketDataProviderResolver(marketDataProvider);
    setMarketDataProviderResolver(resolver);
    return resolver;
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public void setFunctionRepository(final FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }

  private FunctionRepository generateFunctionRepository() {
    final FunctionRepository functionRepository = new InMemoryFunctionRepository();
    setFunctionRepository(functionRepository);
    return functionRepository;
  }

  public DependencyGraphBuilderFactory getDependencyGraphBuilderFactory() {
    return _dependencyGraphBuilderFactory;
  }

  public void setDependencyGraphBuilderFactory(final DependencyGraphBuilderFactory dependencyGraphBuilderFactory) {
    _dependencyGraphBuilderFactory = dependencyGraphBuilderFactory;
  }

  private DependencyGraphBuilderFactory generateDependencyGraphBuilderFactory() {
    final DependencyGraphBuilderFactory factory = new DependencyGraphBuilderFactory();
    setDependencyGraphBuilderFactory(factory);
    return factory;
  }

  public DependencyGraphExecutorFactory getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(final DependencyGraphExecutorFactory dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }

  private DependencyGraphExecutorFactory generateDependencyGraphExecutorFactory() {
    final DependencyGraphExecutorFactory dgef = new SingleNodeExecutorFactory();
    setDependencyGraphExecutorFactory(dgef);
    return dgef;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  private SecuritySource generateSecuritySource() {
    final SecuritySource securitySource = new InMemorySecuritySource();
    setSecuritySource(securitySource);
    return securitySource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public void setFunctionExecutionContext(final FunctionExecutionContext functionExecutionContext) {
    _functionExecutionContext = functionExecutionContext;
  }

  private FunctionExecutionContext generateFunctionExecutionContext() {
    final FunctionExecutionContext fec = new FunctionExecutionContext();
    setFunctionExecutionContext(fec);
    return fec;
  }

  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  public void setFunctionCompilationContext(final FunctionCompilationContext functionCompilationContext) {
    _functionCompilationContext = functionCompilationContext;
  }

  private FunctionCompilationContext generateFunctionCompilationContext() {
    final FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setSecuritySource(getSecuritySource());
    functionCompilationContext.setRawComputationTargetResolver(new DefaultComputationTargetResolver(getSecuritySource(), getPositionSource()));
    setFunctionCompilationContext(functionCompilationContext);
    return functionCompilationContext;
  }

  // Environment accessors
  // -------------------------------------------------------------------------
  public ViewProcessorImpl getViewProcessor() {
    return _viewProcessor;
  }

  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  private FunctionResolver generateFunctionResolver(final CompiledFunctionService compiledFunctions) {
    _functionResolver = new DefaultFunctionResolver(compiledFunctions);
    return _functionResolver;
  }

  public ViewProcessImpl getViewProcess(final ViewProcessorImpl viewProcessor, final UniqueId viewClientId) {
    return viewProcessor.getViewProcessForClient(viewClientId);
  }

  public ViewProcessWorker getCurrentWorker(final ViewProcessImpl viewProcess) {
    return viewProcess.getWorker();
  }

  public static ComputationTargetSpecification getPrimitiveTarget() {
    return s_primitiveTarget;
  }

  public static ValueRequirement getPrimitive1() {
    return s_primitive1;
  }

  public static ValueRequirement getPrimitive2() {
    return s_primitive2;
  }

  public ViewCalculationResultModel getCalculationResult(final ViewResultModel result) {
    return result.getCalculationResult(TEST_CALC_CONFIG_NAME);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.Executors;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.MapViewDefinitionRepository;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessorFactoryBean;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.SingleNodeExecutorFactory;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.LocalCalculationNode;
import com.opengamma.engine.view.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.permission.DefaultViewPermissionProviderFactory;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.test.TestLiveDataClient;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Provides access to a ready-made and customisable view processing environment for testing.
 */
public class ViewProcessorTestEnvironment {

  public static final UserPrincipal TEST_USER = UserPrincipal.getLocalUser();

  public static final String TEST_VIEW_DEFINITION_NAME = "Test View";
  public static final String TEST_CALC_CONFIG_NAME = "Test Calc Config";

  // Settings
  private LiveDataSnapshotProvider _snapshotProvider;
  private LiveDataAvailabilityProvider _availabilityProvider;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private FunctionExecutionContext _functionExecutionContext;
  private FunctionCompilationContext _functionCompilationContext;
  private LiveDataClient _liveDataClient;
  private ViewDefinition _viewDefinition;
  private FunctionRepository _functionRepository;
  private DependencyGraphExecutorFactory<CalculationJobResult> _dependencyGraphExecutorFactory;

  // Environment
  private ViewProcessorImpl _viewProcessor;
  private FunctionResolver _functionResolver;
  private CachingComputationTargetResolver _cachingComputationTargetResolver;
  private final ValueRequirement _primitive1 = new ValueRequirement("Value1", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "PrimitiveValue"));
  private final ValueRequirement _primitive2 = new ValueRequirement("Value2", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "PrimitiveValue"));
  private MapViewDefinitionRepository _viewDefinitionRepository;

  public void init() {
    ViewDefinition viewDefinition = getViewDefinition() != null ? getViewDefinition() : generateViewDefinition();

    ViewProcessorFactoryBean vpFactBean = new ViewProcessorFactoryBean();
    vpFactBean.setId(0L);

    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    PositionSource positionSource = getPositionSource() != null ? getPositionSource() : generatePositionSource();
    SecuritySource securitySource = getSecuritySource() != null ? getSecuritySource() : generateSecuritySource();
    FunctionCompilationContext functionCompilationContext = getFunctionCompilationContext() != null ? getFunctionCompilationContext() : generateFunctionCompilationContext();

    MapViewDefinitionRepository viewDefinitionRepository = new MapViewDefinitionRepository();
    viewDefinitionRepository.addDefinition(viewDefinition);

    InMemoryViewComputationCacheSource cacheSource = new InMemoryViewComputationCacheSource(fudgeContext);
    vpFactBean.setComputationCacheSource(cacheSource);

    DependencyGraphExecutorFactory<CalculationJobResult> dependencyGraphExecutorFactory =
      getDependencyGraphExecutorFactory() != null ? getDependencyGraphExecutorFactory() : generateDependencyGraphExecutorFactory();
    vpFactBean.setDependencyGraphExecutorFactory(dependencyGraphExecutorFactory);

    FunctionRepository functionRepository = getFunctionRepository() != null ? getFunctionRepository() : generateFunctionRepository();
    final CompiledFunctionService compiledFunctions = new CompiledFunctionService(functionRepository, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
    compiledFunctions.initialize();
    vpFactBean.setFunctionCompilationService(compiledFunctions);

    LiveDataClient liveDataClient = getLiveDataClient() != null ? getLiveDataClient() : generateLiveDataClient();
    vpFactBean.setLiveDataClient(liveDataClient);

    if (getSnapshotProvider() == null) {
      generateProviders(securitySource);
    }
    vpFactBean.setLiveDataSnapshotProvider(getSnapshotProvider());
    vpFactBean.setLiveDataAvailabilityProvider(getAvailabilityProvider());

    vpFactBean.setPositionSource(positionSource);
    vpFactBean.setSecuritySource(securitySource);
    vpFactBean.setComputationTargetResolver(generateCachingComputationTargetResolver(positionSource, securitySource));
    vpFactBean.setViewDefinitionRepository(viewDefinitionRepository);
    vpFactBean.setViewPermissionProviderFactory(new DefaultViewPermissionProviderFactory());
    _viewDefinitionRepository = viewDefinitionRepository;

    ViewProcessorQueryReceiver calcNodeQueryReceiver = new ViewProcessorQueryReceiver();
    FudgeRequestDispatcher calcNodeQueryRequestDispatcher = new FudgeRequestDispatcher(calcNodeQueryReceiver);
    InMemoryByteArrayRequestConduit calcNodeQueryRequestConduit = new InMemoryByteArrayRequestConduit(calcNodeQueryRequestDispatcher);
    ByteArrayFudgeRequestSender calcNodeQueryRequestSender = new ByteArrayFudgeRequestSender(calcNodeQueryRequestConduit);
    ViewProcessorQuerySender calcNodeQuerySender = new ViewProcessorQuerySender(calcNodeQueryRequestSender);
    vpFactBean.setViewProcessorQueryReceiver(calcNodeQueryReceiver);

    FunctionExecutionContext functionExecutionContext = getFunctionExecutionContext() != null ? getFunctionExecutionContext() : generateFunctionExecutionContext();
    functionExecutionContext.setSecuritySource(securitySource);

    LocalCalculationNode localCalcNode = new LocalCalculationNode(cacheSource, compiledFunctions, functionExecutionContext, new DefaultComputationTargetResolver(
        securitySource, positionSource), calcNodeQuerySender, Executors.newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
    LocalNodeJobInvoker jobInvoker = new LocalNodeJobInvoker(localCalcNode);
    vpFactBean.setComputationJobDispatcher(new JobDispatcher(jobInvoker));
    vpFactBean.setFunctionResolver(generateFunctionResolver(compiledFunctions));
    
    vpFactBean.setMarketDataSnapshotSource(new MarketDataSnapshotSource() {
      
      @Override
      public StructuredMarketDataSnapshot getSnapshot(UniqueIdentifier uid) {
        return null;
      }

      @Override
      public void addChangeListener(UniqueIdentifier uid, MarketDataSnapshotChangeListener listener) {
      }

      @Override
      public void removeChangeListener(UniqueIdentifier uid, MarketDataSnapshotChangeListener listener) {
      }
    });
    
    _viewProcessor = (ViewProcessorImpl) vpFactBean.createObject();
  }
  
  public CompiledViewDefinitionWithGraphsImpl compileViewDefinition(Instant instant) {
    if (getViewProcessor() == null) {
      throw new IllegalStateException(ViewProcessorTestEnvironment.class.getName() + " has not been initialised");
    }
    
    ViewCompilationServices compilationServices = new ViewCompilationServices(
        getAvailabilityProvider(),
        getFunctionResolver(),
        getFunctionCompilationContext(),
        getCachingComputationTargetResolver(),
        getViewProcessor().getFunctionCompilationService().getExecutorService(),
        getSecuritySource(),
        getPositionSource());
    return ViewDefinitionCompiler.compile(getViewDefinition(), compilationServices, instant);
  }

  // Environment
  // -------------------------------------------------------------------------
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }
  
  public void setViewDefinition(ViewDefinition viewDefinition) {
    _viewDefinition = viewDefinition;
  }

  private ViewDefinition generateViewDefinition() {
    ViewDefinition testDefinition = new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_USER);
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(testDefinition, TEST_CALC_CONFIG_NAME);
    calcConfig.addSpecificRequirement(_primitive1);
    calcConfig.addSpecificRequirement(_primitive2);
    testDefinition.addViewCalculationConfiguration(calcConfig);
    
    setViewDefinition(testDefinition);
    return testDefinition;
  }
  
  public LiveDataSnapshotProvider getSnapshotProvider() {
    return _snapshotProvider;
  }
  
  public LiveDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  public void setProviders(LiveDataSnapshotProvider liveDataSnapshotProvider, LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    ArgumentChecker.notNull(liveDataSnapshotProvider, "liveDataSnapshotProvider");
    ArgumentChecker.notNull(liveDataAvailabilityProvider, "liveDataAvailabilityProvider");
    _snapshotProvider = liveDataSnapshotProvider;
    _availabilityProvider = liveDataAvailabilityProvider;
  }
  
  private void generateProviders(SecuritySource securitySource) {
    InMemoryLKVSnapshotProvider provider = new InMemoryLKVSnapshotProvider(securitySource);
    provider.addValue(getPrimitive1(), 0);
    provider.addValue(getPrimitive2(), 0);
    setProviders(provider, provider);
  }
  
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }
  
  public void setFunctionRepository(FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }

  private FunctionRepository generateFunctionRepository() {
    FunctionRepository functionRepository = new InMemoryFunctionRepository();
    setFunctionRepository(functionRepository);
    return functionRepository;
  }
  
  public DependencyGraphExecutorFactory<CalculationJobResult> getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(DependencyGraphExecutorFactory<CalculationJobResult> dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }
  
  private DependencyGraphExecutorFactory<CalculationJobResult> generateDependencyGraphExecutorFactory() {
    DependencyGraphExecutorFactory<CalculationJobResult> dgef = new SingleNodeExecutorFactory();
    setDependencyGraphExecutorFactory(dgef);
    return dgef;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  private SecuritySource generateSecuritySource() {
    SecuritySource securitySource = new MockSecuritySource();
    setSecuritySource(securitySource);
    return securitySource;
  }
  
  public PositionSource getPositionSource() {
    return _positionSource;
  }
  
  public void setPositionSource(PositionSource positionSource) {
    _positionSource = positionSource;
  }
  
  private PositionSource generatePositionSource() {
    PositionSource positionSource = new MockPositionSource();
    setPositionSource(positionSource);
    return positionSource;
  }
  
  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }
  
  public void setFunctionExecutionContext(FunctionExecutionContext functionExecutionContext) {
    _functionExecutionContext = functionExecutionContext;
  }
  
  private FunctionExecutionContext generateFunctionExecutionContext() {
    FunctionExecutionContext fec = new FunctionExecutionContext();
    setFunctionExecutionContext(fec);
    return fec;
  }
  
  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }
  
  public void setFunctionCompilationContext(FunctionCompilationContext functionCompilationContext) {
    _functionCompilationContext = functionCompilationContext;
  }
  
  private FunctionCompilationContext generateFunctionCompilationContext() {
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setSecuritySource(getSecuritySource());
    setFunctionCompilationContext(functionCompilationContext);
    return functionCompilationContext;
  }
  
  public LiveDataClient getLiveDataClient() {
    return _liveDataClient;
  }
  
  public void setLiveDataClient(LiveDataClient liveDataClient) {
    _liveDataClient = liveDataClient;
  }
  
  private LiveDataClient generateLiveDataClient() {
    LiveDataClient ldc = new TestLiveDataClient();
    setLiveDataClient(ldc);
    return ldc;
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
  
  public CachingComputationTargetResolver getCachingComputationTargetResolver() {
    return _cachingComputationTargetResolver;
  }
  
  private CachingComputationTargetResolver generateCachingComputationTargetResolver(PositionSource positionSource,
      SecuritySource securitySource) {
    _cachingComputationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils.createCacheManager());
    return _cachingComputationTargetResolver;
  }

  public ViewProcessImpl getViewProcess(ViewProcessorImpl viewProcessor, UniqueIdentifier viewClientId) {
    return viewProcessor.getViewProcessForClient(viewClientId);
  }
  
  public ViewComputationJob getCurrentComputationJob(ViewProcessImpl viewProcess) {
    return viewProcess.getComputationJob();
  }

  public Thread getCurrentComputationThread(ViewProcessImpl viewProcess) {
    return viewProcess.getComputationThread();
  }

  public ValueRequirement getPrimitive1() {
    return _primitive1;
  }

  public ValueRequirement getPrimitive2() {
    return _primitive2;
  }

  public ViewCalculationResultModel getCalculationResult(ViewResultModel result) {
    return result.getCalculationResult(TEST_CALC_CONFIG_NAME);
  }
  
  public MapViewDefinitionRepository getViewDefinitionRepository() {
    return _viewDefinitionRepository;
  }
}

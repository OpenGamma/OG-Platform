/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
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

/**
 * Provides access to a ready-made and customisable view processing environment for testing.
 */
public class ViewProcessorTestEnvironment {

  public static final UserPrincipal TEST_USER = UserPrincipal.getLocalUser();

  public static final String TEST_VIEW_DEFINITION_NAME = "Test View";
  public static final String TEST_CALC_CONFIG_NAME = "Test Calc Config";

  // Settings
  private LiveDataSnapshotProvider _userSnapshotProvider;
  private LiveDataAvailabilityProvider _userAvailabilityProvider;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private FunctionExecutionContext _functionExecutionContext;
  private FunctionCompilationContext _functionCompilationContext;
  private LiveDataClient _liveDataClient;
  private ViewDefinition _viewDefinition;
  private FunctionRepository _functionRepository;

  // Environment
  private ViewProcessorImpl _viewProcessor;
  private InMemoryLKVSnapshotProvider _snapshotProvider;
  private DependencyGraphExecutorFactory<CalculationJobResult> _dependencyGraphExecutorFactory;
  private ViewCalculationConfiguration _calcConfig;
  private final ValueRequirement _primitive1 = new ValueRequirement("Value1", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "PrimitiveValue"));
  private final ValueRequirement _primitive2 = new ValueRequirement("Value2", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "PrimitiveValue"));
  private MapViewDefinitionRepository _viewDefinitionRepository;

  public void init() {
    ViewDefinition viewDefinition = getViewDefinition() != null ? getViewDefinition() : generateViewDefinition();

    ViewProcessorFactoryBean vpFactBean = new ViewProcessorFactoryBean();
    vpFactBean.setId(0L);

    FudgeContext fudgeContext = new FudgeContext();
    PositionSource positionSource = getPositionSource() != null ? getPositionSource() : new MockPositionSource();
    SecuritySource securitySource = getSecuritySource() != null ? getSecuritySource() : new MockSecuritySource();
    FunctionCompilationContext functionCompilationContext = getFunctionCompilationContext();
    if (functionCompilationContext == null) {
      functionCompilationContext = new FunctionCompilationContext();
      functionCompilationContext.setSecuritySource(securitySource);
    }

    MapViewDefinitionRepository viewDefinitionRepository = new MapViewDefinitionRepository();
    viewDefinitionRepository.addDefinition(viewDefinition);

    InMemoryViewComputationCacheSource cacheSource = new InMemoryViewComputationCacheSource(fudgeContext);
    vpFactBean.setComputationCacheSource(cacheSource);

    DependencyGraphExecutorFactory<CalculationJobResult> dependencyGraphExecutorFactory = getDependencyGraphExecutorFactory() != null ? getDependencyGraphExecutorFactory()
        : new SingleNodeExecutorFactory();
    vpFactBean.setDependencyGraphExecutorFactory(dependencyGraphExecutorFactory);

    FunctionRepository functionRepository = getFunctionRepository() != null ? getFunctionRepository() : new InMemoryFunctionRepository();
    final CompiledFunctionService compiledFunctions = new CompiledFunctionService(functionRepository, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
    compiledFunctions.initialize();
    vpFactBean.setFunctionCompilationService(compiledFunctions);

    LiveDataClient liveDataClient = getLiveDataClient() != null ? getLiveDataClient() : new TestLiveDataClient();
    vpFactBean.setLiveDataClient(liveDataClient);

    if (getUserSnapshotProvider() != null) {
      vpFactBean.setLiveDataSnapshotProvider(getUserSnapshotProvider());
      vpFactBean.setLiveDataAvailabilityProvider(getUserAvailabilityProvider());
    } else {
      InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
      snapshotProvider.addValue(getPrimitive1(), 0);
      snapshotProvider.addValue(getPrimitive2(), 0);
      vpFactBean.setLiveDataSnapshotProvider(snapshotProvider);
      vpFactBean.setLiveDataAvailabilityProvider(snapshotProvider);
    }

    vpFactBean.setPositionSource(positionSource);
    vpFactBean.setSecuritySource(securitySource);
    vpFactBean.setComputationTargetResolver(new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils.createCacheManager()));
    vpFactBean.setViewDefinitionRepository(viewDefinitionRepository);
    vpFactBean.setViewPermissionProviderFactory(new DefaultViewPermissionProviderFactory());
    _viewDefinitionRepository = viewDefinitionRepository;

    ViewProcessorQueryReceiver calcNodeQueryReceiver = new ViewProcessorQueryReceiver();
    FudgeRequestDispatcher calcNodeQueryRequestDispatcher = new FudgeRequestDispatcher(calcNodeQueryReceiver);
    InMemoryByteArrayRequestConduit calcNodeQueryRequestConduit = new InMemoryByteArrayRequestConduit(calcNodeQueryRequestDispatcher);
    ByteArrayFudgeRequestSender calcNodeQueryRequestSender = new ByteArrayFudgeRequestSender(calcNodeQueryRequestConduit);
    ViewProcessorQuerySender calcNodeQuerySender = new ViewProcessorQuerySender(calcNodeQueryRequestSender);
    vpFactBean.setViewProcessorQueryReceiver(calcNodeQueryReceiver);

    FunctionExecutionContext functionExecutionContext = getFunctionExecutionContext() != null ? getFunctionExecutionContext() : new FunctionExecutionContext();
    functionExecutionContext.setSecuritySource(securitySource);

    LocalCalculationNode localCalcNode = new LocalCalculationNode(cacheSource, compiledFunctions, functionExecutionContext, new DefaultComputationTargetResolver(
        securitySource, positionSource), calcNodeQuerySender, Executors.newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
    LocalNodeJobInvoker jobInvoker = new LocalNodeJobInvoker(localCalcNode);
    vpFactBean.setComputationJobDispatcher(new JobDispatcher(jobInvoker));
    
    _viewProcessor = (ViewProcessorImpl) vpFactBean.createObject();
  }

  private ViewDefinition generateViewDefinition() {
    ViewDefinition testDefinition = new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_USER);
    _calcConfig = new ViewCalculationConfiguration(testDefinition, TEST_CALC_CONFIG_NAME);
    _calcConfig.addSpecificRequirement(_primitive1);
    _calcConfig.addSpecificRequirement(_primitive2);
    testDefinition.addViewCalculationConfiguration(_calcConfig);
    return testDefinition;
  }

  // Pre-init configuration
  // -------------------------------------------------------------------------
  public LiveDataSnapshotProvider getUserSnapshotProvider() {
    return _userSnapshotProvider;
  }
  
  public LiveDataAvailabilityProvider getUserAvailabilityProvider() {
    return _userAvailabilityProvider;
  }

  public void setUserProviders(LiveDataSnapshotProvider liveDataSnapshotProvider, LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    ArgumentChecker.notNull(liveDataSnapshotProvider, "liveDataSnapshotProvider");
    ArgumentChecker.notNull(liveDataAvailabilityProvider, "liveDataAvailabilityProvider");
    _userSnapshotProvider = liveDataSnapshotProvider;
    _userAvailabilityProvider = liveDataAvailabilityProvider;
  }
  
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }
  
  public void setFunctionRepository(FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }

  public DependencyGraphExecutorFactory<CalculationJobResult> getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(DependencyGraphExecutorFactory<CalculationJobResult> dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  public PositionSource getPositionSource() {
    return _positionSource;
  }
  
  public void setPositionSource(PositionSource positionSource) {
    _positionSource = positionSource;
  }
  
  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }
  
  public void setFunctionExecutionContext(FunctionExecutionContext functionExecutionContext) {
    _functionExecutionContext = functionExecutionContext;
  }
  
  public FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }
  
  public void setFunctionCompilationContext(FunctionCompilationContext functionCompilationContext) {
    _functionCompilationContext = functionCompilationContext;
  }
  
  public LiveDataClient getLiveDataClient() {
    return _liveDataClient;
  }
  
  public void setLiveDataClient(LiveDataClient liveDataClient) {
    _liveDataClient = liveDataClient;
  }

  // Environment accessors
  // -------------------------------------------------------------------------
  public ViewProcessorImpl getViewProcessor() {
    return _viewProcessor;
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

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }
  
  public void setViewDefinition(ViewDefinition viewDefinition) {
    _viewDefinition = viewDefinition;
  }

  public InMemoryLKVSnapshotProvider getSnapshotProvider() {
    return _snapshotProvider;
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

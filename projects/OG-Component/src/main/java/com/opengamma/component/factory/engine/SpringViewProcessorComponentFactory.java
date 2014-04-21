/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.GenericApplicationContext;

import com.google.common.base.Supplier;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractSpringComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.calcnode.CalcNodeSocketConfiguration;
import com.opengamma.engine.calcnode.stats.TotallingNodeStatisticsGatherer;
import com.opengamma.engine.exec.MultipleNodeExecutorTuner;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.live.ViewProcessAvailabilityNotificationListener;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.engine.view.impl.ViewProcessorInternal;
import com.opengamma.financial.aggregation.PortfolioAggregationFunctions;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.function.rest.DataFunctionRepositoryResource;
import com.opengamma.financial.view.ViewProcessorManager;
import com.opengamma.financial.view.rest.DataAvailableOutputsProviderResource;
import com.opengamma.financial.view.rest.DataViewProcessorResource;
import com.opengamma.financial.view.rest.RemoteAvailableOutputsProvider;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

/**
 * Component definition for the view processor defined in Spring extended to produce RESTful artifacts.
 */
@BeanDefinition
@SuppressWarnings("deprecation")
public class SpringViewProcessorComponentFactory extends AbstractSpringComponentFactory {

  /**
   * The classifier that the factory should publish under. The Spring config must create this.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The fudge context.
   */
  @PropertyDefinition(validate = "notNull")
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  /**
   * The JMS connector.
   */
  @PropertyDefinition
  private JmsConnector _jmsConnector;
  /**
   * The JMS broker URI.
   */
  @PropertyDefinition
  private String _jmsBrokerUri;
  /**
   * The scheduler.
   */
  @PropertyDefinition(validate = "notNull")
  private ScheduledExecutorService _scheduler;
  /**
   * The volatility (for market data snapshots).
   */
  @PropertyDefinition
  private VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  /**
   * The market data (for debugging).
   */
  @PropertyDefinition
  private MarketDataProviderResolver _marketDataProviderResolver;
  /**
   * Whether to stripe portfolio requirements during a graph build.
   * 
   * @deprecated this is a temporary measure until enabling/disabling the striping logic can be implemented using suitable heuristics
   */
  @Deprecated
  @PropertyDefinition
  private boolean _compileViewsWithRequirementStriping;

  /**
   * The hts source, used in snapshotting if hts data used in place of live data. May be null or not specified.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  /**
   * JMS topic for notifications that the connection Bloomberg has come up.
   */
  @PropertyDefinition
  private String _jmsMarketDataAvailabilityTopic;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    // TODO: Lifecycle beans
    final GenericApplicationContext appContext = createApplicationContext(repo);
    initViewProcessor(repo, appContext);
    initAvailableOutputs(repo, appContext);
    initCalcNodeSocketConfiguration(repo, appContext);
    initAggregators(repo, appContext);
    initUserPrincipal(repo, appContext);
    initFunctions(repo, appContext);
    initForDebugging(repo, appContext);
    registerSpringLifecycleStop(repo, appContext);
    ViewDefinitionCompiler.setStripedPortfolioRequirements(isCompileViewsWithRequirementStriping());
    ViewDefinitionCompiler.registerMetricsStatic(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "ViewDefinitionCompiler");
  }

  /**
   * Registers the view processor.
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initViewProcessor(final ComponentRepository repo, final GenericApplicationContext appContext) {
    final ViewProcessor viewProcessor = appContext.getBean(ViewProcessor.class);
    final ComponentInfo info = new ComponentInfo(ViewProcessor.class, getClassifier());
    if (getJmsBrokerUri() != null) {
      info.addAttribute(ComponentInfoAttributes.JMS_BROKER_URI, getJmsBrokerUri());
    }
    repo.registerComponent(info, viewProcessor);
    if (isPublishRest()) {
      final DataViewProcessorResource vpResource = new DataViewProcessorResource(viewProcessor, repo.getInstance(FunctionCompilationContext.class, "main").getRawComputationTargetResolver(),
          getVolatilityCubeDefinitionSource(), getJmsConnector(), getFudgeContext(), getScheduler(), getHistoricalTimeSeriesSource());
      repo.getRestComponents().publish(info, vpResource);
    }
    if (getJmsConnector() != null && viewProcessor instanceof ViewProcessorInternal) {
      ViewProcessAvailabilityNotificationListener listener = new ViewProcessAvailabilityNotificationListener(getJmsMarketDataAvailabilityTopic(), getJmsConnector(),
          (ViewProcessorInternal) viewProcessor);
      repo.registerLifecycle(listener);

      final ViewProcessorManager viewProcessorManager = appContext.getBean(ViewProcessorManager.class);
      repo.registerLifecycle(viewProcessorManager);
    }
  }

  /**
   * Registers the available outputs.
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initAvailableOutputs(final ComponentRepository repo, final GenericApplicationContext appContext) {
    final AvailableOutputsProvider availableOutputs = appContext.getBean(AvailableOutputsProvider.class);
    final ComponentInfo info = new ComponentInfo(AvailableOutputsProvider.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteAvailableOutputsProvider.class);
    repo.registerComponent(info, availableOutputs);

    if (isPublishRest()) {
      final DataAvailableOutputsProviderResource aoResource = new DataAvailableOutputsProviderResource(availableOutputs);
      repo.getRestComponents().publish(info, aoResource);
    }
  }

  /**
   * Registers the configuration resource.
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initCalcNodeSocketConfiguration(final ComponentRepository repo, final GenericApplicationContext appContext) {
    try {
      final CalcNodeSocketConfiguration calcNodeSocketConfig = appContext.getBean(CalcNodeSocketConfiguration.class);
      final ComponentInfo info = new ComponentInfo(CalcNodeSocketConfiguration.class, getClassifier());
      repo.registerComponent(info, calcNodeSocketConfig);
    } catch (NoSuchBeanDefinitionException e) {
      repo.getLogger().logInfo("No CalcNodeSocketConfiguration found; external calc nodes not supported.");
    }
  }

  /**
   * Registers the aggregators.
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initAggregators(final ComponentRepository repo, final GenericApplicationContext appContext) {
    registerInfrastructureByType(repo, PortfolioAggregationFunctions.class, appContext);
  }

  /**
   * Registers the user (used until proper user management present).
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initUserPrincipal(final ComponentRepository repo, final GenericApplicationContext appContext) {
    registerInfrastructureByType(repo, UserPrincipal.class, appContext);
  }

  private static class FunctionRepositorySupplier implements Supplier<FunctionRepository> {

    private final CompiledFunctionService _cfs;

    public FunctionRepositorySupplier(final CompiledFunctionService cfs) {
      _cfs = cfs;
    }

    @Override
    public FunctionRepository get() {
      return _cfs.getFunctionRepository();
    }
  }

  /**
   * Registers the compiled function service and function .
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initFunctions(final ComponentRepository repo, final GenericApplicationContext appContext) {
    final CompiledFunctionService compiledFunctionService = appContext.getBean(CompiledFunctionService.class);
    final ComponentInfo infoCFS = new ComponentInfo(CompiledFunctionService.class, getClassifier());
    repo.registerComponent(infoCFS, compiledFunctionService);
    // TODO: This is wrong; what is using the function repository we've registered here? It needs to use a supplier, factory, or source
    //final ComponentInfo infoFR = new ComponentInfo(FunctionRepository.class, getClassifier());
    //repo.registerComponent(infoFR, compiledFunctionService.getFunctionRepository());
    final FunctionExclusionGroups functionExclusionGroups = appContext.getBean(FunctionExclusionGroups.class);
    repo.registerComponent(new ComponentInfo(FunctionExclusionGroups.class, getClassifier()), functionExclusionGroups);
    final FunctionResolver functionResolver = appContext.getBean(FunctionResolver.class);
    repo.registerComponent(new ComponentInfo(FunctionResolver.class, getClassifier()), functionResolver);
    if (isPublishRest()) {
      repo.getRestComponents().publishResource(new DataFunctionRepositoryResource(new FunctionRepositorySupplier(compiledFunctionService)));
    }
  }

  /**
   * Registers the debugging RESTful artifacts.
   * 
   * @param repo the repository to register with, not null
   * @param appContext the Spring application context, not null
   */
  protected void initForDebugging(final ComponentRepository repo, final GenericApplicationContext appContext) {
    // TODO: These should not really be exposed to the component repository
    registerInfrastructureByType(repo, TotallingNodeStatisticsGatherer.class, appContext);
    registerInfrastructureByType(repo, TotallingGraphStatisticsGathererProvider.class, appContext);
    registerInfrastructureByType(repo, MultipleNodeExecutorTuner.class, appContext);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SpringViewProcessorComponentFactory}.
   * @return the meta-bean, not null
   */
  public static SpringViewProcessorComponentFactory.Meta meta() {
    return SpringViewProcessorComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SpringViewProcessorComponentFactory.Meta.INSTANCE);
  }

  @Override
  public SpringViewProcessorComponentFactory.Meta metaBean() {
    return SpringViewProcessorComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under. The Spring config must create this.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under. The Spring config must create this.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fudge context.
   * @return the value of the property, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the fudge context.
   * @param fudgeContext  the new value of the property, not null
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    JodaBeanUtils.notNull(fudgeContext, "fudgeContext");
    this._fudgeContext = fudgeContext;
  }

  /**
   * Gets the the {@code fudgeContext} property.
   * @return the property, not null
   */
  public final Property<FudgeContext> fudgeContext() {
    return metaBean().fudgeContext().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * @return the value of the property
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * @param jmsConnector  the new value of the property
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    this._jmsConnector = jmsConnector;
  }

  /**
   * Gets the the {@code jmsConnector} property.
   * @return the property, not null
   */
  public final Property<JmsConnector> jmsConnector() {
    return metaBean().jmsConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS broker URI.
   * @return the value of the property
   */
  public String getJmsBrokerUri() {
    return _jmsBrokerUri;
  }

  /**
   * Sets the JMS broker URI.
   * @param jmsBrokerUri  the new value of the property
   */
  public void setJmsBrokerUri(String jmsBrokerUri) {
    this._jmsBrokerUri = jmsBrokerUri;
  }

  /**
   * Gets the the {@code jmsBrokerUri} property.
   * @return the property, not null
   */
  public final Property<String> jmsBrokerUri() {
    return metaBean().jmsBrokerUri().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the scheduler.
   * @return the value of the property, not null
   */
  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }

  /**
   * Sets the scheduler.
   * @param scheduler  the new value of the property, not null
   */
  public void setScheduler(ScheduledExecutorService scheduler) {
    JodaBeanUtils.notNull(scheduler, "scheduler");
    this._scheduler = scheduler;
  }

  /**
   * Gets the the {@code scheduler} property.
   * @return the property, not null
   */
  public final Property<ScheduledExecutorService> scheduler() {
    return metaBean().scheduler().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the volatility (for market data snapshots).
   * @return the value of the property
   */
  public VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource() {
    return _volatilityCubeDefinitionSource;
  }

  /**
   * Sets the volatility (for market data snapshots).
   * @param volatilityCubeDefinitionSource  the new value of the property
   */
  public void setVolatilityCubeDefinitionSource(VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    this._volatilityCubeDefinitionSource = volatilityCubeDefinitionSource;
  }

  /**
   * Gets the the {@code volatilityCubeDefinitionSource} property.
   * @return the property, not null
   */
  public final Property<VolatilityCubeDefinitionSource> volatilityCubeDefinitionSource() {
    return metaBean().volatilityCubeDefinitionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data (for debugging).
   * @return the value of the property
   */
  public MarketDataProviderResolver getMarketDataProviderResolver() {
    return _marketDataProviderResolver;
  }

  /**
   * Sets the market data (for debugging).
   * @param marketDataProviderResolver  the new value of the property
   */
  public void setMarketDataProviderResolver(MarketDataProviderResolver marketDataProviderResolver) {
    this._marketDataProviderResolver = marketDataProviderResolver;
  }

  /**
   * Gets the the {@code marketDataProviderResolver} property.
   * @return the property, not null
   */
  public final Property<MarketDataProviderResolver> marketDataProviderResolver() {
    return metaBean().marketDataProviderResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to stripe portfolio requirements during a graph build.
   * 
   * @deprecated this is a temporary measure until enabling/disabling the striping logic can be implemented using suitable heuristics
   * @return the value of the property
   */
  @Deprecated
  public boolean isCompileViewsWithRequirementStriping() {
    return _compileViewsWithRequirementStriping;
  }

  /**
   * Sets whether to stripe portfolio requirements during a graph build.
   * 
   * @deprecated this is a temporary measure until enabling/disabling the striping logic can be implemented using suitable heuristics
   * @param compileViewsWithRequirementStriping  the new value of the property
   */
  @Deprecated
  public void setCompileViewsWithRequirementStriping(boolean compileViewsWithRequirementStriping) {
    this._compileViewsWithRequirementStriping = compileViewsWithRequirementStriping;
  }

  /**
   * Gets the the {@code compileViewsWithRequirementStriping} property.
   * 
   * @deprecated this is a temporary measure until enabling/disabling the striping logic can be implemented using suitable heuristics
   * @return the property, not null
   */
  @Deprecated
  public final Property<Boolean> compileViewsWithRequirementStriping() {
    return metaBean().compileViewsWithRequirementStriping().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the hts source, used in snapshotting if hts data used in place of live data. May be null or not specified.
   * @return the value of the property
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  /**
   * Sets the hts source, used in snapshotting if hts data used in place of live data. May be null or not specified.
   * @param historicalTimeSeriesSource  the new value of the property
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    this._historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  /**
   * Gets the the {@code historicalTimeSeriesSource} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
    return metaBean().historicalTimeSeriesSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets jMS topic for notifications that the connection Bloomberg has come up.
   * @return the value of the property
   */
  public String getJmsMarketDataAvailabilityTopic() {
    return _jmsMarketDataAvailabilityTopic;
  }

  /**
   * Sets jMS topic for notifications that the connection Bloomberg has come up.
   * @param jmsMarketDataAvailabilityTopic  the new value of the property
   */
  public void setJmsMarketDataAvailabilityTopic(String jmsMarketDataAvailabilityTopic) {
    this._jmsMarketDataAvailabilityTopic = jmsMarketDataAvailabilityTopic;
  }

  /**
   * Gets the the {@code jmsMarketDataAvailabilityTopic} property.
   * @return the property, not null
   */
  public final Property<String> jmsMarketDataAvailabilityTopic() {
    return metaBean().jmsMarketDataAvailabilityTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SpringViewProcessorComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SpringViewProcessorComponentFactory other = (SpringViewProcessorComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getJmsBrokerUri(), other.getJmsBrokerUri()) &&
          JodaBeanUtils.equal(getScheduler(), other.getScheduler()) &&
          JodaBeanUtils.equal(getVolatilityCubeDefinitionSource(), other.getVolatilityCubeDefinitionSource()) &&
          JodaBeanUtils.equal(getMarketDataProviderResolver(), other.getMarketDataProviderResolver()) &&
          (isCompileViewsWithRequirementStriping() == other.isCompileViewsWithRequirementStriping()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getJmsMarketDataAvailabilityTopic(), other.getJmsMarketDataAvailabilityTopic()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFudgeContext());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsBrokerUri());
    hash += hash * 31 + JodaBeanUtils.hashCode(getScheduler());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVolatilityCubeDefinitionSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataProviderResolver());
    hash += hash * 31 + JodaBeanUtils.hashCode(isCompileViewsWithRequirementStriping());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsMarketDataAvailabilityTopic());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(384);
    buf.append("SpringViewProcessorComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("fudgeContext").append('=').append(JodaBeanUtils.toString(getFudgeContext())).append(',').append(' ');
    buf.append("jmsConnector").append('=').append(JodaBeanUtils.toString(getJmsConnector())).append(',').append(' ');
    buf.append("jmsBrokerUri").append('=').append(JodaBeanUtils.toString(getJmsBrokerUri())).append(',').append(' ');
    buf.append("scheduler").append('=').append(JodaBeanUtils.toString(getScheduler())).append(',').append(' ');
    buf.append("volatilityCubeDefinitionSource").append('=').append(JodaBeanUtils.toString(getVolatilityCubeDefinitionSource())).append(',').append(' ');
    buf.append("marketDataProviderResolver").append('=').append(JodaBeanUtils.toString(getMarketDataProviderResolver())).append(',').append(' ');
    buf.append("compileViewsWithRequirementStriping").append('=').append(JodaBeanUtils.toString(isCompileViewsWithRequirementStriping())).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesSource())).append(',').append(' ');
    buf.append("jmsMarketDataAvailabilityTopic").append('=').append(JodaBeanUtils.toString(getJmsMarketDataAvailabilityTopic())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SpringViewProcessorComponentFactory}.
   */
  public static class Meta extends AbstractSpringComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", SpringViewProcessorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", SpringViewProcessorComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code fudgeContext} property.
     */
    private final MetaProperty<FudgeContext> _fudgeContext = DirectMetaProperty.ofReadWrite(
        this, "fudgeContext", SpringViewProcessorComponentFactory.class, FudgeContext.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", SpringViewProcessorComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code jmsBrokerUri} property.
     */
    private final MetaProperty<String> _jmsBrokerUri = DirectMetaProperty.ofReadWrite(
        this, "jmsBrokerUri", SpringViewProcessorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code scheduler} property.
     */
    private final MetaProperty<ScheduledExecutorService> _scheduler = DirectMetaProperty.ofReadWrite(
        this, "scheduler", SpringViewProcessorComponentFactory.class, ScheduledExecutorService.class);
    /**
     * The meta-property for the {@code volatilityCubeDefinitionSource} property.
     */
    private final MetaProperty<VolatilityCubeDefinitionSource> _volatilityCubeDefinitionSource = DirectMetaProperty.ofReadWrite(
        this, "volatilityCubeDefinitionSource", SpringViewProcessorComponentFactory.class, VolatilityCubeDefinitionSource.class);
    /**
     * The meta-property for the {@code marketDataProviderResolver} property.
     */
    private final MetaProperty<MarketDataProviderResolver> _marketDataProviderResolver = DirectMetaProperty.ofReadWrite(
        this, "marketDataProviderResolver", SpringViewProcessorComponentFactory.class, MarketDataProviderResolver.class);
    /**
     * The meta-property for the {@code compileViewsWithRequirementStriping} property.
     */
    private final MetaProperty<Boolean> _compileViewsWithRequirementStriping = DirectMetaProperty.ofReadWrite(
        this, "compileViewsWithRequirementStriping", SpringViewProcessorComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", SpringViewProcessorComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code jmsMarketDataAvailabilityTopic} property.
     */
    private final MetaProperty<String> _jmsMarketDataAvailabilityTopic = DirectMetaProperty.ofReadWrite(
        this, "jmsMarketDataAvailabilityTopic", SpringViewProcessorComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "fudgeContext",
        "jmsConnector",
        "jmsBrokerUri",
        "scheduler",
        "volatilityCubeDefinitionSource",
        "marketDataProviderResolver",
        "compileViewsWithRequirementStriping",
        "historicalTimeSeriesSource",
        "jmsMarketDataAvailabilityTopic");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case -917704420:  // fudgeContext
          return _fudgeContext;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case 2047189283:  // jmsBrokerUri
          return _jmsBrokerUri;
        case -160710469:  // scheduler
          return _scheduler;
        case 1540542824:  // volatilityCubeDefinitionSource
          return _volatilityCubeDefinitionSource;
        case 56203069:  // marketDataProviderResolver
          return _marketDataProviderResolver;
        case -620124660:  // compileViewsWithRequirementStriping
          return _compileViewsWithRequirementStriping;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case 108776830:  // jmsMarketDataAvailabilityTopic
          return _jmsMarketDataAvailabilityTopic;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SpringViewProcessorComponentFactory> builder() {
      return new DirectBeanBuilder<SpringViewProcessorComponentFactory>(new SpringViewProcessorComponentFactory());
    }

    @Override
    public Class<? extends SpringViewProcessorComponentFactory> beanType() {
      return SpringViewProcessorComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code fudgeContext} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FudgeContext> fudgeContext() {
      return _fudgeContext;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code jmsBrokerUri} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsBrokerUri() {
      return _jmsBrokerUri;
    }

    /**
     * The meta-property for the {@code scheduler} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ScheduledExecutorService> scheduler() {
      return _scheduler;
    }

    /**
     * The meta-property for the {@code volatilityCubeDefinitionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VolatilityCubeDefinitionSource> volatilityCubeDefinitionSource() {
      return _volatilityCubeDefinitionSource;
    }

    /**
     * The meta-property for the {@code marketDataProviderResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataProviderResolver> marketDataProviderResolver() {
      return _marketDataProviderResolver;
    }

    /**
     * The meta-property for the {@code compileViewsWithRequirementStriping} property.
     * @deprecated this is a temporary measure until enabling/disabling the striping logic can be implemented using suitable heuristics
     * @return the meta-property, not null
     */
    @Deprecated
    public final MetaProperty<Boolean> compileViewsWithRequirementStriping() {
      return _compileViewsWithRequirementStriping;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code jmsMarketDataAvailabilityTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsMarketDataAvailabilityTopic() {
      return _jmsMarketDataAvailabilityTopic;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((SpringViewProcessorComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((SpringViewProcessorComponentFactory) bean).isPublishRest();
        case -917704420:  // fudgeContext
          return ((SpringViewProcessorComponentFactory) bean).getFudgeContext();
        case -1495762275:  // jmsConnector
          return ((SpringViewProcessorComponentFactory) bean).getJmsConnector();
        case 2047189283:  // jmsBrokerUri
          return ((SpringViewProcessorComponentFactory) bean).getJmsBrokerUri();
        case -160710469:  // scheduler
          return ((SpringViewProcessorComponentFactory) bean).getScheduler();
        case 1540542824:  // volatilityCubeDefinitionSource
          return ((SpringViewProcessorComponentFactory) bean).getVolatilityCubeDefinitionSource();
        case 56203069:  // marketDataProviderResolver
          return ((SpringViewProcessorComponentFactory) bean).getMarketDataProviderResolver();
        case -620124660:  // compileViewsWithRequirementStriping
          return ((SpringViewProcessorComponentFactory) bean).isCompileViewsWithRequirementStriping();
        case 358729161:  // historicalTimeSeriesSource
          return ((SpringViewProcessorComponentFactory) bean).getHistoricalTimeSeriesSource();
        case 108776830:  // jmsMarketDataAvailabilityTopic
          return ((SpringViewProcessorComponentFactory) bean).getJmsMarketDataAvailabilityTopic();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((SpringViewProcessorComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((SpringViewProcessorComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -917704420:  // fudgeContext
          ((SpringViewProcessorComponentFactory) bean).setFudgeContext((FudgeContext) newValue);
          return;
        case -1495762275:  // jmsConnector
          ((SpringViewProcessorComponentFactory) bean).setJmsConnector((JmsConnector) newValue);
          return;
        case 2047189283:  // jmsBrokerUri
          ((SpringViewProcessorComponentFactory) bean).setJmsBrokerUri((String) newValue);
          return;
        case -160710469:  // scheduler
          ((SpringViewProcessorComponentFactory) bean).setScheduler((ScheduledExecutorService) newValue);
          return;
        case 1540542824:  // volatilityCubeDefinitionSource
          ((SpringViewProcessorComponentFactory) bean).setVolatilityCubeDefinitionSource((VolatilityCubeDefinitionSource) newValue);
          return;
        case 56203069:  // marketDataProviderResolver
          ((SpringViewProcessorComponentFactory) bean).setMarketDataProviderResolver((MarketDataProviderResolver) newValue);
          return;
        case -620124660:  // compileViewsWithRequirementStriping
          ((SpringViewProcessorComponentFactory) bean).setCompileViewsWithRequirementStriping((Boolean) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((SpringViewProcessorComponentFactory) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case 108776830:  // jmsMarketDataAvailabilityTopic
          ((SpringViewProcessorComponentFactory) bean).setJmsMarketDataAvailabilityTopic((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SpringViewProcessorComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((SpringViewProcessorComponentFactory) bean)._fudgeContext, "fudgeContext");
      JodaBeanUtils.notNull(((SpringViewProcessorComponentFactory) bean)._scheduler, "scheduler");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

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
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.context.support.GenericApplicationContext;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractSpringComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.MultipleNodeExecutorTuner;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.CalcNodeSocketConfiguration;
import com.opengamma.engine.view.calcnode.stats.TotallingNodeStatisticsGatherer;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.aggregation.PortfolioAggregationFunctions;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuilderResource;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuilderResourceContextBean;
import com.opengamma.financial.function.rest.DataFunctionRepositoryResource;
import com.opengamma.financial.view.rest.DataAvailableOutputsProviderResource;
import com.opengamma.financial.view.rest.DataViewProcessorResource;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component definition for the view processor defined in Spring extended to produce RESTful artifacts.
 */
@BeanDefinition
public class SpringViewProcessorComponentFactory extends AbstractSpringComponentFactory {

  /**
   * The classifier that the factory should publish under.
   * The Spring config must create this.
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
  @PropertyDefinition(validate = "notNull")
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

  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    // TODO: Lifecycle beans
    GenericApplicationContext appContext = createApplicationContext(repo);
    initViewProcessor(repo, appContext);
    initAvailableOutputs(repo, appContext);
    initCalcNodeSocketConfiguration(repo, appContext);
    initAggregators(repo, appContext);
    initUserPrincipal(repo, appContext);
    initFunctions(repo, appContext);
    initForDebugging(repo, appContext);
  }

  /**
   * Registers the view processor.
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initViewProcessor(ComponentRepository repo, GenericApplicationContext appContext) {
    ViewProcessor viewProcessor = appContext.getBean(ViewProcessor.class);
    ComponentInfo info = new ComponentInfo(ViewProcessor.class, getClassifier());
    if (getJmsBrokerUri() != null) {
      info.addAttribute(ComponentInfoAttributes.JMS_BROKER_URI, getJmsBrokerUri());
    }
    repo.registerComponent(info, viewProcessor);
    
    if (isPublishRest()) {
      final DataViewProcessorResource vpResource = new DataViewProcessorResource(viewProcessor, getVolatilityCubeDefinitionSource(), getJmsConnector(), getFudgeContext(), getScheduler());
      repo.getRestComponents().publish(info, vpResource);
    }
  }

  /**
   * Registers the available outputs.
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initAvailableOutputs(ComponentRepository repo, GenericApplicationContext appContext) {
    AvailableOutputsProvider availableOutputs = appContext.getBean(AvailableOutputsProvider.class);
    ComponentInfo info = new ComponentInfo(AvailableOutputsProvider.class, getClassifier());
    repo.registerComponent(info, availableOutputs);
    
    if (isPublishRest()) {
      DataAvailableOutputsProviderResource aoResource = new DataAvailableOutputsProviderResource(availableOutputs);
      repo.getRestComponents().publish(info, aoResource);
    }
  }

  /**
   * Registers the configuration resource.
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initCalcNodeSocketConfiguration(ComponentRepository repo, GenericApplicationContext appContext) {
    CalcNodeSocketConfiguration calcNodeSocketConfig = appContext.getBean(CalcNodeSocketConfiguration.class);
    ComponentInfo info = new ComponentInfo(CalcNodeSocketConfiguration.class, getClassifier());
    repo.registerComponent(info, calcNodeSocketConfig);
  }

  /**
   * Registers the aggregators.
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initAggregators(ComponentRepository repo, GenericApplicationContext appContext) {
    registerInfrastructureByType(repo, PortfolioAggregationFunctions.class, appContext);
  }

  /**
   * Registers the user (used until proper user management present).
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initUserPrincipal(ComponentRepository repo, GenericApplicationContext appContext) {
    registerInfrastructureByType(repo, UserPrincipal.class, appContext);
  }

  /**
   * Registers the compiled function service and function .
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initFunctions(ComponentRepository repo, GenericApplicationContext appContext) {
    CompiledFunctionService compiledFunctionService = appContext.getBean(CompiledFunctionService.class);
    ComponentInfo infoCFS = new ComponentInfo(CompiledFunctionService.class, getClassifier());
    repo.registerComponent(infoCFS, compiledFunctionService);
    
    ComponentInfo infoFR = new ComponentInfo(FunctionRepository.class, getClassifier());
    repo.registerComponent(infoFR, compiledFunctionService.getFunctionRepository());
    
    FunctionResolver functionResolver = appContext.getBean(FunctionResolver.class);
    ComputationTargetResolver targetResolver = appContext.getBean(ComputationTargetResolver.class);
    
    if (isPublishRest()) {
      repo.getRestComponents().publishResource(new DataFunctionRepositoryResource(compiledFunctionService.getFunctionRepository()));
      
      DependencyGraphBuilderResourceContextBean bean = new DependencyGraphBuilderResourceContextBean();
      bean.setCompiledFunctionService(compiledFunctionService);
      bean.setComputationTargetResolver(targetResolver);
      bean.setFunctionResolver(functionResolver);
      bean.setMarketDataProviderResolver(getMarketDataProviderResolver());
      DependencyGraphBuilderResource resource = new DependencyGraphBuilderResource(bean, getFudgeContext());
      
      // TODO: not really designed as a "component"
      ComponentInfo infoDGB = new ComponentInfo(DependencyGraphBuilderResource.class, getClassifier());
      repo.registerComponent(infoDGB, resource);
      repo.getRestComponents().publish(infoDGB, resource);
    }
  }

  /**
   * Registers the debugging RESTful artifacts.
   * 
   * @param repo  the repository to register with, not null
   * @param appContext  the Spring application context, not null
   */
  protected void initForDebugging(ComponentRepository repo, GenericApplicationContext appContext) {
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

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -614707837:  // publishRest
        return isPublishRest();
      case -917704420:  // fudgeContext
        return getFudgeContext();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
      case 2047189283:  // jmsBrokerUri
        return getJmsBrokerUri();
      case -160710469:  // scheduler
        return getScheduler();
      case 1540542824:  // volatilityCubeDefinitionSource
        return getVolatilityCubeDefinitionSource();
      case 56203069:  // marketDataProviderResolver
        return getMarketDataProviderResolver();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
      case -917704420:  // fudgeContext
        setFudgeContext((FudgeContext) newValue);
        return;
      case -1495762275:  // jmsConnector
        setJmsConnector((JmsConnector) newValue);
        return;
      case 2047189283:  // jmsBrokerUri
        setJmsBrokerUri((String) newValue);
        return;
      case -160710469:  // scheduler
        setScheduler((ScheduledExecutorService) newValue);
        return;
      case 1540542824:  // volatilityCubeDefinitionSource
        setVolatilityCubeDefinitionSource((VolatilityCubeDefinitionSource) newValue);
        return;
      case 56203069:  // marketDataProviderResolver
        setMarketDataProviderResolver((MarketDataProviderResolver) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_fudgeContext, "fudgeContext");
    JodaBeanUtils.notNull(_jmsConnector, "jmsConnector");
    JodaBeanUtils.notNull(_scheduler, "scheduler");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SpringViewProcessorComponentFactory other = (SpringViewProcessorComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getJmsBrokerUri(), other.getJmsBrokerUri()) &&
          JodaBeanUtils.equal(getScheduler(), other.getScheduler()) &&
          JodaBeanUtils.equal(getVolatilityCubeDefinitionSource(), other.getVolatilityCubeDefinitionSource()) &&
          JodaBeanUtils.equal(getMarketDataProviderResolver(), other.getMarketDataProviderResolver()) &&
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
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * The Spring config must create this.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * The Spring config must create this.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * The Spring config must create this.
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
   * @return the value of the property, not null
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * @param jmsConnector  the new value of the property, not null
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    JodaBeanUtils.notNull(jmsConnector, "jmsConnector");
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
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "fudgeContext",
        "jmsConnector",
        "jmsBrokerUri",
        "scheduler",
        "volatilityCubeDefinitionSource",
        "marketDataProviderResolver");

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
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
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

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

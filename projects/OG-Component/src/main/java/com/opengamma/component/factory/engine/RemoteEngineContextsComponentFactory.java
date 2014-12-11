/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.EHCachingConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.convention.impl.EHCachingConventionSource;
import com.opengamma.core.convention.impl.RemoteConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.RemoteExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.EHCachingHistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.CachedHolidaySource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.impl.EHCachingLegalEntitySource;
import com.opengamma.core.legalentity.impl.RemoteLegalEntitySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.EHCachingPositionSource;
import com.opengamma.core.position.impl.RemotePositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.RemoteRegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.analytics.ircurve.EHCachingInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.rest.RemoteVolatilityCubeDefinitionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.EHCachingConventionBundleSource;
import com.opengamma.financial.convention.rest.RemoteConventionBundleSource;
import com.opengamma.financial.security.EHCachingFinancialSecuritySource;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.EHCachingConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.exchange.impl.EHCachingExchangeSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.EHCachingHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesResolver;
import com.opengamma.master.region.impl.EHCachingRegionSource;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Component factory for {@link FunctionCompilationContext} and {@link FunctionExecutionContext} instances that are based on a remote configuration document and local factory which determines the
 * required construction pattern.
 */
@BeanDefinition
@SuppressWarnings("deprecation")
public class RemoteEngineContextsComponentFactory extends AbstractComponentFactory {

  // TODO: Update the Spring based configuration for remote calc nodes to use this

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteEngineContextsComponentFactory.class);
  private static final String CONTEXT_CONFIGURATION_NAME = "remoteConfiguration";
  private static final String CONTEXT_CONFIGURATION_URI_NAME = "remoteConfigurationUri";

  /**
   * The classifier to distinguish these contexts from elsewhere
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;

  /**
   * The configuration URL where the end-points for the components can be found.
   */
  @PropertyDefinition(validate = "notNull")
  private URI _configuration;

  /**
   * The component factory to use as a template for the engine contexts.
   */
  @PropertyDefinition(validate = "notNull")
  private Class<? extends AbstractComponentFactory> _templateEngineContexts = EngineContextsComponentFactory.class;

  /**
   * The component factory to use as a template for the target resolver. Target resolvers aren't accessed remotely as the components that make them up are often used independently. Performance is
   * better if the resolver goes through these, cached, instances rather than make independently cached remote requests.
   */
  @PropertyDefinition(validate = "notNull")
  private Class<? extends AbstractComponentFactory> _templateTargetResolver = TargetResolverComponentFactory.class;

  /**
   * Flag which controls whether to fail if there is a property from the template factory that can't be handled.
   */
  @PropertyDefinition
  private boolean _strict;

  /**
   * The JMS connector to use for components that require it. This might be fetched from the configuration document, or configured separately.
   */
  @PropertyDefinition
  private JmsConnector _jmsConnector;

  /**
   * EHCache, if required, to wrap around the remote sources.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;

  // caching - if _cacheManager is set

  protected ConfigMaster cache(final ConfigMaster configMaster) {
    if (getCacheManager() != null) {
      return new EHCachingConfigMaster("EngineContext.configMaster", configMaster, getCacheManager());
    } else {
      return configMaster;
    }
  }

  protected ConfigSource cache(final ConfigSource configSource) {
    if (getCacheManager() != null) {
      return new EHCachingConfigSource(configSource, getCacheManager());
    } else {
      return configSource;
    }
  }

  protected ConventionBundleSource cache(final ConventionBundleSource conventionBundleSource) {
    if (getCacheManager() != null) {
      return new EHCachingConventionBundleSource(conventionBundleSource, getCacheManager());
    } else {
      return conventionBundleSource;
    }
  }

  protected ConventionSource cache(final ConventionSource conventionSource) {
    if (getCacheManager() != null) {
      return new EHCachingConventionSource(conventionSource, getCacheManager());
    } else {
      return conventionSource;
    }
  }

  protected SecuritySource cache(final FinancialSecuritySource securitySource) {
    if (getCacheManager() != null) {
      return new EHCachingFinancialSecuritySource(securitySource, getCacheManager());
    } else {
      return securitySource;
    }
  }

  protected ExchangeSource cache(final ExchangeSource exchangeSource) {
    if (getCacheManager() != null) {
      return new EHCachingExchangeSource(exchangeSource, getCacheManager());
    } else {
      return exchangeSource;
    }
  }

  protected HistoricalTimeSeriesResolver cache(final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    if (getCacheManager() != null) {
      return new EHCachingHistoricalTimeSeriesResolver(historicalTimeSeriesResolver, getCacheManager());
    } else {
      return historicalTimeSeriesResolver;
    }
  }

  protected HistoricalTimeSeriesSource cache(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    if (getCacheManager() != null) {
      return new EHCachingHistoricalTimeSeriesSource(historicalTimeSeriesSource, getCacheManager());
    } else {
      return historicalTimeSeriesSource;
    }
  }

  protected HolidaySource cache(final HolidaySource holidaySource) {
    return new CachedHolidaySource(holidaySource);
  }

  protected InterpolatedYieldCurveDefinitionSource cache(final InterpolatedYieldCurveDefinitionSource interpolatedYieldCurveDefinitionSource) {
    if (getCacheManager() != null) {
      return new EHCachingInterpolatedYieldCurveDefinitionSource(interpolatedYieldCurveDefinitionSource, getCacheManager());
    } else {
      return interpolatedYieldCurveDefinitionSource;
    }
  }

  protected LegalEntitySource cache(final LegalEntitySource legalEntitySource) {
    if (getCacheManager() != null) {
      return new EHCachingLegalEntitySource(legalEntitySource, getCacheManager());
    } else {
      return legalEntitySource;
    }
  }

  protected PositionSource cache(final PositionSource positionSource) {
    if (getCacheManager() != null) {
      return new EHCachingPositionSource(positionSource, getCacheManager());
    } else {
      return positionSource;
    }
  }

  protected RegionSource cache(final RegionSource regionSource) {
    if (getCacheManager() != null) {
      return new EHCachingRegionSource(regionSource, getCacheManager());
    } else {
      return regionSource;
    }
  }

  // component creation - if URIs are available

  protected ConfigMaster createConfigMaster(final URI uri) {
    if (uri != null) {
      return cache(new RemoteConfigMaster(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected ConfigSource createConfigSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteConfigSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected ConventionBundleSource createConventionBundleSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteConventionBundleSource(uri));
    } else {
      return null;
    }
  }

  protected ConventionSource createConventionSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteConventionSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected ExchangeSource createExchangeSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteExchangeSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected HistoricalTimeSeriesResolver createHistoricalTimeSeriesResolver(final URI uri) {
    if (uri != null) {
      return cache(new RemoteHistoricalTimeSeriesResolver(uri));
    } else {
      return null;
    }
  }

  protected HistoricalTimeSeriesSource createHistoricalTimeSeriesSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteHistoricalTimeSeriesSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected HolidaySource createHolidaySource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteHolidaySource(uri));
    } else {
      return null;
    }
  }

  protected InterpolatedYieldCurveDefinitionSource createInterpolatedYieldCurveDefinitionSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteInterpolatedYieldCurveDefinitionSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected InterpolatedYieldCurveSpecificationBuilder createInterpolatedYieldCurveSpecificationBuilder(final URI uri) {
    if (uri != null) {
      return /*TODO: cache*/(new RemoteInterpolatedYieldCurveSpecificationBuilder(uri));
    } else {
      return null;
    }
  }

  protected LegalEntitySource createLegalEntitySource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteLegalEntitySource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected PositionSource createPositionSource(final URI uri) {
    if (uri != null) {
      return cache(new RemotePositionSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected RegionSource createRegionSource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteRegionSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected SecuritySource createSecuritySource(final URI uri) {
    if (uri != null) {
      return cache(new RemoteFinancialSecuritySource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  protected ViewProcessor createViewProcessor(final URI uri) {
    if ((getJmsConnector() != null) && (uri != null)) {
      return new RemoteViewProcessor(uri, getJmsConnector(), Executors.newSingleThreadScheduledExecutor());
    } else {
      return null;
    }
  }

  protected VolatilityCubeDefinitionSource createVolatilityCubeDefinitionSource(final URI uri) {
    if (uri != null) {
      return /*TODO: cache*/(new RemoteVolatilityCubeDefinitionSource(uri/*, TODO: change manager */));
    } else {
      return null;
    }
  }

  // initialisation

  /**
   * Fetches the remote configuration document and supplies a suitable validation service.
   * 
   * @return the URI validation service and configuration document, not null
   */
  protected Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> fetchConfiguration() {
    final FudgeRestClient client = FudgeRestClient.create();
    s_logger.info("Fetching remote configuration for {} from {}", getClassifier(), getConfiguration());
    final FudgeMsg remoteConfiguration = client.accessFudge(getConfiguration()).get(FudgeMsg.class);
    final UriEndPointDescriptionProvider.Validater validator = UriEndPointDescriptionProvider.validater(Executors.newCachedThreadPool(), getConfiguration());
    return Pairs.of(validator, remoteConfiguration);
  }

  /**
   * Fetches a URI from the remote configuration.
   * 
   * @param remoteConfiguration the remote configuration, as created by {@link #fetchConfiguration}
   * @param label the element from the remote configuration document, not null
   * @return the URI or null if none is available/defined
   */
  protected URI fetchURI(final Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> remoteConfiguration, final String label) {
    final FudgeMsg configuration = remoteConfiguration.getSecond().getMessage(label);
    if (configuration == null) {
      s_logger.warn("{} not defined in remote configuration {}", label, getConfiguration());
      return null;
    }
    return remoteConfiguration.getFirst().getAccessibleURI(configuration);
  }

  /**
   * Gets the classifier used to refer to remote components.
   * 
   * @return the classifier, not null
   */
  protected String getRemoteClassifier() {
    return getClassifier() + "RemoteEngine";
  }

  /**
   * Sets the component in the template and registers it in the local repository. The local repository is used as a cache when local forms, such as the target resolver, are constructed.
   * 
   * @param repo the component repository, not null
   * @param property the template property, not null
   * @param template the template component factory, not null
   * @param component the remote component, null if none is available
   */
  @SuppressWarnings("unchecked")
  protected void remoteComponent(final ComponentRepository repo, final MetaProperty<?> property, final AbstractComponentFactory template, Object component) {
    // Always set the template so that it's null validation can take place
    property.set(template, component);
    if (component != null) {
      s_logger.debug("Registered {}::{}", property.propertyType().getSimpleName(), getRemoteClassifier());
      repo.registerComponent((Class<Object>) property.propertyType(), getRemoteClassifier(), component);
    }
  }

  /**
   * Processes a field from the template component factory, either passing a value from the local configuration or using a value from the remote configuration document to set it accordingly.
   * 
   * @param repo the component repository, not null
   * @param property the template property, not null
   * @param localConfiguration the local configuration, not null
   * @param remoteConfiguration the remote configuration, as created by {@link #fetchConfiguration}
   * @param template the template component factory, not null
   */
  protected void remoteComponentProperty(final ComponentRepository repo, final MetaProperty<?> property, final LinkedHashMap<String, String> localConfiguration,
      final Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> remoteConfiguration, final AbstractComponentFactory template) throws Exception {
    final Object existingComponent = repo.findInstance(property.propertyType(), getRemoteClassifier());
    if (existingComponent != null) {
      s_logger.debug("Got cached {}::{}", property.propertyType().getSimpleName(), getRemoteClassifier());
      property.set(template, existingComponent);
      return;
    }
    switch (property.name()) {
      case "cacheManager":
        property.set(template, getCacheManager());
        break;
      case "classifier":
        property.set(template, (localConfiguration != null) ? getClassifier() : getRemoteClassifier());
        break;
      case "compilationBlacklist":
        // TODO:
        break;
      case "configMaster":
        remoteComponent(repo, property, template, createConfigMaster(fetchURI(remoteConfiguration, "configMaster")));
        break;
      case "configSource":
        remoteComponent(repo, property, template, createConfigSource(fetchURI(remoteConfiguration, "configSource")));
        break;
      case "conventionBundleSource":
        remoteComponent(repo, property, template, createConventionBundleSource(fetchURI(remoteConfiguration, "conventionBundleSource")));
        break;
      case "conventionSource":
        remoteComponent(repo, property, template, createConventionSource(fetchURI(remoteConfiguration, "conventionSource")));
        break;
      case "exchangeSource":
        remoteComponent(repo, property, template, createExchangeSource(fetchURI(remoteConfiguration, "exchangeSource")));
        break;
      case "executionBlacklist":
        // TODO:
        break;
      case "historicalTimeSeriesResolver":
        remoteComponent(repo, property, template, createHistoricalTimeSeriesResolver(fetchURI(remoteConfiguration, "historicalTimeSeriesResolver")));
        break;
      case "historicalTimeSeriesSource":
        remoteComponent(repo, property, template, createHistoricalTimeSeriesSource(fetchURI(remoteConfiguration, "historicalTimeSeriesSource")));
        break;
      case "holidaySource":
        remoteComponent(repo, property, template, createHolidaySource(fetchURI(remoteConfiguration, "holidaySource")));
        break;
      case "interpolatedYieldCurveDefinitionSource":
        remoteComponent(repo, property, template, createInterpolatedYieldCurveDefinitionSource(fetchURI(remoteConfiguration, "interpolatedYieldCurveDefinitionSource")));
        break;
      case "interpolatedYieldCurveSpecificationBuilder":
        remoteComponent(repo, property, template, createInterpolatedYieldCurveSpecificationBuilder(fetchURI(remoteConfiguration, "interpolatedYieldCurveSpecificationBuilder")));
        break;
      case "legalEntitySource":
        remoteComponent(repo, property, template, createLegalEntitySource(fetchURI(remoteConfiguration, "legalEntitySource")));
        break;
      case "positionSource":
        remoteComponent(repo, property, template, createPositionSource(fetchURI(remoteConfiguration, "positionSource")));
        break;
      case "regionSource":
        remoteComponent(repo, property, template, createRegionSource(fetchURI(remoteConfiguration, "regionSource")));
        break;
      case "securitySource":
        remoteComponent(repo, property, template, createSecuritySource(fetchURI(remoteConfiguration, "securitySource")));
        break;
      case "targetResolver":
        initTemplate(repo, null, remoteConfiguration, new TargetResolverComponentFactory());
        property.set(template, repo.findInstance(ComputationTargetResolver.class, getRemoteClassifier()));
        break;
      case "tempTargetRepository":
        // TODO:
        break;
      case "viewProcessor":
        remoteComponent(repo, property, template, createViewProcessor(fetchURI(remoteConfiguration, "viewProcessor")));
        break;
      case "volatilityCubeDefinitionSource":
        remoteComponent(repo, property, template, createVolatilityCubeDefinitionSource(fetchURI(remoteConfiguration, "volatilityCubeDefinitionSource")));
        break;
      default:
        s_logger.warn("Can't handle {} on {}", property.name(), template);
        if ((localConfiguration != null) && !"true".equals(localConfiguration.remove("ignore" + StringUtils.capitalize(property.name())))) {
          if (isStrict()) {
            throw new UnsupportedOperationException("Strict mode set and can't handle template field " + property + " on " + template);
          }
        }
        break;
    }
  }

  /**
   * Processes the fields from a template component factory, either passing a value from local configuration, if any, or using a value from the remote configuration document to set it accordingly.
   * 
   * @param repo the component repository, not null
   * @param localConfiguration the local configuration, null if none
   * @param remoteConfiguration the remote configuration, as created by {@link #fetchConfiguration}
   * @param template the template component factory, not null
   */
  protected void initTemplate(final ComponentRepository repo, final LinkedHashMap<String, String> localConfiguration,
      final Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> remoteConfiguration, final AbstractComponentFactory template) throws Exception {
    for (final MetaProperty<?> templateProperty : template.metaBean().metaPropertyIterable()) {
      final String localValue = (localConfiguration != null) ? localConfiguration.remove(templateProperty.name()) : null;
      if (localValue != null) {
        // Literal or local reference
        if (Boolean.class.equals(templateProperty.propertyType()) || Boolean.TYPE.equals(templateProperty.propertyType())) {
          templateProperty.set(template, "true".equalsIgnoreCase(localValue));
        } else {
          // TODO: literal or local reference
          throw new UnsupportedOperationException("Unsupported literal or local reference " + localValue + " for " + templateProperty.name());
        }
      } else {
        // Not defined locally; try and fetch from remote
        remoteComponentProperty(repo, templateProperty, localConfiguration, remoteConfiguration, template);
      }
    }
    template.init(repo, localConfiguration);
  }

  /**
   * Fetches the original configuration message, if the context was created by this factory.
   * 
   * @param context the context to check, not null
   * @return the original configuration message or null if none
   */
  public static FudgeMsg getConfiguration(final FunctionCompilationContext context) {
    return (FudgeMsg) context.get(CONTEXT_CONFIGURATION_NAME);
  }

  /**
   * Fetches the original configuration URI, if the context was created by this factory.
   * 
   * @param context the context to check, not null
   * @return the original configuration URI or null if none
   */
  public static URI getConfigurationUri(final FunctionCompilationContext context) {
    return (URI) context.get(CONTEXT_CONFIGURATION_URI_NAME);
  }

  /**
   * Fetches the original configuration message, if the context was created by this factory.
   * 
   * @param context the context to check, not null
   * @return the original configuration message or null if none
   */
  public static FudgeMsg getConfiguration(final FunctionExecutionContext context) {
    return (FudgeMsg) context.get(CONTEXT_CONFIGURATION_NAME);
  }

  /**
   * Fetches the original configuration URI, if the context was created by this factory.
   * 
   * @param context the context to check, not null
   * @return the original configuration URI or null if none
   */
  public static URI getConfigurationUri(final FunctionExecutionContext context) {
    return (URI) context.get(CONTEXT_CONFIGURATION_URI_NAME);
  }

  // AbstractComponentFactory

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> localConfiguration) throws Exception {
    ArgumentChecker.notNullInjected(getConfiguration(), "configuration");
    final AbstractComponentFactory template = getTemplateEngineContexts().newInstance();
    final Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> remoteConfiguration = fetchConfiguration();
    initTemplate(repo, localConfiguration, remoteConfiguration, template);
    repo.getInstance(FunctionCompilationContext.class, getClassifier()).put(CONTEXT_CONFIGURATION_NAME, remoteConfiguration.getSecond());
    repo.getInstance(FunctionCompilationContext.class, getClassifier()).put(CONTEXT_CONFIGURATION_URI_NAME, getConfiguration());
    repo.getInstance(FunctionExecutionContext.class, getClassifier()).put(CONTEXT_CONFIGURATION_NAME, remoteConfiguration.getSecond());
    repo.getInstance(FunctionExecutionContext.class, getClassifier()).put(CONTEXT_CONFIGURATION_URI_NAME, getConfiguration());
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RemoteEngineContextsComponentFactory}.
   * @return the meta-bean, not null
   */
  public static RemoteEngineContextsComponentFactory.Meta meta() {
    return RemoteEngineContextsComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RemoteEngineContextsComponentFactory.Meta.INSTANCE);
  }

  @Override
  public RemoteEngineContextsComponentFactory.Meta metaBean() {
    return RemoteEngineContextsComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier to distinguish these contexts from elsewhere
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier to distinguish these contexts from elsewhere
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
   * Gets the configuration URL where the end-points for the components can be found.
   * @return the value of the property, not null
   */
  public URI getConfiguration() {
    return _configuration;
  }

  /**
   * Sets the configuration URL where the end-points for the components can be found.
   * @param configuration  the new value of the property, not null
   */
  public void setConfiguration(URI configuration) {
    JodaBeanUtils.notNull(configuration, "configuration");
    this._configuration = configuration;
  }

  /**
   * Gets the the {@code configuration} property.
   * @return the property, not null
   */
  public final Property<URI> configuration() {
    return metaBean().configuration().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the component factory to use as a template for the engine contexts.
   * @return the value of the property, not null
   */
  public Class<? extends AbstractComponentFactory> getTemplateEngineContexts() {
    return _templateEngineContexts;
  }

  /**
   * Sets the component factory to use as a template for the engine contexts.
   * @param templateEngineContexts  the new value of the property, not null
   */
  public void setTemplateEngineContexts(Class<? extends AbstractComponentFactory> templateEngineContexts) {
    JodaBeanUtils.notNull(templateEngineContexts, "templateEngineContexts");
    this._templateEngineContexts = templateEngineContexts;
  }

  /**
   * Gets the the {@code templateEngineContexts} property.
   * @return the property, not null
   */
  public final Property<Class<? extends AbstractComponentFactory>> templateEngineContexts() {
    return metaBean().templateEngineContexts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the component factory to use as a template for the target resolver. Target resolvers aren't accessed remotely as the components that make them up are often used independently. Performance is
   * better if the resolver goes through these, cached, instances rather than make independently cached remote requests.
   * @return the value of the property, not null
   */
  public Class<? extends AbstractComponentFactory> getTemplateTargetResolver() {
    return _templateTargetResolver;
  }

  /**
   * Sets the component factory to use as a template for the target resolver. Target resolvers aren't accessed remotely as the components that make them up are often used independently. Performance is
   * better if the resolver goes through these, cached, instances rather than make independently cached remote requests.
   * @param templateTargetResolver  the new value of the property, not null
   */
  public void setTemplateTargetResolver(Class<? extends AbstractComponentFactory> templateTargetResolver) {
    JodaBeanUtils.notNull(templateTargetResolver, "templateTargetResolver");
    this._templateTargetResolver = templateTargetResolver;
  }

  /**
   * Gets the the {@code templateTargetResolver} property.
   * better if the resolver goes through these, cached, instances rather than make independently cached remote requests.
   * @return the property, not null
   */
  public final Property<Class<? extends AbstractComponentFactory>> templateTargetResolver() {
    return metaBean().templateTargetResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets flag which controls whether to fail if there is a property from the template factory that can't be handled.
   * @return the value of the property
   */
  public boolean isStrict() {
    return _strict;
  }

  /**
   * Sets flag which controls whether to fail if there is a property from the template factory that can't be handled.
   * @param strict  the new value of the property
   */
  public void setStrict(boolean strict) {
    this._strict = strict;
  }

  /**
   * Gets the the {@code strict} property.
   * @return the property, not null
   */
  public final Property<Boolean> strict() {
    return metaBean().strict().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS connector to use for components that require it. This might be fetched from the configuration document, or configured separately.
   * @return the value of the property
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector to use for components that require it. This might be fetched from the configuration document, or configured separately.
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
   * Gets eHCache, if required, to wrap around the remote sources.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets eHCache, if required, to wrap around the remote sources.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RemoteEngineContextsComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RemoteEngineContextsComponentFactory other = (RemoteEngineContextsComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getConfiguration(), other.getConfiguration()) &&
          JodaBeanUtils.equal(getTemplateEngineContexts(), other.getTemplateEngineContexts()) &&
          JodaBeanUtils.equal(getTemplateTargetResolver(), other.getTemplateTargetResolver()) &&
          (isStrict() == other.isStrict()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfiguration());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTemplateEngineContexts());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTemplateTargetResolver());
    hash = hash * 31 + JodaBeanUtils.hashCode(isStrict());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("RemoteEngineContextsComponentFactory{");
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
    buf.append("configuration").append('=').append(JodaBeanUtils.toString(getConfiguration())).append(',').append(' ');
    buf.append("templateEngineContexts").append('=').append(JodaBeanUtils.toString(getTemplateEngineContexts())).append(',').append(' ');
    buf.append("templateTargetResolver").append('=').append(JodaBeanUtils.toString(getTemplateTargetResolver())).append(',').append(' ');
    buf.append("strict").append('=').append(JodaBeanUtils.toString(isStrict())).append(',').append(' ');
    buf.append("jmsConnector").append('=').append(JodaBeanUtils.toString(getJmsConnector())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RemoteEngineContextsComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", RemoteEngineContextsComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code configuration} property.
     */
    private final MetaProperty<URI> _configuration = DirectMetaProperty.ofReadWrite(
        this, "configuration", RemoteEngineContextsComponentFactory.class, URI.class);
    /**
     * The meta-property for the {@code templateEngineContexts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Class<? extends AbstractComponentFactory>> _templateEngineContexts = DirectMetaProperty.ofReadWrite(
        this, "templateEngineContexts", RemoteEngineContextsComponentFactory.class, (Class) Class.class);
    /**
     * The meta-property for the {@code templateTargetResolver} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Class<? extends AbstractComponentFactory>> _templateTargetResolver = DirectMetaProperty.ofReadWrite(
        this, "templateTargetResolver", RemoteEngineContextsComponentFactory.class, (Class) Class.class);
    /**
     * The meta-property for the {@code strict} property.
     */
    private final MetaProperty<Boolean> _strict = DirectMetaProperty.ofReadWrite(
        this, "strict", RemoteEngineContextsComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", RemoteEngineContextsComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", RemoteEngineContextsComponentFactory.class, CacheManager.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "configuration",
        "templateEngineContexts",
        "templateTargetResolver",
        "strict",
        "jmsConnector",
        "cacheManager");

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
        case 1932752118:  // configuration
          return _configuration;
        case -1897285600:  // templateEngineContexts
          return _templateEngineContexts;
        case -21551:  // templateTargetResolver
          return _templateTargetResolver;
        case -891986231:  // strict
          return _strict;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case -1452875317:  // cacheManager
          return _cacheManager;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RemoteEngineContextsComponentFactory> builder() {
      return new DirectBeanBuilder<RemoteEngineContextsComponentFactory>(new RemoteEngineContextsComponentFactory());
    }

    @Override
    public Class<? extends RemoteEngineContextsComponentFactory> beanType() {
      return RemoteEngineContextsComponentFactory.class;
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
     * The meta-property for the {@code configuration} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<URI> configuration() {
      return _configuration;
    }

    /**
     * The meta-property for the {@code templateEngineContexts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Class<? extends AbstractComponentFactory>> templateEngineContexts() {
      return _templateEngineContexts;
    }

    /**
     * The meta-property for the {@code templateTargetResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Class<? extends AbstractComponentFactory>> templateTargetResolver() {
      return _templateTargetResolver;
    }

    /**
     * The meta-property for the {@code strict} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> strict() {
      return _strict;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((RemoteEngineContextsComponentFactory) bean).getClassifier();
        case 1932752118:  // configuration
          return ((RemoteEngineContextsComponentFactory) bean).getConfiguration();
        case -1897285600:  // templateEngineContexts
          return ((RemoteEngineContextsComponentFactory) bean).getTemplateEngineContexts();
        case -21551:  // templateTargetResolver
          return ((RemoteEngineContextsComponentFactory) bean).getTemplateTargetResolver();
        case -891986231:  // strict
          return ((RemoteEngineContextsComponentFactory) bean).isStrict();
        case -1495762275:  // jmsConnector
          return ((RemoteEngineContextsComponentFactory) bean).getJmsConnector();
        case -1452875317:  // cacheManager
          return ((RemoteEngineContextsComponentFactory) bean).getCacheManager();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((RemoteEngineContextsComponentFactory) bean).setClassifier((String) newValue);
          return;
        case 1932752118:  // configuration
          ((RemoteEngineContextsComponentFactory) bean).setConfiguration((URI) newValue);
          return;
        case -1897285600:  // templateEngineContexts
          ((RemoteEngineContextsComponentFactory) bean).setTemplateEngineContexts((Class<? extends AbstractComponentFactory>) newValue);
          return;
        case -21551:  // templateTargetResolver
          ((RemoteEngineContextsComponentFactory) bean).setTemplateTargetResolver((Class<? extends AbstractComponentFactory>) newValue);
          return;
        case -891986231:  // strict
          ((RemoteEngineContextsComponentFactory) bean).setStrict((Boolean) newValue);
          return;
        case -1495762275:  // jmsConnector
          ((RemoteEngineContextsComponentFactory) bean).setJmsConnector((JmsConnector) newValue);
          return;
        case -1452875317:  // cacheManager
          ((RemoteEngineContextsComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((RemoteEngineContextsComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((RemoteEngineContextsComponentFactory) bean)._configuration, "configuration");
      JodaBeanUtils.notNull(((RemoteEngineContextsComponentFactory) bean)._templateEngineContexts, "templateEngineContexts");
      JodaBeanUtils.notNull(((RemoteEngineContextsComponentFactory) bean)._templateTargetResolver, "templateTargetResolver");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

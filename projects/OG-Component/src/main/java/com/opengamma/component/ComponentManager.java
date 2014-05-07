/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.threeten.bp.ZoneId;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.ResourceUtils;

/**
 * Manages the process of loading and starting OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This class loads and starts the components based on configuration.
 * The end result is a populated {@link ComponentRepository}.
 * <p>
 * Two types of config file format are recognized - properties and INI.
 * The INI file is the primary file for loading the components, see {@link ComponentConfigIniLoader}.
 * The behavior of an INI file can be controlled using properties.
 * <p>
 * The properties can either be specified manually before {@link #start(Resource))}
 * is called or loaded by specifying a properties file instead of an INI file.
 * The properties file must contain the key "MANAGER.NEXT.FILE" which is used to load the next file.
 * The next file is normally the INI file, but could be another properties file.
 * As such, the properties files can be chained.
 * <p>
 * Properties are never overwritten, thus manual properties have priority over file-based, and
 * earlier file-based have priority over later file-based.
 * <p>
 * It is not intended that the manager is retained for the lifetime of
 * the application, the repository is intended for that purpose.
 */
public class ComponentManager {

  /**
   * The server name property.
   */
  private static final String OPENGAMMA_SERVER_NAME = "og.server.name";
  /**
   * The key identifying the next config file in a properties file.
   */
  static final String MANAGER_NEXT_FILE = "MANAGER.NEXT.FILE";
  /**
   * The key identifying the entire combined set of active properties.
   */
  static final String MANAGER_PROPERTIES = "MANAGER.PROPERTIES";
  /**
   * The key identifying the the inclusion of another file.
   */
  static final String MANAGER_INCLUDE = "MANAGER.INCLUDE";

  /**
   * The component repository.
   */
  private final ComponentRepository _repo;
  /**
   * The component logger.
   */
  private final ComponentLogger _logger;
  /**
   * The component properties, updated as properties are discovered.
   */
  private final ConfigProperties _properties = new ConfigProperties();
  /**
   * The component INI, updated as configuration is discovered.
   */
  private ComponentConfig _configIni = new ComponentConfig();

  /**
   * Creates an instance that does not log.
   * 
   * @param serverName  the server name, not null
   */
  public ComponentManager(String serverName) {
    this(serverName, ComponentLogger.Sink.INSTANCE);
  }

  /**
   * Creates an instance.
   * 
   * @param serverName  the server name, not null
   * @param logger  the logger, not null
   */
  public ComponentManager(String serverName, ComponentLogger logger) {
    this(serverName, new ComponentRepository(logger));
  }

  /**
   * Creates an instance.
   * 
   * @param serverName  the server name, not null
   * @param repo  the repository to use, not null
   */
  protected ComponentManager(String serverName, ComponentRepository repo) {
    ArgumentChecker.notNull(serverName, "serverName");
    ArgumentChecker.notNull(repo, "repo");
    _repo = repo;
    _logger = repo.getLogger();
    setServerName(serverName);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the repository of components.
   * 
   * @return the repository, not null
   */
  public ComponentRepository getRepository() {
    return _repo;
  }

  /**
   * Gets the properties used while loading the manager.
   * <p>
   * This may be populated before calling {@link #start()} if desired.
   * This is an alternative to using a separate properties file.
   * 
   * @return the key-value properties, which may be directly edited, not null
   */
  public ConfigProperties getProperties() {
    return _properties;
  }

  /**
   * Gets the component INI.
   * 
   * @return the component INI, not null
   */
  public ComponentConfig getConfigIni() {
    return _configIni;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the server name property.
   * <p>
   * This can be used as a general purpose name for the server.
   * 
   * @return the server name, null if name not set
   */
  public String getServerName() {
    return getProperties().getValue(OPENGAMMA_SERVER_NAME);
  }

  /**
   * Sets the server name property.
   * <p>
   * This can be used as a general purpose name for the server.
   * 
   * @param serverName  the server name, not null
   */
  public void setServerName(String serverName) {
    getProperties().put(OPENGAMMA_SERVER_NAME, serverName);
    System.setProperty(OPENGAMMA_SERVER_NAME, serverName);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads, initializes and starts the components based on the specified resource.
   * <p>
   * See {@link #createResource(String)} for the valid resource location formats.
   * <p>
   * Calls {@link #start(Resource)}.
   * 
   * @param resourceLocation  the configuration resource location, not null
   * @return the created repository, not null
   */
  public ComponentRepository start(String resourceLocation) {
    Resource resource = ResourceUtils.createResource(resourceLocation);
    return start(resource);
  }

  /**
   * Loads, initializes and starts the components based on the specified resource.
   * <p>
   * Calls {@link #load(Resource)}, {@link #init()} and {@link #start()}.
   * 
   * @param resource  the configuration resource to load, not null
   * @return the created repository, not null
   */
  public ComponentRepository start(Resource resource) {
    load(resource);
    init();
    start();
    return getRepository();
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the component configuration based on the specified resource.
   * <p>
   * See {@link #createResource(String)} for the valid resource location formats.
   * <p>
   * Calls {@link #load(Resource)}.
   * 
   * @param resourceLocation  the configuration resource location, not null
   * @return this manager, for chaining, not null
   */
  public ComponentManager load(String resourceLocation) {
    Resource resource = ResourceUtils.createResource(resourceLocation);
    return load(resource);
  }

  /**
   * Loads the component configuration based on the specified resource.
   * 
   * @param resource  the configuration resource to load, not null
   * @return this manager, for chaining, not null
   * @throws ComponentConfigException if the resource cannot be loaded
   */
  public ComponentManager load(Resource resource) {
    _logger.logInfo("  Using item: " + ResourceUtils.getLocation(resource));
    if (resource.getFilename().endsWith(".properties")) {
      String nextConfig = loadProperties(resource);
      if (nextConfig == null) {
        throw new ComponentConfigException("The properties file must contain the key '" + MANAGER_NEXT_FILE + "' to specify the next file to load: " + resource);
      }
      return load(nextConfig);
    }
    if (resource.getFilename().endsWith(".ini")) {
      loadIni(resource);
      return this;
    }
    throw new ComponentConfigException("Unknown file format: " + resource);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads a properties file into the replacements map.
   * <p>
   * The properties file must be in the standard format defined by {@link Properties}.
   * The file must contain a key "component.ini"
   * 
   * @param resource  the properties resource location, not null
   * @return the next configuration file to load, null if not specified
   */
  protected String loadProperties(Resource resource) {
    ComponentConfigPropertiesLoader loader = new ComponentConfigPropertiesLoader(_logger, getProperties());
    return loader.load(resource, 0);
  }

  /**
   * Loads the INI file and initializes the components based on the contents.
   * 
   * @param resource  the INI resource location, not null
   */
  protected void loadIni(Resource resource) {
    ComponentConfigIniLoader loader = new ComponentConfigIniLoader(_logger, getProperties());
    loader.load(resource, 0, _configIni);
    logProperties();
  }

  /**
   * Logs the properties to be used.
   */
  protected void logProperties() {
    _logger.logDebug("--- Using merged properties ---");
    for (String key : getProperties().keySet()) {
      _logger.logDebug(" " + key + " = " + getProperties().loggableValue(key));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the repository from the configuration that has been loaded.
   * <p>
   * Call {@code load(...)} before this method.
   * 
   * @return this manager, for chaining, not null
   */
  public ComponentManager init() {
    getRepository().pushThreadLocal();
    initGlobal();
    initComponents();
    return this;
  }

  /**
   * Initializes the global definitions from the config.
   */
  protected void initGlobal() {
    if (_configIni.getGroups().contains("global")) {
      ConfigProperties global = _configIni.getGroup("global");
      PlatformConfigUtils.configureSystemProperties();
      String zoneId = global.getValue("time.zone");
      if (zoneId != null) {
        OpenGammaClock.setZone(ZoneId.of(zoneId));
      }
    }
  }

  /**
   * Initializes the component definitions from the config.
   */
  protected void initComponents() {
    for (String groupName : _configIni.getGroups()) {
      ConfigProperties groupData = _configIni.getGroup(groupName);
      if (groupData.containsKey("factory")) {
        initComponent(groupName, groupData);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Initialize the component.
   * 
   * @param groupName  the group name, not null
   * @param groupConfig  the config data, not null
   * @throws ComponentConfigException if the resource cannot be initialized
   */
  protected void initComponent(String groupName, ConfigProperties groupConfig) {
    _logger.logInfo("--- Initializing " + groupName + " ---");
    long startInstant = System.nanoTime();
    
    LinkedHashMap<String, String> remainingConfig = new LinkedHashMap<String, String>(groupConfig.toMap());
    LinkedHashMap<String, String> loggableConfig = new LinkedHashMap<String, String>(groupConfig.loggableMap());
    String typeStr = remainingConfig.remove("factory");
    loggableConfig.remove("factory");
    _logger.logDebug(" Initializing factory '" + typeStr);
    _logger.logDebug(" Using properties " + loggableConfig);
    
    // load factory
    ComponentFactory factory = loadFactory(typeStr);
    
    // set properties
    try {
      setFactoryProperties(factory, remainingConfig);
    } catch (Exception ex) {
      throw new ComponentConfigException("Failed to set component factory properties: '" + groupName + "' with " + groupConfig, ex);
    }
    
    // init
    try {
      initFactory(factory, remainingConfig);
    } catch (Exception ex) {
      throw new ComponentConfigException("Failed to init component factory: '" + groupName + "' with " + groupConfig, ex);
    }
    
    long endInstant = System.nanoTime();
    _logger.logInfo("--- Initialized " + groupName + " in " + ((endInstant - startInstant) / 1000000L) + "ms ---");
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the factory.
   * A factory should perform minimal work in the constructor.
   * 
   * @param typeStr  the factory type class name, not null
   * @return the factory, not null
   * @throws ComponentConfigException if the factory cannot be initialized
   */
  protected ComponentFactory loadFactory(String typeStr) {
    ComponentFactory factory;
    try {
      Class<? extends ComponentFactory> cls = getClass().getClassLoader().loadClass(typeStr).asSubclass(ComponentFactory.class);
      factory = cls.newInstance();
    } catch (ExceptionInInitializerError ex) {
      throw new ComponentConfigException("Error starting component factory: " + typeStr, ex);
    } catch (ClassNotFoundException ex) {
      throw new ComponentConfigException("Unknown component factory: " + typeStr, ex);
    } catch (InstantiationException ex) {
      throw new ComponentConfigException("Unable to create component factory: " + typeStr, ex);
    } catch (IllegalAccessException ex) {
      throw new ComponentConfigException("Unable to access component factory: " + typeStr, ex);
    }
    return factory;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the properties on the factory.
   * 
   * @param factory  the factory, not null
   * @param remainingConfig  the config data, not null
   * @throws Exception allowing throwing of a checked exception
   */
  protected void setFactoryProperties(ComponentFactory factory, LinkedHashMap<String, String> remainingConfig) throws Exception {
    if (factory instanceof Bean) {
      Bean bean = (Bean) factory;
      for (MetaProperty<?> mp : bean.metaBean().metaPropertyIterable()) {
        String value = remainingConfig.remove(mp.name());
        setProperty(bean, mp, value);
      }
    }
  }

  /**
   * Sets an individual property.
   * <p>
   * This method handles the main special case formats of the value.
   * 
   * @param bean  the bean, not null
   * @param mp  the property, not null
   * @param value  the configured value, not null
   * @throws Exception allowing throwing of a checked exception
   */
  protected void setProperty(Bean bean, MetaProperty<?> mp, String value) throws Exception {
    if (ComponentRepository.class.equals(mp.propertyType())) {
      // set the repo
      mp.set(bean, getRepository());
      
    } else if (value == null) {
      // set to ensure validated by factory
      mp.set(bean, mp.get(bean));
      
    } else if ("null".equals(value)) {
      // forcibly set to null
      mp.set(bean, null);
      
    } else if (value.contains("::")) {
      // double colon used for component references
      setPropertyComponentRef(bean, mp, value);
      
    } else if (MANAGER_PROPERTIES.equals(value) && Resource.class.equals(mp.propertyType())) {
      // set to the combined set of properties
      setPropertyMergedProperties(bean, mp);
      
    } else {
      // set value
      setPropertyInferType(bean, mp, value);
    }
  }

  /**
   * Intelligently sets the property to the merged set of properties.
   * <p>
   * The key "MANAGER.PROPERTIES" can be used in a properties file to refer to
   * the entire set of merged properties. This is normally what you want to pass
   * into other systems (such as Spring) that need a set of properties.
   * 
   * @param bean  the bean, not null
   * @param mp  the property, not null
   * @throws Exception allowing throwing of a checked exception
   */
  protected void setPropertyMergedProperties(Bean bean, MetaProperty<?> mp) throws Exception {
    final String desc = MANAGER_PROPERTIES + " for " + mp;
    final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    Properties props = new Properties();
    props.putAll(getProperties().toMap());
    props.store(out, desc);
    out.close();
    Resource resource = new AbstractResource() {
      @Override
      public String getDescription() {
        return MANAGER_PROPERTIES;
      }
      @Override
      public String getFilename() throws IllegalStateException {
        return MANAGER_PROPERTIES + ".properties";
      }
      @Override
      public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(out.toByteArray());
      }
      @Override
      public String toString() {
        return desc;
      }
    };
    mp.set(bean, resource);
  }

  /**
   * Intelligently sets the property which is a component reference.
   * <p>
   * The double colon is used in the format {@code Type::Classifier}.
   * If the type is omitted, this method will try to infer it.
   * If the value ends in '?' then it is treated as optional.
   * 
   * @param bean  the bean, not null
   * @param mp  the property, not null
   * @param value  the configured value containing double colon, not null
   * @throws ComponentConfigException if the property cannot be initialized
   */
  protected void setPropertyComponentRef(Bean bean, MetaProperty<?> mp, String value) {
    Class<?> propertyType = mp.propertyType();
    String type = StringUtils.substringBefore(value, "::");
    String classifier = StringUtils.substringAfter(value, "::");
    boolean optional = false;
    if (classifier.endsWith("?")) {
      optional = true;
      classifier = classifier.substring(0, classifier.length() - 1).trim();
    }
    // infer type
    if (type.length() == 0) {
      type = propertyType.getName();
    }
    // find info
    ComponentInfo info = getRepository().findInfo(type, classifier);
    if (info == null) {
      if (optional) {
        return;
      }
      throw new ComponentConfigException("Unable to find component reference '" + value + "' while setting property " + mp);
    }
    // store component
    if (ComponentInfo.class.isAssignableFrom(propertyType)) {
      mp.set(bean, info);
    } else {
      mp.set(bean, getRepository().getInstance(info));
    }
  }

  /**
   * Intelligently sets the property.
   * <p>
   * This uses the repository to link properties declared with classifiers to the instance.
   * 
   * @param bean  the bean, not null
   * @param mp  the property, not null
   * @param value  the configured value, not null
   * @throws ComponentConfigException if the property cannot be initialized
   */
  protected void setPropertyInferType(Bean bean, MetaProperty<?> mp, String value) {
    Class<?> propertyType = mp.propertyType();
    if (propertyType == Resource.class) {
      mp.set(bean, ResourceUtils.createResource(value));
      
    } else if (Collection.class.isAssignableFrom(mp.propertyType()) && JodaBeanUtils.collectionType(mp, bean.getClass()) == String.class) {
      Iterable<String> split = Splitter.on(',').trimResults().split(value);
      mp.set(bean, ImmutableList.copyOf(split));
      
    } else {
      // set property by value type conversion from String
      try {
        mp.setString(bean, value);
        
      } catch (RuntimeException ex) {
        throw new ComponentConfigException("Unable to set property " + mp, ex);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the factory.
   * <p>
   * The real work of creating the component and registering it should be done here.
   * The factory may also publish a RESTful view and/or a life-cycle method.
   * 
   * @param factory  the factory to initialize, not null
   * @param remainingConfig  the remaining configuration data, not null
   * @throws Exception to allow components to throw checked exceptions
   * @throws ComponentConfigException if configuration is specified but not used
   */
  protected void initFactory(ComponentFactory factory, LinkedHashMap<String, String> remainingConfig) throws Exception {
    factory.init(getRepository(), remainingConfig);
    if (remainingConfig.size() > 0) {
      throw new ComponentConfigException("Configuration was specified but not used: " + remainingConfig);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Starts the initialized components.
   * <p>
   * Call {@code load(...)} and {@code init()} before this method.
   */
  public void start() {
    _logger.logInfo("--- Starting Lifecycle ---");
    long startInstant = System.nanoTime();
    
    getRepository().start();
    
    long endInstant = System.nanoTime();
    _logger.logInfo("--- Started Lifecycle in " + ((endInstant - startInstant) / 1000000L) + "ms ---");
  }

}

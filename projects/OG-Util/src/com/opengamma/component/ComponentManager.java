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
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.calendar.TimeZone;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Manages the process of loading and starting OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This class loads and starts the components based on configuration.
 * The end result is a populated {@link ComponentRepository}.
 * <p>
 * Two types of config file format are recognized - properties and INI.
 * The INI file is the primary file for loading the components, see {@link ComponentConfigLoader}.
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

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ComponentManager.class);
  /**
   * The key identifying the next config file in a properties file.
   */
  private static final String MANAGER_NEXT_FILE = "MANAGER.NEXT.FILE";
  /**
   * The key identifying the entire combined set of active properties.
   */
  private static final String MANAGER_PROPERTIES = "MANAGER.PROPERTIES";

  /**
   * The component repository.
   */
  private final ComponentRepository _repo;
  /**
   * The component properties.
   */
  private final ConcurrentMap<String, String> _properties = new ConcurrentHashMap<String, String>();

  /**
   * Creates a resource from a string location.
   * <p>
   * This accepts locations starting with "classpath:" or "file:".
   * It also accepts plain locations, treated as "file:".
   * 
   * @param resourceLocation  the resource location, not null
   * @return the resource, not null
   */
  public static Resource createResource(String resourceLocation) {
    if (resourceLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
      return new ClassPathResource(resourceLocation.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), ClassUtils.getDefaultClassLoader());
    }
    if (resourceLocation.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
      return new FileSystemResource(resourceLocation.substring(ResourceUtils.FILE_URL_PREFIX.length()));
    }
    return new FileSystemResource(resourceLocation);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  public ComponentManager() {
    _repo = new ComponentRepository();
  }

  /**
   * Creates an instance.
   * 
   * @param repo  the repository to use, not null
   */
  protected ComponentManager(ComponentRepository repo) {
    ArgumentChecker.notNull(repo, "repo");
    _repo = repo;
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
   * @return the map of key-value properties, not null
   */
  public ConcurrentMap<String, String> getProperties() {
    return _properties;
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the components based on the specified resource.
   * <p>
   * See {@link #createResource(String)} for the valid resource location formats.
   * 
   * @param resourceLocation  the resource location, not null
   * @return the created repository, not null
   */
  public ComponentRepository start(String resourceLocation) {
    Resource resource = createResource(resourceLocation);
    return start(resource);
  }

  /**
   * Initializes the components based on the specified resource.
   * 
   * @param resource  the config resource to load, not null
   * @return the created repository, not null
   */
  public ComponentRepository start(Resource resource) {
    if (resource.getFilename().endsWith(".properties")) {
      String nextConfig = loadProperties(resource);
      if (nextConfig == null) {
        throw new IllegalArgumentException("The properties file must contain the key '" + MANAGER_NEXT_FILE + "' to specify the next file to load: " + resource);
      }
      return start(nextConfig);
    }
    if (resource.getFilename().endsWith(".ini")) {
      loadIni(resource);
      start();
      return getRepository();
    }
    throw new IllegalArgumentException("Unknown file format: " + resource);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads a properties file into the replacements map.
   * <p>
   * The properties file must be in the standard format defined by {@link Properties}.
   * The file must contain a key "component.ini"
   * 
   * @param resource  the properties resource location, not null
   * @return the next configuration file to load, not null
   */
  protected String loadProperties(Resource resource) {
    Properties properties = new Properties();
    try {
      properties.load(resource.getInputStream());
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
    String nextConfig = null;
    for (Entry<Object, Object> entry : properties.entrySet()) {
      String key = entry.getKey().toString();
      String value = entry.getValue().toString();
      if (key.equals(MANAGER_NEXT_FILE)) {
        // the next config file to load
        nextConfig = value;
      } else {
        // putIfAbsent allows values from an override file to be loaded and not overwritten
        getProperties().putIfAbsent(key, value);
      }
    }
    return nextConfig;
  }

  /**
   * Loads the INI file and initializes the components based on the contents.
   * 
   * @param resource  the INI resource location, not null
   */
  protected void loadIni(Resource resource) {
    ComponentConfigLoader loader = new ComponentConfigLoader();
    ComponentConfig config = loader.load(resource, getProperties());
    _repo.pushThreadLocal();
    initGlobal(config);
    init(config);
  }

  //-------------------------------------------------------------------------
  protected void initGlobal(ComponentConfig config) {
    LinkedHashMap<String, String> global = config.getGroup("global");
    if (global != null) {
      String runMode = global.get("run.mode");
      String mds = global.get("market.data.source");
      if (runMode != null && mds != null) {
        PlatformConfigUtils.configureSystemProperties(runMode, mds);
      } else if (runMode != null && mds == null) {
        PlatformConfigUtils.configureSystemProperties(runMode);
      }
      String zoneId = global.get("time.zone");
      if (zoneId != null) {
        OpenGammaClock.setZone(TimeZone.of(zoneId));
      }
    }
  }

  /**
   * Initializes the repository from the config.
   * 
   * @param config  the loaded config, not null
   */
  protected void init(ComponentConfig config) {
    for (String groupName : config.getGroups()) {
      LinkedHashMap<String, String> groupData = config.getGroup(groupName);
      if (groupData.containsKey("factory")) {
        initComponent(groupName, groupData);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Starts the components.
   */
  protected void start() {
    s_logger.info("Starting repository");
    _repo.start();
  }

  //-------------------------------------------------------------------------
  /**
   * Initialize the component.
   * 
   * @param groupName  the group name, not null
   * @param groupConfig  the config data, not null
   */
  protected void initComponent(String groupName, LinkedHashMap<String, String> groupConfig) {
    LinkedHashMap<String, String> remainingConfig = new LinkedHashMap<String, String>(groupConfig);
    String typeStr = remainingConfig.remove("factory");
    s_logger.debug("Initializing component: {} with properties {}", typeStr, remainingConfig);
    
    // load factory
    ComponentFactory factory = loadFactory(typeStr);
    
    // set properties
    try {
      setFactoryProperties(factory, remainingConfig);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Failed to set component factory properties: '" + groupName + "' with " + groupConfig, ex);
    }
    
    // init
    try {
      initFactory(factory, remainingConfig);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Failed to init component factory: '" + groupName + "' with " + groupConfig, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the factory.
   * A factory should perform minimal work in the constructor.
   * 
   * @param typeStr  the factory type class name, not null
   * @return the factory, not null
   */
  protected ComponentFactory loadFactory(String typeStr) {
    ComponentFactory factory;
    try {
      Class<? extends ComponentFactory> cls = getClass().getClassLoader().loadClass(typeStr).asSubclass(ComponentFactory.class);
      factory = cls.newInstance();
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("Unknown component factory: " + typeStr, ex);
    } catch (InstantiationException ex) {
      throw new IllegalArgumentException("Unable to create component factory: " + typeStr, ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalArgumentException("Unable to access component factory: " + typeStr, ex);
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
      for (MetaProperty<Object> mp : bean.metaBean().metaPropertyIterable()) {
        String value = remainingConfig.remove(mp.name());
        if (value == null) {
          // set to ensure validated by factory
          mp.set(bean, mp.get(bean));
        } else if ("null".equals(value)) {
          // forcibly set to null
          mp.set(bean, null);
        } else if (MANAGER_PROPERTIES.equals(value) && Resource.class.equals(mp.propertyType())) {
          // set to the combined set of properties
          setFactoryPropertyManagerProperties(bean, mp);
        } else {
          // set value
          setFactoryProperty(bean, mp, value);
        }
      }
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
  protected void setFactoryPropertyManagerProperties(Bean bean, MetaProperty<Object> mp) throws Exception {
    final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    Properties props = new Properties();
    props.putAll(getProperties());
    props.store(out, MANAGER_PROPERTIES + " for " + mp);
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
    };
    mp.set(bean, resource);
  }

  /**
   * Intelligently sets the property.
   * <p>
   * This uses the repository to link properties declared with classifiers to the instance.
   * 
   * @param bean  the bean, not null
   * @param mp  the property, not null
   * @param value  the value, not null
   * @throws Exception allowing throwing of a checked exception
   */
  protected void setFactoryProperty(Bean bean, MetaProperty<Object> mp, String value) throws Exception {
    Class<?> propertyType = mp.propertyType();
    if (propertyType == ComponentRepository.class) {
      // set the repo
      mp.set(bean, _repo);
      
    } else if (propertyType == Resource.class) {
      mp.set(bean, ComponentManager.createResource(value));
      
    } else {
      // set property by value type conversion from String
      try {
        mp.setString(bean, value);
        
      } catch (RuntimeException ex) {
        // set property by repo lookup
        try {
          mp.set(bean, _repo.getInstance(propertyType, value));
        } catch (RuntimeException ex2) {
          throw new IllegalArgumentException("Unable to convert value for " + mp, ex2);
        }
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
   */
  protected void initFactory(ComponentFactory factory, LinkedHashMap<String, String> remainingConfig) throws Exception {
    factory.init(_repo, remainingConfig);
    if (remainingConfig.size() > 0) {
      throw new IllegalStateException("Configuration was specified but not used: " + remainingConfig);
    }
  }

}

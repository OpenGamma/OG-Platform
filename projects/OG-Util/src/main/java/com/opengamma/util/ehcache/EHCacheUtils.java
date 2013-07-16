/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.apache.commons.io.IOUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for working with EHCache.
 */
public final class EHCacheUtils {

  /** The set of unique names already seen. */
  private static final ConcurrentMap<String, String> UNIQUE_TEST_NAMES = new ConcurrentHashMap<>();
  /** The default Ehcache configuration file name. */
  private static final String DEFAULT_EHCACHE_CONFIG_FILE = "/com/opengamma/util/ehcache/default-ehcache.xml";
  /** The default test Ehcache configuration file name. */
  private static final String DEFAULT_TEST_EHCACHE_CONFIG_FILE = "/com/opengamma/util/ehcache/test-ehcache.xml";
  /** The cached content of the default config file. */
  private static final AtomicReference<byte[]> CONFIG_TEST_FILE_BYTES = new AtomicReference<>();

  /** Null value to cache */
  private static final Object NULL = new Object();

  /**
   * Restrictive constructor.
   */
  private EHCacheUtils() {
  }

  /**
   * Creates a unique cache manager.
   *
   * @param uniqueName  the unique name, typically a test case class
   * @return the unique cache manager, not null
   */
  public static CacheManager createTestCacheManager(Class<?> uniqueName) {
    ArgumentChecker.notNull(uniqueName, "uniqueName");
    return createTestCacheManager(uniqueName.getName());
  }

  /**
   * Creates a unique cache manager.
   *
   * @param uniqueName  the unique name, typically a test case class name
   * @return the unique cache manager, not null
   */
  public static CacheManager createTestCacheManager(String uniqueName) {
    ArgumentChecker.notNull(uniqueName, "uniqueName");
    if (UNIQUE_TEST_NAMES.putIfAbsent(uniqueName, uniqueName) != null) {
      throw new OpenGammaRuntimeException("CacheManager has already been created with unique name: " + uniqueName);
    }
    try {
      InputStream configStream = getTestEhCacheConfig();
      Configuration config = ConfigurationFactory.parseConfiguration(configStream);
      config.setName(uniqueName);
      config.setUpdateCheck(false);
      return CacheManager.newInstance(config);
    } catch (CacheException ex) {
      throw new OpenGammaRuntimeException("Unable to create CacheManager", ex);
    }
  }

  private static synchronized InputStream getTestEhCacheConfig() {
    byte[] bytes = CONFIG_TEST_FILE_BYTES.get();
    if (bytes == null) {
      String ehcacheConfigFile = DEFAULT_TEST_EHCACHE_CONFIG_FILE;
      String overrideEhcacheConfigFile = System.getProperty("ehcache.config"); // passed in by Ant
      if (overrideEhcacheConfigFile != null) {
        ehcacheConfigFile = overrideEhcacheConfigFile;
        System.err.println("Using ehcache.config from system property: " + ehcacheConfigFile);
      } else {
        System.err.println("Using default test ehcache.config file name: " + ehcacheConfigFile);
      }
      InputStream in = EHCacheUtils.class.getResourceAsStream(ehcacheConfigFile);
      try {
        bytes = IOUtils.toByteArray(in);
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("Unable to read cache configuration", ex);
      }
      CONFIG_TEST_FILE_BYTES.compareAndSet(null, bytes);
      bytes = CONFIG_TEST_FILE_BYTES.get();
    }
    return new ByteArrayInputStream(bytes);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates/returns the singleton cache manager using the default configuration. This should be used only in a test
   * environment; in other environments a properly configured cache manager should be injected.
   * <p>
   * Since Ehcache 2.5 multiple cache managers with the same name are not allowed, so parallel tests have to use the
   * same default cache manager instance.
   *
   * (from Ehcache docs)
   * With Ehcache 2.5.2 and higher, the behavior of the CacheManager creation methods is as follows:
   *   CacheManager.newInstance(Configuration configuration) – Create a new CacheManager or return the existing one
   *     named in the configuration.
   *   CacheManager.create() – Create a new singleton CacheManager with default configuration, or return the existing
   *     singleton. This is the same as CacheManager.getInstance().
   *   CacheManager.create(Configuration configuration) – Create a singleton CacheManager with the passed-in
   *     configuration, or return the existing singleton.
   *   new CacheManager(Configuration configuration) – Create a new CacheManager, or throw an exception if the
   *     CacheManager named in the configuration already exists or if the parameter (configuration) is null.
   * <p>
   * Tests should use {@link #createTestCacheManager(String)} or {@link #createTestCacheManager(Class)}.
   *
   * @return the cache manager, not null
   */
  public static CacheManager createCacheManager() {
    try {
      return CacheManager.newInstance(getEhCacheConfig());
    } catch (CacheException ex) {
      throw new OpenGammaRuntimeException("Unable to create CacheManager", ex);
    }
  }

  private static synchronized Configuration getEhCacheConfig() {
    String ehcacheConfigFile = DEFAULT_EHCACHE_CONFIG_FILE;
    String overrideEhcacheConfigFile = System.getProperty("ehcache.config"); // passed in by Ant
    if (overrideEhcacheConfigFile != null) {
      ehcacheConfigFile = overrideEhcacheConfigFile;
      System.err.println("Using ehcache.config from system property: " + ehcacheConfigFile);
    } else {
      System.err.println("Using default ehcache.config file name: " + ehcacheConfigFile);
    }
    try (InputStream resource = EHCacheUtils.class.getResourceAsStream(ehcacheConfigFile)) {
      Configuration config = ConfigurationFactory.parseConfiguration(resource);
      config.setUpdateCheck(false);
      return config;
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read ehcache file", ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Clears the contents of a cache manager.
   *
   * @param cacheManager  the cache manager, not null
   */
  public static void clear(CacheManager cacheManager) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    cacheManager.clearAll();
  }

  /**
   * Clears the contents of a named cache, if that cache exists, without deleting the cache itself.
   *
   * @param cacheManager  the cache manager, not null
   * @param cacheName  the cache name, not null
   */
  public static void clear(CacheManager cacheManager, String cacheName) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(cacheName, "cacheName");
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.removeAll();
    }
  }

  /**
   * Adds a cache to the cache manager if necessary.
   * <p>
   * The cache configuration is loaded from the manager's configuration, or the default is used.
   *
   * @param manager  the cache manager, not null
   * @param cache  the cache, not null
   */
  public static void addCache(CacheManager manager, Cache cache) {
    ArgumentChecker.notNull(manager, "manager");
    ArgumentChecker.notNull(cache, "cache");
    if (!manager.cacheExists(cache.getName())) {
      try {
        manager.addCache(cache);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to add cache " + cache.getName(), ex);
      }
    }
  }

  /**
   * Adds a cache to the cache manager if necessary.
   * <p>
   * The cache configuration is loaded from the manager's configuration, or the default is used.
   *
   * @param manager  the cache manager, not null
   * @param name  the cache name, not null
   */
  public static void addCache(final CacheManager manager, final String name) {
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(name);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, ex);
      }
    }
  }

  /**
   * Gets a cache from the manager.
   * @param manager  the manager, not null
   * @param name  the cache name, not null
   * @return the cache, not null
   */
  public static Cache getCacheFromManager(CacheManager manager, String name) {
    try {
      return manager.getCache(name);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException(
          "Unable to retrieve from CacheManager, cache: " + name, ex);
    }
  }


  @SuppressWarnings("unchecked")
  public static <T> T get(final Element e) {
    final Object o = e.getObjectValue();
    if (o == NULL) {
      return null;
    }
    if (o instanceof RuntimeException) {
      throw (RuntimeException) o;
    }
    return (T) o;
  }

  public static <T> Collection<T> putValues(final Object key, final Collection<T> values, final Cache cache) {
    final Element e;
    if (values == null) {
      e = new Element(key, NULL);
    } else {
      e = new Element(key, values);
    }
    cache.put(e);
    return values;
  }

  public static <T> T putValue(final Object key, final T value, final Cache cache) {
    final Element e;
    if (value == null) {
      e = new Element(key, NULL);
    } else {
      e = new Element(key, value);
    }
    cache.put(e);
    return value;
  }

  public static <T> T putException(final Object key, final RuntimeException e, final Cache cache) {
    cache.put(new Element(key, e));
    throw e;
  }

  //-------------------------------------------------------------------------
  /**
   * Shuts down the cache manager, only really useful in tests.
   * <p>
   * The cache manager that is shutdown should be started with
   * {@code new CacheManager()} or similar.
   *
   * @param cacheManager  the cache manager, null ignored
   * @return null
   */
  public static CacheManager shutdownQuiet(CacheManager cacheManager) {
    if (cacheManager != null) {
      try {
        cacheManager.shutdown();
      } catch (RuntimeException ex) {
        // ignore
      }
    }
    return null;
  }

}

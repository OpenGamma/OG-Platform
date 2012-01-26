/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caches a set of external function declarations. The cache can be written to
 * disk, and reloaded to survive a JVM restart.
 */
/* package */final class ExternalFunctionCache {

  private static final Logger s_logger = LoggerFactory.getLogger(ExternalFunctionCache.class);

  protected static final String CACHE_PATH_PROPERTY = "language.annotationCachePath";
  protected static final String CACHE_FILE_NAME = "." + ExternalFunctionCache.class.getSimpleName();

  private final Instant _timestamp;
  private final Collection<String> _classNames = new LinkedList<String>();

  private ExternalFunctionCache(final Instant timestamp) {
    _timestamp = timestamp;
  }

  private void add(final String className) {
    _classNames.add(className);
  }

  private void addAll(final Collection<String> classNames) {
    _classNames.addAll(classNames);
  }

  /**
   * Returns the last modification timestamp.
   * 
   * @return the timestamp, not null
   */
  public Instant getTimestamp() {
    return _timestamp;
  }

  protected Collection<String> getClassNames() {
    return _classNames;
  }

  /**
   * Returns the classes from the cache that contain function annotations.
   * 
   * @return the classes
   * @throws ClassNotFoundException if entries in the cache are invalid
   */
  public Collection<Class<?>> getClasses() {
    if (getClassNames().isEmpty()) {
      return Collections.emptyList();
    }
    final Collection<Class<?>> classes = new ArrayList<Class<?>>(getClassNames().size());
    for (String className : getClassNames()) {
      try {
        classes.add(Class.forName(className));
      } catch (ClassNotFoundException e) {
        s_logger.info("Class not found", e);
      }
    }
    return classes;
  }

  /**
   * Loads the function cache from disk (if available).
   * 
   * @return the cache object, not null
   */
  public static ExternalFunctionCache load() {
    final String path = System.getProperty(CACHE_PATH_PROPERTY);
    if (path == null) {
      s_logger.warn("No cache path set in system property {}", CACHE_PATH_PROPERTY);
      return new ExternalFunctionCache(Instant.EPOCH);
    }
    final File cacheFile = new File(new File(path), CACHE_FILE_NAME);
    try {
      final BufferedReader br = new BufferedReader(new FileReader(cacheFile));
      String str = br.readLine();
      final ExternalFunctionCache cache = new ExternalFunctionCache(Instant.ofEpochMillis(Long.parseLong(str.trim())));
      while ((str = br.readLine()) != null) {
        str = str.trim();
        if (str.length() > 0) {
          cache.add(str);
        }
      }
      br.close();
      return cache;
    } catch (Throwable t) {
      s_logger.warn("Couldn't read cache file", t);
      return new ExternalFunctionCache(Instant.EPOCH);
    }
  }

  /**
   * Saves the function cache to disk (if possible).
   */
  public void save() {
    final String path = System.getProperty(CACHE_PATH_PROPERTY);
    if (path == null) {
      s_logger.warn("No cache path set in system property {}", CACHE_PATH_PROPERTY);
      return;
    }
    final File cacheFile = new File(new File(path), CACHE_FILE_NAME);
    try {
      final PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(cacheFile)));
      pw.println(getTimestamp().toEpochMillisLong());
      for (String className : getClassNames()) {
        pw.println(className);
      }
      pw.flush();
      pw.close();
    } catch (Throwable t) {
      s_logger.warn("Couldn't write cache file", t);
    }
  }

  /**
   * Creates a function cache from a set of class names
   * 
   * @return the cache object, not null
   */
  public static ExternalFunctionCache create(final Instant timestamp, final Collection<String> classNames) {
    final ExternalFunctionCache cache = new ExternalFunctionCache(timestamp);
    cache.addAll(classNames);
    return cache;
  }

}

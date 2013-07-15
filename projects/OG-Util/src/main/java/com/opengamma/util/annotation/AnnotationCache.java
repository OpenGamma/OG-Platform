/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

/**
 * Caches a set of annotated classes. The cache can be written to disk, and reloaded to survive a JVM restart.
 */
public final class AnnotationCache {

  private static final Logger s_logger = LoggerFactory.getLogger(AnnotationCache.class);

  /**
   * The cache path property name
   */
  public static final String CACHE_PATH_PROPERTY = "opengamma.annotationCachePath";

  private final Instant _timestamp;
  private final Collection<String> _classNames = new LinkedList<String>();
  private final Class<? extends Annotation> _annotationClass; 
  private final String _cacheFileName;

  private AnnotationCache(final Instant timestamp, Class<? extends Annotation> annotationClass) {
    _timestamp = timestamp;
    _annotationClass = annotationClass;
    _cacheFileName = "." + annotationClass.getSimpleName();
    
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
 
  /**
   * Gets the classNames.
   * @return the classNames
   */
  public Collection<String> getClassNames() {
    return ImmutableList.copyOf(_classNames);
  }

  /**
   * Gets the annotationClass.
   * @return the annotationClass
   */
  public Class<? extends Annotation> getAnnotationClass() {
    return _annotationClass;
  }

  /**
   * Gets the cacheFileName.
   * @return the cacheFileName
   */
  public String getCacheFileName() {
    return _cacheFileName;
  }

  /**
   * Returns the classes from the cache that contain function annotations.
   * 
   * @return the classes
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
  
  protected static String getCacheFileName(Class<? extends Annotation> annotationClass) {
    return "." + annotationClass.getSimpleName();
  }

  /**
   * Loads the function cache from disk (if available).
   * 
   * @param annotationClass the annotation class, not null
   * @return the cache object, not null
   */
  public static AnnotationCache load(Class<? extends Annotation> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotation class");
    final String path = System.getProperty(CACHE_PATH_PROPERTY);
    if (path == null) {
      s_logger.warn("No cache path set in system property {}", CACHE_PATH_PROPERTY);
      return new AnnotationCache(Instant.EPOCH, annotationClass);
    }
    final File cacheFile = new File(new File(path), getCacheFileName(annotationClass));
    try {
      final BufferedReader br = new BufferedReader(new FileReader(cacheFile));
      String str = br.readLine();
      final AnnotationCache cache = new AnnotationCache(Instant.ofEpochMilli(Long.parseLong(str.trim())), annotationClass);
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
      return new AnnotationCache(Instant.EPOCH, annotationClass);
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
    final File cacheFile = new File(new File(path), getCacheFileName());
    try {
      final PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(cacheFile)));
      pw.println(getTimestamp().toEpochMilli());
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
   * @param timestamp  the cache timestamp, not null
   * @param annotationClass  the annotation class, not null
   * @param classNames  the class names, not null
   * @return the cache object, not null
   */
  public static AnnotationCache create(
      final Instant timestamp, final Class<? extends Annotation> annotationClass, final Collection<String> classNames) {
    final AnnotationCache cache = new AnnotationCache(timestamp, annotationClass);
    cache.addAll(classNames);
    return cache;
  }

}

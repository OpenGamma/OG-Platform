/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.fudgemsg.AnnotationReflector;
import org.fudgemsg.FudgeRuntimeException;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.threeten.bp.Instant;

/**
 * Scans the class path for classes that contain the given annotations.
 */
public final class ClasspathScanner {

  private final Set<URL> _urls;
  private final Instant _timestamp;

  private static volatile Set<URL> s_classPathElements;

  private boolean _scanClassAnnotations = true;
  private boolean _scanMethodAnnotations = true;
  private boolean _scanParameterAnnotations = true;
  private boolean _scanFieldAnnotations = true;

  public ClasspathScanner() {
    _urls = getClassPathElements();
    _timestamp = timestamp(_urls);
  }

  private Set<URL> getClassPathElements() {
    if (s_classPathElements == null) {
      s_classPathElements = findClassPathElements();
    }
    return s_classPathElements;
  }

  private Set<URL> findClassPathElements() {
    Set<URL> results = new LinkedHashSet<URL>();
    String javaClassPath = System.getProperty("java.class.path");
    String[] paths = javaClassPath.split(Pattern.quote(File.pathSeparator));
    for (String path : paths) {
      File f = new File(path);
      if (!f.exists()) {
        continue;
      }
      URL url;
      try {
        url = f.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new FudgeRuntimeException("Could not convert file " + f + " to URL", e);
      }
      results.add(url);
    }
    return results;
  }

  private static Instant timestamp(final Iterable<URL> urls) {
    long ctime = 0;
    for (URL url : urls) {
      try {
        final File f = new File(url.toURI());
        final long l = f.lastModified();
        if (l > ctime) {
          ctime = l;
        }
      } catch (URISyntaxException e) {
        // Ignore this one
      }
    }
    return Instant.ofEpochMilli(ctime);
  }

  /**
   * Returns the timestamp of the most recently modified Jar (or class) in the class path.
   * 
   * @return the timestamp, not null
   */
  public Instant getTimestamp() {
    return _timestamp;
  }

  /**
   * Scans the classpath to produce a populated cache.
   * 
   * @param annotationClass the annotation to search for
   * @return the cache, not null
   */
  @SuppressWarnings("rawtypes")
  public AnnotationCache scan(Class<? extends Annotation> annotationClass) {
    int scanners = 0;
    if (isScanClassAnnotations()) {
      scanners++;
    }
    if (isScanFieldAnnotations()) {
      scanners++;
    }
    if (isScanMethodAnnotations()) {
      scanners++;
    }
    if (isScanParameterAnnotations()) {
      scanners++;
    }
    final Object[] config = new Object[scanners + 2];
    scanners = 0;
    if (isScanClassAnnotations()) {
      config[scanners++] = new TypeAnnotationsScanner();
    }
    if (isScanFieldAnnotations()) {
      config[scanners++] = new FieldAnnotationsScanner();
    }
    if (isScanMethodAnnotations()) {
      config[scanners++] = new MethodAnnotationsScanner();
    }
    if (isScanParameterAnnotations()) {
      config[scanners++] = new MethodParameterScanner();
    }
    config[scanners++] = ClasspathScanner.class.getClassLoader();
    config[scanners++] = Thread.currentThread().getContextClassLoader();
    AnnotationReflector reflector = new AnnotationReflector(null, _urls, config);
    final HashSet<String> classNames = new HashSet<String>();
    if (isScanClassAnnotations()) {
      classNames.addAll(reflector.getReflector().getStore().getTypesAnnotatedWith(annotationClass.getName()));
    }
    if (isScanFieldAnnotations()) {
      Set<Field> fields = reflector.getReflector().getFieldsAnnotatedWith(annotationClass);
      for (Field field : fields) {
        classNames.add(field.getDeclaringClass().getName());
      }
    }
    if (isScanMethodAnnotations()) {
      Set<Method> methods = reflector.getReflector().getMethodsAnnotatedWith(annotationClass);
      for (Method method : methods) {
        classNames.add(method.getDeclaringClass().getName());
      }
      Set<Constructor> constructors = reflector.getReflector().getConstructorsAnnotatedWith(annotationClass);
      for (Constructor constructor : constructors) {
        classNames.add(constructor.getDeclaringClass().getName());
      }
    }
    if (isScanParameterAnnotations()) {
      Set<Method> paramMethods = reflector.getReflector().getMethodsWithAnyParamAnnotated(annotationClass);
      for (Method method : paramMethods) {
        classNames.add(method.getDeclaringClass().getName());
      }
    }
    return AnnotationCache.create(getTimestamp(), annotationClass, classNames);
  }

  /**
   * Gets the scanClassAnnotations.
   * 
   * @return the scanClassAnnotations
   */
  public boolean isScanClassAnnotations() {
    return _scanClassAnnotations;
  }

  /**
   * Sets the scanClassAnnotations.
   * 
   * @param scanClassAnnotations the scanClassAnnotations
   */
  public void setScanClassAnnotations(boolean scanClassAnnotations) {
    _scanClassAnnotations = scanClassAnnotations;
  }

  /**
   * Gets the scanMethodAnnotations.
   * 
   * @return the scanMethodAnnotations
   */
  public boolean isScanMethodAnnotations() {
    return _scanMethodAnnotations;
  }

  /**
   * Sets the scanMethodAnnotations.
   * 
   * @param scanMethodAnnotations the scanMethodAnnotations
   */
  public void setScanMethodAnnotations(boolean scanMethodAnnotations) {
    _scanMethodAnnotations = scanMethodAnnotations;
  }

  /**
   * Gets the scanParameterAnnotations.
   * 
   * @return the scanParameterAnnotations
   */
  public boolean isScanParameterAnnotations() {
    return _scanParameterAnnotations;
  }

  /**
   * Sets the scanParameterAnnotations.
   * 
   * @param scanParameterAnnotations the scanParameterAnnotations
   */
  public void setScanParameterAnnotations(boolean scanParameterAnnotations) {
    _scanParameterAnnotations = scanParameterAnnotations;
  }

  /**
   * Gets the scanFieldAnnotations.
   * 
   * @return the scanFieldAnnotations
   */
  public boolean isScanFieldAnnotations() {
    return _scanFieldAnnotations;
  }

  /**
   * Sets the scanFieldAnnotations.
   * 
   * @param scanFieldAnnotations the scanFieldAnnotations
   */
  public void setScanFieldAnnotations(boolean scanFieldAnnotations) {
    _scanFieldAnnotations = scanFieldAnnotations;
  }

}

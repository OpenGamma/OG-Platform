/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeRuntimeException;
import org.scannotation.AnnotationDB;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Scans the class path for classes that contain the given annotations.
 */
/* package */final class ClasspathScanner {

  private final URL[] _urls;
  private final Instant _timestamp;
  
  private static volatile URL[] s_classPathElements;
  
  private boolean _scanClassAnnotations = true;
  private boolean _scanMethodAnnotations = true;
  private boolean _scanParameterAnnotations = true;
  private boolean _scanFieldAnnotations = true;

  public ClasspathScanner() {
    _urls = getClassPathElements();
    _timestamp = timestamp(_urls);
  }

  private URL[] getClassPathElements() {
    if (s_classPathElements == null) {
      s_classPathElements = findClassPathElements();
    }
    
    return s_classPathElements;
  }

  private URL[] findClassPathElements() {
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
    return results.toArray(new URL[0]);
  }

  private static Instant timestamp(final URL[] urls) {
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
   * Returns the timestamp of the most recently modified Jar (or class) in the
   * class path.
   * 
   * @return the timestamp, not null
   */
  public Instant getTimestamp() {
    return _timestamp;
  }

  /**
   * Scans the classpath to produce a populated cache.
   * @param annotationClass 
   * 
   * @return the cache, not null
   */
  protected AnnotationCache scan(Class<? extends Annotation> annotationClass) {
    final AnnotationDB annoDb = new AnnotationDB();
    annoDb.setScanClassAnnotations(_scanClassAnnotations);
    annoDb.setScanFieldAnnotations(_scanFieldAnnotations);
    annoDb.setScanMethodAnnotations(_scanMethodAnnotations);
    annoDb.setScanParameterAnnotations(_scanParameterAnnotations);
    try {
      annoDb.scanArchives(_urls);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Couldn't scan archives", ex);
    }
    Set<String> classNames = annoDb.getAnnotationIndex().get(annotationClass.getName());
    if (classNames == null) {
      classNames = Collections.emptySet();
    }
    final AnnotationCache cache = AnnotationCache.create(getTimestamp(), annotationClass, classNames);
    return cache;
  }

  /**
   * Gets the scanClassAnnotations.
   * @return the scanClassAnnotations
   */
  public boolean isScanClassAnnotations() {
    return _scanClassAnnotations;
  }

  /**
   * Sets the scanClassAnnotations.
   * @param scanClassAnnotations  the scanClassAnnotations
   */
  public void setScanClassAnnotations(boolean scanClassAnnotations) {
    _scanClassAnnotations = scanClassAnnotations;
  }

  /**
   * Gets the scanMethodAnnotations.
   * @return the scanMethodAnnotations
   */
  public boolean isScanMethodAnnotations() {
    return _scanMethodAnnotations;
  }

  /**
   * Sets the scanMethodAnnotations.
   * @param scanMethodAnnotations  the scanMethodAnnotations
   */
  public void setScanMethodAnnotations(boolean scanMethodAnnotations) {
    _scanMethodAnnotations = scanMethodAnnotations;
  }

  /**
   * Gets the scanParameterAnnotations.
   * @return the scanParameterAnnotations
   */
  public boolean isScanParameterAnnotations() {
    return _scanParameterAnnotations;
  }

  /**
   * Sets the scanParameterAnnotations.
   * @param scanParameterAnnotations  the scanParameterAnnotations
   */
  public void setScanParameterAnnotations(boolean scanParameterAnnotations) {
    _scanParameterAnnotations = scanParameterAnnotations;
  }

  /**
   * Gets the scanFieldAnnotations.
   * @return the scanFieldAnnotations
   */
  public boolean isScanFieldAnnotations() {
    return _scanFieldAnnotations;
  }

  /**
   * Sets the scanFieldAnnotations.
   * @param scanFieldAnnotations  the scanFieldAnnotations
   */
  public void setScanFieldAnnotations(boolean scanFieldAnnotations) {
    _scanFieldAnnotations = scanFieldAnnotations;
  }
  
}

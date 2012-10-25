/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.scannotation.AnnotationDB;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClasspathUtils;

/**
 * Static utility for scanning classes for a particular annotation.
 */
public final class ClassNameAnnotationScanner {

  /**
   * Restricted constructor.
   */
  private ClassNameAnnotationScanner() {
  }

  /**
   * Scans the specified classpath for an annotation.
   * 
   * @param classpathElements  the classpath, not null
   * @param annotationClass  the annotation to find, not null
   * @return the matching elements, not null
   */
  public static Set<String> scan(String[] classpathElements, Class<?> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotationClass");
    return scan(classpathElements, annotationClass.getName());
  }

  /**
   * Scans the specified classpath for an annotation.
   * 
   * @param classpathElements  the classpath, not null
   * @param annotationClassName  the annotation to find, not null
   * @return the matching elements, not null
   */
  public static Set<String> scan(String[] classpathElements, String annotationClassName) {
    URL[] classpathUrls = ClasspathUtils.getClasspathURLs(classpathElements);
    return scan(classpathUrls, annotationClassName);
  }

  /**
   * Scans the specified classpath for an annotation.
   * 
   * @param classpathUrls  the classpath, not null
   * @param annotationClassName  the annotation to find, not null
   * @return the matching elements, not null
   */
  public static Set<String> scan(URL[] classpathUrls, String annotationClassName) {
    ArgumentChecker.notNull(annotationClassName, "annotationClassName");
    AnnotationDB annotationDb = getAnnotationDb(classpathUrls);
    Set<String> classNames = annotationDb.getAnnotationIndex().get(annotationClassName);
    if (classNames == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(classNames);
  }

  /**
   * Gets the annotation database for the specific classpath.
   * 
   * @param classpathUrlArray  the classpath URLs, not null
   * @return the annotation database, not null
   */
  private static AnnotationDB getAnnotationDb(URL[] classpathUrlArray) {
    AnnotationDB annotationDb = new AnnotationDB();
    annotationDb.setScanClassAnnotations(true);
    annotationDb.setScanMethodAnnotations(true);
    annotationDb.setScanFieldAnnotations(true);
    annotationDb.setScanParameterAnnotations(false);
    try {
      annotationDb.scanArchives(classpathUrlArray);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error scanning for annotations", ex);
    }
    return annotationDb;
  }

}

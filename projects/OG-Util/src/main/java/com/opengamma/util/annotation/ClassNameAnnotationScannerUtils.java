/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClasspathUtils;

/**
 * Static utility for scanning classes for a particular annotation.
 */
public final class ClassNameAnnotationScannerUtils {

  /**
   * Restricted constructor.
   */
  private ClassNameAnnotationScannerUtils() {
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
    Set<URL> urls = new HashSet<>(Arrays.asList(classpathUrls));
    AnnotationReflector reflector = new AnnotationReflector(
        null, urls, new TypeAnnotationsScanner(),
        ClassNameAnnotationScannerUtils.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
    Set<String> classNames = reflector.getReflector().getStore().getTypesAnnotatedWith(annotationClassName);
    if (classNames == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(classNames);
  }

}

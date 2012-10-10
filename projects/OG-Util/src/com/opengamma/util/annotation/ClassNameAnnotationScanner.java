/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClasspathUtils;

/**
 * Static utility for scanning classes for a particular annotation.
 */
public final class ClassNameAnnotationScanner {

  private static final Logger s_logger = LoggerFactory.getLogger(ClassNameAnnotationScanner.class);
  
  private ClassNameAnnotationScanner() {
  }
  
  public static Set<String> scan(String[] classpath, Class<?> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotationClass");
    return scan(classpath, annotationClass.getName());
  }
  
  public static Set<String> scan(String[] classpathElements, String annotationClassName) {
    URL[] classpathUrls = ClasspathUtils.getClasspathURLs(classpathElements);
    return scan(classpathUrls, annotationClassName);
  }
  
  public static Set<String> scan(URL[] classpathUrls, String annotationClassName) {
    ArgumentChecker.notNull(annotationClassName, "annotationClassName");
    AnnotationDB annotationDb = getAnnotationDb(classpathUrls);
    Set<String> classNames = annotationDb.getAnnotationIndex().get(annotationClassName);
    if (classNames == null) {
      classNames = Collections.emptySet();
    }
    return Collections.unmodifiableSet(classNames);
  }
  
  private static AnnotationDB getAnnotationDb(URL[] classpathUrlArray) {
    AnnotationDB annotationDb = new AnnotationDB();
    annotationDb.setScanClassAnnotations(true);
    annotationDb.setScanMethodAnnotations(true);
    annotationDb.setScanFieldAnnotations(true);
    annotationDb.setScanParameterAnnotations(false);
    try {
      annotationDb.scanArchives(classpathUrlArray);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error scanning for annotations", e);
    }
    return annotationDb;
  }
  
}

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

import org.apache.tools.ant.BuildException;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

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
    URL[] classpathUrls = getClasspathURLs(classpathElements);
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
      throw new BuildException("Error scanning for annotations", e);
    }
    return annotationDb;
  }
  
  private static URL[] getClasspathURLs(String[] classpath) {
    if (classpath == null) {
      return new URL[0];
    }
    Set<URL> classpathUrls = new HashSet<URL>();
    for (String classpathEntry : classpath) {
      File f = new File(classpathEntry);
      if (!f.exists()) {
        s_logger.debug("Skipping non-existent classpath entry '{}'", classpathEntry);
        continue;
      }
      try {
        classpathUrls.add(f.toURI().toURL());
      } catch (MalformedURLException e) {
        throw new BuildException("Error interpreting classpath entry '" + classpathEntry + "' as URL", e);
      }
    }
    URL[] classpathUrlArray = classpathUrls.toArray(new URL[0]);
    return classpathUrlArray;
  }
  
}

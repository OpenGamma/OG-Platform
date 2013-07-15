/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link AnnotationScanner} that caches and uses Reflections.
 */
public class AnnotationScannerImpl implements AnnotationScanner {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AnnotationScannerImpl.class);
  
  @Override
  public synchronized Set<Class<?>> scan(Class<? extends Annotation> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotation class");
    
    AnnotationCache cache = AnnotationCache.load(annotationClass);
    final ClasspathScanner scanner = new ClasspathScanner();
    if (!scanner.getTimestamp().isAfter(cache.getTimestamp())) {
      s_logger.info("loading {} annotation from cache", annotationClass.getSimpleName());
      return ImmutableSet.copyOf(cache.getClasses());
    }
    s_logger.info("Scanning class path for classes annotated with {}", annotationClass.getSimpleName());
    cache = scanner.scan(annotationClass);
    cache.save();
    return ImmutableSet.copyOf(cache.getClasses());
  }
  
}

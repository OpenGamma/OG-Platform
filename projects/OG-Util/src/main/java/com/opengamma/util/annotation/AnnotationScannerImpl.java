/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link AnnotationScanner} using the Reflections library.
 */
public class AnnotationScannerImpl implements AnnotationScanner {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AnnotationScannerImpl.class);
  
  @Override
  public synchronized Set<Class<?>> scan(Class<? extends Annotation> annotationClass) {
    ArgumentChecker.notNull(annotationClass, "annotation class");
    
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    s_logger.info("Scanning class path for classes annotated with {}", annotationClass.getSimpleName());
    Set<Class<?>> result = reflector.getReflector().getTypesAnnotatedWith(annotationClass);
    s_logger.info("Scanned class path found {} results: ", result.size(), result);
    return ImmutableSet.copyOf(result);
  }
  
}

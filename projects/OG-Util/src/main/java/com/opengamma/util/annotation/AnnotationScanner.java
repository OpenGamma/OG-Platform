/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Scans Opengamma archive for a given annotation
 */
public interface AnnotationScanner {
  
  Set<Class<?>> scan(Class<? extends Annotation> annotationClass);

}

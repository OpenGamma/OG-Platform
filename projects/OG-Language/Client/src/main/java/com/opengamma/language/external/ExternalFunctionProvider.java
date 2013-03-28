/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.util.annotation.AnnotationCache;
import com.opengamma.util.annotation.ClasspathScanner;

/**
 * Creates and exposes functions based on methods that are annotated with
 * {@link ExternalFunction}.
 */
public class ExternalFunctionProvider extends AbstractFunctionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(ExternalFunctionProvider.class);
  private static boolean s_excludeTests = true;

  private List<MetaFunction> _functions;

  private static List<MetaFunction> functions() {
    AnnotationCache cache = AnnotationCache.load(ExternalFunction.class);
    final ClasspathScanner scanner = new ClasspathScanner();
    if (!scanner.getTimestamp().isAfter(cache.getTimestamp())) {
      s_logger.info("Loading external functions from cache");
      final List<MetaFunction> functions = createFunctions(cache);
      if (functions != null) {
        return functions;
      }
      s_logger.warn("One or more errors loading functions from cache");
    }
    s_logger.info("Scanning class path for annotated external functions");
    
    scanner.setScanClassAnnotations(false);
    scanner.setScanMethodAnnotations(true);
    scanner.setScanFieldAnnotations(false);
    scanner.setScanParameterAnnotations(false);
    
    cache = scanner.scan(ExternalFunction.class);
    final List<MetaFunction> functions = createFunctions(cache);
    if (functions != null) {
      cache.save();
      return functions;
    }
    s_logger.error("Couldn't load external functions");
    return Collections.emptyList();
  }

  public static void setExcludeTests(final boolean excludeTests) {
    s_excludeTests = excludeTests;
  }

  public static boolean isExcludeTests() {
    return s_excludeTests;
  }

  private static boolean isTestClass(final Class<?> clazz) {
    final String[] cs = clazz.getName().split("[\\.\\$]");
    for (final String c : cs) {
      if (c.endsWith("Test")) {
        return true;
      }
    }
    return false;
  }

  private static List<MetaFunction> createFunctions(final AnnotationCache cache) {
    final List<MetaFunction> functions = new ArrayList<MetaFunction>();
    for (final Class<?> clazz : cache.getClasses()) {
      if (isExcludeTests() && isTestClass(clazz)) {
        continue;
      }
      final ExternalFunctionHandler handler = new ExternalFunctionHandler(clazz);
      functions.addAll(handler.getFunctions());
    }
    return functions;
  }

  public ExternalFunctionProvider() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        getFunctions();
      }
    }).start();
  }

  protected synchronized List<MetaFunction> getFunctions() {
    if (_functions == null) {
      _functions = functions();
    }
    return _functions;
  }

  @Override
  protected void loadDefinitions(final Collection<MetaFunction> definitions) {
    definitions.addAll(getFunctions());
  }

}

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

/**
 * Creates and exposes functions based on methods that are annotated with
 * {@link ExternalFunction}.
 */
public class ExternalFunctionProvider extends AbstractFunctionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(ExternalFunctionProvider.class);

  private final List<MetaFunction> _functions = functions();

  private static List<MetaFunction> functions() {
    ExternalFunctionCache cache = ExternalFunctionCache.load();
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
    cache = scanner.scan();
    final List<MetaFunction> functions = createFunctions(cache);
    if (functions != null) {
      cache.save();
      return functions;
    }
    s_logger.error("Couldn't load external functions");
    return Collections.emptyList();
  }

  @SuppressWarnings("unused")
  private static boolean isTestClass(final Class<?> clazz) {
    String[] cs = clazz.getName().split("[\\.\\$]");
    for (String c : cs) {
      if (c.endsWith("Test")) {
        return true;
      }
    }
    return false;
  }

  private static List<MetaFunction> createFunctions(final ExternalFunctionCache cache) {
    final List<MetaFunction> functions = new ArrayList<MetaFunction>();
    for (Class<?> clazz : cache.getClasses()) {
      // Use the following test when running with the Test classes in the classpath to
      // avoid generating documentation for them.
      /*if (isTestClass(clazz)) {
        continue;
      }*/
      final ExternalFunctionHandler handler = new ExternalFunctionHandler(clazz);
      functions.addAll(handler.getFunctions());
    }
    return functions;
  }

  protected List<MetaFunction> getFunctions() {
    return _functions;
  }

  @Override
  protected void loadDefinitions(Collection<MetaFunction> definitions) {
    definitions.addAll(getFunctions());
  }

}

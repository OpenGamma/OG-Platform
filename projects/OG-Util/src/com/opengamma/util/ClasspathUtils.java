/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Classpath utilities
 */
public class ClasspathUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(ClasspathUtils.class);
  
  /**
   * Obtains an array of URLs from an array of file names.
   * 
   * @param classpath  the classpath, may be null
   * @return an array of URLs
   */
  public static URL[] getClasspathURLs(String[] classpath) {
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
        throw new OpenGammaRuntimeException("Error interpreting classpath entry '" + classpathEntry + "' as URL", e);
      }
    }
    URL[] classpathUrlArray = classpathUrls.toArray(new URL[0]);
    return classpathUrlArray;
  }
  
  public static URL[] getClasspathURLs(Collection<String> classpath) {
    String[] classpathArray = new String[classpath.size()];
    classpathArray = classpath.toArray(classpathArray);
    return getClasspathURLs(classpathArray);
  }
  
}

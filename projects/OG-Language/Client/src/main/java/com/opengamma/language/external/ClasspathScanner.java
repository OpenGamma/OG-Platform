/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import javax.time.Instant;

import org.fudgemsg.types.ClasspathUtilities;
import org.scannotation.AnnotationDB;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.lang.annotation.ExternalFunction;

/**
 * Scans the class path for classes that contain the external function
 * annotations.
 */
/* package */final class ClasspathScanner {

  private final URL[] _urls;
  private final Instant _timestamp;

  public ClasspathScanner() {
    _urls = ClasspathUtilities.getClassPathElements();
    _timestamp = timestamp(_urls);
  }

  private static Instant timestamp(final URL[] urls) {
    long ctime = 0;
    for (URL url : urls) {
      try {
        final File f = new File(url.toURI());
        final long l = f.lastModified();
        if (l > ctime) {
          ctime = l;
        }
      } catch (URISyntaxException e) {
        // Ignore this one
      }
    }
    return Instant.ofEpochMillis(ctime);
  }

  /**
   * Returns the timestamp of the most recently modified Jar (or class) in the
   * class path.
   * 
   * @return the timestamp, not null
   */
  public Instant getTimestamp() {
    return _timestamp;
  }

  /**
   * Scans the classpath to produce a populated cache.
   * 
   * @return the cache, not null
   */
  public ExternalFunctionCache scan() {
    final AnnotationDB annoDb = new AnnotationDB();
    annoDb.setScanClassAnnotations(false);
    annoDb.setScanMethodAnnotations(true);
    annoDb.setScanFieldAnnotations(false);
    annoDb.setScanParameterAnnotations(false);
    try {
      annoDb.scanArchives(_urls);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Couldn't scan archives", ex);
    }
    Set<String> classNames = annoDb.getAnnotationIndex().get(ExternalFunction.class.getName());
    if (classNames == null) {
      classNames = Collections.emptySet();
    }
    final ExternalFunctionCache cache = ExternalFunctionCache.create(getTimestamp(), classNames);
    return cache;
  }

}

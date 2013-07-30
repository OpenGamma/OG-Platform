/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ClassUtils;

/**
 * Utility to assist with Spring style resource locators.
 * <p>
 * This builds on code in Spring, such as
 * {@link org.springframework.core.io.DefaultResourceLoader DefaultResourceLoader}.
 * The recognized locator formats are:
 * <ul>
 * <li>classpath: for a resource in the classpath
 * <li>file: for a resource relative to the working directory
 * <li>a valid URL
 * <li>no prefix, assumed to be a class path resource
 * </ul>
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ResourceUtils {

  /**
   * The prefix for classpath resource locators.
   */
  private static final String CLASSPATH_URL_PREFIX = org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
  /**
   * The prefix for classpath resource locators.
   */
  private static final String FILE_URL_PREFIX = org.springframework.util.ResourceUtils.FILE_URL_PREFIX;

  /**
   * Restricted constructor.
   */
  private ResourceUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a resource from a string location.
   * <p>
   * This accepts locations starting with "classpath:" or "file:".
   * It also accepts plain locations, treated as "file:".
   * 
   * @param resourceLocation  the resource location, not null
   * @return the resource, not null
   */
  public static Resource createResource(String resourceLocation) {
    return createResource(resourceLocation, null);
  }

  /**
   * Creates a resource from a string location.
   * <p>
   * This accepts locations starting with "classpath:" or "file:".
   * It also accepts plain locations, treated as "file:".
   * 
   * @param resourceLocation  the resource location, not null
   * @param classLoader  the class loader, null defaults to {@code ClassUtils.getDefaultClassLoader()}
   * @return the resource, not null
   */
  public static Resource createResource(String resourceLocation, ClassLoader classLoader) {
    ArgumentChecker.notNull(resourceLocation, "resourceLocation");
    if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
      classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
      return new ClassPathResource(resourceLocation.substring(CLASSPATH_URL_PREFIX.length()), classLoader);
    }
    if (resourceLocation.startsWith(FILE_URL_PREFIX)) {
      return new FileSystemResource(resourceLocation.substring(FILE_URL_PREFIX.length()));
    }
    try {
      URL url = new URL(resourceLocation);
      return new UrlResource(url);
    } catch (MalformedURLException ex) {
      return new FileSystemResource(resourceLocation);
    }
  }

  /**
   * Creates a resource locator from a resource.
   * <p>
   * This converts the resource back to a string.
   * Any class loader will be lost.
   * 
   * @param resource  the resource to convert, not null
   * @return the resource locator, not null
   */
  public static String toResourceLocator(Resource resource) {
    ArgumentChecker.notNull(resource, "resource");
    if (resource instanceof ClassPathResource) {
      return CLASSPATH_URL_PREFIX + ((ClassPathResource) resource).getPath();
    }
    if (resource instanceof FileSystemResource) {
      return FILE_URL_PREFIX + ((FileSystemResource) resource).getFile();
    }
    if (resource instanceof UrlResource) {
      try {
        return resource.getURL().toExternalForm();
      } catch (IOException ex) {
        throw new IllegalArgumentException("Invalid UrlResource", ex);
      }
    }
    throw new IllegalArgumentException("Unknown resource type: " + resource.getClass());
  }

  /**
   * Normalizes a resource locator.
   * <p>
   * This creates the resource and converts it back to a string.
   * 
   * @param resourceLocator  the resource to load, not null
   * @return the resource locator, not null
   */
  public static String normalizeResourceLocator(String resourceLocator) {
    return toResourceLocator(createResource(resourceLocator));
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the resource locator to a URL.
   * <p>
   * No check is made for whether the resource exists.
   * This directly invokes Spring.
   * 
   * @param resourceLocator  the resource locator to resolve: either a
   *  "classpath:" pseudo URL, a "file:" URL, or a plain file path
   * @return a corresponding URL object
   * @throws FileNotFoundException if the resource cannot be resolved to a URL
   */
  public static URL getURL(String resourceLocator) throws FileNotFoundException {
    return org.springframework.util.ResourceUtils.getURL(resourceLocator);
  }

  /**
   * Gets the location of the resource, typically for logging.
   * 
   * @param resource  the resource, may be null
   * @return the description of the resource location, not null
   */
  public static String getLocation(Resource resource) {
    if (resource == null) {
      return "null";
    }
    if (resource instanceof FileSystemResource) {
      try {
        return "file: " + resource.getFile().getCanonicalPath();
      } catch (IOException ex) {
        return resource.getDescription();
      }
    }
    if (resource instanceof ClassPathResource) {
      try {
        return "classpath: " + resource.getURL().toExternalForm();
      } catch (IOException ex) {
        return resource.getDescription();
      }
    }
    if (resource instanceof UrlResource) {
      try {
        return "url: " + resource.getURL().toExternalForm();
      } catch (IOException ex) {
        return resource.getDescription();
      }
    }
    return resource.getDescription();
  }

}

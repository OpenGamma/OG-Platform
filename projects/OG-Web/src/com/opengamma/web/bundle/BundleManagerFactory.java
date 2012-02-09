/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.opengamma.util.ArgumentChecker;

/**
 * Factory to create a {@code BundleManager} from the Bundle XML configuration file.
 * <p>
 * This exists to handle the late arrival of the {@code ServletContext}
 */
public class BundleManagerFactory {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BundleManagerFactory.class);

  /**
   * The config resource.
   */
  private Resource _configResource;
  /**
   * The base directory.
   */
  private String _baseDir;
  /**
   * The manager created from this factory.
   */
  private volatile BundleManager _manager;

  /**
   * Creates an instance.
   */
  public BundleManagerFactory() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config resource.
   * 
   * @return the config resource
   */
  public Resource getConfigResource() {
    return _configResource;
  }

  /**
   * Sets the config resource.
   * @param configResource  the config resource
   */
  public void setConfigResource(Resource configResource) {
    _configResource = configResource;
  }

  /**
   * Gets the base directory.
   * @return the base directory
   */
  public String getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the base directory.
   * 
   * @param baseDir  the base directory
   */
  public void setBaseDir(String baseDir) {
    _baseDir = baseDir;
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a bundle manager using the servlet context.
   * <p>
   * This is used to obtain the manager from the servlet context, because the
   * servlet context is usually available after the factory is setup.
   * 
   * @param servletContext  the servlet context, not null
   * @return the manager, not null
   */
  public BundleManager get(ServletContext servletContext) {
    BundleManager manager = _manager;
    if (manager == null) {
      synchronized (this) {
        manager = _manager;
        if (manager == null) {
          _manager = manager = createManager(servletContext);  // CSIGNORE
        }
      }
    }
    return manager;
  }

  /**
   * Creates a bundle manager using the servlet context.
   * 
   * @param servletContext  the servlet context, not null
   * @return the manager, not null
   */
  protected BundleManager createManager(ServletContext servletContext) {
    ArgumentChecker.notNull(servletContext, "servletContext");
    InputStream xmlStream = getXMLStream();
    try {
      BundleParser parser = new BundleParser();
      parser.setBaseDir(resolveBaseDir(servletContext));
      return parser.parse(xmlStream);
    } finally {
      IOUtils.closeQuietly(xmlStream);
    }
  }

  /**
   * Resolves the config file.
   * 
   * @return the resolved file
   */
  protected InputStream getXMLStream() {
    InputStream xmlStream = null;
    try {
      if (_configResource instanceof ClassPathResource) {
        ClassPathResource resource = (ClassPathResource) _configResource;
        s_logger.debug("resource.getPath() : {}", resource.getPath());
        s_logger.debug("resource.getClassLoader() : {}", resource.getClassLoader());
        s_logger.debug("resource.getURL().toString() : {}", resource.getURL().toString());
        s_logger.debug("resource.getDescription() : {}", resource.getDescription());
      }
      xmlStream = _configResource.getInputStream();
      
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot find bundle config xml file in the classpath", ex);
    }
    if (xmlStream == null) {
      throw new IllegalArgumentException("Cannot find bundle config xml file in the classpath");
    }
    return xmlStream;
  }

  /**
   * Resolves the base directory.
   * 
   * @param servletContext  the servlet context, not null
   * @return the base directory, not null
   */
  protected File resolveBaseDir(ServletContext servletContext) {
    String baseDir = getBaseDir().startsWith("/") ? getBaseDir() : "/" + getBaseDir();
    baseDir = servletContext.getRealPath(baseDir);
    return new File(baseDir);
  }

}

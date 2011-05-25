/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.web.bundle.BundleManager;

/**
 * Abstract class for creating BundleManager for Development or Production bundles
 * from the Bundle XML configuration file.
 */
public abstract class AbstractBundleManagerFactoryBean extends SingletonFactoryBean<BundleManager> implements ServletContextAware {

  /**
   * The config file.
   */
  private Resource _configFile;
  /**
   * The base directory.
   */
  private String _baseDir;
  /**
   * The servlet context.
   */
  private ServletContext _servletContext;

  //-------------------------------------------------------------------------
  /**
   * Gets the config file.
   * 
   * @return the config file
   */
  public Resource getConfigFile() {
    return _configFile;
  }

  /**
   * Sets the config file.
   * @param configFile  the config file
   */
  public void setConfigFile(Resource configFile) {
    _configFile = configFile;
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

  /**
   * Gets the servlet context.
   * @return the servlet context
   */
  public ServletContext getServletContext() {
    return _servletContext;
  }

  /**
   * Sets the servlet context.
   * 
   * @param servletContext  the context, not null
   */
  @Override
  public void setServletContext(ServletContext servletContext) {
    _servletContext = servletContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the config file.
   * 
   * @return the resolved file
   */
  protected File resolveConfigurationFile() {
    File configFile = null;
    try {
      configFile = _configFile.getFile();
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot find bundle config file : " + getConfigFile() + " in the classpath");
    }
    return configFile;
  }

  /**
   * Resolves the base directory.
   * 
   * @return the base directory
   */
  protected File resolveBaseDir() {
    ServletContext servletContext = getServletContext();
    if (servletContext != null) {
      String baseDir = getBaseDir().startsWith("/") ? getBaseDir() : "/" + getBaseDir();
      baseDir = servletContext.getRealPath(baseDir);
      return new File(baseDir);
    }
    throw new IllegalStateException("Bundle Manager needs web application context to work out absolute path for bundle base directory");
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.web.bundle.BundleManager;

/**
 * Abstract class for creating BundleManager for Development or Production bundles from the Bundle XML configuration file
 */
public abstract class AbstractBundleManagerFactoryBean extends SingletonFactoryBean<BundleManager>  implements ServletContextAware {
  
  private Resource _configFile;
  
  private String _baseDir;
  
  private ServletContext _servletContext;
  
  /**
   * Gets the configFile field.
   * @return the configFile
   */
  public Resource getConfigFile() {
    return _configFile;
  }

  /**
   * Sets the configFile field.
   * @param configFile  the configFile
   */
  public void setConfigFile(Resource configFile) {
    _configFile = configFile;
  }
  
  /**
   * Gets the baseDir field.
   * @return the baseDir
   */
  public String getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the baseDir field.
   * @param baseDir  the baseDir
   */
  public void setBaseDir(String baseDir) {
    _baseDir = baseDir;
  }

  protected File resolveConfigurationFile() {
    File configFile = null;
    try {
      configFile = _configFile.getFile();
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot find bundle config file : " + getConfigFile() + " in the classpath");
    }
    return configFile;
  }
  
  protected File resolveBaseDir() {
    ServletContext servletContext = getServletContext();
    if (servletContext != null) {
      String baseDir = getBaseDir().startsWith("/") ? getBaseDir() : "/" + getBaseDir();
      baseDir = servletContext.getRealPath(baseDir);
      return new File(baseDir);
    } 
    throw new IllegalStateException("Bundle Manager needs web application context to work out absolute path for bundle base directory");
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    _servletContext = servletContext;
  }
  
  /**
   * Gets the servletContext field.
   * @return the servletContext
   */
  public ServletContext getServletContext() {
    return _servletContext;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Factory to create a {@code BundleManager} from the Bundle XML configuration file.
 * <p>
 * This exists to handle the late arrival of the {@code ServletContext}
 */
public class BundleManagerFactory {
  
  private static final String DEFAULT_CONFIG_XML_PATH = "/WEB-INF/uiResourceConfig.xml";
  
  /**
   * The base directory.
   */
  private String _baseDir;
  /**
   * The config xml path
   */
  private String _configXmlPath;
  /**
   * The manager created from this factory.
   */
  private volatile BundleManager _manager;
  /**
   * The uri provider.
   */
  private volatile UriProvider _uriProvider;

  /**
   * Creates an instance.
   */
  public BundleManagerFactory() {
  }

  //-------------------------------------------------------------------------
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
   * Gets the configXmlPath.
   * @return the configXmlPath
   */
  public String getConfigXmlPath() {
    return _configXmlPath;
  }

  /**
   * Sets the configXmlPath.
   * @param configXmlPath  the configXmlPath
   */
  public void setConfigXmlPath(String configXmlPath) {
    if (!StringUtils.isEmpty(configXmlPath)) {
      _configXmlPath = configXmlPath.startsWith("/") ? configXmlPath : "/" + configXmlPath;
    }
  }
  
  /**
   * Gets the uriProvider.
   * @return the uriProvider
   */
  public UriProvider getUriProvider() {
    return _uriProvider;
  }

  /**
   * Sets the uriProvider.
   * @param uriProvider  the uriProvider
   */
  public void setUriProvider(UriProvider uriProvider) {
    _uriProvider = uriProvider;
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
    InputStream uiResource = null;
    try {
      ServletContextUriProvider uriProvider = new ServletContextUriProvider(getBaseDir(), servletContext);
      setUriProvider(uriProvider);
      uiResource = getXMLStream(servletContext);
      BundleParser parser = new BundleParser(uriProvider, getBaseDir());
      return parser.parse(uiResource);
    } finally {
      IOUtils.closeQuietly(uiResource);
    }
  }

  /**
   * Resolves the config file.
   * @param servletContext 
   * 
   * @return the resolved file
   */
  private InputStream getXMLStream(ServletContext servletContext) {
    String configXMlPath = _configXmlPath == null ? DEFAULT_CONFIG_XML_PATH : _configXmlPath;
    InputStream result = servletContext.getResourceAsStream(configXMlPath);
    if (result == null) {
      throw new IllegalStateException("Cannot find resource XML in " + configXMlPath);
    }
    return result;
  }

}

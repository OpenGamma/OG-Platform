/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.Iterables;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful bundle resources.
 */
public abstract class AbstractWebBundleResource
    extends AbstractPerRequestWebResource<WebBundlesData> {

  /**
   * Creates the resource.
   * 
   * @param bundleManagerFactory  the bundle manager, not null
   * @param compressor  the bundle compressor, not null
   * @param mode  the deploy mode, not null
   */
  protected AbstractWebBundleResource(
      final BundleManagerFactory bundleManagerFactory, final BundleCompressor compressor, final DeployMode mode) {
    super(new WebBundlesData());
    ArgumentChecker.notNull(bundleManagerFactory, "bundleManagerFactory");
    ArgumentChecker.notNull(compressor, "compressedBundleSource");
    ArgumentChecker.notNull(mode, "mode");
    
    data().setBundleManagerFactory(bundleManagerFactory);
    data().setCompressor(compressor);
    data().setMode(mode);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebBundleResource(final AbstractWebBundleResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @Override
  @Context
  public void setServletContext(ServletContext servletContext) {
    super.setServletContext(servletContext);
    
    // initialise the manager now that we have the servlet context
    BundleManager bundleManager = data().getBundleManagerFactory().get(servletContext);
    data().setBundleManager(bundleManager);
    data().setDevBundleManager(new DevBundleBuilder(bundleManager).getDevBundleManager());
  }

  @Context
  public void setHttpHeaders(HttpHeaders httpHeaders) {
    data().setHttpHeaders(httpHeaders);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("ogStyle", new StyleTag(data()));
    out.put("ogScript", new ScriptTag(data()));
    HttpHeaders httpHeaders = data().getHttpHeaders();
    String openfin = "";
    if (httpHeaders != null) {
      out.put("httpHeaders", data().getHttpHeaders());
      List<String> openfinHeader = httpHeaders.getRequestHeader("x-powered-by");
      if (openfinHeader != null) {
        openfin = Iterables.getFirst(openfinHeader, "");
      }
    }
    out.put("openfin", openfin.toLowerCase());
    return out;
  }

}

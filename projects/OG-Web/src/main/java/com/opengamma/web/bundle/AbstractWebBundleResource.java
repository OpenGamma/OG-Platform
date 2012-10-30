/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful bundle resources.
 */
public abstract class AbstractWebBundleResource extends AbstractPerRequestWebResource {

  /**
   * The backing bean.
   */
  private final WebBundlesData _data;

  /**
   * Creates the resource.
   * 
   * @param bundleManagerFactory  the bundle manager, not null
   * @param compressor  the bundle compressor, not null
   * @param mode  the deploy mode, not null
   */
  protected AbstractWebBundleResource(
      final BundleManagerFactory bundleManagerFactory, final BundleCompressor compressor, final DeployMode mode) {
    ArgumentChecker.notNull(bundleManagerFactory, "bundleManagerFactory");
    ArgumentChecker.notNull(compressor, "compressedBundleSource");
    ArgumentChecker.notNull(mode, "mode");
    _data = new WebBundlesData();
    
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
    _data = parent.data();
  }

  //-------------------------------------------------------------------------
  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }

  @Override
  @Context
  public void setServletContext(ServletContext servletContext) {
    super.setServletContext(servletContext);
    
    // initialise the manager now that we have the servlet context
    BundleManager bundleManager = _data.getBundleManagerFactory().get(servletContext);
    data().setBundleManager(bundleManager);
    data().setDevBundleManager(new DevBundleBuilder(bundleManager).getDevBundleManager());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("ogStyle", new StyleTag(data()));
    out.put("ogScript", new ScriptTag(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * 
   * @return the backing bean, not null
   */
  protected WebBundlesData data() {
    return _data;
  }

}

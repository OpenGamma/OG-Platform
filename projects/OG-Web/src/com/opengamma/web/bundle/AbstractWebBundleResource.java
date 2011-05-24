/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;

/**
 * Abstract base class for RESTful bundle resources.
 */
public class AbstractWebBundleResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebBundlesData _data;

  /**
   * Creates the resource.
   * 
   * @param bundleManager  the bundle manager, not null
   * @param compressor  the bundle compressor, not null
   * @param mode  the deploy mode, not null
   */
  protected AbstractWebBundleResource(
      final BundleManager bundleManager, final BundleCompressor compressor, final DeployMode mode) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    ArgumentChecker.notNull(compressor, "compressedBundleSource");
    ArgumentChecker.notNull(mode, "mode");
    _data = new WebBundlesData();
    data().setBundleManager(bundleManager);
    data().setDevBundleManager(new DevBundleBuilder(bundleManager).getDevBundleManager());
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
    _data = parent._data;
  }

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

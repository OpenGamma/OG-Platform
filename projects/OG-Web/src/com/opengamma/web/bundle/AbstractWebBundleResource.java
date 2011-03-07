/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
  
  protected AbstractWebBundleResource(final BundleManager bundleManager, final CompressedBundleSource compressedBundleSource, final DeployMode mode) {
    ArgumentChecker.notNull(bundleManager, "bundleManager");
    ArgumentChecker.notNull(compressedBundleSource, "compressedBundleSource");
    ArgumentChecker.notNull(mode, "mode");
    _data = new WebBundlesData();
    data().setBundleManager(bundleManager);
    data().setCompressedBundleSource(compressedBundleSource);
    data().setMode(mode);
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
  
  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebBundleResource(final AbstractWebBundleResource parent) {
    super(parent);
    _data = parent._data;
  }
  
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    
    BundleManager bundleManager = data().getBundleManager();
    DeployMode mode = data().getMode();
    
    WebBundlesUris webBundlesUris = new WebBundlesUris(data());
        
    ScriptTag scriptTag = new ScriptTag(bundleManager, mode, webBundlesUris);
    StyleTag styleTag = new StyleTag(bundleManager, mode, webBundlesUris);
    out.put("ogStyle", styleTag);
    out.put("ogScript", scriptTag);
    return out;
  }
  
  /**
   * Gets the backing bean.
   * @return the beacking bean, not null
   */
  protected WebBundlesData data() {
    return _data;
  }

}

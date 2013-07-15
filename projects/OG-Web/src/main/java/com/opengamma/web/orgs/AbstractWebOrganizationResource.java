/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.orgs;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.orgs.OrganizationMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful organization resources.
 */
public abstract class AbstractWebOrganizationResource extends AbstractPerRequestWebResource {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebOrganizationResource.class);
  
  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "organizations/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "organizations/json/";

  /**
   * The backing bean.
   */
  private final WebOrganizationsData _data;

  /**
   * Creates the resource.
   * @param organizationMaster  the organization master, not null
   */
  protected AbstractWebOrganizationResource(final OrganizationMaster organizationMaster) {
    ArgumentChecker.notNull(organizationMaster, "organizationMaster");
    _data = new WebOrganizationsData();
    data().setOrganizationMaster(organizationMaster);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebOrganizationResource(final AbstractWebOrganizationResource parent) {
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
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebOrganizationsUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the backing bean.
   * @return the backing bean, not null
   */
  protected WebOrganizationsData data() {
    return _data;
  }
     
}

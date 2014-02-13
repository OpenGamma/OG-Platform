/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/** Abstract base class for RESTful legalEntity resources. */
public abstract class AbstractWebLegalEntityResource extends AbstractPerRequestWebResource {

  /** HTML ftl directory */
  protected static final String HTML_DIR = "legalentities/html/";
  /** JSON ftl directory */
  protected static final String JSON_DIR = "legalentities/json/";

  /** The backing bean. */
  private final WebLegalEntityData _data;

  /**
   * Creates the resource.
   *
   * @param legalEntityMaster the legalEntity master, not null
   * @param securityMaster the securityMaster master, not null
   */
  protected AbstractWebLegalEntityResource(final LegalEntityMaster legalEntityMaster, final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(legalEntityMaster, "legalEntityMaster");
    _data = new WebLegalEntityData();
    data().setLegalEntityMaster(legalEntityMaster);
    data().setSecurityMaster(securityMaster);
    initializeMetaData();
  }

  //init meta-data
  private void initializeMetaData() {
  }

  /**
   * Creates the resource.
   *
   * @param parent the parent resource, not null
   */
  protected AbstractWebLegalEntityResource(final AbstractWebLegalEntityResource parent) {
    super(parent);
    _data = parent._data;
  }

  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   *
   * @param uriInfo the URI info, not null
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
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebLegalEntityUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the backing bean.
   *
   * @return the backing bean, not null
   */
  protected WebLegalEntityData data() {
    return _data;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful legalEntity resources.
 */
public abstract class AbstractWebLegalEntityResource
    extends AbstractPerRequestWebResource<WebLegalEntityData> {

  /** HTML ftl directory */
  protected static final String HTML_DIR = "legalentities/html/";
  /** JSON ftl directory */
  protected static final String JSON_DIR = "legalentities/json/";

  /**
   * Creates the resource.
   * 
   * @param legalEntityMaster  the legalEntity master, not null
   * @param securityMaster  the securityMaster master, not null
   */
  protected AbstractWebLegalEntityResource(final LegalEntityMaster legalEntityMaster, final SecurityMaster securityMaster) {
    super(new WebLegalEntityData());
    ArgumentChecker.notNull(legalEntityMaster, "legalEntityMaster");
    data().setLegalEntityMaster(legalEntityMaster);
    data().setSecurityMaster(securityMaster);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebLegalEntityResource(final AbstractWebLegalEntityResource parent) {
    super(parent);
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
    out.put("uris", new WebLegalEntityUris(data()));
    return out;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.user.UserMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful role resources.
 */
public abstract class AbstractWebRoleResource
    extends AbstractPerRequestWebResource<WebRoleData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "users/html/";

  /**
   * Creates the resource.
   * 
   * @param userMaster  the role master, not null
   */
  protected AbstractWebRoleResource(final UserMaster userMaster) {
    super(new WebRoleData());
    ArgumentChecker.notNull(userMaster, "userMaster");
    data().setUserMaster(userMaster);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebRoleResource(final AbstractWebRoleResource parent) {
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
    out.put("uris", new WebRoleUris(data()));
    return out;
  }

}

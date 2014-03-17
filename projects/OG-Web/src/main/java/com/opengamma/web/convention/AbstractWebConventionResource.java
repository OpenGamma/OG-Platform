/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

import java.util.Map.Entry;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful convention resources.
 */
public abstract class AbstractWebConventionResource
    extends AbstractPerRequestWebResource<WebConventionData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "conventions/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "conventions/json/";

  /**
   * The Convention types provider
   */
  private final ConventionTypesProvider _conventionTypesProvider = ConventionTypesProvider.getInstance();

  /**
   * Creates the resource.
   * 
   * @param conventionMaster  the convention master, not null
   */
  protected AbstractWebConventionResource(final ConventionMaster conventionMaster) {
    super(new WebConventionData());
    ArgumentChecker.notNull(conventionMaster, "conventionMaster");
    data().setConventionMaster(conventionMaster);
    initializeMetaData();
  }

  //init meta-data
  private void initializeMetaData() {
    for (Entry<String, Class<? extends ManageableConvention>> entry : _conventionTypesProvider.getTypeMap().entrySet()) {
      data().getTypeMap().put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebConventionResource(final AbstractWebConventionResource parent) {
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
    out.put("uris", new WebConventionUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the convention types provider.
   * 
   * @return the convention types provider
   */
  public ConventionTypesProvider getConventionTypesProvider() {
    return _conventionTypesProvider;
  }

}

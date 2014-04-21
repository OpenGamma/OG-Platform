/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

import java.net.URI;

import com.opengamma.id.UniqueId;

/** URIs for web-based legalEntity management. */
public class WebLegalEntityUris {

  /** The data. */
  private final WebLegalEntityData _data;

  /**
   * Creates an instance.
   *
   * @param data the web data, not null
   */
  public WebLegalEntityUris(WebLegalEntityData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the base URI.
   *
   * @return the URI
   */
  public URI base() {
    return legalEntities();
  }

  /**
   * Gets the URI.
   *
   * @return the URI
   */
  public URI legalEntities() {
    return WebLegalEntitiesResource.uri(_data);
  }

  /**
   * Gets the URI.
   *
   * @return the URI
   */
  public URI legalEntity() {
    return WebLegalEntityResource.uri(_data);
  }

  /**
   * Gets the URI.
   *
   * @param legalEntityId the legalEntity id, not null
   * @return the URI
   */
  public URI legalEntity(final UniqueId legalEntityId) {
    return WebLegalEntityResource.uri(_data, legalEntityId);
  }

  /**
   * Gets the URI.
   *
   * @return the URI
   */
  public URI legalEntityVersions() {
    return WebLegalEntityVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   *
   * @return the URI
   */
  public URI legalEntityVersion() {
    return WebLegalEntityVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   *
   * @param legalEntityId the legalEntity id, not null
   * @return the URI
   */
  public URI legalEntityVersion(final UniqueId legalEntityId) {
    return WebLegalEntityVersionResource.uri(_data, legalEntityId);
  }

}

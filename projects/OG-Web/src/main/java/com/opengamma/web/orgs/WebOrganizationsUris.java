/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.orgs;

import java.net.URI;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * URIs for web-based organizations.
 */
public class WebOrganizationsUris {

  /**
   * The data.
   */
  private final WebOrganizationsData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebOrganizationsUris(WebOrganizationsData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI organizations() {
    return WebOrganizationsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param identifier  the identifier to search for, may be null
   * @return the URI
   */
  public URI organizations(final ExternalId identifier) {
    return WebOrganizationsResource.uri(_data, ExternalIdBundle.of(identifier));
  }

  /**
   * Gets the URI.
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI
   */
  public URI organizations(final ExternalIdBundle identifiers) {
    return WebOrganizationsResource.uri(_data, identifiers);
  }

  /**
   * Gets the URI, returning a security serach or single security.
   * @param link  the link to search for, may be null
   * @return the URI
   */
  public URI organizations(final SecurityLink link) {
    if (link.getObjectId() != null) {
      return WebOrganizationResource.uri(_data, link.getObjectId().atLatestVersion());
    }
    return WebOrganizationsResource.uri(_data, link.getExternalId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI security() {
    return WebOrganizationResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param security  the security, not null
   * @return the URI
   */
  public URI security(final Security security) {
    return WebOrganizationResource.uri(_data, security.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securityVersions() {
    return WebOrganizationVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securityVersion() {
    return WebOrganizationVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param security  the security, not null
   * @return the URI
   */
  public URI securityVersion(final Security security) {
    return WebOrganizationVersionResource.uri(_data, security.getUniqueId());
  }

}

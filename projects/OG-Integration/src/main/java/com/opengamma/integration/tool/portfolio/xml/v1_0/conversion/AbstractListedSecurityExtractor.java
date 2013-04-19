/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ListedSecurityDefinition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * An extractor of listed secuirties.
 * 
 * @param <T> the type of security
 */
public abstract class AbstractListedSecurityExtractor<T extends ListedSecurityDefinition>
    implements ListedSecurityExtractor {

  /**
   * The security definition.
   */
  private final T _securityDefinition;

  /**
   * Creates an instance.
   * 
   * @param securityDefinition  the security definition, not null
   */
  public AbstractListedSecurityExtractor(T securityDefinition) {
    _securityDefinition = securityDefinition;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security definition.
   * 
   * @return the definition, not null
   */
  protected T getSecurityDefinition() {
    return _securityDefinition;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extract() {
    ManageableSecurity security = createSecurity();
    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(security)
            .toHashCode())));
    security.setAttributes(getSecurityDefinition().getAdditionalAttributes());
    return new ManageableSecurity[]{security};
  }

  /**
   * Creates a security from the definition.
   * 
   * @return the security, not null
   */
  protected abstract ManageableSecurity createSecurity();

}

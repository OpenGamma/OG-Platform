/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;

public abstract class AbstractListedSecurityExtractor<T extends ListedSecurityDefinition> implements ListedSecurityExtractor {

  protected final T _securityDefinition;

  public AbstractListedSecurityExtractor(T securityDefinition) {
    _securityDefinition = securityDefinition;
  }

  @Override
  public ManageableSecurity[] extract() {

    ManageableSecurity security = createSecurity();

    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(security)
            .toHashCode())));

    security.setAttributes(_securityDefinition.getAdditionalAttributes());

    return new ManageableSecurity[]{security};
  }

  protected abstract ManageableSecurity createSecurity();
}
/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/* package */class MockSecurity implements Security, Serializable {

  private static final long serialVersionUID = 1L;

  private final UniqueId _uid;
  private final String _name;
  private final ExternalIdBundle _identifiers;

  public MockSecurity(final int id) {
    _uid = UniqueId.of("Security", Integer.toString(id));
    _name = "Security " + id;
    _identifiers = ExternalIdBundle.of(ExternalId.of("Ticker", Integer.toString(id)));
  }

  @Override
  public Map<String, String> getAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addAttribute(String key, String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UniqueId getUniqueId() {
    return _uid;
  }

  @Override
  public ExternalIdBundle getExternalIdBundle() {
    return _identifiers;
  }

  @Override
  public String getSecurityType() {
    return "MOCK";
  }

  @Override
  public String getName() {
    return _name;
  }

};

/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of a source of securities.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockSecuritySource implements SecuritySource {
  // this is currently public for indirect use by another project via ViewTestUtils

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "Mock";
  /**
   * The securities keyed by identifier.
   */
  private final Map<UniqueIdentifier, Security> _securities = new HashMap<UniqueIdentifier, Security>();
  /**
   * The next index for the identifier.
   */
  private final AtomicLong _nextIdentifier = new AtomicLong();

  /**
   * Creates the security master.
   */
  public MockSecuritySource() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier identifier) {
    return identifier == null ? null : _securities.get(identifier);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    List<Security> result = new ArrayList<Security>();
    for (Security sec : _securities.values()) {
      if (sec.getIdentifiers().containsAny(securityKey)) {
        result.add(sec);
      }
    }
    return result;
  }

  @Override
  public Security getSecurity(IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    for (Identifier secId : securityKey.getIdentifiers()) {
      for (Security sec : _securities.values()) {
        if (sec.getIdentifiers().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security to the master.
   * @param security  the security to add, not null
   */
  public void addSecurity(DefaultSecurity security) {
    ArgumentChecker.notNull(security, "security");
    UniqueIdentifier identifier = UniqueIdentifier.of(DEFAULT_UID_SCHEME, Long.toString(_nextIdentifier.incrementAndGet()));
    security.setUniqueIdentifier(identifier);
    _securities.put(security.getUniqueIdentifier(), security);
  }

}

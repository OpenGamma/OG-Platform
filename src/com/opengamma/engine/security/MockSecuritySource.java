/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A simple purely in-memory implementation of the security master.
 * <p>
 * This class is primarily useful in testing scenarios, or when operating
 * as a cache on top of a slower {@link SecuritySource} implementation.
 */
public class MockSecuritySource implements SecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MockSecuritySource.class);

  /**
   * The securities keyed by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, Security> _securities = new ConcurrentHashMap<UniqueIdentifier, Security>();
  /**
   * The next index for the identifier.
   */
  private final AtomicLong _nextIdentityKey = new AtomicLong();

  /**
   * Creates the security master.
   */
  public MockSecuritySource() {
  }

  /**
   * Creates the security master from a collection of securities.
   * @param securities  the securities to start with, null ignored
   */
  public MockSecuritySource(Collection<? extends DefaultSecurity> securities) {
    if (securities != null) {
      for (DefaultSecurity sec : securities) {
        addSecurity(sec);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a specific security by identifier.
   * @param identifier  the identifier, null returns null
   * @return the security, null if not found
   */
  public Security getSecurity(UniqueIdentifier identifier) {
    return identifier == null ? null : _securities.get(identifier);
  }

  /**
   * Finds all securities that match the specified bundle of keys.
   * If there are none specified, this method must return an
   * empty collection, and not {@code null}.
   * @param securityKey  the bundle keys to match, not null
   * @return all securities matching the specified key, empty if no matches, not null
   */
  public Collection<Security> getSecurities(IdentifierBundle securityKey) {
    List<Security> result = new ArrayList<Security>();
    if (securityKey != null) {
      for (Security sec : _securities.values()) {
        if (sec.getIdentifiers().containsAny(securityKey)) {
          result.add(sec);
        }
      }
    }
    return result;
  }

  /**
   * Finds the single best-fit security that matches the specified bundle of keys.
   * <p>
   * This implementation loops through each identifier in the supplied bundle
   * and searches for a matching security returning the first match.
   * @param securityKey  the bundle keys to match, not null
   * @return the single security matching the bundle of keys, null if not found
   */
  public Security getSecurity(IdentifierBundle securityKey) {
    if (securityKey != null) {
      for (Identifier secId : securityKey.getIdentifiers()) {
        for (Security sec : _securities.values()) {
          if (sec.getIdentifiers().contains(secId)) {
            return sec;
          }
        }
      }
    }
    return null;
  }

  /**
   * Obtain all the available security types in this security master.
   * <p>
   * The implementation should return the available types, however if this is
   * not possible it may return all potential types.
   * @return the set of available security types, not null
   */
  public Set<String> getAllSecurityTypes() {
    Set<String> result = new TreeSet<String>();
    for (Security security : _securities.values()) {
      String securityType = security.getSecurityType();
      if (securityType != null) {
        result.add(securityType);
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security to the master.
   * @param security  the security to add, not null
   */
  public void addSecurity(DefaultSecurity security) {
    if (security.getSecurityType() == null) {
      s_logger.warn("Security {} lacks a security type", security);
    }
    UniqueIdentifier identifier = UniqueIdentifier.of("Memory", Long.toString(_nextIdentityKey.incrementAndGet()));
    security.setUniqueIdentifier(identifier);
    _securities.putIfAbsent(security.getUniqueIdentifier(), security);
  }

}

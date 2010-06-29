/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.InstantProvider;

import org.apache.commons.lang.Validate;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.AddSecurityRequest;
import com.opengamma.financial.security.ManagableSecurityMaster;
import com.opengamma.financial.security.UpdateSecurityRequest;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierTemplate;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, in-memory implementation of {@code ManagableSecurityMaster}.
 * This implementation does not support versioning or resurrection of securities.
 */
public class InMemorySecurityMaster implements ManagableSecurityMaster {

  /**
   * The default scheme used for any {@link UniqueIdentifier}s created by this {@link PositionMaster}.
   */
  public static final String DEFAULT_UID_SCHEME = "Memory";
  /**
   * A cache of securities by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, Security> _securities = new ConcurrentHashMap<UniqueIdentifier, Security>();
  /**
   * The next index for the identifier.
   */
  private final AtomicLong _nextIdentityKey = new AtomicLong();
  /**
   * The template to use for {@link UniqueIdentifier} generation and parsing.
   */
  private final UniqueIdentifierTemplate _uidTemplate;

  /**
   * Creates an empty security master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemorySecurityMaster() {
    this(new UniqueIdentifierTemplate(DEFAULT_UID_SCHEME));
  }
  
  /**
   * Creates an empty security master using the specified template for any {@link UniqueIdentifier}s created.
   * 
   * @param uidTemplate  the template to use for any {@link UniqueIdentifier}s created, not null
   */
  public InMemorySecurityMaster(UniqueIdentifierTemplate uidTemplate) {
    ArgumentChecker.notNull(uidTemplate, "uidTemplate");
    _uidTemplate = uidTemplate;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isManagerFor(UniqueIdentifier uid) {
    Validate.notNull(uid, "UniqueIdentifier must not be null");
    return _uidTemplate.conforms(uid);
  }

  @Override
  public boolean isModificationSupported() {
    return true;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    Validate.notNull(uid, "UniqueIdentifier must not be null");
    return _securities.get(uid);
  }

  @Override
  public Security getSecurity(UniqueIdentifier uid, InstantProvider asAt, InstantProvider asViewedAt) {
    Validate.notNull(uid, "UniqueIdentifier must not be null");
    Validate.notNull(asAt, "Instant asAt must not be null");
    return _securities.get(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    Validate.notNull(secKey, "IdentifierBundle must not be null");
    Set<Security> result = new HashSet<Security>();
    for (Security security : _securities.values()) {
      if (security.getIdentifiers().containsAny(secKey)) {
        result.add(security);
      }
    }
    return result;
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    Validate.notNull(secKey, "IdentifierBundle must not be null");
    for (Identifier secId : secKey.getIdentifiers()) {
      for (Security sec : _securities.values()) {
        if (sec.getIdentifiers().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    Set<String> result = new HashSet<String>();
    for (Security security : _securities.values()) {
      result.add(security.getSecurityType());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier addSecurity(AddSecurityRequest request) {
    Validate.notNull(request, "AddSecurityRequest must not be null");
    request.checkValid();
    
    final UniqueIdentifier uid = getNextSecurityUid();
    _securities.put(uid, request.getSecurity());
    return uid;
  }

  @Override
  public UniqueIdentifier updateSecurity(UpdateSecurityRequest request) {
    Validate.notNull(request, "AddSecurityRequest must not be null");
    request.checkValid();
    
    final UniqueIdentifier uid = request.getUniqueIdentifier();
    if (_securities.replace(uid, request.getSecurity()) == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    return uid;
  }

  @Override
  public UniqueIdentifier removeSecurity(UniqueIdentifier uid) {
    Validate.notNull(uid, "UniqueIdentifier must not be null");
    
    if (_securities.remove(uid) == null) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    return uid;
  }

  //-------------------------------------------------------------------------
  private long getNextId() {
    return _nextIdentityKey.incrementAndGet();
  }

  private UniqueIdentifier getNextSecurityUid() {
    return _uidTemplate.uid(Long.toString(getNextId()));
  }

}

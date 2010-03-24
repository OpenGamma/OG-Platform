/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;


/**
 * A simple purely in-memory implementation of the {@link SecurityMaster}
 * interface.
 * This class is primarily useful in testing scenarios, or when operating
 * as a cache on top of a slower {@link SecurityMaster} implementation.
 * <p/>
 * The lookup strategy followed is based on a logical OR of the identifiers
 * in the key (e.g. any of them can produce a match). The specific
 * lookup algorithm is:
 * <ol>
 *   <li>Look at each {@link DomainSpecificIdentifier} obtained from the
 *       {@link SecurityKey} in turn; for each:</li>
 *   <li>Look at the {@link Security} instances added to this instance,
 *       <em>in the order in which they were added</em>, to determine
 *       if there is a match for that {@link DomainSpecificIdentifier}.</li>
 *   <li>If there is a match for that identifier, match found.</li>
 *   <li>For single {@link Security} lookup, if a match found, return it.</li>
 *   <li>For multiple {@link Security} lookup, identify all matches for all identifiers.</li> 
 * </ol>
 *
 * @author kirk
 */
public class InMemorySecurityMaster implements SecurityMaster {
  private static final Logger s_logger = LoggerFactory.getLogger(InMemorySecurityMaster.class);
  // REVIEW kirk 2009-09-01 -- This is grotesquely unoptimized. Areas that
  // it can be improved:
  // 1 - Tighten down the synchronization dramatically
  // 2 - Cache (using putIfAbsent on a series of ConcurrentHashMaps) the
  //     mapping from SecurityIdentifier to Security.
  private final AtomicLong _nextIdentityKey = new AtomicLong(1l);
  private final List<Security> _securities = new ArrayList<Security>();
  private final Map<DomainSpecificIdentifier, Security> _securitiesByIdentityKey =
    new HashMap<DomainSpecificIdentifier, Security>();
  
  public InMemorySecurityMaster() {
  }
  
  public InMemorySecurityMaster(Collection<? extends DefaultSecurity> securities) {
    if(securities != null) {
      for(DefaultSecurity sec : securities) {
        add(sec);
      }
    }
  }
  
  public synchronized void add(DefaultSecurity security) {
    if(security.getSecurityType() == null) {
      s_logger.warn("Security {} lacks a security type.", security);
    }
    _securities.add(security);
    String identityKey = Long.toString(_nextIdentityKey.getAndAdd(1l));
    security.setIdentityKey(identityKey);
    _securitiesByIdentityKey.put(security.getIdentityKey(), security);
  }

  @Override
  public synchronized Collection<Security> getSecurities(DomainSpecificIdentifiers secKey) {
    List<Security> result = new ArrayList<Security>();
    if(secKey == null) {
      return result;
    }
    for(DomainSpecificIdentifier secId : secKey.getIdentifiers()) {
      for(Security sec : _securities) {
        if(sec.getIdentifiers().contains(secId)) {
          result.add(sec);
        }
      }
    }
    return result;
  }

  @Override
  public synchronized Security getSecurity(DomainSpecificIdentifiers secKey) {
    if(secKey == null) {
      return null;
    }
    for(DomainSpecificIdentifier secId : secKey.getIdentifiers()) {
      for(Security sec : _securities) {
        if(sec.getIdentifiers().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    Set<String> result = new TreeSet<String>();
    for(Security security : _securities) {
      String secType = security.getSecurityType();
      if(secType != null) {
        result.add(secType);
      }
    }
    return result;
  }

  @Override
  public Security getSecurity(DomainSpecificIdentifier identityKey) {
    return _securitiesByIdentityKey.get(identityKey);
  }
  
}

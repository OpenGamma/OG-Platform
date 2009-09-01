/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
 *   <li>Look at each {@link SecurityIdentifier} obtained from the
 *       {@link SecurityKey} in turn; for each:</li>
 *   <li>Look at the {@link Security} instances added to this instance,
 *       <em>in the order in which they were added</em>, to determine
 *       if there is a match for that {@link SecurityIdentifier}.</li>
 *   <li>If there is a match for that identifier, match found.</li>
 *   <li>For single {@link Security} lookup, if a match found, return it.</li>
 *   <li>For multiple {@link Security} lookup, identify all matches for all identifiers.</li> 
 * </ol>
 *
 * @author kirk
 */
public class InMemorySecurityMaster implements SecurityMaster {
  // REVIEW kirk 2009-09-01 -- This is grotesquely unoptimized. Areas that
  // it can be improved:
  // 1 - Tighten down the synchronization dramatically
  // 2 - Cache (using putIfAbsent on a series of ConcurrentHashMaps) the
  //     mapping from SecurityIdentifier to Security.
  private final List<Security> _securities = new ArrayList<Security>();
  
  public InMemorySecurityMaster() {
  }
  
  public InMemorySecurityMaster(Collection<? extends Security> securities) {
    if(securities != null) {
      for(Security sec : securities) {
        add(sec);
      }
    }
  }
  
  public synchronized void add(Security security) {
    _securities.add(security);
  }

  @Override
  public synchronized Collection<Security> getSecurities(SecurityKey secKey) {
    List<Security> result = new ArrayList<Security>();
    if(secKey == null) {
      return result;
    }
    for(SecurityIdentifier secId : secKey.getIdentifiers()) {
      for(Security sec : _securities) {
        if(sec.getIdentifiers().contains(secId)) {
          result.add(sec);
        }
      }
    }
    return result;
  }

  @Override
  public synchronized Security getSecurity(SecurityKey secKey) {
    if(secKey == null) {
      return null;
    }
    for(SecurityIdentifier secId : secKey.getIdentifiers()) {
      for(Security sec : _securities) {
        if(sec.getIdentifiers().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }

}

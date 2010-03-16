/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A list of persistent subscriptions. A LiveData server uses a publication list to:
 * <ul>
 * <li>Automatically start publishing on startup
 * <li>Keep publishing no matter what the heartbeat messages say
 * </ul> 
 *
 * @author pietari
 */
public class PersistentSubscriptionList {
  
  private Set<DomainSpecificIdentifier> _entries;
  
  public PersistentSubscriptionList(Set<DomainSpecificIdentifier> entries) {
    ArgumentChecker.checkNotNull(entries, "Publication list contents");
    _entries = entries;        
  }

  public Set<DomainSpecificIdentifier> getEntries() {
    return _entries;
  }

  public void setEntries(Set<DomainSpecificIdentifier> entries) {
    _entries = entries;
  }
  
}

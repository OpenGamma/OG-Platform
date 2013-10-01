/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A decorator <code>AuditLogger</code> that only sends the message
 * onwards to its delegate if the message is not a duplicate (within
 * a given time period).
 * <p>
 * This implementation is thread-safe.
 */
public class DuplicateFilteringAuditLogger extends AbstractAuditLogger {

  private final AbstractAuditLogger _delegate;
  private final Cache _cache;

  public DuplicateFilteringAuditLogger(AbstractAuditLogger delegate,
      int maxElementsInMemory, int secondsToKeepInMemory) {
    ArgumentChecker.notNull(delegate, "Delegate logger");

    _delegate = delegate;
    _cache = new Cache("audit_log_entry_cache", maxElementsInMemory, false,
        false, secondsToKeepInMemory, secondsToKeepInMemory);
    _cache.setCacheManager(EHCacheUtils.createCacheManager());
    _cache.initialise();
  }

  @Override
  public synchronized void log(String user, String originatingSystem,
      String object, String operation, String description, boolean success) {

    CacheKey key = new CacheKey(user, originatingSystem, object, operation, description, success);

    if (_cache.get(key) == null) {
      Element element = new Element(key, new Object());
      _cache.put(element);

      _delegate.log(user, object, operation, description, success);
    }
  }
  
  private static class CacheKey {
    private final String _user;
    private final String _originatingSystem;
    private final String _object;
    private final String _operation;
    private final String _description;
    private final boolean _success;
    
    CacheKey(String user, String originatingSystem,
        String object, String operation, String description, boolean success) {
      _user = user;
      _originatingSystem = originatingSystem;
      _object = object;
      _operation = operation;
      _description = description;
      _success = success;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((_description == null) ? 0 : _description.hashCode());
      result = prime * result + ((_object == null) ? 0 : _object.hashCode());
      result = prime * result
          + ((_operation == null) ? 0 : _operation.hashCode());
      result = prime * result
          + ((_originatingSystem == null) ? 0 : _originatingSystem.hashCode());
      result = prime * result + (_success ? 1231 : 1237);
      result = prime * result + ((_user == null) ? 0 : _user.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      CacheKey other = (CacheKey) obj;
      if (_description == null) {
        if (other._description != null) {
          return false;
        }
      } else if (!_description.equals(other._description)) {
        return false;
      }
      if (_object == null) {
        if (other._object != null) {
          return false;
        }
      } else if (!_object.equals(other._object)) {
        return false;
      }
      if (_operation == null) {
        if (other._operation != null) {
          return false;
        }
      } else if (!_operation.equals(other._operation)) {
        return false;
      }
      if (_originatingSystem == null) {
        if (other._originatingSystem != null) {
          return false;
        }
      } else if (!_originatingSystem.equals(other._originatingSystem)) {
        return false;
      }
      if (_success != other._success) {
        return false;
      }
      if (_user == null) {
        if (other._user != null) {
          return false;
        }
      } else if (!_user.equals(other._user)) {
        return false;
      }
      return true;
    }
  }
}

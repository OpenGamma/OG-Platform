/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.cache.AbstractEHCachingMaster;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserHistoryRequest;
import com.opengamma.master.user.UserHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code UserMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a {@code DbUserMaster}. In particular, prefetching is employed in paged queries,
 * which tend to scale poorly.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingUserMaster extends AbstractEHCachingMaster<UserDocument> implements UserMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingUserMaster.class);

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   *
   * @param name          the cache name, not empty
   * @param underlying    the underlying user source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingUserMaster(final String name, final UserMaster underlying, final CacheManager cacheManager) {
    super(name, underlying, cacheManager);
  }

  @Override
  public UserSearchResult search(UserSearchRequest request) {
    return ((UserMaster) getUnderlying()).search(request); //TODO
  }

  @Override
  public UserHistoryResult history(UserHistoryRequest request) {
    return ((UserMaster) getUnderlying()).history(request); //TODO
  }

}

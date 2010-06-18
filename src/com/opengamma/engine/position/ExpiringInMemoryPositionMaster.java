/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;
import com.opengamma.util.ehcache.AbstractCacheEventListener;
import com.opengamma.util.ehcache.ExpiryThreadCacheExtension;

/**
 * A mutable {@link PositionMaster} which associates each {Portfolio} with an owner, an arbitrary object of type
 * <code>O</code>. Multiple {@link Portfolio}s may be associated with the same owner.
 * <p>
 * The {@link Portfolio} structures associated with a particular owner will eventually expire unless their lifetime is
 * periodically extended by providing a heartbeat for the owner.
 * <p>
 * For example, this could be used where external clients need to work with temporary {@link Portfolio}s. The owner
 * would identify a client, and the onus would be on the client to keep its session alive by providing heartbeats. If a
 * client dies then its associated {@link Portfolio} structures will be eventually removed automatically.
 * 
 * @param <O>  the type of the owner to be associated with each {@link Portfolio}
 */
public class ExpiringInMemoryPositionMaster<O> implements PositionMaster {
  
  /**
   * The scheme to use for {@link UniqueIdentifier}s assigned to portfolio structures which are added to this
   * {@link PositionMaster}.
   */
  public static final String UID_SCHEME = "ExpiringMemory";
  
  private static final String OWNER_CACHE = "position-master-owner-cache";
  
  private final InMemoryPositionMaster _underlying = new InMemoryPositionMaster(UID_SCHEME);
  private final Cache _expiringOwnerCache;
  
  public ExpiringInMemoryPositionMaster(long expireAfterSeconds) {       
    CacheManager cacheManager = EHCacheUtils.createCacheManager();
    EHCacheUtils.addCache(cacheManager, OWNER_CACHE, 0, MemoryStoreEvictionPolicy.LRU, false, null, false, 0, expireAfterSeconds, false, 0, null);
    _expiringOwnerCache = EHCacheUtils.getCacheFromManager(cacheManager, OWNER_CACHE);
    
    // Ehcache does not have an expiry thread for the MemoryStore, so this extension does the job. Otherwise, the cache
    // relies on accesses to evict old items. 
    ExpiryThreadCacheExtension expiryThread = new ExpiryThreadCacheExtension(_expiringOwnerCache);
    expiryThread.init();
    
    _expiringOwnerCache.getCacheEventNotificationService().registerListener(new AbstractCacheEventListener() {

      @SuppressWarnings("unchecked")
      @Override
      public void notifyElementExpired(Ehcache cache, Element element) {
        expired((O) element.getObjectKey(), (Collection<Portfolio>) element.getObjectValue());
      }
      
    });
  }
  
  //--------------------------------------------------------------------------
  // PositionMaster
  
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    return _underlying.getPortfolio(uid);
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return _underlying.getPortfolioIds();
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    return _underlying.getPortfolioNode(uid);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    return _underlying.getPosition(uid);
  }
  
  //--------------------------------------------------------------------------
  // PositionMaster mutability

  /**
   * Adds a {@link Portfolio} to the master.
   * 
   * @param owner  the owner with which to associate the portfolio. This owner must remain alive in order for the
   *               portfolio to be retained in the master.
   * @param portfolio  the portfolio to add, not null
   */
  @SuppressWarnings("unchecked")
  public void addPortfolio(O owner, Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    
    if (portfolio.getUniqueIdentifier() != null) {
      throw new OpenGammaRuntimeException("Attempted to add a portfolio which already belongs to a PositionMaster");
    }
    
    // Need to synchronize here to prevent race conditions with get/put on the cache, and to synchronize list writes if
    // multiple portfolios are being added to the same owner concurrently.
    synchronized (_expiringOwnerCache) {
      Element cacheElement = _expiringOwnerCache.get(owner);
      if (cacheElement == null) {
        cacheElement = new Element(owner, new ArrayList<Portfolio>());
        _expiringOwnerCache.put(cacheElement);
      }
      Collection<Portfolio> portfolios = (Collection<Portfolio>) cacheElement.getObjectValue();
      portfolios.add(portfolio);
      _underlying.addPortfolio(portfolio);
    }
  }
  
  //--------------------------------------------------------------------------
  // Expiry-related
  
  /**
   * Indicates that the specified owner is alive. If the owner is not known to the {@link PositionMaster} then this
   * has no effect.
   * 
   * @param owner  the owner
   * @return <code>false</code> if the specified owner is not known to the {@link PositionMaster} (or has already been
   *         expired), <code>true</code> otherwise.
   */
  public boolean heartbeat(O owner) {
    // Force the last access time to be updated
    return _expiringOwnerCache.get(owner) != null;
  }
  
  /**
   * Called when an owner has expired.
   * 
   * @param owner  the expired owner
   * @param portfolios  the list of portfolios associated with the expired owner
   */
  private void expired(O owner, Collection<Portfolio> portfolios) {
    // No need to synchronize with addPortfolio because we can't be adding one of the expired portfolios again
    // concurrently (they already have IDs) and the underlying PositionMaster is thread-safe.
    for (Portfolio portfolio : portfolios) {
      _underlying.removePortfolio(portfolio);
    }
  }
  
}

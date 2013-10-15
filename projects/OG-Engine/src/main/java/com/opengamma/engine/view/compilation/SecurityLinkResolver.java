/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.LinkUtils;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Utility to resolve security links in bulk.
 */
public final class SecurityLinkResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityLinkResolver.class);

  /**
   * The executor service.
   */
  private final ExecutorService _executorService;
  /**
   * The caching security source.
   */
  private final CachedSecuritySource _securitySource;
  /**
   * The version-correction.
   */
  private final VersionCorrection _versionCorrection;

  /**
   * Creates an instance.
   * 
   * @param executorService the threading service, not null
   * @param securitySource the security source, not null
   * @param versionCorrection the version correction, not null
   */
  public SecurityLinkResolver(final ExecutorService executorService, final SecuritySource securitySource, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(executorService, "executorService");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _executorService = executorService;
    _securitySource = new CachedSecuritySource(securitySource);
    _versionCorrection = versionCorrection;
  }

  /**
   * Creates an instance.
   * 
   * @param viewCompilationContext the context, not null
   * @param versionCorrection the version-correction, not null
   */
  public SecurityLinkResolver(final ViewCompilationContext viewCompilationContext, VersionCorrection versionCorrection) {
    this(viewCompilationContext.getServices().getExecutorService().asService(), viewCompilationContext.getServices().getFunctionCompilationContext().getSecuritySource(),
        versionCorrection);
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves security links in bulk.
   * <p>
   * Some caching of securities occurs within this instance.
   * 
   * @param securityLinks the bundles to lookup, not null
   * @throws RuntimeException if unable to resolve all the securities
   */
  @SuppressWarnings("unchecked")
  public void resolveSecurities(final Collection<SecurityLink> securityLinks) {
    ArgumentChecker.noNulls(securityLinks, "securityLinks");
    final ExecutorCompletionService<Pair<ObjectId, ExternalIdBundle>> completionService = new ExecutorCompletionService<Pair<ObjectId, ExternalIdBundle>>(_executorService);
    // Filter the links down to collections of "identical" ones; resolving the same underlying.
    final Map<Pair<ObjectId, ExternalIdBundle>, Object> securityLinkMap = new HashMap<Pair<ObjectId, ExternalIdBundle>, Object>();
    for (SecurityLink link : securityLinks) {
      final Security security = link.getTarget();
      if (security == null) {
        final Pair<ObjectId, ExternalIdBundle> key = Pairs.of(link.getObjectId(), link.getExternalId());
        if (securityLinkMap.containsKey(key)) {
          final Object sameLinkObject = securityLinkMap.get(key);
          if (sameLinkObject instanceof Collection<?>) {
            final Collection<SecurityLink> sameLinks = (Collection<SecurityLink>) sameLinkObject;
            for (SecurityLink sameLink : sameLinks) {
              if (sameLink == link) {
                link = null;
                break;
              }
            }
            if (link != null) {
              sameLinks.add(link);
            }
          } else {
            if (sameLinkObject != link) {
              final Collection<SecurityLink> sameLinks = new ArrayList<SecurityLink>();
              final SecurityLink sameLink = (SecurityLink) sameLinkObject;
              sameLinks.add(sameLink);
              sameLinks.add(link);
              securityLinkMap.put(key, sameLinks);
            }
          }
        } else {
          securityLinkMap.put(key, link);
        }
      } else if (security.getUniqueId() != null) {
        _securitySource.addToCache(security);
      }
    }
    s_logger.debug("Submitting {} resolution jobs for {} links", securityLinkMap.size(), securityLinks.size());
    // Submit a job for each "unique" link. The job will serially resolve all "identical" links as they will
    // be in the cache at that point.
    for (Map.Entry<Pair<ObjectId, ExternalIdBundle>, Object> linkEntry : securityLinkMap.entrySet()) {
      final Callable<Pair<ObjectId, ExternalIdBundle>> job;
      if (linkEntry.getValue() instanceof Collection<?>) {
        job = new MultipleSecurityResolutionJob(linkEntry.getKey(), (Collection<SecurityLink>) linkEntry.getValue(), _securitySource, _versionCorrection);
      } else {
        job = new SingleSecurityResolutionJob(linkEntry.getKey(), (SecurityLink) linkEntry.getValue(), _securitySource, _versionCorrection);
      }
      linkEntry.setValue(completionService.submit(job));
    }
    // Wait for the jobs to complete.
    while (!securityLinkMap.isEmpty()) {
      try {
        final Future<Pair<ObjectId, ExternalIdBundle>> future = completionService.take();
        final Pair<ObjectId, ExternalIdBundle> key = future.get();
        if (securityLinkMap.remove(key) == null) {
          s_logger.warn("Completion key {} wasn't in the job map {}", key, securityLinkMap);
          throw new OpenGammaRuntimeException("Internal error resolving securities");
        }
      } catch (InterruptedException ex) {
        Thread.interrupted();
        s_logger.warn("Interrupted, so didn't finish resolution");
        break;
      } catch (Exception ex) {
        s_logger.warn("Unable to resolve security", ex);
        break;
      }
    }
    if (!securityLinkMap.isEmpty()) {
      for (Object future : securityLinkMap.values()) {
        ((Future<SecurityLink>) future).cancel(false);
      }
      throw new OpenGammaRuntimeException("Unable to resolve all securities. Missing: " + securityLinkMap);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves a security link making use of the caching of this instance.
   * <p>
   * Underlying securities are not resolved.
   * 
   * @param link the link to resolve, not null
   * @return the resolved security, not null
   * @throws RuntimeException if unable to resolve the security
   */
  public Security resolveSecurity(final SecurityLink link) {
    return link.resolve(_securitySource, _versionCorrection);
  }

  /**
   * Resolves security links on a position and associated trades.
   * <p>
   * Underlying securities are not resolved. Some caching of securities occurs within this instance.
   * 
   * @param position the position to resolve, not null
   * @throws RuntimeException if unable to resolve all the securities
   */
  public void resolveSecurities(final Position position) {
    Collection<SecurityLink> links = new ArrayList<SecurityLink>(position.getTrades().size() + 1);
    if (LinkUtils.isValid(position.getSecurityLink())) {
      links.add(position.getSecurityLink());
    } else {
      s_logger.warn("Invalid link on position {}", position.getUniqueId());
    }
    for (Trade trade : position.getTrades()) {
      if (LinkUtils.isValid(trade.getSecurityLink())) {
        links.add(trade.getSecurityLink());
      } else {
        s_logger.warn("Invalid link on trade {} within position {}", trade.getUniqueId(), position.getUniqueId());
      }
    }
    resolveSecurities(links);
  }

  /**
   * Resolves security links on the positions and trades of a portfolio node.
   * <p>
   * Underlying securities are not resolved. Some caching of securities occurs within this instance.
   * 
   * @param node the node to resolve, not null
   * @throws RuntimeException if unable to resolve all the securities
   */
  public void resolveSecurities(final PortfolioNode node) {
    final Collection<SecurityLink> links = new ArrayList<SecurityLink>(256);
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {
      @Override
      public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
        if (LinkUtils.isValid(position.getSecurityLink())) {
          links.add(position.getSecurityLink());
        } else {
          s_logger.warn("Invalid link on position {}", position.getUniqueId());
        }
        for (Trade trade : position.getTrades()) {
          if (LinkUtils.isValid(trade.getSecurityLink())) {
            links.add(trade.getSecurityLink());
          } else {
            s_logger.warn("Invalid link in trade {} associated with position {}", trade.getUniqueId(), position.getUniqueId());
          }
        }
      }
    }).traverse(node);
    resolveSecurities(links);
  }

  //-------------------------------------------------------------------------
  /**
   * A small job that can be run in an executor to resolve a security against a security source.
   */
  private static final class SingleSecurityResolutionJob implements Callable<Pair<ObjectId, ExternalIdBundle>> {
    private final Pair<ObjectId, ExternalIdBundle> _key;
    private final SecurityLink _link;
    private final SecuritySource _securitySource;
    private final VersionCorrection _versionCorrection;

    private SingleSecurityResolutionJob(Pair<ObjectId, ExternalIdBundle> key, SecurityLink link, SecuritySource securitySource, VersionCorrection versionCorrection) {
      _key = key;
      _securitySource = securitySource;
      _link = link;
      _versionCorrection = versionCorrection;
    }

    @Override
    public Pair<ObjectId, ExternalIdBundle> call() {
      _link.resolve(_securitySource, _versionCorrection);
      return _key;
    }
  }

  /**
   * Resolves a sequence of links.
   */
  private static final class MultipleSecurityResolutionJob implements Callable<Pair<ObjectId, ExternalIdBundle>> {
    private final Pair<ObjectId, ExternalIdBundle> _key;
    private final Collection<SecurityLink> _links;
    private final SecuritySource _securitySource;
    private final VersionCorrection _versionCorrection;

    private MultipleSecurityResolutionJob(final Pair<ObjectId, ExternalIdBundle> key, final Collection<SecurityLink> link, final SecuritySource securitySource,
        final VersionCorrection versionCorrection) {
      _key = key;
      _links = link;
      _securitySource = securitySource;
      _versionCorrection = versionCorrection;
    }

    @Override
    public Pair<ObjectId, ExternalIdBundle> call() {
      for (SecurityLink link : _links) {
        link.resolve(_securitySource, _versionCorrection);
      }
      return _key;
    }

  }

  //-------------------------------------------------------------------------
  /**
   * Encapsulates caching.
   * <p>
   * This is designed to be used by a single resolution pass, for the efficiency of resolving the same security multiple times.
   */
  static class CachedSecuritySource extends AbstractSecuritySource implements SecuritySource {
    private final SecuritySource _underlying;
    private final ConcurrentMap<ObjectId, Security> _objectIdCache = new ConcurrentHashMap<ObjectId, Security>();
    private final ConcurrentMap<ExternalIdBundle, Security> _weakIdCache = new ConcurrentHashMap<ExternalIdBundle, Security>();

    CachedSecuritySource(SecuritySource underlying) {
      _underlying = underlying;
    }

    void addToCache(Security security) {
      if (security.getUniqueId() != null) {
        _objectIdCache.put(security.getUniqueId().getObjectId(), security);
      }
    }

    @Override
    public Security get(UniqueId uniqueId) {
      Security security = _objectIdCache.get(uniqueId.getObjectId());
      if (security == null) {
        security = _underlying.get(uniqueId);
        _objectIdCache.putIfAbsent(uniqueId.getObjectId(), security);
      }
      return security;
    }

    @Override
    public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
      Security security = _objectIdCache.get(objectId);
      if (security == null) {
        security = _underlying.get(objectId, versionCorrection);
        _objectIdCache.putIfAbsent(objectId, security);
      }
      return security;
    }

    @Override
    public Collection<Security> get(ExternalIdBundle bundle) {
      return _underlying.get(bundle);
    }

    @Override
    public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      return _underlying.get(bundle, versionCorrection);
    }

    @Override
    public Security getSingle(ExternalIdBundle bundle) {
      Security security = _weakIdCache.get(bundle);
      if (security == null) {
        security = _underlying.getSingle(bundle);
        if (security != null) {
          _weakIdCache.putIfAbsent(bundle, security);
        }
      }
      return security;
    }

    @Override
    public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
      Security security = _weakIdCache.get(bundle);
      if (security == null) {
        security = _underlying.getSingle(bundle, versionCorrection);
        if (security != null) {
          _weakIdCache.putIfAbsent(bundle, security);
        }
      }
      return security;
    }

    @Override
    public ChangeManager changeManager() {
      return DummyChangeManager.INSTANCE;
    }

  }

}

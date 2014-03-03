/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalIdDisplayComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.Index;
import com.opengamma.financial.security.index.IndexFamily;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.master.security.impl.AbstractSecurityLoader;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.provider.security.SecurityEnhancer;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Loads securities and populates the security master.
 * <p>
 * This uses a provider to load securities and enhancers to add further information,
 * with a master as the backing storage.
 * <p>
 * By default, the loader will only load securities that are not in the master.
 * However, the "force update" flag can be set to re-load those securities.
 */
public class DefaultSecurityLoader extends AbstractSecurityLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultSecurityLoader.class);

  /**
   * The security master to load into.
   */
  private final SecurityMaster _securityMaster;
  /**
   * The security provider to load from.
   */
  private final SecurityProvider _securityProvider;
  /**
   * The security provider to load from.
   */
  private final List<SecurityEnhancer> _securityEnhancers = Lists.newArrayList();

  /**
   * Creates an instance.
   *
   * @param securityMaster  the security master, not null
   * @param securityProvider  the security provider, not null
   */
  public DefaultSecurityLoader(final SecurityMaster securityMaster, final SecurityProvider securityProvider) {
    this(securityMaster, securityProvider, Collections.<SecurityEnhancer>emptyList());
  }

  /**
   * Creates an instance.
   *
   * @param securityMaster  the security master, not null
   * @param securityProvider  the security provider, not null
   * @param securityEnhancers  the security enhancers, not null
   */
  public DefaultSecurityLoader(final SecurityMaster securityMaster, final SecurityProvider securityProvider, final List<SecurityEnhancer> securityEnhancers) {
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    ArgumentChecker.notNull(securityEnhancers, "securityEnhancers");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
    _securityProvider = securityProvider;
    _securityEnhancers.addAll(securityEnhancers);
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityLoaderResult doBulkLoad(final SecurityLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");
    final SecurityLoaderResult result = new SecurityLoaderResult();

    // find missing
    final Map<ExternalIdBundle, Security> missingAndForcedIds = findMissing(request, result);
    if (missingAndForcedIds.size() == 0) {
      return result;
    }

    // load from provider
    Map<ExternalIdBundle, Security> providedMap = _securityProvider.getSecurities(missingAndForcedIds.keySet());

    // load any underlying securities
    Map<ExternalIdBundle, Security> providedUnderlyingMap = loadUnderlyings(providedMap);
    providedUnderlyingMap.keySet().removeAll(providedMap.keySet());  // requested IDs take precedence

    // enhance
    providedUnderlyingMap = enhance(providedUnderlyingMap);
    providedMap = enhance(providedMap);

    // store
    providedUnderlyingMap = store(providedUnderlyingMap, Collections.<ExternalIdBundle, Security>emptyMap());
    if (request.isForceUpdate()) {
      providedMap = store(providedMap, missingAndForcedIds);
    } else {
      providedMap = store(providedMap, Collections.<ExternalIdBundle, Security>emptyMap());
    }

    // copy data into result
    for (final Entry<ExternalIdBundle, Security> entry : providedMap.entrySet()) {
      result.getResultMap().put(entry.getKey(), entry.getValue().getUniqueId());
      if (request.isReturnSecurityObjects()) {
        result.getSecurityMap().put(entry.getValue().getUniqueId(), entry.getValue());
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Searches the master to find which securities are missing.
   * <p>
   * If the update is being forced, the unique identifier will be associated
   * in the returned map.
   *
   * @param request  the original request, not null
   * @param result  the result to populate, not null
   * @return the list of missing bundles, not null
   */
  protected Map<ExternalIdBundle, Security> findMissing(final SecurityLoaderRequest request, final SecurityLoaderResult result) {
    final Map<ExternalIdBundle, Security> missing = Maps.newHashMap();
    for (final ExternalIdBundle requestedBundle : request.getExternalIdBundles()) {
      final SecuritySearchRequest searchRequest = new SecuritySearchRequest(requestedBundle);
      searchRequest.setSortOrder(SecuritySearchSortOrder.OBJECT_ID_ASC);
      searchRequest.setFullDetail(request.isReturnSecurityObjects() || request.isForceUpdate());

      final SecuritySearchResult searchResult = _securityMaster.search(searchRequest);
      if (searchResult.getDocuments().size() == 0) {
        missing.put(requestedBundle, null);
      } else {
        if (searchResult.getDocuments().size() > 1) {
          s_logger.warn("Multiple securities matched bundle {}", requestedBundle);
          // consistent order for duplicates was selected by the sort order
        }
        final ManageableSecurity sec = searchResult.getFirstSecurity();
        if (request.isForceUpdate()) {
          missing.put(requestedBundle, sec);
        } else {
          result.getResultMap().put(requestedBundle, sec.getUniqueId());
          if (request.isReturnSecurityObjects()) {
            result.getSecurityMap().put(sec.getUniqueId(), sec);
          }
        }
      }
    }
    return missing;
  }

  //-------------------------------------------------------------------------
  /**
   * Finds and loads any missing underlying securities.
   * <p>
   * These are not added into the final result object, as they were not requested.
   * However, they are enhanced before they are stored.
   *
   * @param providedMap  the map of securities that have just been provided, not null
   * @return the map of underlying securities that were provided, not null
   */
  protected Map<ExternalIdBundle, Security> loadUnderlyings(final Map<ExternalIdBundle, Security> providedMap) {
    // find and load dependencies
    final Set<ExternalIdBundle> underlyingIds = Sets.newHashSet();
    final Set<Index> indices = new HashSet<Index>();
    final UnderlyingExternalIdVisitor visitor = new UnderlyingExternalIdVisitor();
    for (final Entry<ExternalIdBundle, Security> entry : providedMap.entrySet()) {
      final Security security = entry.getValue();
      if (security instanceof FinancialSecurity) {
        final FinancialSecurity financialSecurity = (FinancialSecurity) security;
        financialSecurity.accept(visitor);
      } else if (security instanceof Index) {
        // record new indices so we can update index families later.
        final Index index = (Index) security;
        indices.add(index);
      }
    }
    underlyingIds.addAll(visitor.getUnderlyings());

    // check which are missing
    final List<ExternalIdBundle> missing = Lists.newArrayList();
    for (final ExternalIdBundle underlyingId : underlyingIds) {
      final SecuritySearchRequest searchRequest = new SecuritySearchRequest(underlyingId);
      searchRequest.setSortOrder(SecuritySearchSortOrder.OBJECT_ID_ASC);
      searchRequest.setFullDetail(false);
      final SecuritySearchResult searchResult = _securityMaster.search(searchRequest);
      if (searchResult.getDocuments().size() == 0) {
        missing.add(underlyingId);
      }
    }

    // load from provider
    final Map<ExternalIdBundle, Security> underlyingProvidedMap = _securityProvider.getSecurities(missing);

    if (underlyingProvidedMap.size() > 0) {
      // recurse to find any more underlying securities
      underlyingProvidedMap.putAll(loadUnderlyings(underlyingProvidedMap));
    }
    // add any index families required.
    processIndices(indices);
    // return complete set of provided underlying securities
    return underlyingProvidedMap;
  }

  private enum Source { EXISTING, TO_ADD, SEC_SOURCE };

  private void processIndices(final Set<Index> indices) {
    final SecuritySource secSource = new MasterSecuritySource(_securityMaster);
    // keep track of existing FamilyIndex entries, and only update once at the end.
    Map<ExternalIdBundle, Security> existing = new HashMap<>();
    // kep track of new FamilyIndex entries and only update once at the end.
    Map<ExternalIdBundle, Security> toAdd = new HashMap<>();
    for (final Index index : indices) {
      if (index.getIndexFamilyId() == null) {
        break; // skip if no family.
      }
      // Get the Tenor, if there is one
      Tenor tenor;
      if (index instanceof OvernightIndex) {
        tenor = Tenor.ON;
      } else if (index instanceof IborIndex) {
        tenor = ((IborIndex) index).getTenor();
      } else if (index instanceof SwapIndex) {
        tenor = ((SwapIndex) index).getTenor();
      } else if (index instanceof PriceIndex) {
        break; // skip to next index as won't have family.
      } else {
        break; // skip to next index as won't have family.
      }
      final ExternalIdBundle familyBundle = index.getIndexFamilyId().toBundle();
      Security security;
      Source source;
      // see if we've seen this family before, and update that if we have.
      if (existing.containsKey(familyBundle)) {
        security = existing.get(familyBundle);
        source = Source.EXISTING;
      } else if (toAdd.containsKey(familyBundle)) {
        security = toAdd.get(familyBundle);
        source = Source.TO_ADD;
      } else {
        // we haven't seen it before, so we need to look it up.  Note it still might not be available.
        security = secSource.getSingle(familyBundle);
        source = Source.SEC_SOURCE;
      }
      if (security instanceof IndexFamily) {
        final IndexFamily indexFamily = (IndexFamily) security;
        final SortedMap<Tenor, ExternalId> members = indexFamily.getMembers();
        // do we need to update the members or has it been done already for this tenor on this family?
        if (!members.containsKey(tenor)) {
          final ExternalId preferred = preferredExternalId(index.getExternalIdBundle());
          assert tenor != null : "Tenor should not be null here";
          members.put(tenor, preferred);
          if (source == Source.SEC_SOURCE) {
            existing.put(indexFamily.getExternalIdBundle(), indexFamily);
          }
        }
      } else {
        // we haven't seen this before and it's not in the sec source,
        // so create a new one and add to the toAdd bucket.
        final IndexFamily indexFamily = new IndexFamily();
        final ExternalId preferred = preferredExternalId(index.getExternalIdBundle());
        indexFamily.setName(index.getIndexFamilyId().getValue());
        indexFamily.setExternalIdBundle(familyBundle);
        final SortedMap<Tenor, ExternalId> entries = new TreeMap<Tenor, ExternalId>();
        entries.put(tenor, preferred);
        indexFamily.setMembers(entries);
        toAdd.put(familyBundle, indexFamily);
      }
    }
    existing = enhance(existing);
    toAdd = enhance(toAdd);
    storeIndexFamilies(existing, toAdd);
  }

  private static final ExternalIdDisplayComparator s_comparator = new ExternalIdDisplayComparator();

  private ExternalId preferredExternalId(final ExternalIdBundle bundle) {
    ExternalId preferred = null;
    for (final ExternalId current : bundle.getExternalIds()) {
      if (preferred == null) {
        preferred = current;
      } else {
        if (s_comparator.compare(preferred, current) > 0) {
          preferred = current;
        }
      }
    }
    return preferred;
  }

  private void storeIndexFamilies(
      final Map<ExternalIdBundle, Security> modified,
      final Map<ExternalIdBundle, Security> added) {
    for (final Security security : modified.values()) {
      final SecurityDocument doc = new SecurityDocument((ManageableSecurity) security);
      _securityMaster.update(doc);
    }
    for (final Security security : added.values()) {
      final SecurityDocument doc = new SecurityDocument((ManageableSecurity) security);
      _securityMaster.add(doc);
    }
  }


  //-------------------------------------------------------------------------
  /**
   * Enhance the provided securities.
   *
   * @param map  the map, updated with the enhanced security, not null
   * @return the enhanced equivalent to the input map, not null
   */
  protected Map<ExternalIdBundle, Security> enhance(final Map<ExternalIdBundle, Security> map) {
    Map<ExternalIdBundle, Security> result = map;
    for (final SecurityEnhancer securityEnhancer : _securityEnhancers) {
      result = securityEnhancer.enhanceSecurities(result);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Stores the map of securities, handling forced update.
   * <p>
   * This will update the security if the second map contains the loaded security.
   *
   * @param loadedMap  the map, updated with the stored security, not null
   * @param originalIds  map of original security before forceful update, null if not force update
   * @return the stored equivalent to the input map, not null
   */
  protected Map<ExternalIdBundle, Security> store(
      final Map<ExternalIdBundle, Security> loadedMap,
      final Map<ExternalIdBundle, Security> originalIds) {

    final Map<ExternalIdBundle, Security> result = Maps.newHashMap();
    for (final Entry<ExternalIdBundle, Security> entry : loadedMap.entrySet()) {
      // cast here is unsafe really
      final ManageableSecurity loaded = (ManageableSecurity) entry.getValue();
      final ManageableSecurity original = (ManageableSecurity) originalIds.get(entry.getKey());
      if (original == null) {
        // security is brand new
        final SecurityDocument doc = new SecurityDocument(loaded);
        final SecurityDocument added = _securityMaster.add(doc);
        result.put(entry.getKey(), added.getSecurity());

      } else {
        loaded.setUniqueId(original.getUniqueId());  // normalize IDs for comparison
        if (loaded.equals(original)) {
          // no change since last loaded, return original with uniqueId
          result.put(entry.getKey(), original);

        } else {
          // loaded is updated from original
          final SecurityDocument doc = new SecurityDocument(loaded);
          final SecurityDocument updated = _securityMaster.update(doc);
          result.put(entry.getKey(), updated.getSecurity());
        }
      }
    }
    return result;
  }

}

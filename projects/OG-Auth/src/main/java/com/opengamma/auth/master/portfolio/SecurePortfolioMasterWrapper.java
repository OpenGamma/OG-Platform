/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio;

import com.opengamma.auth.AuthorisationException;
import com.opengamma.auth.Either;
import com.opengamma.auth.Entitlement;
import com.opengamma.auth.Signer;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.lambdava.streams.Lambdava.functional;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class SecurePortfolioMasterWrapper implements SecurePortfolioMaster, ChangeProvider {

  private final PortfolioMaster _portfolioMaster;
  private final Clock _timeSource;

  public SecurePortfolioMasterWrapper(PortfolioMaster portfolioMaster) {
    _portfolioMaster = portfolioMaster;
    _timeSource = Clock.systemUTC();
  }

  public SecurePortfolioMasterWrapper(PortfolioMaster portfolioMaster, Clock timeSource) {
    _portfolioMaster = portfolioMaster;
    _timeSource = timeSource;
  }

  /**
   * Returns set of identifiers from given entitlements which have required resource access;
   */
  private static Set<Either<ObjectId, WholePortfolio>> getResourceIdentifiers(Collection<PortfolioEntitlement> entitlements, final ResourceAccess... access) {
    Set<Either<ObjectId, WholePortfolio>> satisfyingIdentifiers = newHashSet();
    HashSet<ResourceAccess> requiredAccess = newHashSet(Arrays.asList(access));
    Map<Either<ObjectId, WholePortfolio>, Set<ResourceAccess>> entitlementsWithRequiredAccess = newHashMap();
    for (PortfolioEntitlement entitlement : entitlements) {
      if (requiredAccess.contains(entitlement.getAccess())) {
        Set<ResourceAccess> entitlementAccess = entitlementsWithRequiredAccess.get(entitlement.getIdentifier());
        if (entitlementAccess == null) {
          entitlementAccess = newHashSet();
          entitlementsWithRequiredAccess.put(entitlement.getIdentifier(), entitlementAccess);
        }
        entitlementAccess.add(entitlement.getAccess());
      }
    }

    for (Either<ObjectId, WholePortfolio> portfolioIdentifier : entitlementsWithRequiredAccess.keySet()) {
      if (entitlementsWithRequiredAccess.get(portfolioIdentifier).containsAll(requiredAccess)) {
        satisfyingIdentifiers.add(portfolioIdentifier);
      }
    }
    return satisfyingIdentifiers;
  }


  private static boolean hasAccess(final Instant now, ObjectId oid, Collection<PortfolioEntitlement> entitlements, ResourceAccess... access) {

    // we need to rewrite the collection from Collection<PortfolioEntitlement> to Collection<PortfolioEntitlement> because java is stupid
    Collection<PortfolioEntitlement> entitlements2 = newArrayList();
    for (Entitlement entitlement : entitlements) {
      PortfolioEntitlement portfolioEntitlement = new PortfolioEntitlement((Either) entitlement.getIdentifier(), entitlement.getExpiry(), entitlement.getAccess());
      entitlements2.add(portfolioEntitlement);
    }

    List<PortfolioEntitlement> notExpired = functional(entitlements2).filter(new Function1<PortfolioEntitlement, Boolean>() {
      @Override
      public Boolean execute(PortfolioEntitlement portfolioEntitlement) {
        return portfolioEntitlement.getExpiry().isAfter(now);
      }
    }).asList();

    Set<Either<ObjectId, WholePortfolio>> identifiers = getResourceIdentifiers(notExpired, access);
    return identifiers.contains(Either.left(oid)) || identifiers.contains(Either.right(WholePortfolio.INSTANCE));
  }

  private static boolean hasGlobalAccess(Collection<PortfolioEntitlement> entitlements, ResourceAccess access) {
    Set<Either<ObjectId, WholePortfolio>> identifiers = getResourceIdentifiers(entitlements, access);
    return identifiers.contains(Either.right(WholePortfolio.INSTANCE));
  }

  private static PortfolioSearchResult filterResults(Collection<PortfolioEntitlement> entitlements, PortfolioSearchResult searchResult, ResourceAccess... access) {
    Set<Either<ObjectId, WholePortfolio>> identifiers = getResourceIdentifiers(entitlements, access);
    List<PortfolioDocument> filtered = newArrayList();
    for (PortfolioDocument document : searchResult.getDocuments()) {
      if (identifiers.contains(Either.left(document.getObjectId())) || identifiers.contains(Either.right(WholePortfolio.INSTANCE))) {
        filtered.add(document);
      }
    }
    return new PortfolioSearchResult(filtered);
  }

  private static PortfolioHistoryResult filterResults(Collection<PortfolioEntitlement> entitlements, PortfolioHistoryResult historyResult, ResourceAccess... access) {
    Set<Either<ObjectId, WholePortfolio>> identifiers = getResourceIdentifiers(entitlements, access);
    List<PortfolioDocument> filtered = newArrayList();
    for (PortfolioDocument document : historyResult.getDocuments()) {
      if (identifiers.contains(Either.left(document.getObjectId())) || identifiers.contains(Either.right(WholePortfolio.INSTANCE))) {
        filtered.add(document);
      }
    }
    return new PortfolioHistoryResult(filtered);
  }

  public PortfolioSearchResult search(PortfolioCapability portfolioCapability, PortfolioSearchRequest request) {
    return filterResults(Signer.verify(portfolioCapability), _portfolioMaster.search(request), ResourceAccess.READ);
  }

  public void remove(PortfolioCapability portfolioCapability, ObjectIdentifiable oid) {
    if (hasAccess(_timeSource.instant(), oid.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.DELETE)) {
      _portfolioMaster.remove(oid);
    } else {
      throw new AuthorisationException("Unauthorised attempt to delete portfolio [" + oid + "]");
    }
  }

  public PortfolioDocument get(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    if (hasAccess(_timeSource.instant(), objectId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.READ)) {
      return _portfolioMaster.get(objectId, versionCorrection);
    } else {
      throw new AuthorisationException("Unauthorised attempt to get portfolio [" + objectId + "]");
    }
  }

  public UniqueId addVersion(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, PortfolioDocument documentToAdd) {
    if (hasAccess(_timeSource.instant(), objectId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.WRITE)) {
      return _portfolioMaster.addVersion(objectId, documentToAdd);
    } else {
      throw new AuthorisationException("Unauthorised attempt to add version portfolio [" + objectId + "]");
    }
  }

  public PortfolioDocument get(PortfolioCapability portfolioCapability, UniqueId uniqueId) {
    if (hasAccess(_timeSource.instant(), uniqueId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.READ)) {
      return _portfolioMaster.get(uniqueId);
    } else {
      throw new AuthorisationException("Unauthorised attempt to get portfolio [" + uniqueId + "]");
    }
  }

  public List<UniqueId> replaceAllVersions(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    if (hasAccess(_timeSource.instant(), objectId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.WRITE)) {
      return _portfolioMaster.replaceAllVersions(objectId, replacementDocuments);
    } else {
      throw new AuthorisationException("Unauthorised attempt to replace all versions portfolio [" + objectId + "]");
    }
  }

  public PortfolioDocument update(PortfolioCapability portfolioCapability, PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    if (hasAccess(_timeSource.instant(), document.getUniqueId().getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.WRITE)) {
      return _portfolioMaster.update(document);
    } else {
      throw new AuthorisationException("Unauthorised attempt to update portfolio [" + document.getUniqueId() + "]");
    }
  }

  public PortfolioHistoryResult history(PortfolioCapability portfolioCapability, PortfolioHistoryRequest request) {
    return filterResults(Signer.verify(portfolioCapability), _portfolioMaster.history(request), ResourceAccess.READ);
  }

  public PortfolioDocument add(PortfolioCapability portfolioCapability, PortfolioDocument document) {
    if (hasGlobalAccess(Signer.verify(portfolioCapability), ResourceAccess.WRITE)) {
      return _portfolioMaster.add(document);
    } else {
      throw new AuthorisationException("Unauthorised attempt to add new portfolio [" + document + "]");
    }
  }

  public List<UniqueId> replaceVersions(PortfolioCapability portfolioCapability, ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    if (hasAccess(_timeSource.instant(), objectId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.WRITE)) {
      return _portfolioMaster.replaceVersions(objectId, replacementDocuments);
    } else {
      throw new AuthorisationException("Unauthorised attempt to replace versions of portfolio [" + objectId + "]");
    }
  }

  public ManageablePortfolioNode getNode(PortfolioCapability portfolioCapability, UniqueId nodeId) {
    ManageablePortfolioNode node = _portfolioMaster.getNode(nodeId);
    UniqueId portfolioId = node.getPortfolioId();
    if (hasAccess(_timeSource.instant(), portfolioId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.READ)) {
      return node;
    } else {
      throw new AuthorisationException("Unauthorised attempt to get node of portfolio [" + portfolioId + "]");
    }
  }

  public Map<UniqueId, PortfolioDocument> get(PortfolioCapability portfolioCapability, Collection<UniqueId> uniqueIds) {
    Set<Either<ObjectId, WholePortfolio>> identifiers = getResourceIdentifiers(Signer.verify(portfolioCapability), ResourceAccess.READ);
    Map<UniqueId, PortfolioDocument> result = newHashMap();
    for (Map.Entry<UniqueId, PortfolioDocument> entry : _portfolioMaster.get(uniqueIds).entrySet()) {
      if (identifiers.contains(Either.left(entry.getKey().getObjectId())) || identifiers.contains(Either.right(WholePortfolio.INSTANCE))) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  public PortfolioDocument correct(PortfolioCapability portfolioCapability, PortfolioDocument document) {
    if (hasAccess(_timeSource.instant(), document.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.WRITE)) {
      return _portfolioMaster.correct(document);
    } else {
      throw new AuthorisationException("Unauthorised attempt to corrent portfolio [" + document + "]");
    }
  }

  public void removeVersion(PortfolioCapability portfolioCapability, UniqueId uniqueId) {
    if (hasAccess(_timeSource.instant(), uniqueId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.DELETE)) {
      _portfolioMaster.removeVersion(uniqueId);
    } else {
      throw new AuthorisationException("Unauthorised attempt to remove version of portfolio [" + uniqueId + "]");
    }
  }

  public UniqueId replaceVersion(PortfolioCapability portfolioCapability, PortfolioDocument replacementDocument) {
    if (hasAccess(_timeSource.instant(), replacementDocument.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.DELETE, ResourceAccess.WRITE)) {
      return _portfolioMaster.replaceVersion(replacementDocument);
    } else {
      throw new AuthorisationException("Unauthorised attempt to replace version of portfolio [" + replacementDocument + "]");
    }
  }

  public List<UniqueId> replaceVersion(PortfolioCapability portfolioCapability, UniqueId uniqueId, List<PortfolioDocument> replacementDocuments) {
    if (hasAccess(_timeSource.instant(), uniqueId.getObjectId(), Signer.verify(portfolioCapability), ResourceAccess.DELETE, ResourceAccess.WRITE)) {
      return _portfolioMaster.replaceVersion(uniqueId, replacementDocuments);
    } else {
      throw new AuthorisationException("Unauthorised attempt to replace version of portfolio [" + uniqueId + "]");
    }

  }

  public ChangeManager changeManager() {
    return _portfolioMaster.changeManager();
  }

}

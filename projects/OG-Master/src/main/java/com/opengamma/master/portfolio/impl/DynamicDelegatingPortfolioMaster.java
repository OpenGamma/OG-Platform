package com.opengamma.master.portfolio.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A portfolio master that uses the scheme of the unique identifier to determine which
 * underlying master should handle the request.
 * <p/>
 * The underlying masters, or delegates, can be registered or deregistered at run time.
 * By default there is an {@link InMemoryPortfolioMaster} that will be used if specific scheme/delegate
 * combinations have not been registered.
 * <p/>
 * Change events are aggregated from the different masters and presented through a single change manager.
 * <p/>
 * The {@link #register(String, PortfolioMaster)}, {@link #deregister(String)} and
 * {@link #add(String, PortfolioDocument)} methods are public API outside
 * of the normal Master interface. Therefore to properly use this class the caller must have
 * a concrete instance of this class and use these methods to properly initialize the delegates
 * as well as clean up resources when a delegate is no longer needed. But the engine itself will
 * be able to interact with the component via standard Master interface.
 */
public class DynamicDelegatingPortfolioMaster implements PortfolioMaster {

  /** The change manager. Aggregates among all the delegates */
  private final AggregatingChangeManager _changeManager;

  /**
   * The default delegate. Should never have data in it. If user ask for data with an unregistered scheme,
   * this empty master will be used
   */
  private final InMemoryPortfolioMaster _defaultEmptyDelegate;

  /** Delegator for maintaining map from scheme to master */
  private final UniqueIdSchemeDelegator<PortfolioMaster> _delegator;

  public DynamicDelegatingPortfolioMaster() {
    _changeManager = new AggregatingChangeManager();
    _defaultEmptyDelegate = new InMemoryPortfolioMaster();
    _delegator = new UniqueIdSchemeDelegator<PortfolioMaster>(_defaultEmptyDelegate);
    _changeManager.addChangeManager(_defaultEmptyDelegate.changeManager());
  }

  /**
   * Registers a scheme and delegate pair.
   * <p/>
   * The caller is responsible for creating a delegate and registering it before making calls
   * to the DynamicDelegatingPortfolioMaster
   *
   * @param scheme the external scheme associated with this delegate master, not null
   * @param delegate the master to be used for this scheme, not null
   */
  public void register(final String scheme, final PortfolioMaster delegate) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(delegate, "delegate");
    _changeManager.addChangeManager(delegate.changeManager());
    _delegator.registerDelegate(scheme, delegate);
  }

  /**
   * Deregisters a scheme and delegate pair.
   * <p/>
   * The caller is responsible for deregistering a delegate when it is no longer needed.
   * For example, if delegates are made up of InMemoryMasters and data is no longer needed,
   * call deregister will free up memory
   *
   * @param scheme the external scheme associated with the delegate master to be removed, not null
   */
  public void deregister(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _changeManager.removeChangeManager(chooseDelegate(scheme).changeManager());
    _delegator.removeDelegate(scheme);
  }

  public PortfolioDocument add(final String scheme, final PortfolioDocument document) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(scheme).add(document);
  }

  private PortfolioMaster chooseDelegate(final String scheme) {
    return _delegator.chooseDelegate(scheme);
  }

  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    Collection<ObjectId> ids = request.getPortfolioObjectIds();
    return chooseDelegate(ids.iterator().next().getScheme()).search(request);
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    return chooseDelegate(request.getObjectId().getScheme()).history(request);
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    return chooseDelegate(nodeId.getScheme()).getNode(nodeId);
  }

  @Override
  public PortfolioDocument get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public PortfolioDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, PortfolioDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, PortfolioDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      PortfolioDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public PortfolioDocument add(PortfolioDocument document) {
    throw new UnsupportedOperationException("Cannot add document without explicitly specifying the scheme");
  }

  @Override
  public PortfolioDocument update(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    Validate.notNull(document.getUniqueId(), "document has no unique id");
    Validate.notNull(document.getObjectId(), "document has no object id");
    return chooseDelegate(document.getObjectId().getScheme()).update(document);
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
    ArgumentChecker.notNull(oid, "objectIdentifiable");
    chooseDelegate(oid.getObjectId().getScheme()).remove(oid);
  }

  @Override
  public PortfolioDocument correct(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<PortfolioDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(uniqueId.getScheme()).replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<PortfolioDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(PortfolioDocument replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");
    ArgumentChecker.notNull(replacementDocument.getObjectId(), "replacementDocument.getObjectId");
    return chooseDelegate(replacementDocument.getObjectId().getScheme()).replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    chooseDelegate(uniqueId.getScheme()).removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, PortfolioDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
}

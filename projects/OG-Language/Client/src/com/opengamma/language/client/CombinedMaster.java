/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.AbstractSearchResult;

/**
 * A 'master' combined from the session, user and global masters.
 *
 * @param <D> master document type
 * @param <M> master type
 */
/* package */abstract class CombinedMaster<D extends AbstractDocument, M extends AbstractMaster<D>> implements AbstractMaster<D> {

  private static final Logger s_logger = LoggerFactory.getLogger(CombinedMaster.class);

  private final CombiningMaster<D, M, ?> _combining;
  private final M _sessionMaster;
  private final M _userMaster;
  private final M _globalMaster;

  protected CombinedMaster(final CombiningMaster<D, M, ?> combining, final M sessionMaster, final M userMaster, final M globalMaster) {
    _combining = combining;
    _sessionMaster = sessionMaster;
    _userMaster = userMaster;
    _globalMaster = globalMaster;
  }

  private CombiningMaster<D, M, ?> getCombining() {
    return _combining;
  }

  protected M getMasterByScheme(final String scheme) {
    final MasterID id = getCombining().getSchemeMasterID(scheme);
    if (id == null) {
      return null;
    }
    switch (id) {
      case SESSION:
        return getSessionMaster();
      case USER:
        return getUserMaster();
      case GLOBAL:
        return getGlobalMaster();
      default:
        throw new IllegalStateException();
    }
  }

  protected void setSessionMasterScheme(final String scheme) {
    getCombining().setSchemeMasterID(scheme, MasterID.SESSION);
  }

  protected void setUserMasterScheme(final String scheme) {
    getCombining().setSchemeMasterID(scheme, MasterID.USER);
  }

  protected void setGlobalMasterScheme(final String scheme) {
    getCombining().setSchemeMasterID(scheme, MasterID.GLOBAL);
  }

  protected M getSessionMaster() {
    return _sessionMaster;
  }

  protected M getUserMaster() {
    return _userMaster;
  }

  protected M getGlobalMaster() {
    return _globalMaster;
  }

  protected abstract class Try<T> {

    public abstract T tryMaster(M master);

    public T each(final String scheme) {
      T result;
      if (getSessionMaster() != null) {
        try {
          s_logger.debug("Trying SESSION master for {}", scheme);
          result = tryMaster(getSessionMaster());
          setSessionMasterScheme(scheme);
          return result;
        } catch (IllegalArgumentException e) {
          s_logger.info("Illegal argument exception from session master", e);
        }
      }
      if (getUserMaster() != null) {
        try {
          s_logger.debug("Trying USER master for {}", scheme);
          result = tryMaster(getUserMaster());
          setUserMasterScheme(scheme);
          return result;
        } catch (IllegalArgumentException e) {
          s_logger.info("Illegal argument exception from user master", e);
        }
      }
      if (getGlobalMaster() != null) {
        try {
          s_logger.debug("Trying GLOBAL master for {}", scheme);
          result = tryMaster(getGlobalMaster());
          setGlobalMasterScheme(scheme);
          return result;
        } catch (IllegalArgumentException e) {
          s_logger.info("Illegal argument exception from global master", e);
        }
      }
      throw new IllegalArgumentException("No masters accepted request on scheme " + scheme);
    }

  }

  @Override
  public D get(final UniqueId uniqueId) {
    final M master = getMasterByScheme(uniqueId.getScheme());
    if (master != null) {
      return master.get(uniqueId);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.get(uniqueId);
      }
    }).each(uniqueId.getScheme());
  }

  @Override
  public D get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    final M master = getMasterByScheme(objectId.getObjectId().getScheme());
    if (master != null) {
      return master.get(objectId, versionCorrection);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.get(objectId, versionCorrection);
      }
    }).each(objectId.getObjectId().getScheme());
  }

  @Override
  public D add(final D document) {
    if (getSessionMaster() != null) {
      return getSessionMaster().add(document);
    } else if (getUserMaster() != null) {
      return getUserMaster().add(document);
    } else if (getGlobalMaster() != null) {
      return getGlobalMaster().add(document);
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public D update(final D document) {
    final M master = getMasterByScheme(document.getUniqueId().getScheme());
    if (master != null) {
      return master.update(document);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.update(document);
      }
    }).each(document.getUniqueId().getScheme());
  }

  @Override
  public void remove(final ObjectIdentifiable objectIdentifiable) {
    final M master = getMasterByScheme(objectIdentifiable.getObjectId().getScheme());
    if (master != null) {
      master.remove(objectIdentifiable);
    }
    (new Try<Void>() {
      @Override
      public Void tryMaster(final M master) {
        master.remove(objectIdentifiable);
        return null;
      }
    }).each(objectIdentifiable.getObjectId().getScheme());
  }

  @Override
  public D correct(final D document) {
    final M master = getMasterByScheme(document.getUniqueId().getScheme());
    if (master != null) {
      master.correct(document);
    }
    return (new Try<D>() {
      @Override
      public D tryMaster(final M master) {
        return master.correct(document);
      }
    }).each(document.getUniqueId().getScheme());
  }

  public interface SearchCallback<D> extends Comparator<D> {

    /**
     * Returns true to include the document in the callback results, false
     * to suppress it.
     *
     * @param document document to consider, not null
     * @return whether to include the document
     */
    boolean include(D document);

    /**
     * Passes a document to the callback. Only documents that were accepted by
     * {@link #include} should be passed.
     *
     * @param document document to consider, not null
     * @param master the master that sourced the document, not null
     * @param masterUnique true if this comparator did not make the document
     *        equal to any other document passed from this master
     * @param clientUnique true if this comparator did not make the document
     *        equal to any other document from the masters on this client
     */
    void accept(D document, MasterID master, boolean masterUnique, boolean clientUnique);

  }

  private D next(final Iterator<D> itr, final SearchCallback<D> callback) {
    while (itr.hasNext()) {
      final D element = itr.next();
      if (callback.include(element)) {
        return element;
      }
    }
    return null;
  }

  private List<D> getNonUnique(final Set<D> nonUnique, final List<D> documents, final SearchCallback<D> callback) {
    Collections.sort(documents, callback);
    D previous = null;
    for (D document : documents) {
      if (callback.include(document)) {
        if (previous != null) {
          if (callback.compare(previous, document) == 0) {
            nonUnique.add(previous);
            nonUnique.add(document);
          }
        }
        previous = document;
      }
    }
    return documents;
  }

  protected void search(final AbstractSearchResult<D> sessionResult, final AbstractSearchResult<D> userResult, final AbstractSearchResult<D> globalResult, final SearchCallback<D> callback) {
    final Set<D> nonUnique = new HashSet<D>();
    final List<D> sessionDocuments = (sessionResult != null) ? getNonUnique(nonUnique, sessionResult.getDocuments(), callback) : Collections.<D>emptyList();
    final List<D> userDocuments = (userResult != null) ? getNonUnique(nonUnique, userResult.getDocuments(), callback) : Collections.<D>emptyList();
    final List<D> globalDocuments = (globalResult != null) ? getNonUnique(nonUnique, globalResult.getDocuments(), callback) : Collections.<D>emptyList();
    final Iterator<D> sessionDocumentIterator = sessionDocuments.iterator();
    final Iterator<D> userDocumentIterator = userDocuments.iterator();
    final Iterator<D> globalDocumentIterator = globalDocuments.iterator();
    D sessionDocument = next(sessionDocumentIterator, callback);
    D userDocument = next(userDocumentIterator, callback);
    D globalDocument = next(globalDocumentIterator, callback);
    do {
      if (sessionDocument != null) {
        if (userDocument != null) {
          final int su = callback.compare(sessionDocument, userDocument);
          if (globalDocument != null) {
            final int sg = callback.compare(sessionDocument, globalDocument);
            if (su == 0) {
              if (sg == 0) {
                callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), false);
                callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), false);
                callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), false);
                sessionDocument = next(sessionDocumentIterator, callback);
                userDocument = next(userDocumentIterator, callback);
                globalDocument = next(globalDocumentIterator, callback);
              } else if (sg < 0) {
                callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), false);
                callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), false);
                sessionDocument = next(sessionDocumentIterator, callback);
                userDocument = next(userDocumentIterator, callback);
              } else {
                callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), true);
                globalDocument = next(globalDocumentIterator, callback);
              }
            } else if (su < 0) {
              if (sg == 0) {
                callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), false);
                callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), false);
                sessionDocument = next(sessionDocumentIterator, callback);
                globalDocument = next(globalDocumentIterator, callback);
              } else if (sg < 0) {
                callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), true);
                sessionDocument = next(sessionDocumentIterator, callback);
              } else {
                callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), true);
                globalDocument = next(globalDocumentIterator, callback);
              }
            } else {
              final int ug = callback.compare(userDocument, globalDocument);
              if (ug == 0) {
                callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), false);
                callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), false);
                userDocument = next(userDocumentIterator, callback);
                globalDocument = next(globalDocumentIterator, callback);
              } else if (ug < 0) {
                callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), true);
                userDocument = next(userDocumentIterator, callback);
              } else {
                callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), true);
                globalDocument = next(globalDocumentIterator, callback);
              }
            }
          } else {
            if (su == 0) {
              callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), false);
              callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), false);
              sessionDocument = next(sessionDocumentIterator, callback);
              userDocument = next(userDocumentIterator, callback);
            } else if (su < 0) {
              callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), true);
              sessionDocument = next(sessionDocumentIterator, callback);
            } else {
              callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), true);
              userDocument = next(userDocumentIterator, callback);
            }
          }
        } else {
          if (globalDocument != null) {
            final int sg = callback.compare(sessionDocument, globalDocument);
            if (sg == 0) {
              callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), false);
              callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), false);
              sessionDocument = next(sessionDocumentIterator, callback);
              globalDocument = next(globalDocumentIterator, callback);
            } else if (sg < 0) {
              callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), true);
              sessionDocument = next(sessionDocumentIterator, callback);
            } else {
              callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), true);
              globalDocument = next(globalDocumentIterator, callback);
            }
          } else {
            callback.accept(sessionDocument, MasterID.SESSION, !nonUnique.contains(sessionDocument), true);
            sessionDocument = next(sessionDocumentIterator, callback);
          }
        }
      } else {
        if (userDocument != null) {
          if (globalDocument != null) {
            final int ug = callback.compare(userDocument, globalDocument);
            if (ug == 0) {
              callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), false);
              callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), false);
              userDocument = next(userDocumentIterator, callback);
              globalDocument = next(globalDocumentIterator, callback);
            } else if (ug < 0) {
              callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), true);
              userDocument = next(userDocumentIterator, callback);
            } else {
              callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), true);
              globalDocument = next(globalDocumentIterator, callback);
            }
          } else {
            callback.accept(userDocument, MasterID.USER, !nonUnique.contains(userDocument), true);
            userDocument = next(userDocumentIterator, callback);
          }
        } else {
          if (globalDocument != null) {
            callback.accept(globalDocument, MasterID.GLOBAL, !nonUnique.contains(globalDocument), true);
            globalDocument = next(globalDocumentIterator, callback);
          } else {
            break;
          }
        }
      }
    } while (true);
  }

  @Override
  public final List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<D> replacementDocuments) {
    final String scheme = uniqueId.getScheme();
    final M master = getMasterByScheme(scheme);
    if (master != null) {
      return master.replaceVersion(uniqueId, replacementDocuments);
    }
    return (new Try<List<UniqueId>>() {
      @Override
      public List<UniqueId> tryMaster(final M master) {
        return master.replaceVersion(uniqueId, replacementDocuments);
      }
    }).each(scheme);
  }

  @Override
  public final List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    final String scheme = objectId.getObjectId().getScheme();
    final M master = getMasterByScheme(scheme);
    if (master != null) {
      return master.replaceAllVersions(objectId, replacementDocuments);
    }
    return (new Try<List<UniqueId>>() {
      @Override
      public List<UniqueId> tryMaster(final M master) {
        return master.replaceAllVersions(objectId, replacementDocuments);
      }
    }).each(scheme);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    final String scheme = objectId.getObjectId().getScheme();
    final M master = getMasterByScheme(scheme);
    if (master != null) {
      return master.replaceVersions(objectId, replacementDocuments);
    }
    return (new Try<List<UniqueId>>() {
      @Override
      public List<UniqueId> tryMaster(final M master) {
        return master.replaceVersions(objectId, replacementDocuments);
      }
    }).each(scheme);
  }

  @Override
  public final void removeVersion(final UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<D>emptyList());
  }

  @Override
  public final UniqueId replaceVersion(D replacementDocument) {
    List<UniqueId> result = replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public final UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd) {
    List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import javax.time.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code ConfigTypeMaster}.
 * <p>
 * This master does not support versioning of configuration documents.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 * 
 * @param <T>  the config element type
 */
public class InMemoryConfigTypeMaster<T> implements ConfigTypeMaster<T> {

  /**
   * A cache of securities by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, ConfigDocument<T>> _configs = new ConcurrentHashMap<UniqueIdentifier, ConfigDocument<T>>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;
  
  private final CopyOnWriteArraySet<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  /**
   * Creates an instance.
   */
  public InMemoryConfigTypeMaster() {
    this(new UniqueIdentifierSupplier(InMemoryConfigMaster.DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryConfigTypeMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigSearchResult<T> search(final ConfigSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    Collection<ConfigDocument<T>> docs = _configs.values();
    if (request.getName() != null) {
      final Pattern pattern = RegexUtils.wildcardsToPattern(request.getName());
      docs = Collections2.filter(docs, new Predicate<ConfigDocument<T>>() {
        @Override
        public boolean apply(final ConfigDocument<T> doc) {
          return pattern.matcher(doc.getName()).matches();
        }
      });
    }
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final ConfigDocument<T> document = _configs.get(uid);
    if (document == null) {
      throw new DataNotFoundException("Config not found: " + uid);
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> add(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    
    final T value = document.getValue();
    final UniqueIdentifier uid = _uidSupplier.get();
    final Instant now = Instant.now();
    UniqueIdentifiables.setInto(value, uid);
    final ConfigDocument<T> doc = new ConfigDocument<T>();
    doc.setName(document.getName());
    doc.setValue(value);
    doc.setUniqueId(uid);
    doc.setVersionFromInstant(now);
    _configs.put(uid, doc);  // unique identifier should be unique
    
    //notify listeners
    notifyDocumentAdded(doc);
    return doc;
  }

  private void notifyDocumentAdded(ConfigDocument<T> added) {
    for (MasterChangeListener listener : _listeners) {
      listener.added(added.getUniqueId());
    }
  }
  
  private void notifyDocumentRemoved(UniqueIdentifier uid) {
    for (MasterChangeListener listener : _listeners) {
      listener.removed(uid);
    }
  }
  
  private void notifyDocumentUpdated(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.updated(oldItem, newItem);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> update(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    final UniqueIdentifier uid = document.getUniqueId();
    final Instant now = Instant.now();
    final ConfigDocument<T> storedDocument = _configs.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Config not found: " + uid);
    }
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    if (_configs.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    
    notifyDocumentUpdated(uid, document.getUniqueId());
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    if (_configs.remove(uid) == null) {
      throw new DataNotFoundException("Config not found: " + uid);
    }
    notifyDocumentRemoved(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigHistoryResult<T> history(final ConfigHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final ConfigHistoryResult<T> result = new ConfigHistoryResult<T>();
    final ConfigDocument<T> doc = get(request.getObjectId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> correct(final ConfigDocument<T> document) {
    final UniqueIdentifier oldItem = document.getUniqueId();
    ConfigDocument<T> corrected = update(document);
    UniqueIdentifier newItem = corrected.getUniqueId();
    notifyDocumentCorrected(oldItem, newItem);
    return corrected;
  }
  
  private void notifyDocumentCorrected(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.corrected(oldItem, newItem);
    }
  }

  @Override
  public void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }

}

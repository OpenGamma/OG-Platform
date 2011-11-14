/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.time.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, in-memory implementation of {@link ManageableViewDefinitionRepository}.
 */
public class InMemoryViewDefinitionRepository implements ManageableViewDefinitionRepository {

  private static final String UID_SCHEME = "MemViewDef";
  private final AtomicLong _viewDefinitionCounter = new AtomicLong();
  private ReentrantReadWriteLock _rwl = new ReentrantReadWriteLock(true);
  
  private final Map<ObjectId, SortedSet<UniqueId>> _definitionsByObjectId = new HashMap<ObjectId, SortedSet<UniqueId>>();
  private final Map<UniqueId, ViewDefinition> _definitionsByUniqueId = new HashMap<UniqueId, ViewDefinition>();
  private final Map<String, SortedSet<ObjectId>> _definitionsByName = new HashMap<String, SortedSet<ObjectId>>();
  private final ChangeManager _changeManager = new BasicChangeManager();

  //-------------------------------------------------------------------------
  @Override
  public ViewDefinition getDefinition(UniqueId uniqueId) {

    ViewDefinition result = null;
    
    // Acquire read lock
    _rwl.readLock().lock();

    try {
      if (uniqueId.isVersioned()) {
        // get specified version
        result = _definitionsByUniqueId.get(uniqueId);
      } else {
        // if version unspecified, get latest version available
        result = getDefinition(uniqueId.getObjectId());
      }  
    } finally {
      // Release read lock
      _rwl.readLock().unlock();    
    }
    
    return result;
  }

  public ViewDefinition getDefinition(ObjectId objectId) {
 
    ViewDefinition result = null;

    // Acquire read lock
    _rwl.readLock().lock();

    try {
      // get latest version available
      SortedSet<UniqueId> matchingUniqueIds = _definitionsByObjectId.get(objectId);
      
      if (matchingUniqueIds != null) {
        assert (!matchingUniqueIds.isEmpty());
        result = _definitionsByUniqueId.get(matchingUniqueIds.last());      
      }
    } finally {
      // Release read lock
      _rwl.readLock().unlock();          
    }
    
    return result;
  }
  
  @Override
  public ViewDefinition getDefinition(String definitionName) {

    ViewDefinition result = null;
        
    // Acquire read lock
    _rwl.readLock().lock();

    try {
      // get some view definitions that match the name and return one of them
      SortedSet<ObjectId> matchingObjectIds = _definitionsByName.get(definitionName);
      if (matchingObjectIds != null) {
        assert (!matchingObjectIds.isEmpty());
        result = getDefinition(matchingObjectIds.last());
      }
    } finally {
      // Release read lock
      _rwl.readLock().unlock();          
    }
    
    return result;
  }

  @Override
  public Set<ObjectId> getDefinitionIds() {
    return new TreeSet<ObjectId>(_definitionsByObjectId.keySet());
  }

  public Map<UniqueId, String> getDefinitionEntries() {
    
    Map<UniqueId, String> result = new HashMap<UniqueId, String>();
    
    // Acquire read lock
    _rwl.readLock().lock();

    try {
      for (Map.Entry<ObjectId, SortedSet<UniqueId>> entry : _definitionsByObjectId.entrySet()) {
        if ((entry.getValue() != null) && (!entry.getValue().isEmpty())) {
          ViewDefinition viewDef = getDefinition(entry.getValue().last());
          if (viewDef != null) {
            result.put(entry.getValue().last(), getDefinition(entry.getValue().last()).getName());
          }
        }
      }
    } finally {
      // Release read lock
      _rwl.readLock().unlock();    
    }
    
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isModificationSupported() {
    return true;
  }

  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    ArgumentChecker.notNull(request, "request");
    request.checkValid();

    // Acquire write lock
    _rwl.writeLock().lock();
   
    try {
      final ViewDefinition viewDefinition = request.getViewDefinition();
      //final String originalName = request.getName();
      final UniqueId origUniqueId = viewDefinition.getUniqueId();
  
      // Just remove old view definition and replace with new one 
      removeViewDefinition(origUniqueId);
      addViewDefinition(new AddViewDefinitionRequest(request.getViewDefinition()));
  
      changeManager().entityChanged(ChangeType.UPDATED, origUniqueId, request.getViewDefinition().getUniqueId(), Instant.now());
    } finally {
      // Release write lock
      _rwl.writeLock().unlock();
    }
  }

  
  @Override
  public UniqueId addViewDefinition(AddViewDefinitionRequest request) {
    
    ArgumentChecker.notNull(request, "request");
    request.checkValid();

    // Acquire write lock
    _rwl.writeLock().lock();

    final ViewDefinition viewDefinition = request.getViewDefinition();

    try {           
      if (viewDefinition.getUniqueId() == null) {
        // Create a new UniqueId if none was passed in the request
        viewDefinition.setUniqueId(UniqueId.of(UID_SCHEME, Long.toString(_viewDefinitionCounter.incrementAndGet()), "1"));
      } else if (!viewDefinition.getUniqueId().isVersioned()) {
        // The unique id cannot be unversioned
        throw new OpenGammaRuntimeException("Cannot add unversioned view definition" + viewDefinition.getUniqueId());
      }
  
      // Fetch set of matching unique ids
      SortedSet<UniqueId> matchingUniqueIds = _definitionsByObjectId.get(viewDefinition.getUniqueId().getObjectId());
      
      UniqueId origUniqueId;
      if (matchingUniqueIds != null) {
        assert (!matchingUniqueIds.isEmpty()); 
        origUniqueId = matchingUniqueIds.last();
      } else {
        // Create empty set of matching unique ids if this is the first entry
        matchingUniqueIds = new TreeSet<UniqueId>();
        _definitionsByObjectId.put(viewDefinition.getUniqueId().getObjectId(), matchingUniqueIds);
        origUniqueId = null;
      }
      
      // Add this view definition to set of matching unique ids
      if (!matchingUniqueIds.contains(viewDefinition.getUniqueId())) {
        matchingUniqueIds.add(viewDefinition.getUniqueId());
      }
            
      // Fetch set of matching object ids
      SortedSet<ObjectId> matchingObjectIds = _definitionsByName.get(viewDefinition.getName());
      
      // Create empty set of matching object ids if this is the first entry
      if (matchingObjectIds == null) {
        matchingObjectIds = new TreeSet<ObjectId>();
        _definitionsByName.put(viewDefinition.getName(), matchingObjectIds);
      }
      
      // Add this view definition to set of matching object ids
      if (!matchingObjectIds.contains(viewDefinition.getUniqueId().getObjectId())) {
        matchingObjectIds.add(viewDefinition.getUniqueId().getObjectId());
        
        // First time this object id has been encountered - added not updated
        changeManager().entityChanged(ChangeType.ADDED, null, request.getViewDefinition().getUniqueId(), Instant.now());
      } else {
        
        // This object id has been seen before - updated not added
        changeManager().entityChanged(ChangeType.UPDATED, origUniqueId, request.getViewDefinition().getUniqueId(), Instant.now());
      }
  
      // Add to unique id index (replace existing duplicate if there)
      _definitionsByUniqueId.put(viewDefinition.getUniqueId(), viewDefinition);
      
    } finally {  
      // Release write lock
      _rwl.writeLock().unlock();
    }
    
    return viewDefinition.getUniqueId();
  }

  
  @Override
  public void removeViewDefinition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, UID_SCHEME);
    
    // Acquire write lock
    _rwl.writeLock().lock();
    
    try {
      // Remove from objectid index
      SortedSet<UniqueId> matchingUniqueIds = _definitionsByObjectId.get(uniqueId.getObjectId());
      
      if (matchingUniqueIds != null && matchingUniqueIds.contains(uniqueId)) {
        matchingUniqueIds.remove(uniqueId);
  
        // Was it the last one in _definitionsByObjectId?
        if (matchingUniqueIds.isEmpty()) {
          
          // remove the list
          _definitionsByObjectId.remove(uniqueId.getObjectId());
          
          // Remove from name index
          SortedSet<ObjectId> matchingObjectIds = _definitionsByName.get(getDefinition(uniqueId).getName());
          
          if (matchingObjectIds != null && matchingObjectIds.contains(uniqueId.getObjectId())) {
            matchingObjectIds.remove(uniqueId.getObjectId());
            
            // Was it the last one in _definitionsByName?
            if (matchingObjectIds.isEmpty()) {
              
              // remove the list
              _definitionsByName.remove(getDefinition(uniqueId).getName());
            }
            
          } else {
          //if (_definitionsByName.remove(getDefinition(uniqueId).getName()) == null) {
            throw new DataNotFoundException("View definition not found in name index: " + uniqueId);
          }
        }
  
        // Remove viewdef from unique id index
        if (_definitionsByUniqueId.remove(uniqueId) == null) {
          throw new DataNotFoundException("View definition not found in UniqueId index: " + uniqueId);
        }
        
        changeManager().entityChanged(ChangeType.REMOVED, uniqueId, null, Instant.now());
  
      } else {
        throw new DataNotFoundException("View definition not found in ObjectId index: " + uniqueId);
      }
      
    } finally {
      // Release write lock
      _rwl.writeLock().unlock();
    }
  }
  

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------

}

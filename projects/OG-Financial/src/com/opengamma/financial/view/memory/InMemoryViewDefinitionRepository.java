/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.time.Instant;

import com.opengamma.DataNotFoundException;
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
  
  private final ConcurrentMap<ObjectId, ViewDefinition> _definitionsByNewest = new ConcurrentSkipListMap<ObjectId, ViewDefinition>();
  private final ConcurrentMap<UniqueId, ViewDefinition> _definitionsByUniqueId = new ConcurrentSkipListMap<UniqueId, ViewDefinition>();
  private final ConcurrentMap<String, ViewDefinition> _definitionsByName = new ConcurrentSkipListMap<String, ViewDefinition>();
  private final ChangeManager _changeManager = new BasicChangeManager();
  
  //-------------------------------------------------------------------------
  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    if (definitionId.isVersioned()) {
      return _definitionsByUniqueId.get(definitionId);
    } else {
      return _definitionsByNewest.get(definitionId.getObjectId());
    }
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    return _definitionsByName.get(definitionName);
  }

  @Override
  public Set<UniqueId> getDefinitionIds() {
    return new TreeSet<UniqueId>(_definitionsByUniqueId.keySet());
  }
    
  public Map<UniqueId, String> getDefinitionEntries() {

    Map<UniqueId, String> result = new HashMap<UniqueId, String>();
    
    for (Map.Entry<UniqueId, ViewDefinition> entry : _definitionsByUniqueId.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getName());
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
    
    final ViewDefinition viewDefinition = request.getViewDefinition();
    final String originalName = request.getName();
    
    if (originalName.equals(viewDefinition.getName())) {
      // Same name - just replace
      if (_definitionsByName.replace(originalName, viewDefinition) == null) {
        throw new DataNotFoundException("View definition not found: " + originalName);
      } else if (_definitionsByUniqueId.replace(_definitionsByName.get(originalName).getUniqueId(), viewDefinition) == null) {
        throw new DataNotFoundException("View definition not found: " + originalName);
      }

      // If this is a newer version of an existing view definition, update latest version index too
      final ViewDefinition existingVersion = _definitionsByNewest.get(viewDefinition.getUniqueId().getObjectId());
      if (existingVersion == null) {
        // this is the first encountered instance of this ObjectId
        _definitionsByNewest.put(viewDefinition.getUniqueId().getObjectId(), viewDefinition);
      } else if (existingVersion.getUniqueId().getVersion().compareTo(viewDefinition.getUniqueId().getVersion()) < 0) {
        // this is a newer version than the one stored in the repository
        _definitionsByNewest.put(viewDefinition.getUniqueId().getObjectId(), viewDefinition);
      }
      
    } else {
      // Changing name - remove old, add new
      removeViewDefinition(_definitionsByName.get(originalName).getUniqueId());
      addViewDefinition(new AddViewDefinitionRequest(request.getViewDefinition()));
    }

    changeManager().entityChanged(ChangeType.UPDATED, _definitionsByName.get(originalName).getUniqueId(), request.getViewDefinition().getUniqueId(), Instant.now());
  }

  @Override
  public void addViewDefinition(AddViewDefinitionRequest request) {
    ArgumentChecker.notNull(request, "request");
    request.checkValid();
    
    final ViewDefinition viewDefinition = request.getViewDefinition();
        
    // Update indexes
    _definitionsByUniqueId.put(viewDefinition.getUniqueId(), viewDefinition);
    _definitionsByName.put(viewDefinition.getName(), viewDefinition);
    
    // If this is a newer version of an existing view definition, update latest version index too
    final ViewDefinition existingVersion = _definitionsByNewest.get(viewDefinition.getUniqueId().getObjectId());
    if (existingVersion == null) {
      // this is the first encountered instance of this ObjectId
      _definitionsByNewest.put(viewDefinition.getUniqueId().getObjectId(), viewDefinition);
    } else if (existingVersion.getUniqueId().getVersion().compareTo(viewDefinition.getUniqueId().getVersion()) < 0) {
      // this is a newer version than the one stored in the repository
      _definitionsByNewest.put(viewDefinition.getUniqueId().getObjectId(), viewDefinition);
    }
      
    changeManager().entityChanged(ChangeType.ADDED, null, request.getViewDefinition().getUniqueId(), Instant.now());
  }
  
  @Override
  public void removeViewDefinition(UniqueId definitionId) {
    ArgumentChecker.notNull(definitionId, UID_SCHEME);
    
    final ViewDefinition oldViewDef = _definitionsByUniqueId.remove(definitionId);
    
    if (oldViewDef == null) {   
      throw new DataNotFoundException("View definition not found in UniqueId index: " + definitionId);
    } else if (_definitionsByName.remove(oldViewDef.getName()) == null) {   
      throw new DataNotFoundException("View definition not found in name index: " + definitionId);
    } else if (_definitionsByNewest.remove(oldViewDef.getUniqueId().getObjectId()) == null) {
      throw new DataNotFoundException("View definition not found in ObjectId index: " + definitionId);
    }
    changeManager().entityChanged(ChangeType.REMOVED, definitionId, null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
  
  //-------------------------------------------------------------------------

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.memory;

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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, in-memory implementation of {@link ManageableViewDefinitionRepository}.
 */
public class InMemoryViewDefinitionRepository implements ManageableViewDefinitionRepository {

  private static final String UID_SCHEME = "MemViewDef";
  
  private final ConcurrentMap<String, ViewDefinition> _definitionsByName = new ConcurrentSkipListMap<String, ViewDefinition>();
  private final ChangeManager _changeManager = new BasicChangeManager();
  
  //-------------------------------------------------------------------------
  @Override
  public ViewDefinition getDefinition(String definitionName) {
    return _definitionsByName.get(definitionName);
  }

  @Override
  public Set<String> getDefinitionNames() {
    return new TreeSet<String>(_definitionsByName.keySet());
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean isModificationSupported() {
    return true;
  }
  
  @Override
  public void addViewDefinition(AddViewDefinitionRequest request) {
    ArgumentChecker.notNull(request, "request");
    request.checkValid();
    
    final ViewDefinition viewDefinition = request.getViewDefinition();
    _definitionsByName.put(viewDefinition.getName(), viewDefinition);
    changeManager().entityChanged(ChangeType.ADDED, null, getUniqueId(request.getViewDefinition().getName()), Instant.now());
  }
  
  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    ArgumentChecker.notNull(request, "request");
    request.checkValid();
    
    final String originalName = request.getName();
    if (originalName.equals(request.getViewDefinition().getName())) {
      // Same name - just replace
      if (_definitionsByName.replace(originalName, request.getViewDefinition()) == null) {
        throw new DataNotFoundException("View definition not found: " + originalName); 
      }
    } else {
      // Changing name - remove old, add new
      removeViewDefinition(originalName);
      addViewDefinition(new AddViewDefinitionRequest(request.getViewDefinition()));
    }
    changeManager().entityChanged(ChangeType.UPDATED, getUniqueId(originalName), getUniqueId(request.getViewDefinition().getName()), Instant.now());
  }

  @Override
  public void removeViewDefinition(String name) {
    if (_definitionsByName.remove(name) == null) {
      throw new DataNotFoundException("View definition not found: " + name);
    }
    changeManager().entityChanged(ChangeType.REMOVED, getUniqueId(name), null, Instant.now());
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
  
  //-------------------------------------------------------------------------
  private UniqueIdentifier getUniqueId(String definitionName) {
    // NOTE jonathan 2011-08-03 -- at the moment view definitions are identified to the engine by name, so this is an
    // intermediate solution
    return UniqueIdentifier.of(UID_SCHEME, definitionName);
  }

}

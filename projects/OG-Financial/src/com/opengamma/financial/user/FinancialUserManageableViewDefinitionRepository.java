/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Map;
import java.util.Set;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.UpdateViewDefinitionRequest;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

/**
 * Wraps a view definition repository to trap calls to record user based information to allow clean up and
 * hooks for access control logics if needed.
 */
public class FinancialUserManageableViewDefinitionRepository extends AbstractFinancialUserService implements ManageableViewDefinitionRepository {

  /**
   * The underlying master.
   */
  private final ManageableViewDefinitionRepository _underlying;

  /**
   * Creates an instance.
   * 
   * @param client  the client, not null
   * @param underlying  the underlying master, not null
   */
  public FinancialUserManageableViewDefinitionRepository(FinancialClient client, ManageableViewDefinitionRepository underlying) {
    super(client, FinancialUserDataType.VIEW_DEFINITION);
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId addViewDefinition(AddViewDefinitionRequest request) {
    UniqueId viewDefinitionId = _underlying.addViewDefinition(request);
    created(request.getViewDefinition().getUniqueId());
    
    return viewDefinitionId;
  }

  @Override
  public boolean isModificationSupported() {
    return _underlying.isModificationSupported();
  }

  @Override
  public void removeViewDefinition(UniqueId definitionId) {
    _underlying.removeViewDefinition(definitionId);
    deleted(definitionId);
  }

  @Override
  public void updateViewDefinition(UpdateViewDefinitionRequest request) {
    _underlying.updateViewDefinition(request);
  }

  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    return _underlying.getDefinition(definitionId);
  }

  @Override
  public ViewDefinition getDefinition(String name) {
    return _underlying.getDefinition(name);
  }

  @Override
  public Set<ObjectId> getDefinitionIds() {
    return _underlying.getDefinitionIds();
  }

  @Override
  public Map<UniqueId, String> getDefinitionEntries() {
    return _underlying.getDefinitionEntries();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _underlying.changeManager();
  }

}

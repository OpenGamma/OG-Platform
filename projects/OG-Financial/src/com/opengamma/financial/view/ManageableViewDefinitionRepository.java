/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;

/**
 * A view definition repository that can be managed. 
 */
public interface ManageableViewDefinitionRepository extends ViewDefinitionRepository {

  /**
   * Checks whether this view definition repository allows modification of the underlying data source.
   * 
   * @return <code>true</code> if the repository supports modification, <code>false</code> otherwise.
   */
  boolean isModificationSupported();
  
  //-------------------------------------------------------------------------
  /**
   * Adds a view definition to the repository.
   * 
   * @param request  the request, not null
   * @return the unique identifier of the view added, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  UniqueId addViewDefinition(AddViewDefinitionRequest request);
  
  /**
   * Updates a view definition in the repository.
   * 
   * @param request  the request, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if the view definition is not found
   */
  void updateViewDefinition(UpdateViewDefinitionRequest request);
  
  /**
   * Removes a view definition from the repository.
   * 
   * @param definitionId  the Id of the view definition to remove, not null
   * @throws DataNotFoundException if the view definition is not found
   */
  void removeViewDefinition(UniqueId definitionId);
  
}

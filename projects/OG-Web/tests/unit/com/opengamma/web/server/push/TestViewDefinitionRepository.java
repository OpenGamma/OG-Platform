package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.rest.ViewDefinitionEntriesResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Trivial implementation for testing {@link ViewDefinitionEntriesResource}.
 * TODO signature has changed significantly
 */
public class TestViewDefinitionRepository implements ViewDefinitionRepository {

  private final Map<UniqueId, String> _viewDefs;

  public TestViewDefinitionRepository(Map<String, String> viewDefs) {
    _viewDefs = new HashMap<UniqueId, String>();
    for (Map.Entry<String, String> entry : viewDefs.entrySet()) {
      _viewDefs.put(UniqueId.parse(entry.getKey()), entry.getValue());
    }
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("changeManager not implemented");
  }

  @Override
  public Set<ObjectId> getDefinitionIds() {
    throw new UnsupportedOperationException("getDefinitionIds not implemented");
  }

  @Override
  public Map<UniqueId, String> getDefinitionEntries() {
    return _viewDefs;
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    throw new UnsupportedOperationException("getDefinition not implemented");
  }

  @Override
  public ViewDefinition getDefinition(UniqueId definitionId) {
    throw new UnsupportedOperationException("getDefinition not implemented");
  }
}

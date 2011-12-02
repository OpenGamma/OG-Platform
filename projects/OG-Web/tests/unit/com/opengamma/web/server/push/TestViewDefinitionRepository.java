package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Trivial implementation for testing {@link ViewDefinitionNamesResource}.
 * TODO signature has changed significantly
 */
public class TestViewDefinitionRepository implements ViewDefinitionRepository {

  private final Set<String> _names;

  public TestViewDefinitionRepository(String... names) {
    _names = new HashSet<String>(Arrays.asList(names));
  }

  //@Override
  public Set<String> getDefinitionNames() {
    return _names;
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
    throw new UnsupportedOperationException("getDefinitionEntries not implemented");
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

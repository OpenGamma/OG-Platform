package com.opengamma.web.server.push;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Trivial implementation for testing {@link ViewDefinitionNamesResource}.
 */
public class TestViewDefinitionRepository implements ViewDefinitionRepository {

  private final Set<String> _names;

  public TestViewDefinitionRepository(String... names) {
    _names = new HashSet<String>(Arrays.asList(names));
  }

  @Override
  public Set<String> getDefinitionNames() {
    return _names;
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    throw new UnsupportedOperationException("getDefinition not implemented");
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("changeManager not implemented");
  }
}

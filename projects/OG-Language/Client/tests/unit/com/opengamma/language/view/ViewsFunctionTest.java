/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.view.AddViewDefinitionRequest;
import com.opengamma.financial.view.memory.InMemoryViewDefinitionRepository;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link ViewsFunction} class.
 */
@Test
public class ViewsFunctionTest {

  private ViewDefinitionRepository createRepository() {
    final InMemoryViewDefinitionRepository repository = new InMemoryViewDefinitionRepository();
    repository.addViewDefinition(new AddViewDefinitionRequest(new ViewDefinition(UniqueId.of("View", "1", "1"), "One", "Test")));
    repository.addViewDefinition(new AddViewDefinitionRequest(new ViewDefinition(UniqueId.of("View", "2", "1"), "Two", "Test")));
    repository.addViewDefinition(new AddViewDefinitionRequest(new ViewDefinition(UniqueId.of("View", "3", "1"), "Three", "Test")));
    return repository;
  }

  public void testAllViews() {
    final ViewDefinitionRepository repo = createRepository();
    final Map<UniqueId, String> result = ViewsFunction.invoke(repo, null);
    assertEquals(result.size(), 3);
  }

  public void testNamedViewPresent() {
    final ViewDefinitionRepository repo = createRepository();
    final Map<UniqueId, String> result = ViewsFunction.invoke(repo, "Two");
    assertEquals(result.size(), 1);
    assertTrue(result.keySet().contains(UniqueId.of("View", "2", "1")));
  }

  public void testNamedViewMissing() {
    final ViewDefinitionRepository repo = createRepository();
    final Map<UniqueId, String> result = ViewsFunction.invoke(repo, "Four");
    assertEquals(result.size(), 0);
  }

}

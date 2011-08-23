/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of a {@link DefinitionFilter} with default values that leave the definition unchanged. Change
 * the {@code identifierFilter} or {@code categoryFilter} attributes to change the behaviour, or subclass to override
 * the {@link #createDefinition} method.
 * 
 * @param <WireDefinition> the logical definition to be sent to a client (Fudge serializable), possibly a subclass
 * @param <MetaDefinition> possibly a subclass of {@code WireDefinition} providing additional information to a language specific filter
 */
public class DefaultDefinitionFilter<WireDefinition extends Definition, MetaDefinition extends WireDefinition> implements
    DefinitionFilter<WireDefinition, MetaDefinition> {

  private IdentifierFilter _identifierFilter = new DefaultIdentifierFilter();
  private CategoryFilter _categoryFilter = new DefaultCategoryFilter();

  public void setIdentifierFilter(final IdentifierFilter identifierFilter) {
    ArgumentChecker.notNull(identifierFilter, "identifierFilter");
    _identifierFilter = identifierFilter;
  }

  public IdentifierFilter getIdentifierFilter() {
    return _identifierFilter;
  }

  public void setCategoryFilter(final CategoryFilter categoryFilter) {
    ArgumentChecker.notNull(categoryFilter, "categoryFilter");
    _categoryFilter = categoryFilter;
  }

  public CategoryFilter getCategoryFilter() {
    return _categoryFilter;
  }

  /**
   * Applied to the logical definition after construction.
   * Return null to fail the construction.
   * 
   * @param logicalDefinition  the logical definition created (this can either be modified, or a substitute value returned)
   * @param fullDefinition  the full definition as originally passed to {@link #createDefinition}
   * @return the logical definition to send to a client (may be the {@code logicalDefinition} parameter)
   */
  protected WireDefinition updateDefinition(final WireDefinition logicalDefinition, final MetaDefinition fullDefinition) {
    return logicalDefinition;
  }

  // DefinitionFilter

  @SuppressWarnings("unchecked")
  @Override
  public final WireDefinition createDefinition(final MetaDefinition definition) {
    final String name = getIdentifierFilter().convertName(definition.getName());
    if (name == null) {
      return null;
    }
    final WireDefinition result = (WireDefinition) definition.clone();
    result.setName(name);
    final List<String> originalAliases = definition.getAlias();
    Collection<String> aliases = null;
    if (originalAliases != null) {
      int i = 0;
      for (final String originalAlias : originalAliases) {
        final String newAlias = getIdentifierFilter().convertAlias(name, originalAlias);
        if (!originalAlias.equals(newAlias)) {
          if (aliases == null) {
            aliases = new ArrayList<String>(originalAliases.size());
            final Iterator<String> previousAliases = originalAliases.iterator();
            while (i > 0) {
              aliases.add(previousAliases.next());
              i--;
            }
          }
          if (newAlias != null) {
            aliases.add(newAlias);
          }
        } else {
          if (aliases != null) {
            aliases.add(originalAlias);
          }
        }
        i++;
      }
      if (aliases != null) {
        // Aliases are different
        result.setAlias(aliases);
      }
    }
    result.setCategory(getCategoryFilter().convertCategory(definition.getCategory()));
    for (Parameter parameter : result.getParameter()) {
      final String newName = getIdentifierFilter().convertParameter(name, parameter.getName());
      parameter.setName(newName);
    }
    return updateDefinition(result, definition);
  }

}

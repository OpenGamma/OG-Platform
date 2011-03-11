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
   * Creates the logical definition from a full meta definition. The name, aliases and category will have already
   * been filtered, so these should be used instead of the values from the meta definition.
   * 
   * @param name the filtered name, not {@code null}
   * @param aliases the filtered aliases, {@code null} for none
   * @param category the filtered category, {@code null} for none
   * @param definition the full definition as originally provided
   * @return the logical definition to send to a client
   */
  @SuppressWarnings("unchecked")
  protected WireDefinition createDefinition(final String name, final Collection<String> aliases, final String category,
      final MetaDefinition definition) {
    final WireDefinition result = (WireDefinition) definition.clone();
    result.setName(name);
    result.setAlias(aliases);
    result.setCategory(category);
    return result;
  }

  // DefinitionFilter

  @Override
  public final WireDefinition createDefinition(final MetaDefinition definition) {
    final String name = getIdentifierFilter().convertName(definition.getName());
    if (name == null) {
      return null;
    }
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
          aliases.add(newAlias);
        } else {
          if (aliases != null) {
            aliases.add(originalAlias);
          }
        }
        i++;
      }
    }
    final String category = getCategoryFilter().convertCategory(definition.getCategory());
    return createDefinition(name, aliases, category, definition);
  }

}

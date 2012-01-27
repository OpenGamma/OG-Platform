/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.util.List;

import com.opengamma.language.definition.Definition;

/**
 * Partial implementation of {@link DefinitionExporter}.
 */
public abstract class AbstractDefinitionExporter implements DefinitionExporter {

  private static final String[] EMPTY = new String[0];

  protected void export(final String category, final String name, final String description, final String text) {
    // No-op
  }

  protected void exportAlias(final String category, final String alias, final String primary, final String description, final String text) {
    export(category, alias, description, text);
  }

  protected void exportPrimary(final String category, final String name, final String[] aliases, final String description, final String text) {
    export(category, name, description, text);
  }

  protected void export(final String category, final String name, final String[] aliases, final String description, final String text) {
    exportPrimary(category, name, aliases, description, text);
    for (String alias : aliases) {
      exportAlias(category, alias, name, description, text);
    }
  }

  protected String getCategory(final Definition definition) {
    return definition.getCategory();
  }

  protected String getPrimaryName(final Definition definition) {
    return definition.getName();
  }

  protected String[] getAliases(final Definition definition) {
    final List<String> aliasList = definition.getAlias();
    return (aliasList != null) ? (String[]) aliasList.toArray(EMPTY) : EMPTY;
  }

  protected String getDescription(final Definition definition) {
    return definition.getDescription();
  }

  // DefinitionExporter

  @Override
  public final void export(final Definition definition, final String text) {
    export(getCategory(definition), getPrimaryName(definition), getAliases(definition), getDescription(definition), text);
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.language.definition.Definition;

/**
 * Groups definition texts into categories as well as providing a single alphabetical list. 
 */
public class CategorizingDefinitionExporter extends AbstractDefinitionExporter {

  /**
   * Definition entry.
   */
  public final class Entry implements Comparable<Entry> {

    private final String _category;
    private final String _name;
    private final String _aliasFor;
    private final String _description;
    private final String _text;

    private Entry(final String category, final String name, final String aliasFor, final String description, final String text) {
      _category = category;
      _name = name;
      _aliasFor = aliasFor;
      _description = description;
      _text = text;
    }

    public String getCategory() {
      return _category;
    }

    public String getName() {
      return _name;
    }

    public String getAliasFor() {
      return _aliasFor;
    }

    public Entry getAliasForEntry() {
      return getEntry(getAliasFor());
    }

    public boolean isAlias() {
      return getAliasFor() != null;
    }

    public String getDescription() {
      return _description;
    }

    public String getText() {
      return _text;
    }

    @Override
    public int compareTo(final Entry e) {
      return getName().compareToIgnoreCase(e.getName());
    }

  }

  private final Map<String, List<Entry>> _entriesByCategory = new HashMap<String, List<Entry>>();
  private final Map<String, Entry> _entriesByName = new HashMap<String, Entry>();

  protected Entry getEntry(final String name) {
    return _entriesByName.get(name);
  }

  protected void exportEntry(final String category, final String name, final String aliasFor, final String description, final String text) {
    final Entry entry = new Entry(category, name, aliasFor, description, text);
    List<Entry> entries = _entriesByCategory.get(category);
    if (entries == null) {
      entries = new ArrayList<Entry>();
      _entriesByCategory.put(category, entries);
    }
    entries.add(entry);
    _entriesByName.put(name, entry);
  }

  @Override
  protected void exportAlias(final String category, final String alias, final String primary, final String description, final String text) {
    exportEntry(category, alias, primary, description, text);
  }

  @Override
  protected void exportPrimary(final String category, final String name, final String[] aliases, final String description, final String text) {
    exportEntry(category, name, null, description, text);
  }

  @Override
  protected String getCategory(final Definition definition) {
    final String category = super.getCategory(definition);
    return (category != null) ? category : "";
  }

  public List<String> getCategories() {
    final List<String> categories = new ArrayList<String>(_entriesByCategory.keySet());
    Collections.sort(categories);
    return categories;
  }

  public List<Entry> getEntries() {
    final List<Entry> entries = new ArrayList<Entry>(_entriesByName.values());
    Collections.sort(entries);
    return entries;
  }

  public List<Entry> getEntries(final String category) {
    final List<Entry> entries = new ArrayList<Entry>(_entriesByCategory.get(category));
    Collections.sort(entries);
    return entries;
  }

}

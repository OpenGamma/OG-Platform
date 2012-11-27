/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.opengamma.language.export.CategorizingDefinitionExporter.Entry;

/**
 * Exports pages for upload to a Wiki
 */
public class WikiPageExporter extends CategorizedPageExporter {

  /**
   * Page hook. This inverts the dependency on the Wiki API.
   */
  public interface WikiPageHook {

    void emitAToZPage(String content);

    void emitCategoryPage(String name, String content);

    void emitEntryPage(String name, String content);

  }

  private final Map<Entry, String> _entryPages = new HashMap<Entry, String>();
  private final Map<String, ByteArrayOutputStream> _pageContent = new HashMap<String, ByteArrayOutputStream>();

  private WikiPageHook _hook = new WikiPageHook() {

    @Override
    public void emitAToZPage(final String content) {
      System.out.println("### A-Z ###");
      System.out.println();
      System.out.println(content);
    }

    @Override
    public void emitCategoryPage(final String category, final String content) {
      System.out.println("### " + category + " ###");
      System.out.println();
      System.out.println(content);
    }

    @Override
    public void emitEntryPage(final String name, final String content) {
      System.out.println("### " + name + " ###");
      System.out.println();
      System.out.println(content);
    }

  };

  public WikiPageExporter(final CategorizingDefinitionExporter exporter) {
    super(exporter);
  }

  public void setWikiPageHook(final WikiPageHook hook) {
    _hook = hook;
  }

  public WikiPageHook getWikiPageHook() {
    return _hook;
  }

  @Override
  protected void writePageFooter(final PrintStream out, final String title) {
    out.println();
  }

  @Override
  protected PrintStream openAToZPage() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    return new PrintStream(baos) {
      @Override
      public void close() {
        _hook.emitAToZPage(baos.toString());
      }
    };
  }

  @Override
  protected void writeAToZPageHeader(final PrintStream out) {
    writePageHeader(out, "A-Z");
  }

  @Override
  protected void writeAToZTableHeader(final PrintStream out) {
    out.println("|| Name || Category || Description ||");
  }

  protected String pageAddressEntry(final Entry entry) {
    return _entryPages.get(entry) + "#" + entry.getName();
  }

  protected String hyperlinkEntry(final Entry entry) {
    return "[" + entry.getName() + "|" + pageAddressEntry(entry) + "]";
  }

  protected String prettyPrintCategory(final String category) {
    final StringBuilder sb = new StringBuilder().append(category.charAt(0));
    for (int i = 1; i < category.length(); i++) {
      if (Character.isUpperCase(category.charAt(i))) {
        sb.append(' ');
      }
      sb.append(category.charAt(i));
    }
    return sb.toString();
  }

  protected String pageAddressCategory(final String category) {
    return category;
  }

  protected String hyperlinkCategory(final String category) {
    return "[" + prettyPrintCategory(category) + "|" + pageAddressCategory(category) + "]";
  }

  protected String shortDescription(final String description) {
    final int dot = description.indexOf('.');
    if (dot < 0) {
      return description;
    } else {
      return description.substring(0, dot);
    }
  }

  @Override
  protected void writeAToZTableRow(final PrintStream out, final Entry entry) {
    out.println("| " + hyperlinkEntry(entry) + " | " + hyperlinkCategory(entry.getCategory()) + " | " + shortDescription(entry.getDescription()) + " |");
  }

  @Override
  protected void writeContentsPageHeader(final PrintStream out, final String category) {
    writePageHeader(out, prettyPrintCategory(category));
  }

  @Override
  protected void writeContentsTableHeader(final PrintStream out, final String category) {
    out.println("|| Name || Description ||");
  }

  @Override
  protected void writeContentsTableRow(final PrintStream out, final Entry entry) {
    out.println("| " + hyperlinkEntry(entry) + " | " + shortDescription(entry.getDescription()) + " |");
  }

  @Override
  protected PrintStream openCategoryPage(final String category) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    return new PrintStream(baos) {
      @Override
      public void close() {
        _hook.emitCategoryPage(pageAddressCategory(category), baos.toString());
      }
    };
  }

  @Override
  protected PrintStream openEntryPage(final Entry entry) {
    final String pageName = _entryPages.get(entry);
    ByteArrayOutputStream writer = _pageContent.get(pageName);
    final PrintStream ps;
    if (writer == null) {
      writer = new ByteArrayOutputStream();
      _pageContent.put(pageName, writer);
      ps = new PrintStream(writer);
    } else {
      ps = new PrintStream(writer);
    }
    return uncloseable(ps);
  }

  @Override
  protected void writeEntryPageHeader(final PrintStream out, final Entry entry) {
    out.println("{anchor:" + entry.getName() + "}");
    out.println("h2. " + entry.getName());
    out.println();
  }

  @Override
  protected void endWriteEntryPages() {
    for (Map.Entry<String, ByteArrayOutputStream> page : _pageContent.entrySet()) {
      _hook.emitEntryPage(page.getKey(), page.getValue().toString());
    }
  }

  @Override
  public void init() {
    final Map<String, Map<String, List<Entry>>> pages = new HashMap<String, Map<String, List<Entry>>>();
    for (Entry entry : getExporter().getEntries()) {
      Map<String, List<Entry>> page = pages.get(entry.getCategory());
      if (page == null) {
        page = new HashMap<String, List<Entry>>();
        final List<Entry> pageEntries = new LinkedList<Entry>();
        pageEntries.add(entry);
        page.put(null, pageEntries);
        pages.put(entry.getCategory(), page);
      } else {
        page.get(null).add(entry);
      }
    }
    // Split the pages up
    for (Map<String, List<Entry>> page : pages.values()) {
      paginate(page);
    }
    // Build the page mapping
    for (Map.Entry<String, Map<String, List<Entry>>> categoryPages : pages.entrySet()) {
      for (Map.Entry<String, List<Entry>> categoryPage : categoryPages.getValue().entrySet()) {
        if (categoryPage.getKey() != null) {
          for (Entry entry : categoryPage.getValue()) {
            _entryPages.put(entry, createPageName(categoryPages.getKey(), categoryPage.getKey()));
          }
        } else {
          for (Entry entry : categoryPage.getValue()) {
            _entryPages.put(entry, createPageName(entry));
          }
        }
      }
    }
  }

  protected String getPage(String name) {
    if (name.startsWith("Set") || name.startsWith("Get")) {
      name = name.substring(3);
    } else if (name.startsWith("Expand")) {
      name = name.substring(6);
    } else if (name.startsWith("Fetch") || name.startsWith("Store")) {
      name = name.substring(5);
    }
    return Character.toString(name.charAt(0));
  }

  protected void paginate(final Map<String, List<Entry>> pages) {
    final List<Entry> toCategorise = pages.get(null);
    for (Entry entry : toCategorise) {
      final String page = getPage(entry.getName());
      List<Entry> pageEntries = pages.get(page);
      if (pageEntries == null) {
        pageEntries = new ArrayList<Entry>();
        pages.put(page, pageEntries);
      }
      pageEntries.add(entry);
    }
    toCategorise.clear();
  }

  protected String createPageName(final String category, final String page) {
    return category + " " + page;
  }

  protected String createPageName(final Entry entry) {
    return entry.getName();
  }

}

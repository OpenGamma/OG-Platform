/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.io.PrintStream;
import java.util.List;

import com.opengamma.language.export.CategorizingDefinitionExporter.Entry;

/**
 * Exports pages containing the text along with content/index pages.
 */
public abstract class CategorizedPageExporter {

  private final CategorizingDefinitionExporter _exporter;
  private boolean _includeAliases;

  protected PrintStream uncloseable(final PrintStream ps) {
    return new PrintStream(ps) {
      @Override
      public void close() {
        // No-op
      }
    };
  }

  public CategorizedPageExporter(final CategorizingDefinitionExporter exporter) {
    _exporter = exporter;
  }

  public final void setIncludeAliases(final boolean includeAliases) {
    _includeAliases = includeAliases;
  }

  public final boolean isIncludeAliases() {
    return _includeAliases;
  }

  protected CategorizingDefinitionExporter getExporter() {
    return _exporter;
  }

  // Generic fallback

  protected void writePageHeader(final PrintStream out, final String title) {
    // No-op
  }

  protected void writeTableHeader(final PrintStream out, final String title) {
    // No-op
  }

  protected void writeTableRow(final PrintStream out, final Entry entry) {
    // No-op
  }

  protected void writeTableFooter(final PrintStream out, final String title) {
    // No-op
  }

  protected void writePageFooter(final PrintStream out, final String title) {
    // No-op
  }

  // A-Z page

  protected void writeAToZPageHeader(final PrintStream out) {
    writePageHeader(out, null);
  }

  protected void writeAToZTableHeader(final PrintStream out) {
    writeTableHeader(out, null);
  }

  protected void writeAToZTableRow(final PrintStream out, final Entry entry) {
    writeTableRow(out, entry);
  }

  protected void writeAToZTableFooter(final PrintStream out) {
    writeTableFooter(out, null);
  }

  protected void writeAToZPageFooter(final PrintStream out) {
    writePageFooter(out, null);
  }

  protected PrintStream openAToZPage() {
    System.out.println("### A-Z ###");
    return uncloseable(System.out);
  }

  public final void writeFullAToZPage() {
    final List<Entry> entries = getExporter().getEntries();
    final PrintStream ps = openAToZPage();
    writeAToZPageHeader(ps);
    writeAToZTableHeader(ps);
    for (Entry entry : entries) {
      writeAToZTableRow(ps, entry);
    }
    writeAToZTableFooter(ps);
    writeAToZPageFooter(ps);
    ps.close();
  }

  // Contents page

  protected void writeContentsPageHeader(final PrintStream out, final String category) {
    writePageHeader(out, category);
  }

  protected void writeContentsTableHeader(final PrintStream out, final String category) {
    writeTableHeader(out, category);
  }

  protected void writeContentsTableRow(final PrintStream out, final Entry entry) {
    writeTableRow(out, entry);
  }

  protected void writeContentsTableFooter(final PrintStream out, final String category) {
    writeTableFooter(out, category);
  }

  protected void writeContentsPageFooter(final PrintStream out, final String category) {
    writePageFooter(out, category);
  }

  private void writeCategoryContentsPage(final PrintStream out, final String category, final List<Entry> entries) {
    writeContentsPageHeader(out, category);
    writeContentsTableHeader(out, category);
    for (Entry entry : entries) {
      writeContentsTableRow(out, entry);
    }
    writeContentsTableFooter(out, category);
    writeContentsPageFooter(out, category);
  }

  protected PrintStream openCategoryPage(final String category) {
    System.out.println("### Content page " + category + " ###");
    return uncloseable(System.out);
  }

  public final void writeCategoryContentsPages() {
    final List<String> categories = getExporter().getCategories();
    for (String category : categories) {
      final PrintStream ps = openCategoryPage(category);
      writeCategoryContentsPage(ps, category, getExporter().getEntries(category));
      ps.close();
    }
  }

  // Entry pages

  protected void writeEntryPageHeader(final PrintStream out, final Entry entry) {
    writePageHeader(out, entry.getName());
  }

  protected void writeEntryPage(final PrintStream out, final String text) {
    out.print(text);
  }

  protected void writeEntryPageFooter(final PrintStream out, final Entry entry) {
    writePageFooter(out, entry.getName());
  }

  protected void writeEntryPage(final PrintStream out, final Entry entry) {
    writeEntryPageHeader(out, entry);
    writeEntryPage(out, entry.getText());
    writeEntryPageFooter(out, entry);
  }

  protected PrintStream openEntryPage(final Entry entry) {
    System.out.println("### Entry page " + entry.getName() + " ###");
    return uncloseable(System.out);
  }

  protected void endWriteEntryPages() {
    // No-op
  }

  public final void writeEntryPages() {
    final List<Entry> entries = getExporter().getEntries();
    for (Entry entry : entries) {
      if (!entry.isAlias() || isIncludeAliases()) {
        final PrintStream ps = openEntryPage(entry);
        writeEntryPage(ps, entry);
        ps.close();
      }
    }
    endWriteEntryPages();
  }

  public void init() {
    // No-op
  }

  public final void writePages() {
    writeFullAToZPage();
    writeCategoryContentsPages();
    writeEntryPages();
  }

}

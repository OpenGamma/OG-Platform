/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.io.PrintStream;

import com.opengamma.language.export.CategorizingDefinitionExporter.Entry;

/**
 * Exports pages for upload to a Wiki
 */
public class WikiPageExporter extends CategorizedPageExporter {

  public WikiPageExporter(final CategorizingDefinitionExporter exporter) {
    super(exporter);
  }

  // TODO: open the print streams to produce fragments on the filesystem

  @Override
  protected void writePageHeader(final PrintStream out, final String title) {
    out.println(".h1 " + title);
    out.println();
  }

  @Override
  protected void writePageFooter(final PrintStream out, final String title) {
    out.println();
  }

  @Override
  protected void writeAToZPageHeader(final PrintStream out) {
    writePageHeader(out, "A-Z");
  }

  @Override
  protected void writeAToZTableHeader(final PrintStream out) {
    out.println("|| Name || Category || Description ||");
  }

  protected String hyperlinkEntry(final Entry entry) {
    // TODO: hyperlink construct
    return entry.getName();
  }

  protected String hyperlinkCategory(final String category) {
    // TODO: hyperlink construct
    return category;
  }

  @Override
  protected void writeAToZTableRow(final PrintStream out, final Entry entry) {
    out.println("| " + hyperlinkEntry(entry) + " | " + hyperlinkCategory(entry.getCategory()) + " | " + entry.getDescription() + " |");
  }

  @Override
  protected void writeContentsPageHeader(final PrintStream out, final String category) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < category.length(); i++) {
      if (Character.isUpperCase(category.charAt(i))) {
        sb.append(' ');
      }
      sb.append(category.charAt(i));
    }
    sb.append(" Utilities");
    writePageHeader(out, sb.toString());
  }

  @Override
  protected void writeContentsTableHeader(final PrintStream out, final String category) {
    out.println("|| Name || Description ||");
  }

  @Override
  protected void writeContentsTableRow(final PrintStream out, final Entry entry) {
    out.println("| " + hyperlinkEntry(entry) + " | " + entry.getDescription() + " |");
  }

}

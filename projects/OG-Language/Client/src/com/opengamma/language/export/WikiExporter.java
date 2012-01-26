/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.context.SessionContext;

/**
 * Exporter service for constructing Wiki documentation for a client.
 */
public class WikiExporter extends Exporter {

  public WikiExporter(final SessionContext sessionContext) {
    super(sessionContext);
  }

  protected CategorizingDefinitionExporter getCategorizingDefinitionExporter() {
    return new CategorizingDefinitionExporter();
  }

  protected WikiDocumentationExporter getDocumentationExporter(final CategorizingDefinitionExporter underlying) {
    return new WikiDocumentationExporter(underlying);
  }

  protected WikiPageExporter getPageExporter(final CategorizingDefinitionExporter underlying) {
    return new WikiPageExporter(underlying);
  }

  @Override
  public void run() {
    final CategorizingDefinitionExporter defExporter = getCategorizingDefinitionExporter();
    final WikiDocumentationExporter docExporter = getDocumentationExporter(defExporter);
    docExporter.init();
    setExporter(docExporter);
    super.run();
    final WikiPageExporter pageExporter = getPageExporter(defExporter);
    pageExporter.init();
    pageExporter.writePages();
  }

}

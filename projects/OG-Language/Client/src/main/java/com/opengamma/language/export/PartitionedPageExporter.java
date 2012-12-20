/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.io.PrintStream;

import com.opengamma.language.export.CategorizingDefinitionExporter.Entry;

/**
 * Groups entries into larger pages subject to naming conventions.
 */
public abstract class PartitionedPageExporter extends CategorizedPageExporter {

  public PartitionedPageExporter(final CategorizingDefinitionExporter exporter) {
    super(exporter);
  }

  @Override
  protected final PrintStream openEntryPage(final Entry entry) {
    // request all entries
    // get a list of possible verbs
    // take the most regular one and use that to infer possible nouns
    // repeat until all are done
    System.out.println("### Entry page " + entry.getName() + " ###");
    return uncloseable(System.out);
  }

}

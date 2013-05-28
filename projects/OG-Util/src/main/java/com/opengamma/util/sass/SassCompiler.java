/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.sass;

import java.io.File;

/**
 * Compiler to convert scss/sass to css.
 */
public interface SassCompiler {
  
  /**
   * Transforms a sass content into css using Sass ruby engine.
   *
   * @param sass the Sass content to process.
   * @return the complied sass content.
   */
  String sassConvert(final String sass);
  
  /**
   * Updates out-of-date stylesheets.
   * <p>
   * Checks each Sass/SCSS file in templateDir to see if it’s been modified more recently than the corresponding CSS file in cssDir. 
   * If it has, it updates the CSS file.
   * 
   * @param templateDir the sass template directory.
   * @param cssDir the complied to css directory.
   */
  void updateStyleSheets(final File templateDir, final File cssDir);

}

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.sass;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class RubySassCompiler {
  
  private static final RubySassCompiler s_instance = new RubySassCompiler();
  
  private String _options;
  
  private RubySassCompiler() {
    File cacheLocation = new File(new File(System.getProperty("java.io.tmpdir")), "sass-cache");
    _options = ":syntax => :scss, :always_update => true, :style => :expanded, :cache_location => '" + cacheLocation.toString() + "'";
  }
  
  public static RubySassCompiler getInstance() {
    return s_instance;
  }

  private String buildUpdateStyleSheetsScript(final String templateDir, final String cssDir) {
    StringWriter raw = new StringWriter();
    PrintWriter script = new PrintWriter(raw);
    
    script.println("  require 'rubygems'                                                            ");
    script.println("  require 'sass/plugin'                                                         ");
    script.println("  require 'sass'                                                                ");
    script.println("  Sass::Plugin.options.merge!(" + _options + ")                                 ");
    script.println("  Sass::Plugin.add_template_location('" + templateDir + "', '" + cssDir + "')   ");
    script.println("  Sass::Plugin.update_stylesheets                                               ");
    script.flush();
    return raw.toString();
  }
    
  /**
   * Transforms a sass content into css using Sass ruby engine.
   *
   * @param sass the Sass content to process.
   * @return the complied sass content.
   */
  public String sassConvert(final String sass) {
    String css = StringUtils.EMPTY;
    if (!StringUtils.isEmpty(sass)) {
      Object result = executeRubyScript(buildSassConvertScript(sass));
      if (result != null) {
        css = result.toString();
      }
    }
    return css;
  }

  private Object executeRubyScript(final String script) {
    try {
      ScriptEngine rubyEngine = new ScriptEngineManager().getEngineByName("jruby");
      return rubyEngine.eval(script);
    } catch (final ScriptException e) {
      throw new OpenGammaRuntimeException(e.getMessage(), e);
    }
  }
  
  /**
   * Updates out-of-date stylesheets.
   * <p>
   * Checks each Sass/SCSS file in templateDir to see if it’s been modified more recently than the corresponding CSS file in cssDir. 
   * If it has, it updates the CSS file.
   * 
   * @param templateDir the sass template directory.
   * @param cssDir the complied to css directory.
   */
  public void updateStyleSheets(final File templateDir, final File cssDir) {
    ArgumentChecker.notNull(templateDir, "template directory");
    ArgumentChecker.notNull(cssDir, "css directory");
    executeRubyScript(buildUpdateStyleSheetsScript(templateDir.toString(), cssDir.toString()));
  }

  private String buildSassConvertScript(final String sass) {
    ArgumentChecker.notNull(sass, "sass content");
    final StringWriter raw = new StringWriter();
    final PrintWriter script = new PrintWriter(raw);

    script.println("  require 'rubygems'                                       ");
    script.println("  require 'sass/plugin'                                    ");
    script.println("  require 'sass/engine'                                    ");
    script.println("  source = '" + sass.replace("'", "\"") + "'               ");
    script.println("  engine = Sass::Engine.new(source, {" + _options + "})    ");
    script.println("  result = engine.render                                   ");
    script.flush();
    return raw.toString();
  }

}

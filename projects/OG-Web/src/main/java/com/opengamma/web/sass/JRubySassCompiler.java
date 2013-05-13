/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.sass;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils.EvalUnit;
import org.jruby.runtime.builtin.IRubyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ZipUtils;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Jruby sass compiler
 * 
 * <p> Uses Jruby complete and sass-gem to compile scss/sass to css. <p>
 */
public final class JRubySassCompiler {
  
  private static final Logger s_logger = LoggerFactory.getLogger(JRubySassCompiler.class);
  
  /**
   * Jruby GEMS_PATH
   */
  private static final File GEMS_PATH = getGemsPath();
  /**
   * Sass-gems resource path
   */
  private static final String SASS_GEM_RESOURCE = "gems/sass-lang-3.2.7.jar";
  
  static {
    extractRubySassGem();
  }
  
  private static final JRubySassCompiler s_instance = new JRubySassCompiler();
  
  private final String _options;
  private final ScriptingContainer _container;
  private final EvalUnit _sassConvertUnit;
  private final EvalUnit _updateStyleSheetsUnit;
 
  private JRubySassCompiler() {
    File cacheLocation = new File(new File(System.getProperty("java.io.tmpdir")), "sass-cache");
    _options = ":syntax => :scss, :always_update => true, :style => :expanded, :cache_location => '" + cacheLocation.toString() + "'";
    _container = new ScriptingContainer();
    _container.getProvider().getRubyInstanceConfig().setCompileMode(CompileMode.JIT);
    _sassConvertUnit = _container.parse(buildSassConvertScript());
    _updateStyleSheetsUnit = _container.parse(buildUpdateStyleSheetsScript());
  }
  
  private static void extractRubySassGem() {
    ClassLoader classLoader = JRubySassCompiler.class.getClassLoader();
    URL sassGemUrl = classLoader.getResource(SASS_GEM_RESOURCE);
    
    if (sassGemUrl == null) {
      throw new OpenGammaRuntimeException("Sass gem jar is missing in the classpath");
    }
    try {
      URLConnection urlConnection = sassGemUrl.openConnection();
      if (urlConnection instanceof JarURLConnection) {
        JarFile jarFile = ((JarURLConnection) urlConnection).getJarFile();
        ZipUtils.unzipArchive(jarFile, GEMS_PATH);
      } else {
        ZipUtils.unzipArchive(new File(sassGemUrl.toURI()), GEMS_PATH);
      }
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Error extracting Sass gem jar to " + GEMS_PATH.getPath(), ex);
    }
  }
  
  private static File getGemsPath() {
    return new File(FileUtils.getUserDirectoryPath(), ".opengamma" + java.io.File.separator + "gems");
  }

  public static JRubySassCompiler getInstance() {
    return s_instance;
  }

  private String buildUpdateStyleSheetsScript() {
    StringWriter raw = new StringWriter();
    PrintWriter script = new PrintWriter(raw);
       
    script.println("  ENV['GEM_PATH']='" + GEMS_PATH.getPath() + "'                ");
    script.println("  require 'rubygems'                                           ");
    script.println("  require 'sass/plugin'                                        ");
    script.println("  require 'sass'                                               ");
    script.println("  Sass::Plugin.options.merge!(" + _options + ")                ");
    script.println("  Sass::Plugin.add_template_location(@templateDir, @cssDir)    ");
    script.println("  Sass::Plugin.update_stylesheets                              ");
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
    String result = null;
    String input = StringUtils.trimToNull(sass);
    if (input != null) {
      s_logger.debug("<sass mode=raw>\n{}\n</sass>", sass);
      final OperationTimer timer = new OperationTimer(s_logger, "Sass text compilation");
      _container.put("@source", input);
      IRubyObject rubyResult = _sassConvertUnit.run();
      if (rubyResult != null) {
        result = rubyResult.asJavaString();
      }
      long duration = timer.finished();
      s_logger.debug("<sass mode=compiled duration={}ms>\n{}\n</sass>", duration, sass);
    }
    return result;
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
    
    _container.put("@templateDir", templateDir.toString());
    _container.put("@cssDir", cssDir.toString());
    _updateStyleSheetsUnit.run();
  }

  private String buildSassConvertScript() {
    final StringWriter raw = new StringWriter();
    final PrintWriter script = new PrintWriter(raw);

    script.println("  ENV['GEM_PATH']='" + GEMS_PATH.getPath() + "'            ");
    script.println("  require 'rubygems'                                       ");
    script.println("  require 'sass/plugin'                                    ");
    script.println("  require 'sass/engine'                                    ");
    script.println("  source = @source                                         ");
    script.println("  engine = Sass::Engine.new(source, {" + _options + "})    ");
    script.println("  result = engine.render                                   ");
    script.flush();
    return raw.toString();
  }

}

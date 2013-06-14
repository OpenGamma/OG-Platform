/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.generate.scripts;

import static com.google.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Generator that can produce command line scripts.
 * <p>
 * Scripts are normally identified based on the {@link Scriptable} annotation.
 */
public class ScriptsGenerator {

  private static final Logger s_logger = LoggerFactory.getLogger(ScriptsGenerator.class);

  /**
   * Generates the scripts.
   * 
   * @param scriptDir  the output directory to put the script in, not null
   * @param project  the project name, not null
   * @param className  the class name, not null
   */
  public static void generate(File scriptDir, String project, String className) {
    try {
      Configuration cfg = new Configuration();
      cfg.setObjectWrapper(new DefaultObjectWrapper());
      cfg.setClassForTemplateLoading(ScriptsGenerator.class, "");
      Map<String, Object> templateData = newHashMap();
      templateData.put("className", className);
      templateData.put("project", project.toLowerCase());
      Template winTemplate = cfg.getTemplate("script-template-win.ftl");
      generate(className, scriptDir, winTemplate, templateData, true);
      Template nixTemplate = cfg.getTemplate("script-template.ftl");
      generate(className, scriptDir, nixTemplate, templateData, false);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Generates the scripts.
   * 
   * @param className  the class name, not null
   * @param scriptDir  the output directory to put the script in, not null
   * @param template  the Freemarker template, not null
   * @param templateData  the lookup data injected into the template, not null
   * @param windows  true for Windows, false for Unix
   */
  public static void generate(String className, File scriptDir, Template template, Object templateData, boolean windows) {
    String scriptName = scriptName(className);
    File outputFile;
    if (windows) {
      outputFile = new File(scriptDir + File.separator + scriptName + ".bat");
    } else {
      outputFile = new File(scriptDir + File.separator + scriptName + ".sh");
    }
    writeScriptFile(outputFile, template, templateData);
  }

  /**
   * Calculates the script name.
   * 
   * @param className  the class name, not null
   * @return the script name, not null
   */
  private static String scriptName(String className) {
    StringUtils.substringBeforeLast(className, ".");  // strip package name
    String[] split = StringUtils.splitByCharacterTypeCamelCase(className);
    return StringUtils.join(split, '-').toLowerCase(Locale.ENGLISH);
  }

  /**
   * Writes the script using the Freemarker template.
   * 
   * @param outputFile  the file to write to, not null
   * @param template  the Freemarker template, not null
   * @param templateData  the lookup data injected into the template, not null
   */
  private static void writeScriptFile(File outputFile, Template template, Object templateData) {
    try {
      PrintWriter writer = new PrintWriter(outputFile);
      template.process(templateData, writer);
      writer.flush();
      writer.close();
      outputFile.setReadable(true, false);
      outputFile.setExecutable(true, false);
    } catch (IOException e) {
      s_logger.error("Error writing to output file", e);
      throw new OpenGammaRuntimeException("Error writing to output file", e);
    } catch (TemplateException e) {
      s_logger.error("Error writing to output file", e);
      throw new OpenGammaRuntimeException("Error writing to output file", e);
    }
  }

}

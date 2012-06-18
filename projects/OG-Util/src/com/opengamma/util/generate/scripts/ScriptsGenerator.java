/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.generate.scripts;

import static com.google.common.collect.Maps.newHashMap;
import static com.opengamma.util.functional.Functional.filter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.functional.Functional;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ScriptsGenerator {

  private static final Logger s_logger = LoggerFactory.getLogger(ScriptsGenerator.class);

  public static void generate(File scriptDir, String project, String className) {
    try {
      Configuration cfg = new Configuration();
      cfg.setObjectWrapper(new DefaultObjectWrapper());
      cfg.setClassForTemplateLoading(ScriptsGenerator.class, "");
      Map<String, Object> root = newHashMap();
      root.put("className", className);
      root.put("project", project.replaceFirst("(?i)og-", "").toLowerCase());
      //
      Template template = cfg.getTemplate("script-template.ftl");
      String scriptName = scriptName(className);
      File outputFile = new File(scriptDir + File.separator + scriptName + ".sh");
      writeScriptFile(outputFile, template, root);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String scriptName(String camelCase) {
    camelCase = camelCase.replaceFirst("^.*\\.", "");

    List<String> split = Functional.map(
      new ArrayList<String>(),
      filter(Arrays.asList(camelCase.split("(?=[A-Z])")), new Function1<String, Boolean>() {
        @Override
        public Boolean execute(String s) {
          return !s.equals("");
        }
      })
      , new Function1<String, String>() {
      @Override
      public String execute(String s) {
        return s.toLowerCase();
      }
    });
    return Joiner.on("-").join(split);
  }

  private static void writeScriptFile(File outputFile, Template template, Object data) {
    try {
      PrintWriter writer = new PrintWriter(outputFile);
      template.process(data, writer);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      s_logger.error("Error writing to output file", e);
      throw new BuildException("Error writing to output file", e);
    } catch (TemplateException e) {
      s_logger.error("Error writing to output file", e);
      throw new BuildException("Error writing to output file", e);
    }
  }
}

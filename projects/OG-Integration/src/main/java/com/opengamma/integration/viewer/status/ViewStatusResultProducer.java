/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.viewer.status.ViewStatusReporterOption.ResultFormat;
import com.opengamma.util.ArgumentChecker;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * View status result producer
 */
public class ViewStatusResultProducer {
  
  /**
   * The name of directory holding the freemarker templates.
   */
  private static final String FTL_LOCATION = "viewstatus-ftl";
  /**
   * The directory holding the freemamker templates.
   */
  private static final File FTL_DIRECTORY;
  static {
    URL resource = ViewStatusResultProducer.class.getClassLoader().getResource(FTL_LOCATION);
    if (resource == null) {
      throw new OpenGammaRuntimeException("File not found in classpath: " + FTL_LOCATION);
    }
    FTL_DIRECTORY = new File(resource.getFile());
  }
  
  public String statusResult(ViewStatusResultAggregator aggregator, ResultFormat format) {
    ArgumentChecker.notNull(aggregator, "aggregator");
    ArgumentChecker.notNull(format, "format");
    
    ViewStatusModel viewStatusModel = aggregator.aggregate(ViewAggregationType.CURRENCY, ViewAggregationType.SECURITY, ViewAggregationType.VALUE_REQUIREMENT_NAME);
 
    StringWriter stringWriter = new StringWriter();
    Configuration cfg = new Configuration();
    try {
      cfg.setDirectoryForTemplateLoading(FTL_DIRECTORY);
      String templateName = getTemplateName(format);
      Template template = cfg.getTemplate(templateName);
      Map<String, Object> input = new HashMap<String, Object>();
      input.put("viewStatus", viewStatusModel);
      template.process(input, stringWriter);
      stringWriter.flush();

    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error generating html format for View status report", e);
    } 
    return stringWriter.toString();
  }

  private String getTemplateName(ResultFormat format) {
    switch (format) {
      case HTML:
        return "view-status-html.ftl";
      case CSV:
        return "view-status-csv.ftl";
      case XML:
        return "view-status-xml.ftl";
      default:
        throw new OpenGammaRuntimeException("Unsupported format: " + format.name());
    }
  }

}

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import org.apache.commons.lang.StringUtils;

import com.opengamma.language.definition.Definition;
import com.opengamma.language.definition.Parameter;

/**
 * Wiki formatted documentation exporter
 */
public class WikiDocumentationExporter extends AbstractDocumentationExporter {

  protected static String sentence(final String text) {
    String s = StringUtils.capitalize(text);
    if (s.charAt(s.length() - 1) != '.') {
      return s + ".";
    } else {
      return s;
    }
  }

  protected String getFunctionTerminology() {
    return "function";
  }

  protected String getLiveDataTerminology() {
    return "live data connection";
  }

  protected String getProcedureTerminology() {
    return "procedure";
  }

  /**
   * Base Wiki documentation behavior.
   */
  public class WikiDocumenter<D extends Definition> extends Documenter<D> {

    @Override
    protected String getDescription(final D definition) {
      return sentence(definition.getDescription()) + "\n\n";
    }

    @Override
    protected String getParameterTableHeader(final D definition) {
      return "|| Parameter || Required || Description ||\n";
    }

    @Override
    protected String getParameterTableRow(final D definition, final Parameter parameter) {
      final StringBuilder sb = new StringBuilder();
      sb.append("| ").append(parameter.getName()).append(" | ");
      if (parameter.getRequired()) {
        sb.append("Yes ");
      }
      sb.append("| ").append(parameter.getDescription()).append(" |\n");
      return sb.toString();
    }

    @Override
    protected String getParameterTableFooter(final D definition) {
      return "\n";
    }

  }

  /**
   * Wiki function documentation behavior.
   */
  public class WikiFunctionDocumenter extends AbstractDocumenter<com.opengamma.language.function.Definition> {

    protected String getTerminology() {
      return getFunctionTerminology();
    }

    public WikiFunctionDocumenter(final Documenter<com.opengamma.language.function.Definition> base) {
      super(base);
    }

    @Override
    protected String getNoParameters(final com.opengamma.language.function.Definition definition) {
      return "This " + getTerminology() + " takes no parameters.\n\n";
    }

    protected String getOneResult(final com.opengamma.language.function.Definition definition) {
      // No-op
      return "";
    }

    protected String getNoResults(final com.opengamma.language.function.Definition definition) {
      // Can this happen?
      return "This " + getTerminology() + " does not return a value.\n\n";
    }

    protected String getMultipleResults(final com.opengamma.language.function.Definition definition, final int resultCount) {
      return "This " + getTerminology() + " returns " + resultCount + " values.\n\n";
    }

    @Override
    protected String getResult(final com.opengamma.language.function.Definition definition, final int resultCount) {
      if (resultCount != 1) {
        if (resultCount == 0) {
          return getNoResults(definition);
        } else {
          return getMultipleResults(definition, resultCount);
        }
      } else {
        return getOneResult(definition);
      }
    }

  }

  /**
   * Wiki live data documentation behavior.
   */
  public class WikiLiveDataDocumenter extends AbstractDocumenter<com.opengamma.language.livedata.Definition> {

    protected String getTerminology() {
      return getLiveDataTerminology();
    }

    public WikiLiveDataDocumenter(final Documenter<com.opengamma.language.livedata.Definition> base) {
      super(base);
    }

    @Override
    protected String getNoParameters(final com.opengamma.language.livedata.Definition definition) {
      return "This " + getTerminology() + " takes no parameters.\n\n";
    }

  }

  /**
   * Wiki procedure documentation behavior.
   */
  public class WikiProcedureDocumenter extends AbstractDocumenter<com.opengamma.language.procedure.Definition> {

    protected String getTerminology() {
      return getProcedureTerminology();
    }

    public WikiProcedureDocumenter(final Documenter<com.opengamma.language.procedure.Definition> base) {
      super(base);
    }

    @Override
    protected String getNoParameters(final com.opengamma.language.procedure.Definition definition) {
      return "This " + getTerminology() + " takes no parameters.\n\n";
    }

    protected String getOneResult(final com.opengamma.language.procedure.Definition definition) {
      return "This " + getTerminology() + " returns a single value.\n\n";
    }

    protected String getNoResults(final com.opengamma.language.procedure.Definition definition) {
      // Can this happen?
      return "This " + getTerminology() + " does not return a value.\n\n";
    }

    protected String getMultipleResults(final com.opengamma.language.procedure.Definition definition, final int resultCount) {
      return "This " + getTerminology() + " returns " + resultCount + " values.\n\n";
    }

    @Override
    protected String getResult(final com.opengamma.language.procedure.Definition definition, final int resultCount) {
      if (resultCount != 1) {
        if (resultCount == 0) {
          return getNoResults(definition);
        } else {
          return getMultipleResults(definition, resultCount);
        }
      } else {
        return getOneResult(definition);
      }
    }

  }

  public WikiDocumentationExporter(final DefinitionExporter exporter) {
    super(exporter);
  }

  @SuppressWarnings("unchecked")
  protected WikiDocumenter createBaseDocumenter() {
    return new WikiDocumenter();
  }

  @SuppressWarnings("unchecked")
  protected WikiFunctionDocumenter createFunctionDocumenter(final WikiDocumenter base) {
    return new WikiFunctionDocumenter(base);
  }

  @SuppressWarnings("unchecked")
  protected WikiLiveDataDocumenter createLiveDataDocumenter(final WikiDocumenter base) {
    return new WikiLiveDataDocumenter(base);
  }

  @SuppressWarnings("unchecked")
  protected WikiProcedureDocumenter createProcedureDocumenter(final WikiDocumenter base) {
    return new WikiProcedureDocumenter(base);
  }

  @SuppressWarnings("unchecked")
  protected void init() {
    final WikiDocumenter base = createBaseDocumenter();
    setFunctionDocumenter(createFunctionDocumenter(base));
    setLiveDataDocumenter(createLiveDataDocumenter(base));
    setProcedureDocumenter(createProcedureDocumenter(base));
  }

}

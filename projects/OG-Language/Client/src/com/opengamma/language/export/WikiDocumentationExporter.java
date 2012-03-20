/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
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

  protected String[] getWikiDocFolders() {
    return new String[] {"./wikiDoc" };
  }

  private static String[] removeFirst(final String[] ss) {
    final String[] ssNew = new String[ss.length - 1];
    System.arraycopy(ss, 1, ssNew, 0, ssNew.length);
    return ssNew;
  }

  protected String getWikiDoc(final String name, final String[] folders) {
    for (String folder : folders) {
      // TODO: if there are a lot of files, split the file system slightly
      final File f = new File(folder + File.separatorChar + name + ".txt");
      if (f.exists()) {
        final StringBuilder sb = new StringBuilder();
        try {
          final BufferedReader r = new BufferedReader(new FileReader(f));
          String s = r.readLine();
          while (s != null) {
            final String[] tokens = s.trim().split("\\s+");
            if (tokens.length > 0) {
              if ("!inherit".equals(tokens[0])) {
                sb.append(getWikiDoc(name, removeFirst(folders)));
                s = r.readLine();
                continue;
              } else if ("!copy".equals(tokens[0])) {
                for (int i = 1; i < tokens.length; i++) {
                  if (i > 1) {
                    sb.append('\n');
                  }
                  sb.append(getWikiDoc(tokens[i], folders));
                }
                s = r.readLine();
                continue;
              }
            }
            sb.append(s).append('\n');
            s = r.readLine();
          }
          r.close();
        } catch (IOException e) {
          throw new OpenGammaRuntimeException("Error building man page for " + name, e);
        }
        return sb.toString();
      }
    }
    return "";
  }

  /**
   * Base Wiki documentation behavior.
   */
  public class WikiDocumenter<D extends Definition> extends Documenter<D> {

    private final String[] _wikiDocFolders = getWikiDocFolders();

    @Override
    protected String getDescription(final D definition) {
      return sentence(definition.getDescription()).replaceAll("\n", "\n\n") + "\n\n";
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

    protected String getWikiDoc(final String name) {
      return WikiDocumentationExporter.this.getWikiDoc(name, _wikiDocFolders);
    }

    @Override
    protected String getBlurb(final D definition) {
      return getWikiDoc(definition.getName());
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

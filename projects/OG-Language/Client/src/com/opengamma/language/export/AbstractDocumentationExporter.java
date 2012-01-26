/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.util.List;

import com.opengamma.language.definition.Parameter;
import com.opengamma.util.ArgumentChecker;

/**
 * Produces structured definitions for documentation.
 */
public abstract class AbstractDocumentationExporter extends AbstractRepositoryExporter {

  /**
   * Documenter behavior base class.
   */
  public static class Documenter<D extends com.opengamma.language.definition.Definition> {

    protected String getDescription(final D definition) {
      // No-op
      return "";
    }

    protected String getNoParameters(final D definition) {
      return getParameterTableHeader(definition) + getParameterTableFooter(definition);
    }

    protected String getParameterTableHeader(final D definition) {
      // No-op
      return "";
    }

    protected String getParameterTableRow(final D definition, final Parameter parameter) {
      // No-op
      return "";
    }

    protected String getParameterTableFooter(final D definition) {
      // No-op
      return "";
    }

    private String getParameters(final D definition) {
      final List<Parameter> parameters = definition.getParameter();
      if ((parameters == null) || parameters.isEmpty()) {
        return getNoParameters(definition);
      } else {
        final StringBuilder sb = new StringBuilder();
        sb.append(getParameterTableHeader(definition));
        for (Parameter parameter : parameters) {
          sb.append(getParameterTableRow(definition, parameter));
        }
        sb.append(getParameterTableFooter(definition));
        return sb.toString();
      }
    }

    protected String getResult(final D definition, final int resultCount) {
      // No-op
      return "";
    }

    protected String getBlurb(final D definition) {
      return "";
    }

  }

  /**
   * Documenter behavior for sub-classes, delegating to an alternative instance. 
   */
  public abstract static class AbstractDocumenter<D extends com.opengamma.language.definition.Definition> extends Documenter<D> {

    private final Documenter<D> _underlying;

    public AbstractDocumenter(final Documenter<D> underlying) {
      ArgumentChecker.notNull(underlying, "underlying");
      _underlying = underlying;
    }

    private Documenter<D> getUnderlying() {
      return _underlying;
    }

    @Override
    protected String getDescription(final D definition) {
      return getUnderlying().getDescription(definition);
    }

    @Override
    protected String getParameterTableHeader(final D definition) {
      return getUnderlying().getParameterTableHeader(definition);
    }

    @Override
    protected String getParameterTableRow(final D definition, final Parameter parameter) {
      return getUnderlying().getParameterTableRow(definition, parameter);
    }

    @Override
    protected String getParameterTableFooter(final D definition) {
      return getUnderlying().getParameterTableFooter(definition);
    }

    @Override
    protected String getResult(final D definition, final int resultCount) {
      return getUnderlying().getResult(definition, resultCount);
    }

    @Override
    protected String getBlurb(final D definition) {
      return getUnderlying().getBlurb(definition);
    }

  }

  private Documenter<com.opengamma.language.function.Definition> _functionDocumenter = new Documenter<com.opengamma.language.function.Definition>();
  private Documenter<com.opengamma.language.livedata.Definition> _liveDataDocumenter = new Documenter<com.opengamma.language.livedata.Definition>();
  private Documenter<com.opengamma.language.procedure.Definition> _procedureDocumenter = new Documenter<com.opengamma.language.procedure.Definition>();

  public AbstractDocumentationExporter(final DefinitionExporter exporter) {
    super(exporter);
  }

  public void setFunctionDocumenter(final Documenter<com.opengamma.language.function.Definition> functionDocumenter) {
    ArgumentChecker.notNull(functionDocumenter, "functionDocumenter");
    _functionDocumenter = functionDocumenter;
  }

  public Documenter<com.opengamma.language.function.Definition> getFunctionDocumenter() {
    return _functionDocumenter;
  }

  public void setLiveDataDocumenter(final Documenter<com.opengamma.language.livedata.Definition> liveDataDocumenter) {
    ArgumentChecker.notNull(liveDataDocumenter, "liveDataDocumenter");
    _liveDataDocumenter = liveDataDocumenter;
  }

  public Documenter<com.opengamma.language.livedata.Definition> getLiveDataDocumenter() {
    return _liveDataDocumenter;
  }

  public void setProcedureDocumenter(final Documenter<com.opengamma.language.procedure.Definition> procedureDocumenter) {
    ArgumentChecker.notNull(procedureDocumenter, "procedureDocumenter");
    _procedureDocumenter = procedureDocumenter;
  }

  public Documenter<com.opengamma.language.procedure.Definition> getProcedureDocumenter() {
    return _procedureDocumenter;
  }

  @Override
  protected String getFunctionText(final com.opengamma.language.function.Definition definition) {
    if (definition.getDescription() == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(getFunctionDocumenter().getDescription(definition));
    sb.append(getFunctionDocumenter().getParameters(definition));
    sb.append(getFunctionDocumenter().getResult(definition, definition.getReturnCount()));
    sb.append(getFunctionDocumenter().getBlurb(definition));
    return sb.toString();
  }

  @Override
  protected String getLiveDataText(final com.opengamma.language.livedata.Definition definition) {
    if (definition.getDescription() == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(getLiveDataDocumenter().getDescription(definition));
    sb.append(getLiveDataDocumenter().getParameters(definition));
    sb.append(getLiveDataDocumenter().getBlurb(definition));
    return sb.toString();
  }

  @Override
  protected String getProcedureText(final com.opengamma.language.procedure.Definition definition) {
    if (definition.getDescription() == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(getProcedureDocumenter().getDescription(definition));
    sb.append(getProcedureDocumenter().getParameters(definition));
    sb.append(getProcedureDocumenter().getResult(definition, definition.getReturnCount()));
    sb.append(getProcedureDocumenter().getBlurb(definition));
    return sb.toString();
  }

}

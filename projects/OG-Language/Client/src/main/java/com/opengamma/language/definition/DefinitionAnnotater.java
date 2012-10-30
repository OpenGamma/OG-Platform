/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.livedata.MetaLiveData;
import com.opengamma.language.procedure.MetaProcedure;

/**
 * Renames parameters and applies descriptions to match a local environment. Use this with localized property files rather than
 * embed strings in the Java source code.
 */
public class DefinitionAnnotater {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String OBJECT = "_";

  private final Map<String, Map<String, String>> _info = new HashMap<String, Map<String, String>>();

  public DefinitionAnnotater(final Class<?> hostFunctionClass) {
    final ResourceBundle resource = ResourceBundle.getBundle(hostFunctionClass.getName());
    for (String key : resource.keySet()) {
      final String value = resource.getString(key);
      final int split = key.indexOf('.');
      final String element = key.substring(0, split);
      final String what = key.substring(split + 1);
      Map<String, String> info = _info.get(element);
      if (info == null) {
        info = new HashMap<String, String>();
        _info.put(element, info);
      }
      info.put(what, value);
    }
  }

  protected Map<String, String> getInfo(final String name) {
    return _info.get(name);
  }

  /**
   * Adjusts the parameter with information from the annotation file. E.g. renames the parameter to match a locale and adds a
   * description.
   * 
   * @param parameter parameter to annotate
   * @return the annotated parameter
   */
  public MetaParameter annotate(final MetaParameter parameter) {
    final Map<String, String> info = getInfo(parameter.getName());
    if (info == null) {
      // No annotations for the parameter
      return parameter;
    }
    String value = info.get(NAME);
    if (value != null) {
      parameter.setName(value);
    }
    value = info.get(DESCRIPTION);
    if (value != null) {
      parameter.setDescription(value);
    }
    return parameter;
  }

  public <T extends Collection<MetaParameter>> T annotate(final T parameters) {
    for (MetaParameter parameter : parameters) {
      annotate(parameter);
    }
    return parameters;
  }

  protected void annotateDefinition(final Definition definition) {
    Map<String, String> info = getInfo(OBJECT);
    if (info == null) {
      info = getInfo(OBJECT + definition.getName());
      if (info == null) {
        // No annotations for the definition      
        return;
      }
    }
    String value = info.get(NAME);
    if (value != null) {
      definition.setName(value);
    }
    value = info.get(DESCRIPTION);
    if (value != null) {
      definition.setDescription(value);
    }
  }

  /**
   * Adjusts the function with information from the annotation file. E.g. renames the function to match a locale and adds a
   * description.
   * 
   * @param function function to annotate
   * @return the annotated function
   */
  public MetaFunction annotate(final MetaFunction function) {
    annotateDefinition(function);
    return function;
  }

  /**
   * Adjusts the live data definition with information from the annotation file.
   * 
   * @param liveData live data definition to annotate
   * @return the annotated definition
   */
  public MetaLiveData annotate(final MetaLiveData liveData) {
    annotateDefinition(liveData);
    return liveData;
  }

  /**
   * Adjusts the procedure with information from the annotation file.
   * 
   * @param procedure procedure to annotate
   * @return the annotated definition
   */
  public MetaProcedure annotate(final MetaProcedure procedure) {
    annotateDefinition(procedure);
    return procedure;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;

/**
 * Loads configuration from the props/ini format file.
 * <p>
 * The format is line-based as follows:<br>
 *  <code>#</code> or <code>;</code> for comment lines (at the start of the line)<br>
 *  </code>${key} = value</code> declares a replacement for use later in the file<br>
 *  </code>${key}</code> is replaced by an earlier replacement declaration<br>
 *  </code>[group]</code> defines the start of a named group of configs<br>
 *  </code>key = value</code> defines a single config element within a group<br>
 *  Everything is trimmed as necessary.
 */
public class ComponentConfigLoader {

  /**
   * Starts the components defined in the specified resource.
   * <p>
   * The specified properties are simple key=value pairs and must not be surrounded with ${}.
   * 
   * @param resource  the config resource to load, not null
   * @param replacements  the default set of replacements, null ignored
   * @return the config, not null
   */
  public ComponentConfig load(Resource resource, Map<String, String> replacements) {
    replacements = adjustReplacements(Objects.firstNonNull(replacements, new HashMap<String, String>()));
    
    List<String> lines = readLines(resource);
    ComponentConfig config = new ComponentConfig();
    String group = "global";
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      if (line.startsWith("${") && line.substring(2).contains("=") &&
          StringUtils.substringBefore(line, "=").trim().endsWith("}")) {
        parseReplacement(line, replacements);
        
      } else {
        line = applyReplacements(line, replacements);
        
        if (line.startsWith("[") && line.endsWith("]")) {
          group = line.substring(1, line.length() - 1);
        
        } else {
          String key = StringUtils.substringBefore(line, "=").trim();
          String value = StringUtils.substringAfter(line, "=").trim();
          if (key.length() == 0) {
            throw new IllegalArgumentException("Invalid key, line " + lineNum);
          }
          config.add(group, key, value);
        }
      }
    }
    return config;
  }

  //-------------------------------------------------------------------------
  private Map<String, String> adjustReplacements(Map<String, String> input) {
    Map<String, String> map = new HashMap<String, String>();
    for (String key : input.keySet()) {
      map.put("${" + key + "}", input.get(key));
    }
    return map;
  }

  private void parseReplacement(String line, Map<String, String> properties) {
    String key = StringUtils.substringBefore(line, "=").trim();
    String value = StringUtils.substringAfter(line, "=").trim();
    properties.put(key, value);
  }

  private String applyReplacements(String line, Map<String, String> properties) {
    for (Entry<String, String> entry : properties.entrySet()) {
      line = StringUtils.replace(line, entry.getKey(), entry.getValue());
    }
    return line;
  }

  /**
   * Reads lines from the resource.
   * 
   * @param resource  the resource to read, not null
   * @return the lines, not null
   */
  private List<String> readLines(Resource resource) {
    try {
      return IOUtils.readLines(resource.getInputStream(), "UTF8");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read resource: " + resource, ex);
    }
  }

}

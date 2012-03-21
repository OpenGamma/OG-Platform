/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.elsql;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.opengamma.util.ArgumentChecker;

/**
 * A bundle of elsql formatted SQL.
 * <p>
 * The bundle encapsulates the SQL needed for a particular feature.
 * This will typically correspond to a data access object, or set of related tables.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ElSqlBundle {

  /**
   * The map of known elsql.
   */
  private final Map<String, NameSqlFragment> _map;
  /**
   * The config.
   */
  private final ElSqlConfig _config;

  /**
   * Loads external SQL based for the specified type.
   * <p>
   * This will load a file from the same package, with the ".elsql" extension
   * followed by one with the suffix "-$ConfigName.elsql".
   * 
   * @param config  the config, not null
   * @param type  the type, not null
   * @return the bundle, not null
   * @throws IllegalArgumentException if the input cannot be parsed
   */
  public static ElSqlBundle of(ElSqlConfig config, Class<?> type) {
    ArgumentChecker.notNull(config, "config");
    ArgumentChecker.notNull(type, "type");
    ClassPathResource baseResource = new ClassPathResource(type.getSimpleName() + ".elsql", type);
    ClassPathResource configResource = new ClassPathResource(type.getSimpleName() + "-" + config.getName() + ".elsql", type);
    return parse(config, baseResource, configResource);
  }

  /**
   * Parses a bundle from a resource locating a file, specify the config.
   * <p>
   * The config is designed to handle some, but not all, database differences.
   * 
   * @param config  the config to use, not null
   * @param resources  the resources to load, not null
   * @return the external identifier, not null
   * @throws IllegalArgumentException if the input cannot be parsed
   */
  public static ElSqlBundle parse(ElSqlConfig config, Resource... resources) {
    ArgumentChecker.notNull(config, "config");
    ArgumentChecker.notNull(resources, "resources");
    return parseResource(resources, config);
  }

  private static ElSqlBundle parseResource(Resource[] resources, ElSqlConfig config) {
    List<List<String>> files = new ArrayList<List<String>>();
    for (Resource resource : resources) {
      if (resource.exists()) {
        List<String> lines = loadResource(resource);
        files.add(lines);
      }
    }
    return parse(files, config);
  }

  // package scoped for testing
  static ElSqlBundle parse(List<String> lines) {
    ArrayList<List<String>> files = new ArrayList<List<String>>();
    files.add(lines);
    return parse(files, ElSqlConfig.DEFAULT);
  }

  private static ElSqlBundle parse(List<List<String>> files, ElSqlConfig config) {
    Map<String, NameSqlFragment> parsed = new LinkedHashMap<String, NameSqlFragment>();
    for (List<String> lines : files) {
      ElSqlParser parser = new ElSqlParser(lines);
      parsed.putAll(parser.parse());
    }
    return new ElSqlBundle(parsed, config);
  }

  private static List<String> loadResource(Resource resource) {
    InputStream in = null;
    try {
      in = resource.getInputStream();
      return IOUtils.readLines(in);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Creates an instance..
   * 
   * @param map  the map of names, not null
   * @param config  the config to use, not null
   */
  private ElSqlBundle(Map<String, NameSqlFragment> map, ElSqlConfig config) {
    if (map == null) {
      throw new IllegalArgumentException("Fragment map must not be null");
    }
    if (config == null) {
      throw new IllegalArgumentException("Config must not be null");
    }
    _map = map;
    _config = config;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config.
   * 
   * @return the config, not null
   */
  public ElSqlConfig getConfig() {
    return _config;
  }

  /**
   * Gets SQL for a named fragment key.
   * 
   * @param config  the new config, not null
   * @return a bundle with the config updated, not null
   */
  public ElSqlBundle withConfig(ElSqlConfig config) {
    return new ElSqlBundle(_map, config);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets SQL for a named fragment key, without specifying parameters.
   * <p>
   * Note that if the SQL contains tags that depend on variables, like AND or LIKE,
   * then an error will be thrown.
   * 
   * @param name  the name, not null
   * @return the SQL, not null
   * @throws IllegalArgumentException if there is no fragment with the specified name
   * @throws RuntimeException if a problem occurs
   */
  public String getSql(String name) {
    return getSql(name, new MapSqlParameterSource());
  }

  /**
   * Gets SQL for a named fragment key.
   * 
   * @param name  the name, not null
   * @param paramSource  the Spring SQL parameters, not null
   * @return the SQL, not null
   * @throws IllegalArgumentException if there is no fragment with the specified name
   * @throws RuntimeException if a problem occurs
   */
  public String getSql(String name, SqlParameterSource paramSource) {
    NameSqlFragment fragment = getFragment(name);
    StringBuilder buf = new StringBuilder(1024);
    fragment.toSQL(buf, this, paramSource);
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a fragment by name.
   * 
   * @param name  the name, not null
   * @return the fragment, not null
   * @throws IllegalArgumentException if there is no fragment with the specified name
   */
  NameSqlFragment getFragment(String name) {
    NameSqlFragment fragment = _map.get(name);
    if (fragment == null) {
      throw new IllegalArgumentException("Unknown fragment name: " + name);
    }
    return fragment;
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * Encapsulates the structure of a grid, and the mapping from output values to rows and columns.
 */
public class RequirementBasedGridStructure {

  private static final Logger s_logger = LoggerFactory.getLogger(RequirementBasedGridStructure.class);

  private final Map<UniqueIdentifier, Integer> _targetIdMap;
  private final List<WebViewGridColumn> _orderedColumns;
  private final Map<RequirementBasedColumnKey, WebViewGridColumn> _specificationBasedColumns;

  public RequirementBasedGridStructure(CompiledViewDefinition compiledViewDefinition, EnumSet<ComputationTargetType> targetTypes,
      List<RequirementBasedColumnKey> requirements, List<UniqueIdentifier> targets) {
    // This is complicated by the fact that requirements have constraints, while specifications have properties which
    // satisfy those constraints but are probably different (more complete). Further, we want to display one column
    // per requirement, which corresponds to more than one specification when there is more than one target type. 

    ValueSpecificationAnalysisResult analysisResult = analyseValueSpecifications(compiledViewDefinition, requirements, targetTypes, targets);
    Map<RequirementBasedColumnKey, WebViewGridColumn> specificationBasedColumns = new HashMap<RequirementBasedColumnKey, WebViewGridColumn>();
    Map<RequirementBasedColumnKey, WebViewGridColumn> requirementBasedColumns = new HashMap<RequirementBasedColumnKey, WebViewGridColumn>();
    List<WebViewGridColumn> orderedColumns = new ArrayList<WebViewGridColumn>();

    // Generate columns in correct order
    int colId = 0;
    for (RequirementBasedColumnKey requirement : requirements) {
      if (requirementBasedColumns.containsKey(requirement)) {
        continue;
      }
      // Not seen the requirement before - generate a column
      String columnHeader = getColumnHeader(requirement);
      String columnDescription = getColumnDescription(requirement);
      WebViewGridColumn column = new WebViewGridColumn(colId++, columnHeader, columnDescription, requirement.getValueName());
      requirementBasedColumns.put(requirement, column);
      orderedColumns.add(column);
    }

    for (Map.Entry<RequirementBasedColumnKey, RequirementBasedColumnKey> specToRequirement : analysisResult.getSpecificationToRequirement().entrySet()) {
      RequirementBasedColumnKey specificationBasedKey = specToRequirement.getKey();
      RequirementBasedColumnKey requirementBasedKey = specToRequirement.getValue();

      // Record the specification to column mapping
      WebViewGridColumn column = requirementBasedColumns.get(requirementBasedKey);
      if (column == null) {
        s_logger.warn("No column found for requirement {}", requirementBasedKey);
        continue;
      }
      specificationBasedColumns.put(specificationBasedKey, column);
    }

    _specificationBasedColumns = specificationBasedColumns;
    _orderedColumns = orderedColumns;

    // Order of targets could be important, so use a linked map
    if (targets == null) {
      targets = analysisResult.getTargets();
    }
    _targetIdMap = new LinkedHashMap<UniqueIdentifier, Integer>();
    int nextId = 0;
    for (UniqueIdentifier target : targets) {
      _targetIdMap.put(target, nextId++);
    }
  }

  private static ValueSpecificationAnalysisResult analyseValueSpecifications(CompiledViewDefinition compiledViewDefinition,
      Collection<RequirementBasedColumnKey> requirements, EnumSet<ComputationTargetType> targetTypes, List<UniqueIdentifier> targets) {
    Map<Pair<String, String>, Set<RequirementBasedColumnKey>> requirementsByConfigValueName = getRequirementsMap(requirements);
    Set<UniqueIdentifier> impliedTargets = targets == null ? new HashSet<UniqueIdentifier>() : null;
    Map<RequirementBasedColumnKey, Set<RequirementBasedColumnKey>> specificationsToRequirementCandidates = new HashMap<RequirementBasedColumnKey, Set<RequirementBasedColumnKey>>();
    Map<RequirementBasedColumnKey, RequirementBasedColumnKey> specToRequirement = new HashMap<RequirementBasedColumnKey, RequirementBasedColumnKey>();

    for (CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDefinition.getCompiledCalculationConfigurations()) {
      Set<RequirementBasedColumnKey> requirementsMatchedToSpec = new HashSet<RequirementBasedColumnKey>();
      String calcConfigName = compiledCalcConfig.getName();
      
      // Process each value specification, recording the requirement if a single one can be identified, or the set of
      // requirements if there is more than one candidate.
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications()) {
        if (!targetTypes.contains(valueSpec.getTargetSpecification().getType())) {
          // Not relevant
          continue;
        }

        if (impliedTargets != null) {
          impliedTargets.add(valueSpec.getTargetSpecification().getUniqueId());
        }

        String valueName = valueSpec.getValueName();
        ValueProperties valueProperties = valueSpec.getProperties();
        RequirementBasedColumnKey specificationBasedKey = new RequirementBasedColumnKey(calcConfigName, valueName, valueProperties);
        if (specToRequirement.containsKey(specificationBasedKey) || specificationsToRequirementCandidates.containsKey(specificationBasedKey)) {
          // Seen this specification before for a different target, so it has been / will be dealt with
          continue;
        }

        Set<RequirementBasedColumnKey> requirementsSatisfiedBySpec = findRequirementsSatisfiedBySpec(requirementsByConfigValueName, calcConfigName, valueSpec);
        if (requirementsSatisfiedBySpec.isEmpty()) {
          s_logger.warn("Could not find any original requirements satisfied by terminal value specification {}. Assuming this is an unwanted output, so ignoring.", valueSpec);
          continue;
        } else if (requirementsSatisfiedBySpec.size() == 1) {
          // The specification satisfies only one requirement, so we've found a definite match.
          RequirementBasedColumnKey requirementMatch = requirementsSatisfiedBySpec.iterator().next();
          specToRequirement.put(specificationBasedKey, requirementMatch);
          requirementsMatchedToSpec.add(requirementMatch);
        } else {
          // Cannot yet identify the requirement behind this specification. Store for later elimination.
          specificationsToRequirementCandidates.put(specificationBasedKey, requirementsSatisfiedBySpec);
        }
      }
      
      // Eliminate the requirements which have been identified as the cause of a specification from the candidates.
      for (Map.Entry<RequirementBasedColumnKey, Set<RequirementBasedColumnKey>> specificationToRequirementCandidates : specificationsToRequirementCandidates.entrySet()) {
        RequirementBasedColumnKey specificationBasedKey = specificationToRequirementCandidates.getKey();
        Set<RequirementBasedColumnKey> requirementCandidates = specificationToRequirementCandidates.getValue();
        requirementCandidates.removeAll(requirementsMatchedToSpec);
        
        if (requirementCandidates.size() == 0) {
          s_logger.warn("Eliminated all requirement candidates for specification {}, indicating an error in the algorithm. This specification will not map to a column.", specificationBasedKey);
          continue;
        }
        
        if (requirementCandidates.size() > 1) {
          // NOTE jonathan 2011-05-05 -- this will happen if there genuinely are multiple requirements satisfied by a
          // single value specification, where values would be duplicated across columns but only sent once in the
          // output. This is currently unsupported by the web client, but it would not be difficult to do by
          // allowing multiple columns to be associated with each specification.
          s_logger.warn("Failed to identify an individual requirement as the cause of specification {} appearing in" +
              " the dependency graph. Selecting a requirement arbitrarily from: {}", specificationBasedKey, requirementCandidates);
        }
        
        RequirementBasedColumnKey requirementBasedKey = requirementCandidates.iterator().next();
        specToRequirement.put(specificationBasedKey, requirementBasedKey);
      }
    }
    
    if (targets == null) {
      targets = new ArrayList<UniqueIdentifier>(impliedTargets);
    }

    return new ValueSpecificationAnalysisResult(specToRequirement, targets);
  }

  public boolean isEmpty() {
    return _specificationBasedColumns.isEmpty() || _targetIdMap.isEmpty();
  }

  public WebViewGridColumn getColumn(String calcConfigName, ValueSpecification valueSpec) {
    return _specificationBasedColumns.get(new RequirementBasedColumnKey(calcConfigName, valueSpec.getValueName(), valueSpec.getProperties()));
  }

  public List<WebViewGridColumn> getColumns() {
    return Collections.unmodifiableList(_orderedColumns);
  }

  public Map<UniqueIdentifier, Integer> getTargets() {
    return Collections.unmodifiableMap(_targetIdMap);
  }

  public Integer getRowId(UniqueIdentifier target) {
    return _targetIdMap.get(target);
  }

  //-------------------------------------------------------------------------
  private static String getColumnHeader(RequirementBasedColumnKey requirementBasedKey) {
    if ("default".equals(requirementBasedKey.getCalcConfigName().toLowerCase())) {
      return requirementBasedKey.getValueName();
    }
    return requirementBasedKey.getCalcConfigName() + "/" + requirementBasedKey.getValueName();
  }

  private static String getColumnDescription(RequirementBasedColumnKey requirementBasedKey) {
    return requirementBasedKey.getValueName() + ": \n" + getPropertiesString(requirementBasedKey.getValueProperties());
  }

  private static String getPropertiesString(ValueProperties constraints) {
    if (constraints.isEmpty()) {
      return "No constraints";
    }

    StringBuilder sb = new StringBuilder();
    boolean firstProperty = true;
    for (String propertyName : constraints.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
        continue;
      }
      if (firstProperty) {
        firstProperty = false;
      } else {
        sb.append("; \n");
      }
      sb.append(propertyName).append("=");
      Set<String> propertyValues = constraints.getValues(propertyName);
      if (propertyValues.size() == 0) {
        sb.append("[empty]");
      } else if (propertyValues.size() == 1) {
        sb.append(propertyValues.iterator().next());
      } else {
        sb.append("(");
        boolean firstValue = true;
        for (String propertyValue : propertyValues) {
          if (firstValue) {
            firstValue = false;
          } else {
            sb.append(", ");
          }
          sb.append(propertyValue);
        }
        sb.append(")");
      }
    }
    return sb.toString();
  }

  private static Map<Pair<String, String>, Set<RequirementBasedColumnKey>> getRequirementsMap(Collection<RequirementBasedColumnKey> requirements) {
    Map<Pair<String, String>, Set<RequirementBasedColumnKey>> result = new HashMap<Pair<String, String>, Set<RequirementBasedColumnKey>>();
    for (RequirementBasedColumnKey requirement : requirements) {
      Pair<String, String> requirementKey = Pair.of(requirement.getCalcConfigName(), requirement.getValueName());
      Set<RequirementBasedColumnKey> requirementsSet = result.get(requirementKey);
      if (requirementsSet == null) {
        requirementsSet = new HashSet<RequirementBasedColumnKey>();
        result.put(requirementKey, requirementsSet);
      }
      requirementsSet.add(requirement);
    }
    return result;
  }

  private static Set<RequirementBasedColumnKey> findRequirementsSatisfiedBySpec(Map<Pair<String, String>,
      Set<RequirementBasedColumnKey>> requirementsMap, String calcConfigName, ValueSpecification valueSpec) {
    Set<RequirementBasedColumnKey> requirementsSet = requirementsMap.get(Pair.of(calcConfigName, valueSpec.getValueName()));
    if (requirementsSet == null) {
      return Collections.emptySet();
    }
    Set<RequirementBasedColumnKey> matches = new HashSet<RequirementBasedColumnKey>();
    for (RequirementBasedColumnKey key : requirementsSet) {
      if (key.getValueProperties().isSatisfiedBy(valueSpec.getProperties())) {
        matches.add(key);
      }
    }
    return matches;
  }

  private static class ValueSpecificationAnalysisResult {

    private final Map<RequirementBasedColumnKey, RequirementBasedColumnKey> _specificationToRequirement;
    private final List<UniqueIdentifier> _targets;

    public ValueSpecificationAnalysisResult(Map<RequirementBasedColumnKey, RequirementBasedColumnKey> specificationToRequirement, List<UniqueIdentifier> targets) {
      _specificationToRequirement = specificationToRequirement;
      _targets = targets;
    }

    public Map<RequirementBasedColumnKey, RequirementBasedColumnKey> getSpecificationToRequirement() {
      return _specificationToRequirement;
    }

    public List<UniqueIdentifier> getTargets() {
      return _targets;
    }

  }

}

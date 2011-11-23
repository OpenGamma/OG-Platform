/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

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

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * Encapsulates the structure of a grid, and the mapping from output values to rows and columns.
 */
public class RequirementBasedGridStructure {

  private static final Logger s_logger = LoggerFactory.getLogger(RequirementBasedGridStructure.class);

  private final Map<ComputationTargetSpecification, Integer> _targetIdMap;
  private final List<WebViewGridColumn> _orderedColumns;
  private final Map<RequirementBasedColumnKey, Collection<WebViewGridColumn>> _specificationBasedColumns;
  private final Map<Integer, Set<Integer>> _unsatisfiedCells;

  public RequirementBasedGridStructure(CompiledViewDefinition compiledViewDefinition, EnumSet<ComputationTargetType> targetTypes,
      List<RequirementBasedColumnKey> requirements, List<ComputationTargetSpecification> targets) {
    ValueSpecificationAnalysisResult analysisResult = analyseValueSpecifications(compiledViewDefinition, requirements, targetTypes, targets);
    Map<RequirementBasedColumnKey, Collection<WebViewGridColumn>> specificationBasedColumns = new HashMap<RequirementBasedColumnKey, Collection<WebViewGridColumn>>();
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

    for (Map.Entry<RequirementBasedColumnKey, Collection<RequirementBasedColumnKey>> specToRequirement : analysisResult.getSpecificationToRequirements().entrySet()) {
      RequirementBasedColumnKey specificationBasedKey = specToRequirement.getKey();
      Collection<RequirementBasedColumnKey> requirementBasedKeys = specToRequirement.getValue();

      // Turn requirements into columns
      Collection<WebViewGridColumn> columns = specificationBasedColumns.get(specificationBasedKey);
      if (columns == null) {
        columns = new ArrayList<WebViewGridColumn>();
        specificationBasedColumns.put(specificationBasedKey, columns);
      }
      for (RequirementBasedColumnKey requirementBasedKey : requirementBasedKeys) {
        WebViewGridColumn column = requirementBasedColumns.get(requirementBasedKey);
        if (column == null) {
          s_logger.warn("No column found for requirement {}", requirementBasedKey);
          continue;
        }
        columns.add(column);
      }
    }

    _specificationBasedColumns = specificationBasedColumns;
    _orderedColumns = orderedColumns;

    // Order of targets could be important, so use a linked map
    if (targets == null) {
      targets = analysisResult.getTargets();
    }
    _targetIdMap = new LinkedHashMap<ComputationTargetSpecification, Integer>();
    _unsatisfiedCells = new HashMap<Integer, Set<Integer>>();
    int nextId = 0;
    for (ComputationTargetSpecification target : targets) {
      int targetRowId = nextId++;
      _targetIdMap.put(target, targetRowId);
      Set<RequirementBasedColumnKey> missingColumnKeys = analysisResult.getUnsatisfiedRequirements(target);
      if (missingColumnKeys == null) {
        continue;
      }
      IntSet missingColumnIds = new IntArraySet();
      for (RequirementBasedColumnKey requirementBasedKey : missingColumnKeys) {
        WebViewGridColumn missingGridColumn = requirementBasedColumns.get(requirementBasedKey);
        if (missingGridColumn == null) {
          continue;
        }
        missingColumnIds.add(missingGridColumn.getId());
      }
      _unsatisfiedCells.put(targetRowId, missingColumnIds);
    }
  }

  private static ValueSpecificationAnalysisResult analyseValueSpecifications(CompiledViewDefinition compiledViewDefinition,
      Collection<RequirementBasedColumnKey> requirements, EnumSet<ComputationTargetType> targetTypes, List<ComputationTargetSpecification> targets) {
    Map<Pair<String, String>, Set<RequirementBasedColumnKey>> requirementsByConfigValueName = getRequirementsMap(requirements);
    Set<ComputationTargetSpecification> impliedTargets = targets == null ? new HashSet<ComputationTargetSpecification>() : null;
    Map<RequirementBasedColumnKey, Set<RequirementBasedColumnKey>> specificationsToRequirementCandidates = new HashMap<RequirementBasedColumnKey, Set<RequirementBasedColumnKey>>();
    Map<RequirementBasedColumnKey, Collection<RequirementBasedColumnKey>> specToRequirements = new HashMap<RequirementBasedColumnKey, Collection<RequirementBasedColumnKey>>();
    Map<RequirementBasedColumnKey, Set<ComputationTargetSpecification>> specToTargets = new HashMap<RequirementBasedColumnKey, Set<ComputationTargetSpecification>>();

    for (CompiledViewCalculationConfiguration compiledCalcConfig : compiledViewDefinition.getCompiledCalculationConfigurations()) {
      Set<RequirementBasedColumnKey> requirementsMatchedToSpec = new HashSet<RequirementBasedColumnKey>();
      String calcConfigName = compiledCalcConfig.getName();
      
      // Process each value specification, recording the requirement if a single one can be identified, or the set of
      // requirements if there is more than one candidate.
      for (ValueSpecification valueSpec : compiledCalcConfig.getTerminalOutputSpecifications().keySet()) {
        if (!targetTypes.contains(valueSpec.getTargetSpecification().getType())) {
          // Not relevant
          continue;
        }

        if (impliedTargets != null) {
          impliedTargets.add(valueSpec.getTargetSpecification());
        }

        String valueName = valueSpec.getValueName();
        ValueProperties valueProperties = valueSpec.getProperties();
        RequirementBasedColumnKey specificationBasedKey = new RequirementBasedColumnKey(calcConfigName, valueName, valueProperties);
        
        Set<ComputationTargetSpecification> targetsForSpec = specToTargets.get(specificationBasedKey);
        if (targetsForSpec == null) {
          targetsForSpec = new HashSet<ComputationTargetSpecification>();
          specToTargets.put(specificationBasedKey, targetsForSpec);
        }
        targetsForSpec.add(valueSpec.getTargetSpecification());
        
        if (specToRequirements.containsKey(specificationBasedKey) || specificationsToRequirementCandidates.containsKey(specificationBasedKey)) {
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
          specToRequirements.put(specificationBasedKey, Collections.singleton(requirementMatch));
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
        
        // Specification must be present to satisfy all remaining requirement candidates
        specToRequirements.put(specificationBasedKey, requirementCandidates);
      }
    }
    
    if (targets == null) {
      targets = new ArrayList<ComputationTargetSpecification>(impliedTargets);
    }
    
    Map<ComputationTargetSpecification, Set<RequirementBasedColumnKey>> missingCellMap = generateCompleteMissingCellMap(targets, requirements);
    for (Map.Entry<RequirementBasedColumnKey, Set<ComputationTargetSpecification>> specToTargetsEntry : specToTargets.entrySet()) {
      RequirementBasedColumnKey spec = specToTargetsEntry.getKey();
      Collection<RequirementBasedColumnKey> requirementsForSpec = specToRequirements.get(spec);
      if (requirementsForSpec == null) {
        // No columns identified for spec
        continue;
      }
      Set<ComputationTargetSpecification> targetsForSpec = specToTargetsEntry.getValue();
      for (ComputationTargetSpecification targetForSpec : targetsForSpec) {
        Set<RequirementBasedColumnKey> requirementsForTarget = missingCellMap.get(targetForSpec);
        if (requirementsForTarget == null) {
          // Target not in grid
          continue;
        }
        requirementsForTarget.removeAll(requirementsForSpec);
      }
    }

    return new ValueSpecificationAnalysisResult(specToRequirements, targets, missingCellMap);
  }

  public boolean isEmpty() {
    return _specificationBasedColumns.isEmpty() || _targetIdMap.isEmpty();
  }

  public Collection<WebViewGridColumn> getColumns(String calcConfigName, ValueSpecification valueSpec) {
    return _specificationBasedColumns.get(new RequirementBasedColumnKey(calcConfigName, valueSpec.getValueName(), valueSpec.getProperties()));
  }

  public List<WebViewGridColumn> getColumns() {
    return Collections.unmodifiableList(_orderedColumns);
  }

  public Map<ComputationTargetSpecification, Integer> getTargets() {
    return Collections.unmodifiableMap(_targetIdMap);
  }

  public Integer getRowId(ComputationTargetSpecification target) {
    return _targetIdMap.get(target);
  }
  
  public Set<Integer> getUnsatisfiedCells(int rowId) {
    return _unsatisfiedCells.get(rowId);
  }

  //-------------------------------------------------------------------------
  private static String getColumnHeader(RequirementBasedColumnKey requirementBasedKey) {
    if ("default".equals(requirementBasedKey.getCalcConfigName().toLowerCase())) {
      return requirementBasedKey.getValueName();
    }
    return requirementBasedKey.getCalcConfigName() + "/" + requirementBasedKey.getValueName();
  }

  private static String getColumnDescription(RequirementBasedColumnKey requirementBasedKey) {
    return getPropertiesString(requirementBasedKey.getValueProperties());
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
      boolean isOptional = constraints.isOptional(propertyName);
      if (propertyValues.size() == 0) {
        sb.append("[empty]");
      } else if (propertyValues.size() == 1 && !isOptional) {
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
      if (isOptional) {
        sb.append("?");
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
  
  private static Map<ComputationTargetSpecification, Set<RequirementBasedColumnKey>> generateCompleteMissingCellMap(
      Collection<ComputationTargetSpecification> targets, Collection<RequirementBasedColumnKey> requirements) {
    Map<ComputationTargetSpecification, Set<RequirementBasedColumnKey>> result = new HashMap<ComputationTargetSpecification, Set<RequirementBasedColumnKey>>();
    for (ComputationTargetSpecification target : targets) {
      result.put(target, new HashSet<RequirementBasedColumnKey>(requirements));
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

    private final Map<RequirementBasedColumnKey, Collection<RequirementBasedColumnKey>> _specificationToRequirements;
    private final List<ComputationTargetSpecification> _targets;
    private final Map<ComputationTargetSpecification, Set<RequirementBasedColumnKey>> _unsatisfiedRequirementMap;

    public ValueSpecificationAnalysisResult(Map<RequirementBasedColumnKey,
        Collection<RequirementBasedColumnKey>> specificationToRequirements, List<ComputationTargetSpecification> targets,
        Map<ComputationTargetSpecification, Set<RequirementBasedColumnKey>> unsatisfiedRequirementMap) {
      _specificationToRequirements = specificationToRequirements;
      _targets = targets;
      _unsatisfiedRequirementMap = unsatisfiedRequirementMap;
    }

    public Map<RequirementBasedColumnKey, Collection<RequirementBasedColumnKey>> getSpecificationToRequirements() {
      return _specificationToRequirements;
    }

    public List<ComputationTargetSpecification> getTargets() {
      return _targets;
    }
    
    public Set<RequirementBasedColumnKey> getUnsatisfiedRequirements(ComputationTargetSpecification target) {
      return _unsatisfiedRequirementMap.get(target);
    }

  }

}

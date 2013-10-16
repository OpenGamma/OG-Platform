/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@link DependencyGraph}
 */
@GenericFudgeBuilderFor(DependencyGraph.class)
public class DependencyGraphFudgeBuilder implements FudgeBuilder<DependencyGraph> {

  // TODO: This is very inefficient at the moment

  private static final String CALCULATION_CONFIGURATION_NAME_FIELD = "calculationConfiguration";
  private static final String NODE_FIELD = "node";
  private static final String SIZE_FIELD = "size";
  private static final String TERMINAL_OUTPUT_FIELD = "terminal";
  private static final String VALUE_SPECIFICATION_FIELD = "specification";
  private static final String VALUE_REQUIREMENT_FIELD = "requirement";
  private static final String IDENTIFIER_FIELD = "id";
  private static final String FUNCTION_IDENTIFIER_FIELD = "function";
  private static final String FUNCTION_PARAMETERS_FIELD = "parameters";
  private static final String TARGET_FIELD = "target";
  private static final String INPUT_FIELD = "input";
  private static final String OUTPUT_FIELD = "output";
  private static final String VALUE_NAME_FIELD = "value";
  private static final String PROPERTIES_FIELD = "properties";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DependencyGraph depGraph) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(CALCULATION_CONFIGURATION_NAME_FIELD, null, depGraph.getCalculationConfigurationName());
    final Map<DependencyNode, Integer> nodeIdentifiers = new HashMap<DependencyNode, Integer>(depGraph.getSize());
    final int count = depGraph.getRootCount();
    for (int i = 0; i < count; i++) {
      final DependencyNode root = depGraph.getRootNode(i);
      addNodeToMessage(serializer, msg, root, nodeIdentifiers);
    }
    final int size = nodeIdentifiers.size();
    msg.add(SIZE_FIELD, null, size);
    // TODO: The value specifications are already written as part of the node data; refer to them as a node identifier and output index instead
    for (Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : depGraph.getTerminalOutputs().entrySet()) {
      final MutableFudgeMsg subMsg = msg.addSubMessage(TERMINAL_OUTPUT_FIELD, null);
      serializer.addToMessage(subMsg, VALUE_SPECIFICATION_FIELD, null, terminal.getKey());
      for (ValueRequirement requirement : terminal.getValue()) {
        serializer.addToMessage(subMsg, VALUE_REQUIREMENT_FIELD, null, requirement);
      }
    }
    return msg;
  }

  private void addNodeToMessage(final FudgeSerializer serializer, final MutableFudgeMsg msg, final DependencyNode node, final Map<DependencyNode, Integer> nodeIdentifiers) {
    Integer identifier = nodeIdentifiers.get(node);
    if (identifier != null) {
      msg.add(NODE_FIELD, null, identifier);
      return;
    }
    identifier = nodeIdentifiers.size();
    nodeIdentifiers.put(node, identifier);
    final MutableFudgeMsg nodeMsg = msg.addSubMessage(NODE_FIELD, null);
    nodeMsg.add(IDENTIFIER_FIELD, null, identifier);
    nodeMsg.add(FUNCTION_IDENTIFIER_FIELD, null, node.getFunction().getFunctionId());
    if (!EmptyFunctionParameters.INSTANCE.equals(node.getFunction().getParameters())) {
      serializer.addToMessageWithClassHeaders(nodeMsg, FUNCTION_PARAMETERS_FIELD, null, node.getFunction().getParameters(), FunctionParameters.class);
    }
    // TODO: Targets may be duplicated throughout the graph; could just refer to a previously written node that uses the same target
    serializer.addToMessage(nodeMsg, TARGET_FIELD, null, node.getTarget());
    int count = node.getInputCount();
    for (int i = 0; i < count; i++) {
      final ValueSpecification inputSpec = node.getInputValue(i);
      final MutableFudgeMsg inputMsg = nodeMsg.addSubMessage(INPUT_FIELD, null);
      inputMsg.add(VALUE_NAME_FIELD, inputSpec.getValueName());
      serializer.addToMessage(inputMsg, PROPERTIES_FIELD, null, inputSpec.getProperties());
      addNodeToMessage(serializer, inputMsg, node.getInputNode(i), nodeIdentifiers);
    }
    // TODO: Outputs will be duplicated; only *need* to write outputs that are not consumed by other nodes
    count = node.getOutputCount();
    for (int i = 0; i < count; i++) {
      final ValueSpecification outputSpec = node.getOutputValue(i);
      final MutableFudgeMsg outputMsg = nodeMsg.addSubMessage(OUTPUT_FIELD, null);
      outputMsg.add(VALUE_NAME_FIELD, outputSpec.getValueName());
      serializer.addToMessage(outputMsg, PROPERTIES_FIELD, null, outputSpec.getProperties());
    }
  }

  private DependencyNode getNodeFromField(final FudgeDeserializer deserializer, final FudgeField field, final DependencyNode[] nodes) {
    final Object fieldValue = field.getValue();
    if (fieldValue instanceof Number) {
      final int identifier = ((Number) fieldValue).intValue();
      assert nodes[identifier] != null;
      return nodes[identifier];
    }
    final FudgeMsg nodeMsg = (FudgeMsg) fieldValue;
    final String function = nodeMsg.getString(FUNCTION_IDENTIFIER_FIELD);
    final FudgeField parameters = nodeMsg.getByName(FUNCTION_PARAMETERS_FIELD);
    final ComputationTargetSpecification target = deserializer.fieldValueToObject(ComputationTargetReference.class, nodeMsg.getByName(TARGET_FIELD)).getSpecification();
    final Collection<FudgeField> outputFields = nodeMsg.getAllByName(OUTPUT_FIELD);
    final Collection<ValueSpecification> outputs = new ArrayList<ValueSpecification>(outputFields.size());
    for (FudgeField outputField : outputFields) {
      final FudgeMsg outputMsg = (FudgeMsg) outputField.getValue();
      final String valueName = outputMsg.getString(VALUE_NAME_FIELD);
      final ValueProperties properties = deserializer.fieldValueToObject(ValueProperties.class, outputMsg.getByName(PROPERTIES_FIELD));
      outputs.add(new ValueSpecification(valueName, target, properties));
    }
    final Collection<FudgeField> inputFields = nodeMsg.getAllByName(INPUT_FIELD);
    final Map<ValueSpecification, DependencyNode> inputs = Maps.newHashMapWithExpectedSize(inputFields.size());
    for (FudgeField inputField : inputFields) {
      final FudgeMsg inputMsg = (FudgeMsg) inputField.getValue();
      final DependencyNode inputNode = getNodeFromField(deserializer, inputMsg.getByName(NODE_FIELD), nodes);
      final String valueName = inputMsg.getString(VALUE_NAME_FIELD);
      final ValueProperties properties = deserializer.fieldValueToObject(ValueProperties.class, inputMsg.getByName(PROPERTIES_FIELD));
      inputs.put(new ValueSpecification(valueName, inputNode.getTarget(), properties), inputNode);
    }
    final DependencyNode node = new DependencyNodeImpl(DependencyNodeFunctionImpl.of(function, (parameters != null) ? deserializer.fieldValueToObject(FunctionParameters.class, parameters)
        : EmptyFunctionParameters.INSTANCE), target, outputs, inputs);
    nodes[nodeMsg.getInt(IDENTIFIER_FIELD)] = node;
    return node;
  }

  @Override
  public DependencyGraph buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_NAME_FIELD);
    final DependencyNode[] nodes = new DependencyNode[msg.getInt(SIZE_FIELD)];
    Collection<FudgeField> fields = msg.getAllByName(NODE_FIELD);
    final Collection<DependencyNode> roots = new ArrayList<DependencyNode>(fields.size());
    for (FudgeField field : fields) {
      roots.add(getNodeFromField(deserializer, field, nodes));
    }
    fields = msg.getAllByName(TERMINAL_OUTPUT_FIELD);
    final Map<ValueSpecification, Set<ValueRequirement>> terminals = Maps.newHashMapWithExpectedSize(fields.size());
    for (FudgeField field : fields) {
      final FudgeMsg fieldMsg = (FudgeMsg) field.getValue();
      final ValueSpecification valueSpec = deserializer.fieldValueToObject(ValueSpecification.class, fieldMsg.getByName(VALUE_SPECIFICATION_FIELD));
      final Collection<FudgeField> requirementFields = fieldMsg.getAllByName(VALUE_REQUIREMENT_FIELD);
      if (requirementFields.size() == 1) {
        final ValueRequirement valueReq = deserializer.fieldValueToObject(ValueRequirement.class, requirementFields.iterator().next());
        terminals.put(valueSpec, Collections.singleton(valueReq));
      } else {
        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(requirementFields.size());
        for (FudgeField requirementField : requirementFields) {
          requirements.add(deserializer.fieldValueToObject(ValueRequirement.class, requirementField));
        }
        terminals.put(valueSpec, requirements);
      }
    }
    return new DependencyGraphImpl(calcConfigName, roots, nodes.length, terminals);
  }

}

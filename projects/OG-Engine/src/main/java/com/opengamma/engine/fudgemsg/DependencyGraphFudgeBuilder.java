/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import com.opengamma.engine.depgraph.DependencyNodeFunction;
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
 * <p>
 * Nodes are all allocated an identifier. When a node is used by two or more other nodes, one will write the node content (including its identifier) as a sub-message and the other(s) will reference
 * the node with a numeric field value.
 * <p>
 * {@link ValueSpecification}s labeling graph edges are written as a value name and {@link ValueProperties} pair instead of a full value specification as the target is implied by the target of the
 * function. The inputs to a node are written in full - these imply the outputs of the node that provides them. Only outputs that are not consumed by other nodes (typically terminal outputs in a graph
 * that has had unnecessary values removed from it) are written for each node explicitly.
 */
@GenericFudgeBuilderFor(DependencyGraph.class)
public class DependencyGraphFudgeBuilder implements FudgeBuilder<DependencyGraph> {

  private static final Integer VISITED = Integer.valueOf(0);
  private static final ValueSpecification[] NO_OUTPUTS = new ValueSpecification[0];

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

  private static class MessageEncoder {

    private final FudgeSerializer _serializer;
    private final Map<DependencyNode, Integer> _nodeIdentifiers;
    private final Set<ValueSpecification> _unconsumedOutputs = new HashSet<ValueSpecification>();

    public MessageEncoder(final FudgeSerializer serializer, final int graphSize) {
      _serializer = serializer;
      _nodeIdentifiers = Maps.newHashMapWithExpectedSize(graphSize);
    }

    public MutableFudgeMsg encode(final DependencyGraph depGraph) {
      MutableFudgeMsg msg = _serializer.newMessage();
      msg.add(CALCULATION_CONFIGURATION_NAME_FIELD, null, depGraph.getCalculationConfigurationName());
      final int count = depGraph.getRootCount();
      for (int i = 0; i < count; i++) {
        final DependencyNode root = depGraph.getRootNode(i);
        findUnconsumedOutputs(root);
      }
      _nodeIdentifiers.clear();
      System.err.println("Writing " + _unconsumedOutputs.size() + " unconsumed output(s)");
      for (int i = 0; i < count; i++) {
        final DependencyNode root = depGraph.getRootNode(i);
        addNodeToMessage(msg, root);
      }
      final int size = _nodeIdentifiers.size();
      msg.add(SIZE_FIELD, null, size);
      // TODO: The value specifications are already written as part of the node data; refer to them as a node identifier and output index instead
      for (Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : depGraph.getTerminalOutputs().entrySet()) {
        final MutableFudgeMsg subMsg = msg.addSubMessage(TERMINAL_OUTPUT_FIELD, null);
        _serializer.addToMessage(subMsg, VALUE_SPECIFICATION_FIELD, null, terminal.getKey());
        for (ValueRequirement requirement : terminal.getValue()) {
          _serializer.addToMessage(subMsg, VALUE_REQUIREMENT_FIELD, null, requirement);
        }
      }
      return msg;
    }

    private void findUnconsumedOutputs(final DependencyNode node) {
      if (_nodeIdentifiers.put(node, VISITED) != null) {
        return;
      }
      int count = node.getInputCount();
      for (int i = 0; i < count; i++) {
        findUnconsumedOutputs(node.getInputNode(i));
        _unconsumedOutputs.remove(node.getInputValue(i));
      }
      count = node.getOutputCount();
      for (int i = 0; i < count; i++) {
        _unconsumedOutputs.add(node.getOutputValue(i));
      }
    }

    private void addNodeToMessage(final MutableFudgeMsg msg, final DependencyNode node) {
      Integer identifier = _nodeIdentifiers.get(node);
      if (identifier != null) {
        msg.add(NODE_FIELD, null, identifier);
        return;
      }
      identifier = _nodeIdentifiers.size();
      _nodeIdentifiers.put(node, identifier);
      final MutableFudgeMsg nodeMsg = msg.addSubMessage(NODE_FIELD, null);
      nodeMsg.add(IDENTIFIER_FIELD, null, identifier);
      nodeMsg.add(FUNCTION_IDENTIFIER_FIELD, null, node.getFunction().getFunctionId());
      if (!EmptyFunctionParameters.INSTANCE.equals(node.getFunction().getParameters())) {
        _serializer.addToMessageWithClassHeaders(nodeMsg, FUNCTION_PARAMETERS_FIELD, null, node.getFunction().getParameters(), FunctionParameters.class);
      }
      // TODO: Targets may be duplicated throughout the graph; could just refer to a previously written node that uses the same target
      _serializer.addToMessage(nodeMsg, TARGET_FIELD, null, node.getTarget());
      int count = node.getInputCount();
      for (int i = 0; i < count; i++) {
        final ValueSpecification inputSpec = node.getInputValue(i);
        final MutableFudgeMsg inputMsg = nodeMsg.addSubMessage(INPUT_FIELD, null);
        inputMsg.add(VALUE_NAME_FIELD, inputSpec.getValueName());
        _serializer.addToMessage(inputMsg, PROPERTIES_FIELD, null, inputSpec.getProperties());
        addNodeToMessage(inputMsg, node.getInputNode(i));
      }
      count = node.getOutputCount();
      for (int i = 0; i < count; i++) {
        final ValueSpecification outputSpec = node.getOutputValue(i);
        if (_unconsumedOutputs.contains(outputSpec)) {
          final MutableFudgeMsg outputMsg = nodeMsg.addSubMessage(OUTPUT_FIELD, null);
          outputMsg.add(VALUE_NAME_FIELD, outputSpec.getValueName());
          _serializer.addToMessage(outputMsg, PROPERTIES_FIELD, null, outputSpec.getProperties());
        }
      }
    }

  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DependencyGraph depGraph) {
    final MessageEncoder encoder = new MessageEncoder(serializer, depGraph.getSize());
    return encoder.encode(depGraph);
  }

  private static class TempDependencyNode {

    private final DependencyNodeFunction _function;
    private final ComputationTargetSpecification _target;
    private final ValueSpecification[] _inputValues;
    private final TempDependencyNode[] _inputNodes;
    private ValueSpecification[] _outputValues;
    private DependencyNode _node;

    public TempDependencyNode(final DependencyNodeFunction function, final ComputationTargetSpecification target, final ValueSpecification[] outputValues, final ValueSpecification[] inputValues,
        final TempDependencyNode[] inputNodes) {
      _function = function;
      _target = target;
      _outputValues = outputValues;
      _inputNodes = inputNodes;
      _inputValues = inputValues;
    }

    public ComputationTargetSpecification getTarget() {
      return _target;
    }

    public void addOutputValue(final ValueSpecification output) {
      for (ValueSpecification existing : _outputValues) {
        if (output.equals(existing)) {
          return;
        }
      }
      final ValueSpecification[] newOutputs = new ValueSpecification[_outputValues.length + 1];
      System.arraycopy(_outputValues, 0, newOutputs, 0, _outputValues.length);
      newOutputs[_outputValues.length] = output;
      _outputValues = newOutputs;
    }

    public DependencyNode toNode() {
      if (_node == null) {
        final DependencyNode[] inputNodes = new DependencyNode[_inputNodes.length];
        for (int i = 0; i < inputNodes.length; i++) {
          final TempDependencyNode input = _inputNodes[i];
          // Allow the GC to get rid of the temporary data as soon as possible
          _inputNodes[i] = null;
          inputNodes[i] = input.toNode();
        }
        _node = DependencyNodeImpl.of(_function, _target, _outputValues, _inputValues, inputNodes);
      }
      return _node;
    }

  }

  private static class MessageDecoder {

    private final FudgeDeserializer _deserializer;
    private TempDependencyNode[] _nodes;

    public MessageDecoder(final FudgeDeserializer deserializer) {
      _deserializer = deserializer;
    }

    private TempDependencyNode getNodeFromField(final FudgeField field) {
      final Object fieldValue = field.getValue();
      if (fieldValue instanceof Number) {
        final int identifier = ((Number) fieldValue).intValue();
        assert _nodes[identifier] != null;
        return _nodes[identifier];
      }
      final FudgeMsg nodeMsg = (FudgeMsg) fieldValue;
      final String function = nodeMsg.getString(FUNCTION_IDENTIFIER_FIELD);
      final FudgeField parameters = nodeMsg.getByName(FUNCTION_PARAMETERS_FIELD);
      final ComputationTargetSpecification target = _deserializer.fieldValueToObject(ComputationTargetReference.class, nodeMsg.getByName(TARGET_FIELD)).getSpecification();
      final Collection<FudgeField> outputFields = nodeMsg.getAllByName(OUTPUT_FIELD);
      final ValueSpecification[] outputs;
      if (outputFields.isEmpty()) {
        outputs = NO_OUTPUTS;
      } else {
        outputs = new ValueSpecification[outputFields.size()];
        int i = 0;
        for (FudgeField outputField : outputFields) {
          final FudgeMsg outputMsg = (FudgeMsg) outputField.getValue();
          final String valueName = outputMsg.getString(VALUE_NAME_FIELD);
          final ValueProperties properties = _deserializer.fieldValueToObject(ValueProperties.class, outputMsg.getByName(PROPERTIES_FIELD));
          outputs[i++] = new ValueSpecification(valueName, target, properties);
        }
      }
      final Collection<FudgeField> inputFields = nodeMsg.getAllByName(INPUT_FIELD);
      final ValueSpecification[] inputValues = new ValueSpecification[inputFields.size()];
      final TempDependencyNode[] inputNodes = new TempDependencyNode[inputFields.size()];
      int i = 0;
      for (FudgeField inputField : inputFields) {
        final FudgeMsg inputMsg = (FudgeMsg) inputField.getValue();
        final TempDependencyNode inputNode = getNodeFromField(inputMsg.getByName(NODE_FIELD));
        final String valueName = inputMsg.getString(VALUE_NAME_FIELD);
        final ValueProperties properties = _deserializer.fieldValueToObject(ValueProperties.class, inputMsg.getByName(PROPERTIES_FIELD));
        final ValueSpecification value = new ValueSpecification(valueName, inputNode.getTarget(), properties);
        inputNode.addOutputValue(value);
        inputValues[i] = value;
        inputNodes[i++] = inputNode;
      }
      final TempDependencyNode node = new TempDependencyNode(DependencyNodeFunctionImpl.of(function, (parameters != null) ? _deserializer.fieldValueToObject(FunctionParameters.class, parameters)
          : EmptyFunctionParameters.INSTANCE), target, outputs, inputValues, inputNodes);
      _nodes[nodeMsg.getInt(IDENTIFIER_FIELD)] = node;
      return node;
    }

    private Collection<DependencyNode> getRoots(final FudgeMsg msg, final int size) {
      _nodes = new TempDependencyNode[size];
      Collection<FudgeField> fields = msg.getAllByName(NODE_FIELD);
      final TempDependencyNode[] roots = new TempDependencyNode[fields.size()];
      int i = 0;
      for (FudgeField field : fields) {
        roots[i++] = getNodeFromField(field);
      }
      // Allow the GC to get rid of the temporary data as soon as possible
      _nodes = null;
      final Collection<DependencyNode> rootNodes = new ArrayList<DependencyNode>(roots.length);
      for (i = 0; i < roots.length; i++) {
        final TempDependencyNode node = roots[i];
        // Allow the GC to get rid of the temporary data as soon as possible
        roots[i] = null;
        rootNodes.add(node.toNode());
      }
      return rootNodes;
    }

    public DependencyGraph decode(final FudgeMsg msg) {
      final int size = msg.getInt(SIZE_FIELD);
      final String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_NAME_FIELD);
      final Collection<DependencyNode> roots = getRoots(msg, size);
      Collection<FudgeField> fields = msg.getAllByName(TERMINAL_OUTPUT_FIELD);
      final Map<ValueSpecification, Set<ValueRequirement>> terminals = Maps.newHashMapWithExpectedSize(fields.size());
      for (FudgeField field : fields) {
        final FudgeMsg fieldMsg = (FudgeMsg) field.getValue();
        final ValueSpecification valueSpec = _deserializer.fieldValueToObject(ValueSpecification.class, fieldMsg.getByName(VALUE_SPECIFICATION_FIELD));
        final Collection<FudgeField> requirementFields = fieldMsg.getAllByName(VALUE_REQUIREMENT_FIELD);
        if (requirementFields.size() == 1) {
          final ValueRequirement valueReq = _deserializer.fieldValueToObject(ValueRequirement.class, requirementFields.iterator().next());
          terminals.put(valueSpec, Collections.singleton(valueReq));
        } else {
          final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(requirementFields.size());
          for (FudgeField requirementField : requirementFields) {
            requirements.add(_deserializer.fieldValueToObject(ValueRequirement.class, requirementField));
          }
          terminals.put(valueSpec, requirements);
        }
      }
      return new DependencyGraphImpl(calcConfigName, roots, size, terminals);
    }

  }

  @Override
  public DependencyGraph buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final MessageDecoder decoder = new MessageDecoder(deserializer);
    return decoder.decode(msg);
  }

}

/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of a {@link DependencyNode}.
 */
public class DependencyNodeImpl implements DependencyNode, Serializable {

  // TODO: Change DependencyNode from an interface to an abstract class and put the static stuff into it rather than have static methods & instanceof checks here

  private static final long serialVersionUID = 1L;

  private static final DependencyNode[] EMPTY_NODE_ARRAY = new DependencyNode[0];
  private static final ValueSpecification[] EMPTY_SPECIFICATION_ARRAY = new ValueSpecification[0];

  /**
   * The target specification.
   */
  private final ComputationTargetSpecification _target;

  /**
   * The function and parameters.
   */
  private DependencyNodeFunction _function;

  /**
   * The values consumed by this node. These can be considered as the labels on input edges. The length of this array and order matches {@link #_inputNodes}.
   */
  private final ValueSpecification[] _inputValues;

  /**
   * The nodes that produce values consumed by this node. The length of this array and order matches {@link #_inputNodes}.
   */
  private final DependencyNode[] _inputNodes;

  // TODO: If there are more then N inputs, then sort the array by value specification to speed up the findInputValue operation

  /**
   * The values produced by this node. These can be considered as potential labels on output edges.
   */
  private final ValueSpecification[] _outputValues;

  // TODO: If there are more than N outputs, then sort the array by value specification to speed up the hasOutputValue operation

  // Construction operations

  /**
   * Creates a new node.
   * 
   * @param function the function, not null
   * @param target the target specification, not null
   * @param outputs the outputs of the node, not null and not containing null
   * @param inputs the input values to the node, not null and not containing null
   */
  public DependencyNodeImpl(final DependencyNodeFunction function, final ComputationTargetSpecification target, final Collection<ValueSpecification> outputs,
      final Map<ValueSpecification, DependencyNode> inputs) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(outputs, "outputs");
    ArgumentChecker.notNull(inputs, "inputs");
    _function = function;
    _target = target;
    int size = outputs.size();
    _outputValues = new ValueSpecification[size];
    int i = 0;
    for (ValueSpecification output : outputs) {
      ArgumentChecker.notNull(output, "output");
      assert _target.equals(output.getTargetSpecification());
      _outputValues[i++] = output;
    }
    assert i == size;
    size = inputs.size();
    if (size == 0) {
      _inputValues = EMPTY_SPECIFICATION_ARRAY;
      _inputNodes = EMPTY_NODE_ARRAY;
    } else {
      _inputValues = new ValueSpecification[size];
      _inputNodes = new DependencyNode[size];
      i = 0;
      for (Map.Entry<ValueSpecification, DependencyNode> input : inputs.entrySet()) {
        ArgumentChecker.notNull(input, "input");
        ArgumentChecker.notNull(input.getKey(), "input.key");
        ArgumentChecker.notNull(input.getValue(), "input.value");
        _inputValues[i] = input.getKey();
        _inputNodes[i] = input.getValue();
        i++;
      }
      assert i == size;
    }
  }

  private DependencyNodeImpl(final DependencyNodeFunction function, final ComputationTargetSpecification target, final ValueSpecification[] outputValues, final ValueSpecification[] inputValues,
      final DependencyNode[] inputNodes) {
    _function = function;
    _target = target;
    _outputValues = outputValues;
    _inputValues = inputValues;
    _inputNodes = inputNodes;
  }

  private DependencyNode addInputs(final ValueSpecification[] inputValues, final DependencyNode[] inputNodes) {
    int additionalInputs = 0;
    for (int i = 0; i < inputValues.length; i++) {
      if (findInputValue(inputValues[i]) < 0) {
        inputValues[additionalInputs] = inputValues[i];
        inputNodes[additionalInputs] = inputNodes[i];
        additionalInputs++;
      }
    }
    if (additionalInputs == 0) {
      // No changes; all of those inputs are present
      return this;
    }
    final int oldInputs = _inputValues.length;
    final ValueSpecification[] newInputValues = Arrays.copyOf(_inputValues, oldInputs + additionalInputs);
    final DependencyNode[] newInputNodes = Arrays.copyOf(_inputNodes, oldInputs + additionalInputs);
    System.arraycopy(inputValues, 0, newInputValues, oldInputs, additionalInputs);
    System.arraycopy(inputNodes, 0, newInputNodes, oldInputs, additionalInputs);
    return new DependencyNodeImpl(_function, _target, _outputValues, newInputValues, newInputNodes);
  }

  public static DependencyNode addInputs(final DependencyNode oldNode, final ValueSpecification[] inputValues, final DependencyNode[] inputNodes) {
    if (oldNode instanceof DependencyNodeImpl) {
      return ((DependencyNodeImpl) oldNode).addInputs(inputValues, inputNodes);
    } else {
      int additionalInputs = 0;
      for (int i = 0; i < inputValues.length; i++) {
        if (oldNode.findInputValue(inputValues[i]) < 0) {
          inputValues[additionalInputs] = inputValues[i];
          inputNodes[additionalInputs] = inputNodes[i];
          additionalInputs++;
        }
      }
      if (additionalInputs == 0) {
        // No changes; all of those inputs are present
        return oldNode;
      }
      final int oldInputs = oldNode.getInputCount();
      final ValueSpecification[] newInputValues = new ValueSpecification[oldInputs + additionalInputs];
      final DependencyNode[] newInputNodes = new DependencyNode[oldInputs + additionalInputs];
      for (int i = 0; i < oldInputs; i++) {
        newInputValues[i] = oldNode.getInputValue(i);
        newInputNodes[i] = oldNode.getInputNode(i);
      }
      System.arraycopy(inputValues, 0, newInputValues, oldInputs, additionalInputs);
      System.arraycopy(inputNodes, 0, newInputNodes, oldInputs, additionalInputs);
      return new DependencyNodeImpl(oldNode.getFunction(), oldNode.getTarget(), getOutputValueArray(oldNode), inputValues, inputNodes);
    }
  }

  private DependencyNode replaceInput(final ValueSpecification oldInputValue, final ValueSpecification newInputValue, final DependencyNode newInputNode) {
    final int oldInputs = _inputValues.length;
    ValueSpecification[] newInputValues = new ValueSpecification[oldInputs];
    DependencyNode[] newInputNodes = new DependencyNode[oldInputs];
    boolean replaced = false;
    int j = 0;
    for (int i = 0; i < oldInputs; i++) {
      final ValueSpecification oldInput = _inputValues[i];
      if (oldInputValue.equals(oldInput) || newInputValue.equals(oldInput)) {
        if (!replaced) {
          // Apply the replacement, but don't introduce a duplicate
          newInputValues[j] = newInputValue;
          newInputNodes[j++] = newInputNode;
          replaced = true;
        }
      } else {
        // Keep the existing input
        newInputValues[j] = oldInput;
        newInputNodes[j++] = _inputNodes[i];
      }
    }
    if (j < oldInputs) {
      // A duplicate has shortened the input arrays
      newInputValues = Arrays.copyOf(newInputValues, j);
      newInputNodes = Arrays.copyOf(newInputNodes, j);
    }
    return new DependencyNodeImpl(_function, _target, _outputValues, newInputValues, newInputNodes);
  }

  public static DependencyNode replaceInput(final DependencyNode oldNode, final ValueSpecification oldInputValue, final ValueSpecification newInputValue, final DependencyNode newInputNode) {
    if (oldNode instanceof DependencyNodeImpl) {
      return ((DependencyNodeImpl) oldNode).replaceInput(oldInputValue, newInputValue, newInputNode);
    } else {
      final int oldInputs = oldNode.getInputCount();
      ValueSpecification[] newInputValues = new ValueSpecification[oldInputs];
      DependencyNode[] newInputNodes = new DependencyNode[oldInputs];
      boolean replaced = false;
      int j = 0;
      for (int i = 0; i < oldInputs; i++) {
        final ValueSpecification oldInput = oldNode.getInputValue(i);
        if (oldInputValue.equals(oldInput) || newInputValue.equals(oldInput)) {
          if (!replaced) {
            newInputValues[j] = newInputValue;
            newInputNodes[j++] = newInputNode;
            replaced = true;
          }
        } else {
          newInputValues[j] = oldInput;
          newInputNodes[j++] = oldNode.getInputNode(i);
        }
      }
      if (j < oldInputs) {
        newInputValues = Arrays.copyOf(newInputValues, j);
        newInputNodes = Arrays.copyOf(newInputNodes, j);
      }
      return new DependencyNodeImpl(oldNode.getFunction(), oldNode.getTarget(), getOutputValueArray(oldNode), newInputValues, newInputNodes);
    }
  }

  private DependencyNode withOutputs(final ValueSpecification[] outputValues) {
    return new DependencyNodeImpl(_function, _target, outputValues, _inputValues, _inputNodes);
  }

  public static DependencyNode withOutputs(final DependencyNode oldNode, final ValueSpecification[] outputValues) {
    if (oldNode instanceof DependencyNodeImpl) {
      return ((DependencyNodeImpl) oldNode).withOutputs(outputValues);
    } else {
      return new DependencyNodeImpl(oldNode.getFunction(), oldNode.getTarget(), outputValues, getInputValueArray(oldNode), getInputNodeArray(oldNode));
    }
  }

  private static boolean outputValueTargets(final ComputationTargetSpecification target, final ValueSpecification[] outputValues) {
    for (ValueSpecification outputValue : outputValues) {
      if (!target.equals(outputValue.getTargetSpecification())) {
        return false;
      }
    }
    return true;
  }

  public static DependencyNode of(final DependencyNodeFunction function, final ComputationTargetSpecification target, final ValueSpecification[] outputValues, final ValueSpecification[] inputValues,
      final DependencyNode[] inputNodes) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.noNulls(outputValues, "outputValues");
    ArgumentChecker.noNulls(inputValues, "inputValues");
    ArgumentChecker.noNulls(inputNodes, "inputNodes");
    assert inputValues.length == inputNodes.length;
    assert outputValueTargets(target, outputValues);
    return new DependencyNodeImpl(function, target, outputValues, inputValues, inputNodes);
  }

  private DependencyNode removeUnnecessaryValues(final Map<ValueSpecification, DependencyNode> necessary) {
    ValueSpecification[] newOutputs = null;
    int newOutputCount = 0;
    for (int j = 0; j < _outputValues.length; j++) {
      final ValueSpecification output = _outputValues[j];
      if (necessary.containsKey(output)) {
        if (newOutputs != null) {
          newOutputs[newOutputCount++] = output;
        }
      } else {
        if (newOutputs == null) {
          newOutputs = new ValueSpecification[_outputValues.length - 1];
          if (j > 0) {
            newOutputCount = j;
            System.arraycopy(_outputValues, 0, newOutputs, 0, j);
          }
        }
      }
    }
    if ((newOutputs != null) && (newOutputCount == 0)) {
      // We have a node that isn't needed anymore - it produces no necessary outputs
      return null;
    }
    ValueSpecification[] newInputValues = null;
    DependencyNode[] newInputNodes = null;
    int newInputCount = 0;
    for (int j = 0; j < _inputValues.length; j++) {
      final ValueSpecification inputValue = _inputValues[j];
      final DependencyNode oldInputNode = _inputNodes[j];
      DependencyNode newInputNode = necessary.get(inputValue);
      if (newInputNode == null) {
        assert necessary.containsKey(inputValue);
        newInputNode = removeUnnecessaryValues(oldInputNode, necessary);
        assert newInputNode != null;
      }
      if (newInputNode == oldInputNode) {
        if (newInputValues != null) {
          newInputValues[newInputCount] = inputValue;
          newInputNodes[newInputCount++] = newInputNode;
        }
      } else {
        if (newInputValues == null) {
          newInputValues = new ValueSpecification[_inputValues.length];
          newInputNodes = new DependencyNode[_inputValues.length];
          newInputCount = j;
          System.arraycopy(_inputValues, 0, newInputValues, 0, newInputCount);
          System.arraycopy(_inputNodes, 0, newInputNodes, 0, newInputCount);
        }
        newInputValues[newInputCount] = inputValue;
        newInputNodes[newInputCount++] = newInputNode;
      }
    }
    final DependencyNode newNode;
    if (newOutputs == null) {
      newOutputs = _outputValues;
      newOutputCount = _outputValues.length;
      if (newInputNodes == null) {
        // No changes required at this node
        newNode = this;
      } else {
        // New inputs, previous output set
        if (newInputCount != newInputValues.length) {
          newInputValues = Arrays.copyOf(newInputValues, newInputCount);
          newInputNodes = Arrays.copyOf(newInputNodes, newInputCount);
        }
        newNode = new DependencyNodeImpl(_function, _target, newOutputs, newInputValues, newInputNodes);
      }
    } else {
      if (newOutputCount != newOutputs.length) {
        newOutputs = Arrays.copyOf(newOutputs, newOutputCount);
      }
      if (newInputNodes == null) {
        // New outputs, previous input set
        newInputValues = _inputValues;
        newInputNodes = _inputNodes;
      } else {
        if (newInputCount != newInputValues.length) {
          newInputValues = Arrays.copyOf(newInputValues, newInputCount);
          newInputNodes = Arrays.copyOf(newInputNodes, newInputCount);
        }
      }
      newNode = new DependencyNodeImpl(_function, _target, newOutputs, newInputValues, newInputNodes);
    }
    for (int i = 0; i < newOutputCount; i++) {
      necessary.put(newOutputs[i], newNode);
    }
    return newNode;
  }

  /* package */static DependencyNode removeUnnecessaryValues(final DependencyNode oldNode, final Map<ValueSpecification, DependencyNode> necessary) {
    if (oldNode instanceof DependencyNodeImpl) {
      return ((DependencyNodeImpl) oldNode).removeUnnecessaryValues(necessary);
    } else {
      int outputCount = oldNode.getOutputCount();
      ValueSpecification[] newOutputs = null;
      int newOutputCount = 0;
      for (int j = 0; j < outputCount; j++) {
        final ValueSpecification output = oldNode.getOutputValue(j);
        if (necessary.containsKey(output)) {
          if (newOutputs != null) {
            newOutputs[newOutputCount++] = output;
          }
        } else {
          if (newOutputs == null) {
            newOutputs = new ValueSpecification[outputCount - 1];
            for (newOutputCount = 0; newOutputCount < j; newOutputCount++) {
              newOutputs[newOutputCount] = oldNode.getOutputValue(newOutputCount);
            }
          }
        }
      }
      if ((newOutputs != null) && (newOutputCount == 0)) {
        // We have a node that isn't needed anymore - it produces no necessary outputs
        return null;
      }
      final int inputCount = oldNode.getInputCount();
      ValueSpecification[] newInputValues = null;
      DependencyNode[] newInputNodes = null;
      int newInputCount = 0;
      for (int j = 0; j < inputCount; j++) {
        final ValueSpecification inputValue = oldNode.getInputValue(j);
        final DependencyNode oldInputNode = oldNode.getInputNode(j);
        DependencyNode newInputNode = necessary.get(inputValue);
        if (newInputNode == null) {
          assert necessary.containsKey(inputValue);
          newInputNode = removeUnnecessaryValues(oldInputNode, necessary);
          assert newInputNode != null;
        }
        if (newInputNode == oldInputNode) {
          if (newInputValues != null) {
            newInputValues[newInputCount] = inputValue;
            newInputNodes[newInputCount++] = newInputNode;
          }
        } else {
          if (newInputValues == null) {
            newInputValues = new ValueSpecification[inputCount];
            newInputNodes = new DependencyNode[inputCount];
            for (newInputCount = 0; newInputCount < j; newInputCount++) {
              newInputValues[newInputCount] = oldNode.getInputValue(newInputCount);
              newInputNodes[newInputCount] = oldNode.getInputNode(newInputCount);
            }
          }
          newInputValues[newInputCount] = inputValue;
          newInputNodes[newInputCount++] = newInputNode;
        }
      }
      final DependencyNode newNode;
      if (newOutputs == null) {
        newOutputs = DependencyNodeImpl.getOutputValueArray(oldNode);
        newOutputCount = newOutputs.length;
        if (newInputNodes == null) {
          // No changes required at this node
          newNode = oldNode;
        } else {
          // New inputs, previous output set
          if (newInputCount != newInputValues.length) {
            newInputValues = Arrays.copyOf(newInputValues, newInputCount);
            newInputNodes = Arrays.copyOf(newInputNodes, newInputCount);
          }
          newNode = new DependencyNodeImpl(oldNode.getFunction(), oldNode.getTarget(), newOutputs, newInputValues, newInputNodes);
        }
      } else {
        if (newOutputCount != newOutputs.length) {
          newOutputs = Arrays.copyOf(newOutputs, newOutputCount);
        }
        if (newInputNodes == null) {
          // New outputs, previous input set
          newInputValues = DependencyNodeImpl.getInputValueArray(oldNode);
          newInputNodes = DependencyNodeImpl.getInputNodeArray(oldNode);
        } else {
          if (newInputCount != newInputValues.length) {
            newInputValues = Arrays.copyOf(newInputValues, newInputCount);
            newInputNodes = Arrays.copyOf(newInputNodes, newInputCount);
          }
        }
        newNode = new DependencyNodeImpl(oldNode.getFunction(), oldNode.getTarget(), newOutputs, newInputValues, newInputNodes);
      }
      for (int i = 0; i < newOutputCount; i++) {
        necessary.put(newOutputs[i], newNode);
      }
      return newNode;
    }
  }

  // Function & Target operations

  @Override
  public DependencyNodeFunction getFunction() {
    return _function;
  }

  @Override
  public ComputationTargetSpecification getTarget() {
    return _target;
  }

  // Input node/value operations

  @Override
  public int getInputCount() {
    return _inputValues.length;
  }

  @Override
  public ValueSpecification getInputValue(final int index) {
    return _inputValues[index];
  }

  @Override
  public DependencyNode getInputNode(final int index) {
    return _inputNodes[index];
  }

  @Override
  public int findInputValue(final ValueSpecification value) {
    for (int i = 0; i < _inputValues.length; i++) {
      if (value.equals(_inputValues[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Obtains a copy of the input value specifications for a node as a set.
   * 
   * @param node the node instance to query
   * @return the set of input value specifications to the node
   */
  public static Set<ValueSpecification> getInputValues(final DependencyNode node) {
    final int count = node.getInputCount();
    final Set<ValueSpecification> inputs = Sets.newHashSetWithExpectedSize(count);
    for (int i = 0; i < count; i++) {
      inputs.add(node.getInputValue(i));
    }
    return inputs;
  }

  /**
   * Obtains a copy of the input value specifications and nodes as a map of value specifications to the node that produces each one
   * 
   * @param node the node instance to query
   * @return the input values and nodes
   */
  public static Map<ValueSpecification, DependencyNode> getInputs(final DependencyNode node) {
    final int count = node.getInputCount();
    final Map<ValueSpecification, DependencyNode> inputs = Maps.newHashMapWithExpectedSize(count);
    for (int i = 0; i < count; i++) {
      inputs.put(node.getInputValue(i), node.getInputNode(i));
    }
    return inputs;
  }

  /**
   * Obtains a copy of the input values to a node as an array.
   * 
   * @param node the node instance to query, not null
   * @return the array of input values to the node, not null and not containing null
   */
  public static ValueSpecification[] getInputValueArray(final DependencyNode node) {
    final int count = node.getInputCount();
    if (count == 0) {
      return EMPTY_SPECIFICATION_ARRAY;
    }
    final ValueSpecification[] inputs = new ValueSpecification[count];
    for (int i = 0; i < count; i++) {
      inputs[i] = node.getInputValue(i);
    }
    return inputs;
  }

  /**
   * Obtains a copy of the input nodes to a node as an array.
   * 
   * @param node the node instance to query, not null
   * @return the array of input nodes, not null and not containing null
   */
  public static DependencyNode[] getInputNodeArray(final DependencyNode node) {
    final int count = node.getInputCount();
    if (count == 0) {
      return EMPTY_NODE_ARRAY;
    }
    final DependencyNode[] inputs = new DependencyNode[count];
    for (int i = 0; i < count; i++) {
      inputs[i] = node.getInputNode(i);
    }
    return inputs;
  }

  // Output node/value operations

  @Override
  public int getOutputCount() {
    return _outputValues.length;
  }

  @Override
  public ValueSpecification getOutputValue(final int index) {
    return _outputValues[index];
  }

  @Override
  public boolean hasOutputValue(final ValueSpecification value) {
    for (int i = 0; i < _outputValues.length; i++) {
      if (value.equals(_outputValues[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Obtains a copy of the output value specifications for a node as a set.
   * 
   * @param node the node instance to query, not null
   * @return the set of output value specifications to the node, not null
   */
  public static Set<ValueSpecification> getOutputValues(final DependencyNode node) {
    final int count = node.getOutputCount();
    final Set<ValueSpecification> inputs = Sets.newHashSetWithExpectedSize(count);
    for (int i = 0; i < count; i++) {
      inputs.add(node.getOutputValue(i));
    }
    return inputs;
  }

  /**
   * Obtains a copy of the output value specifications for a node as an array.
   * 
   * @param node the node instance to query, not null
   * @return the array of output value specifications to the node, not null
   */
  public static ValueSpecification[] getOutputValueArray(final DependencyNode node) {
    final int count = node.getOutputCount();
    final ValueSpecification[] outputs = new ValueSpecification[count];
    for (int i = 0; i < count; i++) {
      outputs[i] = node.getOutputValue(i);
    }
    return outputs;
  }

  /* package */static void gatherOutputValues(final DependencyNode node, final Map<ValueSpecification, DependencyNode> outputs) {
    int count = node.getOutputCount();
    for (int i = 0; i < count; i++) {
      final ValueSpecification output = node.getOutputValue(i);
      DependencyNode existing = outputs.put(output, node);
      if (existing != null) {
        assert existing == node;
        return;
      }
    }
    count = node.getInputCount();
    for (int i = 0; i < count; i++) {
      gatherOutputValues(node.getInputNode(i), outputs);
    }
  }

  // Misc

  @Override
  public String toString() {
    return "Node" + Integer.toHexString(System.identityHashCode(this)) + "[" + getFunction() + " on " + getTarget() + ", " + _inputValues.length + " input(s), " + _outputValues.length + " output(s)]";
  }

  private void writeObject(final ObjectOutputStream out) throws IOException {
    if (!(_function instanceof Serializable)) {
      _function = DependencyNodeFunctionImpl.of(_function.getFunctionId(), _function.getParameters());
    }
    out.defaultWriteObject();
  }

}

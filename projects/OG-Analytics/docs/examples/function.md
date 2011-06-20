Start with a DoubleMatrix1D.
{{ d['function.py|idio|jythoni|pyg|pynliner']['create-matrix'] }}

Import a static Function<DoubleMatrix1D,DoubleMatrix1D> that squares each element of X
{{ d['function.py|idio|jythoni|pyg|pynliner']['square-elements'] }}

As functions are first class objects in jython, we can also do this
{{ d['function.py|idio|jythoni|pyg|pynliner']['evaluate'] }}

Finally, we import the differentiation package
{{ d['function.py|idio|jythoni|pyg|pynliner']['import-differentiate'] }}

compute the derivative of our Squares function object
{{ d['function.py|idio|jythoni|pyg|pynliner']['compute-derivative'] }}

Here is the java code we are referencing
{{ d['javadoc-data.json|javadocs']['packages'].keys() }}


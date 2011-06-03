Start with a DoubleMatrix1D.

{{ d['function.py|idio|jythoni|pyg']['create-matrix'] }}

Import a static Function<DoubleMatrix1D,DoubleMatrix1D> that squares each element of X
{{ d['function.py|idio|jythoni|pyg']['square-elements'] }}

As functions are first class objects in jython, we can also do this
{{ d['function.py|idio|jythoni|pyg']['evaluate'] }}

Finally, we import the differentiation package
{{ d['function.py|idio|jythoni|pyg']['import-differentiate'] }}

compute the derivative of our Squares function object
{{ d['function.py|idio|jythoni|pyg']['compute-derivative'] }}

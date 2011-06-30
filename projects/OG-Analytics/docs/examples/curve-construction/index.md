This tutorial covers yield curve construction in OpenGamma, in the process of which we introduce matrices, functions and root finding.

## Matrices

We begin with matrices. First we define a simple method to allow us to create identity matrices in the form of two-dimensional Python arrays:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['define-identity'] }}

Now we can create DoubleMatrix2D objects by passing these 2D arrays to the constructor:
{{ d['curve-construction.py|fn|idio|jythoni|pyg']['double-matrix-2d'] }}

And we can perform some simple operations on this matrix:
{{ d['curve-construction.py|fn|idio|jythoni|pyg']['double-matrix-2d-operations'] }}

Now we create a DoubleMatrix1D (a vector):
{{ d['curve-construction.py|fn|idio|jythoni|pyg']['double-matrix-1d'] }}

We create an instance of ColtMatrixAlgebra and multiply the matrix by the vector:
{{ d['curve-construction.py|fn|idio|jythoni|pyg']['matrix-multiply'] }}

And we see that multiplication by the identity matrix preserves the vector, as we expect.

## Functions

Now let's create a function. The com.opengamma.math.function class defines several function types. Let's create a simple polynomial:

\\begin{eqnarray}
f(x) &=& (x-5)^3 \\\\
&=& x^3 - 15x^2 + 75x - 125
\\end{eqnarray}

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['create-poly'] }}

Here are the methods that can be called on this function:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['function-methods'] }}

Let's take the derivative:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['derivative'] }}

And we can verify that 5 is a root:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['known-root'] }}

## Rootfinding

Now let's look at the rootfinding methods:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['rootfinding'] }}

Here is the Cubic rootfinder:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['cubic'] }}

And the BrentSingleRootFinder:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['brent'] }}

Note that we must pass an interval which brackets a root:

{{ d['curve-construction.py|fn|idio|jythoni|pyg']['brent-not-bracketing-root'] }}

## Curve Construction

TBD

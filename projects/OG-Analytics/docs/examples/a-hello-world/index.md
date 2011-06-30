This is a very simple example intended to help you troubleshoot connecting to the OG codebase using Jython.

First we print the java classpath as a debugging aid:

{{ d['complex.py|fn|idio|jythoni|pyg']['classpath'] }}

Next we import the com.opengamma.math.number.ComplexNumber class:

{{ d['complex.py|fn|idio|jythoni|pyg']['import'] }}

We create an instance of a complex number:

{{ d['complex.py|fn|idio|jythoni|pyg']['define'] }}

Now we do some simple operations:

{{ d['complex.py|fn|idio|jythoni|pyg']['get-real'] }}
{{ d['complex.py|fn|idio|jythoni|pyg']['get-imaginary'] }}
{{ d['complex.py|fn|idio|jythoni|pyg']['to-string'] }}

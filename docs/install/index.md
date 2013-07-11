The Examples-Simulated database provides example data and an example server configuration.

As per [this wiki page](http://docs.opengamma.com/display/DOC090/Running+the+Example+Engine), you can configure and start the example server by running:

{{ d['start-server.sh|pyg'] }}

The new-hsqldb task creates a database and defines the necessary tables in it:

{{ d['/build.xml|xxml']['target:new-hsqldb']['source-html'] }}

The demo-database task runs com.opengamma.examples.loader.DemoDatabasePopulater:

{{ d['/build.xml|xxml']['target:demo-database']['source-html'] }}

Here is the main method that is run:

{{ d['javadoc-data.json|javadoc']['packages']['com.opengamma.examples.loader']['classes']['DemoDatabasePopulater']['methods']['main(String[])']['source-html'] }}

We can see a number of classes and data files which are use populate the database:

<pre>
{{ d['file-tree.sh|bash'] }}
</pre>

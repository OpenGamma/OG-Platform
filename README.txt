Welcome to the OpenGamma Platform!
----------------------------------
OpenGamma's flagship technology for financial institutions, the OpenGamma
Platform, is a comprehensive solution for financial analytics capable of
satisfying the full range of front-office and risk requirements.
It supports pre-trade ad-hoc calculations, near-real-time streaming analytics,
batch/overnight risk calculations, and complex historical scenarios and
stress tests in the same system.

Built as a completely open architecture, the OpenGamma Platform is designed so
that every component can be individually used, or individually replaced, based
on customer requirements. We don't believe in forklift upgrades, and we built
the OpenGamma Platform so that they're never necessary: individual projects can
use OpenGamma components when they provide a clear advantage, and later migrate
additional portions of their infrastructure if and when time and resources
permit.

Visit the developer website at http://developers.opengamma.com for more
information, downloads, docs and more.


Building OpenGamma
------------------
Firstly you need to make sure Apache Ant and Git are installed and working.  
Our build uses Apache Ivy and our favoured IDE is Eclipse.

Maven files do exist, but they should not be relied on for building OpenGamma.

Initializing the build
----------------------

The OpenGamma Platform is open source software using the Apache License v2.
The [company](http://www.opengamma.com/) behind OpenGamma also offers support
and some additional commercial components (The commercial components typically
have dependencies with restrictive licensing incompatible with open source.)

The source code can be cloned using git from GitHub or downloaded as a source
tarball from http://developers.opengamma.com.

  git clone https://github.com/OpenGamma/OG-Platform.git

If you do not have access to the OpenGamma commercial components, then skip
the rest of this section.

To download the commercial components, initialize the OpenGamma system with
your username and password:

  ant init

Then, download the additional commercial components:

  ant clone-or-pull

This will do the initial clone of each project into the `projects/` directory.
When you run this subsequently, it does a git pull on each project, although
you will probably need to edit the `build.xml` file to tell the git task which
branch you want of each project.

Now, continue on to the next section.

Completing the build
--------------------
The source code must be compiled before use. This will build multiple jar files
and install them into your local Ivy repository, which is located in `~/.ivy2`:

  ant build

To run the unit tests (once the build is complete), use:

  ant tests

The output is generated into `build/test-reports/html`.


Running the OpenGamma engine
----------------------------
The primary program in the OpenGamma platform is known as the "engine".
For production, the engine is typically customized, however two example engine
configurations are pre-supplied, one with Bloomberg support and one with
simulated market data.

To build and install the example engines run:

  ant install

which will create a folder called `opengamma/` in the project root.  In there 
you'll find a `scripts/` folder which contains

  init-og-bloombergexample-db.sh      Create an example database using data from a local Bloomberg terminal
  init-og-examples-db.sh              Create an example database using fake data (if you don't have a terminal)
  og-bloombergexample.sh              Run the example engine with (start|stop|restart|status|reload|debug)
                                       sourcing market data from your Bloomberg Terminal
  og-examples.sh                      Run the example engine with (start|stop|restart|status|reload|debug)
                                       sourcing market data from a simulated market data generator
  time-series-updater.sh              Updates historical time series to latest values
  load-portfolio.sh                   Utility to load previously zipped group of CSV files into the database
  save-portfolio.sh                   Utility to save an existing portfolio into a zip file containing CSVs
  create-portfolio-template.sh        Creates template CSV files with headers for use with load-portfolio.sh

Note you should only run the appropriate init script paired with the
appropriate example script.  Running, e.g. `init-og-examples-db.sh` before
running `og-bloomberg-example.sh` will cause the system to not operate correctly.

If you have a Bloomberg terminal, cd to the scripts directory and run:

  init-og-bloombergexample-db.sh
  og-bloombergexample.sh start

Without a Bloomberg terminal, you can use simulated data:

  init-og-examples-db.sh
  og-examples.sh start

Wait for the components to load and then point your browser at
`http://localhost:8080` to see the web user interface.  
Go to `http://localhost:8080/jax/components` to get a sense of
the underlying power of the system, available via REST.

___

Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of
instructions to work properly, see the file `ECLIPSE.txt`.

Ivy Repositories and settings files
-----------------------------------
If you wish to set up a shared ivy repository, this can be specified as the
`IVY_SHARED_DEFAULT_ROOT` environment variable.  See the ivy settings files in
`common/`.  By default the shared repository is configured to be the `repository/`
directory in the root, which is where the dependencies go when you download a
source tarball that includes the dependencies.  If they're not present, Ivy
will continue up the resolver chain until it finds the artifacts from the
OpenGamma public Ivy repos, or fails if you do not have connectivity.

Each project's `build.xml` includes the `common.xml` file in `common/` which defines
most targets.  Any tasks that need further customization can be customized by
simply overriding that task (for an example, see the javadocs task in 
`OG-Analytics/build.xml`.

`common.xml` also includes `common/build.properties` and `tests.properties` files.
All the database settings are in the .properties files under config/ in the
appropriate project (e.g. OG-Examples or OG-BloombergExample)

More information
----------------
For more information go to http://developers.opengamma.com

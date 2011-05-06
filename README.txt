Welcome to the OpenGamma Platform!
----------------------------------
OpenGamma's flagship technology, the OpenGamma Platform, is a comprehensive solution for analytics capable of 
satisfying the full range of front-office and risk requirements. It supports pre-trade ad-hoc calculations, 
near-real-time streaming analytics, batch/overnight risk calculations, and complex historical scenarios and stress 
tests in the same system.

Built as a completely open architecture, the OpenGamma Platform is designed so that every component can be 
individually used, or individually replaced, based on customer requirements. We don't believe in forklift 
upgrades, and we built the OpenGamma Platform so that they're never necessary: individual projects can use 
OpenGamma components when they provide a clear advantage, and later migrate additional portions of their 
infrastructure if and when time and resources permit.

Visit the developer website at http://developer.opengamma.com for more information, downloads, docs and more

How to use the build system
---------------------------

If you've pulled the source code from GitHub
--------------------------------------------
As usual, all this assumes that ant is already in your path and that git is installed.

  ant init

will prompt you for a username and password.  If you are an OpenGamma customer, enter your login details here and
you will be able to access the commercial component.  If you're looking at the pure open source release, just 
press ENTER twice to use the defaults.

  ant clone-or-pull

will do the initial clone of each project into the 'projects' directory.  When you run this subsequently, it does a
git pull on each project, although you will probably need to edit the build.xml file to tell the git task which 
branch you want of each project.

If you've just downloaded the source tarball or are using Git and have completed the above steps
------------------------------------------------------------------------------------------------
To build and publish the results to your local repository (stored in ~/.ivy2), use:

  ant publish-all-local

to run the unit tests (after a publish-all-local) use:

  ant tests

which will put the JUnitReportRunner output in tests/output/html.  To run the example engine template (which won't 
actually do very much at all without some data in the databases and a data provider implementation of OpenGamma 
LiveData, change directory to projects/OG-Examples and run:

  ant jetty-debug

wait for "END JETTY START" in the debug output and point your browser at http://localhost:8080.

ant -p will give you all the targets available and they're pretty self explainatory.

Eclipse
-------
Importing the projects into Eclipse requires following a very specific set of instructions to work properly,
see the file ECLIPSE.txt

Ivy Repositories and settings files
-----------------------------------
If you wish to set up a shared ivy repository, this can be specified as the IVY_SHARED_DEFAULT_ROOT environment 
variable.  See the ivy settings files in common/.  By default the shared repository is configured to be the
repository/ directory in the root, which is where the dependencies go when you download a source tarball that 
includes the dependencies.  If they're not present, Ivy will continue up the resolver chain until it finds the 
artifacts from the OpenGamma public Ivy repos, or fails if you do not have connectivity.

Each project's build.xml includes the common.xml file in common/ which defines most targets.  Any tasks that need
further customization can be customized by simply overriding that task (for an example, see the javadocs task in 
OG-Analytics/build.xml.

common.xml also includes common/build.properties and tests.properties files.  All the database settings are 
centralized in common/build.properties and common/tests.properties (except for 
OG-Language/config/OpenGamma.properties).

More information
----------------
For more information go to http://developers.opengamma.com

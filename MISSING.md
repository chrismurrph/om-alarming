
The third party libraries come from smartgas-deps.jar. Also we have the SMARTGAS-connect server source, which includes configuration. 
What is missing is pieces of Scala code for where a virtual mine driver is used. So these missing pieces should not be required in
production where talk to real hardware.

I have found that putting these missing pieces into the smartgas-deps.jar is the only way to get everything working nicely together.
I blame the Scala (.class) code not being recognised by lein/Clojure code. In effect we are keeping the Scala code behind a Java
barrier.

Some facts about our setup:

* MyPLC has a copy of the latest Scala source code, which we will be altering slightly, which is not currently version controlled
* MyPLC uses simlinks to also have all the Java (client and server) code available, which is version controlled by cmtsproject
* MyPLC can be built using sbt resulting in the classes ending up here: /home/chris/IdeaProjects/MyPLC/target/scala-2.11/classes
* Building MyPLC requires smartgas-deps.jar, which is unmanaged (not in maven) 
* missing.jar uses the classes from MyPLC
* missing.jar becomes a part of enhanced-smartgas-deps.jar
* smartgas-deps.jar really is just the third party deps i.e. does not have missing.jar
* om-alarming is clojure/lein, so enhanced-smartgas-deps.jar needs to be in maven
* There are separate projects for smartgas-deps and enhanced-smartgas-deps, each really just a build.sbt file

When changes are made to Scala code these are the steps to then getting it working with om-alarming:

1. MyPLC to be built using sbt (compile is enough)
2. missing.jar to be built here (ant -f jarMissing.xml)
3. enhanced-smartgas-deps has its own project that uses sbt-assembly to build enhanced-smartgas-deps.jar
4. There is a shell script (cp-out.sh) to copy enhanced-smartgas-deps.jar to the alarm-server project and install it into maven from there
5. In the om alarming IDEA project you will notice that it gets re-indexed (same for alarm-server)
6. I believe you can run the om alarming server and it will pick up the changes

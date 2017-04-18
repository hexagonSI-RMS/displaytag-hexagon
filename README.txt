Building from Source
====================

1. Displaytag uses maven for build and site generation. First of all, you need to download and install
maven (you will need at least Maven 1.0) from maven.apache.org. Follow the instructions on the Maven
site on how to do this.

2. To build this release, simply go to the root folder containing the project and run
 (for Maven 1.x) maven
   or
 (for Maven 2.x) mvn clean install
  Then look in the target subdirectory to find the build output and the final library.

# About


## Building 

This project uses [maven-toolchains-plugin][maven-toolchains-plugin], so you'll need to [setup toolchains][maven-toolchains-plugin-setup].  
Examples for various OS/architectures can be found [here][maven-central-earcam-toolchain] 

With toolchains configured, run `mvn clean install`.

When modifying the code beware/be-aware the build will fail if Maven POMs, Java source or Javascript source aren't formatted according to conventions (Apache 
Maven's standards for POMs, my own undocumented formatting for source).  To auto-format the lot, simply run `mvn -P '!strict,tidy'`.

To run PiTest use `mvn -P analyse`

To run against SonarQube use `mvn -P analyse,sonar`

### Site Generation

Site generation is a bit fiddly, as we'd like to include the offline search functionality but can't use the [maven plugin][earcam-maven-plugins] due to circular dependency (so uses maven-exec and antrun)

    mvn -P 'analyse,report,site' clean install site   &&   mvn -P post-site   &&   mvn site:stage


## Roadmap

The current early release(s) have just been to provide functionality and test utilities for modules in [io.earcam.maven.plugin][earcam-maven-plugins]. Other tiny utilities are in the pipeline. 

Generally, lots of improvements and documentation TODO...


[maven-toolchains-plugin]: http://maven.apache.org/plugins/maven-toolchains-plugin/
[maven-toolchains-plugin-setup]: https://maven.apache.org/guides/mini/guide-using-toolchains.html
[maven-central-earcam-toolchain]: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22io.earcam.maven.toolchain%22
[earcam-maven-plugins]: https://plugin.maven.earcam.io
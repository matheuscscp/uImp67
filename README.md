uImpala
=======

![alt tag](http://www.supernaturalwiki.com/images/0/05/Impala.jpg "The Impala")

A Java engine for ubiquitous games, made with the uOS middleware.

Five steps to create an Eclipse Maven project with this library:
* Clone uos_core (branch 3.1.0), uos_socket_plugin (branch 3.1.0) and uImpala (branch master) (**links** below);
* Import these three existing Maven projects;
* Create a Maven project and add the tags below in the **pom.xml**;
* Right-click on project -> Maven -> Update Project... -> OK;
* Right-click on project -> Properties -> Java Build Path -> Libraries -> Maven Dependencies arrow -> Native library location -> Edit... -> Insert Location path as replace-with-project-name/target/natives -> OK -> OK.
 * Another option for this final step is to install [this plugin](http://mavennatives.googlecode.com/svn/eclipse-update/).

Check the [Wiki](https://github.com/matheuscscp/uImpala/wiki) for tutorials.

Links
=====

* [uos_core](https://github.com/UnBiquitous/uos_core)
* [uos_socket_plugin](https://github.com/UnBiquitous/uos_socket_plugin)
* [uImpala](https://github.com/matheuscscp/uImpala)

pom.xml
=======

```xml
  <dependencies>
    <dependency>
      <groupId>org.unbiquitous</groupId>
      <artifactId>uImpala-jse</artifactId>
      <version>1.0</version>
    </dependency>
  </dependencies>
  
  <build>
    <plugins>
      <plugin>
        
        <groupId>com.googlecode.mavennatives</groupId>
        <artifactId>maven-nativedependencies-plugin</artifactId>
        <version>0.0.7</version>
        
        <executions>
          <execution>
            <id>unpacknatives</id>
            <goals>
              <goal>copy</goal>
            </goals>
          </execution>
        </executions>
        
      </plugin>
    </plugins>
  </build>
  
  <repositories>
		<repository>
			<id>ubiquitos</id>
			<url>http://ubiquitos.googlecode.com/svn/trunk/src/Java/maven/</url>
		</repository>
	</repositories>
```

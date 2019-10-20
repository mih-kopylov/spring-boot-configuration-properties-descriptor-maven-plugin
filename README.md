# Spring Boot Configuration Descriptor Processor Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/ru.mihkopylov/spring-boot-configuration-properties-descriptor-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22ru.mihkopylov%22%20AND%20a:%22spring-boot-configuration-properties-descriptor-maven-plugin%22)
![License](https://img.shields.io/github/license/mih-kopylov/spring-boot-configuration-properties-descriptor-maven-plugin)

## Description

When writing a service which has a lot of configuration, it is very useful to have a single document describing all possible properties of the service.

If the service binds properties to beans using [@ConfigurationProperties](https://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/spring-boot-features.html#boot-features-external-config-typesafe-configuration-properties), such beans with javadocs can be a single source of truth for the service configuration.   

The goal of this plugin is to generate markdown description for all the `@ConfigurationProperties`.


## Usage

To have this plugin work, it's required to have `spring-boot-configuration-processor` dependency [that generates metadata](https://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/appendix-configuration-metadata.html) `/META-INF/spring-configuration-metadata.json` which is used as a base for a resulting Markdown document. 

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

And need to add the plugin to `/build/plugins` section of `pom.xml`.

```xml
<plugin>
    <groupId>ru.mihkopylov</groupId>
    <artifactId>spring-boot-configuration-descriptor-processor-maven-plugin</artifactId>
    <version>${spring-boot-configuration-descriptor-processor-maven-plugin.version}</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>describe</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Once `mvn compile` is run, a result `CONFIGURATION.md` file will be generated. By default it will be placed to root directory beside `pom.xml`, but this can be configured. 

## Configuration

This is the default configuration:

```xml
<plugin>
    <groupId>ru.mihkopylov</groupId>
    <artifactId>spring-boot-configuration-descriptor-processor-maven-plugin</artifactId>
    <version>${spring-boot-configuration-descriptor-processor-maven-plugin.version}</version>
    <configuration>
        <!--the metadata json file generated by Spring-->
        <jsonFileName>${project.build.outputDirectory}/META-INF/spring-configuration-metadata.json</jsonFileName>
        <!--should the plugin fail if the json metadata file is not found-->
        <failIfNoMetadataFileFound>false</failIfNoMetadataFileFound>
        <!--where to put the result file-->
        <outputFileName>${basedir}/CONFIGURATION.md</outputFileName>
    </configuration>
</plugin>
```
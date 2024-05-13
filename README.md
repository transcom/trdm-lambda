# Table of Contents
- [Table of Contents](#table-of-contents)
- [Pre-Requisites](#pre-requisites)
- [Running tests](#running-tests)
- [How to Deploy](#how-to-deploy)
- [History](#history)
  - [Disabled RESTful to SOAP Conversion Capabilities](#disabled-restful-to-soap-conversion-capabilities)
  - [Deprecated API Gateway](#deprecated-api-gateway)
  - [TRDM](#trdm)
  - [Whitelisted tables](#whitelisted-tables)
  - [Apache CXF Code Generation](#apache-cxf-code-generation)
  - [Plugin](#plugin)
  - [CI/CD Known Issue](#cicd-known-issue)
  - [trdm-lambda](#trdm-lambda)
    - [ENV variables](#env-variables)
    - [Environment Specific Yaml files](#environment-specific-yaml-files)
    - [Application Information](#application-information)
    - [Meta Annotations](#meta-annotations)
    - [Controller Advice](#controller-advice)
    - [Custom Properties](#custom-properties)
  - [Deprecated Endpoints](#deprecated-endpoints)
    - [lastTableUpdate](#lasttableupdate)
    - [getTable](#gettable)

# Pre-Requisites
* OpenJDK 17
* Maven (No specific version)
* [MyMove PreReqs](https://transcom.github.io/mymove-docs/docs/getting-started/application-setup/prerequisites)

# Running tests
This repository relies on the Transcom [mymove repository](https://github.com/transcom/mymove). This means that tests require a connection to that repositories docker containers. Please see the following steps:
1. Have two open terminal windows, one for this repository and another for the mymove repository
2. `cd` into the mymove repository in your second window
   1. If you haven't already, run `direnv allow`
   2. Run `make server_test_setup` to configure your docker container
   3. For more documentation on this, please see mymove docs [here](https://transcom.github.io/mymove-docs/docs/getting-started/development/testing)
3. Open Docker Desktop and confirm that the `milmove-db-test` container is up and running 
4. Change back to your first terminal window for this repository
5. Run `mvn test`

# How to Deploy

To deploy the application to AWS, download the latest release, upload it into the bucket under lambda builds -> trdm-soap-proxy -> Make a new version folder -> upload with proper encryption keys set.

After it has been uploaded into the bucket please update the `trdm_soap_proxy_version` variable in terraform [here](https://github.com/transcom/terraform-aws-app-environment) under `variables.tf`.

# History
This section of the README is dedicated to the past history of this repository. It will provide a high-level of the previous version of this repository which was to serve as an API gateway from Transcom's MyMove to Transcom Relational Database Management's (TRDM) servers. This approach was abandoned and switched to a cron job for the sake of time sensitivity and a vastly already out-of-scope feature approach. It can be previewed via the git history prior to release `v1.0.0.0`. The release and its changelog can be found [here](https://github.com/transcom/trdm-lambda/releases/tag/v1.0.0.0). There are still some deprecated files remaining within this repository.

## Disabled RESTful to SOAP Conversion Capabilities
This lambda function as of version 1 received an overhaul to function as a cron based lambda function invoked via a timer.

It has the complete capability of providing a REST to SOAP conversion service. This can be of use in the future when other services may need to interface through us. It currently is running as spring boot application which is not necessarily needed for a cron job, but due to time sensitivity it was not phased out when overhauling. It does still offer the future ability to turn back on the RESTful aspects to provide said conversion service, but as of right now it is disabled.

## Deprecated API Gateway
Please see documentation [here](https://dp3.atlassian.net/wiki/spaces/MT/pages/2275573761/TRDM+Soap+Proxy+API+Gateway+Lambda+Function).

## TRDM
We are leveraging the `ReturnTableV7` WSDL provided by TRDM. This file has been verified to be unclassified in its entirety, holding no sensitive information and cleared to release into our open source repository by their administrators.

Please refer to the [code generation](#apache-cxf-code-generation) section as to how this WSDL is so important for our function.

## Whitelisted tables
Read about which tables we are allowed to access [here](https://dp3.atlassian.net/wiki/spaces/MT/pages/2275573761/TRDM+Soap+Proxy+API+Gateway+Lambda+Function#Accessible-Tables).

See information about the truststore [here](https://dp3.atlassian.net/wiki/spaces/MT/pages/2290483201/Updating+TRDM+Lambda+Function+Trust+Store).

## Apache CXF Code Generation
It is very important to understand the backbone of the SOAP envelope generation. By using the `cxf-codegen-plugin` we can provide a WSDL and it will auto generate us code under `target` that can be used to generate SOAP envelopes.

## Plugin

```
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <version>4.0.3</version>
    <executions>
        <execution>
            <id>generate-sources</id>
            <phase>generate-sources</phase>
            <configuration>
                <sourceRoot>${project.build.directory}/generated-sources/cxf</sourceRoot>
                <wsdlOptions>
                    <wsdlOption>
                        <wsdl>${project.basedir}/src/main/resources/ReturnTableV7.wsdl</wsdl>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
            <goals>
                <goal>wsdl2java</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## CI/CD Known Issue
We have found that CI/CD does not work well when using Apache CXF code generation. The plugin generates code at build time, and since CircleCI handles our actual build and release this means that it will be generated within CircleCI. This is normally not a problem and actually a good thing; however, this plugin makes hard coded file references to the file system directories it was built inside of. So, when CircleCI builds it and releases it the code will have hard coded *file* references to the WSDL such as (Exaggeration) `CircleCI/resources/`, meaning when deployed in AWS it will always fail. So, the way around that is to generate it locally, modify it as necessary, and store it within GitHub. This is why you can find the ReturnTableV7 code generated with that plugin added to our src manually [here](src/main/java/cxf).

After the code was generated and plugged into `src`, we had to manually modify the WSDL references. See examples when you search that directory and sub directories for `classpath:ReturnTableV7.wsdl` - before these were class paths they were file references with hard coded CircleCI values - hence why when it was hosted into AWS Lambda it couldn't find the CircleCI directories.

Additionally, the following static code was needed to grab the WSDL for the class.

```
static {
    URL url = ReturnTable.class.getClassLoader().getResource("ReturnTableV7.wsdl");
    if (url == null) {
        java.util.logging.Logger.getLogger(ReturnTable.class.getName())
            .log(java.util.logging.Level.INFO,
                "Can not initialize the default wsdl from {0}", "classpath:ReturnTableV7.wsdl");
    }
    WSDL_LOCATION = url;
}
```

**In short**, we generated the code with that plugin, imported it into the repository, and manually modified the WSDL file refs to be based on the classpath. Additionally, the imports within the services had to be imported like so:

`import cxf.trdm.returntableservice.ReturnTable;` 

Instead of relying on the target that gets auto generated with the plugin.


## trdm-lambda
Provides a serverless Java application to host an internal RESTful interface. This Java application will handle SOAP-based requests toward TRDM on behalf of our Go MilMove server. It will be hosted as an AWS Lambda function for MilMove API requests to TRDM via SOAP.

### ENV variables

The `application.yml` can store configurations for your env variables. Which includes both secrets and configs.

To add a new variable simply add it to the `application.yml` following the yaml syntax. Then create a new Java class under the `config` package.

If there is a prefix for the config, make sure to add it. Then add all the properties related to the config in this file.

Java will read `my-prop` as `myProp` by default so if you use `-` just follow camel case standards when naming your variables in Java.

### Environment Specific Yaml files

In the `resources` directory you will find multiple different yaml files. The default is `application.yml`. Every other yaml file is environment specific. 

Ex:
- application-stg.yml
- application-prod.yml

If each env requires a different configuration create a new yaml file following the pattern `application-env.yml` where env is the name of your environment. (stg, prod, test, etc).

[Spring documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.properties-and-configuration).

### Application Information

This application utilizes several core Spring Features.
1. Custom Validation for a request body using `Meta Annotations`
2. Controller Advice to handle errors globally
3. Bean represenation of env variables


### Meta Annotations

For the request objects there is some validation that occurs before the application processes it.

In `LastTableUpdateRequest` for ex, you will find the annotation `@PhysicalNameConstraint`. This lets spring know that this field is constrained by the `PhysicalNameConstraint` interface. In this class you will also see `PhysicalNameValidator`. This is where the actual logic is placed. You can customize this logic however you see fit. 

How to apply the custom validation:

1. Create a Constraint.java class
2. Create a Validator.java class
3. Apply constraint annotation to the field you wish to validate.

### Controller Advice

The controller advice is called `ErrorHandler` in the project.

This class can be used as the global generic error handler for simple errors such as validation, internal server errors, or other common error responses. If you wish to have more specific errors you can have the `service` classes throw a custom error and create a new controller advice to handle those specific errors from the service. Just follow how the other methods in `ErrorHandler` are setup to manage the errors.

### Custom Properties

When adding custom properties to the application you can add them directly the the application.yml. (Make sure you add them to the other application-env.yml as well)

Then if you plan on reading them into the application create a `CustomPropProps.java` class to handle the custom props you are loading in.

## Deprecated Endpoints

### lastTableUpdate
[Go here](docs/lastTableUpdate.md)

### getTable
[Go here](docs/getTable.md)

### Running Tests

Before running tests first run the following commands in the mymove repo to set up the test_db if it is not already set up.:
- In mymove repo
1. make db_test_create
2. make db_test_migrate_standalone

After running the commands mentioned above in the mymove repo, run `mvn test` in the trdm-lambda repo to run the tests.

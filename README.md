# Table of Contents
- [Table of Contents](#table-of-contents)
- [API Gateway](#api-gateway)
- [trdm-lambda](#trdm-lambda)
  - [Pre-Requisites](#pre-requisites)
    - [Build](#build)
    - [Run Application Locally](#run-application-locally)
  - [Deploy](#deploy)
  - [ENV variables](#env-variables)
    - [Environment Specific Yaml files](#environment-specific-yaml-files)
  - [Application Information](#application-information)
    - [Meta Annotations](#meta-annotations)
    - [Controller Advice](#controller-advice)
    - [Custom Properties](#custom-properties)
  - [Endpoints](#endpoints)
    - [lastTableUpdate](#lasttableupdate)
    - [getTable](#gettable)
- [Apache CXF Code Generation](#apache-cxf-code-generation)
  - [Plugin](#plugin)
  - [Known Issue](#known-issue)
- [TRDM](#trdm)

# API Gateway
Please see documentation [here](https://dp3.atlassian.net/wiki/spaces/MT/pages/2275573761/TRDM+Soap+Proxy+API+Gateway+Lambda+Function).

# trdm-lambda
Provides a serverless Java application to host an internal RESTful interface. This Java application will handle SOAP-based requests toward TRDM on behalf of our Go MilMove server. It will be hosted as an AWS Lambda function for MilMove API requests to TRDM via SOAP.

## Pre-Requisites

The first step is to download and install [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) and then [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html).

Let’s run AWS SAM CLI on the path where the template.yaml is located and execute the command:
### Build

To build the application ru `sam build` from the root directory or in the same directory as the `template.yml` file.

### Run Application Locally

To run the application locally run `sam local start-api`. It will require AWS credentials to turn on, so either you need `/.aws/credentials` or if using
aws-vault to manage your credentials you need to wrap your start command like so: `aws-vault exec transcom-gov-dev -- sam local start-api`.

## Deploy

To deploy the application to AWS, download the latest release, upload it into the bucket under lambda builds -> trdm-soap-proxy -> Make a new version folder -> upload with proper encryption keys set.

After it has been uploaded into the bucket please update the `trdm_soap_proxy_version` variable in terraform [here](https://github.com/transcom/terraform-aws-app-environment) under `variables.tf`.


## ENV variables

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

## Application Information

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

## Endpoints

### lastTableUpdate
[Go here](docs/lastTableUpdate.md)

### getTable
[Go here](docs/getTable.md)

# Apache CXF Code Generation
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

## Known Issue
The issue with this and why we no longer have the plugin generating code at runtime is that during CI/CD this plugin generates files with hard code file references. This does not play well during compiling as the filenames will use hard coded references to the CircleCI file structure. That is why you can find the ReturnTableV7 code generated and added to our src [here](src/main/java/cxf).

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


# TRDM
We are leveraging the `ReturnTableV7` WSDL provided by TRDM. This file has been verified to be unclassified in its entirety, holding no sensitive information and cleared to release into our open source repository by their administrators.

Please see back to the [code generation](#apache-cxf-code-generation) section as to how this WSDL is so important for our function.
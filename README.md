# trdm-lambda
Provides a serverless Java application to host an internal RESTful interface. This Java application will handle SOAP-based requests toward TRDM on behalf of our Go MilMove server. It will be hosted as an AWS Lambda function for MilMove API requests to TRDM via SOAP.

## Pre-Requisites

The first step is to download and install [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) and then [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html).

Let’s run AWS SAM CLI on the path where the template.yaml is located and execute the command:
### Build

To build the application ru `sam build` from the root directory or in the same directory as the `template.yml` file.

### Run Application Locally

To run the application locally run `sam local start-api`

### Deploy

To deploy the application to AWS use `sam deploy`. Add the `--guided` flag to go through a guided deploy.


## ENV variables

The `application.yml` can store configurations for your env variables. Which includes both secrets and configs.

To add a new variable simply add it to the `application.yml` following the yaml syntax. Then create a new Java class under the `config` pacakge. `TrdmProps.java` is a good example to follow.

If there is a prefix for the config, make sure to add it. Then add all the properties related to the config in this file.

Java will read `my-prop` as `myProp` by default so if you use `-` just follow camel case standards when naming your variables in Java.

### Environment Specific Yaml files

In the `resources` directory you will find multiple different yaml files. The default is `application.yml`. Every other yaml file is environment specific. 

Ex:
- application-stg.yml
- application-prod.yml

If each env requires a different configuration create a new yaml file following the pattern `application-env.yml` where env is the name of your environment. (stg, prod, test, etc).

[Spring documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.properties-and-configuration).

## OpenAPI

Swagger is only available in the local development environment. It is disabled in stg and production.

To view the API definitions you can navigate to either:
1. http://localhost:8080/swagger-ui/index.html - For the Swagger UI
2. http://localhost:8080/v3/api-docs - For the JSON definition of the API


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


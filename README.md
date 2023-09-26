# trdm-lambda
AWS Lambda function for MilMove API requests to TRDM via SOAP.


## Build

To build the application ru `sam build` from the root directory or in the same directory as the `template.yml` file.

## Deploy

To deploy the application to AWS use `sam deploy`. Add the `--guided` flag to go through a guided deploy.


## ENV variables

The `application.yml` can store configurations for your env variables. Which includes both secrets and configs.

To add a new variable simply add it to the `application.yml` following the yaml syntax. Then create a new Java class under the `config` pacakge. `TrdmProps.java` is a good example to follow.

If there is a prefix for the config, make sure to add it. Then add all the properties related to the config in this file.

Java will read `my-prop` as `myProp` by default so if you use `-` just follow camel case standards when naming your variables in Java.
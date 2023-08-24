# TI Invoices Integration
This project handles the integration of the entities related to the
invoices management in trade innovation:

* Invoices.
* Programs.
* Customers.
* ... [TBD]

## Run Locally (Spring boot)

> **Important:**
> Please modify the `application-<env>.yaml` file in `src/main/resources`.

Clone the project
```shell
git clone https://da-reyes@bitbucket.org/tcmpartners/ti-invoices-interface.git
```

Go to the project directory
```shell
cd ti-invoices-interface
```

And start the spring boot application
```shell
./mvnw spring-boot:run
```

## Run Locally (Azure)
This project uses the `spring-cloud-function-adapter-azure-web` dependency, so you can use
the default `RestControllers` from Spring.

To start the project locally with azure functions
```shell
./mvnw -U clean package -DskipTests=true && ./mvnw azure-functions:run
```

## Deploy to Azure Functions
You can deploy the project with the following command (you must be logged in to the azure cli)
```shell
./mvnw azure-functions:deploy
```

## Authors
- David Reyes
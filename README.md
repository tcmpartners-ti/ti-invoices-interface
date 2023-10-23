# TI Integration
This project handles the integration of the entities related to the
invoices management in trade innovation:

* Invoices.
* Programs.
* Customers.

## Run Locally

> **Important:**
> - Please set the required system environment variables for `application-<env>.yaml`
> file in `src/main/resources`.
> - A local gradle installation is required.
> - Access to the development vpn is required.

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
./gradle bootRun
```

## Deploy to Azure Container Registry
Todo

## Authors
- David Reyes
- Franklin Rodríguez
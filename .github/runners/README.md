<h1 align=center>Scala Actions</h1>

This folder contains all the configuration of *Scala Actions*; the infrastructure used to run all the jobs
of The Scala Programming Language.

## The *Scala Actions* GitHub App

https://github.com/apps/scala-actions

## Configuration of the Kubernetes Cluster:

...

## Notes on using pre-defined Kubernetes secrets:
##   You need to make sure your predefined secret has all the required secret data set properly.
##   For a pre-defined secret using GitHub PAT, the secret needs to be created like this:
##   > kubectl create secret generic pre-defined-secret --namespace=my_namespace --from-literal=github_token='ghp_your_pat'
##   For a pre-defined secret using GitHub App, the secret needs to be created like this:
##   > kubectl create secret generic pre-defined-secret --namespace=my_namespace --from-literal=github_app_id=123456 --from-literal=github_app_installation_id=654321 --from-literal=github_app_private_key='-----BEGIN CERTIFICATE-----*******'


## *Scala Actions* Runner Images



TODO: Write this section

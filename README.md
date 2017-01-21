Trainfinder
========================
**This product is currently a work in progress**

This is a web service that utilizes the NS (Dutch public transport provider) public API to try to find out which train a user currently is in. The user input is a coordinate (latitude, longitude) and optionally a direction in which the user is traveling.

The service creates a model of all public trains currently active in the netherlands and their location. Note that this model will be simplified for now (e.g. train tracks are straight lines between stations, the train speed is assumed to be constant between stations).

How to install
---------------
This service is originally aimed created for running on a JBoss/WildFly server. It should be portable to other servers (maybe with minor changes), but I haven't tested this.

In order to connect to the NS public API, you need provide a username and password. This should be provided in the WildFly/JBoss configuration dir (the folder where you put `standalone.xml`):
```
wildfly-10.1.0.Final/standalone/configuration
```
In this folder, you need to create the following file:
```
applications/trainfinder/Configuration.xml
```
Which should have the following content:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<TrainFinder>
	<NSApi>
		<Username>your@email.com</Username>
		<Password>NS.Provided.Password</Password>
	</NSApi>
</TrainFinder>
```
Where you should replace `your@email.com` and `NS.Provided.Password` by your login details.

Now build the repository (`mvn clean package`) and deploy the generated `trainfinder.war` (in the `target` directory) to your application server (either by placing it in `wildfly-10.1.0.Final/standalone/deployments` or by using WildFly/JBoss CLI).

How to use
----------
The functionality is accessible through a REST interface at: `http://host:port/trainfinder/rest/train`. Details of this interface are not yet provided.

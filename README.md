Trainfinder
========================
**This product is currently a work in progress**

This is a web service that utilizes the NS (Dutch public transport provider) public API to try to find out which train a user currently is in. The user input is a coordinate (latitude, longitude) and optionally a direction in which the user is traveling.

The service creates a model of all public trains currently active in the Netherlands and their location. Note that this model will be simplified for now (e.g. the train speed is assumed to be constant between stations).

Prerequisites
-------------
In order to run the application you need to provide two sources of information:

 * __[NS public API](http://www.ns.nl/reisinformatie/ns-api) credentials__: you need to get a username and password for API access, which you can acquire [here](http://www.ns.nl/ews-aanvraagformulier/).
 * __Railway map of the Netherlands__: An [OpenStreetMap](http://www.openstreetmap.org/) XML (.osm) containing (at least) the railways in the Netherlands (`railway=rail` tag on OpenStreetMap). The smaller the file is, the faster it is to process. The easiest way to get a file with just railway tracks in the Netherlands is using the Overpass API:<br>
	<http://www.overpass-api.de/api/xapi?way[railway=rail][bbox=3.33984,50.53438,7.6355,53.67068]>
    

Configuration
-------------
This service is originally aimed created for running on a JBoss/WildFly server. It should be portable to other servers (maybe with minor changes), but I haven't tested this.

You need to provide your [NS public API](http://www.ns.nl/reisinformatie/ns-api) username and password and the location of the railway map file. Configuration is stored in the WildFly/JBoss configuration directory (the folder where you put `standalone.xml`):
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
    <OpenStreetMap>
    	<RailroadFile>/path/to/your/railway-map.osm</RailroadFile>
    </OpenStreetMap>
</TrainFinder>
```
Where you should replace `your@email.com` and `NS.Provided.Password` by your NS API credentials, and `/path/to/your/railway-map.osm` by the path to tour railway map.

Running the application
-----------------------
Build the repository (`mvn clean package`) and deploy the generated `trainfinder.war` (found in the `target` directory) to your application server (either by placing it in `wildfly-10.1.0.Final/standalone/deployments` or by using WildFly/JBoss CLI).

It will be accessible on whatever is the default location for `war` files on your application server. For wildfly it is `host:port/trainfinder/`. In wildfly, you can easily change the context root and virtual server using the `src\main\webapp\WEB-INF\jboss-web.xml` deployment descriptor with the following content:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jboss-web>

    <context-root>/</context-root>
    <virtual-host>trainfinder-host</virtual-host>
    
</jboss-web>
```

When you start the server, it should start reading the railway map and polling stations.

How to use
----------
You can view live train information on a map using `http://host:port/trainfinder/live`.
The functionality is accessible through a REST interface at: `http://host:port/trainfinder/rest/train`. The following resources exists:
 * `rest/train/stations`: returns a JSON array with all known stations
 * `rest/train/trains`: returns a JSON array with information on all known active trains, such as the current position of the train, the previous and next stations, and the ride is in currently on.

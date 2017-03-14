/**
 * 
 */

var trainmap = L.map('trainmap').setView([51.32889547080779, 5.034484863281251], 9);
var trainMarkers = {};
var stationNames = {};

var trainIcon = L.icon({
    iconUrl: '../style/images/train-icon.svg',
    iconSize: [30, 30],
    iconAnchor: [15, 30],
    popupAnchor: [0, -30],
});

L.tileLayer('https://api.mapbox.com/styles/v1/immortaly007/cj02l26qv004m2rmv1cp2k1mv/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1IjoiaW1tb3J0YWx5MDA3IiwiYSI6ImNqMDJsMGVoeDAwOXUycXI0dm1tc3M4bWEifQ.AhylxEtNYHnfSGY0SQkMnA', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 22,
    minZoom: 0
}).addTo(trainmap);


window.setInterval(function(){
	updateTrains();
}, 5000);

$(document).ready(function() {
	getStations()
	updateTrains();
})

function getStations() {
	$.ajax({
		url: "../rest/train/stations",
		context: document.body,
		success: function(stations){	
			for(var i = 0; i < stations.length; i++) {
				var station = stations[i];
				console.log(station + " " + station.code + "=" + station.fullName);
				stationNames[station.code] = station.fullName;
			}
		}
	});
}

function getStationName(stationCode) {
	var stationName = stationNames[stationCode];
	if (stationName == null) {
		stationName = stationCode;
	}
	return stationName;
}

function updateTrains() {
	$.ajax({
		url: "../rest/train/trains",
		context: document.body,
		success: function(trains){
			updateMarkers(trains);
		}
	});
}

function updateMarkers(trains) {
	var rideCodes = [];
	trains.forEach(function(train) {
		var rideCode = train.rideCode;
		rideCodes.push(rideCode);
		if (rideCode in trainMarkers) {
			var trainMarker = trainMarkers[rideCode];
			trainMarker.setLatLng([train.position.lat, train.position.lng]);
			trainMarker.setPopupContent(getPopupContent(train));
		} else {
			var trainMarker = L
				.marker([train.position.lat, train.position.lng], {icon: trainIcon})
				.bindPopup(getPopupContent(train))
				.addTo(trainmap);
			trainMarkers[rideCode] = trainMarker;
		}
	});
	
	for(rideCode in trainMarkers) {
		if (!rideCode in rideCodes) {
			var rideMarker = rideCodes[rideCode];
			if (rideMarker != null) {
				trainmap.removeLayer(rideMarker);
				rideCodes[rideCode] = undefined;
			}
		}
	} 
}

function getPopupContent(train) {
	
	var finalDest = train.stops[train.stops.length - 1];
	var popup = "<p>" + train.carrier + " " + train.trainType + " naar " + getStationName(finalDest.station) + ":</p>"
		+ "<table>";
	
	for (var i = 0; i < train.stops.length; i++) {
		var stop = train.stops[i];
		var isNextStop = train.nextStop == stop.station;
		var departureTime = moment.unix(stop.departureTime);
		popup += "<tr><td>" +
			(isNextStop ? "<b>" : "") +
			getStationName(stop.station) + 
			(isNextStop ? "</b>" : "") +
			"</td><td>" +
			(isNextStop ? "<b>" : "") +
			departureTime.format('LT');
		if (stop.delay != null && stop.delay != 0) {
			popup += "+" + Math.floor(stop.delay / 60) + "min";
		}
		popup += (isNextStop ? "</b>" : "");
		popup += "</td></tr>";
	}
	popup += "</table>";
	
	return popup;	
}

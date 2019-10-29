/**
 * Balena Drones Tracker
 * 
 * @author Michael H. Siemaszko, email: mhs@into.software, www:
 *         https://ideas.into.software/
 */

'use strict';

const dronesLastUpdateMap = new Map();

const dronesArrivedArr = new Array();

const droneMinUpdatePeriod = 10100;

const droneIdleMonitorPeriod = 3000;

var allDronesArrived = false;

var dronesStatusUpdateError = false;

function storeStatusUpdate(droneDataRecord) {
	console.log("Drone ID: " + droneDataRecord.droneId + " has new update with timestamp: " + droneDataRecord.timestamp);
	dronesLastUpdateMap.set(droneDataRecord.droneId, droneDataRecord.timestamp);
}

function constructTableRowId(droneId) {
	return `drone_${droneId}`;
}

function hasRow(rowId) {
	return $(`#${rowId}`).length;
}

function hasArrived(droneDataRecord) {
	return droneDataRecord.arrived;
}

function clearRowAnimations(row) {
	if (row.is(':animated')) {
		row.effect = null;
	}
}

function markDroneAsArrived(droneDataRecord) {
	dronesArrivedArr.push(droneDataRecord.droneId);
	
	console.log("Drone ID " + droneDataRecord.droneId + " arrived at destination!");
	
	// visual feedback about drone having arrived at destination..
	let rowId = constructTableRowId(droneDataRecord.droneId);
	$(`#${rowId}`).removeClass().addClass('arrived');
}

function updateRow(rowId, droneDataRecord) {	
	let existingRow = $(`#${rowId}`);
	
	existingRow.find('.drone_position_lat').text(droneDataRecord.position.latitude);
	existingRow.find('.drone_position_lon').text(droneDataRecord.position.longitude);
	existingRow.find('.drone_speed').text(droneDataRecord.speed);
	
	// visual feedback about drone status update..
	existingRow.removeClass().addClass('updated');
	setTimeout(function() {
		existingRow.removeClass()
	 }, 300);
}

function addRow(rowId, droneDataRecord) {
	let newRowMarkup = `
		<tr id="${rowId}">
			<td class="drone_id">${droneDataRecord.droneId}</td>
			<td class="drone_position"><span class="drone_position_lat">${droneDataRecord.position.latitude}</span>, <span class="drone_position_lon">${droneDataRecord.position.longitude}</span></td>
			<td class="drone_speed">${droneDataRecord.speed}</td>
		</tr>
	`;
	 $("table#drones_list tbody").append(newRowMarkup);
}

function displayRow(droneDataRecord) {
	let rowId = constructTableRowId(droneDataRecord.droneId);
	
	if (hasRow(rowId)) {
		updateRow(rowId, droneDataRecord);
	} else {
		addRow(rowId, droneDataRecord);
	}
}

function browserSupported() {
	$("#error_notsupported").hide();
	$("#drones_list").show();
	$("#drones_legend").show();
}

function monitorInFlightDrones() {
	console.log("Monitoring drones' status..");
	
	let sseMonitor = $.SSE(`/status`, {
		onOpen : function(e) {
			console.log(e);
		},
		events : {
			end : function(e) { // default 'onEnd' does not get called ..
				console.log(e);
				allDronesArrived = true;
				this.close();
			},
			
			error : function(e) { // default 'onError' does not get called ..
				console.log(e);
				dronesStatusUpdateError = true;
			},

			status : function(e) {
				console.log(e);

				let eventData = e.data;
				
				let droneStatusUpdate = JSON.parse(eventData);
				
				storeStatusUpdate(droneStatusUpdate);
				
				if (hasArrived(droneStatusUpdate)) { 
					markDroneAsArrived(droneStatusUpdate);
				} else {
					displayRow(droneStatusUpdate);
				}				
			}
		}
	});

	sseMonitor.start();
}

function isDroneIdle(lastUpdate) {
	let now = Date.now();
	
	if ((now - lastUpdate) > droneMinUpdatePeriod) {
		return true;
	} else {
		return false;
	}
}

function highlightIdleDrone(droneId) {
	let rowId = constructTableRowId(droneId);
	
	if (hasRow(rowId)) {
		
		// visual feedback about drone having arrived at destination..
		$(`#${rowId}`).removeClass().addClass('idle');
	}	
}

function highlightIdleDrones() {
	for (var [droneId, lastUpdateTimestamp] of dronesLastUpdateMap) {
		let droneArrived = dronesArrivedArr.includes(droneId);
		
		let droneIdle = isDroneIdle(lastUpdateTimestamp);
		
		if (!droneArrived && droneIdle) {		
			console.log("Drone ID '" + droneId + "' is idle!");			
			highlightIdleDrone(droneId);
		}
	}
}

function monitorIdleDrones() {	
	if (!allDronesArrived && !dronesStatusUpdateError) {
		
		highlightIdleDrones();
		
		setTimeout(function() {
			monitorIdleDrones();
		  }, droneIdleMonitorPeriod);
	}
}

( function( $, window, document, undefined ) {
	
  let isSupported = function() {
	  return ('Array' in window && 'Map' in window && 'Date' in window && 'EventSource' in window);
  }();	
	
	
  if (isSupported) {
	  browserSupported();
  } else {
	  return false;
  }
  
  monitorInFlightDrones();
  
  monitorIdleDrones();
  
})( jQuery, window, document );


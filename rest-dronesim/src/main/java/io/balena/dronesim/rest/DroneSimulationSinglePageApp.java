package io.balena.dronesim.rest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

@Component(service = DroneSimulationSinglePageApp.class, immediate = true)
@HttpWhiteboardResource(pattern = "/drones/*", prefix = "static")
public class DroneSimulationSinglePageApp {

}

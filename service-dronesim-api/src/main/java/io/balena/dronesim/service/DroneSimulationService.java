package io.balena.dronesim.service;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.util.pushstream.PushStream;

import io.balena.dronesim.dto.DroneStatusUpdateDTO;

@ProviderType
public interface DroneSimulationService {

	PushStream<DroneStatusUpdateDTO> getStatusUpdates();
}

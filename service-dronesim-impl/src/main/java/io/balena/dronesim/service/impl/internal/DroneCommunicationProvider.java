package io.balena.dronesim.service.impl.internal;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.osgi.util.pushstream.SimplePushEventSource;

import io.balena.dronesim.dto.DronePositionDTO;
import io.balena.dronesim.dto.DroneStatusUpdateDTO;

public class DroneCommunicationProvider {
	private DroneConfig config;
	private SimplePushEventSource<DroneStatusUpdateDTO> pushEventSource;
	private CountDownLatch countDownLatch;

	public DroneCommunicationProvider(DroneConfig config, SimplePushEventSource<DroneStatusUpdateDTO> pushEventSource,
			CountDownLatch countDownLatch) {
		this.config = config;
		this.pushEventSource = pushEventSource;
		this.countDownLatch = countDownLatch;
	}

	public void sendInFlightUpdate(DronePositionDTO position) {
		Objects.requireNonNull(position, "Position is required!");
		DroneStatusUpdateDTO statusUpdate = new DroneStatusUpdateDTO();
		statusUpdate.droneId = this.config.getId();
		statusUpdate.position = position;
		statusUpdate.speed = this.config.getSpeed();
		statusUpdate.timestamp = Instant.now().toEpochMilli();

		this.pushEventSource.publish(statusUpdate);
	}

	public void sendArrivedUpdate() {
		DroneStatusUpdateDTO statusUpdate = new DroneStatusUpdateDTO();
		statusUpdate.droneId = this.config.getId();
		statusUpdate.arrived = true;
		statusUpdate.timestamp = Instant.now().toEpochMilli();

		this.pushEventSource.publish(statusUpdate);

		this.countDownLatch.countDown();
	}

}

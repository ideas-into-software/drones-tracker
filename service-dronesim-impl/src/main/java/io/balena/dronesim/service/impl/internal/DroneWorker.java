package io.balena.dronesim.service.impl.internal;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.balena.dronesim.dto.DronePositionDTO;

public class DroneWorker implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(DroneWorker.class);
	private static final double KMH_TO_MS_DIVISOR = 3.6;

	private DroneConfig config;
	private DroneTrack track;
	private DroneCommunicationProvider communicationProvider;

	/**
	 * Constructs a 'drone', initialized with:
	 * 
	 * @param config                configuration
	 * @param track                 track
	 * @param communicationProvider communication provider
	 */
	public DroneWorker(DroneConfig config, DroneTrack track, DroneCommunicationProvider communicationProvider) {
		Objects.requireNonNull(config, "Drone configuration is required!");
		Objects.requireNonNull(track, "Drone track is required!");
		Objects.requireNonNull(communicationProvider, "Drone communication provider is required!");

		this.config = config;
		this.track = track;
		this.communicationProvider = communicationProvider;
	}

	@Override
	public void run() {

		// to simulate time it takes to move between track points (p), let's calculate
		// for how long thread must sleep in between intermediate points..
		long travelTime = calculateTravelTime();

		// now, our 'drone' is ready to 'fly' along a track, consisting of intermediate
		// points (p)..
		track.proceedAlongTrack().forEach(p -> {

			LOG.debug("Drone '" + config.getId() + "' arrived at: " + p.toString());

			/**
			 * .. upon arrival to given point, status update is sent, consisting of drone's
			 * position in a 3-D axis (latitude, longitude, altitude)
			 * 
			 * .. all other information comes from drone configuration, so no need to pass
			 * this each time, instead, drone's id, its speed, as well as timestamp - to
			 * determine when most recent update was sent - are set in the underlying
			 * communication mechanism
			 * 
			 */
			DronePositionDTO position = new DronePositionDTO();
			position.latitude = p.latitude;
			position.longitude = p.longitude;
			position.altitude = this.config.getAltitude();

			this.track.addTrackPointPassed(p);

			this.communicationProvider.sendInFlightUpdate(position);

			try {

				LOG.debug("Drone '" + config.getId() + "' passed " + this.track.getTrackPointsPassedCount()
						+ " point(s).. ");

				// 'hover' our 'drone' over a given point for a bit ..
				if (shouldDroneHover()) {
					LOG.debug("Drone '" + config.getId() + "' will hover now for: "
							+ (this.config.getHoverTime() / 1000) + " second(s)..");
					TimeUnit.MILLISECONDS.sleep(this.config.getHoverTime());

					// .. otherwise, our 'drone' continues on it's journey..
				} else {
					LOG.debug("Drone '" + config.getId() + "' proceeds to next point.. ");
					TimeUnit.MILLISECONDS.sleep(travelTime);
				}

			} catch (InterruptedException e) {
				LOG.error(e.getMessage());
			}
		});

		// finally, our 'drone' arrived at destination..
		LOG.debug("Drone '" + config.getId() + "' arrived at destination.. ");
		this.communicationProvider.sendArrivedUpdate();
	}

	/**
	 * Check whether drone should hover.
	 * 
	 * @return true if drone should hover, false otherwise
	 */
	private boolean shouldDroneHover() {
		return ((this.track.getTrackPointsTotalCount() > this.track.getTrackPointsPassedCount())
				&& (this.track.getTrackPointsPassedCount() % this.config.getHoverPointsPassedCount()) == 0);
	}

	/**
	 * Calculate speed in m / s (meters per second).
	 * 
	 * Configuration specifies speed in km / h for easier readability, hence the
	 * conversion.
	 * 
	 * @return speed in m/s
	 */
	private double calculateSpeedMS() {
		return (config.getSpeed() / KMH_TO_MS_DIVISOR);
	}

	/**
	 * Calculate for how long thread must sleep in between intermediate points.
	 * 
	 * This is determined by speed and length of 'segment' (distance between
	 * intermediate points).
	 * 
	 * This is used to simulate time it takes to move between track points.
	 * 
	 * @return thread sleep time
	 */
	private long calculateTravelTime() {
		return (new Double(this.track.getTrackSegmentLength() / calculateSpeedMS()).longValue() * 1000);
	}
}

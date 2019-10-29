package io.balena.dronesim.service.impl.internal;

import java.util.Map;
import java.util.Objects;

public class DroneConfig {
	private String id;
	private double latitudeStart;
	private double longitudeStart;
	private double latitudeEnd;
	private double longitudeEnd;
	private double altitude;
	private double speed;
	private int hoverPointsPassedCount;
	private long hoverTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getLatitudeStart() {
		return latitudeStart;
	}

	public void setLatitudeStart(double latitudeStart) {
		this.latitudeStart = latitudeStart;
	}

	public double getLongitudeStart() {
		return longitudeStart;
	}

	public void setLongitudeStart(double longitudeStart) {
		this.longitudeStart = longitudeStart;
	}

	public double getLatitudeEnd() {
		return latitudeEnd;
	}

	public void setLatitudeEnd(double latitudeEnd) {
		this.latitudeEnd = latitudeEnd;
	}

	public double getLongitudeEnd() {
		return longitudeEnd;
	}

	public void setLongitudeEnd(double longitudeEnd) {
		this.longitudeEnd = longitudeEnd;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getHoverPointsPassedCount() {
		return hoverPointsPassedCount;
	}

	public void setHoverPointsPassedCount(int hoverPointsPassedCount) {
		this.hoverPointsPassedCount = hoverPointsPassedCount;
	}

	public long getHoverTime() {
		return hoverTime;
	}

	public void setHoverTime(long hoverTime) {
		this.hoverTime = hoverTime;
	}

	public static DroneConfig newInstance(Map<String, String> config) {
		Objects.requireNonNull(config, "Configuration is required!");

		DroneConfig droneConfig = new DroneConfig();

		config.keySet().forEach(key -> {
			String value = config.get(key);

			switch (key) {
			case "id":
				droneConfig.setId(value);
				break;

			case "latitudeStart":
				droneConfig.setLatitudeStart(Double.valueOf(value));
				break;

			case "longitudeStart":
				droneConfig.setLongitudeStart(Double.valueOf(value));
				break;

			case "latitudeEnd":
				droneConfig.setLatitudeEnd(Double.valueOf(value));
				break;

			case "longitudeEnd":
				droneConfig.setLongitudeEnd(Double.valueOf(value));
				break;

			case "altitude":
				droneConfig.setAltitude(Double.valueOf(value));
				break;

			case "speed":
				droneConfig.setSpeed(Double.valueOf(value));
				break;
			}

		});

		Objects.requireNonNull(droneConfig.getId(), "Drone ID is required!");
		Objects.requireNonNull(droneConfig.getLatitudeStart(), "Start latitude is required!");
		Objects.requireNonNull(droneConfig.getLongitudeStart(), "Start longitude is required!");
		Objects.requireNonNull(droneConfig.getLatitudeEnd(), "End latitude is required!");
		Objects.requireNonNull(droneConfig.getLongitudeEnd(), "End longitude is required!");
		Objects.requireNonNull(droneConfig.getAltitude(), "Altitude is required!");
		Objects.requireNonNull(droneConfig.getSpeed(), "Speed is required!");

		return droneConfig;
	}
}
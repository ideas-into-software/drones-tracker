package io.balena.dronesim.service.impl.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

public class DroneTrackCalculator {
	private static final Logger LOG = LoggerFactory.getLogger(DronesConfigReader.class);

	private DroneConfig droneConfig;
	private int segmentLength;

	public DroneTrackCalculator(DroneConfig droneConfig, int segmentLength) {
		Objects.requireNonNull(droneConfig, "Drones configuration is required!");

		this.droneConfig = droneConfig;
		this.segmentLength = segmentLength;
	}

	public DroneTrack calculateTrack() {
		Point startPoint = buildStartPoint();
		LOG.debug("Start point: " + startPoint.toString());

		Point endPoint = buildEndPoint();
		LOG.debug("End point: " + endPoint.toString());

		double bearing = calculateBearing(startPoint, endPoint);
		LOG.debug("Azimuth bearing: " + bearing); // in decimal degrees

		double distance = calculateDistance(startPoint, endPoint);
		LOG.debug("Distance: " + distance); // in meters

		double[] segments = calculateSegments(distance);
		LOG.debug("Calculated " + segments.length + " segment(s)");

		List<Point> track = calculateTrack(startPoint, bearing, segments);
		LOG.debug("Track consists of " + track.size() + " point(s)");
		if (LOG.isTraceEnabled()) {
			track.forEach(p -> LOG.trace(p.toString()));
		}

		return new DroneTrack(track, segmentLength);
	}

	private List<Point> calculateTrack(Point startPoint, double bearing, double[] segments) {
		List<Point> track = Arrays.stream(segments).mapToObj(segment -> calculatePointAt(startPoint, bearing, segment))
				.collect(Collectors.toList());

		return track;
	}

	private Point calculatePointAt(Point startPoint, double bearing, double segment) {
		return EarthCalc.pointAt(startPoint, bearing, segment);
	}

	private double[] calculateSegments(double distance) {
		List<Double> segments = new ArrayList<Double>();
		for (double segment = 0; segment <= distance; segment += segmentLength) {
			segments.add(Double.valueOf(segment));
		}

		segments.add(Double.valueOf(distance));

		return segments.stream().mapToDouble(i -> i).toArray();
	}

	private double calculateDistance(Point startPoint, Point endPoint) {
		return EarthCalc.vincentyDistance(startPoint, endPoint);
	}

	private double calculateBearing(Point startPoint, Point endPoint) {
		return EarthCalc.vincentyBearing(startPoint, endPoint);
	}

	private Point buildStartPoint() {
		Coordinate latitude = Coordinate.fromDegrees(droneConfig.getLatitudeStart());
		Coordinate longitude = Coordinate.fromDegrees(droneConfig.getLongitudeStart());

		return buildPoint(latitude, longitude);
	}

	private Point buildEndPoint() {
		Coordinate latitude = Coordinate.fromDegrees(droneConfig.getLatitudeEnd());
		Coordinate longitude = Coordinate.fromDegrees(droneConfig.getLongitudeEnd());

		return buildPoint(latitude, longitude);
	}

	private Point buildPoint(Coordinate latitude, Coordinate longitude) {
		return Point.at(latitude, longitude);
	}
}

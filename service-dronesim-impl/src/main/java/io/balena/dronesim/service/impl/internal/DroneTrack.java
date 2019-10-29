package io.balena.dronesim.service.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.grum.geocalc.Point;

public class DroneTrack {
	private List<Point> track;
	private int trackSegmentLength;
	private List<Point> trackPointsPassed;

	public DroneTrack(List<Point> track, int trackSegmentLength) {
		this.track = track;
		this.trackSegmentLength = trackSegmentLength;
		this.trackPointsPassed = new ArrayList<Point>();
	}

	public Stream<Point> proceedAlongTrack() {
		return this.track.stream();
	}

	public int getTrackSegmentLength() {
		return trackSegmentLength;
	}

	public int getTrackPointsTotalCount() {
		return this.track.size();
	}

	public void addTrackPointPassed(Point p) {
		this.trackPointsPassed.add(p);
	}

	public int getTrackPointsPassedCount() {
		return this.trackPointsPassed.size();
	}
}

package io.balena.drones.service.simulation.impl.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import io.balena.dronesim.service.impl.internal.DroneConfig;
import io.balena.dronesim.service.impl.internal.DroneTrack;
import io.balena.dronesim.service.impl.internal.DroneTrackCalculator;
import io.balena.dronesim.service.impl.internal.DronesConfigReader;

public class DroneTrackCalculatorTest {
	private static final String BALENA_DRONES_CONFIG_FILENAME = "balena-drones-test.properties";
	private static final String BALENA_DRONES_CONFIG_KEY = "balena.drones";
	private static final int BALENA_DRONES_SEGMENT_LENGTH = 25;

	@Test
	public void testCalculateTrack() throws URISyntaxException {

		Path dronesConfigFilePath = Paths.get(ClassLoader.getSystemResource(BALENA_DRONES_CONFIG_FILENAME).toURI());
		assertNotNull(dronesConfigFilePath);

		DronesConfigReader dronesConfigReader = new DronesConfigReader(dronesConfigFilePath, BALENA_DRONES_CONFIG_KEY);
		List<DroneConfig> dronesConfig = dronesConfigReader.readConfig();
		assertEquals(2, dronesConfig.size());

		DroneConfig droneConfig = dronesConfig.get(0);

		DroneTrackCalculator dronesTrackCalculator = new DroneTrackCalculator(droneConfig,
				BALENA_DRONES_SEGMENT_LENGTH);
		DroneTrack droneTrack = dronesTrackCalculator.calculateTrack();
		assertEquals(110, droneTrack.getTrackPointsTotalCount());
	}
}

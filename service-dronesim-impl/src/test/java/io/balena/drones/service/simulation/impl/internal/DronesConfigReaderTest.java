package io.balena.drones.service.simulation.impl.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import io.balena.dronesim.service.impl.internal.DroneConfig;
import io.balena.dronesim.service.impl.internal.DronesConfigReader;

public class DronesConfigReaderTest {
	private static final String BALENA_DRONES_CONFIG_FILENAME = "balena-drones-test.properties";
	private static final String BALENA_DRONES_CONFIG_KEY = "balena.drones";
	private static final double DELTA = 0.00000f;

	@Test
	public void testReadConfig() throws URISyntaxException {

		Path dronesConfigFilePath = Paths.get(ClassLoader.getSystemResource(BALENA_DRONES_CONFIG_FILENAME).toURI());
		assertNotNull(dronesConfigFilePath);

		DronesConfigReader dronesConfigReader = new DronesConfigReader(dronesConfigFilePath, BALENA_DRONES_CONFIG_KEY);
		List<DroneConfig> dronesConfig = dronesConfigReader.readConfig();
		assertEquals(2, dronesConfig.size());

		DroneConfig droneConfig = dronesConfig.get(0);
		assertEquals("TestDrone1", droneConfig.getId());

		assertEquals(51.4843774, droneConfig.getLatitudeStart(), DELTA);
		assertEquals(-0.2912044, droneConfig.getLongitudeStart(), DELTA);

		assertEquals(51.4613418, droneConfig.getLatitudeEnd(), DELTA);
		assertEquals(-0.3035466, droneConfig.getLongitudeEnd(), DELTA);

		assertEquals(220, droneConfig.getAltitude(), DELTA);

		assertEquals(29, droneConfig.getSpeed(), DELTA);
	}

}

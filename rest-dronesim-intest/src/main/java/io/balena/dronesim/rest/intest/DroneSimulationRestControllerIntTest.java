package io.balena.dronesim.rest.intest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.util.tracker.ServiceTracker;

import io.balena.dronesim.service.DroneSimulationService;

/**
 * JUnit test run inside an OSGi framework.
 */
public class DroneSimulationRestControllerIntTest {

	private final Bundle bundle = FrameworkUtil.getBundle(this.getClass());

	private DroneSimulationService mockDroneSimulationService;

	private ServiceRegistration<DroneSimulationService> registration;

	private ServiceTracker<JaxrsServiceRuntime, JaxrsServiceRuntime> runtimeTracker;

	private ServiceTracker<SseEventSourceFactory, SseEventSourceFactory> sseEventSourceFactoryTracker;

	private JaxrsServiceRuntime jaxrsServiceRuntime;

	private SseEventSourceFactory sseEventSourceFactory;

	@Before
	public void setUp() throws Exception {
		assertNotNull("OSGi Bundle tests must be run inside an OSGi framework", bundle);

		mockDroneSimulationService = mock(DroneSimulationService.class);

		runtimeTracker = new ServiceTracker<>(bundle.getBundleContext(), JaxrsServiceRuntime.class, null);
		runtimeTracker.open();

		sseEventSourceFactoryTracker = new ServiceTracker<>(bundle.getBundleContext(), SseEventSourceFactory.class,
				null);
		sseEventSourceFactoryTracker.open();

		jaxrsServiceRuntime = runtimeTracker.waitForService(2000);
		assertNotNull(jaxrsServiceRuntime);

	}

	@After
	public void tearDown() throws Exception {
		runtimeTracker.close();

		sseEventSourceFactoryTracker.close();

		if (registration != null) {
			registration.unregister();
		}
	}

	private void registerDroneSimulationService() {
		registration = bundle.getBundleContext().registerService(DroneSimulationService.class,
				mockDroneSimulationService, null);
	}

	@Test
	public void testRestServiceRegistered() throws Exception {

		assertEquals(0, jaxrsServiceRuntime.getRuntimeDTO().defaultApplication.resourceDTOs.length);

		registerDroneSimulationService();

		assertEquals(1, jaxrsServiceRuntime.getRuntimeDTO().defaultApplication.resourceDTOs.length);
	}

	@Test
	public void testSseEventSourceFactoryRegistered() throws Exception {

		sseEventSourceFactory = sseEventSourceFactoryTracker.getService();

		assertNotNull(sseEventSourceFactory);
	}
}

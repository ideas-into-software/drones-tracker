package io.balena.dronesim.service.impl;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.pushstream.PushStream;
import org.osgi.util.pushstream.PushStreamProvider;
import org.osgi.util.pushstream.SimplePushEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.balena.dronesim.dto.DroneStatusUpdateDTO;
import io.balena.dronesim.service.DroneSimulationService;
import io.balena.dronesim.service.impl.internal.DroneCommunicationProvider;
import io.balena.dronesim.service.impl.internal.DroneConfig;
import io.balena.dronesim.service.impl.internal.DroneTrack;
import io.balena.dronesim.service.impl.internal.DroneTrackCalculator;
import io.balena.dronesim.service.impl.internal.DroneWorker;
import io.balena.dronesim.service.impl.internal.DronesConfigReader;

@Component(immediate = true, configurationPid = {
		"io.balena.drones.service.simulation" }, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = DroneSimulationServiceImpl.ServiceConfig.class)
public class DroneSimulationServiceImpl implements DroneSimulationService {
	private static final Logger LOG = LoggerFactory.getLogger(DroneSimulationServiceImpl.class);

	private ServiceConfig serviceConfig;

	private List<DroneConfig> dronesConfig;

	private ExecutorService dronesExec;

	private CountDownLatch dronesExecCountDownLatch;

	private PushStreamProvider dronesPushStreamProvider;

	private SimplePushEventSource<DroneStatusUpdateDTO> dronesPushEventSource;

	@ObjectClassDefinition
	@interface ServiceConfig {

		@AttributeDefinition(required = true)
		String dronesConfigFilePath();

		@AttributeDefinition(required = false)
		String dronesConfigKeyPrefix() default "balena.drones";

		@AttributeDefinition(required = false)
		int dronesTrackSegmentLength() default 100; // in meters

		@AttributeDefinition(required = false)
		int dronesHoverPointsPassedCount() default 20;

		@AttributeDefinition(required = false)
		long dronesHoverTime() default 20000; // in milliseconds
	}

	@Activate
	protected void activate(ServiceConfig config) {
		this.serviceConfig = config;

		LOG.debug("Reading drones' configuration..");
		DronesConfigReader dronesConfigReader = new DronesConfigReader(this.serviceConfig.dronesConfigFilePath(),
				this.serviceConfig.dronesConfigKeyPrefix());
		this.dronesConfig = dronesConfigReader.readConfig();

		int dronesCount = this.dronesConfig.size();
		LOG.debug(dronesCount + " drone(s) will be started..");

		LOG.debug("Drones will hover every " + this.serviceConfig.dronesHoverPointsPassedCount() + " point(s).. ");

		this.dronesPushStreamProvider = new PushStreamProvider();

		this.dronesPushEventSource = this.dronesPushStreamProvider.createSimpleEventSource(DroneStatusUpdateDTO.class);

		this.dronesExec = Executors.newFixedThreadPool(dronesCount);

		this.dronesExecCountDownLatch = new CountDownLatch(dronesCount);

		this.dronesPushEventSource.connectPromise().then(this::startDrones);
	}

	@Override
	public PushStream<DroneStatusUpdateDTO> getStatusUpdates() {
		return this.dronesPushStreamProvider.createStream(this.dronesPushEventSource);
	}

	@Deactivate
	protected void deactivate() {
		this.dronesPushEventSource.close();
		if (!this.dronesExec.isShutdown()) {
			this.dronesExec.shutdown();
		}
	}

	private Promise<Void> startDrones(Promise<Void> connectPromise) {

		new Thread(() -> {

			this.dronesConfig.forEach(dc -> {

				// pass global config to drone configuration..
				dc.setHoverPointsPassedCount(this.serviceConfig.dronesHoverPointsPassedCount());
				dc.setHoverTime(this.serviceConfig.dronesHoverTime());

				LOG.debug("Calculating track for drone: " + dc.getId());
				DroneTrackCalculator dronesTrackCalculator = new DroneTrackCalculator(dc,
						this.serviceConfig.dronesTrackSegmentLength());
				DroneTrack dt = dronesTrackCalculator.calculateTrack();

				LOG.debug("Initializing communication provider for drone: " + dc.getId());
				DroneCommunicationProvider dcp = new DroneCommunicationProvider(dc, dronesPushEventSource,
						dronesExecCountDownLatch);

				LOG.debug("Starting drone: " + dc.getId());
				this.dronesExec.execute(new DroneWorker(dc, dt, dcp));
			});

			try {

				this.dronesExecCountDownLatch.await();

				this.dronesPushEventSource.endOfStream();

			} catch (InterruptedException e) {
				this.dronesPushEventSource.error(e);
				LOG.error(e.getMessage());
			}

		}).start();

		return connectPromise;
	}
}

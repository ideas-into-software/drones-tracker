package io.balena.dronesim.rest.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.junit.Test;

import io.balena.dronesim.dto.DronePositionDTO;
import io.balena.dronesim.dto.DroneStatusUpdateDTO;

public class JsonConverterTest {
	private static final double DELTA = 0.00000f;

	@Test
	public void testSerDeser() throws WebApplicationException, IOException {

		JsonConverter<DroneStatusUpdateDTO> jsonConverter = new JsonConverter<DroneStatusUpdateDTO>();

		long now = Instant.now().toEpochMilli();

		DroneStatusUpdateDTO statusUpdate = new DroneStatusUpdateDTO();
		statusUpdate.droneId = "TestDrone1";

		DronePositionDTO position = new DronePositionDTO();
		position.latitude = 50.302302;
		position.longitude = -0.103023;
		position.altitude = 200;

		statusUpdate.position = position;

		statusUpdate.speed = 25;

		statusUpdate.timestamp = now;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		jsonConverter.writeTo(statusUpdate, DroneStatusUpdateDTO.class, DroneStatusUpdateDTO.class, null,
				MediaType.APPLICATION_JSON_TYPE, null, baos);

		DroneStatusUpdateDTO deserd = jsonConverter.readFrom(DroneStatusUpdateDTO.class, DroneStatusUpdateDTO.class, null,
				MediaType.APPLICATION_JSON_TYPE, null, new ByteArrayInputStream(baos.toByteArray()));

		assertEquals("TestDrone1", deserd.droneId);
		assertEquals(25, deserd.speed, DELTA);
		assertEquals(now, deserd.timestamp);

		DronePositionDTO positionDeserd = deserd.position;

		assertEquals(50.302302, positionDeserd.latitude, DELTA);
		assertEquals(-0.103023, positionDeserd.longitude, DELTA);
		assertEquals(200, positionDeserd.altitude, DELTA);
	}

}

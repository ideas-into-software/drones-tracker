package io.balena.dronesim.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.osgi.util.pushstream.PushStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.balena.dronesim.dto.DroneStatusUpdateDTO;
import io.balena.dronesim.rest.internal.JsonConverter;
import io.balena.dronesim.service.DroneSimulationService;

@Component(service = DroneSimulationRestController.class)
@JaxrsResource
@Path("status")
@Produces(MediaType.SERVER_SENT_EVENTS)
public class DroneSimulationRestController {
	private static final Logger LOG = LoggerFactory.getLogger(DroneSimulationRestController.class);

	private JsonConverter<DroneStatusUpdateDTO> jsonConverter = new JsonConverter<DroneStatusUpdateDTO>();

	@Reference
	private DroneSimulationService droneSimulationService;

	@Context
	private Sse sse;

	@Context
	private SseEventSink sink;

	@GET
	public void getStatusUpdates(@Context SseEventSink sink) {
		this.sink = sink;

		PushStream<DroneStatusUpdateDTO> pushStream = this.droneSimulationService.getStatusUpdates();
		pushStream.forEach(this::deliverEvent).onFailure(this::failure).onResolve(this::resolved);
	}

	private void deliverEvent(DroneStatusUpdateDTO statusUpdate) {
		LOG.debug("Delivering: " + statusUpdate);

		OutboundSseEvent event = this.sse.newEvent("status", convertToJSONString(statusUpdate));
		this.sink.send(event);
	}

	private void failure(Throwable t) {
		LOG.debug("Error: " + t.getMessage());

		OutboundSseEvent event = this.sse.newEvent("error", t.getMessage());
		this.sink.send(event);
	}

	private void resolved() {
		LOG.debug("End of stream");

		OutboundSseEvent event = this.sse.newEvent("end", "End of Stream");
		this.sink.send(event);
	}

	private String convertToJSONString(DroneStatusUpdateDTO statusUpdate) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			jsonConverter.writeTo(statusUpdate, DroneStatusUpdateDTO.class, DroneStatusUpdateDTO.class, null,
					MediaType.APPLICATION_JSON_TYPE, null, baos);

			return baos.toString();

		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

		return null;
	}

}

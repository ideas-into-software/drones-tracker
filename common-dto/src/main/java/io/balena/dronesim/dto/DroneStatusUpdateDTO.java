package io.balena.dronesim.dto;

import org.osgi.dto.DTO;

public class DroneStatusUpdateDTO extends DTO {
	public String droneId;
	public DronePositionDTO position;
	public double speed;
	public long timestamp;
	public boolean arrived;
}

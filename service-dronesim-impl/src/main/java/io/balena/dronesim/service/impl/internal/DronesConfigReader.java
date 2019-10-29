package io.balena.dronesim.service.impl.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DronesConfigReader {
	private static final Logger LOG = LoggerFactory.getLogger(DronesConfigReader.class);

	private Path dronesConfigFilePath;
	private String dronesConfigKeyPrefix;

	public DronesConfigReader(String dronesConfigFilePath, String dronesConfigKeyPrefix) {
		this(Paths.get(dronesConfigFilePath), dronesConfigKeyPrefix);
	}

	public DronesConfigReader(Path dronesConfigFilePath, String dronesConfigKeyPrefix) {
		Objects.requireNonNull(dronesConfigFilePath, "Drones' configuration file path is required!");
		Objects.requireNonNull(dronesConfigKeyPrefix, "Drones' configuration key prefix is required!");

		this.dronesConfigFilePath = dronesConfigFilePath;
		this.dronesConfigKeyPrefix = dronesConfigKeyPrefix;
	}

	public List<DroneConfig> readConfig() {
		List<DroneConfig> dronesConfig = new ArrayList<DroneConfig>();

		LOG.debug("Reading drones' configuration from: " + dronesConfigFilePath);

		try (InputStream input = new FileInputStream(dronesConfigFilePath.toFile())) {

			Properties prop = new Properties();

			Objects.requireNonNull(input, "Unable to find configuration file!");

			prop.load(input);

			if (prop.isEmpty()) {
				throw new IllegalArgumentException("Drones' configuration is empty!");
			}

			int maxIndex = calculateMaxIndex(prop.keySet());
			if (maxIndex <= 0) {
				throw new IllegalArgumentException("Drones' configuration is malformed!");
			}

			LOG.debug("Max index is: " + maxIndex);

			for (int i = 1; i <= maxIndex; i++) {
				String keyName = constructKeyName(i);
				LOG.debug("Constructed key name: " + keyName);

				Map<String, String> subset = getSubset(prop, keyName);

				dronesConfig.add(DroneConfig.newInstance(subset));
			}

			if (LOG.isDebugEnabled()) {
				dronesConfig.forEach(dc -> {
					LOG.debug("Drone ID: " + dc.getId());
					LOG.debug("Latitude (start): " + dc.getLatitudeStart());
					LOG.debug("Longitude (start): " + dc.getLongitudeStart());
					LOG.debug("Latitude (end): " + dc.getLatitudeEnd());
					LOG.debug("Longitude (end): " + dc.getLongitudeEnd());
					LOG.debug("Altitude: " + dc.getAltitude());
					LOG.debug("Speed: " + dc.getSpeed());
					LOG.debug("---");
				});
			}

		} catch (IOException ex) {
			LOG.error(ex.getMessage());
		}

		return dronesConfig;
	}

	private int calculateMaxIndex(Set<Object> keys) {
		return keys.stream().map(key -> getKeyIndex((String) key)).max(Comparator.comparing(Integer::valueOf)).get();
	}

	private Map<String, String> getSubset(Properties prop, String key) {
		return prop.entrySet().stream().filter(entry -> (String.valueOf(entry.getKey())).startsWith(key))
				.collect(Collectors.toMap(entry -> String.valueOf(entry.getKey()).replaceFirst(key, ""),
						entry -> String.valueOf(entry.getValue())));
	}

	private int getKeyIndex(String key) {
		String[] keyArr = key.split("\\.");
		if (keyArr.length > 0) {
			return Integer.parseInt(keyArr[2]);
		} else {
			return -1;
		}
	}

	private String constructKeyName(int index) {
		StringBuilder sb = new StringBuilder();
		sb.append(dronesConfigKeyPrefix).append(".").append(index).append(".");
		return sb.toString();
	}
}

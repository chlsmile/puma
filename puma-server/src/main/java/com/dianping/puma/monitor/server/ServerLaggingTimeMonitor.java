package com.dianping.puma.monitor.server;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.exception.ConfigException;
import com.dianping.puma.core.monitor.HeartbeatMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Calendar;

@Service("serverLaggingTimeMonitor")
public class ServerLaggingTimeMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(ServerLaggingTimeMonitor.class);

	public static final String SERVER_LAGGING_TIME_THRESHOLD = "puma.server.serverlagging.time.threshold";

	private long serverLaggingTimeThreshold;

	private HeartbeatMonitor heartbeatMonitor;

	private String title;

	private int delay;

	private int period;

	public ServerLaggingTimeMonitor() {}

	@PostConstruct
	public void init() throws ConfigException {
		try {
			serverLaggingTimeThreshold = ConfigCache.getInstance().getLongProperty(SERVER_LAGGING_TIME_THRESHOLD);

			ConfigCache.getInstance().addChange(new ConfigChange() {
				@Override
				public void onChange(String key, String value) {
					if (key.equals(SERVER_LAGGING_TIME_THRESHOLD)) {
						serverLaggingTimeThreshold = Long.parseLong(value);
					}
				}
			});
		} catch (LionException e) {
			LOG.error("Lion gets values error: {}.", e.getMessage());
			throw new ConfigException(String.format("Lion gets values error: %s.", e.getCause()));
		}

		title = "ServerLagging.time";
		delay = 60;
		period = 60;
		start();
	}

	public void start() {
		heartbeatMonitor = new HeartbeatMonitor(title, delay, period);
		heartbeatMonitor.start();
	}

	public void stop() {
		heartbeatMonitor.stop();
	}

	public void record(String taskName, long execTime) {
		heartbeatMonitor.record(taskName, genStatus(execTime));
	}

	public long getServerLaggingTimeThreshold() {
		return serverLaggingTimeThreshold;
	}

	public void setServerLaggingTimeThreshold(long serverLaggingTimeThreshold) {
		this.serverLaggingTimeThreshold = serverLaggingTimeThreshold;
	}

	private String genStatus(long execSeconds) {
		long diff = System.currentTimeMillis() / 1000 - execSeconds;
		if (diff < serverLaggingTimeThreshold) {
			return "0";
		} else {
			return "1";
		}
	}
}
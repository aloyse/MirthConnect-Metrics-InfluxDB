package lu.chdn.eai.Util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

public class DirectoryInfo {

	private Path path;
	private long minUpdateInterval;
	private long lastUpdate = 0L;
	private int fileCount = 0;
	private int nonFileCount = 0;
	private long totalBytes = 0L;
	private long minAge = Long.MAX_VALUE;
	private long maxAge = 0L;

	public Path getPath() {
		return path;
	}

	public long getMinUpdateInterval() {
		return minUpdateInterval;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public int getFileCount() {
		return fileCount;
	}

	public int getNonFileCount() {
		return nonFileCount;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public long getMinAge() {
		return minAge;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public DirectoryInfo(String pathName) throws IOException {
		this(pathName, 15000L);
	}

	public DirectoryInfo(String pathName, long minUpdateInterval)
			throws IOException {
		this.minUpdateInterval = minUpdateInterval;
		path = Paths.get(pathName);
		update();
	}

	public void update() throws IOException {
		long now = new Date().getTime();
		if (now - lastUpdate > minUpdateInterval) {
			lastUpdate = now;

			fileCount = 0;
			nonFileCount = 0;
			totalBytes = 0L;
			minAge = Long.MAX_VALUE;
			maxAge = 0L;
			
			DirectoryStream<Path> ds = Files.newDirectoryStream(path);
			Iterator<Path> it = ds.iterator();
			while (it.hasNext()) {
				Path p = it.next();
				BasicFileAttributes attrs = Files.readAttributes(p,
						BasicFileAttributes.class);
				if (attrs.isRegularFile()) {
					fileCount += 1;
					totalBytes += attrs.size();
					long age = now - attrs.creationTime().toMillis();
					if (age > maxAge)
						maxAge = age;
					if (age < minAge)
						minAge = age;
				} else {
					nonFileCount += 1;
				}
			}
			ds.close();
		}
	}

	public Map<String,Gauge<?>> registerGauges(MetricRegistry registry) {
		
		final HashMap<String,Gauge<?>> map = new HashMap<>();
		String metricName = "fileCount";
		
		metricName = "fileCount";
		map.put(metricName,	registry.register(metricName, new Gauge<Integer>() {
		    @Override
		    public Integer getValue() {
		    	int value = 0;
		    	try {
					update();
					value=getFileCount();
				} 
		    	catch (IOException e) {}
		        return value;
		    }
		}));

		metricName = "nonFileCount";
		map.put(metricName, registry.register(metricName, new Gauge<Integer>() {
		    @Override
		    public Integer getValue() {
		    	int value = 0;
		    	try {
					update();
					value=getNonFileCount();
				} 
		    	catch (IOException e) {}
		        return value;
		    }
		}));

		metricName = "totalBytes";
		map.put(metricName, registry.register(metricName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	Long value = 0L;
		    	try {
					update();
					value=getTotalBytes();
				} 
		    	catch (IOException e) {}
		        return value;
		    }
		}));

		metricName = "minAge";
		map.put(metricName, registry.register(metricName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	Long value = 0L;
		    	try {
					update();
					value=getMinAge();
				} 
		    	catch (IOException e) {}
		        return value;
		    }
		}));

		metricName = "maxAge";
		map.put(metricName, registry.register(metricName, new Gauge<Long>() {
		    @Override
		    public Long getValue() {
		    	Long value = 0L;
		    	try {
					update();
					value=getMaxAge();
				} 
		    	catch (IOException e) {}
		        return value;
		    }
		}));

		return map;
	}
}

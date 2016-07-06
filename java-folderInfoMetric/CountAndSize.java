package filecountandsize;

import java.io.IOException;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import lu.chdn.eai.Util.DirectoryInfo;

public class CountAndSize {

	private static String dir= "C:\\tmp\\testDirInfo\\subFolder";

	public static void main(String[] args) throws IOException {
		
		DirectoryInfo di = new DirectoryInfo(dir, 100);
			
		System.out.printf("Folder: %s\n",di.getPath().toAbsolutePath());

		System.out.printf("%d files\n",di.getFileCount());
		System.out.printf("%d non-files\n",di.getNonFileCount());
		System.out.printf("%.3f MBytes\n",(double)di.getTotalBytes()/1024/1024);
		System.out.printf("minimun age: %d s/ %d hours\n",di.getMinAge()/1000,di.getMinAge()/1000/3600);
		System.out.printf("maximun age: %d s/ %d hours\n",di.getMaxAge()/1000,di.getMaxAge()/1000/3600);
		
		System.out.printf("Last update: %d\n",di.getLastUpdate());
		di.update();
		System.out.printf("Last update: %d\n",di.getLastUpdate());
		
		MetricRegistry registry= new MetricRegistry();
		Map<String, Gauge<?>> map = di.registerGauges(registry);

		System.out.printf("%d files with Gauge\n",map.get("fileCount").getValue());

	}

}

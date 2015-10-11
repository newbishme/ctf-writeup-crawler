import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ParallelCrawlerHandler {

	private static final String FILENAME = "writeup_urls.txt";
	private static final int REQUEST_DELAY = 3000;
	private int maxUrls;
	private int maxCrawlers;
	
	private HashSet<String> crawledUrls = new HashSet<String>();
	private ArrayList<String> crawlingUrls = new ArrayList<String>();
	private ThreadPoolExecutor executorPool;
	private int crawledCounts = 0;
	private ArrayList<String> resultUrls = new ArrayList<String>();
	
	
	public ParallelCrawlerHandler(ArrayList<String> seedUrls, int maxUrls, int maxCrawlers) throws URISyntaxException {
		
		this.maxUrls = maxUrls;
		this.maxCrawlers = maxCrawlers;
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(maxCrawlers, true);
		this.executorPool = new ThreadPoolExecutor(maxCrawlers, maxCrawlers, Long.MAX_VALUE, TimeUnit.SECONDS, workQueue);
		addToCrawlingUrls(seedUrls);
	}
	
	public void beginCrawl() throws UnknownHostException, IOException, URISyntaxException {
		while ((crawledCounts < maxUrls) && !hasReachCrawlerLimit()) {
			if (crawlingUrls.isEmpty() || executorPool.getActiveCount() >= maxCrawlers) {
				continue;
			}

			try {
				Thread.sleep(REQUEST_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			String url = crawlingUrls.get(0);
			crawlingUrls.remove(0);
			
			executorPool.execute(new WebCrawler(this, url));
		}
		
		executorPool.shutdownNow();
		writeToFile();
	
        while (!executorPool.isTerminated()) {
			// do nothing and wait
		}
		if (executorPool.isTerminated()) {
			System.out.println("All Web crawlers has terminated.");
		}
	}	

	public synchronized void addCrawledUrls(String crawledLink, long serverRT, ArrayList<String> links) {
		if (crawledCounts >= maxUrls) {
			return;
		}
		
		crawledUrls.add(crawledLink);
		crawledCounts += 1;
		resultUrls.add(crawledLink + " " + serverRT + "ms");
		System.out.println("Visited: " +crawledLink + " " + serverRT + "ms");
		addToCrawlingUrls(links);
	}
	
	private void addToCrawlingUrls(ArrayList<String> urlList) {
		for (String url : urlList) {
			if (!crawledUrls.contains(url)) {
				crawlingUrls.add(url);
			}
		}
	}
	
	private boolean hasReachCrawlerLimit() {
		return crawlingUrls.isEmpty() && (executorPool.getActiveCount() == 0);
	}
	
	private void writeToFile() {
		try {
			File file = new File(FILENAME);
			if(!file.exists()) {
				file.createNewFile();
				System.out.println("Created file: " + FILENAME);
			} 
			
			System.out.println("Writing to file: " + FILENAME);
			PrintWriter writer = new PrintWriter(FILENAME);
			for (String line : resultUrls) {
				writer.println(line);
			}
			writer.close();
			System.out.println("Closed file: " + FILENAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
}

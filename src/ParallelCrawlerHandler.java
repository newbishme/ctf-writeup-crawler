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


/**
 * This is the multi-threaded handler class for the parallel web crawler.
 */
public class ParallelCrawlerHandler {

	private static final String FILENAME = "writeup_urls.txt";
	private static final int REQUEST_DELAY = 100;
	private int maxUrls;
	private int maxThreads;
	
	private HashSet<String> crawledUrls = new HashSet<String>();
	private ArrayList<String> crawlingUrls = new ArrayList<String>();
	private ThreadPoolExecutor executorPool;
	private int crawledCounts = 0;
	private ArrayList<String> resultUrls = new ArrayList<String>();
	
	
	/**
	 * Constructor for ParallelCrawlerHandler
	 * @param seedUrls The initial urls for crawling.
	 * @param maxUrls The maximum number of urls to crawl.
	 * @param maxThreads The maximum number of threads (WebCrawlers) at any point of time.
	 * @throws URISyntaxException
	 */
	public ParallelCrawlerHandler(ArrayList<String> seedUrls, int maxUrls, int maxThreads) throws URISyntaxException {
		
		this.maxUrls = maxUrls;
		this.maxThreads = maxThreads;
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(maxThreads, true);
		this.executorPool = new ThreadPoolExecutor(maxThreads, maxThreads, Long.MAX_VALUE, TimeUnit.SECONDS, workQueue);
		addToCrawlingUrls(seedUrls);
	}
	
	
	/**
	 * Begin the parallel crawler. All crawled links will be written to a file.
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void beginCrawl() throws UnknownHostException, IOException, URISyntaxException {
		String url;
		while ((crawledCounts < maxUrls) && !isCrawlerExhausted()) {
			if (crawlingUrls.isEmpty() || executorPool.getActiveCount() >= maxThreads) {
				continue;
			}

			try {
				Thread.sleep(REQUEST_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if ( (crawledCounts%50) == 0) {
				crawlingUrls.removeAll(crawledUrls);
			}
			
			url = crawlingUrls.get(0);
			crawlingUrls.remove(0);
			if (!crawledUrls.contains(url)) {
				crawledUrls.add(url);
				executorPool.execute(new WebCrawler(this, url));
			}
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

	/**
	 * Callback function used by the WebCrawler to update the crawlingUrls and resultUrls.
	 * @param crawledLink the link that was visited.
	 * @param serverRT the server response time.
	 * @param links the list of URL strings to visit next.
	 */
	public synchronized void addCrawledUrls(String crawledLink, long serverRT, ArrayList<String> links, String categoryTag) {
		if (crawledCounts >= maxUrls) {
			return;
		}
		
		crawledUrls.add(crawledLink);
		crawledCounts += 1;
		if (categoryTag.isEmpty()) {
			//resultUrls.add(crawledLink + " " + serverRT + "ms");
			System.out.println(crawledCounts + ": " + crawledLink + " " + serverRT + "ms");
		} else {
			resultUrls.add(crawledLink + " " + serverRT + "ms " + categoryTag);
			System.out.println(crawledCounts + ": " + crawledLink + " " + serverRT + "ms " + categoryTag);
		}	
		addToCrawlingUrls(links);
	}
	
	/**
	 * Add a list of URLs to the crawlingUrls list. 
	 * @param urlList The list of URLs of Strings type.
	 */
	private void addToCrawlingUrls(ArrayList<String> urlList) {
		for (String url : urlList) {
			if (!crawledUrls.contains(url)) {
				crawlingUrls.add(url);
			}
		}
	}
	
	/**
	 * Checks if there are any more links left to visit and any more threads running. 
	 * If crawlingUrls is empty and no thread is running, crawler has exhausted.
	 * @return true if reached crawler exhausted, else return false.
	 */
	private boolean isCrawlerExhausted() {
		return crawlingUrls.isEmpty() && (executorPool.getActiveCount() == 0);
	}
	
	
	/**
	 * Writes the resultsUrls into the file of FILENAME.
	 */
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

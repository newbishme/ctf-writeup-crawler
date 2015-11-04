import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * This is the multi-threaded handler class for the parallel web crawler.
 */
public class ParallelCrawlerHandler {

	private static final String FILENAME = "writeup_urls.txt";
	private static final String POTENTIAL_CATEGORIES_FILENAME = "Potential_Categories.txt";
	private static final String POTENTIAL_WHITELIST_DOMAINS_FILENAME = "Potential_WhiteList_Domains.txt";
	private static final int REQUEST_DELAY = 200;
	private DatabaseHandler dbHandler;
	private int maxUrls;
	private int maxThreads;
	
	private HashSet<String> crawledUrls = new HashSet<String>();
	private ArrayList<String> crawlingUrls = new ArrayList<String>();
	private ThreadPoolExecutor executorPool;
	private int crawledCounts = 0;
	private int updateCounts = 0;
	private ArrayList<String> resultUrls = new ArrayList<String>();
	private HashSet<String> domainsList = new HashSet<String>();
	private HashSet<String> categoriesList = new HashSet<String>();
	
	private HashSet<String> rssLinks = new HashSet<String>();
	
	
	/**
	 * Constructor for ParallelCrawlerHandler
	 * @param seedUrls The initial urls for crawling.
	 * @param maxUrls The maximum number of urls to crawl.
	 * @param maxThreads The maximum number of threads (WebCrawlers) at any point of time.
	 * @throws URISyntaxException
	 */
	public ParallelCrawlerHandler(ArrayList<String> seedUrls, int maxUrls, int maxThreads) throws URISyntaxException {
		
		dbHandler = new DatabaseHandler();
		this.maxUrls = maxUrls;
		this.maxThreads = maxThreads;
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(maxThreads, true);
		this.executorPool = new ThreadPoolExecutor(maxThreads, maxThreads, Long.MAX_VALUE, TimeUnit.SECONDS, workQueue);
		addToCrawlingUrls(seedUrls);
	}
	
	/**
	 * Constructor for ParallelCrawlerHandler
	 * @param maxThreads The maximum number of threads (WebCrawlers) at any point of time.
	 * @throws URISyntaxException
	 */
	public ParallelCrawlerHandler(int maxThreads) throws URISyntaxException {
		
		this.maxThreads = maxThreads;
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(maxThreads, true);
		this.executorPool = new ThreadPoolExecutor(maxThreads, maxThreads, Long.MAX_VALUE, TimeUnit.SECONDS, workQueue);
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
			
			if ( crawlingUrls.size() > 500) {
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
		
        while (!executorPool.isTerminated()) {
			// do nothing and wait
		}
        
		System.out.println("All Web crawlers has terminated.");
		//System.out.println("crawledUrls: " + crawledUrls.size() + " crawlingUrls: " + crawlingUrls.size());
		//writeToFile(FILENAME, resultUrls);
	}	

	/**
	 * Begin the parallel updater. Domains added to WhiteList_domains.txt. Categories added to Categories.txt
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void beginUpdateWhiteListAndCategories() throws UnknownHostException, IOException, URISyntaxException {	
		for (int i = 0; i < 2500; i++) {
			try {
				Thread.sleep(REQUEST_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executorPool.execute(new Updater(this, "https://ctftime.org/writeup/"+i));
		}
		
		executorPool.shutdownNow();
		
        while (!executorPool.isTerminated()) {
			// do nothing and wait
		}

		domainsList.removeAll(UrlFilter.getInstance().getWhiteListDomains());
		ArrayList<String> domains = new ArrayList<String>();
		domains.addAll(domainsList);
		writeToFile(POTENTIAL_WHITELIST_DOMAINS_FILENAME, domains);
		System.out.println("New potential whitelist domains can be found in : Potential_WhiteList_domains.txt");
		
		categoriesList.removeAll(Category.getInstance().getCategories());
		ArrayList<String> categories = new ArrayList<String>();
		categories.addAll(categoriesList);
		writeToFile(POTENTIAL_CATEGORIES_FILENAME, categories);
		System.out.println("New potential categories can be found in : Potential_Catergories.txt");
	}
	
	/**
	 * Callback function used by the WebCrawler to update the crawlingUrls and resultUrls.
	 * @param crawledLink the link that was visited.
	 * @param serverRT the server response time.
	 * @param links the list of URL strings to visit next.
	 */
	public synchronized void addCrawledUrls(String crawledLink, long serverRT, ArrayList<String> links, String[] categoryTag) {
		if (crawledCounts >= maxUrls) {
			return;
		}
		
		crawledUrls.add(crawledLink);
		crawledCounts += 1;
		if (categoryTag == null) {
			System.out.println(crawledCounts + ": " + crawledLink + " " + serverRT + "ms");
		} else {
			//resultUrls.add(crawledLink + " " + serverRT + "ms " + Arrays.toString(categoryTag));
			dbHandler.insertToCTFCrawler(new CtfCrawlEntry(crawledLink, Long.toString(serverRT), categoryTag));
			System.out.println(crawledCounts + ": " + crawledLink + " " + serverRT + "ms " + Arrays.toString(categoryTag));
		}
		
		addToCrawlingUrls(links);
	}
	
	/**
	 * Callback function used by the WebCrawler to indicate an RSS URL
	 * @param rssurl the RSS link.
	 */
	public synchronized void addRssUrl(String rssUrl) {
		rssLinks.add(rssUrl);
	}
	
	/**
	 * Callback function used by the Updater to update the whitelist domains and categories
	 * @param domains the domains for updating
	 * @param categories the categories for updating.
	 */
	public synchronized void addDomainsAndCategories(HashSet<String> domains, HashSet<String> categories) {
		updateCounts += 1 ;
		if (domains != null) {
			System.out.println(updateCounts + ": " + "Domains : " + domains.toString());
			domainsList.addAll(domains);
		}
		if (categories != null) {
			System.out.println(updateCounts + ": " + "Categories : " + categories.toString());
			categoriesList.addAll(categories);
		}
	}
	
	/**
	 * Add a list of URLs to the crawlingUrls list. 
	 * @param urlList The list of URLs of Strings type.
	 */
	private void addToCrawlingUrls(ArrayList<String> urlList) {
		if (urlList == null) {
			return;
		}
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
	private void writeToFile(String filename, ArrayList<String> lines) {
		try {
			File file = new File(filename);
			if(!file.exists()) {
				file.createNewFile();
				System.out.println("Created file: " + filename);
			} 
			
			System.out.println("Writing to file: " + filename);
			PrintWriter writer = new PrintWriter(filename);
			for (String line : lines) {
				writer.println(line);
			}
			writer.close();
			System.out.println("Closed file: " + filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Getter for RSS Links
	 * @return a HashSet<String> containing the RSS links
	 */
	public HashSet<String> getRSSLinks() {
		return rssLinks;
	}
	
}

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A driver class to test web crawler. 
 * The user has to give the web crawler a list of seed URLs to begin with.
 * The user also has to set the maximum number of URLs to crawl 
 * and the maximum number of threads that can run at any point of time.
 */
public class WebCrawlerDriver {

	public static void main(String[] args) {
		ParallelCrawlerHandler parallelCrawlerHandler;
		ArrayList<String> seedUrls = new ArrayList<String>();
		int maxUrls = 100000;
		int maxThreads = 50;
		
		seedUrls.add("https://ctftime.org/writeups/");
		seedUrls.add("http://nandynarwhals.org");	
		seedUrls.add("https://0x90r00t.com");
		seedUrls.add("http://blog.squareroots.de/en/");
		seedUrls.add("http://0xecute.com/");
		seedUrls.add("http://whitehatters.academy/");
		seedUrls.add("http://capturetheswag.blogspot.com.au/");
		seedUrls.add("http://ctf-for-beginners.blogspot.sg/");
		
		while (true) {
			try {
				parallelCrawlerHandler = new ParallelCrawlerHandler(seedUrls, maxUrls, maxThreads);
				System.out.println("Web crawler is now starting to crawl....");
				parallelCrawlerHandler.beginCrawl();
				System.out.println("Web crawler sleep for 1 minute....");
				TimeUnit.MINUTES.sleep(1);
				System.out.println("Web crawler is now awake....");
				parallelCrawlerHandler = new ParallelCrawlerHandler(maxThreads);
				System.out.println("Web crawler is now starting to crawl for potential new whitelist domains....");
				System.out.println("Web crawler is now starting to crawl for potential new categories....");
				parallelCrawlerHandler.beginUpdateWhiteListAndCategories();
				System.out.println("Crawl complete, fetching residual links from RSS. System will sleep for an hour.");
				Thread RSSThread = new Thread(new RSSReader(parallelCrawlerHandler.getRSSLinks()));
				RSSThread.start();
				TimeUnit.HOURS.sleep(1);
				if(RSSThread.isAlive()) RSSThread.interrupt();
				System.out.println("Web crawler is now awake....");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


	}

}
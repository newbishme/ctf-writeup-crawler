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
		ArrayList<String> seedUrls = new ArrayList<String>();
		int maxUrls = 100000;
		int maxThreads = 50;
		
		seedUrls.add("https://ctftime.org/writeups/");
		seedUrls.add("http://nandynarwhals.org");	
		seedUrls.add("https://0x90r00t.com");
		seedUrls.add("http://blog.squareroots.de/en/");
		seedUrls.add("http://0xecute.com/");
		
		while (true) {
			try {
				ParallelCrawlerHandler parallelCrawlerHandler = new ParallelCrawlerHandler(seedUrls, maxUrls, maxThreads);
				parallelCrawlerHandler.beginCrawl();
				System.out.println("Web crawler sleep for 1 hour....");
				TimeUnit.HOURS.sleep(1);
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
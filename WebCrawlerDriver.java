import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WebCrawlerDriver {

	public static void main(String[] args) {
		ArrayList<String> seedUrls = new ArrayList<String>();
		int maxUrls = 500;
		int maxCrawlers = 100;
		
		seedUrls.add("https://ctftime.org/writeups");
		seedUrls.add("http://mslc.ctf.su/wp/awesomecorp-secured-ranges-300/");
		
		try {
			ParallelCrawlerHandler parallelCrawlerHandler = new ParallelCrawlerHandler(seedUrls, maxUrls, maxCrawlers);
			parallelCrawlerHandler.beginCrawl();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
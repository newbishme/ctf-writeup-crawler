import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class WebCrawlerDriver {

	public static void main(String[] args) {
		String[] seedUrls = {"https://ctftime.org/writeups", 
								"http://mslc.ctf.su/wp/awesomecorp-secured-ranges-300/"};
		
		
		try {
			for (int i = 0; i < seedUrls.length; i++) {
				WebCrawler webCrawler = new WebCrawler(seedUrls[i]);
				webCrawler.crawl();
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
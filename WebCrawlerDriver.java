import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class WebCrawlerDriver {

	public static void main(String[] args) {
		String[] seedUrls = {"https://github.com/ctfs/write-ups-2015", 
								"http://mslc.ctf.su/wp/awesomecorp-secured-ranges-300/"};
		WebCrawler webCrawler = new WebCrawler();
		
		try {
			for (int i = 0; i < seedUrls.length; i++) {
				webCrawler.crawl(seedUrls[i]);
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
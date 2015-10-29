import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.*;

import java.net.UnknownHostException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;


/**
 * The WebCrawler class represents one worker(thread) in the ParallelCrawlerHandler class
 * The WebCrawler will send a GET request to a given URL and measure the server response time. 
 * This class will also extract all the links from the given URL.
 * This class use Jsoup library for html parsing.
 */
public class WebCrawler implements Runnable {
	public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
	public static String FEED_TYPE_RSS = "application/rss+xml";
	public static String FEED_TYPE_ATOM = "application/rss+xml";
	public static String FEED_TYPE_RSS1 = "application/rdf+xml";
	
	private ParallelCrawlerHandler parallelCrawlerHandler;
    private Socket sock;
    private String url;
    private URI uri;
	private long serverRT; // Server Response Time
	private Category category;
	private String[] categoryTag;
	private UrlFilter urlFilter;

	/**
	 * Constructor for WebCrawler
	 * @param parallelCrawlerHandler The handler to callback to.
	 * @param url The url to crawl.
	 * @throws URISyntaxException
	 */
	public WebCrawler(ParallelCrawlerHandler parallelCrawlerHandler, String url) throws URISyntaxException {
		this.parallelCrawlerHandler = parallelCrawlerHandler;
		this.url = url;
		sock = new Socket();
		uri = new URI(url);
		serverRT = 0;
		categoryTag = null;
		category = Category.getInstance();
		urlFilter = UrlFilter.getInstance();
	}

	/**
	 * Open socket connection to the given url and obtain the absolute links inside the url.
	 * @return the list of absolute URLs obtained.
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public ArrayList<String> crawl() throws ConnectException, UnknownHostException, IOException, URISyntaxException {
		String host = uri.getHost();
		int port = getPort(uri);
		String path = uri.getPath();
		String query = uri.getQuery();

		if (port == 80) {
			sock = new Socket(host, port);
		} else if (port == 443) {
			SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
			sock = ssf.createSocket(host, port);
		} else {
			return null;
		}
	
		if(query != null) {
			path += "?" + query;
		}
		
		sendGetRequest(host, path);
		String html = recvGetResponse();
		ArrayList<String> absLinks = getLinksFromHTML(html);
		
		sock.close();
		return absLinks;
	}

	/**
	 * Send the GET Request to the established socket.
	 * @param host the URI host. 
	 * @param path the URI request path.
	 * @throws IOException
	 */
	private void sendGetRequest(String host, String path) throws IOException {

		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

		//Request start time
		long startTime = System.currentTimeMillis();

		//Get Request
		dos.writeBytes("GET " + path + " HTTP/1.1\r\n");
		dos.writeBytes("Host: " + host + "\r\n");
		dos.writeBytes("User-Agent: " + USER_AGENT + "\r\n");
		dos.writeBytes("Connection: close\r\n\r\n");

		//Request end time
		long endTime = System.currentTimeMillis();
		serverRT += (endTime - startTime);
	}

	/**
	 * Get the response of the GET Request from the socket.
	 */
	private String recvGetResponse() throws IOException {

		InputStream inputStream = sock.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		String line = "";
		
		// Response start time
		long startTime = System.currentTimeMillis();

		sb.append(bufferedReader.readLine());
		
		// Response end time
		long endTime = System.currentTimeMillis();
		serverRT += (endTime - startTime);

		//Get Response
		while ((line = bufferedReader.readLine()) != null) {
		    sb.append(line);
		}

		return sb.toString();
	}

	/**
	 * Gets the port based on the URI's scheme.
	 * @param uri the URI of given url.
	 * @return 80 if HTTP, or 443 if HTTPS, else -1
	 */
	private int getPort(URI uri) {
		String protocol = uri.getScheme(); 
		if (uri.getPort() == -1) {
		    if (protocol.equals("http")) { 
		        return 80;
		    } else if (protocol.equals("https")) {
		        return 443;
		    }
		}
		return -1;
	}
	
	private String verifyAbsLink(Element link) {
		String absLink = link.attr("abs:href");
		
		//href is root-relative link
		if (absLink == ""){ 
			absLink = uri.getScheme()+ "://" + uri.getAuthority() + link.attr("href");
		}
		
		return absLink;
	}
	
	/**
	 * Get the absolute links from the give html String.
	 * @param html the html received from the GET Request.
	 * @return the list of absolute links.
	 */
	private ArrayList<String> getLinksFromHTML(String html) {
		//NOTE: may need to detect if href is relative link
		ArrayList<String> absLinks = new ArrayList<String>();
		String absLink;
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			absLink = verifyAbsLink(link);
			
			if (!urlFilter.shouldFilterOff(absLink)) {
				absLinks.add(absLink);
			}
        }
		categoryTag = category.getTags(doc);
		return absLinks;
	}
	
	public static String getRssLink(Document doc) {
		/**
		 * Gets a HTML document from the specified web address
		 * and searches for the MIME types that specify RSS/Atom feeds.
		 * Returns a String representing the URL of the first valid feed found.
		 * Returns null if there is no feed found
		 * @param the string representing the target website
		 */
		String result = null;
		// gets the first valid link of a matching MIME type
		ArrayList<String> mimeTypes = new ArrayList<String>();
		mimeTypes.add(FEED_TYPE_RSS);
		mimeTypes.add(FEED_TYPE_ATOM);
		mimeTypes.add(FEED_TYPE_RSS1);
		Iterator<String> mimeIter = mimeTypes.iterator();
		while(mimeIter.hasNext() && result == null) {
			Elements rssElements = doc.getElementsByAttributeValue("type", mimeIter.next());
			if(rssElements.size() > 0) {
				result = rssElements.get(0).attr("href"); // loop will break
			}
		}
		return result;
	}
	
	public ArrayList<String> getLinksFromRSS(String rssFeed) {
		ArrayList<String> result = new ArrayList<String>();

		try {
			URI rssuri = new URI(rssFeed);
			sendGetRequest(rssuri.getHost(), rssuri.getPath());
			String html = recvGetResponse();
			InputStream is = new ByteArrayInputStream(html.getBytes());
			
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed;
			feed = input.build(new XmlReader(is));
			List<SyndEntry> entries = feed.getEntries();
			for (SyndEntry ent : entries) {
				result.add(ent.getLink());
			}
		} catch (IllegalArgumentException | FeedException | IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Thread entry point. The thread will crawl and obtain links in the given url to update handler
	 */
	@Override
	public void run() {
		if (uri == null) {
			return;
		}
		ArrayList<String> links = null;

		try {
			links = crawl();
		} catch (ConnectException e) {
			parallelCrawlerHandler.addCrawledUrls(url, -1, null, null);
		} catch (URISyntaxException e) {
			parallelCrawlerHandler.addCrawledUrls(url, -1, null, null);
		} catch (UnknownHostException e) {
			parallelCrawlerHandler.addCrawledUrls(url, -1, null, null);
		} catch (PortUnreachableException e) {
			parallelCrawlerHandler.addCrawledUrls(url, -1, null, null);
		} catch (IOException e) {
			parallelCrawlerHandler.addCrawledUrls(url, -1, null, null);
		}
		
		if (links != null) {
			parallelCrawlerHandler.addCrawledUrls(url, serverRT, links, categoryTag);
		}
		
		return;
	}	
}
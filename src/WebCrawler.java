import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.*;

import java.net.UnknownHostException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * The WebCrawler class represents one worker(thread) in the ParallelCrawlerHandler class
 * The WebCrawler will send a GET request to a given URL and measure the server response time. 
 * This class will also extract all the links from the given URL.
 * This class use Jsoup library for html parsing.
 */
public class WebCrawler implements Runnable {
	
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
			absLink = link.attr("abs:href");
			
			//href is root-relative link
			if (absLink == ""){ 
				absLink = uri.getScheme()+ "://" + uri.getAuthority() + link.attr("href");
			}
			
			if (!urlFilter.shouldFilterOff(absLink)) {
				absLinks.add(absLink);
			}
        }
		categoryTag = category.getTags(doc);
		return absLinks;
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
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		} catch (PortUnreachableException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if (links != null) {
			parallelCrawlerHandler.addCrawledUrls(url, serverRT, links, categoryTag);
		}
		
		return;
	}	
}
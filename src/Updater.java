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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.net.ssl.SSLSocketFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * This Updater class will search for potential whitelist domains and categories from ctftime.org
 * Current implementation only find whitelist domains from html elements: <div><a href=...>
 * Current implementation only find categories from html elements: <span class="label label-info">
 */
public class Updater implements Runnable {
	
	private ParallelCrawlerHandler parallelCrawlerHandler;
    private Socket sock;
    private URI uri;
	private HashSet<String> categories;
	private HashSet<String> domains;
	
	/**
	 * Constructor for Updater
	 * @param parallelCrawlerHandler The handler to callback to.
	 * @param url The url to crawl.
	 * @throws URISyntaxException
	 */
	public Updater(ParallelCrawlerHandler parallelCrawlerHandler, String url) throws URISyntaxException {
		this.parallelCrawlerHandler = parallelCrawlerHandler;
		sock = new Socket();
		uri = new URI(url);
		categories = new HashSet<String>();
		domains = new HashSet<String>();
	}
	
	/**
	 * Open socket connection to the given url and obtain whitelist domains and categories inside the url.
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void crawl() throws ConnectException, UnknownHostException, IOException, URISyntaxException {
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
			return;
		}
	
		if(query != null) {
			path += "?" + query;
		}
		
		sendGetRequest(host, path);
		String html = recvGetResponse();
		obtainDomainsAndCategoriesFromHTML(html);
		
		sock.close();
	}

	/**
	 * Send the GET Request to the established socket.
	 * @param host the URI host. 
	 * @param path the URI request path.
	 * @throws IOException
	 */
	private void sendGetRequest(String host, String path) throws IOException {

		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

		//Get Request
		dos.writeBytes("GET " + path + " HTTP/1.1\r\n");
		dos.writeBytes("Host: " + host + "\r\n");
		dos.writeBytes("Connection: close\r\n\r\n");
	}

	/**
	 * Get the response of the GET Request from the socket.
	 */
	private String recvGetResponse() throws IOException {

		InputStream inputStream = sock.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		String line = "";

		sb.append(bufferedReader.readLine());

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
	 * @throws URISyntaxException 
	 */
	private void obtainDomainsAndCategoriesFromHTML(String html) throws URISyntaxException {
		URI uri; 
		Document doc = Jsoup.parse(html);
		Elements links = doc.select(" div > a[href]");
		for (Element link : links) {
			uri = new URI(link.attr("abs:href"));
			if (uri != null && uri.getHost() != null) {
				domains.add(uri.getHost());
			}
        }
		Elements categories = doc.select("span[class=label label-info]");
		for (Element category : categories) {
			if (category.text() != null && category.text() != "/") {
				try {
					Integer.parseInt(category.text());
				} catch (NumberFormatException e) {
					this.categories.add(category.text().toLowerCase());
				}	
			}
		}
	}
	
	/**
	 * Thread entry point. The thread will crawl given url and 
	 * obtain potential whitelist domain and categories.
	 */
	@Override
	public void run() {
		if (uri == null) {
			return;
		}

		try {
			crawl();
		} catch (ConnectException e) {
			parallelCrawlerHandler.addDomainsAndCategories(null, null);
		} catch (URISyntaxException e) {
			parallelCrawlerHandler.addDomainsAndCategories(null, null);
		} catch (UnknownHostException e) {
			parallelCrawlerHandler.addDomainsAndCategories(null, null);
		} catch (PortUnreachableException e) {
			parallelCrawlerHandler.addDomainsAndCategories(null, null);
		} catch (IOException e) {
			parallelCrawlerHandler.addDomainsAndCategories(null, null);
		}
		
		parallelCrawlerHandler.addDomainsAndCategories(domains, categories);	
		return;
	}	
}

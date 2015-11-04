import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class RSSReader {
 /**
  * Gets a common set of RSS URLs
  * Checks set of writeup URLs to ensure that link has not been accessed before
  * Attempts to classify links by description, and inserts them into writeup list
  * only crawls for an hour
  */
	public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
	public static String FEED_TYPE_RSS = "application/rss+xml";
	public static String FEED_TYPE_ATOM = "application/rss+xml";
	public static String FEED_TYPE_RSS1 = "application/rdf+xml";
	
	private String rssLinksFileName;
	private ArrayList<String> rssLinks;
	private HashSet<String> rssLinksSet;
	private HashSet<String> dictionary;
	
	public RSSReader(String dictionaryFileName, String rssLinksFileName) {
		this.rssLinksFileName = rssLinksFileName;
	}
	
	public ArrayList<String> getRSSLinks(String url) throws ConnectException, UnknownHostException, IOException, URISyntaxException {

		ArrayList<String> result = new ArrayList<String>();
		URI uri = new URI(url);
		
		Socket sock = createSocket(uri);
		if (sock == null) return result;
		
		// write get request
		String getRequest= generateGetRequest(uri);
		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		dos.writeBytes(getRequest);

		String html = getResponse(sock);
		
		sock.close();
		return result;
	}
	
	private Socket createSocket(URI uri) {
		try {
			String protocol = uri.getScheme();
			String host = uri.getHost();
			if (uri.getPort() == -1) {
				if (protocol.equals("http")) {
					return new Socket(host, 80);
				} else if (protocol.equals("https")) {
					SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
					return ssf.createSocket(host, 443);
				}
			}				
			// fail silently
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String getResponse(Socket sock) throws IOException {
		InputStream inputStream = sock.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();

		sb.append(bufferedReader.readLine());

		//Get Response
		String line = bufferedReader.readLine();
		while (line != null) {
		    sb.append(line);
		    line = bufferedReader.readLine();
		}

		return sb.toString();
	}
	
	private String generateGetRequest(URI uri) {

		String host = uri.getHost();
		String path = uri.getPath();
		
		{
			String query = uri.getQuery();
			if(query != null) {
				path += "?" + query;
			}
		}

		StringBuilder get = new StringBuilder();

		//Get Request
		get.append("GET " + path + " HTTP/1.1\r\n");
		get.append("Host: " + host + "\r\n");
		get.append("User-Agent: " + USER_AGENT + "\r\n");
		get.append("Connection: close\r\n\r\n");
		
		return get.toString();
	}
	
	public ArrayList<String> getLinksFromRSS(String rssFeed) throws IllegalArgumentException, FeedException, IOException {
		/**
		 * Gets entries from RSS, and performs the following steps
		 * 	Checks if the link is already in the DB
		 * 	if it is, ignore
		 * 	else, check description for tags, and attach
		 * 	write straight to db?
		 */
		
		ArrayList<String> result = new ArrayList<String>();

		InputStream is = new ByteArrayInputStream(rssFeed.getBytes());
		
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed;
		feed = input.build(new XmlReader(is));
		List<SyndEntry> entries = feed.getEntries();
		for (SyndEntry ent : entries) {
			result.add(ent.getLink());
		}

		return result;
	}
}

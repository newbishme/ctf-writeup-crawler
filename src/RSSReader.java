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

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;


public class RSSReader implements Runnable {
 /**
  * Gets a common set of RSS URLs
  * Checks set of writeup URLs to ensure that link has not been accessed before
  * Attempts to classify links by description, and inserts them into writeup list
  * only crawls for an hour
  */
	public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
	
	private class RSSLink { // just a containter
		public String url;
		public Long serverRT;
		public HashSet<String> categories;
		public RSSLink(String url, Long serverRT, HashSet<String> cats) {
			this.url = url;
			this.serverRT = serverRT;
			categories = cats;
		}
	}
	
	private class DB {
		DatabaseHandler dbHandler;
		public DB() {
			dbHandler = new DatabaseHandler();
		}
		
		public void write(RSSLink link) {
			String[] cats = new String[link.categories.size()];
			cats = link.categories.toArray(cats);
			CtfCrawlEntry entry = new CtfCrawlEntry(link.url, Long.toString(link.serverRT), cats); 
			dbHandler.insertToCTFCrawler(entry);
		}
		
		public boolean isCrawled (String url) {
			return dbHandler.isInserted(url);
		}
	}
	
	private HashSet<String> rssLinksSet;
	private DB database;
	
	public RSSReader(HashSet<String> rssLinks) {
		rssLinksSet = rssLinks;
		database = new DB();
	}
	
	private ArrayList<RSSLink> getRSSLinks(String url) throws ConnectException, UnknownHostException, IOException, URISyntaxException, IllegalArgumentException, FeedException {

		ArrayList<RSSLink> result = new ArrayList<RSSLink>();
		URI uri = new URI(url);
		
		Socket sock = createSocket(uri);
		if (sock == null) return result;
		
		// write get request
		String getRequest= generateGetRequest(uri);
		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		dos.writeBytes(getRequest);

		String rssxml = getResponse(sock);
		
		result = getLinksFromRSS(rssxml);
		
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
	
	private ArrayList<RSSLink> getLinksFromRSS(String rssFeed) throws IllegalArgumentException, FeedException, IOException {
		/**
		 * Gets entries from RSS, and performs the following steps
		 * 	Checks if the link is already in the DB
		 * 	if it is, ignore
		 * 	else, check description for tags, and attach
		 * 	write straight to db?
		 */
		
		ArrayList<RSSLink> result = new ArrayList<RSSLink>();

		InputStream is = new ByteArrayInputStream(rssFeed.getBytes());
		
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed;
		feed = input.build(new XmlReader(is));
		List<SyndEntry> entries = feed.getEntries();
		
		for (SyndEntry ent : entries) {
			String url = ent.getLink();
			if(!database.isCrawled(url)) {
				try {
					RSSLink current = attachCategories(ent);
					result.add(current);
				} catch (Exception e) {
					// fail silently
				}
			}
		}

		return result;
	}
	
	private RSSLink attachCategories(SyndEntry entry) throws Exception {
		List<SyndCategory> categories = entry.getCategories();
		HashSet<String> cats = new HashSet<String>();
		if (!categories.isEmpty()) {
			for (SyndCategory cat : categories) {
				cats.add(cat.getName());
			}
		} else {
			String description = entry.getDescription().getValue();
			Category dictionary = Category.getInstance();
			cats = dictionary.getTags(description);
		}
		
		Long rtt = getRTT(entry.getLink());
		return new RSSLink(entry.getLink(), rtt, cats);
	}
	
	private Long getRTT(String url) throws Exception {
		URI uri = new URI(url);
		
		Socket sock = createSocket(uri);
		if (sock == null) throw new Exception();
		String getRequest = generateGetRequest(uri);
		
		// measure time
		Long start = System.currentTimeMillis();
		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
		dos.writeBytes(getRequest);
		getResponse(sock);
		Long end = System.currentTimeMillis();
		
		sock.close();
		return end - start;
	}

	@Override
	public void run() {
		for (String rssLink : rssLinksSet) {
			if(Thread.currentThread().isInterrupted()) {
				// break
				return;
			} else {
				ArrayList<RSSLink> links;
				try {
					links = getRSSLinks(rssLink);
					for (RSSLink link : links) {
						database.write(link);
					}
				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FeedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}

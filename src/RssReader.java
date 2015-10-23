import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class RssReader {
	public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
	public static String FEED_TYPE_RSS = "application/rss+xml";
	public static String FEED_TYPE_ATOM = "application/rss+xml";
	public static String FEED_TYPE_RSS1 = "application/rdf+xml";
	/**
	 * Gets a set of links from the link fields of a given RSS feed
	 * If the feed link is malformed XML or not an RSS feed, throws exception.
	 * @param the string representing the URL of the feed
	 */
	public static ArrayList<String> getLinks(String rssFeed) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			URL url = new URL(rssFeed);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", USER_AGENT);

			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new InputSource(connection.getInputStream()));
			List<SyndEntry> entries = feed.getEntries();
			for (SyndEntry ent : entries) {
				result.add(ent.getLink());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	public static String getRssLink(String website) {
		/**
		 * Gets a HTML document from the specified web address
		 * and searches for the MIME types that specify RSS/Atom feeds.
		 * Returns a String representing the URL of the first valid feed found.
		 * Returns null if there is no feed found
		 * @param the string representing the target website
		 */
		String result = null;

		try {
			URL url;
			url = new URL(website);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", USER_AGENT);
			StringBuilder sb = new StringBuilder();
			Scanner sc = new Scanner(connection.getInputStream());
			while(sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			sc.close();
			
			// parse HTML
			Document doc = Jsoup.parse(sb.toString());
			
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
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
		
	}
}

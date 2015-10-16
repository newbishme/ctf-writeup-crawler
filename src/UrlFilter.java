import java.net.URI;
import java.net.URISyntaxException;


/**
 * This UrlFilter class will check if the given url should be filter off from crawling list.
 * This class uses singleton pattern as only one object is needed throughout the crawling process 
 */
public class UrlFilter {

	private static UrlFilter singletonInstance = null;
	
	// Need another way to check url extension? filter off png, jpg, exe, etc
	private final String[] URL_FILTER_KEYWORDS_GENERIC = {"google", "facebook", "twitter", "png", "jpg", "exe", "pdf", "#",
															"wolframalpha", "wikipedia", "wiki", "github"};
	private final String[] URL_FILTER_KEYWORDS_CTFTIME_ORG = {"facebook", "twitter", "contact", "about","faq", "#", "/ctf/", 
																"login", "stats", "dating", "team", "event", "task", "calendar"};
	
	/**
	 * Provides reference of the Category singleton if it exists, or returns it after creating it
	 * @return singletonInstance
	 */
	public static UrlFilter getInstance() {
		if(singletonInstance == null) {
		   singletonInstance = new UrlFilter();
		}
		return singletonInstance;
	}
	
	/**
	 * A private constructor to enable Singleton Pattern
	 */
	private UrlFilter() {
		
	}
	
	/**
	 * Check if the absolute link should be filter off.
	 * @param abslink the absolute link to be checked.
	 * @return true if the link passed the filter check, else false.
	 */
	public boolean shouldFilterOff(String absLink) {
		try {
			URI absLinkUri = new URI(absLink);
			String absLinkHost = absLinkUri.getHost();
			if (!absLinkHost.isEmpty()) {
				if(absLinkHost.equalsIgnoreCase("ctftime.org")) {
					return isInUrlFilterKeywords(absLink,URL_FILTER_KEYWORDS_CTFTIME_ORG);
				} else {
					return isInUrlFilterKeywords(absLink,URL_FILTER_KEYWORDS_GENERIC);
				}
			} else {
				return true;
			}
		} catch (URISyntaxException e) {
			return true;
		}
	}
	
	
	/**
	 * Check if the absolute link contain the filter keywords.
	 * @param abslink the absolute link to be checked.
	 * @param keywords the array of keywords
	 * @return true if the absLink contain the keyword, else false.
	 */
	private boolean isInUrlFilterKeywords(String absLink, String[] keywords) {
		for (String keyword : keywords) {
			if (absLink.contains(keyword)) 
				return true;
		}
		return false;
	}
	
}

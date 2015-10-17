import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;


/**
 * This UrlFilter class will check if the given url should be filter off from crawling list.
 * This class uses singleton pattern as only one object is needed throughout the crawling process 
 */
public class UrlFilter {

	private static UrlFilter singletonInstance = null;
	private static final String WHITELIST_DOMAINS_FILENAME = "WhiteList_Domains.txt";
	
	// Need another way to check url extension? filter off png, jpg, exe, etc
	private final String[] FILTER_KEYWORDS_GENERIC = {"google", "facebook", "twitter", "png", "jpg", "exe", "pdf", "#",
														"wolframalpha", "wikipedia", "wiki", "github", "wordpress",
														"contact", "faq", "about", "login", "sourceforge", "media"};
	private final String[] FILTER_KEYWORDS_CTFTIME_ORG = {"/ctf/", "stats", "dating", "team", "event", "task", "calendar", "user"};
	private final String[] FILTER_KEYWORDS_NANDYNARWHALS_ORG = {"scripting-for-ctfs", "comment"};
	private final String[] FILTER_KEYWORDS_0X90R00T_COM = {"about-us",};
	
//	private final String[]	WHITELIST_DOMAIN = {"maryjane.ru",
//												"ctftime.org",
//												"nandynarwhals.org",
//												"0x90r00t.com",
//												"kt.pe",
//												"0xecute.com",									
//												"blog.squareroots.de",
//												"br0ns.dk",
//												"81glntd.blogspot.sg",
//												"ibrahim-elsayed.com	",								
//												"b01lers.net",
//												"akaminsky.net",
//												"whitehatters.academy",
//												"khack40.info",
//												"security.claudio.pt",
//												"capturetheswag.blogspot.com.au",
//												"thekillingtime.com",
//												"kikisctf.blogspot.hr",
//												"cafelinux.info",
//												"blog.bitsforeveryone.com",
//												"toh.necst.it",
//												"cclub.cs.wmich.edu",
//												"ctf-for-beginners.blogspot.sg",
//												"fadec0d3.blogspot.sg",
//												"www.securifera.com",
//												"ohaithe.re",
//												"buer.haus",
//												"www.truedigitalsecurity.com",
//												"zybuluo.com",
//												"trich.im",
//												"gameofpwnz.com",
//												"pawpetersen.dk",
//												"incertia.net",
//												"reyammer.blogspot.sg",
//												"xd4rker.blogspot.sg",};
	private HashSet<String> whiteListDomains;
	
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
		whiteListDomains = new HashSet<String>();
		initializeWhiteListDomains();
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
			if (absLinkHost != null && !absLinkHost.isEmpty()) {
				if (!whiteListDomains.contains(absLinkHost)) {
					return true;
				}
				if(isInFilterKeywords(absLink,FILTER_KEYWORDS_GENERIC)) {
					return true;
				}
				if(absLinkHost.equalsIgnoreCase("ctftime.org")) {
					return isInFilterKeywords(absLink,FILTER_KEYWORDS_CTFTIME_ORG);
				} else if (absLinkHost.equalsIgnoreCase("nandynarwhals.org")) {
					return isInFilterKeywords(absLink,FILTER_KEYWORDS_NANDYNARWHALS_ORG);
				} else if (absLinkHost.equalsIgnoreCase("0x90r00t.com")) {
					return isInFilterKeywords(absLink,FILTER_KEYWORDS_0X90R00T_COM );
				} else {
					return false;
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
	 * @param absLink the absolute link to be checked.
	 * @param keywords the array of keywords
	 * @return true if the absLink contain the keyword, else false.
	 */
	private boolean isInWhiteListDomains(String absLink) {
		//for (String domain : WHITELIST_DOMAIN) {
		for (String domain : whiteListDomains) {
			if (absLink.contains(domain)) 
				return true;
		}
		return false;
	}
	
	/**
	 * Check if the absolute link contain the filter keywords.
	 * @param absLink the absolute link to be checked.
	 * @param keywords the array of keywords
	 * @return true if the absLink contain the keyword, else false.
	 */
	private boolean isInFilterKeywords(String absLink, String[] keywords) {
		for (String keyword : keywords) {
			if (absLink.contains(keyword)) 
				return true;
		}
		return false;
	}
	
	/**
	 * Read and add lines from file to whiteListDomains hashset
	 */
	private void initializeWhiteListDomains(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(WHITELIST_DOMAINS_FILENAME));
		    String line = br.readLine();

		    while (line != null) {
		    	whiteListDomains.add(line);
		    	whiteListDomains.add("www."+line);
		        line = br.readLine();
		    }
		    br.close();
		} catch (IOException e) {
		   
		}
	}
	
}

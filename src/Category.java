import java.util.HashSet;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * This Category class will parse the given html and tag the url with the category that fit well with it.
 * Current implmentation only analyze the following html elements: title,h1,h2,h3 
 * This class uses singleton pattern as only one object is needed throughout the crawling process 
 */
public class Category {
		
	private static Category singletonInstance = null;
	
	// Maybe further group words into a single tag, e.g crypto: "crypto, cryptography, cipher"
	private final String[] CATEGORY_LIST = {"defensive", "offensive", "analysis", "analyze", "forensic", "injection", "binary", 
												"reversing", "reverse", "reverse engineering", "exploit", "exploitation", "crypto", 
												"cryptography", "cipher", "recon", "reconnaisse", "misc", "programming", "shell", 
												"stego", "stegography", "malware", "xss"};
	
	/**
	 * Provides reference of the Category singleton if it exists, or returns it after creating it
	 * @return singletonInstance
	 */
	public static Category getInstance() {
		if(singletonInstance == null) {
		   singletonInstance = new Category();
		}
		return singletonInstance;
	}
	
	/**
	 * A private constructor to enable Singleton Pattern
	 */
	private Category() {
		
	}
	
	/**
	 * Get the category tags fitted with the given html document.
	 * @param doc the parsed html document using Jsoup.
	 * @return the string containing the category tags.
	 */
	public String[] getTags(Document doc) {
		HashSet<String> tags = new HashSet<String>();
		
		String text;
		Elements elements = doc.select("title,h1,h2,h3");
		for (Element element : elements) {
			text = element.text().toLowerCase();
			for (String category : CATEGORY_LIST) {
				if (text.contains(category)) {
					tags.add(category);
				}
			}
		}
		if (tags.isEmpty()) {
			return null;
		} else {
			return tags.toArray(new String[tags.size()]);
		}
		
	}
	
	/**
	 * Get the category tags fitted with the given html document from ctftimes.org
	 * @param doc the parsed html document using Jsoup.
	 * @return the string containing the category tags.
	 */
	public String[] getTagsFromCtfTimes(Document doc) {
		HashSet<String> tags = new HashSet<String>();
		
		String text;
		Elements elements = doc.select("h2,span");
		for (Element element : elements) {
			text = element.text().toLowerCase();
			for (String category : CATEGORY_LIST) {
				if (text.contains(category)) {
					tags.add(category);
				}
			}
		}
		if (tags.isEmpty()) {
			return null;
		} else {
			return tags.toArray(new String[tags.size()]);
		}
		
	}
}

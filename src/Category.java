import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * This Category class will parse the given html and tag the url with the category that fit well with it.
 * Current implementation only analyze the following html elements: title,h1,h2,h3 
 * This class uses singleton pattern as only one object is needed throughout the crawling process 
 */
public class Category {
		
	private static Category singletonInstance = null;
	private static final String CATEGORIES_FILENAME = "Categories.txt";
	
	// Maybe further group words into a single tag, e.g crypto: "crypto, cryptography, cipher"
	private HashSet<String> categories;
	
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
		categories = new HashSet<String>();
		initializeCategories();
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
			tags.addAll(getTags(text));
		}
		if (tags.isEmpty()) {
			return null;
		} else {
			return tags.toArray(new String[tags.size()]);
		}
		
	}
	
	public HashSet<String> getTags(String text) {
		HashSet<String> tags = new HashSet<String>();
		text = text.toLowerCase();
		for (String category : categories) {
			if (text.contains(category)) {
				tags.add(category);
			}
		}
		
		return tags;
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
			for (String category : categories) {
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
	 * Get the categories from the HashSet
	 * @return the categories hashset.
	 */
	public HashSet<String> getCategories() {
		return categories;
	}
	
	/**
	 * Read and add lines from file to Categories hashset
	 */
	private void initializeCategories(){
		try {
			BufferedReader br = new BufferedReader(new FileReader(CATEGORIES_FILENAME));
		    String line = br.readLine();

		    while (line != null) {
		    	categories.add(line);
		        line = br.readLine();
		    }
		    br.close();
		} catch (IOException e) {
		   
		}
	}
}

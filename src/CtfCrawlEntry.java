
public class CtfCrawlEntry {
    private String url = "";
    private String response = "";
    private String[] tags;
    
    public CtfCrawlEntry(String url, String response, String[] tags) {
        this.url = url;
        this.response = response;
        this.tags = tags;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getResponse() {
        return response;
    }
    
    public String[] getTags() {
        return tags;
    }
}

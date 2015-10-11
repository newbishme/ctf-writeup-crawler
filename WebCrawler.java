import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import javax.net.ssl.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {

    private Socket sock; 
    private URI uri;
	private long serverRT; // Server Response Time

	public WebCrawler(String url) throws URISyntaxException {
		sock = new Socket();
		uri = new URI(url);
		serverRT = 0;
	}

	public void crawl() throws UnknownHostException, IOException, URISyntaxException {
		String[] urls;
		String host = uri.getHost();
		int port = getPort(uri);
		String path = uri.getPath();

		if (port == 80) {
			sock = new Socket(host, port);
		} else if (port == 443) {
			SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
			sock = ssf.createSocket(host, port);
		} else {
			return;
		}

		System.out.println(sock);
		sendGetRequest(path, host);
		String html = recvGetResponse();
		ArrayList<String> absLinks = getLinksFromHTML(html);
		
		for (int i = 0; i < absLinks.size(); i++) {
			System.out.println(absLinks.get(i));
		}
		System.out.println("Server Response Time: " + serverRT  + "ms");

		sock.close();
	}

	private void sendGetRequest(String path, String host) throws IOException {

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
				absLinks.add(absLink);
			} else {
				absLinks.add(absLink);
			}
        }
		return absLinks;
	}

}
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

public class WebCrawler {

    private Socket sock; 
	private long serverRT; // Server Response Time

	public WebCrawler() {
		sock = new Socket();
		serverRT = 0;
	}

	public void crawl(String url)  
						throws UnknownHostException, IOException, URISyntaxException {
		String[] urls;
		URI uri = new URI(url);
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

		System.out.println(html);
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

}
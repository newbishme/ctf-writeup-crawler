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

	public WebCrawler() {
		sock = new Socket();
	}

	//public void crawl(String host, int port, String path)
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

		sock.close();
	}

	private void sendGetRequest(String path, String host) throws IOException {

		DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

		dos.writeBytes("GET " + path + " HTTP/1.1\r\n");
		dos.writeBytes("Host: " + host + "\r\n");
		dos.writeBytes("Connection: close\r\n\r\n");
	}

	private String recvGetResponse() throws IOException {

		InputStream inputStream = sock.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		String line = "";
		
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
package page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * IMGFetcher impl Runnable
 * 
 * public method:
 * IMGFetcher(String url)
 * run()
 * List<String> getIMGList()
 */
public class IMGFetcher implements Runnable {
	private final static Pattern PATTERN_IMG = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
	protected final String url;
	protected String pageContent;
	protected List<String> imgQueue;
	
	public IMGFetcher(String url) {
		if (url.length() >= 4 && !url.substring(url.length() - 4, url.length()).equals("html") && url.charAt(url.length() - 1) != '/') {
			url = url + "/";
		}
		this.url = url;
		this.pageContent = "";
		this.imgQueue = new ArrayList<>();
	}
	
	private String openPage(final String url) {
		StringBuilder pageContentSB = new StringBuilder();
		
		try {
			URLConnection connection = new URL(this.url).openConnection();
			connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				pageContentSB.append(line + "\n");
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("URL invalid" + "\n" + e.getMessage());
		} catch (IOException e) {
			throw new IllegalArgumentException("URL unreadable" + "\n" + e.getMessage());
		}
		
		return pageContentSB.toString();
	}
	
	private void fetch() {		
		Matcher matcher = this.PATTERN_IMG.matcher(this.pageContent);
		Set<String> set = new HashSet<String>();
		String root = getRoot();
		
		
		while (matcher.find()) {
			StringBuilder imgSB = new StringBuilder(matcher.group(1));
			
			if (imgSB.length() >= 1 && imgSB.substring(0, 1).equals("/")) {
				// relative path to root
				imgSB = new StringBuilder(root + imgSB.substring(1, imgSB.length()));
			} else if (imgSB.length() >= 4 && imgSB.substring(0, 4).equals("http")) {
				// abs path
			} else {
				// relative path to cur file
				imgSB = new StringBuilder(this.url + imgSB);
			}
			
			String img = imgSB.toString();
			if (!set.contains(img)) {
				set.add(img);
				this.imgQueue.add(img);
			}
		}
	}
	
	private String getRoot() {
		int index = 0;
		int counterDelimiter = 0;
		while (index < this.url.length()) {
			if (this.url.charAt(index) == '/') {
				if (counterDelimiter < 2) {
					counterDelimiter++;
				} else {
					return this.url.substring(0, index + 1);
				}
			}
			index++;
		}
		
		return this.url;
	}
	
	public List<String> getIMGList() {
		return this.imgQueue;
	}
	
	@Override
	public void run() {	
		this.pageContent = this.openPage(this.url);
		this.fetch();
	}
	
	public static void main(String[] args) {		
		long timeStart = System.nanoTime();

		IMGFetcher f1 = new IMGFetcher("http://nutch.apache.org/");
		IMGFetcher f2 = new IMGFetcher("https://wiki.apache.org/nutch/FrontPage#What_is_Apache_Nutch.3F/");
		IMGFetcher f3 = new IMGFetcher("http://www.apache.org/foundation/thanks.html");
		
		Thread t1 = new Thread(f1);
		Thread t2 = new Thread(f2);
		Thread t3 = new Thread(f3);

		t1.start();
		t2.start();
		t3.start();
		
		try {
			t1.join();
			t2.join();
			t3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<String> result = new ArrayList<>();
		result.addAll(f1.getIMGList());
		result.addAll(f2.getIMGList());
		result.addAll(f3.getIMGList());
		
		System.out.println(result.size());
		
		long timeStop = System.nanoTime();
		
		System.out.println("multithread finished   : " + (timeStop - timeStart));
	}
}
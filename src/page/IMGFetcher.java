package page;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;
/**
 * IMGFethcer fetches all img in a page
 * 
 * @author Xin
 *
 */
public class ImgFetcher implements Runnable {
	private final static Pattern PATTERN_IMG = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
	protected final String url;
	protected String pageContent;
	protected List<String> imgQueue;
	
	public ImgFetcher(String url) {
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
		Matcher matcher = ImgFetcher.PATTERN_IMG.matcher(this.pageContent);
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
}
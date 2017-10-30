package page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AnchorFetcher fetch all anchors in a page
 * @author Xin
 * 
 */
public class AnchorFetcher implements Runnable {
	private final static Pattern PATTERN_ANCHOR = Pattern.compile("<a href=\"([^\"]*)\"[^<]*</a>");
	protected final String url;
	protected Set<String> anchorSet;
	protected int width;
	
	public AnchorFetcher(String url, Set<String> anchorSet, int width) {
		if (url.length() >= 4 && !url.substring(url.length() - 4, url.length()).equals("html") && url.charAt(url.length() - 1) != '/') {
			url = url + "/";
		}
		this.url = url;
		this.anchorSet = anchorSet;
		this.width = width;
	}
	
	private String openPage() {
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
		Matcher matcher = this.PATTERN_ANCHOR.matcher(openPage());
		String root = getRoot();
		int size = 0;
		
		while (matcher.find() && size < this.width) {
			StringBuilder anchorSB = new StringBuilder(matcher.group(1));
			
			if (anchorSB.length() >= 1 && anchorSB.substring(0, 1).equals("/")) {
				// relative path to root
				anchorSB = new StringBuilder(root + anchorSB.substring(1, anchorSB.length()));
			} else if (anchorSB.length() >= 4 && anchorSB.substring(0, 4).equals("http")) {
				// abs path
			} else if (anchorSB.length() >= 4 && anchorSB.substring(anchorSB.length() - 4, anchorSB.length()).equals("html")) {
				// leaf path
				anchorSB = new StringBuilder(root + anchorSB.substring(1, anchorSB.length()));
			} else {
				// relative path to cur file
				anchorSB = new StringBuilder(this.url + anchorSB);
			}
			
			String anchor = anchorSB.toString();
			if (!this.anchorSet.contains(anchor)) {
				this.anchorSet.add(anchor);
				size++;
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

	@Override
	public void run() {
		fetch();
	}
}
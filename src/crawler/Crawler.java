package crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import page.AnchorFetcher;
import page.IMGFetcher;

/**
 * Crawler main class for crawling
 * depth: crawling depth
 * width: for each page, crawling maximum size
 * @author Xin
 *
 */
public class Crawler {
	protected final String url;
	protected final int depth;
	protected final int width;
	protected Set<String> anchorSet;
	protected Set<String> imgSet;
	
	public Crawler(final String url, final int depth, final int width) {
		this.url = url;
		this.depth = depth;
		this.width = width;
		this.anchorSet = new HashSet<>();
		this.imgSet = new HashSet<>();
	}
	
	private void crawlAnchor() {
		anchorSet.add(url);
		
		// iterate with depth
		for (int i = 1; i <= depth; i++) {
			Set<String> anchorSetNext = new HashSet<>();
			for (String anchorEach : anchorSet) {
				// iterate with width
				Thread thread = new Thread(new AnchorFetcher(anchorEach, anchorSetNext, width));
				try {
					thread.start();
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
			anchorSet = anchorSetNext;
		}
	}
	
	private void crawlImg() {
		List<IMGFetcher> missionQueue = new ArrayList<>();
		for (String anchorEach : anchorSet) {
			missionQueue.add(new IMGFetcher(anchorEach));
		}
		
		for (IMGFetcher mission : missionQueue) {
			Thread thread = new Thread(mission);
			try {
				thread.start();
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (IMGFetcher mission : missionQueue) {
			imgSet.addAll(mission.getIMGList());
		}
	}
	
	public List<String> getImgList() {
		crawlAnchor();
		crawlImg();
		return imgSet.stream().collect(Collectors.toList());
	}
	
	public static void main(String[] args) {
		Crawler test = new Crawler("http://nutch.apache.org/", 3, 5);
		long startTime = System.nanoTime();
		List<String> result = test.getImgList();
		System.out.println(result.size());
		long endTime = System.nanoTime();
		System.out.println("Time: " + (endTime - startTime));
		for (String val : result) {
			System.out.println(val);
		}
	}
}

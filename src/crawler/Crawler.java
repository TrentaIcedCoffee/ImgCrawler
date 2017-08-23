package crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import page.AnchorFetcher;
import page.IMGFetcher;

/*
 * depth: crawling depth
 * width: for each page, crawling maximum size
 * 
 * public method:
 * Crawler(final String url, final int depth, final int width)
 * public List<String> getImgList()
 */
public class Crawler {
	protected final String _url;
	protected final int _depth;
	protected final int _width;
	protected Set<String> _anchorSet;
	protected Set<String> _imgSet;
	
	public Crawler(final String url, final int depth, final int width) {
		_url = url;
		_depth = depth;
		_width = width;
		_anchorSet = new HashSet<>();
		_imgSet = new HashSet<>();
	}
	
	private void crawlAnchor() {
		_anchorSet.add(_url);
		
		// iterate with depth
		for (int i = 1; i <= _depth; i++) {
			Set<String> _anchorSetNext = new HashSet<>();
			for (String anchorEach : _anchorSet) {
				// iterate with width
				Thread thread = new Thread(new AnchorFetcher(anchorEach, _anchorSetNext, _width));
				thread.start();
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			_anchorSet = _anchorSetNext;
		}
	}
	
	private void crawlImg() {
		List<IMGFetcher> missionQueue = new ArrayList<>();
		for (String anchorEach : _anchorSet) {
			missionQueue.add(new IMGFetcher(anchorEach));
		}
		
		for (IMGFetcher mission : missionQueue) {
			Thread thread = new Thread(mission);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (IMGFetcher mission : missionQueue) {
			_imgSet.addAll(mission.getIMGList());
		}
	}
	
	public List<String> getImgList() {
		crawlAnchor();
		crawlImg();
		return _imgSet.stream().collect(Collectors.toList());
	}
	
	public static void main(String[] args) {
		Crawler test = new Crawler("http://nutch.apache.org/", 3, 5);
		List<String> result = test.getImgList();
		System.out.println(result.size());
		for (String val : result) {
			System.out.println(val);
		}
	}
}

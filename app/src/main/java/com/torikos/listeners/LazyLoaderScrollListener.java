package com.torikos.listeners;


import android.os.Handler;
import android.widget.AbsListView;


public class LazyLoaderScrollListener implements AbsListView.OnScrollListener {
	
	public static final int SCROLL = 0;
	
	private static final int DEFAULT_THRESHOLD = 10;
	
	private boolean loading = true;
	private int previousTotal = 0;
	private int threshold = DEFAULT_THRESHOLD;
	
	private Handler _onScrollHandler;
	
	
	public LazyLoaderScrollListener() {
	}
	
	public LazyLoaderScrollListener(int threshold) {
		this.threshold = threshold;
	}
	
	
	public void initOnScrollHandler(Handler handler) {
		_onScrollHandler = handler;
	}
	
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}
		
		if (!loading && ((firstVisibleItem + visibleItemCount) >= (totalItemCount - threshold))) {
			loading = true;
			
			sendScrollMessage(firstVisibleItem, visibleItemCount);
		}
	}
	
	
	private void sendScrollMessage(int firstVisibleItem, int visibleItemCount) {
		if (_onScrollHandler != null) {
			_onScrollHandler.obtainMessage(SCROLL, firstVisibleItem, visibleItemCount, null).sendToTarget();
		}
	}
	
}

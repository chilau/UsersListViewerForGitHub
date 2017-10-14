package com.torikos.managers;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.torikos.data.User;
import com.torikos.managers.runnables.GetUserAvatarRunnable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class AvatarManager {
	
	public static final int SUCCESS = 0;
	
	private HashMap<String, Bitmap> _images;
	private final HashMap<String, ArrayList<Handler>> _completeHandlers;
	private ArrayList<String> _removeOldAvatarsUrlBuffer;
	
	private int _avatars_collection_max_count;
	
	
	public AvatarManager(int avatars_collection_max_count) {
		_images = new HashMap<>();
		_completeHandlers = new HashMap<>();
		_removeOldAvatarsUrlBuffer = new ArrayList<>();
		
		_avatars_collection_max_count = avatars_collection_max_count;
	}
	
	
	public void loadUserAvatar(User user, Handler handler) {
		synchronized (_completeHandlers) {
			if (!_completeHandlers.containsKey(user.avatar_url)) {
				ArrayList<Handler> completeHandlers = new ArrayList<>();
				completeHandlers.add(handler);
				_completeHandlers.put(user.avatar_url, completeHandlers);
			} else {
				ArrayList<Handler> completeHandlers = _completeHandlers.get(user.avatar_url);
				completeHandlers.add(handler);
			}
		}
		
		if (_images.containsKey(user.avatar_url)) {
			sendCompleteMessage(user);
		} else {
			synchronized (_completeHandlers) {
				ArrayList<Handler> completeHandlers = _completeHandlers.get(user.avatar_url);
				if (completeHandlers.size() > 1) {
					return;
				}
			}
			
			Thread sendRequestThread = new Thread(new GetUserAvatarRunnable(user) {
				@Override
				public void run() {
					try {
						User currentUser = getUser();
						if (currentUser != null) {
							String urlString = currentUser.avatar_url;
							
							URL url = new URL(urlString);
							
							HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
							httpURLConnection.setDoInput(true);
							httpURLConnection.connect();
							
							InputStream inputStream = httpURLConnection.getInputStream();
							Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
							_images.put(urlString, bitmap);
							
							clearOldAvatar();
							
							sendCompleteMessage(currentUser);
						}
					} catch (Exception ignored) {
					}
				}
			});
			sendRequestThread.start();
		}
	}
	
	
	private void clearOldAvatar() {
		if (_images.size() <= _avatars_collection_max_count) {
			return;
		}
		
		_images.remove(_removeOldAvatarsUrlBuffer.get(0));
		_removeOldAvatarsUrlBuffer.remove(0);
	}
	
	private void sendCompleteMessage(User user) {
		synchronized (_completeHandlers) {
			ArrayList<Handler> completeHandlers = _completeHandlers.get(user.avatar_url);
			for (int i = 0; i < completeHandlers.size(); i++) {
				Handler completeHandler = completeHandlers.get(i);
				completeHandler.obtainMessage(SUCCESS, -1, -1, user).sendToTarget();
			}
			
			completeHandlers.clear();
		}
	}
	
	public Bitmap getImage(String imageUrl) {
		if (_images == null) {
			return null;
		}
		
		if (!_images.containsKey(imageUrl)) {
			return null;
		}
		
		if (_removeOldAvatarsUrlBuffer.indexOf(imageUrl) != -1) {
			_removeOldAvatarsUrlBuffer.remove(imageUrl);
		}
		_removeOldAvatarsUrlBuffer.add(imageUrl);
		
		return _images.get(imageUrl);
	}
	
}

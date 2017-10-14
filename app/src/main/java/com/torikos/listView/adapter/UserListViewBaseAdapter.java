package com.torikos.listView.adapter;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.torikos.R;
import com.torikos.data.collections.RepositoriesArrayList;
import com.torikos.data.Repository;
import com.torikos.data.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UserListViewBaseAdapter extends BaseAdapter {
	
	public static final int NEED_UPDATE = 0;
	
	private Context _context;
	private LayoutInflater _layoutInflater;
	private ArrayList<User> _users;
	private HashMap<User, View> _views;
	private Handler _userListViewBaseAdapterHandler;
	
	
	public UserListViewBaseAdapter(Context context, ArrayList<User> users, Handler handler) {
		_context = context;
		_users = users;
		_layoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		_userListViewBaseAdapterHandler = handler;
		
		_views = new HashMap<>();
	}
	
	
	public void addToList(ArrayList<User> users) {
		_users.addAll(users);
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = _layoutInflater.inflate(R.layout.user_list_view_item, parent, false);
		}
		
		User oldUser = getUserByView(view);
		if (oldUser != null) {
			_views.put(oldUser, null);
		}
		
		User user = getUser(position);
		_views.put(user, view);
		
		ImageView imageView = (ImageView) view.findViewById(R.id.userAvatarImageView);
		TextView textView = (TextView) view.findViewById(R.id.userLoginTextView);
		
		textView.setText(user.login);
		imageView.setImageBitmap(null);
		imageView.invalidate();
		
		updateUserRepositories(user, view);
		
		if (_userListViewBaseAdapterHandler != null) {
			_userListViewBaseAdapterHandler.obtainMessage(NEED_UPDATE, -1, -1, user).sendToTarget();
		}
		
		return view;
	}
	
	
	public void updateUserAvatar(final User user, final Bitmap bitmap) {
		Activity activity = (Activity) _context;
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					View view = _views.get(user);
					
					if (view != null) {
						ImageView imageView = (ImageView) view.findViewById(R.id.userAvatarImageView);
						imageView.setImageDrawable(null);
						imageView.setImageBitmap(bitmap);
						imageView.invalidate();
					}
				}
			});
		}
	}
	
	public void updateUserRepositories(final User user) {
		Activity activity = (Activity) _context;
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					View view = _views.get(user);
					updateUserRepositories(user, view);
				}
			});
		}
	}
	
	private void updateUserRepositories(final User user, View view) {
		if (view != null) {
			TextView userRepositoriesTextView = (TextView) view.findViewById(R.id.userRepositoriesTextView);
			RepositoriesArrayList repositories = user.getRepositories();
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < repositories.size(); i++) {
				Repository repository = repositories.get(i);
				if (!stringBuilder.toString().isEmpty()) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(repository.full_name);
			}
			
			String repositoriesString = stringBuilder.toString();
			userRepositoriesTextView.setText(repositoriesString);
			
			if (!repositoriesString.isEmpty()) {
				userRepositoriesTextView.setVisibility(View.VISIBLE);
			} else {
				userRepositoriesTextView.setVisibility(View.GONE);
			}
		}
	}
	
	
	@Override
	public int getCount() {
		return _users.size();
	}
	
	@Override
	public Object getItem(int position) {
		return _users.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private User getUser(int position) {
		return ((User) getItem(position));
	}
	
	private User getUserByView(View view) {
		for (Object object : _views.entrySet()) {
			Map.Entry pair = (Map.Entry) object;
			
			User currentUser = (User) pair.getKey();
			View currentView = (View) pair.getValue();
			
			if (currentView != null && currentView.equals(view)) {
				return currentUser;
			}
		}
		
		return null;
	}
	
}

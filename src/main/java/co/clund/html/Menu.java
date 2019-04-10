package co.clund.html;

import java.util.ArrayList;
import java.util.List;

import co.clund.db.model.User;
import co.clund.module.Api;
import co.clund.module.Content;
import co.clund.module.Downloads;
import co.clund.module.Feed;
import co.clund.module.Forum;
import co.clund.module.Profile;
import co.clund.module.Settings;
import co.clund.module.Video;
import co.clund.submodule.video.EditPlatform;
import co.clund.submodule.video.LinkOnlySubscription;
import co.clund.submodule.video.ManageSubscriptions;
import co.clund.submodule.video.VideoIndex;

public class Menu {

	private static Menu dummyMenu = new Menu();

	public class MenuEntry {

		public final String name;
		public final String url;
		public final List<MenuEntry> subMenu;

		public MenuEntry(String name, String url) {
			this.name = name;
			this.url = url;
			this.subMenu = null;
		}

		public MenuEntry(String name, List<MenuEntry> subMenu) {
			this.name = name;
			this.url = null;
			this.subMenu = subMenu;
		}
	}

	public static List<MenuEntry> loadMenuData(User u) {
		List<MenuEntry> retList = new ArrayList<>();


		retList.add(dummyMenu.new MenuEntry("Content", "/" + Content.CONTENT_LOCATION));
		retList.add(dummyMenu.new MenuEntry("Downloads", "/" + Downloads.DOWNLOADS_LOCATION));
		retList.add(dummyMenu.new MenuEntry("Feed", "/" + Feed.FEED_LOCATION));
		retList.add(dummyMenu.new MenuEntry("Forum", "/" + Forum.FORUM_LOCATION));
		
		retList.add(dummyMenu.new MenuEntry("UrlSubscription Box", "/" + Video.VIDEO_LOCATION + "/" + LinkOnlySubscription.LOCATION));

		if (u == null) {
			retList.add(dummyMenu.new MenuEntry("Newest Videos", "/" + Video.VIDEO_LOCATION + "/" + VideoIndex.INDEX_LOCATION));
		} else {
			retList.add(dummyMenu.new MenuEntry("Videos & Subscriptions", "/" + Video.VIDEO_LOCATION + "/" + VideoIndex.INDEX_LOCATION));
			retList.add(dummyMenu.new MenuEntry("Manage your Subscriptions", "/" + Video.VIDEO_LOCATION + "/" + ManageSubscriptions.LOCATION));
			// retList.add(dummyMenu.new MenuEntry("Your channels", ViewChannel.LOCATION));

			if (u.getIsRoot()) {
				retList.add(dummyMenu.new MenuEntry("Manage Platforms", "/" + Video.VIDEO_LOCATION + "/" +  EditPlatform.LOCATION));
				retList.add(dummyMenu.new MenuEntry("Api Documentation", "/" + Video.VIDEO_LOCATION + "/" + Api.LOCATION));
			}
		}

		return retList;
	}

	public static MenuEntry loadUserMenuData(String username) {
		List<MenuEntry> userList = new ArrayList<>();

		userList.add(dummyMenu.new MenuEntry("Profile", "/" + Profile.LOCATION));
		userList.add(dummyMenu.new MenuEntry("Settings", "/" + Settings.LOCATION));
		userList.add(dummyMenu.new MenuEntry("Logout", "/" + Profile.LOCATION + "." + Profile.FUNCTION_NAME_LOGOUT));
		return dummyMenu.new MenuEntry(username, userList);
	}

}

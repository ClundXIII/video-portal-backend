package co.clund.video.html;

import java.util.ArrayList;
import java.util.List;

import co.clund.video.db.model.User;
import co.clund.video.module.Api;
import co.clund.video.module.EditPlatform;
import co.clund.video.module.Index;
import co.clund.video.module.LinkOnlySubscription;
import co.clund.video.module.ManageSubscriptions;
import co.clund.video.module.Profile;
import co.clund.video.module.Settings;

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

		retList.add(dummyMenu.new MenuEntry("UrlSubscription Box", LinkOnlySubscription.LOCATION));

		if (u == null) {
			retList.add(dummyMenu.new MenuEntry("Newest Videos", Index.INDEX_LOCATION));
		} else {
			retList.add(dummyMenu.new MenuEntry("Videos & Subscriptions", Index.INDEX_LOCATION));
			retList.add(dummyMenu.new MenuEntry("Manage your Subscriptions", ManageSubscriptions.LOCATION));
			// retList.add(dummyMenu.new MenuEntry("Your channels", ViewChannel.LOCATION));

			if (u.getIsRoot()) {
				retList.add(dummyMenu.new MenuEntry("Manage Platforms", EditPlatform.LOCATION));
				retList.add(dummyMenu.new MenuEntry("Api Documentation", Api.LOCATION));
			}
		}

		return retList;
	}

	public static MenuEntry loadUserMenuData(String username) {
		List<MenuEntry> userList = new ArrayList<>();

		userList.add(dummyMenu.new MenuEntry("Profile", Profile.LOCATION));
		userList.add(dummyMenu.new MenuEntry("Settings", Settings.LOCATION));
		userList.add(dummyMenu.new MenuEntry("Logout", Profile.LOCATION + "." + Profile.FUNCTION_NAME_LOGOUT));
		return dummyMenu.new MenuEntry(username, userList);
	}

}

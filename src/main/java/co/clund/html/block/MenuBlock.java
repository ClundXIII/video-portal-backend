package co.clund.html.block;

import static co.clund.html.Builder.escapeForHtml;

import org.json.JSONArray;
import org.json.JSONObject;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.submodule.core.dbmodel.TMenuStructure;

public class MenuBlock extends AbstractBlock {

	public MenuBlock() {
		super("menu");
	}

	@Override
	protected String render(DatabaseConnector dbCon, String pagePath, UserSession session, String content) {

		JSONObject jData = new JSONObject(content);

		String level = jData.getString("level");

		TMenuStructure tMenu = TMenuStructure.getTMenuStructureByKey(dbCon, jData.getString("structure"));

		JSONArray menuStructure = new JSONArray(tMenu.getContent());

		StringBuilder retData = new StringBuilder();

		retData.append("\n" + "<nav class='navbar navbar-expand-md navbar-light bg-light rounded'>\n"
				+ "<a class='navbar-brand visible-md' href='#'>Menu</a>\n"
				+ "<button class='navbar-toggler' type='button' data-toggle='collapse' "
				+ " data-target='#navbarsExample04' aria-controls='navbarsExample04' "
				+ " aria-expanded='false' aria-label='Toggle navigation'>\n"
				+ "<span class='navbar-toggler-icon'></span>\n </button> "
				+ "<div class='collapse navbar-collapse' id='navbarsExample04'>");

		if (level.equals("primary")) {
			retData.append("<ul class='navbar-nav mr-auto menu-primary'>");

			for (int i = 0; i < menuStructure.length(); i++) {
				retData.append(renderMenuEntry(menuStructure.getJSONObject(i)));
			}

			retData.append("</ul>");
		} else if (level.equals("secondary")) {

			retData.append("<ul class='navbar-nav mr-auto menu-secondary'>");

			JSONArray subMenuList = null;

			for (int i = 0; i < menuStructure.length(); i++) {
				JSONObject thisM = menuStructure.getJSONObject(i);
				if (thisM.getString("type").equals("submenu")
						&& (pagePath.substring(1).startsWith(thisM.getString("module")))) {
					subMenuList = thisM.getJSONArray("submenu");
				}
				if (thisM.getString("type").equals("manual-highlight")) {
					JSONArray activeForArray = thisM.getJSONArray("active-for");

					for (int j = 0; j < activeForArray.length(); j++) {
						if (pagePath.startsWith(activeForArray.getString(j))) {
							subMenuList = thisM.getJSONArray("submenu");
						}
					}
				}
			}

			if (subMenuList == null) {
				retData.append("error: cannot find secondary menu");
			} else {
				for (int i = 0; i < subMenuList.length(); i++) {
					retData.append(renderMenuEntry(subMenuList.getJSONObject(i)));
				}
			}

			retData.append("</ul>");
		}

		retData.append("\n</div></nav><br/>");

		return retData.toString();
	}

	private static String renderMenuEntry(JSONObject menuData) {
		StringBuilder stream = new StringBuilder();

		stream.append("<li class='nav-item'><a class='nav-link' a href='" + menuData.getString("link") + "'>"
				+ escapeForHtml(menuData.getString("name")) + "</a></li>" + "\n");

		return stream.toString();
	}

}

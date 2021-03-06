package co.clund.submodule.video;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.AbstractDbTable;
import co.clund.db.model.User;
import co.clund.html.HtmlForm;
import co.clund.html.HtmlPage;
import co.clund.html.HtmlTable;
import co.clund.html.HtmlForm.ButtonType;
import co.clund.html.HtmlForm.Method;
import co.clund.html.HtmlTable.HtmlTableRow;
import co.clund.module.AbstractModule;
import co.clund.module.FunctionResult;
import co.clund.submodule.video.dbmodel.VideoPlatform;

public class EditPlatform extends AbstractModule {

	public static final String LOCATION = "editPlatform";

	private static final String FUNCTION_NAME_ADD = "add";
	private static final String FUNCTION_NAME_EDIT = "edit";

	private static final String GET_PARAM_EDIT = "edit";
	private static final String GET_PARAM_VALUE_PLATFORM = "platform";

	public EditPlatform(AbstractModule parent, DatabaseConnector dbCon) {
		super(parent.getModulePath(), LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		User thisUser = s.getThisUser();
		if ((thisUser == null) || (!thisUser.getIsRoot())) {
			return "no access".getBytes();
		}

		HtmlPage p = new HtmlPage("Edit Platforms", s, dbCon, "/video/" + LOCATION);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		HtmlTable platformTable = new HtmlTable();

		platformTable.addHeader(Arrays.asList("id", "key", "name", "type", "OAuth2 platform id", "edit"));

		platformTable.startBody();

		for (VideoPlatform plat : VideoPlatform.getAllPlatform(dbCon)) {
			HtmlTableRow row = platformTable.new HtmlTableRow();

			row.writeText(plat.getId() + "");
			row.writeText(plat.getKey());
			row.writeText(plat.getName());
			row.writeText(plat.getType());
			row.writeText(plat.getOauth2PlatId() + "");

			HtmlForm editPlatformForm = new HtmlForm(LOCATION, Method.GET);
			editPlatformForm.addHiddenElement(GET_PARAM_EDIT, GET_PARAM_VALUE_PLATFORM);
			editPlatformForm.addHiddenElement(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID, plat.getId() + "");
			editPlatformForm.addSubmit("Edit", ButtonType.INFO);
			row.write(editPlatformForm);

			platformTable.write(row);
		}

		p.write(platformTable);

		p.writeHline();

		if (!parameters.containsKey(GET_PARAM_EDIT) || (parameters.get(GET_PARAM_EDIT) == null)) {

			p.writeH2("Add new VideoPlatform");

			HtmlForm addPlatF = new HtmlForm(LOCATION + "." + FUNCTION_NAME_ADD, Method.POST);

			addPlatF.addTextElement("Key", VideoPlatform.DB_TABLE_COLUMN_NAME_KEY, "");
			addPlatF.addTextElement("Name", VideoPlatform.DB_TABLE_COLUMN_NAME_NAME, "");
			addPlatF.addTextElement("Type", VideoPlatform.DB_TABLE_COLUMN_NAME_TYPE, "");
			addPlatF.addTextElement("OAuth2 platform id", VideoPlatform.DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID, "");
			addPlatF.addSubmit("Edit", ButtonType.WARNING);

			p.write(addPlatF);

		} else {

			p.writeLink(getModuleName(), "back");

			switch (parameters.get(GET_PARAM_EDIT)[0]) {
			case GET_PARAM_VALUE_PLATFORM: {

				int id = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

				VideoPlatform plat = VideoPlatform.getPlatformById(dbCon, id);

				p.writeH2("Edit platform " + plat.getName());

				HtmlForm editPlatF = new HtmlForm(LOCATION + "." + FUNCTION_NAME_EDIT, Method.POST);

				editPlatF.addHiddenElement(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID, plat.getId() + "");
				editPlatF.addTextElement("Key", VideoPlatform.DB_TABLE_COLUMN_NAME_KEY, plat.getKey());
				editPlatF.addTextElement("Name", VideoPlatform.DB_TABLE_COLUMN_NAME_NAME, plat.getName());
				editPlatF.addTextElement("Type", VideoPlatform.DB_TABLE_COLUMN_NAME_TYPE, plat.getType());
				editPlatF.addTextElement("OAuth2 platform id", VideoPlatform.DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID,
						plat.getOauth2PlatId() + "");
				editPlatF.addTextArea(VideoPlatform.DB_TABLE_COLUMN_NAME_CONFIG, plat.getConfig().toString(4), 40, 40);
				editPlatF.addSubmit("Edit", ButtonType.WARNING);

				p.write(editPlatF);

				break;
			}

			default: {
				p.writeText("get param not found");
			}

			}
		}

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();
		allFunctions.put(FUNCTION_NAME_ADD, this::addPlatform);
		allFunctions.put(FUNCTION_NAME_EDIT, this::editPlatform);

		return allFunctions;
	}

	private FunctionResult addPlatform(UserSession s, Map<String, String[]> parameters) {

		User thisUser = s.getThisUser();

		if ((thisUser == null) || (!thisUser.getIsRoot())) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "not logged in");
		}

		String key = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_KEY)[0];
		String pname = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_NAME)[0];
		String type = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_TYPE)[0];
		int oAuth2PlatId = new Integer(parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID)[0])
				.intValue();

		VideoPlatform.addNewPlatform(dbCon, key, pname, type, oAuth2PlatId);

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

	private FunctionResult editPlatform(UserSession s, Map<String, String[]> parameters) {

		User thisUser = s.getThisUser();

		if ((thisUser == null) || (!thisUser.getIsRoot())) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "not logged in");
		}

		int platformId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);
		String key = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_KEY)[0];
		String pname = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_NAME)[0];
		String type = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_TYPE)[0];
		String config = parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_CONFIG)[0];
		int oAuth2PlatId = new Integer(parameters.get(VideoPlatform.DB_TABLE_COLUMN_NAME_OAUHT2_PLATFORM_ID)[0])
				.intValue();

		VideoPlatform plat = VideoPlatform.getPlatformById(dbCon, platformId);

		plat.edit(dbCon, key, pname, type, config, oAuth2PlatId);

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

}

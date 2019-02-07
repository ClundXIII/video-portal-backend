package co.clai.video.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import co.clai.video.UserSession;
import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.AbstractDbTable;
import co.clai.video.db.model.Platform;
import co.clai.video.db.model.User;
import co.clai.video.html.HtmlForm;
import co.clai.video.html.HtmlPage;
import co.clai.video.html.HtmlTable;
import co.clai.video.html.HtmlForm.ButtonType;
import co.clai.video.html.HtmlForm.Method;
import co.clai.video.html.HtmlTable.HtmlTableRow;

public class EditPlatform extends AbstractModule {

	public static final String LOCATION = "editPlatform";

	private static final String FUNCTION_NAME_ADD = "add";
	private static final String FUNCTION_NAME_EDIT = "edit";

	private static final String GET_PARAM_EDIT = "edit";
	private static final String GET_PARAM_VALUE_PLATFORM = "platform";

	public EditPlatform(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		User thisUser = s.getThisUser();
		if ((thisUser == null) || (!thisUser.getIsRoot())) {
			return "no access".getBytes();
		}

		HtmlPage p = new HtmlPage("Edit Platforms", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		HtmlTable platformTable = new HtmlTable();

		platformTable.addHeader(Arrays.asList("id", "key", "name", "type", "edit"));

		platformTable.startBody();

		for (Platform plat : Platform.getAllPlatform(dbCon)) {
			HtmlTableRow row = platformTable.new HtmlTableRow();

			row.writeText(plat.getId() + "");
			row.writeText(plat.getKey());
			row.writeText(plat.getName());
			row.writeText(plat.getType());

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

			p.writeH2("Add new Platform");

			HtmlForm addPlatF = new HtmlForm(LOCATION + "." + FUNCTION_NAME_ADD, Method.POST);

			addPlatF.addTextElement("Key", Platform.DB_TABLE_COLUMN_NAME_KEY, "");
			addPlatF.addTextElement("Name", Platform.DB_TABLE_COLUMN_NAME_NAME, "");
			addPlatF.addTextElement("Type", Platform.DB_TABLE_COLUMN_NAME_TYPE, "");
			addPlatF.addSubmit("Edit", ButtonType.WARNING);

			p.write(addPlatF);

		} else {

			p.writeLink(getModuleName(), "back");

			switch (parameters.get(GET_PARAM_EDIT)[0]) {
			case GET_PARAM_VALUE_PLATFORM: {

				int id = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

				Platform plat = Platform.getPlatformById(dbCon, id);

				p.writeH2("Edit platform " + plat.getName());

				HtmlForm editPlatF = new HtmlForm(LOCATION + "." + FUNCTION_NAME_EDIT, Method.POST);

				editPlatF.addHiddenElement(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID, plat.getId() + "");
				editPlatF.addTextElement("Key", Platform.DB_TABLE_COLUMN_NAME_KEY, plat.getKey());
				editPlatF.addTextElement("Name", Platform.DB_TABLE_COLUMN_NAME_NAME, plat.getName());
				editPlatF.addTextElement("Type", Platform.DB_TABLE_COLUMN_NAME_TYPE, plat.getType());
				editPlatF.addTextArea(Platform.DB_TABLE_COLUMN_NAME_CONFIG, plat.getConfig().toString(4), 40, 40);
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

		String key = parameters.get(Platform.DB_TABLE_COLUMN_NAME_KEY)[0];
		String pname = parameters.get(Platform.DB_TABLE_COLUMN_NAME_NAME)[0];
		String type = parameters.get(Platform.DB_TABLE_COLUMN_NAME_TYPE)[0];

		Platform.addNewPlatform(dbCon, key, pname, type);

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

	private FunctionResult editPlatform(UserSession s, Map<String, String[]> parameters) {

		User thisUser = s.getThisUser();

		if ((thisUser == null) || (!thisUser.getIsRoot())) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "not logged in");
		}

		int platformId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);
		String key = parameters.get(Platform.DB_TABLE_COLUMN_NAME_KEY)[0];
		String pname = parameters.get(Platform.DB_TABLE_COLUMN_NAME_NAME)[0];
		String type = parameters.get(Platform.DB_TABLE_COLUMN_NAME_TYPE)[0];
		String config = parameters.get(Platform.DB_TABLE_COLUMN_NAME_CONFIG)[0];

		Platform plat = Platform.getPlatformById(dbCon, platformId);

		plat.edit(dbCon, key, pname, type, config);

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

}

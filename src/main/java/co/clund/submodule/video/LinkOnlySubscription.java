package co.clund.submodule.video;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.exception.RateLimitException;
import co.clund.html.HtmlForm;
import co.clund.html.HtmlGenericDiv;
import co.clund.html.HtmlPage;
import co.clund.html.HtmlResponsiveColumns;
import co.clund.html.HtmlTable;
import co.clund.html.HtmlForm.ButtonType;
import co.clund.html.HtmlForm.Method;
import co.clund.html.HtmlTable.HtmlTableRow;
import co.clund.module.AbstractModule;
import co.clund.module.FunctionResult;
import co.clund.module.FunctionResult.Status;
import co.clund.submodule.video.dbmodel.Platform;
import co.clund.submodule.video.platform.AbstractPlatform;
import co.clund.submodule.video.subscription.SubscriptionHelper;

public class LinkOnlySubscription extends AbstractModule {

	public static final String LOCATION = "linkSub";

	public static final String FUNCTION_NAME_ADD_CHANNEL = "addChannel";

	public static final String GET_PARAM_NEW_CHANNEL = "newChannel";
	public static final String GET_PARAM_SUB = "s";

	private final SubscriptionHelper subHelper;

	public LinkOnlySubscription(AbstractModule parent, DatabaseConnector dbCon) {
		super(parent.getModulePath(), LOCATION, dbCon);
		subHelper = new SubscriptionHelper(dbCon, (co.clund.module.Video) parent);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage b = new HtmlPage("Url Subscription - Video Portal", s, dbCon, "/video/" + LOCATION);

		HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

		cols.startColumn(8);

		if (parameters.containsKey(GET_PARAM_SUB)) {
			List<HtmlGenericDiv> list;
			try {
				list = subHelper.renderOrderedSubscribedVideosFromList(Arrays.asList(parameters.get(GET_PARAM_SUB)));

				int videoDisplayCount = 0;
				for (HtmlGenericDiv div : list) {
					if (videoDisplayCount > 50) {
						break;
					}
					cols.write(div);
					videoDisplayCount++;
				}
			} catch (Exception e) {
				cols.writeText("error while loading videos: " + e.getMessage());
			}
		}

		cols.startColumn(4);

		cols.writeH3("Current Channels:");

		Set<String> allChannels = new HashSet<>();

		if (parameters.containsKey(GET_PARAM_SUB)) {

			allChannels = new HashSet<>(Arrays.asList(parameters.get(GET_PARAM_SUB)));

			HtmlTable channelTable = new HtmlTable();
			for (String channelKey : parameters.get(GET_PARAM_SUB)) {

				HtmlTableRow thisRow = channelTable.new HtmlTableRow();

				String platformKey = channelKey.substring(0, channelKey.indexOf("_"));
				String channelIdentifier = channelKey.substring(channelKey.indexOf("_") + 1);

				Platform plat = Platform.getPlatformByKey(dbCon, platformKey);

				AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(plat);

				try {
					thisRow.writeLink(abPlat.getOriginalChannelLink(channelIdentifier),
							abPlat.getCachedChannelName(channelIdentifier), true);
				} catch (RateLimitException e) {
					logger.log(Level.WARNING, "Error while getting channel name: " + e.getMessage());
					thisRow.writeLink(abPlat.getOriginalChannelLink(channelIdentifier),
							"<error>", true);
				}

				Set<String> allChannelsCpy = new HashSet<>(allChannels);

				allChannelsCpy.remove(channelKey);

				HtmlForm form = new HtmlForm(LOCATION, Method.GET);

				for (String tmpChannelKey : allChannelsCpy) {
					form.addHiddenElement(GET_PARAM_SUB, tmpChannelKey);
				}

				form.addSubmit("Delete from List", ButtonType.DANGER);

				thisRow.write(form);

				channelTable.write(thisRow);
			}
			cols.write(channelTable);
		}

		HtmlForm form = new HtmlForm(LOCATION + "." + FUNCTION_NAME_ADD_CHANNEL, Method.GET);

		for (String tmpChannelKey : allChannels) {
			form.addHiddenElement(GET_PARAM_SUB, tmpChannelKey);
		}

		cols.writeText("Paste the channel link into the box below to add a channel. In order to remember the list, "
				+ "just set a bookmark to the page! The channel list is encoded in the URL.");

		form.addTextElement("Channel Link:", GET_PARAM_NEW_CHANNEL, "");

		form.addSubmit("Add to List", ButtonType.SUCCESS);

		cols.write(form);

		b.write(cols);

		return b.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		allFunctions.put(FUNCTION_NAME_ADD_CHANNEL, this::addChannel);

		return allFunctions;
	}

	@SuppressWarnings("unused")
	private FunctionResult addChannel(UserSession s, Map<String, String[]> parameters) {
		URIBuilder redirect = null;
		try {
			redirect = new URIBuilder(LOCATION);

			if (parameters.containsKey(GET_PARAM_SUB)) {
				for (String channelIdentifier : parameters.get(GET_PARAM_SUB)) {
					redirect.addParameter(GET_PARAM_SUB, channelIdentifier);
				}
			}

			String channelUrl = parameters.get(GET_PARAM_NEW_CHANNEL)[0];

			String platformKey = null;

			for (Entry<Pattern, String> regExpEntry : Platform.getPlatformRegExps(dbCon).entrySet()) {
				System.out.println("trying " + regExpEntry.getKey().pattern());
				if (regExpEntry.getKey().matcher(channelUrl).find()) {
					platformKey = regExpEntry.getValue();
					System.out.println("matches " + regExpEntry.getValue());
					break;
				}
			}

			if (platformKey == null) {
				return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), "cannot detect platform!");
			}

			Platform plat = Platform.getPlatformByKey(dbCon, platformKey);

			AbstractPlatform abPlat = AbstractPlatform.getPlatformFromConfig(plat);

			String channelIdentifier = abPlat.getChannelIdentifierFromUrl(channelUrl);

			redirect.addParameter(GET_PARAM_SUB, plat.getKey() + "_" + channelIdentifier);

			return new FunctionResult(Status.OK, redirect);
		} catch (URISyntaxException | RateLimitException e) {
			e.printStackTrace();
			if (redirect != null) {
				return new FunctionResult(Status.INTERNAL_ERROR, redirect);
			}
			throw new RuntimeException(e);
		}
	}
}

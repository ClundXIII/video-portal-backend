package co.clund.submodule.video;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.db.model.AbstractDbTable;
import co.clund.db.model.User;
import co.clund.html.HtmlForm;
import co.clund.html.HtmlGenericDiv;
import co.clund.html.HtmlPage;
import co.clund.html.HtmlResponsiveColumns;
import co.clund.html.HtmlStyleConstants;
import co.clund.html.HtmlTable;
import co.clund.html.HtmlForm.ButtonType;
import co.clund.html.HtmlForm.Method;
import co.clund.html.HtmlTable.HtmlTableRow;
import co.clund.module.AbstractModule;
import co.clund.module.FunctionResult;
import co.clund.module.Profile;
import co.clund.oauth2.AbstractOAuth2UserPlatform;
import co.clund.submodule.video.dbmodel.Channel;
import co.clund.submodule.video.dbmodel.ExternalSubscription;
import co.clund.submodule.video.dbmodel.InternalSubscription;
import co.clund.submodule.video.dbmodel.VideoPlatform;
import co.clund.submodule.video.platform.AbstractVideoPlatform;
import co.clund.submodule.video.platform.PlatformVideo;

public class ManageSubscriptions extends AbstractModule {

	public static final String LOCATION = "subs";

	private static final String FUNCTION_NAME_ADD_SUBSCRIPTION = "addSubscription";
	private static final String FUNCTION_NAME_REMOVE_INT_SUBSCRIPTION = "removeIntSubscription";
	private static final String FUNCTION_NAME_REMOVE_EXT_SUBSCRIPTION = "removeExtSubscription";

	private static final String GET_PARAM_CHANNEL_URL = "channel_url";

	public ManageSubscriptions(AbstractModule parent, DatabaseConnector dbCon) {
		super(parent.getModulePath(), LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("Manage your Subscriptions", s, dbCon, "/video/" + LOCATION);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		User thisUser = s.getThisUser();
		if (thisUser == null) {
			p.writeLink(Profile.LOCATION, "Log in to manage your subscriptions");
			return p.finish().getBytes();
		}

		HtmlGenericDiv infoText = new HtmlGenericDiv(HtmlStyleConstants.DIV_ID_INFO_TEXT);
		infoText.writeText("Internal subscriptions are subscriptions to channels that have been "
				+ "added to this website by the user. They can be channels from any "
				+ "platform as well. External subscriptions are subscriptions to channels "
				+ "where the owner has not yet registered on this platform. Go and ask "
				+ "them to join us here! Once someone registered here and connected "
				+ "their channel to out platform, your subscription will automatically "
				+ "be converted into an internal subscription.");
		p.write(infoText);

		HtmlForm addNewSubForm = new HtmlForm(getModuleName() + "." + FUNCTION_NAME_ADD_SUBSCRIPTION,
				HtmlForm.Method.POST);
		addNewSubForm.addTextElement("URL", GET_PARAM_CHANNEL_URL, "");
		addNewSubForm.addSubmit("Subscribe", ButtonType.SUCCESS);
		p.write(addNewSubForm);

		HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

		cols.startColumn(6);

		cols.writeH2("Internal Subscriptions:");

		HtmlTable intSubTable = new HtmlTable();

		intSubTable.addHeader(Arrays.asList("Channel", "User", "VideoPlatform", "Unsubscribe"));

		List<InternalSubscription> internalSubs = InternalSubscription.getInternalSubscriptionByUserId(dbCon,
				thisUser.getId());

		for (InternalSubscription intSub : internalSubs) {

			try {

				HtmlTableRow row = intSubTable.new HtmlTableRow();

				Channel c = Channel.getChannelById(dbCon, intSub.getChannelId());

				row.writeText(c.getName());

				User u = User.getUserById(dbCon, c.getOwnerId());

				row.writeText(u.getUsername());

				VideoPlatform plat = VideoPlatform.getPlatformById(dbCon, c.getPlatformId());

				row.writeText(plat.getName());

				HtmlForm unsubForm = new HtmlForm(getModuleName() + "." + FUNCTION_NAME_REMOVE_INT_SUBSCRIPTION,
						Method.POST);
				unsubForm.addHiddenElement(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID, intSub.getId() + "");
				unsubForm.addSubmit("Unsubscribe", ButtonType.DANGER);
				row.write(unsubForm);

				intSubTable.write(row);
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"Error rendering internal sub with id: " + intSub.getId() + ": " + e.getMessage());
			}
		}
		cols.write(intSubTable);

		cols.startColumn(6);

		cols.writeH2("External Subscriptions:");

		HtmlTable extSubTable = new HtmlTable();

		extSubTable.addHeader(Arrays.asList("Channel", "User", "VideoPlatform", "Unsubscribe"));

		List<ExternalSubscription> externalSubs = ExternalSubscription.getExternalSubscriptionByUserId(dbCon,
				thisUser.getId());

		for (ExternalSubscription extSub : externalSubs) {

			try {

				HtmlTableRow row = extSubTable.new HtmlTableRow();

				VideoPlatform plat = VideoPlatform.getPlatformById(dbCon, extSub.getPlatformId());

				AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = PlatformVideo.getOAuth2PlatformIfNeeded(dbCon,
						plat);
				AbstractVideoPlatform f = AbstractVideoPlatform.getPlatformFromConfig(plat, abstractOAuth2UserPlatform);

				row.writeLink(f.getOriginalChannelLink(extSub.getChannelIdentifier()),
						f.getCachedChannelName(extSub.getChannelIdentifier()), true);

				row.writeText(f.getCachedUserName(extSub.getChannelIdentifier()));

				row.writeText(plat.getName());

				HtmlForm unsubForm = new HtmlForm(getModuleName() + "." + FUNCTION_NAME_REMOVE_EXT_SUBSCRIPTION,
						Method.POST);
				unsubForm.addHiddenElement(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID, extSub.getId() + "");
				unsubForm.addSubmit("Unsubscribe", ButtonType.DANGER);
				row.write(unsubForm);

				extSubTable.write(row);
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"Error rendering external sub with id: " + extSub.getId() + ": " + e.getMessage());
			}
		}

		cols.write(extSubTable);

		p.write(cols);

		p.writeText(
				"If you want to use this subscription list without logging in or share it with a friend, use this link: ");

		try {
			URIBuilder urlSubBuilder = new URIBuilder(LinkOnlySubscription.LOCATION);

			for (ExternalSubscription sub : externalSubs) {
				VideoPlatform plat = VideoPlatform.getPlatformById(dbCon, sub.getPlatformId());

				urlSubBuilder.addParameter(LinkOnlySubscription.GET_PARAM_SUB,
						plat.getKey() + "_" + sub.getChannelIdentifier());
			}

			p.writeLink(urlSubBuilder, "url Based Subscription Box");
		} catch (URISyntaxException e) {
			logger.log(Level.WARNING,
					"error while trying to create sub export list link for user id " + thisUser.getId());
			e.printStackTrace();
		}

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		allFunctions.put(FUNCTION_NAME_ADD_SUBSCRIPTION, this::addSubscription);
		allFunctions.put(FUNCTION_NAME_REMOVE_INT_SUBSCRIPTION, this::removeIntSubscription);
		allFunctions.put(FUNCTION_NAME_REMOVE_EXT_SUBSCRIPTION, this::removeExtSubscription);

		return allFunctions;
	}

	private FunctionResult addSubscription(UserSession s, Map<String, String[]> parameters) {

		if (s.getThisUser() == null) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(),
					"You need to be logged in in order to subscribe!");
		}

		try {

			String channelUrl = parameters.get(GET_PARAM_CHANNEL_URL)[0];

			String platformKey = null;

			for (Entry<Pattern, String> regExpEntry : VideoPlatform.getPlatformRegExps(dbCon).entrySet()) {
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

			VideoPlatform plat = VideoPlatform.getPlatformByKey(dbCon, platformKey);

			AbstractOAuth2UserPlatform abstractOAuth2UserPlatform = PlatformVideo.getOAuth2PlatformIfNeeded(dbCon,
					plat);
			AbstractVideoPlatform abPlat = AbstractVideoPlatform.getPlatformFromConfig(plat,
					abstractOAuth2UserPlatform);

			String channelIdentifier = abPlat.getChannelIdentifierFromUrl(channelUrl);

			Channel chan = Channel.getChannelByPlatformIdAndIdentifier(dbCon, plat.getId(), channelIdentifier);

			if (chan == null) {
				ExternalSubscription.addNewExternalSubscription(dbCon, s.getThisUser().getId(), plat.getId(),
						channelIdentifier);
			} else {
				InternalSubscription.addNewInternalSubscription(dbCon, s.getThisUser().getId(), chan.getId());
			}

		} catch (Exception e) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(), e.getMessage());
		}

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

	private FunctionResult removeIntSubscription(UserSession s, Map<String, String[]> parameters) {

		if (s.getThisUser() == null) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(),
					"You need to be logged in in order to subscribe!");
		}

		int subId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

		InternalSubscription sub = InternalSubscription.getInternalSubscriptionById(dbCon, subId);

		if (sub.getUserId() != s.getThisUser().getId()) {
			return new FunctionResult(FunctionResult.Status.NO_ACCESS, getModuleName());
		}

		sub.delete(dbCon);

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}

	private FunctionResult removeExtSubscription(UserSession s, Map<String, String[]> parameters) {

		if (s.getThisUser() == null) {
			return new FunctionResult(FunctionResult.Status.FAILED, getModuleName(),
					"You need to be logged in in order to subscribe!");
		}

		int subId = Integer.parseInt(parameters.get(AbstractDbTable.DB_TABLE_COLUMN_NAME_ID)[0]);

		ExternalSubscription sub = ExternalSubscription.getExternalSubscriptionById(dbCon, subId);

		if (sub.getUserId() != s.getThisUser().getId()) {
			return new FunctionResult(FunctionResult.Status.NO_ACCESS, getModuleName());
		}

		sub.delete(dbCon);

		return new FunctionResult(FunctionResult.Status.OK, getModuleName());
	}
}

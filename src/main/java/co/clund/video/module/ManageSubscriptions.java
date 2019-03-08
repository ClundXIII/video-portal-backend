package co.clund.video.module;

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

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.AbstractDbTable;
import co.clund.video.db.model.Channel;
import co.clund.video.db.model.ExternalSubscription;
import co.clund.video.db.model.InternalSubscription;
import co.clund.video.db.model.Platform;
import co.clund.video.db.model.User;
import co.clund.video.html.HtmlForm;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlPage;
import co.clund.video.html.HtmlResponsiveColumns;
import co.clund.video.html.HtmlStyleConstants;
import co.clund.video.html.HtmlTable;
import co.clund.video.html.HtmlForm.ButtonType;
import co.clund.video.html.HtmlForm.Method;
import co.clund.video.html.HtmlTable.HtmlTableRow;
import co.clund.video.platform.AbstractPlatform;

public class ManageSubscriptions extends AbstractModule {

	public static final String LOCATION = "subs";

	private static final String FUNCTION_NAME_ADD_SUBSCRIPTION = "addSubscription";
	private static final String FUNCTION_NAME_REMOVE_INT_SUBSCRIPTION = "removeIntSubscription";
	private static final String FUNCTION_NAME_REMOVE_EXT_SUBSCRIPTION = "removeExtSubscription";

	private static final String GET_PARAM_CHANNEL_URL = "channel_url";

	public ManageSubscriptions(DatabaseConnector dbCon) {
		super(LOCATION, dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("Manage your Subscriptions", null, null, s);

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

		intSubTable.addHeader(Arrays.asList("Channel", "User", "Platform", "Unsubscribe"));

		List<InternalSubscription> internalSubs = InternalSubscription.getInternalSubscriptionByUserId(dbCon,
				thisUser.getId());

		for (InternalSubscription intSub : internalSubs) {

			try {

				HtmlTableRow row = intSubTable.new HtmlTableRow();

				Channel c = Channel.getChannelById(dbCon, intSub.getChannelId());

				row.writeText(c.getName());

				User u = User.getUserById(dbCon, c.getOwnerId());

				row.writeText(u.getUsername());

				Platform plat = Platform.getPlatformById(dbCon, c.getPlatformId());

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

		extSubTable.addHeader(Arrays.asList("Channel", "User", "Platform", "Unsubscribe"));

		List<ExternalSubscription> externalSubs = ExternalSubscription.getExternalSubscriptionByUserId(dbCon,
				thisUser.getId());

		for (ExternalSubscription extSub : externalSubs) {

			try {

				HtmlTableRow row = extSubTable.new HtmlTableRow();

				Platform plat = Platform.getPlatformById(dbCon, extSub.getPlatformId());

				AbstractPlatform f = AbstractPlatform.getPlatformFromConfig(plat);

				row.writeLink(f.getOriginalChannelLink(extSub.getChannelIdentifier()),
						f.getChannelName(extSub.getChannelIdentifier()), true);

				row.writeText(f.getUserName(extSub.getChannelIdentifier()));

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
				Platform plat = Platform.getPlatformById(dbCon, sub.getPlatformId());

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

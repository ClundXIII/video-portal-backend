package co.clund.video.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import co.clund.video.UserSession;
import co.clund.video.db.DatabaseConnector;
import co.clund.video.db.model.User;
import co.clund.video.html.Builder;
import co.clund.video.html.HtmlGenericDiv;
import co.clund.video.html.HtmlPage;
import co.clund.video.html.HtmlResponsiveColumns;
import co.clund.video.html.HtmlStyleConstants;
import co.clund.video.subscription.SubscriptionHelper;

public class Index extends AbstractModule {

	public static final String INDEX_LOCATION = "index";

	private final SubscriptionHelper subscriptionHelper;

	public Index(DatabaseConnector dbCon) {
		super(INDEX_LOCATION, dbCon);

		subscriptionHelper = new SubscriptionHelper(dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("Video Portal", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

		User thisUser = s.getThisUser();
		if (thisUser == null) {
			p.writeLink(Profile.LOCATION, "Log in");
			p.writeText(" to see your subscriptions");
			p.newLine();
			p.writeText("Or use the URL based ");
			p.writeLink(LinkOnlySubscription.LOCATION, "subscription box");
			p.writeText(".");
		} else {
			cols.startColumn(8);
			cols.writeH1("Livestreams");
			try {
				HtmlGenericDiv newVidsDiv = new HtmlGenericDiv(HtmlStyleConstants.DIV_ID_NEW_VIDEOS);

				List<HtmlGenericDiv> newVideos = subscriptionHelper.renderLiveStreams(thisUser);

				if (newVideos == null) {
					newVidsDiv.writeText("error loading recent videos");
				} else {
					for (Builder video : newVideos) {
						newVidsDiv.write(video);
					}
				}

				cols.write(newVidsDiv);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error while loading recent videos: " + e.getMessage());
				e.printStackTrace();
				cols.writeText("--error--");
			}

			cols.writeH1("Videos from Subscriptions");
			try {
				HtmlGenericDiv newVidsDiv = new HtmlGenericDiv(HtmlStyleConstants.DIV_ID_NEW_VIDEOS);

				List<HtmlGenericDiv> newVideos = subscriptionHelper.renderOrderedSubscribedVideosList(thisUser);

				if (newVideos == null) {
					newVidsDiv.writeText("error loading recent videos");
				} else {

					if (newVideos.isEmpty()) {
						newVidsDiv.writeText("cannot find any videos! ");
						newVidsDiv.writeLink(ManageSubscriptions.LOCATION, "click here");
						newVidsDiv.writeText(" to manage your Subscriptions.");
					} else {

						int videoDisplayCount = 0;
						for (Builder video : newVideos) {
							if (videoDisplayCount > 50) {
								break;
							}
							newVidsDiv.write(video);
							videoDisplayCount++;
						}
					}
				}

				cols.write(newVidsDiv);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "error while loading recent videos: " + e.getMessage());
				e.printStackTrace();
				cols.writeText("--error--");
			}

		}

		if (thisUser != null) {
			cols.startColumn(4);
		} else {
			cols.startColumn(12);
		}

		cols.writeH1("Newest videos");

		try {
			HtmlGenericDiv newVidsDiv = new HtmlGenericDiv(HtmlStyleConstants.DIV_ID_NEW_VIDEOS);

			List<HtmlGenericDiv> newVideos = subscriptionHelper.renderOrderedNewestVideosList();

			if (newVideos == null) {
				newVidsDiv.writeText("error loading recent videos");
			} else {
				int videoDisplayCount = 0;
				for (Builder video : newVideos) {
					if (videoDisplayCount > 10) {
						break;
					}
					newVidsDiv.write(video);
					videoDisplayCount++;
				}
			}

			cols.write(newVidsDiv);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error while loading recent videos: " + e.getMessage());
			e.printStackTrace();
			cols.writeText("--error--");
		}

		p.write(cols);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}

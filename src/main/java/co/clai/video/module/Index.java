package co.clai.video.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;

import co.clai.video.db.DatabaseConnector;
import co.clai.video.db.model.User;
import co.clai.video.html.HtmlGenericDiv;
import co.clai.video.html.HtmlPage;
import co.clai.video.html.HtmlResponsiveColumns;
import co.clai.video.html.HtmlStyleConstants;
import co.clai.video.subscription.SubscriptionHelper;
import co.clai.video.UserSession;
import co.clai.video.html.Builder;

public class Index extends AbstractModule {

	public static final String INDEX_LOCATION = "index";

	private final SubscriptionHelper subscriptionHelper;

	public Index(DatabaseConnector dbCon) {
		super(INDEX_LOCATION, dbCon);

		subscriptionHelper = new SubscriptionHelper(dbCon);
	}

	@Override
	protected byte[] invokePlain(UserSession s, Map<String, String[]> parameters) {

		HtmlPage p = new HtmlPage("CLAI Video Portal", null, null, s);

		p.writeWithoutEscaping(HtmlPage.getMessage(parameters));

		HtmlResponsiveColumns cols = new HtmlResponsiveColumns();

		User thisUser = s.getThisUser();
		if (thisUser == null) {
			p.writeLink(Profile.LOCATION, "Log in to see your subscriptions");
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

						for (Builder video : newVideos) {
							newVidsDiv.write(video);
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

		p.write(cols);

		return p.finish().getBytes();
	}

	@Override
	protected Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> loadFunctions() {
		Map<String, BiFunction<UserSession, Map<String, String[]>, FunctionResult>> allFunctions = new HashMap<>();

		return allFunctions;
	}
}

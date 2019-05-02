package co.clund.html;

import static co.clund.html.Builder.escapeForHtml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import co.clund.UserSession;
import co.clund.db.DatabaseConnector;
import co.clund.html.block.AbstractBlock;
import co.clund.module.Core;
import co.clund.submodule.core.dbmodel.TBlock;
import co.clund.submodule.core.dbmodel.TBlockRegionRelation;
import co.clund.submodule.core.dbmodel.TConfiguration;
import co.clund.submodule.core.dbmodel.TSiteTemplate;

public class HtmlPage implements Builder {

	private final StringBuilder contentSb = new StringBuilder();

	private final List<String> regions = new ArrayList<>();

	private final String pageTitle;
	private final TSiteTemplate pageTemplate;
	private final DatabaseConnector dbCon;
	private final String pagePath;
	private final UserSession session;

	public HtmlPage(String pageTitle, UserSession session, DatabaseConnector dbCon, String pagePath) {
		this(pageTitle, session, dbCon,
				TConfiguration
						.getConfigValueByModuleAndKey(dbCon.getRootDbCon().getSubmoduleConnector(Core.CORE_LOCATION),
								Core.CORE_LOCATION, "site.template.default")
						.getContent(),
				pagePath);
	}

	public HtmlPage(String pageTitle, UserSession session, DatabaseConnector dbCon, String pageTemplateKey,
			String pagePath) {
		this.pageTitle = pageTitle;
		this.dbCon = dbCon.getRootDbCon().getSubmoduleConnector(Core.CORE_LOCATION);

		this.pageTemplate = TSiteTemplate.getTSiteTemplateByKey(this.dbCon, pageTemplateKey);
		this.pagePath = pagePath;
		this.session = session;

		String rawTemplate = pageTemplate.getContent();
		while (true) {
			int regionPos = rawTemplate.indexOf("$page.region.");

			if (regionPos < 0) {
				break;
			}

			rawTemplate = rawTemplate.substring(regionPos + 13);

			String regionName = rawTemplate.substring(0, rawTemplate.indexOf("$"));

			regions.add(regionName);

			rawTemplate = rawTemplate.substring(1 + regionName.length()); // cut away the ending $
		}

	}

	private void write(String html) {
		contentSb.append(html + "\n");
	}

	@Override
	public void writeWithoutEscaping(String html) {
		write(html);
	}

	@Override
	public String finish() {

		String content = pageTemplate.getContent();

		content = content.replace("$page.title$", pageTitle);

		content = content.replace("$page.meta_tags$", "");
		content = content.replace("$page.style_tags$", "");

		for (String r : regions) {
			List<TBlockRegionRelation> blockList = TBlockRegionRelation
					.getOrderedTBlockRegionRelationsForTemplateRegion(dbCon, pageTemplate.getKey(), r);

			StringBuilder blockContent = new StringBuilder();

			for (TBlockRegionRelation br : blockList) {
				TBlock b = TBlock.getTBlockByKey(dbCon, br.getBlockKey());

				switch (b.getType()) {
				case HTML:
					blockContent.append(b.getContent());
					break;
				case CLASS:
					JSONObject bJData = new JSONObject(b.getContent());
					blockContent.append(
							AbstractBlock.render(dbCon, bJData.getString("type"), pagePath, session, b.getContent()));
					break;
				case NATIVE:
					switch (b.getKey()) {
					case "content":
						blockContent.append(contentSb.toString());
						break;
					default:
						throw new RuntimeException("unknown native block type");
					}
					break;
				default:
					throw new RuntimeException("unknown block type");
				}
			}
			content = content.replace("$page.region." + r + "$", blockContent.toString());
		}

		return content;
	}

	public static String getMessage(Map<String, String[]> parameters) {
		String[] messageParams = parameters.get("message");
		if ((messageParams == null) || (messageParams.length == 0)) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<div id=\"function_return_message\">");
		sb.append(escapeForHtml(messageParams[0]));
		sb.append("</div>");

		return sb.toString();
	}

	public void writeH1(String string) {
		write("<h1>" + escapeForHtml(string) + "</h1>");
	}

	public void writeH2(String string) {
		write("<h2>" + escapeForHtml(string) + "</h2>");
	}

	public void writeH3(String string) {
		write("<h3>" + escapeForHtml(string) + "</h3>");
	}

	public void writeText(String string) {
		write(escapeForHtml(string));
	}

	@Override
	public void write(Builder b) {
		write(b.finish());
	}

	public void writeLink(String location, String text) {
		write("<a href='" + location + "'>" + escapeForHtml(text) + "</a>");
	}

	public void writeLink(URIBuilder location, String text) {
		writeLink(location.toString(), text);
	}

	public void writeHline() {
		write("<hr>");
	}

	public void newLine() {
		write("<br>");
	}

	public void writePre(String string) {
		write("<pre>" + escapeForHtml(string) + "</pre>");
	}

}

package co.clai.video.html;

import static co.clai.video.html.Builder.escapeForHtml;

public class HtmlGenericDiv extends AbstractRenderer {

	public HtmlGenericDiv() {
		super("<div>");
	}

	public HtmlGenericDiv(String cssId) {
		super("<div id='" + cssId + "'>");
	}

	public HtmlGenericDiv(String cssId, String cssClass) {
		super("<div id='" + cssId + "' class='" + cssClass + "'>");
	}

	@Override
	public void write(Builder b) {
		appendData(b.finish());
	}

	@Override
	public String finish() {
		return super.finish() + "</div>";
	}

	public void writePre(String string) {
		appendData("<pre>" + escapeForHtml(string) + "</pre>");
	}

}

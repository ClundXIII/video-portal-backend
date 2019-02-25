package co.clund.video.html;

public class HtmlImg extends AbstractRenderer {

	public HtmlImg(String url) {
		super("<img src='" + url + "' / >");
	}

	public HtmlImg(String url, String cssId) {
		super("<img src='" + url + "' id='" + cssId + "' / >");
	}

	public HtmlImg(String url, String cssId, String cssClass) {
		super("<img src='" + url + "' id='" + cssId + "' class='" + cssClass + "' / >");
	}

	@Override
	public void write(Builder b) {
		throw new RuntimeException("HtmlImg cannot be written on!");
	}

	@Override
	public String finish() {
		return super.finish();
	}
}

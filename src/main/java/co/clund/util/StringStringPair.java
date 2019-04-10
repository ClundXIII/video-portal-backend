package co.clund.util;

public class StringStringPair extends ValueValuePair {

	private final String id;
	private final String name;

	public StringStringPair(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
}

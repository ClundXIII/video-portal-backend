package co.clai.video.module;

import org.apache.http.client.utils.URIBuilder;

public class FunctionResult {

	private final Status status;
	private final String message;
	private final URIBuilder builder;
	private final byte[] data;

	public FunctionResult(byte[] data) {
		status = Status.DATA_RESPONSE;
		message = null;
		builder = null;
		this.data = data;
	}

	public FunctionResult(Status status, String redirect, String message) {
		this.status = status;
		this.message = message;
		try {
			this.builder = new URIBuilder(redirect);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.data = null;
	}

	public FunctionResult(Status status, URIBuilder builder) {
		this.status = status;
		this.message = status.name();
		this.builder = builder;
		this.data = null;
	}

	public FunctionResult(Status status, String redirect) {
		this.status = status;
		this.message = status.name();
		try {
			this.builder = new URIBuilder(redirect);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.data = null;
	}

	public Status getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public URIBuilder getBuilder() {
		return builder;
	}

	public byte[] getData() {
		return data;
	}

	public enum Status {
		DATA_RESPONSE, OK, NOT_FOUND, FAILED, NO_ACCESS, INTERNAL_ERROR, MALFORMED_REQUEST, NONE,
	}
}

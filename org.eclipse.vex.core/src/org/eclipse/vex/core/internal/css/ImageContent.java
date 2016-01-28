package org.eclipse.vex.core.internal.css;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ImageContent implements IPropertyContent {

	public final String baseURI;
	public final List<IPropertyContent> parameters;

	public ImageContent(final String baseURI, final List<IPropertyContent> parameters) {
		this.baseURI = baseURI;
		this.parameters = parameters;
	}

	@Override
	public <T> T accept(final IPropertyContentVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public URL getResolvedImageURL() throws MalformedURLException {
		final String urlSpecification = getParametersAsString();
		final URL baseURL = getBaseURL();
		if (baseURL == null) {
			return new URL(urlSpecification);
		} else {
			return new URL(baseURL, urlSpecification);
		}
	}

	private String getParametersAsString() {
		final StringBuilder string = new StringBuilder();
		for (final IPropertyContent parameter : parameters) {
			string.append(parameter.toString());
		}
		return string.toString();
	}

	private URL getBaseURL() throws MalformedURLException {
		final URL baseURL;
		if (baseURI == null) {
			baseURL = null;
		} else {
			baseURL = new URL(baseURI);
		}
		return baseURL;
	}

	@Override
	public String toString() {
		return "ImageContent [baseURI=" + baseURI + ", urlSpecification=" + getParametersAsString() + "]";
	}
}

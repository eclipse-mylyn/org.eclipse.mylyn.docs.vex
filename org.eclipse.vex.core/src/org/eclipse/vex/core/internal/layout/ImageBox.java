package org.eclipse.vex.core.internal.layout;

import java.net.URL;

import org.eclipse.vex.core.internal.core.Image;
import org.eclipse.vex.core.internal.core.Point;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.INode;

public class ImageBox extends AbstractInlineBox {

	private final Image image;

	public static ImageBox create(final INode node, final LayoutContext context, final int maxWidth) {
		if (node == null) {
			return null;
		}
		final Styles styles = context.getStyleSheet().getStyles(node);
		final URL imageUrl = context.resolveUrl(node.getBaseURI(), styles.getBackgroundImage());
		if (imageUrl == null) {
			return null;
		}

		final Image image = context.getGraphics().getImage(imageUrl);
		final Point imageDimensions = getImageDimensions(image, styles);
		final int width = Math.min(imageDimensions.getX(), maxWidth);
		final int height = scale(imageDimensions.getY(), imageDimensions.getX(), width);

		final ImageBox result = new ImageBox(image);
		result.setWidth(width);
		result.setHeight(height);
		return result;
	}

	private static Point getImageDimensions(final Image image, final Styles styles) {
		final int styleWidth = styles.getElementWidth().get(image.getWidth());
		final int styleHeight = styles.getElementHeight().get(image.getHeight());
		if (styleWidth != 0 && styleHeight != 0) {
			return new Point(styleWidth, styleHeight);
		}
		if (styleWidth == 0 && styleHeight != 0) {
			return new Point(scale(image.getWidth(), image.getHeight(), styleHeight), styleHeight);
		}
		if (styleWidth != 0 && styleHeight == 0) {
			return new Point(styleWidth, scale(image.getHeight(), image.getWidth(), styleWidth));
		}
		return new Point(image.getWidth(), image.getHeight());
	}

	private static int scale(final int opposite, final int current, final int scaled) {
		return Math.round(1f * scaled / current * opposite);
	}

	public static ImageBox createWithHeight(final INode node, final LayoutContext context, final int maxHeight) {
		if (node == null) {
			return null;
		}
		final URL imageUrl = context.resolveUrl(node.getBaseURI(), context.getStyleSheet().getStyles(node).getBackgroundImage());
		if (imageUrl == null) {
			return null;
		}

		final Image image = context.getGraphics().getImage(imageUrl);
		final int height = Math.min(image.getHeight(), maxHeight);
		final int width = scale(image.getWidth(), image.getHeight(), height);

		final ImageBox result = new ImageBox(image);
		result.setWidth(width);
		result.setHeight(height);
		return result;
	}

	public ImageBox(final Image image) {
		this.image = image;
		setWidth(image.getWidth());
		setHeight(image.getHeight());
	}

	@Override
	public void paint(final LayoutContext context, final int x, final int y) {
		if (skipPaint(context, x, y)) {
			return;
		}
		context.getGraphics().drawImage(image, x, y, getWidth(), getHeight());
		super.paint(context, x, y);
	}

	public int getBaseline() {
		return 0;
	}

	@Override
	public void alignOnBaseline(final int baseline) {
		setY(0);
	}

	public boolean isEOL() {
		return false;
	}

}

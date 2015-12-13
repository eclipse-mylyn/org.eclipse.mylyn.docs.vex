package org.eclipse.vex.ui.boxview;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;

public class RefreshHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final BoxDemoView boxView = (BoxDemoView) ((IEvaluationContext) event.getApplicationContext()).getVariable(ISources.ACTIVE_PART_NAME);
		boxView.recreateBoxWidget();
		boxView.setFocus();
		System.gc();

		return null;
	}
}

package org.eclipse.vex.ui.boxview;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;

public class RefreshHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final BoxView boxView = (BoxView) ((IEvaluationContext) event.getApplicationContext()).getVariable(ISources.ACTIVE_PART_NAME);
		boxView.refresh();

		return null;
	}
}

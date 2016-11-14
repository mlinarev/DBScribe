package handlers;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import dbscribe.wizards.DBScribeSetUpWizard;

public class GenerateDocumentationHandler  extends AbstractHandler{

	private IWorkbenchWindow window;

	/**
	 * Constructor
	 */
	public GenerateDocumentationHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			//changescribe.core.handlers.HandlerUtil.openMonitorDialog(new DescribeVersionsDialog(window.getShell(), differences, git, javaProject));
			WizardDialog wizardDialog = new WizardDialog(window.getShell(),
					new DBScribeSetUpWizard());
			wizardDialog.setPageSize(600, 200);
			wizardDialog.setDialogHelpAvailable(false);
			if (wizardDialog.open() == Window.OK) {
				System.out.println("Ok pressed");
			} else {
				System.out.println("Cancel pressed");
			}

		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
		return null;
	}
}

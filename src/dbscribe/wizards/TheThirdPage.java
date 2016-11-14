package dbscribe.wizards;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.swt.widgets.Label;

public class TheThirdPage extends WizardPage {
	private Text textOutputFile;

	public Text getTextOutputFile() {
		return textOutputFile;
	}

	/**
	 * Create the wizard.
	 */
	public TheThirdPage() {
		super("wizardPage");
		setTitle("Select output folder");
		setDescription("Please specify the output folder");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);

		textOutputFile = new Text(container, SWT.BORDER);
		textOutputFile.setBounds(47, 87, 391, 21);

		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		btnNewButton.setBounds(459, 85, 75, 25);
		btnNewButton.setText("Browse...");
		
		Label lblPleaseSpecifyThe = new Label(container, SWT.NONE);
		lblPleaseSpecifyThe.setBounds(48, 57, 208, 15);
		lblPleaseSpecifyThe.setText("The output folder :");
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select output folder");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				textOutputFile.setText(((Path) result[0]).toString());
			}
		}
	}
}

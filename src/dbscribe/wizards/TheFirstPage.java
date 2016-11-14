package dbscribe.wizards;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;

public class TheFirstPage extends WizardPage {
	private Text systemFolder;

	public Text getSystemFolder() {
		return systemFolder;
	}

	/**
	 * Create the wizard.
	 */
	public TheFirstPage() {
		super("wizardPage");
		setTitle("Select system");
		setDescription("Please select source code folder for the system under analysis");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		systemFolder = new Text(container, SWT.BORDER);
		systemFolder.setBounds(58, 89, 395, 21);
		
		Button button = new Button(container, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		button.setBounds(484, 87, 65, 25);
		button.setText("Browse...");
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(58, 56, 340, 15);
		lblNewLabel.setText("Source code folder for the system under analysis :");
	}
	
	
	
	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				systemFolder.setText(((Path) result[0]).toString());
			}
		}
	}
}

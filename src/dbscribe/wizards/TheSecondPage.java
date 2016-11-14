package dbscribe.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;

public class TheSecondPage extends WizardPage {
	private Text textHost;
	private Text textSchema;
	private Text textPass;
	private Text textUID;

	
	public Text getTextHost() {
		return textHost;
	}

	public Text getTextSchema() {
		return textSchema;
	}

	public Text getTextPass() {
		return textPass;
	}

	public Text getTextUID() {
		return textUID;
	}
	
	
	/**
	 * Create the wizard.
	 */
	public TheSecondPage() {
		super("wizardPage");
		setTitle("Database connection");
		setDescription("Please input database connection info");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		textHost = new Text(container, SWT.BORDER);
		textHost.setText("localhost");
		textHost.setBounds(187, 32, 286, 21);
		
		textSchema = new Text(container, SWT.BORDER);
		textSchema.setBounds(187, 70, 286, 21);
		
		textPass = new Text(container, SWT.PASSWORD | SWT.BORDER);
		textPass.setBounds(187, 147, 286, 21);
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setBounds(61, 112, 101, 15);
		lblNewLabel.setText("Database user ID :");
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setBounds(61, 150, 66, 15);
		lblNewLabel_1.setText("Password :");
		
		Label lblNewLabel_2 = new Label(container, SWT.NONE);
		lblNewLabel_2.setBounds(61, 35, 98, 15);
		lblNewLabel_2.setText("Database host :");
		
		textUID = new Text(container, SWT.BORDER);
		textUID.setText("root");
		textUID.setBounds(187, 109, 286, 21);
		
		Label lblDatabaseSchema = new Label(container, SWT.NONE);
		lblDatabaseSchema.setBounds(61, 73, 101, 15);
		lblDatabaseSchema.setText("Database schema :");
	}

}

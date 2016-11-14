package dbscribe.wizards;

import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import edu.semeru.wm.qextractor.processors.DBScribe;

public class FirstPage extends WizardPage {

	/**
	 * Create the wizard.
	 */
	public FirstPage() {
		super("wizardPage");
		setTitle("Wizard Page title");
		setDescription("Wizard Page description");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);

		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				//ASTParser parser = ASTParser.newParser(AST.JLS4);
//				//Change output folder before running any test cases
//				String host = "localhost";
//				String schema = "university";
//				String user = "root";
//				String passwd = "boyang";
//				String systemFolder = "C:\\Users\\USBOLI\\Desktop\\Research\\DBScribe\\DBScribeTool\\UMAS\\src";
//				String outputFile = "C:\\Users\\USBOLI\\Desktop\\DBScribeOutputs\\UMAS.html";
//				try {
//					DBScribe.runDBScribe(systemFolder, outputFile, host, schema, user, passwd);
//				} catch (ClassNotFoundException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (InstantiationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (IllegalAccessException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (SQLException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				System.out.println("DONE");
			}
		});
		btnNewButton.setBounds(69, 56, 75, 25);
		btnNewButton.setText("New Button111");
	}

}

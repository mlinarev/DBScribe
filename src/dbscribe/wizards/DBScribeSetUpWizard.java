package dbscribe.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import java.awt.Desktop;
import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

import edu.semeru.wm.qextractor.processors.DBScribe;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class DBScribeSetUpWizard extends Wizard implements INewWizard {
	private TheFirstPage page1;
	private TheSecondPage page2;
	private TheThirdPage page3;

	//private SampleNewWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public DBScribeSetUpWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page1 = new TheFirstPage();
		addPage(page1);
		page2 = new TheSecondPage();
		addPage(page2);
		page3 = new TheThirdPage();
		addPage(page3);
		//addPage(new FirstPage());
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String pathToWorkSpace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		final String host = page2.getTextHost().getText();
		final String schema = page2.getTextSchema().getText();
		final String user = page2.getTextUID().getText();
		final String passwd = page2.getTextPass().getText();

		final String splitter = System.getProperty("file.separator");
		final String systemFolder = (pathToWorkSpace + page1.getSystemFolder().getText()).replace("/", splitter);			
		final String outputFile = (pathToWorkSpace + page3.getTextOutputFile().getText()  + "/DBScribeReport.html").replace("/", splitter);
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(host, schema, user, passwd, systemFolder, outputFile,  monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(String host, String schema, String user, String passwd, String systemFolder, String outputFile, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Generating documentation ", 8);

		monitor.worked(1);
		System.out.println("START");
		try {
			DBScribe.runDBScribe(systemFolder, outputFile, host, schema, user, passwd, monitor);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("DONE");
		monitor.worked(1);
		monitor.done();
		File htmlFile = new File(outputFile);
		if(htmlFile.exists()){
			try {
				Desktop.getDesktop().browse(htmlFile.toURI());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}



	//	private void throwCoreException(String message) throws CoreException {
	//		IStatus status =
	//				new Status(IStatus.ERROR, "DBScribe", IStatus.OK, message, null);
	//		throw new CoreException(status);
	//	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
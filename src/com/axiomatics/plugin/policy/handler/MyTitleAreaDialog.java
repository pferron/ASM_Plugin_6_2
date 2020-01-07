package com.axiomatics.plugin.policy.handler;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MyTitleAreaDialog extends TitleAreaDialog {

    private Text txtDomainName;
    private Text txtDescription;

    private String domainName;
    private String description;

    public MyTitleAreaDialog(Shell parentShell) {
        super(parentShell);
    }

    public void create(String title, String message) {
        super.create();
        setTitle(title);
        setMessage(message , IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createDomainName(container);
        createDescription(container);

        return area;
    }

    private void createDomainName(Composite container) {
        Label lbtDomainName = new Label(container, SWT.NONE);
        lbtDomainName.setText("Domain Name");

        GridData dataDomainName = new GridData();
        dataDomainName.grabExcessHorizontalSpace = true;
        dataDomainName.horizontalAlignment = GridData.FILL;

        txtDomainName = new Text(container, SWT.BORDER);
        txtDomainName.setLayoutData(dataDomainName);
    }

    private void createDescription(Composite container) {
        Label lbtDescription = new Label(container, SWT.NONE);
        lbtDescription.setText("Description (optional)");

        GridData dataDescription = new GridData();
        dataDescription.grabExcessHorizontalSpace = true;
        dataDescription.horizontalAlignment = GridData.FILL;
        txtDescription = new Text(container, SWT.BORDER);
        txtDescription.setLayoutData(dataDescription);
    }



    @Override
    protected boolean isResizable() {
        return true;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private void saveInput() {
    	domainName = txtDomainName.getText();
        description = txtDescription.getText();

    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    public String getDomainName() {
        return domainName;
    }

    public String getDescription() {
        return description;
    }
}
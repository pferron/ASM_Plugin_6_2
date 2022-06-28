package com.axiomatics.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Packager {
    private File mainPolicy;
    private File policyFolder;
    
    public Packager(File mainPolicy, File policyFolder) {
        this.mainPolicy = mainPolicy;
        this.policyFolder = policyFolder;
    }

    public File producePackage(String destination) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destination));
        
        File[] files = policyFolder.listFiles();
        
        for (File f : files){
            // If the file is not a folder and it is not the main policy
            if (f.isFile() && f.getAbsolutePath().equals(mainPolicy.getAbsolutePath())==false){
            	//TODO: Check if the file has .xml extension
                addZipEntry("referenceable/"+f.getName(), f, zip);
            }
        }
        // Add the main policy now
        addZipEntry("root-policy.xml", mainPolicy, zip);        
   
        zip.close();
        
        return new File(destination);
    }

    private void addZipEntry(String entryName, File f, ZipOutputStream zip) throws IOException {
        ZipEntry zentry = new ZipEntry(entryName);
        zip.putNextEntry(zentry);
        FileInputStream in = new FileInputStream(f);
        byte[] b = new byte[1024];
        int count;
        while ((count = in.read(b)) > 0) {
            System.out.println();
            zip.write(b, 0, count);
        }
        in.close();
    }
}

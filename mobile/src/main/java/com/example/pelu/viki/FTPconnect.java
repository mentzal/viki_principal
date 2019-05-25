package com.example.pelu.viki;

/**
 * Created by Pelu on 14/10/2017.
 */

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FTPconnect {


    private static final String TAG = "FTPConnect";
    public FTPClient mFTPClientConnect = null;

    public boolean ftpConnect(String host, String username, String password, int port) {

        try {
            mFTPClientConnect = new FTPClient();
            // connecting to the host
            mFTPClientConnect.connect(host, port);
            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClientConnect.getReplyCode())) {
            // login using username &amp;amp; password
                boolean status =mFTPClientConnect.login(username, password);
                mFTPClientConnect.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClientConnect.enterLocalPassiveMode();
                return status;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: could not connect to host " + host);
        }
        return false;
    }

    public boolean ftpDisconnect() {
        try {
            mFTPClientConnect.logout();
            mFTPClientConnect.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
        }
        return false;
    }


    public String ftpGetCurrentWorkingDirectory() {
        try {
            String workingDir = mFTPClientConnect.printWorkingDirectory();
            return workingDir;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not get current working directory.");
        }
        return null;
    }

    public boolean ftpChangeDirectory(String directory_path) {
        try {
            mFTPClientConnect.changeWorkingDirectory("/gastosExcel");
        } catch (Exception e) {
            System.out.println("error al cambiar el directorio" + directory_path);
        }
        return false;
    }


    public boolean ftpUpload(String srcFilePath, String desFileName,
                             String desDirectory, Context context) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            // change working directory to the destination directory

                status = mFTPClientConnect.storeFile(desFileName, srcFileStream);


            srcFileStream.close();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error en la subida");
        }
        return status;
    }

    public boolean ftpMakeDirectory(String new_dir_path) {
        try {
            boolean status = mFTPClientConnect.makeDirectory(new_dir_path);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Error: could not create new directory named "
                    + new_dir_path);
        }
        return false;
    }


    public boolean ftpRenameFile(String from, String to) {
        try {
            boolean status = mFTPClientConnect.rename(from, to);
            return status;
        } catch (Exception e) {
            Log.d(TAG, "Could not rename file: " + from + " to: " + to);
        }
        return false;
    }

    public boolean ftpDownload(String srcFilePath, String desFilePath) {
        boolean status = false;
        try {
            FileOutputStream desFileStream = new FileOutputStream(desFilePath);
            ;
            status = mFTPClientConnect.retrieveFile(srcFilePath, desFileStream);
            desFileStream.close();
            return status;
        } catch (Exception e) {
            Log.d(TAG, "download failed");
        }
        return status;
    }


}

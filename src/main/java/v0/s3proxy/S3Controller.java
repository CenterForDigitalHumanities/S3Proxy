/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package v0.s3proxy;

import conn.TinyManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.servlet.http.Part;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.CompletedDownload;
import software.amazon.awssdk.transfer.s3.CompletedUpload;
import software.amazon.awssdk.transfer.s3.Download;
import software.amazon.awssdk.transfer.s3.S3ClientConfiguration;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.Upload;
import software.amazon.awssdk.transfer.s3.UploadRequest;

/**
 *
 * @author bhaberbe
 */
public class S3Controller {
    private String s3_access_id = "";
    private String s3_secret = "";
    private String bucket_name = "";
    private Region region;
    public S3TransferManager transferManager;
    public S3Client s3;
    /**
     * Initializer for a TinyTokenManager that reads in the properties File
     * @throws IOException if no properties file
     */
    public S3Controller() throws IOException {
        System.out.println("initializing S3...");
        init();
    }
    
    private void init() throws FileNotFoundException, IOException{
        /*
            Your properties file must be in the deployed .war file in WEB-INF/classes/tokens.  It is there automatically
            if you have it in Source Packages/java/tokens when you build.  That is how this will read it in without defining a root location
            https://stackoverflow.com/questions/2395737/java-relative-path-of-a-file-in-a-java-web-application
        */
        TinyManager manager = new TinyManager();
        s3_access_id = manager.getS3AccessID();
        s3_secret = manager.getS3Secret();
        bucket_name = manager.getS3BucketName();
        region = manager.getS3Region();
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(s3_access_id,s3_secret);
        
        System.out.println("Build S3 Transfer Manager...");
        //How the TransferManager docs said to build this out for it when we have to provide the connection info
        S3ClientConfiguration s3Config =
            S3ClientConfiguration.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();
        transferManager = S3TransferManager.builder().s3ClientConfiguration(s3Config).build(); 
        
        System.out.println("Build S3 Client...");
        //How the General AWS docs said to make the client.
        s3 = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .region(region)
            .build();
    }
    
    public CompletedDownload downloadFile(String filename, String saveAs){
        Download download = transferManager.download(b -> b.getObjectRequest(r -> r.bucket(bucket_name).key(filename)).destination(Paths.get(saveAs)));
        CompletedDownload completedDownload = download.completionFuture().join();
        return completedDownload;
    }
    
    /**
     * This is more for like when you have the file on the server this servlet is running on and want to upload it
     * @param filename
     * @param uploadAs
     * @return 
     */
    public CompletedUpload uploadFile(String filename, String uploadAs){
        Upload upload = transferManager.upload(b -> b.putObjectRequest(r -> r.bucket(bucket_name).key(uploadAs)).source(Paths.get(uploadAs)));
        CompletedUpload completedUpload = upload.completionFuture().join();
        return completedUpload;
    }
    
    /**
     * You had a file in a File object and just want to upload that file!
     * @param file
     * @return 
     */
    public CompletedUpload uploadFile(File file){
        System.out.println("Sent File to upload");
        System.out.println(file);
        final String name = file.getName();
        PutObjectRequest pr = PutObjectRequest.builder()
                .bucket(bucket_name)
                .key(name)
                .build();
        UploadRequest ur = UploadRequest.builder().putObjectRequest(pr).source(Paths.get(file.getPath())).build();
        Upload upload = transferManager.upload(ur);
        
        //Upload upload = transferManager.upload(b -> b.putObjectRequest(r -> r.bucket(bucket_name).key(name)).source(Paths.get(name)));
        CompletedUpload completedUpload = upload.completionFuture().join();
        return completedUpload;
    }
    
    /**
     * You had a file in a Path object and just want to upload that file!
     * @param file
     * @return 
     */
    public CompletedUpload uploadFile(Path file){
        System.out.println("Sent Path to upload");
        System.out.println(file);
        PutObjectRequest pr = PutObjectRequest.builder()
                .bucket(bucket_name)
                .key(file.getFileName().toString())
                .build();
        UploadRequest ur = UploadRequest.builder().putObjectRequest(pr).source(file).build();
        Upload upload = transferManager.upload(ur);
        
        //Upload upload = transferManager.upload(b -> b.putObjectRequest(r -> r.bucket(bucket_name).key(name)).source(Paths.get(name)));
        CompletedUpload completedUpload = upload.completionFuture().join();
        return completedUpload;
    }
    
    /**
     * You had a multipart file from a servlet request and passed it in.
     * @param file
     * @return 
     */
    public CompletedUpload uploadFile(Part file){
        System.out.println("Sent Part to upload");
        System.out.println(file);
        String fileName = Paths.get(file.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
        PutObjectRequest pr = PutObjectRequest.builder()
                .bucket(bucket_name)
                .key(fileName)
                .build();
        UploadRequest ur = UploadRequest.builder().putObjectRequest(pr).source(Paths.get(fileName)).build();
        Upload upload = transferManager.upload(ur);
        
        //Upload upload = transferManager.upload(b -> b.putObjectRequest(r -> r.bucket(bucket_name).key(name)).source(Paths.get(name)));
        CompletedUpload completedUpload = upload.completionFuture().join();
        return completedUpload;
    }
    
    public ArrayList<String> listBucketFiles(){
       ArrayList<String> filenames = new ArrayList<>();
       try {
           System.out.println("Build out list of objects in bucket for bucket_name= "+bucket_name);
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucket_name)
                    .build();
            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();
            for (ListIterator iterVals = objects.listIterator(); iterVals.hasNext(); ) {
                S3Object myValue = (S3Object) iterVals.next();
                filenames.add(myValue.key() +"  "+calKb(myValue.size()) + " KBs");
            }
        } 
        catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            filenames.clear();
        }
        return filenames;
    }
    
    //convert bytes to kbs
    private static long calKb(Long val) {
        return val/1024;
    }
}

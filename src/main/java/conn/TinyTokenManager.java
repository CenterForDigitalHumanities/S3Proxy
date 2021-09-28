
package conn;

import v0.s3proxy.Constant;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import software.amazon.awssdk.regions.Region;

/**
 * @author bhaberbe
 * This is the token manager for this application.  It handles all token interactions.
 */
public class TinyTokenManager{
    //Notice that when it is initialized, nothing is set.
    private String currentS3AccessID = "";
    private String currentS3BucketName = "";
    private String currentS3Secret = "";
    private Region currentS3Region;
    private String propFileLocation = Constant.PROPERTIES_FILE_NAME; //This package.
    private String apiSetting = "";
    private Properties props = new Properties();
    
    /**
     * Initializer for a TinyTokenManager that reads in the properties File
     * @throws IOException if no properties file
     */
    public TinyTokenManager() throws IOException {
        init();
    }
    
    /**
     * After initializing, read in the properties you have and set the class values.
     * @return A Properties object containing the properties from the file.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public final Properties init() throws FileNotFoundException, IOException{
        /*
            Your properties file must be in the deployed .war file in WEB-INF/classes/tokens.  It is there automatically
            if you have it in Source Packages/java/tokens when you build.  That is how this will read it in without defining a root location
            https://stackoverflow.com/questions/2395737/java-relative-path-of-a-file-in-a-java-web-application
        */
        String fileLoc =TinyTokenManager.class.getResource(Constant.PROPERTIES_FILE_NAME).toString();
        fileLoc = fileLoc.replace("file:", "");
        setFileLocation(fileLoc);
        InputStream input = new FileInputStream(propFileLocation);
        props.load(input);
        input.close();
        currentS3AccessID = props.getProperty("s3_access_id");
        currentS3Secret = props.getProperty("s3_secret");
        currentS3BucketName = props.getProperty("s3_bucket_name");
        currentS3Region = Constant.S3_BUCKET_REGION;
        apiSetting = props.getProperty("open_api_cors");
        return props;
    }
    
    /**
     * 
     * @param prop The property to write or overwrite
     * @param propValue The value of the property
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void writeProperty (String prop, String propValue) throws FileNotFoundException, IOException{
        OutputStream output = null;
        output = new FileOutputStream(propFileLocation);
        // set the properties value
        props.setProperty(prop, propValue);
        // save properties to propFileLocation
        props.store(output, null);
        output.close();
    }
    
    public void setFileLocation(String location){
        propFileLocation = location;
    }
    
    public String getAPISetting(){
        //ensure invalid strings result to false
        if(!apiSetting.equals("true")){
            apiSetting = "false";
        }
        return apiSetting;
    }
    
    public String getFileLocation(){
        return propFileLocation;
    }
    
    public String getS3Secret(){
        return currentS3Secret;
    }
    
    public String getS3AccessID(){
        return currentS3AccessID;
    }
    
    public String getS3BucketName(){
        return currentS3BucketName;
    }
    
    public Region getS3Region(){
        return currentS3Region;
    }
    
}

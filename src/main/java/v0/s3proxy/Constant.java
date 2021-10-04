/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package v0.s3proxy;

import software.amazon.awssdk.regions.Region;

/**
 *
 * @author bhaberbe
 */
public class Constant {
    public static String PROPERTIES_FILE_NAME = "s3.properties";
    public static Region S3_BUCKET_REGION = Region.US_EAST_1;
    public static String S3_URI_PREFIX = "https://rerum-server-files.s3.us-east-1.amazonaws.com/";
}

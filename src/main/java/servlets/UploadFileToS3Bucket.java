/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.transfer.s3.CompletedUpload;
import software.amazon.awssdk.transfer.s3.Upload;
import v0.s3proxy.S3Controller;

/**
 *
 * @author bhaberbe
 */
@WebServlet(name = "UploadFileToS3Bucket", urlPatterns = {"/uploadFile"})
@MultipartConfig
public class UploadFileToS3Bucket extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        S3Controller bucket = new S3Controller();
        System.out.println("S3 UploadFileToS3Bucket.java");
        
        Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
        System.out.println("Got file part...");
        System.out.println(filePart);
        
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        InputStream fileContent = filePart.getInputStream();
        System.out.println();
        System.out.println("Got file name and contents...");
        System.out.println(fileName);
        
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(fileContent));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }
        System.out.println();
        System.out.println("File contents...may or may not be readable.  txt files are best to check this part with.");
        System.out.println(sb.toString());
        
        File tempFile = new File(fileName);
        System.out.println();
        System.out.println("Made temporary File object...");
        FileUtils.copyInputStreamToFile(fileContent, tempFile);
        System.out.println("Populated temp file with contents...");
        System.out.println(tempFile);
        
        Path tempPath = Paths.get(tempFile.getAbsolutePath());
        System.out.println();
        System.out.println("Made temporary Path object...");
        System.out.println(tempPath);
        
        System.out.println();
        System.out.println("Need to pick a way to invoke bucket.uploadFile...");
        
        //CompletedUpload up = bucket.uploadFile(filepath,saveFileAs);
        System.out.println("Choosing to send file part now...");
        CompletedUpload up = bucket.uploadFile(filePart);
        //CompletedUpload up = bucket.uploadFile(tempPath);
        //CompletedUpload up = bucket.uploadFile(tempFile);
        System.out.println("Got completed upload back!  See Etag below, will exist if call was successful.");
        System.out.println(up.response().eTag());
        
        //Note the stuff above is not optimized.  This leaves behind temp files in /CLASSPATHROOT/, they need to be removed.  We might not need them at all.
        tempFile.delete();        
        //Not sure what content type this should be yet...we are sending a PutObjectResponse back
        response.setHeader("Content-Type", "text/plain; charset=utf-8");
        response.getWriter().print(up.response());
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        multipart.transferTo(convFile);
        return convFile;
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import v0.s3proxy.Constant;
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
        try{
            S3Controller bucket = new S3Controller();
            System.out.println("S3 UploadFileToS3Bucket.java");

            Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
            String fileType = filePart.getContentType();
            String tmpdir = System.getProperty("java.io.tmpdir") + FileSystems.getDefault().getSeparator();
            String fileName = tmpdir + Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            
            System.out.println("Got file name...");
            System.out.println(fileName);
            
            File tempFile = new File(fileName);
            InputStream fis = filePart.getInputStream(); 
            FileUtils.copyInputStreamToFile(fis, tempFile);
            CompletedUpload up = bucket.uploadFile(tempFile, fileType);

            System.out.println("Got completed upload back!  See Etag below, will exist if call was successful.");
            System.out.println(up.response().eTag());

            //Note the stuff above is not optimized.  This leaves behind temp files in 'tmpdir', they need to be removed.  We might not to generate temp files at all.
            tempFile.delete();     

            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Expose-Headers", "*");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setHeader("Location", Constant.S3_URI_PREFIX + fileName);
            response.getWriter().print(up.response());
        }
        catch(Exception e){
            System.out.println("The caught error is...");
            System.out.println(e);
            throw e;
        }
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
     * Handles the HTTP <code>OPTIONS</code> preflight method.
     * This should be a configurable option.  Turning this on means you
     * intend for this version of Tiny Things to work like an open API.  
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //These headers must be present to pass browser preflight for CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setStatus(200);
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

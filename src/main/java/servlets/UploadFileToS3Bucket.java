/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlets;

import java.io.File;
import java.io.IOException;
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

@WebServlet(name = "UploadFileToS3Bucket", urlPatterns = {"/uploadFile"})
@MultipartConfig
public class UploadFileToS3Bucket extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        try {
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Request Protocol: " + request.getProtocol());
            System.out.println("Remote Address: " + request.getRemoteAddr());

            S3Controller bucket = new S3Controller();
            System.out.println("S3 UploadFileToS3Bucket.java");
            Part filePart = request.getPart("file");
            System.out.println(Paths.get(filePart.getSubmittedFileName()));
            System.out.println(Paths.get(filePart.getSubmittedFileName()).getFileName());
            System.out.println("Walrus: ");
            
            // This is where we are broken
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String fileType = filePart.getContentType();
            System.out.println("Got file name: " + fileName);

            File tempFile = new File(fileName);
            System.out.println("Created temporary File object...");

            String tmpdir = System.getProperty("java.io.tmpdir");
            System.out.println("Temp file path: " + tmpdir);

            FileUtils.copyInputStreamToFile(filePart.getInputStream(), tempFile);
            System.out.println("Populated temp file with contents...");

            CompletedUpload up = bucket.uploadFile(tempFile, fileType);
            System.out.println("Got completed upload back! Etag: " + up.response().eTag());

            tempFile.delete();

            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Expose-Headers", "*");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setHeader("Location", Constant.S3_URI_PREFIX + fileName);
            response.getWriter().print(up.response());

            System.out.println("Response Status: " + response.getStatus());
            System.out.println("Response Headers: " + response.getHeaderNames());
        } catch(Exception e) {
            System.err.println("Error during file upload:");
            e.printStackTrace();
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

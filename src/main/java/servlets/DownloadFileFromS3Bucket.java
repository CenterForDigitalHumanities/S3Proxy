/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import v0.s3proxy.S3Controller;
import software.amazon.awssdk.transfer.s3.CompletedDownload;

/**
 *
 * @author bhaberbe
 */
@WebServlet(name = "DownloadFileToS3Bucket", urlPatterns = {"/downloadFile"})
public class DownloadFileFromS3Bucket extends HttpServlet {

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
        String filename = request.getParameter("key");
        //String localFilesystemDestination = System.getProperty("user.home")+"/Downloads/"+filename;
        //String localFilesystemDestination = request.getParameter("destination");;
        System.out.println("Try to bucket.downloadFile()...");
        CompletedDownload down = bucket.downloadFile(filename);
        System.out.println("We have a completed download respose!  Send file back to user.");
        String ctype = down.response().contentType();
        File tempFile = new File(filename);
        System.out.println("Accessing temp file");
        System.out.println(tempFile.getAbsolutePath());
        response.setHeader("Content-disposition", "attachment; filename="+filename);
        response.setHeader("Content-Type", ctype+"; charset=utf-8");
        /**
         * bucket.downloadFile() has made a temporary file at the classpath root, which we turn into a File object for use as out resource.
         * Using HttpServletResponse#getOutputStream(), we then read from the input stream of the resource and write to the response's OutputStream.
         * The size of the byte array we use is arbitrary. We can decide the size based on the amount of memory is reasonable to allocate for passing the data from the InputStream to the OutputStream; the smaller the number, the more loops; the bigger the number, the higher memory usage.
         * his cycle continues until numByteRead is 0 as that indicates the end of the file.
         */
        System.out.println("Take input stream of file and write to response output stream");
        try(InputStream in = new FileInputStream(tempFile);
          OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int numBytesRead;
            while ((numBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numBytesRead);
            }
            System.out.println("File has been written to output stream!");
            tempFile.delete();
        }
        //response.getWriter().print(down.response());
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /**
     * Handles the HTTP <code>POST</code> method.  We may to do this to pass information in the body.
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

}

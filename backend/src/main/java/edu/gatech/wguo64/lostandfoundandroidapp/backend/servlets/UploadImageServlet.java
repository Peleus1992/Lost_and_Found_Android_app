package edu.gatech.wguo64.lostandfoundandroidapp.backend.servlets;

/**
 * Created by guoweidong on 2/18/16.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import org.json.simple.JSONObject;

public class UploadImageServlet extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private ImagesService imagesService = ImagesServiceFactory.getImagesService();
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
        List<BlobKey> blobKeys = blobs.get("image");
//        System.out.println("image : " + blobKeys);
        if (blobKeys != null && !blobKeys.isEmpty()) {
            BlobKey blobKey = blobKeys.get(0);
            ServingUrlOptions servingUrlOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);

            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");

            //JSONObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("imageKey", blobKey.getKeyString());
            jsonObject.put("imageURL", imagesService.getServingUrl(servingUrlOptions));
            //Output
            PrintWriter out = res.getWriter();
            out.print(jsonObject.toJSONString());
            out.flush();
            out.close();
        }
    }
}

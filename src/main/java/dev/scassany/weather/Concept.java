package dev.scassany.weather;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class Concept {


    public static void main(String[] args){
        // The ID of your GCP project
        String projectId = "weather-data-435406";

        // The ID of your GCS bucket
        String bucketName = "gcp-public-data-nexrad-l3-realtime";
        String site = "ILX";
        String product = "N1B";
        String[] products = {"N0B","N1B", "N2B", "N3B", "NAB","NBB"};



        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        for(String prod : products) {
            Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix("NIDS/" + site + "/" + product));

            int highest = -1;
            Blob highestBlob = null;
            String dateTime = "20240918";

            for (Blob blob : blobs.iterateAll()) {
                String blobName = blob.getName();
                String[] blobSplit = blobName.split("_");
                //System.out.println(blobSplit);
                if (blobSplit[1].equals(dateTime)) {
                    if (Integer.parseInt(blobSplit[2]) > highest) {
                        highest = Integer.parseInt(blobSplit[2]);
                        highestBlob = blob;
                    }
                }
                System.out.println(blob.getName());
            }
            if (highestBlob == null) {
                System.out.println("huh");
            } else {
                System.out.println("Highest Blob was: " + highestBlob.getName());
                download(highestBlob, prod, dateTime, highest);
            }
        }

    }

    public static void download(Blob blob, String prod, String date, int time){
        String destFilePath = "C:\\Users\\tneub\\Desktop\\debug\\"+prod+"_"+date+"_"+time;
        System.out.println(blob.getName());
        File f = new File(destFilePath);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        blob.downloadTo(Paths.get(destFilePath));
    }


}

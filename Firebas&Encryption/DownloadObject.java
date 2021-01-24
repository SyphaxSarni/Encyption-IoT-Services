import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class DownloadObject {
    public static void downloadObject(String projectId, String bucketName, String objectName, String filePath) throws IOException {
        // The ID of your GCP project
        // String projectId = "your-project-id";

        // The ID of your GCS bucket
        // String bucketName = "your-unique-bucket-name";

        // The ID of your GCS object
        // String objectName = "your-object-name";

        // The path to your file to upload
        // String filePath = "path/to/your/file"
        FileInputStream serviceAccount = new FileInputStream("C:/Users/Computer/Desktop/PFE L3/Private Key/crypto-serv-firebase-adminsdk.json");
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(GoogleCredentials.fromStream(serviceAccount)).build().getService();
        Blob blob = storage.get(BlobId.of(bucketName,objectName));
        blob.downloadTo(Paths.get(filePath));

        System.out.println("[*] File " + filePath + " downloaded from bucket " + bucketName + " as " + objectName +"\n\n");
    }
}

import com.bapcb.remote.BAPCBConnector;
import com.bapcb.remote.EventListenerInterface;
import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BAPCB implements EventListenerInterface {

    // BAPCBConnector API connector
    BAPCBConnector remote = BAPCBConnector.getInstance();

    public BAPCB() {

    }

    public void testFile() throws Exception {
        try {
            String fileName = "nfistest.csv"; // Change this to your CSV file's name
            byte[] csvBytes = readCsvFileToByteArray(fileName);
            System.out.println(csvBytes);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    public void csvUploadBAP() throws Exception {
        try {

        } catch (Exception e) {

        }
    }

    // Read local CSV file
    private static byte[] readCsvFileToByteArray(String fileName) {
        File csvFile = new File(fileName);

        try (FileInputStream fis = new FileInputStream(csvFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void runBapcb() throws Exception {
        try {

            // API Credentials
            String username = "ewb.api01";
            String password = "EastW123!";
            Date start = new Date();
            remote.setEventListener(this);

            // Attempt to connect. If connected:
            System.out.println("Trying to connect: " + start + "\n");

            System.out.println("Successful connection: " + remote.connect(username,
                    password, "api8"));
            Date now = new Date();
            String referenceid = remote.generateReferenceID();
            System.out.println("LIST RESULT:\n " + remote.list(referenceid, "report"));
            System.out.println("Duration:" + (now.getTime() - start.getTime()) + "msec.");
            System.out.println("LIST DONE End:" + now);

            // String fileName = "NFISTest.csv"; // Change this to your CSV file's name
            // byte[] csvBytes = readCsvFileToByteArray(fileName);

            // String xmlFromBAP = remote.upload(null, "nfistest.csv", csvBytes,
            // "INDIVIDUAL");
            // System.out.println("UPLOAD RESULT:\n" + xmlFromBAP);

            // System.out.println("Duration:" + (now.getTime() - start.getTime()) +
            // "msec.");
            // System.out.println("===UPLOAD DONE=== End:" + now);

            String downloadFile = "nfistest.csv-I-20230804164457j58s-(ewb.api01).csv.dbf.xml";
            System.out.println("===DOWNLOAD== (" + downloadFile.getBytes().length + "bytes.)Start:" + start);
            System.out.println(
                    "Download successful: " + new String(remote.download(null, downloadFile,
                            "REPORT", "CSV")));

            System.out.println("Disconnecting now: " + remote.disconnect());
        } catch (Exception e) {
            System.out.println("Error (Stacktrace)");
            e.printStackTrace();
        } finally {
            System.out.println("Disconnecting " + remote.disconnect());
        }
    }

    public static void main(String[] args) throws Exception {
        BAPCB bpObj = new BAPCB();
        bpObj.runBapcb();
        // bpObj.testFile();
    }

    @Override
    public void event(String referenceid, Date eventtimestamp, String type, String remarks) throws Exception {
        System.out.println(
                eventtimestamp + " DEBUG: [" + type + "] REFID: [" + referenceid + "] remarks: [" + remarks + "].");
    }

}
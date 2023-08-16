import com.bapcb.remote.BAPCBConnector;
import com.bapcb.remote.EventListenerInterface;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;

public class NFISFileSystem implements EventListenerInterface {

    // Constructor
    public NFISFileSystem() {

    }

    // Global API connection (BACPB)
    BAPCBConnector remote = BAPCBConnector.getInstance();

    // Credential storage
    private class NFISCredentials {
        private String username;
        private String password;

        public NFISCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    public Boolean nfisConnection() throws Exception {
        NFISCredentials credentials = new NFISCredentials("ewb.api01", "EastW123!");
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        Date startConnection = new Date();

        remote.setEventListener(this);

        // Start connection
        System.out.println("Connecting to BAPCB UAT Environment: " + startConnection);
        Boolean connection;
        try {
            connection = remote.connect(username, password, "api8");
            return true;
        } catch (Exception e) {
            System.out.println("runNfis block error stacktrace: ");
            e.printStackTrace();
            return false;
        }

    }

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
            System.out.println("CSV to Byte error: ");
            e.printStackTrace();
            return null;
        }
    }

    public void checkFilesInRepository() throws Exception {
        try {
            String referenceid = remote.generateReferenceID();
            System.out.println("Files in repository: \n " + remote.list(referenceid, "report"));
        } catch (Exception e) {
            System.out.println("List method error: ");
            e.printStackTrace();
        }
    }

    // code to get NAME FROM XML

    public void runNfis() throws Exception {
        try {
            // Call to remote.connect API
            Boolean isConnected = nfisConnection();
            String connectionStatus = isConnected ? "Connected" : "Not connected";
            System.out.println("Connection status: " + connectionStatus);

            if (isConnected) {
                // Locate all the files LOCALLY that are CSV (should be in the same folder)
                File folder = new File("C:\\Users\\KOV78896\\Desktop\\api");
                File[] files = folder.listFiles();
                List<String> fileNames = new ArrayList<>();
                List<String> xmlFromBAP = new ArrayList<>();
                List<String> bapFileName = new ArrayList<>();
                List<String> bapFileContents = new ArrayList<>();

                long delay = 10 * 60 * 1000;

                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                            fileNames.add(file.getName());
                        }
                    }
                }

                // Convert located files into byte[] to be used for remote.upload
                for (String fileName : fileNames) {
                    byte[] csvBytes = readCsvFileToByteArray(fileName);
                    xmlFromBAP.add(remote.upload(null, fileName, csvBytes, "INDIVIDUAL"));
                }

                // Check the response if uploaded or not via returned XML
                for (int i = 0; i < fileNames.size(); i++) {
                    System.out.println("File name: " + fileNames.get(i));
                    System.out.println("File contents: " + xmlFromBAP.get(i));

                    String xmlResponse = xmlFromBAP.get(i);
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        InputStream stream = new ByteArrayInputStream(xmlResponse.getBytes());

                        Document doc = builder.parse(stream);
                        NodeList fileList = doc.getElementsByTagName("FILE");

                        if (fileList.getLength() > 0) {
                            Element fileElement = (Element) fileList.item(0);
                            String name = fileElement.getAttribute("NAME") + ".dbf.xml";
                            System.out.println("File name from XML: " + name);
                            bapFileName.add(name);

                        } else {
                            System.out.println("No FILE element found in XML response.");
                        }
                    } catch (Exception e) {
                        System.out.println("XML parsing error:");
                        e.printStackTrace();
                    }
                }
                // Check bapFileName contents
                System.out.println("Checking files that were uploaded: ");
                for (int i = 0; i < bapFileName.size(); i++) {
                    System.out.println(bapFileName.get(i));
                }

                // 15 minute allowance to allow BAP to process the files
                System.out.println("15 minute break");
                Thread.sleep(delay);

                // Check File list in BAP repository
                checkFilesInRepository();
                System.out.println("2 minute break");
                Thread.sleep(2 * 60 * 1000);
                // Download the contents of the file
                System.out.println("Download start. ");
                for (int i = 0; i < bapFileName.size(); i++) {
                    try {

                        System.out.println("File being downloaded: " + bapFileName.get(i));
                        byte[] bapFileContent = remote.download(null, bapFileName.get(i), "REPORT", "CSV");
                        System.out.println("CSV File contents: " + bapFileContent);
                        bapFileContents.add(new String(bapFileContent));
                    } catch (Exception e) {
                        System.out.println("Downloading from BAPCB error:");
                        e.printStackTrace();
                    }
                }

                // Convert the downloaded file (String) to CSV

                String directory = "C:\\Users\\KOV78896\\Desktop\\api\\download";

                if (bapFileContents != null) {
                    for (int i = 0; i < bapFileContents.size(); i++) {
                        String downloadFilename = bapFileName.get(i) + ".csv";
                        try {
                            FileWriter fileWriter = new FileWriter(directory + downloadFilename);
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                            bufferedWriter.write(bapFileContents.get(i));

                            bufferedWriter.close();
                        } catch (Exception e) {
                            System.out.println("Converting to CSV from String error:");
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println(
                            "CSV contents from download() method does not go to bapFileConents making it empty. ");
                }

                System.out.println("Program ran successfully. ");
            } else {
                System.out.println("Program had an error");
                remote.disconnect();
            }

        } catch (Exception e) {
            System.out.println("Error (Stacktrace)");
            e.printStackTrace();
        } finally {
            System.out.println("Disconnecting: " + remote.disconnect());
        }
    }

    // PSVM, Program entrypoint
    public static void main(String[] args) throws Exception {
        NFISFileSystem nfsObj = new NFISFileSystem();
        nfsObj.runNfis();
    }

    @Override
    public void event(String referenceid, Date eventtimestamp, String type, String remarks) throws Exception {
        System.out.println(
                eventtimestamp + " DEBUG: [" + type + "] REFID: [" + referenceid + "] remarks: [" + remarks + "].");
    }
}
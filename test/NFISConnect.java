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

public class NFISConnect implements EventListenerInterface {

    // Constructor
    public NFISConnect() {

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

    // code to get NAME FROM XML

    public void runNfisConnection() throws Exception {
        try {
            // Call to remote.connect API
            Boolean isConnected = nfisConnection();
            String connectionStatus = isConnected ? "Connected" : "Not connected";
            System.out.println("Connection status: " + connectionStatus);

        } catch (Exception e) {
            System.out.println("Connection stacktrace: ");
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
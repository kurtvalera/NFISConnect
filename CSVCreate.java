import java.io.FileWriter;
import java.io.IOException;

public class CSVCreate {

    public void createCsvFromString() throws Exception {
        try {
            String headers = "reckey,source,name,birth,address,spouse,sex,tin,sss,civilstat,business,busaddrs,bdt,bdtdate,sec,secdate,reported,bank,branch,remarks,negdate,closedate,type,joint,loan_sala,amt_limit,secu_case,cardbal,ext_plaint,card_date,ind1,ind2,incdate";
            

        } catch (Exception e) {

        } finally {
            System.out.println("Program end.");
        }
    }

    public static void main(String[] args) throws Exception {
        CSVCreate csv = new CSVCreate();
        csv.createCsvFromString();
    }

}

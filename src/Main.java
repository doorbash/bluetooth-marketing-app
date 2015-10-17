import java.io.File;

public class Main {

    public static File[] fileNames;
    public static DatabaseHelper db;
    private static BluetoothBrowser browser;


    public static void main(String[] args) {
        fileNames = new File("dir").listFiles(pathname -> {
            if (pathname.isFile()) return true;
            return false;
        });

        db = new DatabaseHelper("files/devices.db");
        while (true) {
            try {
                browser = new BluetoothBrowser();
                break;
            } catch (Exception e1) {
                try {
                    Thread.sleep(5000);
                    System.out.println("Bluetooth device is off.");
                } catch (Exception ex) {
                }
                browser = null;
            }
        }
        db.Reset();
        new Thread(() -> {
            try {

                while (true) {


                    while (browser.is_searching) {
                        Thread.sleep(1000);
                    }
                    browser.inquiry();
                }

            } catch (Exception e) {

            }
        }).start();
    }
}

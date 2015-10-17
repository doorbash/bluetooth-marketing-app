import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class BluetoothBrowser implements DiscoveryListener {

    private LocalDevice localDevice;
    private DiscoveryAgent agent;
    public boolean is_searching = false;

    public BluetoothBrowser() throws BluetoothStateException {
        localDevice = LocalDevice.getLocalDevice();
        agent = localDevice.getDiscoveryAgent();
    }

    public void inquiry() throws BluetoothStateException {
        System.out.println("inquiry()");
        is_searching = true;
        agent.startInquiry(DiscoveryAgent.GIAC, this);
    }

    public void deviceDiscovered(RemoteDevice device, DeviceClass devClass) {

        try {

            String address = device.getBluetoothAddress();

            System.out.println("device found : " + device.getFriendlyName(false) + " " + device.getBluetoothAddress());

            if (!Main.db.contains(address)) {
                searchForDeviceServices(device, agent);
                Thread.sleep(2000);
            }

        } catch (Exception e) {
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
    }

    public void serviceSearchCompleted(int transID, int responseCode) {
    }

    public void inquiryCompleted(int discType) {
        System.out.println("inquiryCompleted()");
        is_searching = false;
    }


    public void searchForDeviceServices(RemoteDevice device, DiscoveryAgent agent) {
        final UUID OBEX_OBJECT_PUSH = new UUID(4357L);

        int[] attrIDs = new int[]{0x0100};

        UUID[] searchUuidSet = new UUID[]{OBEX_OBJECT_PUSH};

        try {
            agent.searchServices(attrIDs, searchUuidSet, device, new DiscoveryListener() {

                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                }

                public void inquiryCompleted(int discType) {
                }

                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                    for (int i = 0; i < servRecord.length; i++) {
                        String url = servRecord[i].getConnectionURL(
                                ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                        if (url == null) {
                            continue;
                        }

                        DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
                        if (serviceName != null) {
                            System.out.println("service " + serviceName.getValue() + " found " + url);
                        } else {
                            System.out.println("service found " + url);
                        }

                        try {
                            synchronized (OBEX_OBJECT_PUSH) {
                                new SendFileTask(url).start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;

                    }

                }

                @Override
                public void serviceSearchCompleted(int arg0, int arg1) {
                }
            });
            System.out.println("adding address to db: " + device.getBluetoothAddress());
        } catch (Exception e) {
            System.out.println("Error adding address to db: " + device.getBluetoothAddress());
        }
        Main.db.add(device.getBluetoothAddress());
    }
}

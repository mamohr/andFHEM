package li.klass.fhem.service;

import android.content.Context;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.data.DeviceListProvider;
import li.klass.fhem.domain.RoomDeviceList;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomListService {
    public static final RoomListService INSTANCE = new RoomListService();

    private Map<String,RoomDeviceList> deviceListMap;
    public static final String CACHE_FILENAME = "cache.obj";

    private RoomListService() {
    }

    public List<String> getRoomList(boolean refresh) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(refresh);
        ArrayList<String> roomNames = new ArrayList<String>(deviceListMap.keySet());
        for (RoomDeviceList roomDeviceList : deviceListMap.values()) {
            if (roomDeviceList.isOnlyLogDeviceRoom()) {
                roomNames.remove(roomDeviceList.getRoomName());
            }
        }
        roomNames.remove(RoomDeviceList.ALL_DEVICES_ROOM);
        return roomNames;
    }
    
    public RoomDeviceList getOrCreateDeviceListForRoom(String roomName, boolean update) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(update);
        RoomDeviceList roomDeviceList = deviceListMap.get(roomName);
        
        if (roomDeviceList == null) {
            roomDeviceList = new RoomDeviceList(roomName);
            deviceListMap.put(roomName, roomDeviceList);
        }
        return roomDeviceList;
    }
    
    public void removeDeviceListForRoom(String roomName) {
        Map<String, RoomDeviceList> roomDeviceListMap = getRoomDeviceListMap(false);
        roomDeviceListMap.remove(roomName);
    }
    

    public RoomDeviceList deviceListForRoom(String roomName, boolean update) {
        Map<String, RoomDeviceList> deviceListMap = getRoomDeviceListMap(update);
        return deviceListMap.get(roomName);
    }

    public RoomDeviceList deviceListForAllRooms(boolean update) {
        return getRoomDeviceListMap(update).get(RoomDeviceList.ALL_DEVICES_ROOM);
    }

    private Map<String, RoomDeviceList> getRoomDeviceListMap(boolean update) {
        if (update) {
            deviceListMap = updateDeviceListMap();
        } else if (deviceListMap == null) {
            deviceListMap = getStoredDataFromFile();
        }
        return deviceListMap;
    }

    public void storeDeviceListMap() {
        cacheRoomDeviceListMap(deviceListMap);
    }

    private Map<String, RoomDeviceList> updateDeviceListMap() {
        Map<String, RoomDeviceList> newList = DeviceListProvider.INSTANCE.listDevices();
        cacheRoomDeviceListMap(newList);
        return newList;
    }


    private void cacheRoomDeviceListMap(Map<String, RoomDeviceList> content) {
        Context context = AndFHEMApplication.getContext();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(context.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE));
            objectOutputStream.writeObject(content);
        } catch (Exception e) {
            Log.e(FHEMService.class.getName(), "error occurred while serializing data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, RoomDeviceList> getStoredDataFromFile() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(AndFHEMApplication.getContext().openFileInput(CACHE_FILENAME));
            Map<String, RoomDeviceList> roomDeviceListMap = (Map<String, RoomDeviceList>) objectInputStream.readObject();

            if (roomDeviceListMap != null) {
                return roomDeviceListMap;
            }
        } catch (Exception e) {
            Log.d(FHEMService.class.getName(), "error occurred while de-serializing data", e);
        }
        return updateDeviceListMap();
    }
}

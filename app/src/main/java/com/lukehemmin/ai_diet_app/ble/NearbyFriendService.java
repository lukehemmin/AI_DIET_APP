package com.lukehemmin.ai_diet_app.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * BLE 기반 근거리 친구 찾기 서비스
 * - 사용자가 친구 추가 화면을 열면 BLE Advertise + Scan 시작
 * - 주변에서 같은 앱을 사용 중인 사용자 발견
 * - 발견된 사용자 정보를 콜백으로 전달
 */
public class NearbyFriendService {
    private static final String TAG = "NearbyFriendService";
    
    // DoDiet 앱 전용 UUID
    private static final UUID SERVICE_UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb");
    
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private BluetoothLeAdvertiser advertiser;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isScanning = false;
    private boolean isAdvertising = false;
    
    private NearbyFriendCallback callback;
    private String currentUserId;
    private String currentUserName;
    
    // 스캔 타임아웃 (30초)
    private static final long SCAN_TIMEOUT = 30000;
    
    public interface NearbyFriendCallback {
        void onFriendFound(NearbyFriend friend);
        void onScanStarted();
        void onScanStopped();
        void onError(String message);
    }
    
    public static class NearbyFriend {
        public String oduserId;
        public String name;
        public int rssi; // 신호 강도 (거리 추정용)
        
        public NearbyFriend(String userId, String name, int rssi) {
            this.oduserId = userId;
            this.name = name;
            this.rssi = rssi;
        }
    }
    
    public NearbyFriendService(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;
    }
    
    public void setCallback(NearbyFriendCallback callback) {
        this.callback = callback;
    }
    
    public void setCurrentUser(String userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
    }
    
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null && 
               context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    
    public boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * BLE 스캔 및 광고 시작
     */
    public void startDiscovery() {
        if (!isBluetoothSupported()) {
            if (callback != null) callback.onError("블루투스를 지원하지 않는 기기입니다.");
            return;
        }
        
        if (!isBluetoothEnabled()) {
            if (callback != null) callback.onError("블루투스를 켜주세요.");
            return;
        }
        
        if (!hasRequiredPermissions()) {
            if (callback != null) callback.onError("블루투스 권한이 필요합니다.");
            return;
        }
        
        startAdvertising();
        startScanning();
        
        if (callback != null) callback.onScanStarted();
        
        // 타임아웃 후 자동 종료
        handler.postDelayed(this::stopDiscovery, SCAN_TIMEOUT);
    }
    
    /**
     * BLE 스캔 및 광고 중지
     */
    public void stopDiscovery() {
        stopAdvertising();
        stopScanning();
        
        if (callback != null) callback.onScanStopped();
    }
    
    private void startAdvertising() {
        if (isAdvertising) return;
        
        try {
            advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            if (advertiser == null) {
                Log.w(TAG, "BLE Advertiser not supported");
                return;
            }
            
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setConnectable(false)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .build();
            
            // 서비스 UUID와 사용자 정보를 광고 데이터에 포함
            String userData = currentUserId != null ? currentUserId.substring(0, Math.min(8, currentUserId.length())) : "unknown";
            
            AdvertiseData data = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .addServiceUuid(new ParcelUuid(SERVICE_UUID))
                    .addServiceData(new ParcelUuid(SERVICE_UUID), userData.getBytes(StandardCharsets.UTF_8))
                    .build();
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                advertiser.startAdvertising(settings, data, advertiseCallback);
                isAdvertising = true;
                Log.d(TAG, "BLE Advertising started");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start advertising", e);
        }
    }
    
    private void stopAdvertising() {
        if (!isAdvertising || advertiser == null) return;
        
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                advertiser.stopAdvertising(advertiseCallback);
            }
            isAdvertising = false;
            Log.d(TAG, "BLE Advertising stopped");
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop advertising", e);
        }
    }
    
    private void startScanning() {
        if (isScanning) return;
        
        try {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner == null) {
                Log.w(TAG, "BLE Scanner not available");
                return;
            }
            
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            
            // DoDiet 앱의 서비스 UUID만 필터링
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                    .build();
            
            List<ScanFilter> filters = Collections.singletonList(filter);
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                scanner.startScan(filters, settings, scanCallback);
                isScanning = true;
                Log.d(TAG, "BLE Scanning started");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start scanning", e);
        }
    }
    
    private void stopScanning() {
        if (!isScanning || scanner == null) return;
        
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                scanner.stopScan(scanCallback);
            }
            isScanning = false;
            Log.d(TAG, "BLE Scanning stopped");
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop scanning", e);
        }
    }
    
    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d(TAG, "Advertise started successfully");
        }
        
        @Override
        public void onStartFailure(int errorCode) {
            Log.e(TAG, "Advertise failed with error: " + errorCode);
            isAdvertising = false;
        }
    };
    
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processScanResult(result);
        }
        
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processScanResult(result);
            }
        }
        
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan failed with error: " + errorCode);
            isScanning = false;
            if (callback != null) callback.onError("스캔 실패: " + errorCode);
        }
    };
    
    private void processScanResult(ScanResult result) {
        if (result.getScanRecord() == null) return;
        
        byte[] serviceData = result.getScanRecord().getServiceData(new ParcelUuid(SERVICE_UUID));
        if (serviceData == null) return;
        
        String userId = new String(serviceData, StandardCharsets.UTF_8);
        
        // 자기 자신은 무시
        if (currentUserId != null && userId.startsWith(currentUserId.substring(0, Math.min(8, currentUserId.length())))) {
            return;
        }
        
        Log.d(TAG, "Found nearby friend: " + userId + ", RSSI: " + result.getRssi());
        
        // TODO: 서버에서 userId로 사용자 정보 조회
        NearbyFriend friend = new NearbyFriend(userId, "주변 사용자", result.getRssi());
        
        handler.post(() -> {
            if (callback != null) callback.onFriendFound(friend);
        });
    }
    
    public boolean isDiscovering() {
        return isScanning || isAdvertising;
    }
}

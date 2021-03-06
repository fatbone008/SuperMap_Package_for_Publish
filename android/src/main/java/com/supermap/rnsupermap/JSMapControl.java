package com.supermap.rnsupermap;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.supermap.RNUtils.N_R_EventSender;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.supermap.data.Enum;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoPie;
import com.supermap.data.GeoPoint;
import com.supermap.data.GeoRegion;
import com.supermap.data.Geometry;
import com.supermap.data.Point2D;
import com.supermap.mapping.Action;
import com.supermap.mapping.ActionChangedListener;
import com.supermap.mapping.ConfigurationChangedListener;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapParameterChangedListener;
import com.supermap.mapping.RefreshListener;
import com.supermap.navi.Navigation2;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by will on 2016/6/16.
 */
public class JSMapControl extends ReactContextBaseJavaModule {

    private static Map<String,MapControl> mapControlList=new HashMap<String,MapControl>();
    private static String mSTine;
    private MapControl mMapControl;
    private Navigation2 mNavigation2;
    ReactContext mReactContext;

    private static final String BOUNDSCHANGED = "Supermap.MapControl.MapParamChanged.BoundsChanged";
    private static final String SCALECHANGED = "Supermap.MapControl.MapParamChanged.ScaleChanged";
    private static final String ANGLECHANGED = "Supermap.MapControl.MapParamChanged.AngleChanged";
    private static final String SIZECHANGED = "Supermap.MapControl.MapParamChanged.SizeChanged";
    private static final String TOHORIZONTALSCREEN = "com.supermap.RN.JSMapControl.to_horizontal_screen";
    private static final String TOVERTICALSCREEN = "com.supermap.RN.JSMapControl.to_verticalscreen";


    private static final String LONGPRESS_EVENT = "com.supermap.RN.JSMapcontrol.long_press_event";

    private static final String REFRESH_EVENT = "com.supermap.RN.JSMapcontrol.refresh_event";

    @Override
    public String getName(){return "JSMapControl";}

    public JSMapControl(ReactApplicationContext reactContext){
        super(reactContext);
        mReactContext = reactContext;
    }

    public static String registerId(MapControl mapControl){
        for(Map.Entry entry:mapControlList.entrySet()){
            if(mapControl.equals(entry.getValue())){
                return (String)entry.getKey();
            }
        }
        Calendar calendar=Calendar.getInstance();
        String id=Long.toString(calendar.getTimeInMillis());
        mapControlList.put(id,mapControl);
        return id;
    }

    @ReactMethod
    public void getMap(String mapControlId, Promise promise){
        try{
            MapControl mapControl=mapControlList.get(mapControlId);
            com.supermap.mapping.Map map=mapControl.getMap();

//            写入map及其ID，返回ID，如果已经map已经存在，返回已存在的Id
            String mapId=JSMap.registerId(map);

            WritableMap rtnMap= Arguments.createMap();
            rtnMap.putString("mapId",mapId);
            promise.resolve(rtnMap);
        }catch (Exception e){
            promise.reject(e);
        }
    }

    /**
     * 监听编辑行为的变更事件
     * @param mapControlId mapControl实例ID
     * @param promise JS层的promise对象
     */
    @ReactMethod
    public void addActionChangedListener(String mapControlId,Promise promise){
        try{
            MapControl mapControl = mapControlList.get(mapControlId);
            mapControl.addActionChangedListener(new ActionChangedListener() {
                @Override
                public void actionChanged(Action action, Action action1) {
                    N_R_EventSender n_r_eventSender = new N_R_EventSender();
                    n_r_eventSender.putString("newAction",action.name());
                    n_r_eventSender.putString("oldAction",action1.name());
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("ActionChange",n_r_eventSender.createSender());
                }
            });
            promise.resolve(true);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    /**
     * 监听长按动作和滚动动作
     * @param mapControlId
     * @param promise
     */
    @ReactMethod
    public void setGestureDetector(String mapControlId, final Promise promise){
        try{
            MapControl mapControl = mapControlList.get(mapControlId);
            mapControl.setGestureDetector(new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
                public void onLongPress(MotionEvent event){
                    WritableMap map = Arguments.createMap();
                    map.putInt("x",(int)event.getX());
                    map.putInt("y",(int)event.getY());

                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(LONGPRESS_EVENT,map);
                }

                public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                        float distanceX, float distanceY){
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("NavigationScroll",null);
                    return false;
                }
            }));
            promise.resolve(true);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    /**
     * 监听原生map刷新事件
     * @param mapControlId
     * @param promise
     */
    @ReactMethod
    public void setRefreshListener(String mapControlId , Promise promise){
        try{
            MapControl mapControl = mapControlList.get(mapControlId);
            mapControl.setRefreshListener(new RefreshListener() {
                @Override
                public void mapRefresh() {
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(REFRESH_EVENT,null);
                }
            });
            promise.resolve(true);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setAction(String mapControlId,int actionType,Promise promise){
        try{
            MapControl mapControl = mapControlList.get(mapControlId);
            mapControl.setAction((Action) Enum.parse(Action.class,actionType));
            promise.resolve(true);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    @ReactMethod
    public void submit(String mapControlId,Promise promise){
        try{
            MapControl mapControl = mapControlList.get(mapControlId);
            Boolean b = mapControl.submit();

            promise.resolve(b);
        }catch (Exception e){
            promise.reject(e);
        }
    }
    @ReactMethod
    public void getNavigation2(String mapControlId,Promise promise){
        try{
            mMapControl = mapControlList.get(mapControlId);
            getCurrentActivity().runOnUiThread(updateThread);
            String navigation2Id = JSNavigation2.registerId(mNavigation2);

            WritableMap map = Arguments.createMap();
            map.putString("navigation2Id",navigation2Id);
            promise.resolve(map);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    /**
     * 监听地图参数变化
     * @param mapControlId
     * @param promise
     */
    @ReactMethod
    public void setMapParamChangedListener(String mapControlId,Promise promise){
        try{
            mMapControl = mapControlList.get(mapControlId);
            mMapControl.setMapParamChangedListener(new MapParameterChangedListener() {
                @Override
                public void scaleChanged(double v) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("scale",v);
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(BOUNDSCHANGED,map);
                }

                @Override
                public void boundsChanged(Point2D point2D) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("x",(int)point2D.getX());
                    map.putInt("y",(int)point2D.getY());
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(SCALECHANGED,map);
                }

                @Override
                public void angleChanged(double v) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("angle",v);
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(ANGLECHANGED,map);
                }

                @Override
                public void sizeChanged(int i, int i1) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("width",i);
                    map.putInt("height",i1);
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(SIZECHANGED,map);
                }
            });
            promise.resolve(true);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getCurrentGeometry(String mapControlId,Promise promise){
        try{
            mMapControl = mapControlList.get(mapControlId);
            Geometry geometry = mMapControl.getCurrentGeometry();
            String geometryId = JSGeometry.registerId(geometry);

            WritableMap map = Arguments.createMap();
            map.putString("geometryId",geometryId);

            String type = "";
            if(geometry instanceof GeoPoint){
                type = "GeoPoint";
            }else if(geometry instanceof GeoLine){
                type = "GeoLine";
            }else if(geometry instanceof GeoRegion){
                type = "GeoRegion";
            }
            map.putString("geoType",type);

            promise.resolve(map);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setConfigurationChangedListener(String mapControlId,Promise promise){
        try{
            mMapControl = mapControlList.get(mapControlId);
            mMapControl.setConfigurationChangedListener(new ConfigurationChangedListener() {
                @Override
                public void toHorizontalScreen() {
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(TOHORIZONTALSCREEN,null);
                }

                @Override
                public void toVerticalScreen() {
                    mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(TOVERTICALSCREEN,null);
                }
            });

            promise.resolve(true);
        }catch(Exception e){
            promise.reject(e);
        }
    }

    Runnable updateThread = new Runnable(){
        @Override
        public void run(){
            mNavigation2 = mMapControl.getNavigation2();
        }
    };
}

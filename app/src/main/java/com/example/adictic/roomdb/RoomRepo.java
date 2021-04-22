package com.example.adictic.roomdb;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RoomRepo {
    private final RoomDB roomDB;

    //Creació de la BDD
    public RoomRepo(Context context){
        String DB_NAME = "room_database";
        roomDB = Room.databaseBuilder(context,RoomDB.class, DB_NAME).build();
    }

    /**
     * Funcions per Entity BlockedApp
     */

    public List<BlockedApp> getAllBlockedApps() {
        try {
            Callable<List<BlockedApp>> callable = () -> roomDB.blockedAppDAO().getAll();

            Future<List<BlockedApp>> future = Executors.newSingleThreadExecutor().submit(callable);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
    public List<BlockedApp> getAllNotBlockedApps() { return roomDB.blockedAppDAO().getAllNotBlocked(); }

    public BlockedApp findBlockedAppByPkg(String pkg){
        return roomDB.blockedAppDAO().findByPkgName(pkg);
    }

    public void updateBlockApp(BlockedApp blockedApp){
        roomDB.blockedAppDAO().update(blockedApp);
    }

    public void deleteBlockApp(BlockedApp blockedApp){ roomDB.blockedAppDAO().delete(blockedApp); }
    public int deleteAllBlockApps(){
        try {
            Callable<Integer> callable = () -> roomDB.blockedAppDAO().deleteAll();

            Future<Integer> future = Executors.newSingleThreadExecutor().submit(callable);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void insertBlockApp(BlockedApp blockedApp){
        roomDB.blockedAppDAO().insert(blockedApp);
    }

    /**
     * Funcions per Entity FreeUseApp
     */

    public List<FreeUseApp> getAllFreeUseApps(){
        return roomDB.freeUseAppDAO().getAll();
    }

    public FreeUseApp findFreeUseAppByPkg(String pkg){
        return roomDB.freeUseAppDAO().findByPkgName(pkg);
    }

    public void updateFreeUseApp(FreeUseApp freeUseApp){
        roomDB.freeUseAppDAO().update(freeUseApp);
    }

    public void deleteFreeUseApp(FreeUseApp freeUseApp){ roomDB.freeUseAppDAO().delete(freeUseApp); }
    public void deleteAllFreeUseApps(){ roomDB.freeUseAppDAO().deleteAll(); }

    public void insertFreeUseApp(FreeUseApp freeUseApp){
        roomDB.freeUseAppDAO().insert(freeUseApp);
    }

    /**
     * Funcions per Entity HorarisNit
     */

    public List<HorarisNit> getAllHorarisNit(){
        return roomDB.horarisNitDAO().getAll();
    }

    public HorarisNit findByDay(long day){
        return roomDB.horarisNitDAO().findByDay(day);
    }

    public void updateHorarisNit(HorarisNit horarisNit){
        roomDB.horarisNitDAO().update(horarisNit);
    }

    public void deleteHorarisNit(HorarisNit horarisNit){
        roomDB.horarisNitDAO().delete(horarisNit);
    }

    public void insertHorarisNit(HorarisNit horarisNit){
        roomDB.horarisNitDAO().insert(horarisNit);
    }

    /**
     * Funcions per Entity EventBlock
     */

    public List<EventBlock> getAllEventBlocks(){
        return roomDB.eventBlockDAO().getAll();
    }

    public List<EventBlock> getAllActiveEvents(){
        Callable<List<EventBlock>> callable = () -> roomDB.eventBlockDAO().getActiveEvents();

        Future<List<EventBlock>> future = Executors.newSingleThreadExecutor().submit(callable);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public EventBlock getEventFromList(String name){
        return roomDB.eventBlockDAO().getEventFromList(name);
    }

    public EventBlock getEventFromListWithID(Long id){
        return roomDB.eventBlockDAO().getEventFromListWithID(id);
    }

    public List<EventBlock> getMondayEvents(){
        return roomDB.eventBlockDAO().getMonday();
    }

    public List<EventBlock> getTuesdayEvents(){
        return roomDB.eventBlockDAO().getTuesday();
    }

    public List<EventBlock> getWednesdayEvents(){
        return roomDB.eventBlockDAO().getWednesday();
    }

    public List<EventBlock> getThursdayEvents(){
        return roomDB.eventBlockDAO().getThursday();
    }

    public List<EventBlock> getFridayEvents(){
        return roomDB.eventBlockDAO().getFriday();
    }

    public List<EventBlock> getSaturdayEvents(){
        return roomDB.eventBlockDAO().getSaturday();
    }

    public List<EventBlock> getSundayEvents(){
        return roomDB.eventBlockDAO().getSunday();
    }

    public void updateEventBlock(EventBlock eventBlock){
        roomDB.eventBlockDAO().update(eventBlock);
    }

    public void deleteEventBlock(EventBlock eventBlock){
        roomDB.eventBlockDAO().delete(eventBlock);
    }

    public void insertEventBlock(EventBlock eventBlock){
        roomDB.eventBlockDAO().insert(eventBlock);
    }
}

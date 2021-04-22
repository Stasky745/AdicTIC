package com.example.adictic.roomdb;

import android.content.Context;

import androidx.room.Room;

import java.util.List;

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

    public List<BlockedApp> getAllBlockedApps(){
        return roomDB.blockedAppDAO().getAll();
    }

    public BlockedApp findBlockedAppByPkg(String pkg){
        return roomDB.blockedAppDAO().findByPkgName(pkg);
    }

    public void updateBlockApp(BlockedApp blockedApp){
        roomDB.blockedAppDAO().update(blockedApp);
    }

    public void deleteBlockApp(BlockedApp blockedApp){
        roomDB.blockedAppDAO().delete(blockedApp);
    }

    public void insertBlockApp(BlockedApp blockedApp){
        roomDB.blockedAppDAO().insert(blockedApp);
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
        return roomDB.eventBlockDAO().getActiveEvents();
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

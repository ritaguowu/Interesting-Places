package ca.wu.interestingplaces.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import ca.wu.interestingplaces.models.InterestingPlaceModel

class DBHelper(val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "InterestingPlacesDatabase"
        private const val TABLE_INTERESTING_PLACE = "InterestingPlacesTable"

        //All the Columns names
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION ="location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE ="longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_INTERESTING_PLACE_TABLE = ("CREATE TABLE " + TABLE_INTERESTING_PLACE + "( "
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_TITLE + " TEXT, "
                + KEY_IMAGE + " TEXT, "
                + KEY_DESCRIPTION + " TEXT, "
                + KEY_DATE + " TEXT, "
                + KEY_LOCATION + " TEXT, "
                + KEY_LATITUDE + " TEXT, "
                + KEY_LONGITUDE + " TEXT)"
                )
        db?.execSQL(CREATE_INTERESTING_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_INTERESTING_PLACE")
        onCreate(db)
    }

    fun deleteAllRecordsFromDB(){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_INTERESTING_PLACE")
        db.close()
    }

    fun addInterestingPlace(place: InterestingPlaceModel): Long{
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, place.title)
        contentValues.put(KEY_IMAGE, place.image)
        contentValues.put(KEY_DESCRIPTION, place.address)
        contentValues.put(KEY_DATE, place.date)
        contentValues.put(KEY_LOCATION, place.location)
        contentValues.put(KEY_LATITUDE, place.latitude)
        contentValues.put(KEY_LONGITUDE, place.longitude)

        //Insert Rows
        val result = db.insert(TABLE_INTERESTING_PLACE, null, contentValues)

        db.close()
        return result
    }

    fun updateInterestingPlace(interestingPlace: InterestingPlaceModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, interestingPlace.title)
        contentValues.put(KEY_IMAGE, interestingPlace.image)
        contentValues.put(KEY_DESCRIPTION, interestingPlace.address)
        contentValues.put(KEY_DATE, interestingPlace.date)
        contentValues.put(KEY_LOCATION, interestingPlace.location)
        contentValues.put(KEY_LATITUDE, interestingPlace.latitude)
        contentValues.put(KEY_LONGITUDE, interestingPlace.longitude)

        val whereClass = KEY_ID + "=" + interestingPlace.id
        //Insert Rows
        val result = db.update(TABLE_INTERESTING_PLACE, contentValues, whereClass,null)

        db.close()
        return result
    }

    fun deleteInterestingPlace(interestingPlace: InterestingPlaceModel): Int{
        val db = this.writableDatabase
        val whereClass = KEY_ID + "=" + interestingPlace.id
        val result = db.delete(TABLE_INTERESTING_PLACE, whereClass,null)

        db.close()
        return result
    }

    @SuppressLint("Range")
    fun getAllPlaces(): ArrayList<InterestingPlaceModel>{
        val interestingPlaceList: ArrayList<InterestingPlaceModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_INTERESTING_PLACE"
        val db = this.readableDatabase

        try{
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do{
                    val place = InterestingPlaceModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    interestingPlaceList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return interestingPlaceList
    }
}

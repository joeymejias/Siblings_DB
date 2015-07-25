package edu.nyu.scps.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Helper extends SQLiteOpenHelper {

    public Helper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //a Java array of five Strings containing five SQLite statements.
        String[] statements = {
                "CREATE TABLE people ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "name TEXT"
                        + ");",

                "INSERT INTO people (_id, name) VALUES (NULL, 'Joey');",
                "INSERT INTO people (_id, name) VALUES (NULL, 'Andy');",
                "INSERT INTO people (_id, name) VALUES (NULL, 'Madisyn');",
        };

        for (String statement: statements) {
            db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public Cursor getCursor() {
        SQLiteDatabase db = getReadableDatabase(); // the db passed to onCreate
        //can say "_id, name" instead of "*", but _id must be included.
        Cursor cursor = db.rawQuery("SELECT * FROM people ORDER BY name;", null);
        cursor.moveToFirst();
        return cursor;
    }
}

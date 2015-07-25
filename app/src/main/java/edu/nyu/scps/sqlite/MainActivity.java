package edu.nyu.scps.sqlite;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private String databaseName = "siblings.db";
    private String tableName = "people";
    private Helper helper;   //Can't initialize these fields before onCreate.
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listView = (ListView)findViewById(R.id.listView);
        TextView textView = (TextView)findViewById(R.id.empty);
        listView.setEmptyView(textView);   //Display this TextView when table contains no records.

        helper = new Helper(this, databaseName);
        Cursor cursor = helper.getCursor();

        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[] {"name",             "_id"},
                new int[]    {android.R.id.text1, android.R.id.text2},
                0	//don't need any flags
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, final long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.title_update_or_delete);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position); //downcast
                int nameIndex = cursor.getColumnIndex("name");
                int _idIndex = cursor.getColumnIndex("_id");
                String message = cursor.getString(nameIndex) + "\n" + cursor.getString(_idIndex);
                builder.setMessage(message);
                builder.setNeutralButton(R.string.button_cancel, null); //Make dialog disappear without doing anything.

                builder.setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase database = helper.getWritableDatabase();
                        database.delete(tableName, "_id = ?", new String[] {Long.toString(id)});
                        adapter.changeCursor(helper.getCursor());
                    }
                });

                builder.setPositiveButton(R.string.button_update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.title_update);
                        Cursor cursor = (Cursor) parent.getItemAtPosition(position); //downcast
                        int nameIndex = cursor.getColumnIndex("name");
                        int _idIndex = cursor.getColumnIndex("_id");
                        String message = cursor.getString(nameIndex) + "\n" + cursor.getString(_idIndex);
                        builder.setMessage(message);

                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.edittext, null);
                        builder.setView(view);

                        builder.setNegativeButton(R.string.button_cancel, null);
                        final AlertDialog alertDialog = builder.create();
                        EditText editText = (EditText)view.findViewById(R.id.editText);

                        editText.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN
                                        && keyCode == KeyEvent.KEYCODE_ENTER) {
                                    EditText editText = (EditText)v;
                                    Editable editable = editText.getText();
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("name", editable.toString());
                                    SQLiteDatabase database = helper.getWritableDatabase();
                                    database.update(tableName, contentValues, "_id = ?", new String[] {Long.toString(id)});
                                    adapter.changeCursor(helper.getCursor());
                                    alertDialog.dismiss();
                                    return true;
                                }
                                return false;
                            }
                        });


                        alertDialog.show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_append) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_new);
            builder.setMessage(R.string.message_new);

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.edittext, null);
            builder.setView(view);

            builder.setNegativeButton(R.string.button_cancel, null);
            final AlertDialog alertDialog = builder.create();
            EditText editText = (EditText)view.findViewById(R.id.editText);

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN
                            && keyCode == KeyEvent.KEYCODE_ENTER) {
                        EditText editText = (EditText)v;
                        Editable editable = editText.getText();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("name", editable.toString());
                        SQLiteDatabase database = helper.getWritableDatabase();
                        database.insert(tableName, null, contentValues);
                        adapter.changeCursor(helper.getCursor());
                        alertDialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });

            alertDialog.show();
            return true;
        }

        if (id == R.id.action_delete_all) {
            //Delete all the records, but do not destroy the database itself.
            SQLiteDatabase database = helper.getWritableDatabase();
            database.delete(tableName, null, null);
            adapter.changeCursor(helper.getCursor());
            return true;
        }

        if (id == R.id.action_reset) {
            //Destroy the database and re-create it.
            helper.close();
            deleteDatabase(databaseName);
            helper = new Helper(this, databaseName);
            adapter.changeCursor(helper.getCursor());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

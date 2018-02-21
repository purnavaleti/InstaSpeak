package com.status200.hassan.instaspeak;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static android.R.attr.name;
import static android.R.attr.text;
import static android.R.attr.version;

public class SplashPage extends AppCompatActivity {
    public final static String KEY_EXTRA_CONTACT_ID = "KEY_EXTRA_CONTACT_ID";
    private ListView listView;
    DatabaseSql dbHelper;
    TextToSpeech t1;
    SimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
        Button button = (Button) findViewById(R.id.button4);
        Button button1 = (Button) findViewById(R.id.button2);
        dbHelper = new DatabaseSql(this);
        final Cursor cursor = dbHelper.getAllPersons();
        final String[] columns = new String[]{
                dbHelper.PERSON_COLUMN_ID,
                dbHelper.PERSON_COLUMN_NAME
        };

        final int[] widgets = new int[]{
                R.id.textView,
                R.id.textView2
        };
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.wordslist,
                cursor, columns, widgets, 1);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(cursorAdapter);

        final EditText editText = (EditText) findViewById(R.id.editText2);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adding db
                dbHelper.insertWord(editText.getText().toString());
                Cursor newc = dbHelper.getAllPersons();
                cursorAdapter.swapCursor(newc);

                listView.setAdapter(cursorAdapter);
                Context context = getApplicationContext();
                CharSequence text = editText.getText().toString() + " Added to list";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                String speaker = editText.getText().toString();
                String utterenceId = this.hashCode() + "";
                t1.speak(speaker,TextToSpeech.QUEUE_FLUSH,null,utterenceId);
                int duration = Toast.LENGTH_SHORT;
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Speaking!", duration);
                toast.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView listView, View view,
                                    int position, long id) {
                Cursor itemCursor = (Cursor) SplashPage.this.listView.getItemAtPosition(position);
                int textView1 = itemCursor.getInt(itemCursor.getColumnIndex(dbHelper.PERSON_COLUMN_ID));
                Cursor rs = dbHelper.getPerson(textView1);
                rs.moveToFirst();
                String personName = rs.getString(rs.getColumnIndex(dbHelper.PERSON_COLUMN_NAME));
                String utterenceId = this.hashCode() + "";
                t1.speak(personName,TextToSpeech.QUEUE_FLUSH,null,utterenceId);
                if (!rs.isClosed()) {
                    rs.close();
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.clear:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
// Add the buttons
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        dbHelper.deleteAll();
                        Cursor newca = dbHelper.getAllPersons();
                        cursorAdapter.swapCursor(newca);

                        listView.setAdapter(cursorAdapter);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                builder.setMessage("Your dialogs will be flushed")
                        .setTitle("Are you sure?");
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.info:
                Intent ir = new Intent(this,Information.class);
                startActivity(ir);
                break;
            default:
                break;
        }
        return true;
    }
}

class DatabaseSql extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SQLiteExample.db";
    private static final int DATABASE_VERSION = 1;
    public static final String PERSON_TABLE_NAME = "sentences";
    public static final String PERSON_COLUMN_ID = "_id";
    public static final String PERSON_COLUMN_NAME = "word";

    public DatabaseSql(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + PERSON_TABLE_NAME + "(" +
                PERSON_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                PERSON_COLUMN_NAME + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PERSON_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertWord(String word) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PERSON_COLUMN_NAME, word);
        db.insert(PERSON_TABLE_NAME, null, contentValues);
        
        return true;
    }

    public Cursor getPerson(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + PERSON_TABLE_NAME + " WHERE " +
                PERSON_COLUMN_ID + "=?", new String[] { Integer.toString(id) } );
        return res;
    }

    public Cursor getAllPersons() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "SELECT * FROM " + PERSON_TABLE_NAME, null );
        return res;
    }

    public Integer deletePerson(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PERSON_TABLE_NAME,
                PERSON_COLUMN_ID + " = ? ",
                new String[] { Integer.toString(id) });
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(PERSON_TABLE_NAME,null,null);
    }
}
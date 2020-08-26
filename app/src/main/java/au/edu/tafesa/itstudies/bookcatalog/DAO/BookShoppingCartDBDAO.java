package au.edu.tafesa.itstudies.bookcatalog.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.edu.tafesa.itstudies.bookcatalog.models.Book;
import au.edu.tafesa.itstudies.bookcatalog.models.BookShoppingCartModel;

/**
 * Created by sruiz on 26/03/2018.
 * Update 26/05/2018 - tidy up for release
 * Implements an set of Data Access Objects based on using a SQLite database
 * This is a Singleton class - only one object is evr created and the user doesnot create that
 * access to the single object is via the connectToBookDB static method.
 */

public class BookShoppingCartDBDAO {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Books.db";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_BOOK_TITLE = "title";
    public static final String COLUMN_BOOK_PRICE = "price";
    public static final String COLUMN_BOOK_SELECTED = "selected";

    public static final String TABLE_BOOKS = "tblBooks";

    public static final String SQL_CREATE_TBL_BOOKS =
            " CREATE TABLE IF NOT EXISTS " + TABLE_BOOKS +
                    " (" + COLUMN_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COLUMN_BOOK_TITLE + " TEXT, " +
                    COLUMN_BOOK_SELECTED + " INT, " +
                    COLUMN_BOOK_PRICE + " NUMBER)";

    private SQLiteDatabase database;
    private BookSQLiteOpenHelper dbHelper;
    private static BookShoppingCartDBDAO singleBookDBObject=null;

    private BookShoppingCartDBDAO(Context context) {
        long start, end;
        start = System.currentTimeMillis();
        dbHelper = new BookSQLiteOpenHelper(context);
        end = System.currentTimeMillis();
        Log.i("connecting", "-> Time taken (ms) =" + (end - start));
        open();
        database.beginTransaction();
    }

    public static BookShoppingCartDBDAO ConnectToBookDB(Context context){
        if (singleBookDBObject==null){
            singleBookDBObject = new BookShoppingCartDBDAO(context);
        }
        singleBookDBObject.open();
        return singleBookDBObject;
    }

    public void open() throws SQLException {
        long start, end;
        start = System.currentTimeMillis();
        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }
        if (!database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }

        end = System.currentTimeMillis();
        Log.i("open", "-> Time taken (ms) =" + (end - start));

    }

    public void close() {
        long start, end;
        start = System.currentTimeMillis();
        dbHelper.close();
        end = System.currentTimeMillis();
        Log.i("close", "-> Time taken (ms) =" + (end - start));
    }

    /**
     *
     * @param modelRead The model that will be filled with the Books read from the database
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadFromDB(BookShoppingCartModel modelRead) throws IOException, ClassNotFoundException {
        String table;
        String columns[] = new String[4];
        List<Book> booksFromDB=null;
        Cursor cursor;
        Book b;

        this.open();

        //Use the class constants to set up the table name and the columns for the query
        table = TABLE_BOOKS;
        columns[0] = COLUMN_ID;
        columns[1] = COLUMN_BOOK_TITLE;
        columns[2] = COLUMN_BOOK_PRICE;
        columns[3] = COLUMN_BOOK_SELECTED;

        booksFromDB = new ArrayList<Book>();
        //query the database retrieving all books and then traversing the cursor and adding the
        // books to the booksFromDB List
		//TODO - DONE
        cursor = database.query(table, columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            b = new Book(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_TITLE)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_BOOK_PRICE)), (cursor.getInt(cursor.getColumnIndex(COLUMN_BOOK_SELECTED)) > 0));
            booksFromDB.add(b);
            cursor.moveToNext();
        }

        // booksFromDB.add(new Book("Hello", 9.99));

        //Set the models catalog List to be the booksFromDB list
        //TODO - DONE
        modelRead.setCatalog(booksFromDB);

		//CLose the cursor
        cursor.close();
    }

    /**
     * Performs a database update using te provided model. Updates existing records and
     * inserts any new ones.
     * @param bookShoppingCartModel the model to be saved to the database
     * @return The number of new records added to the database
     * @throws IOException
     */
    public int saveToDB(BookShoppingCartModel bookShoppingCartModel) throws IOException {
        ContentValues values = new ContentValues();
        int numInserted = 0;
        String whereValues[] = new String[1];
        String where;

        Log.i("SaveToDB", "is db open" + database.isOpen());
        this.open();
        // Insert all of the new books in the list with id as -1,
        // indicating that they did not come from the
        // DB ie are new
        // and update the data in the DB when the book object has an ID (!=-1)
        //TODO
        // Get ArrayList catalogList
        ArrayList<Book> catalogList = (ArrayList) bookShoppingCartModel.getCatalog();
        where = COLUMN_ID + " == ?";
        // LOOP through the ARRAYLIST.
        for(int i = 0; i < catalogList.size(); i++) {
            values.put(COLUMN_BOOK_TITLE, catalogList.get(i).getTitle());
            values.put(COLUMN_BOOK_PRICE, catalogList.get(i).getPrice());
            values.put(COLUMN_BOOK_SELECTED, catalogList.get(i).isSelected());
            whereValues[0] = catalogList.get(i).getId() + "";
            // If Book Id == -1 meaning that is a new book, will insert into database.
            // Otherwise Id != -1, update the data
            if((catalogList.get(i).getId())==-1) {
                numInserted = (int) database.insert(TABLE_BOOKS, null, values);
                // Log.i("INSERT", "INSERT AT " + numInserted);
            } else {
                database.update(TABLE_BOOKS, values, where, whereValues);
                // Log.i("UPDATE ", "SELECTED : "  + whereValues[0] + " = " + catalogList.get(i).isSelected());
            }
        }
        return numInserted;
    }

    /**
     * deletes all books in teh DB with a price > 100
     * @return The number of books deleted
     */
    public long deleteExpensive() {
        String table;
        String where;
        String whereValues[] = new String[1];
        long deleteCount = 0;

        this.open();
        whereValues[0] = "100";
        //TODO - DONE
        //Use the class constants to setup the table name and the where clause
        table = TABLE_BOOKS;
        where = COLUMN_BOOK_PRICE + " > ? ";
        //perform the delete
        deleteCount = database.delete(TABLE_BOOKS, where, whereValues);
        return deleteCount;
    }

    public long deleteBooks(int id) {
        long deleteCount = 0;
        String table = TABLE_BOOKS;
        String whereValues[] = new String[1];
        String where = COLUMN_ID + " == ? ";
        whereValues[0] = id + "";
        // Delete the book with the ID send from BookCatalogActivity
        deleteCount = database.delete(TABLE_BOOKS, where, whereValues);
        return  deleteCount;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    /**
     * Helps to setup the database
     */
    public class BookSQLiteOpenHelper extends SQLiteOpenHelper {

        public static final String CLASS_TAG = "BookSQLiteOpenHelper";


        public BookSQLiteOpenHelper(Context context) {
            //Call super class constructor to get the database created
            //TODO - DONE
            super(context, "5JAM.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("onCreate", "db = " + db);
            //Create the necessary tables
            //TODO - DONE
            db.execSQL(SQL_CREATE_TBL_BOOKS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("onUpgrade", "Upgrade old and new = " + oldVersion + "," + newVersion);
            // If there is a version change - we are going to keep it very simple and start afresh -
            // users would hate this as all data would be lost!
            if (newVersion != oldVersion) {
                Log.w(CLASS_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                        + ", which will destroy all old data");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
                onCreate(db);
            }
        }
    }
}

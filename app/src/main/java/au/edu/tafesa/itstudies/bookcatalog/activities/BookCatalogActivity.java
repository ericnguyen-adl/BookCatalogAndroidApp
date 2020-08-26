package au.edu.tafesa.itstudies.bookcatalog.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import au.edu.tafesa.itstudies.bookcatalog.DAO.BookShoppingCartDBDAO;
import au.edu.tafesa.itstudies.bookcatalog.models.Book;
import au.edu.tafesa.itstudies.bookcatalog.models.BookShoppingCartModel;
import au.edu.tafesa.itstudies.bookcatalog.R;


/**
 * Displays a list of books that can be added/removed to/from a shopping cart
 *
 * @author sruiz
 */
public class BookCatalogActivity extends Activity {

    private static final int NEW_BOOK_REQUEST = 1;
    private static final int EDIT_BOOK_REQUEST = 2;
    public static final String SELECT_POSITION = "POSITION_IN_LISTVIEW";

    private BookShoppingCartModel bookShoppingCartModel;
    private ListView listViewCatalog;
    private Button btnViewCart;
    private Button btnAddNewBook;
    private Button btnDelExp;
    private Button btnUndo;
    private Button btnCommit;
    private BookShoppingCartDBDAO bookShoppingCartDB;
    private boolean goingToChildActivity;

    // private Button btnEditABook;

    //LIFE-CYCLE EVENTS
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

            Log.i("onCreate", "current object = " + this);
        goingToChildActivity = false;
        //Connect to DB
        bookShoppingCartDB = BookShoppingCartDBDAO.ConnectToBookDB(this);

        //Create the model
        bookShoppingCartModel = new BookShoppingCartModel();

        // Create the Catalog and display in the List using the model to drive teh list Adapter
        listViewCatalog = (ListView) findViewById(R.id.ListViewCatalog);
        listViewCatalog.setAdapter(new CatalogListViewAdapter(bookShoppingCartModel));

        // Set the OnTouchListener for listview
        listViewCatalog.setOnTouchListener(new OnSwipeTouchListener(this));

        //Extract interface objects
        btnViewCart = (findViewById(R.id.ButtonViewCart));
        btnAddNewBook = (findViewById(R.id.ButtonAddBookToCatalog));
        btnDelExp = (findViewById(R.id.ButtonDeleteExpensive));
        btnUndo = (findViewById(R.id.ButtonUndo));
        btnCommit = (findViewById(R.id.ButtonCommit));

        // btnEditABook = (findViewById(R.id.ButtonEditABookInCatalog));

        //Register the Event Handlers
        btnViewCart.setOnClickListener((new ButtonViewCartOnClickHandler()));
        btnAddNewBook.setOnClickListener((new ButtonAddNewBookOnClickHandler()));
        btnDelExp.setOnClickListener((new ButtonDeleteExpensiveOnClickHandler()));
        btnUndo.setOnClickListener((new ButtonUndoOnClickHandler()));
        btnCommit.setOnClickListener((new ButtonCommitOnClickHandler()));

        // btnEditABook.setOnClickListener((new ButtonEditABookOnClickHandler()));

        //Initialise interface
        btnUndo.setEnabled(false);
        btnCommit.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFromDB();
    }

    @Override
    protected void onPause() {
        saveToDB();
        super.onPause();
    }

    @Override
    protected void onStop() {
        //Assume want a commit - perhaps should ask user
        Log.i("onStop", "Entered");
        if (btnCommit.isEnabled() && !goingToChildActivity){
            Log.i("onStop", "Commiting");
            Toast.makeText(this,"Committing all Book Catalogue changes...",Toast.LENGTH_LONG).show();
            //End the transaction with success (so data is comitted)
            bookShoppingCartDB.getDatabase().setTransactionSuccessful();
            bookShoppingCartDB.getDatabase().endTransaction();
            btnCommit.setEnabled(false);
            btnUndo.setEnabled(false);
            //Record the start of another transaction
            bookShoppingCartDB.getDatabase().beginTransaction();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Assume want a commit - perhaps should ask user
        Log.i("onDestroy", "Entered");
        if (btnCommit.isEnabled()){
            Log.i("onDestroy", "Commiting");
            //End the current transaction with success (so data is saved)
            bookShoppingCartDB.getDatabase().setTransactionSuccessful();
            bookShoppingCartDB.getDatabase().endTransaction();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == NEW_BOOK_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                bookShoppingCartModel = (BookShoppingCartModel) data.getSerializableExtra(BookShoppingCartModel.INTENT_IDENTIFIER);
                //As we now have a new Model instance we need to update the ListView Adapter
                ((CatalogListViewAdapter) listViewCatalog.getAdapter()).setTheData(bookShoppingCartModel);
                saveToDB();
                goingToChildActivity = false;
            }
        }
        if (requestCode == EDIT_BOOK_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                bookShoppingCartModel = (BookShoppingCartModel) data.getSerializableExtra(BookShoppingCartModel.INTENT_IDENTIFIER);
                //As we now have a new Model instance we need to update the ListView Adapter
                ((CatalogListViewAdapter) listViewCatalog.getAdapter()).setTheData(bookShoppingCartModel);
                saveToDB();
                goingToChildActivity = false;
            }
        }

    }

    //DO THE WORK METHODS
    private void saveToDB() {
        try {
            bookShoppingCartDB.saveToDB(bookShoppingCartModel);
        } catch (IOException e) {
            Toast.makeText(this, "Unable to save data. Problem with the Database.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFromDB() {

        try {
            // bookShoppingCartModel.setCatalog(BookShoppingCartModel.getDummyCatalogData());
            bookShoppingCartDB.loadFromDB(bookShoppingCartModel);
            ((CatalogListViewAdapter) listViewCatalog.getAdapter()).notifyDataSetChanged();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to load data. Problem with the DB.", Toast.LENGTH_LONG).show();
            //Use some dummy data (hard-coded in the model class - for testing only)
            bookShoppingCartModel.setCatalog(BookShoppingCartModel.getDummyCatalogData());
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Unable to load data. Problem with the DB.", Toast.LENGTH_LONG).show();
            //Use some dummy data (hard-coded in the model class - for testing only)
            bookShoppingCartModel.setCatalog(BookShoppingCartModel.getDummyCatalogData());
        }

    }

    //View Adapters
    /**
     * Provides the details of how to display each row of the listViewCatalog and makes use of a List of books
     * (which comes from the bookShoppingCartModel) for the data.
     *
     * @author sruiz
     */
    class CatalogListViewAdapter extends BaseAdapter {

        public BookShoppingCartModel getTheData() {
            return theData;
        }

        public void setTheData(BookShoppingCartModel theData) {
            this.theData = theData;
        }

        private BookShoppingCartModel theData;

        public CatalogListViewAdapter(BookShoppingCartModel theData) {
            this.theData = theData;
        }

        public int getCount() {
            return theData.getCatalog().size();
        }

        public Object getItem(int position) {
            return theData.getCatalog().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewItem rowViewComponents;

            // First time the converttView will be null and we will create it using the "book_list_item" layout
            if (convertView == null) {
                convertView = BookCatalogActivity.this.getLayoutInflater().inflate(R.layout.book_list_item, null);
                rowViewComponents = new ViewItem();
                //Set the components of rowViewComponent from the "book_list_item" layout
                rowViewComponents.textViewTitle = (TextView) convertView.findViewById(R.id.textViewItem);
                rowViewComponents.textViewPrice = (TextView) convertView.findViewById(R.id.textViewPrice);

                rowViewComponents.btnEdit = (Button) convertView.findViewById(R.id.btnEdit);
                rowViewComponents.btnAdd = (Button) convertView.findViewById(R.id.btnAdd);
                rowViewComponents.btnRemove = (Button) convertView.findViewById(R.id.btnRemove);
                //Register the handlers for the buttons on the rowView

                rowViewComponents.btnEdit.setOnClickListener(new ButtonEditOnClickHandler());
                rowViewComponents.btnAdd.setOnClickListener(new ButtonAddOnClickHandler());
                rowViewComponents.btnRemove.setOnClickListener(new ButtonRemoveOnClickHandler());
                // Ensure we save this in our convertView as a Tag
                convertView.setTag(rowViewComponents);
            } else {
                rowViewComponents = (ViewItem) convertView.getTag();
            }

            //Get the current book from the Model and set the rowView components appropriately
            // Remembering to adjust whether the Add/Remove buttons are enabled or disabled
            Book curBook = theData.getCatalog().get(position);
            rowViewComponents.textViewTitle.setText(curBook.getTitle());
            rowViewComponents.textViewPrice.setText("$" + curBook.getPrice());
            rowViewComponents.btnAdd.setEnabled(!curBook.isSelected());
            rowViewComponents.btnRemove.setEnabled(curBook.isSelected());

            return convertView;
        }

        private class ViewItem {
            TextView textViewTitle;
            TextView textViewPrice;
            // Adding the btnEdit
            Button btnEdit;

            Button btnAdd;
            Button btnRemove;
        }

    }

    // OnSwipeTouchListener Event.
    class OnSwipeTouchListener implements View.OnTouchListener {
        private GestureDetector gestureDetector;
        private Context context;
        // create a new GestureDetector to start detecting the gesture from user
        public OnSwipeTouchListener(Context ctx) {
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            context = ctx;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public void onSwipeRight(int pos) {
            // Log.i("SWIPE", "SWIPING FROM LEFT TO RIGHT. At pos = " + pos);
            if(pos != -1) {
                int id = bookShoppingCartModel.getCatalog().get(pos).getId();
                String bookTitle = bookShoppingCartModel.getCatalog().get(pos).getTitle();
                // Log.i("ID TO BE DELETED ", "ID =" + id);
                bookShoppingCartDB.deleteBooks(id);
                loadFromDB();
                Toast.makeText(context, "DELETED BOOK TITLE '" + bookTitle +
                        "' FROM Database. Click on 'COMMIT ALL CHANGES' button to complete the DELETION.", Toast.LENGTH_LONG).show();
                btnUndo.setEnabled(true);
                btnCommit.setEnabled(true);
            }
        }
        // No Implementation for onSwipeLeft yet.
        public void onSwipeLeft(int pos) {
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            // Override the onDown method and return true. This is because all gestures begin with onDown().
            // If onDown return false, the system will ignore the rest of gesture.
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            // pointToPosition of listview will return the position of selected list item.
            private int getPostion(MotionEvent e1) {
                return listViewCatalog.pointToPosition((int) e1.getX(), (int) e1.getY());
            }
            // Override onFling. This will define how the action SWIPE TO RIGHT is.
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY)
                        && Math.abs(distanceX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight(getPostion(e1));
                    else
                        onSwipeLeft(getPostion(e1));
                    return true;
                }
                return false;
            }
        }
    }


    class ButtonEditOnClickHandler implements OnClickListener {
        public void onClick(View v) {
            int position;
            View rowView;

            rowView = (View) v.getParent();
            position = listViewCatalog.getPositionForView(rowView);
            // Ask the model to set the book at this position to be selected=true
            // Remember to notify the listview if the data change so the view updates.

            // get the book object at current position
            Book book = bookShoppingCartModel.getCatalog().get(position);

            goingToChildActivity = true;
            btnUndo.setEnabled(true);
            btnCommit.setEnabled(true);

            Intent viewShoppingCartIntent = new Intent(getBaseContext(), EditBookActivity.class);

            // add book to the intent and send to EDIT Book activity
            viewShoppingCartIntent.putExtra(Book.BOOK_IDENTIFIER, book);
            // add bookShoppingCartModel and send to EDIT Book activity
            viewShoppingCartIntent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
            // add position to the intend and send to EDIT book activity
            viewShoppingCartIntent.putExtra(SELECT_POSITION, position);
            // Sending book, bookShoppingCartModel and pos of book to the Edit book activity
            startActivityForResult(viewShoppingCartIntent, EDIT_BOOK_REQUEST);
        }
    }

    //EVENT HANDLERS
    /**
     * Handles the Add book to cart button click by adding the book at the position in the models catalog to
     * the models cart.
     *
     * @author sruiz
     */
    class ButtonAddOnClickHandler implements OnClickListener {

        public void onClick(View v) {
            int position;
            View rowView;
            rowView = (View) v.getParent();
            position = listViewCatalog.getPositionForView(rowView);
            // Ask the model to set the book at this position to be selected=true
            // Remember to notify the listview if the data change so the view updates.
            bookShoppingCartModel.setSelectedForCart(position, true);
            ((CatalogListViewAdapter) listViewCatalog.getAdapter()).notifyDataSetChanged();

            // Added by student...DONE
            // Save the data if ADD button clicked
            saveToDB();
            // Enable button COMMIT and UNDO
            btnUndo.setEnabled(true);
            btnCommit.setEnabled(true);
        }
    }



    /**
     * Handles the Remove book from cart button click by removing the book at the position in the models catalog from
     * the models cart.
     *
     * @author sruiz
     */
    class ButtonRemoveOnClickHandler implements OnClickListener {

        public void onClick(View v) {
            int position;
            View rowView;
            rowView = (View) v.getParent();
            position = listViewCatalog.getPositionForView(rowView);
            // Ask the model to set the book at this position to be selected=false
            // Remember to notify the listview if the data change so the view updates.
            bookShoppingCartModel.setSelectedForCart(position, false);
            ((CatalogListViewAdapter) listViewCatalog.getAdapter()).notifyDataSetChanged();

            // Added by student...DONE
            // Save the data if REMOVE button clicked
            saveToDB();
            // Enable button COMMIT and UNDO
            btnUndo.setEnabled(true);
            btnCommit.setEnabled(true);
        }
    }

    /**
     * Handles the View Cart button click by using an explicit Intent to start the the book at the
     * BookShoppingCartActivity
     *
     * @author sruiz
     */
    class ButtonViewCartOnClickHandler implements OnClickListener {
        public void onClick(View v) {
            goingToChildActivity = true;
            // Use an Explicit Intent to start an Activity called BookShoppingCartActivity that will
            // display the books in the cart. There is no return result from the Activity we are starting,
            // it is just a view of the books in the cart.
            Intent viewShoppingCartIntent = new Intent(getBaseContext(), BookShoppingCartActivity.class);
            viewShoppingCartIntent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
            startActivity(viewShoppingCartIntent);
        }
    }

    class ButtonAddNewBookOnClickHandler implements OnClickListener {
        public void onClick(View v) {
            goingToChildActivity = true;
            btnUndo.setEnabled(true);
            btnCommit.setEnabled(true);

            Intent viewShoppingCartIntent = new Intent(getBaseContext(), NewBookActivity.class);
            viewShoppingCartIntent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
            startActivityForResult(viewShoppingCartIntent, NEW_BOOK_REQUEST);

        }
    }

    // EVENT HANDLER FOR EDIT BUTTON. IF NEED TO USE ONLY 1 EDIT BUTTON
//    class ButtonEditABookOnClickHandler implements OnClickListener {
//        public void onClick(View v) {
//            goingToChildActivity = true;
//            btnUndo.setEnabled(true);
//            btnCommit.setEnabled(true);
//            Intent viewShoppingCartIntent = new Intent(getBaseContext(), EditBookActivity.class);
//            viewShoppingCartIntent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
//            startActivityForResult(viewShoppingCartIntent, EDIT_BOOK_REQUEST);
//        }
//    }

    class ButtonDeleteExpensiveOnClickHandler implements OnClickListener {
        public void onClick(View v) {
            bookShoppingCartDB.deleteExpensive();
            loadFromDB();
            btnUndo.setEnabled(true);
            btnCommit.setEnabled(true);
        }
    }

    class ButtonCommitOnClickHandler implements OnClickListener {
        public void onClick(View v) {
            //End the current transaction with success (so data changes are saved permanently)
			//Then immediately mark the beginning of another Transaction
			//TODO - DONE
            bookShoppingCartDB.getDatabase().setTransactionSuccessful();
            bookShoppingCartDB.getDatabase().endTransaction();
            bookShoppingCartDB.getDatabase().beginTransaction();

            //Disable the Undo and Commit buttons no need for them until there has been another change
            btnUndo.setEnabled(false);
            btnCommit.setEnabled(false);
        }
    }

    class ButtonUndoOnClickHandler implements OnClickListener {
        public void onClick(View v) {

            //End the current transaction without success (so data changes are undone)
			//Then immediately mark the beginning of another Transaction
			//TODO - DONE
            bookShoppingCartDB.getDatabase().endTransaction();
            bookShoppingCartDB.getDatabase().beginTransaction();
			
            //Reload teh data from the DN after having undone the changes
            loadFromDB();
            //Disable the Undo and Commit buttons no need for them until there has been another change
            btnUndo.setEnabled(false);
            btnCommit.setEnabled(false);
        }
    }
}

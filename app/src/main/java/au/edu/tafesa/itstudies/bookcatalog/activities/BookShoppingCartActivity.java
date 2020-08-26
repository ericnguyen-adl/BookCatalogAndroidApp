package au.edu.tafesa.itstudies.bookcatalog.activities;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.widget.ArrayAdapter;

import au.edu.tafesa.itstudies.bookcatalog.models.Book;
import au.edu.tafesa.itstudies.bookcatalog.models.BookShoppingCartModel;

/**
 * Uses the Android provided ListActivity class to display the books in the cart in the BookShoppingCartModel passed
 * in by the Intent
 * @author sruiz
 *
 */
public class BookShoppingCartActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BookShoppingCartModel bookShoppingCartModel=null;
        Intent theIntent;
        // Retrieve the Intent and then the BookShoppingCartModel object passed in as bundled data ("Extra" data)
        // so we can pass that on to the ListActivities Adapter
        theIntent = this.getIntent();
        bookShoppingCartModel = (BookShoppingCartModel) theIntent.getSerializableExtra(BookShoppingCartModel.INTENT_IDENTIFIER);


        // If there is no Model passed in we create a default Model, which will show no data.
        if (bookShoppingCartModel == null) {
            bookShoppingCartModel = new BookShoppingCartModel();
        }

        ArrayAdapter<Book> adapter;
		// Could just create the "selected" list by traversing the Catalogue and using the isSelected field - 
		// no need to maintain the cart list separately
//        adapter = new ArrayAdapter<Book>(this, android.R.layout.simple_list_item_1, bookShoppingCartModel.getCart());
        adapter = new ArrayAdapter<Book>(this, android.R.layout.simple_list_item_1, bookShoppingCartModel.getListOfSelectedBooks());

        setListAdapter(adapter);
    }

}

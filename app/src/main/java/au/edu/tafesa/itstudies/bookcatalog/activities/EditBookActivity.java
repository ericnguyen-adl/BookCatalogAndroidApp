package au.edu.tafesa.itstudies.bookcatalog.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import au.edu.tafesa.itstudies.bookcatalog.R;
import au.edu.tafesa.itstudies.bookcatalog.models.Book;
import au.edu.tafesa.itstudies.bookcatalog.models.BookShoppingCartModel;

public class EditBookActivity extends Activity {

    private BookShoppingCartModel bookShoppingCartModel=null;

    private Button btnCancelEditABook;
    private Button btnEditABook;
    private EditText etTitle;
    private EditText etPrice;
    private int pos;


    private Book book;
    // private BookShoppingCartModel bookSCModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        Intent theIntent;
        // Retrieve the Intent and then the BookShoppingCartModel object passed in as bundled data ("Extra" data)
        // so we can pass that on to the ListActivities Adapter
        theIntent = this.getIntent();
        // Retrieve book, bookShoppingCartModel, pos of the book
        book = (Book) theIntent.getSerializableExtra(Book.BOOK_IDENTIFIER);
        bookShoppingCartModel = (BookShoppingCartModel) theIntent.getSerializableExtra(BookShoppingCartModel.INTENT_IDENTIFIER);
        pos = (int) theIntent.getSerializableExtra(BookCatalogActivity.SELECT_POSITION);

        etTitle = EditBookActivity.this.findViewById(R.id.etBookTitle);
        etPrice = EditBookActivity.this.findViewById(R.id.etBookPrice);

        // display the BOOK title and Price
        etTitle.setText(book.getTitle());
        etPrice.setText(Double.toString(book.getPrice()));

        btnCancelEditABook = this.findViewById(R.id.btnCancelEditABook);
        btnEditABook = this.findViewById(R.id.btnEditABook);

        //Event handler registration
        btnCancelEditABook.setOnClickListener(new HandleButtonCancelOnClick());
        btnEditABook.setOnClickListener(new HandleButtonEditABookOnClick());
    }

    private class HandleButtonEditABookOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Return the model with the added Book
            Book newBook;
            String title;
            double price;

            etTitle = EditBookActivity.this.findViewById(R.id.etBookTitle);
            etPrice = EditBookActivity.this.findViewById(R.id.etBookPrice);

            Intent intent = new Intent();

            if (etTitle.getText().toString().equals("")){
                Toast.makeText(EditBookActivity.this, "Illegal Title - cannnot be blank!", Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    // Create the newBook with current ID and SELECTED, new TITLE and PRICE, to replace the current book
                    int id = book.getId();
                    title = etTitle.getText().toString();
                    price = Double.parseDouble(etPrice.getText().toString());
                    boolean selected = book.isSelected();
                    newBook = new Book(id, title, price, selected);
                    // update the new book details
                    bookShoppingCartModel.getCatalog().set(pos, newBook);

                    // put book into the intent to send back to BookCatalogActivity
                    intent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
                    setResult(RESULT_OK, intent);
                    finish();

                } catch (NumberFormatException e) {
                    Toast.makeText(EditBookActivity.this, "Illegal Price", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class HandleButtonCancelOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Return the same model, unaltered
            Intent intent = new Intent();
            intent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}

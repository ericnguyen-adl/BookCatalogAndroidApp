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

public class NewBookActivity extends Activity {

    private BookShoppingCartModel bookShoppingCartModel=null;

    private Button btnCancel;
    private Button btnSaveNewBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        Intent theIntent;
        // Retrieve the Intent and then the BookShoppingCartModel object passed in as bundled data ("Extra" data)
        // so we can pass that on to the ListActivities Adapter
        theIntent = this.getIntent();
        bookShoppingCartModel = (BookShoppingCartModel) theIntent.getSerializableExtra(BookShoppingCartModel.INTENT_IDENTIFIER);
        btnCancel = this.findViewById(R.id.btnCancelNewBook);
        btnSaveNewBook = this.findViewById(R.id.btnSaveNewBook);

        //Event handler registration
        btnCancel.setOnClickListener(new HandleButtonCancelOnClick());
        btnSaveNewBook.setOnClickListener(new HandleButtonSaveNewBookOnClick());
    }

    //EVENT HANDLERS
    private class HandleButtonSaveNewBookOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //Return the model with the added Book
            Book newBook;
            String title;
            double price;
            EditText etTitle;
            EditText etPrice;

            etTitle = NewBookActivity.this.findViewById(R.id.etBookTitle);
            etPrice = NewBookActivity.this.findViewById(R.id.etBookPrice);

            Intent intent = new Intent();

            if (etTitle.getText().toString().equals("")){
                Toast.makeText(NewBookActivity.this, "Illegal Title - cannnot be blank!", Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    newBook = new Book(etTitle.getText().toString(), Double.parseDouble(etPrice.getText().toString()));
                    bookShoppingCartModel.getCatalog().add(newBook);
                    intent.putExtra(BookShoppingCartModel.INTENT_IDENTIFIER, bookShoppingCartModel);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (NumberFormatException e) {
                    Toast.makeText(NewBookActivity.this, "Illegal Price", Toast.LENGTH_LONG).show();
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

package au.edu.tafesa.itstudies.bookcatalog.models;

import java.io.Serializable;

public class Book implements Serializable {
	public static final String BOOK_IDENTIFIER = "EDIT_BOOK";
    public static final int DEFAULT_ID = -1;
    public static final boolean DEFAULT_SELECTED = false;
	private static final long serialVersionUID = 1L;

    private int id;
	private String title;
	private double price;
	private boolean selected;

	public Book(int id, String title, double price, boolean selected) {
		this.id =  id;
	    this.title = title;
		this.price = price;
		this.selected = selected;
	}

    public Book(int id, String title, double price) {
        this(id,title,price,DEFAULT_SELECTED);
    }

    public Book(String title,  double price) {
        this(DEFAULT_ID,title,price,DEFAULT_SELECTED);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(super.toString());
        builder.append(", id=");
        builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", price=$");
		builder.append(price);
		builder.append(", selected=");
		builder.append(selected);
		builder.append("]");
		return builder.toString();
	}

}

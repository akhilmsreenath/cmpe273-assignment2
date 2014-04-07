package edu.sjsu.cmpe.library.ui.views;

import java.util.ArrayList;
import java.util.List;

import com.yammer.dropwizard.views.View;

import edu.sjsu.cmpe.library.domain.Book;
//import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book_lost;

public class HomeView extends View {
    private final List<Book_lost> books;
    public HomeView(List<Book> books) {
              super("home.mustache");
              this.books = new ArrayList<Book_lost>();
              for (Book book : books) {
            	  Book_lost book_lost = new Book_lost();
            	  book_lost.setCategory_book(book.getCategory_book());
            	  book_lost.setCoverimage(book.getCoverimage());
            	  book_lost.setIsbn(book.getIsbn());
            	  book_lost.setStatus(book.getStatus());
            	  book_lost.setTitle(book.getTitle());
            	  book_lost.setLost(book.getStatus());   
            	  this.books.add(book_lost);
              }
              
    }
    public List<Book_lost> getBooks() {
              return books;
    }
}
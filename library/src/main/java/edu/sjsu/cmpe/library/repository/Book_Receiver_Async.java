package edu.sjsu.cmpe.library.repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.JMSException;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;

public class Book_Receiver_Async implements MessageListener {
	LibraryServiceConfiguration config;
	BookRepositoryInterface bookRepository;
	Book newbook=new Book();
	public Book_Receiver_Async(LibraryServiceConfiguration config, BookRepositoryInterface bookRepository_a)
			throws JMSException {
		this.config = config;
		this.bookRepository = bookRepository_a;
	
	}

	/**
	 * This method is called asynchronously by JMS when a message arrives at the
	 * queue. Client applications must not throw any exceptions in the onMessage
	 * method.
	 * 
	 * @param message
	 *            A JMS message.
	 */
	public void onMessage(Message message) {
		TextMessage msg = (TextMessage) message;
		try {
			System.out.println("received:::: " + msg.getText());
		} catch (JMSException ex) {
			ex.printStackTrace();
		}
	}

	public void listernerMsg() throws JMSException, MalformedURLException {
		
		ArrayList<String> receivedBooks = new ArrayList<String>();

		long isbn;
		String book_T;
		String book_c;
		URL web_Url;
		Book received_BookItems;
		String user = "admin";
		String password = "password";
		String host = "54.215.133.131";
		int port = Integer.parseInt("61613");

		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + host + ":" + port);

		while (true) {

			Connection connection = factory.createConnection(user, password);

			connection.start();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination dest = new StompJmsDestination(
					config.getStompTopicName());

			MessageConsumer consumer = session.createConsumer(dest);
			System.currentTimeMillis();
			 
			while (true) {
				Message msg = consumer.receive(500);
				if (msg == null)
					break;
				if (msg instanceof TextMessage) {
					String body = ((TextMessage) msg).getText();
					System.out.println("Received message =  " + body);
					receivedBooks.add(body);
				} else {
					System.out.println("unexpected msg " + msg.getClass());
				}
			}
			connection.close();
			if(!receivedBooks.isEmpty()){
			for (String books : receivedBooks) 
			{
				System.out.println("reachable");
				isbn = Long.parseLong(books.split(":\"")[0]);
				book_T = books.split(":\"")[1].replaceAll("^\"|\"$", "");
				book_c = books.split(":\"")[2].replaceAll("^\"|\"$", "");
				String str = books.split(":\"")[3];
				str = str.substring(0,str.length()-1);
				web_Url=new URL(str);
				System.out.println(web_Url);
				received_BookItems = bookRepository.getBookByISBN(isbn);
				
				if(received_BookItems.getIsbn()==0){
					System.out.println("addding newly added book");
					received_BookItems.setIsbn(isbn);
					received_BookItems.setCategory_book(book_c);
					received_BookItems.setCoverimage(web_Url);
					received_BookItems.setStatus(Status.available);
					received_BookItems.setTitle(book_T);
				
					bookRepository.add(received_BookItems);
					System.out.println("new book received is  "+ received_BookItems.toString());
				}
				else
					received_BookItems.setStatus(Status.available);
				

			}
			receivedBooks.clear();
		}

	}
	}
}
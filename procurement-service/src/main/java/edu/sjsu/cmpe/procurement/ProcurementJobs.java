package edu.sjsu.cmpe.procurement;

import java.util.ArrayList;


import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;



@Every("5s")
public class ProcurementJobs extends Job{
	@Override
	public void doJob() {
		
		System.out.println("came into dojob()");
		
		ProcurementJobs a=new ProcurementJobs();
		try{
			System.out.println("making job for consumer");
			String str = a.consumer_jobs();
			if(str!=null)
			{
				
				a.doposting(str);
			}
			ArrayList<String>books=a.getBooksFromPulisher();
			System.out.println("in try printing books arraylist "+books.toString());
			a.publisher(books);
		}
		catch(Exception e){e.printStackTrace();}
    }

		
	public String consumer_jobs() throws JMSException{

		String user ="admin";
		String password ="password";
		String host = "54.215.133.131";
		int port = Integer.parseInt("61613");
		//String destination = arg(args, 0, queue);
		System.out.println("executing consumer");
		StompJmsConnectionFactory factory1 = new StompJmsConnectionFactory();
		factory1.setBrokerURI("tcp://" + host + ":" + port);

		Connection connection = factory1.createConnection(user, password);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dest = new StompJmsDestination("/queue/64762.book.orders");//destination);

		MessageConsumer consumer = session.createConsumer(dest);
		//System.out.println(.toString());

		System.out.println("Waiting for messages from " + "/queue/64762.book.orders" + "...");
		//System.out.println(consumer.receive(System.currentTimeMillis()));
		//Message time=consumer.receive(System.currentTimeMillis());
		String temp1="";
		while(true) {
		    Message msg = consumer.receive(5000);
		    if(msg==null)
		    	break;
		    
		    if( msg instanceof  TextMessage ) {
			String body = ((TextMessage) msg).getText();
			
			System.out.println("Received message = " + body);
			
			String temp=body;
			String isbn=temp.split(":")[1];
			
			temp1=temp1+isbn+",";
			System.out.println("temp 1 is "+temp1);
			if( "SHUTDOWN".equals(body)) {
			    break;
			}

		    } else if (msg instanceof StompJmsMessage) {
			StompJmsMessage smsg = ((StompJmsMessage) msg);
			String body = smsg.getFrame().contentAsString();
			if ("SHUTDOWN".equals(body)) {
			    break;
			}
			System.out.println("Received message = " + body);

		    } else {
			System.out.println("Unexpected message type: "+msg.getClass());
		    }
		}
		connection.close();
		String jsonIsbn="";
		if(temp1==""){
			return null;
		}

		jsonIsbn=temp1;
		System.out.println("jsonisbn "+jsonIsbn);
		jsonIsbn=temp1.substring(0,temp1.length()-1);
		System.out.println("jsonisbn is "+jsonIsbn);
		return jsonIsbn;
	    }

	
	void doposting(String str)
	{
		try {
			System.out.println("inside post method");
			Client client = Client.create();
			WebResource webResource = client
					.resource("http://54.215.133.131:9000/orders");
			String input="{\"id\" :\"64762\",\"order_book_isbns\":["+str+"]}";
			ClientResponse response = webResource.type("application/json")
					.post(ClientResponse.class, input);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatus());
			}
			System.out.println("Output from Server .... \n");
			String output = response.getEntity(String.class);
			System.out.println(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    	public ArrayList<String> getBooksFromPulisher() throws JSONException

    	{
    		ArrayList<String> library=new ArrayList<String>();
    		
    			Client client = Client.create();
    			//WebResource webResource = client
    			//		.resource("http://localhost:8001/library/v1/books");
    			System.out.println("in side try");
    			WebResource webResource=client.resource("http://54.215.133.131:9000/orders/64762");
    			ClientResponse response = webResource.accept("application/json")
    					.get(ClientResponse.class);
    			if (response.getStatus() != 200) {
    				throw new RuntimeException("Failed : HTTP error code : "
    						+ response.getStatus());
    			}
    			String output = response.getEntity(String.class);
    			System.out.println("Output from Server .... \n");
    			System.out.println(output);
    			JSONObject obj=new JSONObject(output);
    			JSONArray shipping=obj.getJSONArray("shipped_books");
    			int n=shipping.length();
    			
    			for(int i=0;i<n;i++)
    			{
    				JSONObject getbooks=shipping.getJSONObject(i);
    				System.out.println("isbn is "+getbooks.getLong("isbn"));
    				System.out.println("title is "+getbooks.getString("title"));
    				System.out.println("category is "+getbooks.getString("category"));
    				System.out.println("coverimage is "+getbooks.getString("coverimage"));
    				//123:”Restful Web Services”:”computer”:”http://goo.gl/ZGmzoJ”  
    				String str=""+getbooks.getLong("isbn")+":\""+getbooks.getString("title")+ "\""+":\""+getbooks.getString("category")+"\""+":\""+getbooks.getString("coverimage")+"\"";
    				System.out.println(str);
    				library.add(str);
    			}
    		return library;
    	}
    	

    	public void publisher(ArrayList<String>books) throws JMSException
    	{
    		String user = "admin";
    		String password = "password";
    		String host = "54.215.133.131";
    		int port = Integer.parseInt("61613");
    		String destinationa = "/topic/64762.book.all";
    		String destinationb="/topic/64762.book.computer";

    		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
    		factory.setBrokerURI("tcp://" + host + ":" + port);

    		Connection connection = factory.createConnection(user, password);
    		connection.start();
    		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    		
    		Destination destLibrarya = new StompJmsDestination(destinationa);
    		MessageProducer producera = session.createProducer(destLibrarya);
    		
    		Destination destLibraryb = new StompJmsDestination(destinationb);
    		MessageProducer producerb = session.createProducer(destLibraryb);
    		
    		String data;
    		for(int i=0;i<books.size();i++)
    		{
    			data=books.get(i);
    			TextMessage msg=session.createTextMessage(data);
        		msg.setLongProperty("id", System.currentTimeMillis());
    			producera.send(msg);
				System.out.println("Sent msg for producer_a"+msg);
    			if(data.split(":")[2].equals("\"computer\""))
    			{
    				producerb.send(msg);
    				System.out.println("Sent msg for producer_b"+msg);
    			}
    		}
       		/**
    		 * Notify all Listeners to shut down. if you don't signal them, they
    		 * will be running forever.
    		 */
    		//producer.send(session.createTextMessage("SHUTDOWN"));
    		connection.close();
    	    }
    	}


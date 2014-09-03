package com.opengamma.reportingframework;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.beans.ser.JodaBeanSer;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.reportingframework.EngineControllerRequest;

public class SecurityProducer {
	
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static String subject = "TESTQUEUE";
	private MessageProducer _producer;
	private Session _session;
	
	

	public static void main(String[] args) {
		try {
			//TODO remove after demo
			//ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd \"C:\\Program Files\\MongoDB\\bin\" && mongoimport -d test -host devsvr-lx-7 -c FullSecurityDataCsv --type csv --file C:\\Users\\Ankit\\Desktop\\In\\securitydata.csv --headerline");
		    //Process p = builder.start();
		    //p.waitFor();
		    //System.exit(0);
			
			SecurityProducer securityProducer = new SecurityProducer();
			securityProducer.connectToActivemq();
			
			while(true) {
				List<ManageableSecurity> inputs = securityProducer.loadTrades(securityProducer.getFileName(args, "-fileName"));
				if(inputs == null) {
					Thread.sleep(500);
					continue;
				}
				securityProducer.sendMessages(inputs);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessages(List<ManageableSecurity> securities) {
		//TODO read and add view and marketdata to run risk against
		EngineControllerRequest request = new EngineControllerRequest(securities, "", "");  
		//for(ManageableSecurity security : securities) {
		try {
			String xmlMessage = JodaBeanSer.COMPACT.xmlWriter().write(request); //TODO use binWriter() to send as binary
			TextMessage message = _session.createTextMessage(xmlMessage);
			_producer.send(message);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		//}
	}
	
	
	public void connectToActivemq() {
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
	        Connection connection = connectionFactory.createConnection();
	        connection.start();

	        // JMS messages are sent and received using a Session. We will
	        // create here a non-transactional session object. If you want
	        // to use transactions you should set the first parameter to 'true'
	        _session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	        // Destination represents here our queue 'TESTQUEUE' on the
	        // JMS server. You don't have to do anything special on the
	        // server to create it, it will be created automatically.
	        Destination destination = _session.createQueue(subject);

	        // MessageProducer is used for sending messages (as opposed
	        // to MessageConsumer which is used for receiving them)
	        _producer = _session.createProducer(destination);

	        // We will send a small text message saying 'Hello' in Japanese
	        //TextMessage message = _session.createTextMessage("Hello ActiveMQ");

	        // Here we are sending the message!
	        //_producer.send(message);
	        //System.out.println("Sent message '" + message.getText() + "'");

	        //connection.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<ManageableSecurity> loadTrades(String fileName) {
		SecurityLoaderTest securityLoader = new SecurityLoaderTest();
		List<ManageableSecurity> inputs = securityLoader.execute(fileName);
		return inputs;
	}
	
	public String getFileName(String[] args, String attribute) {
		String fileName = null;
		for(int i=0; i<args.length; i++) {
			if(attribute.equals("-fileName")) {
		    	//System.out.println("User name: " + args[i+1]);
		    	fileName = args[i+1];
		    	break;
			}
		}
		return fileName;
	}
}
